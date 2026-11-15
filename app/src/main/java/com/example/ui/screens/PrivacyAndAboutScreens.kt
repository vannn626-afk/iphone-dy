package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.IslandConfiguration
import com.example.data.PermissionStatusSnapshot
import com.example.manager.PermissionManager
import com.example.ui.components.IosCardGroup
import com.example.ui.components.IosRowDivider
import com.example.ui.components.IosSectionHeader
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
import com.example.ui.theme.StatusRed

@Composable
fun PrivacyScreen(
    config: IslandConfiguration,
    permissions: PermissionStatusSnapshot,
    notificationHistoryCount: Int,
    onUpdateConfig: ((IslandConfiguration) -> IslandConfiguration) -> Unit,
    onToggleMasterIsland: (Boolean) -> Unit,
    onClearNotificationHistory: () -> Unit,
    onResetAllSettings: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        IosSectionHeader(
            title = "Transparansi Izin & Penjelasan Akses",
            subtitle = "LEOVANISLAND menjelaskan fungsi setiap izin secara terbuka sebelum Anda mengaktifkannya."
        )
        IosCardGroup {
            PermissionExplanationItem(
                icon = Icons.Default.Layers,
                title = "Tampilkan di Atas Aplikasi Lain (SYSTEM_ALERT_WINDOW)",
                purpose = "Digunakan semata-mata untuk menggambar floating pill Dynamic Island di bagian atas layar dengan flag FLAG_NOT_FOCUSABLE sehingga tidak menghalangi sentuhan layar Anda.",
                isGranted = permissions.canDrawOverlays,
                actionLabel = if (permissions.canDrawOverlays) "Kelola Izin" else "Berikan Izin",
                onClick = { PermissionManager.openOverlayPermissionSettings(context) }
            )
            IosRowDivider()
            PermissionExplanationItem(
                icon = Icons.Default.Notifications,
                title = "Akses Notifikasi & Media (NotificationListenerService)",
                purpose = "Digunakan untuk menampilkan pengirim/pesan dari aplikasi yang Anda izinkan, menyediakan tombol Balas Cepat (RemoteInput), dan membaca metadata lagu dari MediaSessionManager.",
                isGranted = permissions.isNotificationListenerEnabled,
                actionLabel = if (permissions.isNotificationListenerEnabled) "Kelola Izin" else "Berikan Izin",
                onClick = { PermissionManager.openNotificationListenerSettings(context) }
            )
        }

        // Prinsip Keamanan & Privasi Ketat
        IosSectionHeader(
            title = "Jaminan Keamanan & Privasi LEOVANISLAND",
            subtitle = "Dirancang sesuai standar keamanan Android resmi tanpa kompromi."
        )
        IosCardGroup {
            SecurityGuaranteeRow(
                icon = Icons.Default.VerifiedUser,
                title = "Tidak Membutuhkan Akses Root",
                description = "Berjalan di seluruh perangkat Android standar menggunakan API resmi WindowManager & NotificationListenerService."
            )
            IosRowDivider()
            SecurityGuaranteeRow(
                icon = Icons.Default.GppGood,
                title = "Tanpa AccessibilityService Tersembunyi",
                description = "Tidak menyalahgunakan layanan aksesibilitas untuk membaca layar, mengambil kata sandi, atau mengakali pembatasan sistem."
            )
            IosRowDivider()
            SecurityGuaranteeRow(
                icon = Icons.Default.Storage,
                title = "100% Lokal — Tanpa Pengiriman ke Server",
                description = "Isi notifikasi, riwayat lokal, dan preset tampilan disimpan eksklusif di penyimpanan internal perangkat Anda tanpa koneksi ke server pihak ketiga."
            )
            IosRowDivider()
            SecurityGuaranteeRow(
                icon = Icons.Default.Lock,
                title = "Tidak Mengakses Kontak atau Kata Sandi",
                description = "LEOVANISLAND tidak meminta izin buku kontak, mikrofon, kamera, ataupun lokasi."
            )
        }

        // Kontrol Fitur & Pengaturan Data Lokal
        IosSectionHeader(
            title = "Pengaturan Data Lokal & Kontrol Fitur",
            subtitle = "Anda memiliki kendali penuh untuk menonaktifkan fitur atau menghapus data lokal kapan saja."
        )
        IosCardGroup {
            IosToggleRow(
                icon = Icons.Default.Storage,
                title = "Simpan Riwayat Notifikasi Lokal",
                subtitle = "Saat ini tersimpan $notificationHistoryCount entri di database lokal Room",
                checked = config.saveNotificationHistory,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(saveNotificationHistory = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.Lock,
                title = "Sembunyikan Pesan Saat Perangkat Terkunci",
                subtitle = "Cegah orang lain membaca isi pesan di Dynamic Island saat layar terkunci",
                checked = config.hideSensitiveWhenLocked,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(hideSensitiveWhenLocked = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.PowerSettingsNew,
                title = "Status Utama LEOVANISLAND",
                subtitle = "Nonaktifkan layanan overlay dan seluruh fitur kapan saja dengan satu ketukan",
                checked = config.isIslandEnabled,
                onCheckedChange = onToggleMasterIsland
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = onClearNotificationHistory,
                shape = RoundedCornerShape(16.dp),
                color = LeovanCardPrimary,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, LeovanBorder, RoundedCornerShape(16.dp))
                    .testTag("privacy_clear_history_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Hapus Riwayat",
                        tint = StatusRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hapus Riwayat Lokal",
                        style = MaterialTheme.typography.labelLarge,
                        color = StatusRed
                    )
                }
            }

            Surface(
                onClick = onResetAllSettings,
                shape = RoundedCornerShape(16.dp),
                color = LeovanCardPrimary,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .border(1.dp, LeovanBorder, RoundedCornerShape(16.dp))
                    .testTag("privacy_reset_settings_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Reset Pengaturan",
                        tint = StatusOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset Pengaturan",
                        style = MaterialTheme.typography.labelLarge,
                        color = StatusOrange
                    )
                }
            }
        }
    }
}

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Kartu Identitas & Logo LEOVANISLAND
        val shape = RoundedCornerShape(24.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(LeovanCardPrimary)
                .border(1.dp, LeovanBorder, shape)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(LeovanBlack)
                    .border(1.dp, LeovanBorder, RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_launcher_icon_1794705105476),
                    contentDescription = "Logo LEOVANISLAND",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = "LEOVANISLAND",
                style = MaterialTheme.typography.headlineLarge,
                color = LeovanTextPrimary
            )
            Text(
                text = "Dynamic Island Pintar untuk Android",
                style = MaterialTheme.typography.bodyMedium,
                color = LeovanTextSecondary
            )
            StatusPillBadge(label = "Versi 1.0.0 • Rilis Produksi Native", isPositive = true)
        }

        // Informasi Pengembang & Teknologi
        IosSectionHeader(
            title = "Informasi Pengembang & Arsitektur",
            subtitle = "Dikembangkan secara native untuk performa tinggi, efisiensi baterai, dan stabilitas maksimal."
        )
        IosCardGroup {
            AboutInfoRow(label = "Nama Aplikasi", value = "LEOVANISLAND")
            IosRowDivider()
            AboutInfoRow(label = "Versi Rilis", value = "1.0.0 (Build 1)")
            IosRowDivider()
            AboutInfoRow(label = "Pengembang", value = "Leovan Engineering Studio")
            IosRowDivider()
            AboutInfoRow(label = "Teknologi Utama", value = "Kotlin, Jetpack Compose, Material 3, Coroutines & StateFlow")
            IosRowDivider()
            AboutInfoRow(label = "Layanan Sistem", value = "WindowManager Overlay, NotificationListenerService, MediaSessionManager")
            IosRowDivider()
            AboutInfoRow(label = "Penyimpanan Lokal", value = "Jetpack DataStore & Room SQLite Database")
        }

        // Panduan Lengkap Penggunaan & Build (11–15 dari Spesifikasi)
        IosSectionHeader(
            title = "Panduan Lengkap Penggunaan & Teknis",
            subtitle = "Ringkasan panduan instalasi, perizinan, dan pengujian fitur (tersedia lengkap di README.md)."
        )
        IosCardGroup {
            GuideStepRow(
                stepNumber = "1",
                title = "Panduan Membuka Project & Build APK",
                content = "Buka folder project di Android Studio terbaru, tunggu Gradle Sync selesai, lalu pilih menu Build > Build Bundle(s) / APK(s) > Build APK(s) atau jalankan perintah `gradle assembleDebug`."
            )
            IosRowDivider()
            GuideStepRow(
                stepNumber = "2",
                title = "Panduan Instalasi",
                content = "Pasang file APK hasil build pada perangkat Android (minimal Android 7.0 / API 24 hingga Android 15/16) tanpa memerlukan akses root."
            )
            IosRowDivider()
            GuideStepRow(
                stepNumber = "3",
                title = "Panduan Mengaktifkan Izin",
                content = "Pada halaman Beranda atau Privasi, ketuk 'Aktifkan' pada izin Tampilkan di Atas Aplikasi Lain (Overlay) dan Akses Notifikasi Pintar, lalu aktifkan sakelar LEOVANISLAND."
            )
            IosRowDivider()
            GuideStepRow(
                stepNumber = "4",
                title = "Panduan Menguji Fitur",
                content = "Gunakan Simulator Interaktif di halaman Beranda untuk menguji mode Compact, Split Pill (Musik + Timer), Expanded, Balasan Cepat WhatsApp, Pengisian Daya, maupun jalankan aplikasi musik seperti Spotify secara langsung."
            )
        }

        // Lisensi & Sumber Terbuka
        IosSectionHeader(
            title = "Lisensi & Pustaka Sumber Terbuka",
            subtitle = "Menggunakan pustaka resmi Android Jetpack berlisensi Apache License 2.0."
        )
        IosCardGroup {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Pustaka Open Source yang Digunakan:",
                    style = MaterialTheme.typography.titleSmall,
                    color = LeovanTextPrimary
                )
                Text(
                    text = "• AndroidX Core KTX & Activity Compose (Apache 2.0)\n" +
                        "• AndroidX Jetpack Compose UI & Material 3 (Apache 2.0)\n" +
                        "• AndroidX Lifecycle ViewModel & Runtime Compose (Apache 2.0)\n" +
                        "• AndroidX Room Runtime & KTX (Apache 2.0)\n" +
                        "• AndroidX DataStore Preferences (Apache 2.0)\n" +
                        "• Kotlinx Coroutines Android (Apache 2.0)",
                    style = MaterialTheme.typography.bodySmall,
                    color = LeovanTextSecondary
                )
            }
        }
    }
}

@Composable
private fun PermissionExplanationItem(
    icon: ImageVector,
    title: String,
    purpose: String,
    isGranted: Boolean,
    actionLabel: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isGranted) StatusGreen else StatusOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = LeovanTextPrimary
                )
            }
            StatusPillBadge(
                label = if (isGranted) "Diizinkan" else "Belum Aktif",
                isPositive = isGranted
            )
        }
        Text(
            text = purpose,
            style = MaterialTheme.typography.bodySmall,
            color = LeovanTextSecondary
        )
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = LeovanCardSecondary,
            modifier = Modifier.border(1.dp, LeovanBorder, RoundedCornerShape(12.dp))
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = LeovanTextPrimary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun SecurityGuaranteeRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = StatusGreen,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
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
}

@Composable
private fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = LeovanTextSecondary,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = LeovanTextPrimary,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun GuideStepRow(
    stepNumber: String,
    title: String,
    content: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LeovanCardSecondary)
                .border(1.dp, LeovanBorder, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = LeovanTextPrimary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = LeovanTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = LeovanTextSecondary
            )
        }
    }
}
