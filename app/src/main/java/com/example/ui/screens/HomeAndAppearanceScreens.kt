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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AnimationStyle
import com.example.data.ConnectivityType
import com.example.data.IslandConfiguration
import com.example.data.IslandDisplayMode
import com.example.data.NotchCompatibilityMode
import com.example.data.PermissionStatusSnapshot
import com.example.data.SystemTelemetrySnapshot
import com.example.manager.IslandStateCoordinator
import com.example.manager.PermissionManager
import com.example.ui.components.IosActionChip
import com.example.ui.components.IosCardGroup
import com.example.ui.components.IosRowDivider
import com.example.ui.components.IosSectionHeader
import com.example.ui.components.IosSliderRow
import com.example.ui.components.IosToggleRow
import com.example.ui.components.StatusPillBadge
import com.example.ui.theme.LeovanBlack
import com.example.ui.theme.LeovanBorder
import com.example.ui.theme.LeovanCardPrimary
import com.example.ui.theme.LeovanCardSecondary
import com.example.ui.theme.LeovanTextPrimary
import com.example.ui.theme.LeovanTextSecondary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.viewmodel.LeovanSection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    config: IslandConfiguration,
    permissions: PermissionStatusSnapshot,
    telemetry: SystemTelemetrySnapshot,
    displayMode: IslandDisplayMode,
    isOverlayRunning: Boolean,
    onToggleIslandEnabled: (Boolean) -> Unit,
    onUpdateConfig: ((IslandConfiguration) -> IslandConfiguration) -> Unit,
    onNavigateToSection: (LeovanSection) -> Unit,
    onTriggerNotification: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Kartu Utama Status LEOVANISLAND
        val heroShape = RoundedCornerShape(24.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(heroShape)
                .background(LeovanCardPrimary)
                .border(1.dp, LeovanBorder, heroShape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LEOVANISLAND",
                        style = MaterialTheme.typography.headlineMedium,
                        color = LeovanTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dynamic Island Pintar untuk Android",
                        style = MaterialTheme.typography.bodySmall,
                        color = LeovanTextSecondary
                    )
                }
                StatusPillBadge(
                    label = if (config.isIslandEnabled) "Aktif" else "Nonaktif",
                    isPositive = config.isIslandEnabled
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMiniCard(
                    title = "Mode Tampilan",
                    value = displayMode.label,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Overlay Sistem",
                    value = if (isOverlayRunning) "Berjalan" else if (permissions.canDrawOverlays) "Siaga" else "Pratinjau",
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "Layar & Panel",
                    value = "${telemetry.screenWidthDp}×${telemetry.screenHeightDp} dp",
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                onClick = { onToggleIslandEnabled(!config.isIslandEnabled) },
                shape = RoundedCornerShape(16.dp),
                color = if (config.isIslandEnabled) LeovanTextPrimary else LeovanCardSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, if (config.isIslandEnabled) LeovanTextPrimary else LeovanBorder, RoundedCornerShape(16.dp))
                    .testTag("master_island_toggle_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Sakelar Island",
                        tint = if (config.isIslandEnabled) LeovanBlack else LeovanTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (config.isIslandEnabled) "Dynamic Island Aktif (Ketuk untuk Nonaktifkan)" else "Aktifkan LEOVANISLAND Sekarang",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (config.isIslandEnabled) LeovanBlack else LeovanTextPrimary
                    )
                }
            }
        }

        // Simulator & Pengendali Pratinjau Langsung
        IosSectionHeader(
            title = "Simulator & Uji Interaktif Langsung",
            subtitle = "Ketuk salah satu skenario di bawah untuk menguji animasi Dynamic Island di bagian atas layar secara instan."
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
                    Text(
                        text = "Kontrol Bentuk Kapsul",
                        style = MaterialTheme.typography.titleSmall,
                        color = LeovanTextPrimary
                    )
                    Text(
                        text = "Ketuk / Long-Press / Geser Pill",
                        style = MaterialTheme.typography.labelSmall,
                        color = LeovanTextSecondary
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IosActionChip(
                        label = if (displayMode == IslandDisplayMode.EXPANDED) "Ringkas (Compact)" else "Perluas (Expand)",
                        icon = Icons.Default.AspectRatio,
                        isSelected = displayMode == IslandDisplayMode.EXPANDED,
                        onClick = { IslandStateCoordinator.toggleExpandCollapse() },
                        testTag = "sim_expand_collapse_chip"
                    )
                    IosActionChip(
                        label = "Split Pill (Musik + Timer)",
                        icon = Icons.Default.SwapHoriz,
                        isSelected = displayMode == IslandDisplayMode.SPLIT_PILL,
                        onClick = { IslandStateCoordinator.triggerSimulatedSplitPillMusicAndTimer() },
                        testTag = "sim_split_pill_chip"
                    )
                    IosActionChip(
                        label = if (displayMode == IslandDisplayMode.HIDDEN) "Tampilkan Kembali" else "Sembunyikan Manual",
                        icon = if (displayMode == IslandDisplayMode.HIDDEN) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        isSelected = displayMode == IslandDisplayMode.HIDDEN,
                        onClick = {
                            if (displayMode == IslandDisplayMode.HIDDEN) {
                                IslandStateCoordinator.restoreFromHidden()
                            } else {
                                IslandStateCoordinator.hideManually()
                            }
                        },
                        testTag = "sim_hide_restore_chip"
                    )
                }

                IosRowDivider()

                Text(
                    text = "Simulasi Aktivitas Masuk",
                    style = MaterialTheme.typography.titleSmall,
                    color = LeovanTextPrimary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IosActionChip(
                        label = "Notifikasi WhatsApp",
                        icon = Icons.Default.NotificationsActive,
                        onClick = onTriggerNotification,
                        testTag = "sim_notif_chip"
                    )
                    IosActionChip(
                        label = "Putar LaguBerikutnya",
                        icon = Icons.Default.MusicNote,
                        onClick = { IslandStateCoordinator.skipToNextTrack() },
                        testTag = "sim_music_chip"
                    )
                    IosActionChip(
                        label = "Pengisian Daya 88%",
                        icon = Icons.Default.BatteryChargingFull,
                        onClick = { IslandStateCoordinator.triggerSimulatedBatteryCharging(88, false) },
                        testTag = "sim_battery_chip"
                    )
                    IosActionChip(
                        label = "Timer 3 Menit",
                        icon = Icons.Default.Timer,
                        onClick = { IslandStateCoordinator.startOrUpdateTimer(180, "Timer Fokus") },
                        testTag = "sim_timer_chip"
                    )
                    IosActionChip(
                        label = "Bluetooth Audio",
                        icon = Icons.Default.Bluetooth,
                        onClick = { IslandStateCoordinator.triggerSimulatedConnectivity(ConnectivityType.BLUETOOTH, true) }
                    )
                    IosActionChip(
                        label = "Indikator Papan Klip",
                        icon = Icons.Default.ContentPaste,
                        onClick = { IslandStateCoordinator.triggerSimulatedClipboard("Kode OTP Verifikasi: 849201") }
                    )
                }
            }
        }

        // Status Izin Sistem Android
        IosSectionHeader(
            title = "Status Izin Resmi Android",
            subtitle = "LEOVANISLAND bekerja tanpa root dan hanya menggunakan mekanisme perizinan standar Android."
        )
        IosCardGroup {
            PermissionStatusRow(
                title = "Tampilkan di Atas Aplikasi Lain (Overlay)",
                description = "Diperlukan agar floating pill Dynamic Island muncul di atas layar beranda dan aplikasi lain.",
                isGranted = permissions.canDrawOverlays,
                onActionClick = { PermissionManager.openOverlayPermissionSettings(context) },
                testTag = "perm_overlay_row"
            )
            IosRowDivider()
            PermissionStatusRow(
                title = "Akses Notifikasi Pintar & Sesi Media",
                description = "Membaca notifikasi yang diizinkan, balasan cepat RemoteInput, dan kontrol musik aktif.",
                isGranted = permissions.isNotificationListenerEnabled,
                onActionClick = { PermissionManager.openNotificationListenerSettings(context) },
                testTag = "perm_notif_listener_row"
            )
        }

        // Ringkasan Fitur Aktif & Pintasan Navigasi
        IosSectionHeader(
            title = "Ringkasan Modul & Fitur Aktif",
            subtitle = "Ketuk modul untuk membuka pengaturan lengkap atau gunakan sakelar cepat."
        )
        IosCardGroup {
            IosToggleRow(
                icon = Icons.Default.NotificationsActive,
                title = "Notifikasi Pintar & Balasan Cepat",
                subtitle = if (config.notificationsEnabled) "Aktif • Pengelompokan & filter aplikasi berjalan" else "Nonaktif",
                checked = config.notificationsEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(notificationsEnabled = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.MusicNote,
                title = "Pemutar Musik Universal",
                subtitle = if (config.musicEnabled) "Aktif • Sampul album, visualizer & kontrol sesi media" else "Nonaktif",
                checked = config.musicEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(musicEnabled = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.SwapHoriz,
                title = "Mode Kapsul Ganda (Split Pill)",
                subtitle = "Menampilkan 2 aktivitas sekaligus berdampingan seperti iOS",
                checked = config.splitPillEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(splitPillEnabled = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.PhoneAndroid,
                title = "Pemantau Aktivitas Sistem",
                subtitle = "Baterai (${telemetry.batteryPercentage}%), Volume, Timer, Stopwatch, Wi-Fi & Bluetooth",
                checked = config.batteryAlertEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(batteryAlertEnabled = enabled) } }
            )
        }

        // Pintasan Menu Lengkap (Semua 8 Halaman)
        IosSectionHeader(
            title = "Semua Halaman Pengaturan",
            subtitle = "Akses cepat ke seluruh modul konfigurasi LEOVANISLAND."
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LeovanSection.entries.forEach { section ->
                IosActionChip(
                    label = section.title,
                    isSelected = section == LeovanSection.BERANDA,
                    onClick = { onNavigateToSection(section) },
                    testTag = "quick_nav_${section.name.lowercase()}"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppearanceScreen(
    config: IslandConfiguration,
    telemetry: SystemTelemetrySnapshot,
    displayMode: IslandDisplayMode,
    onUpdateConfig: ((IslandConfiguration) -> IslandConfiguration) -> Unit,
    onResetDefaults: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        IosSectionHeader(
            title = "Mode Tampilan Kapsul",
            subtitle = "Ubah mode Dynamic Island secara langsung untuk melihat pratinjau Compact, Split Pill, dan Expanded."
        )
        IosCardGroup {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IosActionChip(
                        label = "Mode Compact",
                        isSelected = displayMode == IslandDisplayMode.COMPACT,
                        onClick = { IslandStateCoordinator.collapseToCompact() }
                    )
                    IosActionChip(
                        label = "Mode Split Pill",
                        isSelected = displayMode == IslandDisplayMode.SPLIT_PILL,
                        onClick = { IslandStateCoordinator.triggerSimulatedSplitPillMusicAndTimer() }
                    )
                    IosActionChip(
                        label = "Mode Expanded",
                        isSelected = displayMode == IslandDisplayMode.EXPANDED,
                        onClick = {
                            if (displayMode != IslandDisplayMode.EXPANDED) {
                                IslandStateCoordinator.toggleExpandCollapse()
                            }
                        }
                    )
                }
            }
        }

        // Kompatibilitas Jenis Layar & Posisi Kamera Depan
        IosSectionHeader(
            title = "Kompatibilitas Layar & Kamera Depan",
            subtitle = "Hitam solid #000000 mendukung AMOLED, OLED, IPS LCD, TFT LCD, PLS LCD, dan LTPS LCD pada layar ${telemetry.screenWidthDp}×${telemetry.screenHeightDp} dp."
        )
        IosCardGroup {
            NotchCompatibilityMode.entries.forEachIndexed { index, mode ->
                if (index > 0) IosRowDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onUpdateConfig {
                                it.copy(
                                    notchMode = mode,
                                    offsetYDp = mode.recommendedY
                                )
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = LeovanTextPrimary
                        )
                        Text(
                            text = mode.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = LeovanTextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = mode.label,
                        tint = if (config.notchMode == mode) StatusGreen else LeovanBorder,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Ukuran Kapsul Ringkas (Compact) & Diperluas (Expanded)
        IosSectionHeader(
            title = "Ukuran Dynamic Island",
            subtitle = "Sesuaikan lebar dan tinggi pill dalam satuan dp agar pas dengan kamera depan perangkat Anda."
        )
        IosCardGroup {
            IosSliderRow(
                icon = Icons.Default.AspectRatio,
                title = "Lebar Kapsul Ringkas (Compact)",
                valueText = "${config.compactWidthDp} dp",
                value = config.compactWidthDp.toFloat(),
                valueRange = 88f..220f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(compactWidthDp = newVal) } },
                testTag = "slider_compact_width"
            )
            IosRowDivider()
            IosSliderRow(
                icon = Icons.Default.Height,
                title = "Tinggi Kapsul Ringkas (Compact)",
                valueText = "${config.compactHeightDp} dp",
                value = config.compactHeightDp.toFloat(),
                valueRange = 26f..56f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(compactHeightDp = newVal) } },
                testTag = "slider_compact_height"
            )
            IosRowDivider()
            IosSliderRow(
                icon = Icons.Default.AspectRatio,
                title = "Lebar Mode Diperluas (Expanded)",
                valueText = "${config.expandedWidthDp} dp",
                value = config.expandedWidthDp.toFloat(),
                valueRange = 280f..400f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(expandedWidthDp = newVal) } },
                testTag = "slider_expanded_width"
            )
            IosRowDivider()
            IosSliderRow(
                icon = Icons.Default.Height,
                title = "Tinggi Mode Diperluas (Expanded)",
                valueText = "${config.expandedHeightDp} dp",
                value = config.expandedHeightDp.toFloat(),
                valueRange = 130f..260f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(expandedHeightDp = newVal) } },
                testTag = "slider_expanded_height"
            )
        }

        // Posisi & Radius Sudut
        IosSectionHeader(
            title = "Posisi & Radius Sudut",
            subtitle = "Atur koordinat horizontal, jarak vertikal dari atas layar, serta kelengkungan sudut kapsul."
        )
        IosCardGroup {
            IosSliderRow(
                icon = Icons.Default.OpenWith,
                title = "Posisi Horizontal (Offset X)",
                subtitle = "Geser ke kiri (-) atau ke kanan (+) dari titik tengah",
                valueText = "${config.offsetXDp} dp",
                value = config.offsetXDp.toFloat(),
                valueRange = -120f..120f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(offsetXDp = newVal) } },
                testTag = "slider_offset_x"
            )
            IosRowDivider()
            IosSliderRow(
                icon = Icons.Default.OpenWith,
                title = "Posisi Vertikal (Offset Y)",
                subtitle = "Jarak dari tepi atas layar agar tidak menutupi lensa kamera",
                valueText = "${config.offsetYDp} dp",
                value = config.offsetYDp.toFloat(),
                valueRange = 0f..64f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(offsetYDp = newVal) } },
                testTag = "slider_offset_y"
            )
            IosRowDivider()
            IosSliderRow(
                icon = Icons.Default.RoundedCorner,
                title = "Radius Sudut Kapsul",
                subtitle = "Tingkat kebulatan sudut Dynamic Island",
                valueText = "${config.cornerRadiusDp} dp",
                value = config.cornerRadiusDp.toFloat(),
                valueRange = 12f..48f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(cornerRadiusDp = newVal) } },
                testTag = "slider_corner_radius"
            )
        }

        // Gaya Animasi & Durasi Transisi
        IosSectionHeader(
            title = "Gaya Animasi & Kecepatan Transisi",
            subtitle = "Pilih karakter transisi saat kapsul mengembang, menyusut, atau berganti aktivitas."
        )
        IosCardGroup {
            AnimationStyle.entries.forEachIndexed { idx, style ->
                if (idx > 0) IosRowDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateConfig { it.copy(animationStyle = style) } }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = style.labelId,
                            style = MaterialTheme.typography.titleSmall,
                            color = LeovanTextPrimary
                        )
                        Text(
                            text = style.descriptionId,
                            style = MaterialTheme.typography.bodySmall,
                            color = LeovanTextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = style.labelId,
                        tint = if (config.animationStyle == style) StatusGreen else LeovanBorder,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            IosRowDivider()
            IosSliderRow(
                icon = Icons.Default.Speed,
                title = "Durasi Animasi Transisi",
                subtitle = "Lebih rendah = lebih cepat & instan",
                valueText = "${config.animationDurationMs} ms",
                value = config.animationDurationMs.toFloat(),
                valueRange = 150f..800f,
                onValueChange = { newVal -> onUpdateConfig { it.copy(animationDurationMs = newVal) } },
                testTag = "slider_anim_duration"
            )
        }

        Surface(
            onClick = onResetDefaults,
            shape = RoundedCornerShape(16.dp),
            color = LeovanCardSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, LeovanBorder, RoundedCornerShape(16.dp))
                .testTag("appearance_reset_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Pulihkan Pengaturan Awal",
                    style = MaterialTheme.typography.titleSmall,
                    color = StatusOrange
                )
            }
        }
    }
}

@Composable
private fun StatMiniCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(LeovanCardSecondary)
            .border(0.8.dp, LeovanBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = LeovanTextSecondary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = LeovanTextPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onActionClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActionClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(LeovanCardSecondary)
                    .border(1.dp, LeovanBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Security,
                    contentDescription = title,
                    tint = if (isGranted) StatusGreen else StatusOrange,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = LeovanTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = LeovanTextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            onClick = onActionClick,
            shape = RoundedCornerShape(10.dp),
            color = if (isGranted) LeovanCardSecondary else LeovanTextPrimary
        ) {
            Text(
                text = if (isGranted) "Diizinkan" else "Aktifkan",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isGranted) StatusGreen else LeovanBlack,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
