package com.example.data

import android.app.PendingIntent
import android.app.RemoteInput
import android.graphics.Bitmap
import android.os.Bundle

enum class AnimationStyle(val labelId: String, val descriptionId: String) {
    SPRING("Pegas (Spring)", "Pantulan organik khas Dynamic Island iOS"),
    MORPH("Morfosis (Morph)", "Perubahan bentuk kapsul yang mulus dan menyatu"),
    FADE("Pudar Halus (Fade)", "Transisi opasitas lembut untuk perangkat hemat daya"),
    SLIDE("Geser Vertikal (Slide)", "Efek muncul dari bagian atas layar")
}

enum class NotchCompatibilityMode(val label: String, val subtitle: String, val recommendedY: Int) {
    PUNCH_HOLE_TENGAH("Punch-Hole Tengah", "Menyatu dengan lubang kamera tengah", 6),
    PUNCH_HOLE_KIRI("Punch-Hole Kiri/Kanan", "Menyesuaikan kamera sudut atas", 8),
    PONI_LEBAR("Notch / Poni Lebar", "Selaras dengan lekukan layar atas", 4),
    TANPA_PONI("Layar Tanpa Notch (Full)", "Mengambang elegan di bawah status bar", 10)
}

enum class IslandDisplayMode(val label: String) {
    COMPACT("Ringkas (Compact)"),
    SPLIT_PILL("Kapsul Ganda (Split Pill)"),
    EXPANDED("Diperluas (Expanded)"),
    HIDDEN("Tersembunyi")
}

data class IslandConfiguration(
    val isIslandEnabled: Boolean = true,
    // Ukuran & Posisi
    val compactWidthDp: Int = 128,
    val compactHeightDp: Int = 36,
    val expandedWidthDp: Int = 352,
    val expandedHeightDp: Int = 178,
    val offsetXDp: Int = 0,
    val offsetYDp: Int = 6,
    val cornerRadiusDp: Int = 28,
    val opacityPercent: Int = 100,
    val showBorderStroke: Boolean = true,
    val notchMode: NotchCompatibilityMode = NotchCompatibilityMode.PUNCH_HOLE_TENGAH,
    // Animasi & Interaksi
    val animationStyle: AnimationStyle = AnimationStyle.SPRING,
    val animationDurationMs: Int = 340,
    val autoHideEnabled: Boolean = true,
    val autoHideDurationSec: Int = 6,
    val splitPillEnabled: Boolean = true,
    val swipeToHideEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    // Smart Notification
    val notificationsEnabled: Boolean = true,
    val showMessagePreview: Boolean = true,
    val quickReplyEnabled: Boolean = true,
    val groupNotificationsEnabled: Boolean = true,
    val hideSensitiveWhenLocked: Boolean = true,
    val saveNotificationHistory: Boolean = true,
    // Universal Music Player
    val musicEnabled: Boolean = true,
    val showAlbumCover: Boolean = true,
    val showMusicControls: Boolean = true,
    val showMusicVisualizer: Boolean = true,
    // Fitur Sistem Android
    val batteryAlertEnabled: Boolean = true,
    val chargingIndicatorEnabled: Boolean = true,
    val fullChargeAlertEnabled: Boolean = true,
    val timerIslandEnabled: Boolean = true,
    val stopwatchIslandEnabled: Boolean = true,
    val clipboardIndicatorEnabled: Boolean = true,
    val bluetoothIndicatorEnabled: Boolean = true,
    val wifiIndicatorEnabled: Boolean = true,
    val hotspotIndicatorEnabled: Boolean = true,
    val volumeIndicatorEnabled: Boolean = true,
    val screenRecordingIndicatorEnabled: Boolean = true
)

data class NotificationActionItem(
    val title: String,
    val pendingIntent: PendingIntent? = null,
    val remoteInput: RemoteInput? = null,
    val resultKey: String? = null
)

