package com.example.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import com.example.data.ConnectivityType
import com.example.data.IslandPayload
import com.example.data.SystemTelemetrySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SystemActivityMonitor(private val context: Context) {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val clipboardManager = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val _telemetry = MutableStateFlow(readInitialTelemetry())
    val telemetry: StateFlow<SystemTelemetrySnapshot> = _telemetry.asStateFlow()

    private var isRegistered = false
    private var lastChargingState: Boolean? = null

    private val systemReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                Intent.ACTION_BATTERY_CHANGED -> handleBatteryIntent(intent)
                Intent.ACTION_POWER_CONNECTED -> {
                    handlePowerConnectionChange(isConnected = true)
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    handlePowerConnectionChange(isConnected = false)
                }
                "android.media.VOLUME_CHANGED_ACTION",
                AudioManager.RINGER_MODE_CHANGED_ACTION -> {
                    handleVolumeChanged()
                }
                BluetoothAdapter.ACTION_STATE_CHANGED,
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    handleBluetoothIntent(intent)
                }
                WifiManager.WIFI_STATE_CHANGED_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                    handleWifiChanged()
                }
                "android.net.wifi.WIFI_AP_STATE_CHANGED" -> {
                    handleHotspotIntent(intent)
                }
            }
        }
    }

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        val cfg = IslandStateCoordinator.configuration.value
        if (!cfg.clipboardIndicatorEnabled) return@OnPrimaryClipChangedListener
        runCatching {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).coerceToText(appContext)?.toString()?.trim().orEmpty()
                if (text.isNotEmpty()) {
                    val preview = if (text.length > 60) text.take(60) + "…" else text
                    IslandStateCoordinator.postPayload(
                        IslandPayload.ClipboardCopied(
                            previewText = preview,
                            charCount = text.length
                        )
                    )
                }
            }
        }
    }

    fun startMonitoring() {
        if (isRegistered) return
        runCatching {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction("android.media.VOLUME_CHANGED_ACTION")
                addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                addAction("android.net.wifi.WIFI_AP_STATE_CHANGED")
            }
            appContext.registerReceiver(systemReceiver, filter)
            clipboardManager.addPrimaryClipChangedListener(clipboardListener)
            isRegistered = true
        }
    }

    fun stopMonitoring() {
        if (!isRegistered) return
        runCatching {
            appContext.unregisterReceiver(systemReceiver)
            clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        }
        isRegistered = false
    }

    fun refreshDisplayMetrics() {
        val dm = appContext.resources.displayMetrics
        val config = appContext.resources.configuration
        val widthDp = (dm.widthPixels / dm.density).toInt()
        val heightDp = (dm.heightPixels / dm.density).toInt()
        val isPortrait = config.orientation != Configuration.ORIENTATION_LANDSCAPE
        _telemetry.update {
            it.copy(
                screenWidthDp = widthDp,
                screenHeightDp = heightDp,
                densityDpi = dm.densityDpi,
                isPortrait = isPortrait
            )
        }
    }

    fun adjustMediaVolume(newVolume: Int) {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val clamped = newVolume.coerceIn(0, maxVol)
        runCatching {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, clamped, 0)
        }
        _telemetry.update {
            it.copy(
                mediaVolume = clamped,
                maxMediaVolume = maxVol,
                isMuted = clamped == 0
            )
        }
        if (IslandStateCoordinator.configuration.value.volumeIndicatorEnabled) {
            IslandStateCoordinator.postPayload(
                IslandPayload.VolumeChange(
                    currentVolume = clamped,
                    maxVolume = maxVol,
                    streamLabel = "Volume Media",
                    isMuted = clamped == 0
                )
            )
        }
    }

    private fun handleBatteryIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 85

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val isFull = status == BatteryManager.BATTERY_STATUS_FULL || percentage >= 100

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val powerSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "Adaptor Daya AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "Port Daya USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Pengisian Nirkabel Qi"
            else -> "Baterai Internal"
        }

        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300)
        val tempC = tempTenths / 10f

        _telemetry.update {
            it.copy(
                batteryPercentage = percentage,
                isCharging = isCharging,
                isBatteryFull = isFull,
                batteryTempC = tempC,
                powerSource = powerSource
            )
        }

        val cfg = IslandStateCoordinator.configuration.value
        val previousCharging = lastChargingState
        lastChargingState = isCharging

        if (previousCharging != null && previousCharging != isCharging && cfg.chargingIndicatorEnabled) {
            IslandStateCoordinator.postPayload(
                IslandPayload.BatteryStatus(
                    percentage = percentage,
                    isCharging = isCharging,
                    isFull = isFull,
                    powerSourceLabel = powerSource,
                    temperatureCelsius = tempC
                )
            )
        } else if (isFull && cfg.fullChargeAlertEnabled && previousCharging == false) {
            IslandStateCoordinator.postPayload(
                IslandPayload.BatteryStatus(
                    percentage = 100,
                    isCharging = true,
                    isFull = true,
                    powerSourceLabel = "Baterai Terisi Penuh 100%",
                    temperatureCelsius = tempC
                )
            )
        }
    }

    private fun handlePowerConnectionChange(isConnected: Boolean) {
        val snap = _telemetry.value
        val cfg = IslandStateCoordinator.configuration.value
        if (cfg.chargingIndicatorEnabled) {
            IslandStateCoordinator.postPayload(
                IslandPayload.BatteryStatus(
                    percentage = snap.batteryPercentage,
                    isCharging = isConnected,
                    isFull = snap.batteryPercentage >= 100,
                    powerSourceLabel = if (isConnected) "Pengisian Daya Aktif" else "Pengisian Daya Dilepas",
                    temperatureCelsius = snap.batteryTempC
                )
            )
        }
    }

    private fun handleVolumeChanged() {
        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val isMuted = currentVol == 0
        val previous = _telemetry.value.mediaVolume
        _telemetry.update {
            it.copy(
                mediaVolume = currentVol,
                maxMediaVolume = maxVol,
                isMuted = isMuted
            )
        }
        if (currentVol != previous && IslandStateCoordinator.configuration.value.volumeIndicatorEnabled) {
            IslandStateCoordinator.postPayload(
                IslandPayload.VolumeChange(
                    currentVolume = currentVol,
                    maxVolume = maxVol,
                    streamLabel = "Volume Media",
                    isMuted = isMuted
                )
            )
        }
    }

    private fun handleBluetoothIntent(intent: Intent) {
        val cfg = IslandStateCoordinator.configuration.value
        when (intent.action) {
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                val enabled = state == BluetoothAdapter.STATE_ON
                _telemetry.update {
                    it.copy(
                        bluetoothEnabled = enabled,
                        bluetoothDeviceName = if (enabled) "Bluetooth Aktif" else "Nonaktif"
                    )
                }
                if (cfg.bluetoothIndicatorEnabled && (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_OFF)) {
                    IslandStateCoordinator.postPayload(
                        IslandPayload.ConnectivityStatus(
                            type = ConnectivityType.BLUETOOTH,
                            title = if (enabled) "Bluetooth Diaktifkan" else "Bluetooth Dinonaktifkan",
                            subtitle = if (enabled) "Siap menghubungkan perangkat audio" else "Koneksi nirkabel dimatikan",
                            isActive = enabled
                        )
                    )
                }
            }
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                _telemetry.update { it.copy(bluetoothEnabled = true, bluetoothDeviceName = "Perangkat Audio Terhubung") }
                if (cfg.bluetoothIndicatorEnabled) {
                    IslandStateCoordinator.postPayload(
                        IslandPayload.ConnectivityStatus(
                            type = ConnectivityType.BLUETOOTH,
                            title = "Perangkat Bluetooth Terhubung",
                            subtitle = "Audio Nirkabel Aktif",
                            isActive = true
                        )
                    )
                }
            }
        }
    }

    private fun handleWifiChanged() {
        val isWifiActive = isWifiConnectedNow()
        val prev = _telemetry.value.wifiConnected
        _telemetry.update {
            it.copy(
                wifiConnected = isWifiActive,
                wifiSsid = if (isWifiActive) "Jaringan Wi-Fi Terhubung" else "Tidak Terhubung"
            )
        }
        if (prev != isWifiActive && IslandStateCoordinator.configuration.value.wifiIndicatorEnabled) {
            IslandStateCoordinator.postPayload(
                IslandPayload.ConnectivityStatus(
                    type = ConnectivityType.WIFI,
                    title = if (isWifiActive) "Wi-Fi Terhubung" else "Wi-Fi Terputus",
                    subtitle = if (isWifiActive) "Koneksi internet nirkabel aktif" else "Menggunakan jaringan seluler",
                    isActive = isWifiActive
                )
            )
        }
    }

    private fun handleHotspotIntent(intent: Intent) {
        val state = intent.getIntExtra("wifi_state", 0)
        // 13 = WIFI_AP_STATE_ENABLED in Android WifiManager
        val isEnabled = state == 13
        val prev = _telemetry.value.hotspotEnabled
        _telemetry.update { it.copy(hotspotEnabled = isEnabled) }
        if (prev != isEnabled && IslandStateCoordinator.configuration.value.hotspotIndicatorEnabled) {
            IslandStateCoordinator.postPayload(
                IslandPayload.ConnectivityStatus(
                    type = ConnectivityType.HOTSPOT,
                    title = if (isEnabled) "Hotspot Pribadi Aktif" else "Hotspot Dinonaktifkan",
                    subtitle = if (isEnabled) "Membagikan koneksi internet" else "Hotspot dimatikan",
                    isActive = isEnabled
                )
            )
        }
    }

    private fun isWifiConnectedNow(): Boolean {
        return runCatching {
            val network = connectivityManager.activeNetwork ?: return false
            val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }.getOrDefault(wifiManager?.isWifiEnabled == true)
    }

    private fun readInitialTelemetry(): SystemTelemetrySnapshot {
        val dm = appContext.resources.displayMetrics
        val config = appContext.resources.configuration
        val widthDp = (dm.widthPixels / dm.density).toInt()
        val heightDp = (dm.heightPixels / dm.density).toInt()
        val currentVol = runCatching { audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) }.getOrDefault(10)
        val maxVol = runCatching { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }.getOrDefault(15).coerceAtLeast(1)

        return SystemTelemetrySnapshot(
            batteryPercentage = 85,
            isCharging = false,
            isBatteryFull = false,
            batteryTempC = 30.5f,
            powerSource = "Baterai Internal",
            wifiConnected = isWifiConnectedNow(),
            wifiSsid = if (isWifiConnectedNow()) "Jaringan Wi-Fi Aktif" else "Terputus",
            bluetoothEnabled = false,
            bluetoothDeviceName = "Siap Terhubung",
            hotspotEnabled = false,
            mediaVolume = currentVol,
            maxMediaVolume = maxVol,
            isMuted = currentVol == 0,
            screenWidthDp = widthDp,
            screenHeightDp = heightDp,
            densityDpi = dm.densityDpi,
            isPortrait = config.orientation != Configuration.ORIENTATION_LANDSCAPE
        )
    }
}
