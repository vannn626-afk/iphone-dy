package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ConnectivityType
import com.example.data.IslandConfiguration
import com.example.data.IslandPayload
import com.example.data.IslandPresetEntity
import com.example.data.SystemTelemetrySnapshot
import com.example.manager.IslandStateCoordinator
import com.example.ui.components.IosActionChip
import com.example.ui.components.IosCardGroup
import com.example.ui.components.IosRowDivider
import com.example.ui.components.IosSectionHeader
import com.example.ui.components.IosSliderRow
import com.example.ui.components.IosToggleRow
import com.example.ui.theme.LeovanBlack
import com.example.ui.theme.LeovanBorder
import com.example.ui.theme.LeovanCardPrimary
import com.example.ui.theme.LeovanCardSecondary
import com.example.ui.theme.LeovanTextPrimary
import com.example.ui.theme.LeovanTextSecondary
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SystemFeaturesScreen(
    config: IslandConfiguration,
    telemetry: SystemTelemetrySnapshot,
    primaryPayload: IslandPayload,
    secondaryPayload: IslandPayload?,
    onUpdateConfig: ((IslandConfiguration) -> IslandConfiguration) -> Unit,
    onSetMediaVolume: (Int) -> Unit
) {
    var selectedTimerMinutes by remember { mutableIntStateOf(3) }
    val activeTimer = (primaryPayload as? IslandPayload.TimerActivity)
        ?: (secondaryPayload as? IslandPayload.TimerActivity)
    val activeStopwatch = (primaryPayload as? IslandPayload.StopwatchActivity)
        ?: (secondaryPayload as? IslandPayload.StopwatchActivity)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Utilitas Timer & Stopwatch Langsung di Dynamic Island
        IosSectionHeader(
            title = "Timer & Stopwatch Dynamic Island",
            subtitle = "Jalankan hitung mundur atau stopwatch yang langsung tampil di kapsul atas (mendukung Split Pill bersama musik)."
        )
        IosCardGroup {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Timer Hitung Mundur",
                            style = MaterialTheme.typography.titleSmall,
                            color = LeovanTextPrimary
                        )
                        Text(
                            text = if (activeTimer != null) {
                                "Berjalan: ${formatSec(activeTimer.remainingSeconds)} tersisa"
                            } else {
                                "Pilih durasi dan mulai hitung mundur di Island"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (activeTimer != null) StatusOrange else LeovanTextSecondary
                        )
                    }
                    if (activeTimer != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = { IslandStateCoordinator.toggleTimerPauseResume() }) {
                                Icon(
                                    imageVector = if (activeTimer.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Jeda/Lanjut",
                                    tint = StatusOrange
                                )
                            }
                            IconButton(onClick = { IslandStateCoordinator.stopAndClearTimer() }) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Hentikan",
                                    tint = StatusRed
                                )
                            }
                        }
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 3, 5, 10, 25).forEach { mins ->
                        IosActionChip(
                            label = "$mins Menit",
                            isSelected = selectedTimerMinutes == mins,
                            onClick = {
                                selectedTimerMinutes = mins
                                IslandStateCoordinator.startOrUpdateTimer(
                                    totalSeconds = mins * 60,
                                    label = "Timer $mins Menit"
                                )
                            }
                        )
                    }
                }

                IosRowDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Stopwatch Presisi",
                            style = MaterialTheme.typography.titleSmall,
                            color = LeovanTextPrimary
                        )
                        Text(
                            text = if (activeStopwatch != null) {
                                "Putaran ${activeStopwatch.lapCount} • ${(activeStopwatch.elapsedMillis / 1000.0)} dtk"
                            } else {
                                "Hitung waktu aktivitas secara real-time"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (activeStopwatch != null) StatusGreen else LeovanTextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IosActionChip(
                            label = if (activeStopwatch?.isRunning == true) "Jeda" else "Mulai",
                            icon = if (activeStopwatch?.isRunning == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                            onClick = {
                                if (activeStopwatch?.isRunning == true) {
                                    IslandStateCoordinator.pauseStopwatch()
                                } else {
                                    IslandStateCoordinator.startOrResumeStopwatch()
                                }
                            },
                            testTag = "stopwatch_start_pause_button"
                        )
                        if (activeStopwatch != null) {
                            IosActionChip(
                                label = "Reset",
                                icon = Icons.Default.Refresh,
                                onClick = { IslandStateCoordinator.resetAndClearStopwatch() }
                            )
                        }
                    }
                }
            }
        }

        // 2. Status Baterai & Pengisian Daya
        IosSectionHeader(
            title = "Status Baterai & Pengisian Daya",
            subtitle = "Pantau persentase baterai, pengisian daya cepat, dan peringatan baterai penuh melalui API BatteryManager resmi."
        )
        IosCardGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${telemetry.batteryPercentage}% • ${telemetry.powerSource}",
                        style = MaterialTheme.typography.titleMedium,
                        color = StatusGreen
                    )
                    Text(
                        text = "Suhu Baterai: ${telemetry.batteryTempC}°C • ${if (telemetry.isCharging) "Sedang Mengisi Daya" else "Menggunakan Baterai"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LeovanTextSecondary
                    )
                }
                IosActionChip(
                    label = "Tampilkan di Island",
                    icon = Icons.Default.BatteryChargingFull,
                    onClick = {
                        IslandStateCoordinator.triggerSimulatedBatteryCharging(
                            percentage = telemetry.batteryPercentage,
                            isFull = telemetry.isBatteryFull
                        )
                    }
                )
            }
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.BatteryFull,
                title = "Indikator Status Baterai",
                subtitle = "Tampilkan persentase baterai saat berubah signifikan",
                checked = config.batteryAlertEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(batteryAlertEnabled = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.BatteryChargingFull,
                title = "Animasi Saat Charger Terhubung",
                subtitle = "Munculkan animasi pengisian daya seperti iPhone saat kabel dicolokkan",
                checked = config.chargingIndicatorEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(chargingIndicatorEnabled = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.CheckCircle,
                title = "Peringatan Pengisian Daya Penuh (100%)",
                subtitle = "Beri tahu di Dynamic Island ketika baterai telah terisi penuh",
                checked = config.fullChargeAlertEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(fullChargeAlertEnabled = enabled) } }
            )
        }

        // 3. Kontrol Volume Media Sistem
        IosSectionHeader(
            title = "Volume & Audio Sistem",
            subtitle = "Terhubung langsung ke AudioManager Android. Geser untuk mengubah volume media perangkat dan memunculkan indikator di Island."
        )
        IosCardGroup {
            IosSliderRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Volume Media Perangkat",
                subtitle = "Level ${telemetry.mediaVolume} dari ${telemetry.maxMediaVolume}",
                valueText = "${((telemetry.mediaVolume * 100f) / telemetry.maxMediaVolume.coerceAtLeast(1)).toInt()}%",
                value = telemetry.mediaVolume.toFloat(),
                valueRange = 0f..telemetry.maxMediaVolume.coerceAtLeast(1).toFloat(),
                onValueChange = { newVol -> onSetMediaVolume(newVol) },
                testTag = "system_volume_slider"
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Tampilkan Perubahan Volume di Island",
                subtitle = "Munculkan bar progres volume di kapsul saat tombol volume ditekan",
                checked = config.volumeIndicatorEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(volumeIndicatorEnabled = enabled) } }
            )
        }

        // 4. Konektivitas, Papan Klip & Perekaman Layar
        IosSectionHeader(
            title = "Konektivitas & Indikator Aktivitas Lainnya",
            subtitle = "Semua indikator mendukung tampilan ringkas (Compact) maupun diperluas (Expanded). Catatan: Android membatasi pengubahan Wi-Fi/Hotspot tanpa interaksi pengguna demi keamanan."
        )
        IosCardGroup {
            IosToggleRow(
                icon = Icons.Default.Bluetooth,
                title = "Indikator Koneksi Bluetooth",
                subtitle = "Status saat ini: ${telemetry.bluetoothDeviceName}",
                checked = config.bluetoothIndicatorEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(bluetoothIndicatorEnabled = enabled) } },
                iconTint = StatusCyan
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.Wifi,
                title = "Indikator Status Wi-Fi",
                subtitle = "Status saat ini: ${telemetry.wifiSsid}",
                checked = config.wifiIndicatorEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(wifiIndicatorEnabled = enabled) } },
                iconTint = StatusCyan
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.WifiTethering,
                title = "Indikator Hotspot Pribadi",
                subtitle = "Tampilkan kapsul saat berbagi koneksi internet aktif",
                checked = config.hotspotIndicatorEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(hotspotIndicatorEnabled = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.ContentPaste,
                title = "Indikator Papan Klip (Clipboard)",
                subtitle = "Beri tahu saat teks berhasil disalin ke papan klip",
                checked = config.clipboardIndicatorEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(clipboardIndicatorEnabled = enabled) } },
                iconTint = StatusGreen
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.FiberManualRecord,
                title = "Indikator Perekaman Layar",
                subtitle = "Tampilkan titik merah dan penghitung waktu saat merekam layar",
                checked = config.screenRecordingIndicatorEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(screenRecordingIndicatorEnabled = enabled) } },
                iconTint = StatusRed
            )
            IosRowDivider()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Uji Tampilan Indikator Sistem di Island",
                    style = MaterialTheme.typography.labelMedium,
                    color = LeovanTextSecondary
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IosActionChip(
                        label = "Wi-Fi",
                        icon = Icons.Default.Wifi,
                        onClick = { IslandStateCoordinator.triggerSimulatedConnectivity(ConnectivityType.WIFI, true) }
                    )
                    IosActionChip(
                        label = "Hotspot",
                        icon = Icons.Default.WifiTethering,
                        onClick = { IslandStateCoordinator.triggerSimulatedConnectivity(ConnectivityType.HOTSPOT, true) }
                    )
                    IosActionChip(
                        label = "Rekam Layar",
                        icon = Icons.Default.FiberManualRecord,
                        onClick = { IslandStateCoordinator.triggerSimulatedScreenRecording(true) }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalizationScreen(
    config: IslandConfiguration,
    presets: List<IslandPresetEntity>,
    onUpdateConfig: ((IslandConfiguration) -> IslandConfiguration) -> Unit,
    onApplyPreset: (IslandPresetEntity) -> Unit,
    onSaveCustomPreset: (String, String) -> Unit,
    onDeleteCustomPreset: (Int) -> Unit,
    onResetDefaults: () -> Unit
) {
    var showSavePresetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Profil Preset Tampilan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IosSectionHeader(
                title = "Profil Preset Tampilan",
                subtitle = "Terapkan gaya ukuran dan animasi dengan satu ketukan atau simpan profil kustom Anda.",
                modifier = Modifier.weight(1f)
            )
            Surface(
                onClick = { showSavePresetDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = LeovanCardSecondary,
                modifier = Modifier
                    .border(1.dp, LeovanBorder, RoundedCornerShape(12.dp))
                    .testTag("save_preset_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Simpan Preset",
                        tint = LeovanTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Simpan Preset",
                        style = MaterialTheme.typography.labelMedium,
                        color = LeovanTextPrimary
                    )
                }
            }
        }

        IosCardGroup {
            presets.forEachIndexed { index, preset ->
                if (index > 0) IosRowDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onApplyPreset(preset) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = LeovanTextPrimary
                        )
                        Text(
                            text = preset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = LeovanTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${preset.compactWidthDp}×${preset.compactHeightDp} dp • Radius ${preset.cornerRadiusDp} dp • ${preset.animationDurationMs} ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusGreen
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!preset.isBuiltIn) {
                            IconButton(onClick = { onDeleteCustomPreset(preset.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Hapus Preset",
                                    tint = LeovanTextSecondary
                                )
                            }
                        }
                        Surface(
                            onClick = { onApplyPreset(preset) },
                            shape = RoundedCornerShape(10.dp),
                            color = LeovanCardSecondary,
                            modifier = Modifier.border(1.dp, LeovanBorder, RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = "Pakai",
                                style = MaterialTheme.typography.labelMedium,
                                color = LeovanTextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Efek Visual, Transparansi & Auto-Hide
        IosSectionHeader(
            title = "Transparansi, Auto-Hide & Interaksi",
            subtitle = "Atur opasitas kapsul, garis tepi, waktu sembunyi otomatis, serta respons getaran."
        )
        IosCardGroup {
            IosSliderRow(
                icon = Icons.Default.Opacity,
                title = "Tingkat Kepekatan Kapsul (Opasitas)",
                subtitle = "100% = Hitam Solid Pekat (#000000) terbaik untuk semua panel layar",
                valueText = "${config.opacityPercent}%",
                value = config.opacityPercent.toFloat(),
                valueRange = 60f..100f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(opacityPercent = newVal) } }
            )
            IosRowDivider()
            IosSliderRow(
                icon = Icons.Default.RoundedCorner,
                title = "Radius Sudut Kapsul",
                valueText = "${config.cornerRadiusDp} dp",
                value = config.cornerRadiusDp.toFloat(),
                valueRange = 12f..48f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(cornerRadiusDp = newVal) } }
            )
            IosRowDivider()
            IosSliderRow(
                icon = Icons.Default.Speed,
                title = "Kecepatan Animasi",
                valueText = "${config.animationDurationMs} ms",
                value = config.animationDurationMs.toFloat(),
                valueRange = 150f..800f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(animationDurationMs = newVal) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.VisibilityOff,
                title = "Sembunyikan Otomatis (Auto-Hide)",
                subtitle = "Kembalikan pop-up notifikasi ke mode ringkas setelah beberapa detik",
                checked = config.autoHideEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(autoHideEnabled = enabled) } }
            )
            if (config.autoHideEnabled) {
                IosRowDivider()
                IosSliderRow(
                    icon = Icons.Default.Timer,
                    title = "Durasi Tampil Sebelum Auto-Hide",
                    valueText = "${config.autoHideDurationSec} detik",
                    value = config.autoHideDurationSec.toFloat(),
                    valueRange = 2f..20f,
                    onValueChange = { newVal -> onUpdateConfig { it.copy(autoHideDurationSec = newVal) } }
                )
            }
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.BorderOuter,
                title = "Garis Tepi Kapsul Halus (#252525)",
                subtitle = "Memberikan definisi elegan saat berada di atas aplikasi gelap",
                checked = config.showBorderStroke,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(showBorderStroke = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.Swipe,
                title = "Geser (Swipe) untuk Menyembunyikan",
                subtitle = "Geser kapsul ke kiri atau kanan untuk menyembunyikan secara manual",
                checked = config.swipeToHideEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(swipeToHideEnabled = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.Vibration,
                title = "Umpan Balik Getar (Haptic Feedback)",
                subtitle = "Getaran halus saat mengetuk atau menahan kapsul Dynamic Island",
                checked = config.hapticFeedbackEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(hapticFeedbackEnabled = enabled) } }
            )
        }

        // Informasi Kompatibilitas Panel Layar
        IosSectionHeader(
            title = "Optimasi Semua Jenis Panel Layar",
            subtitle = "LEOVANISLAND menggunakan hitam solid #000000 tanpa ketergantungan khusus pada panel AMOLED."
        )
        IosCardGroup {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Panel yang Didukung Sepenuhnya:",
                    style = MaterialTheme.typography.titleSmall,
                    color = LeovanTextPrimary
                )
                Text(
                    text = "• AMOLED & Super AMOLED\n• OLED & POLED\n• IPS LCD\n• TFT LCD\n• PLS LCD\n• LTPS LCD",
                    style = MaterialTheme.typography.bodySmall,
                    color = LeovanTextSecondary
                )
            }
        }

        // Tombol Pulihkan Pengaturan Awal
        Surface(
            onClick = onResetDefaults,
            shape = RoundedCornerShape(16.dp),
            color = LeovanCardPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(1.dp, LeovanBorder, RoundedCornerShape(16.dp))
                .testTag("personalize_reset_defaults_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Pulihkan Pengaturan Awal",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = StatusOrange
                )
            }
        }
    }

    if (showSavePresetDialog) {
        var nameInput by remember { mutableStateOf("") }
        var descInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            containerColor = LeovanCardPrimary,
            titleContentColor = LeovanTextPrimary,
            textContentColor = LeovanTextSecondary,
            title = { Text("Simpan Profil Preset Tampilan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Preset (mis. Mode Malam Saya)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LeovanTextPrimary,
                            unfocusedBorderColor = LeovanBorder,
                            focusedTextColor = LeovanTextPrimary,
                            unfocusedTextColor = LeovanTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Deskripsi Singkat") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LeovanTextPrimary,
                            unfocusedBorderColor = LeovanBorder,
                            focusedTextColor = LeovanTextPrimary,
                            unfocusedTextColor = LeovanTextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            onSaveCustomPreset(nameInput, descInput)
                            showSavePresetDialog = false
                        }
                    }
                ) {
                    Text("Simpan", color = LeovanTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Batal", color = LeovanTextSecondary)
                }
            }
        )
    }
}

private fun formatSec(sec: Int): String {
    val c = sec.coerceAtLeast(0)
    return String.format(Locale.getDefault(), "%02d:%02d", c / 60, c % 60)
}