sealed class IslandPayload(
    open val key: String,
    open val priority: Int,
    open val timestamp: Long = System.currentTimeMillis()
) {
    data object Idle : IslandPayload(key = "idle", priority = 0)

    data class NotificationItem(
        val id: String,
        val packageName: String,
        val appName: String,
        val senderName: String,
        val messageText: String,
        val timeFormatted: String,
        val groupCount: Int = 1,
        val canQuickReply: Boolean = false,
        val contentIntent: PendingIntent? = null,
        val actions: List<NotificationActionItem> = emptyList(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : IslandPayload(key = "notif_$id", priority = 90, timestamp = timestamp)

    data class MusicPlayback(
        val title: String,
        val artist: String,
        val album: String = "",
        val appName: String = "Pemutar Media",
        val packageName: String = "",
        val isPlaying: Boolean = true,
        val positionMs: Long = 84_000L,
        val durationMs: Long = 216_000L,
        val albumArt: Bitmap? = null,
        override val timestamp: Long = System.currentTimeMillis()
    ) : IslandPayload(key = "music", priority = 70, timestamp = timestamp) {
        val progressFraction: Float
            get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }

    data class TimerActivity(
        val totalSeconds: Int,
        val remainingSeconds: Int,
        val isRunning: Boolean,
        val label: String = "Timer",
        override val timestamp: Long = System.currentTimeMillis()
    ) : IslandPayload(key = "timer", priority = 80, timestamp = timestamp) {
        val progressFraction: Float
            get() = if (totalSeconds > 0) (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f) else 0f
    }

    data class StopwatchActivity(
        val elapsedMillis: Long,
        val isRunning: Boolean,
        val lapCount: Int = 0,
        override val timestamp: Long = System.currentTimeMillis()
    ) : IslandPayload(key = "stopwatch", priority = 65, timestamp = timestamp)

    data class BatteryStatus(
        val percentage: Int,
        val isCharging: Boolean,
        val isFull: Boolean,
        val powerSourceLabel: String = "USB-C Fast Charge",
        val temperatureCelsius: Float = 31.5f,
        override val timestamp: Long = System.currentTimeMillis()
    ) : IslandPayload(key = "battery", priority = 85, timestamp = timestamp)

    data class VolumeChange(
        val currentVolume: Int,
        val maxVolume: Int,
        val streamLabel: String = "Media",
        val isMuted: Boolean = false,
        override val timestamp: Long = System.currentTimeMillis()
    ) : IslandPayload(key = "volume", priority = 95, timestamp = timestamp) {
        val percent: Int
            get() = if (maxVolume > 0) ((currentVolume * 100f) / maxVolume).toInt().coerceIn(0, 100) else 0
    }

    data class ConnectivityStatus(
        val type: ConnectivityType,
        val title: String,
        val subtitle: String,
        val isActive: Boolean,
        override val timestamp: Long = System.currentTimeMillis()
    ) : IslandPayload(key = "conn_${type.name}", priority = 82, timestamp = timestamp)

    data class ClipboardCopied(
        val previewText: String,
        val charCount: Int,
        override val timestamp: Long = System.currentTimeMillis()
    ) : IslandPayload(key = "clipboard", priority = 88, timestamp = timestamp)

    data class ScreenRecordingStatus(
        val elapsedSeconds: Int,
        val isRecording: Boolean,
        override val timestamp: Long = System.currentTimeMillis()
    ) : IslandPayload(key = "screen_record", priority = 78, timestamp = timestamp)
}

enum class ConnectivityType(val label: String) {
    WIFI("Wi-Fi"),
    BLUETOOTH("Bluetooth"),
    HOTSPOT("Hotspot Pribadi")
}

data class SystemTelemetrySnapshot(
    val batteryPercentage: Int = 85,
    val isCharging: Boolean = false,
    val isBatteryFull: Boolean = false,
    val batteryTempC: Float = 30.0f,
    val powerSource: String = "Baterai Internal",
    val wifiConnected: Boolean = true,
    val wifiSsid: String = "Jaringan Wi-Fi Aktif",
    val bluetoothEnabled: Boolean = false,
    val bluetoothDeviceName: String = "Siap Terhubung",
    val hotspotEnabled: Boolean = false,
    val mediaVolume: Int = 10,
    val maxMediaVolume: Int = 15,
    val isMuted: Boolean = false,
    val screenWidthDp: Int = 392,
    val screenHeightDp: Int = 850,
    val densityDpi: Int = 420,
    val isPortrait: Boolean = true
)

data class PermissionStatusSnapshot(
    val canDrawOverlays: Boolean = false,
    val isNotificationListenerEnabled: Boolean = false,
    val isPostNotificationsGranted: Boolean = true
)
