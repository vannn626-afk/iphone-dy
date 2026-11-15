package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkChatUnread
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AppFilterRuleEntity
import com.example.data.IslandConfiguration
import com.example.data.IslandDisplayMode
import com.example.data.IslandPayload
import com.example.data.NotificationRecordEntity
import com.example.data.PermissionStatusSnapshot
import com.example.manager.IslandStateCoordinator
import com.example.manager.PermissionManager
import com.example.ui.components.AudioWaveformBars
import com.example.ui.components.IosActionChip
import com.example.ui.components.IosCardGroup
import com.example.ui.components.IosRowDivider
import com.example.ui.components.IosSectionHeader
import com.example.ui.components.IosToggleRow
import com.example.ui.theme.LeovanBlack
import com.example.ui.theme.LeovanBorder
import com.example.ui.theme.LeovanCardPrimary
import com.example.ui.theme.LeovanCardSecondary
import com.example.ui.theme.LeovanTextPrimary
import com.example.ui.theme.LeovanTextSecondary
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationsScreen(
    config: IslandConfiguration,
    permissions: PermissionStatusSnapshot,
    appRules: List<AppFilterRuleEntity>,
    notificationHistory: List<NotificationRecordEntity>,
    onUpdateConfig: ((IslandConfiguration) -> IslandConfiguration) -> Unit,
    onUpdateAppRule: (AppFilterRuleEntity) -> Unit,
    onAddCustomAppRule: (String, String, String) -> Unit,
    onTriggerSampleNotification: (String, String, String, String) -> Unit,
    onDeleteNotificationRecord: (Long) -> Unit,
    onClearAllNotifications: () -> Unit
) {
    val context = LocalContext.current
    var showAddAppDialog by remember { mutableStateOf(false) }
    var editingKeywordRule by remember { mutableStateOf<AppFilterRuleEntity?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (!permissions.isNotificationListenerEnabled) {
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
                            text = "Akses Notifikasi Belum Aktif",
                            style = MaterialTheme.typography.titleSmall,
                            color = StatusOrange
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Izinkan NotificationListenerService agar notifikasi nyata dari WhatsApp, Telegram, Instagram, dan Gmail dapat ditampilkan di Dynamic Island.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LeovanTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        onClick = { PermissionManager.openNotificationListenerSettings(context) },
                        shape = RoundedCornerShape(12.dp),
                        color = LeovanTextPrimary
                    ) {
                        Text(
                            text = "Izinkan",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = LeovanBlack,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        IosSectionHeader(
            title = "Pengaturan Utama Notifikasi Pintar",
            subtitle = "Atur bagaimana pesan masuk, pengelompokan, dan balasan cepat ditampilkan di Dynamic Island."
        )
        IosCardGroup {
            IosToggleRow(
                icon = Icons.Default.NotificationsActive,
                title = "Aktifkan Notifikasi di Island",
                subtitle = "Tampilkan notifikasi masuk sebagai pop-up kapsul interaktif",
                checked = config.notificationsEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(notificationsEnabled = enabled) } },
                testTag = "toggle_notif_enabled"
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.Visibility,
                title = "Tampilkan Preview Isi Pesan",
                subtitle = "Perlihatkan isi pesan pengirim di dalam kapsul",
                checked = config.showMessagePreview,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(showMessagePreview = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.AutoMirrored.Filled.Reply,
                title = "Balasan Cepat (RemoteInput)",
                subtitle = "Munculkan kotak balasan langsung jika aplikasi mendukung RemoteInput",
                checked = config.quickReplyEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(quickReplyEnabled = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.MarkChatUnread,
                title = "Pengelompokan Notifikasi",
                subtitle = "Gabungkan beberapa pesan dari aplikasi yang sama dalam satu badge",
                checked = config.groupNotificationsEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(groupNotificationsEnabled = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.Lock,
                title = "Sembunyikan Isi Saat Layar Terkunci",
                subtitle = "Jaga privasi pesan ketika perangkat berada di layar kunci",
                checked = config.hideSensitiveWhenLocked,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(hideSensitiveWhenLocked = enabled) } }
            )
        }

        // Uji Cepat Berbagai Aplikasi
        IosSectionHeader(
            title = "Uji Notifikasi Aplikasi",
            subtitle = "Simulasikan notifikasi masuk dari aplikasi populer lengkap dengan tombol Balas Cepat."
        )
        IosCardGroup {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IosActionChip(
                        label = "WhatsApp",
                        icon = Icons.Default.Notifications,
                        onClick = {
                            onTriggerSampleNotification(
                                "WhatsApp",
                                "com.whatsapp",
                                "Dimas Anggara",
                                "File presentasi proyek sudah saya kirim ke grup ya!"
                            )
                        }
                    )
                    IosActionChip(
                        label = "Telegram",
                        icon = Icons.Default.Notifications,
                        onClick = {
                            onTriggerSampleNotification(
                                "Telegram",
                                "org.telegram.messenger",
                                "Komunitas Android ID",
                                "Build APK rilis terbaru sudah siap diunduh."
                            )
                        }
                    )
                    IosActionChip(
                        label = "Instagram",
                        icon = Icons.Default.Notifications,
                        onClick = {
                            onTriggerSampleNotification(
                                "Instagram",
                                "com.instagram.android",
                                "@nara.studio",
                                "Menyebut Anda dalam cerita: Desain hitam pekat yang sangat rapi!"
                            )
                        }
                    )
                    IosActionChip(
                        label = "Gmail",
                        icon = Icons.Default.Notifications,
                        onClick = {
                            onTriggerSampleNotification(
                                "Gmail",
                                "com.google.android.gm",
                                "Tim Produk",
                                "Jadwal rilis versi produksi LEOVANISLAND minggu depan."
                            )
                        }
                    )
                }
            }
        }

        // Filter & Pengaturan Per Aplikasi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IosSectionHeader(
                title = "Filter & Pengaturan Per Aplikasi",
                subtitle = "Atur izin tampil, preview pesan, balasan cepat, dan filter kata tersembunyi untuk tiap aplikasi.",
                modifier = Modifier.weight(1f)
            )
            Surface(
                onClick = { showAddAppDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = LeovanCardSecondary,
                modifier = Modifier
                    .border(1.dp, LeovanBorder, RoundedCornerShape(12.dp))
                    .testTag("add_custom_app_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Aplikasi",
                        tint = LeovanTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tambah",
                        style = MaterialTheme.typography.labelMedium,
                        color = LeovanTextPrimary
                    )
                }
            }
        }

        IosCardGroup {
            appRules.forEachIndexed { index, rule ->
                if (index > 0) IosRowDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rule.appName,
                                style = MaterialTheme.typography.titleSmall,
                                color = LeovanTextPrimary
                            )
                            Text(
                                text = "${rule.categoryLabel} • ${rule.packageName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = LeovanTextSecondary
                            )
                        }
                        Switch(
                            checked = rule.isAllowed,
                            onCheckedChange = { allowed ->
                                onUpdateAppRule(rule.copy(isAllowed = allowed))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LeovanTextPrimary,
                                checkedTrackColor = StatusGreen,
                                uncheckedThumbColor = LeovanTextSecondary,
                                uncheckedTrackColor = LeovanCardSecondary,
                                uncheckedBorderColor = LeovanBorder
                            )
                        )
                    }
                    if (rule.isAllowed) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiniRuleChip(
                                text = if (rule.showMessagePreview) "Preview: Aktif" else "Preview: Sembunyi",
                                active = rule.showMessagePreview,
                                onClick = {
                                    onUpdateAppRule(rule.copy(showMessagePreview = !rule.showMessagePreview))
                                }
                            )
                            MiniRuleChip(
                                text = if (rule.allowQuickReply) "Balas Cepat: Ya" else "Balas Cepat: Tidak",
                                active = rule.allowQuickReply,
                                onClick = {
                                    onUpdateAppRule(rule.copy(allowQuickReply = !rule.allowQuickReply))
                                }
                            )
                            MiniRuleChip(
                                text = if (rule.hideSpecificKeywords.isBlank()) "Filter Kata" else "Filter: ${rule.hideSpecificKeywords}",
                                active = rule.hideSpecificKeywords.isNotBlank(),
                                onClick = { editingKeywordRule = rule }
                            )
                        }
                    }
                }
            }
        }

        // Riwayat Notifikasi Lokal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IosSectionHeader(
                title = "Riwayat Notifikasi Lokal (${notificationHistory.size})",
                subtitle = "Tersimpan 100% secara lokal di perangkat Anda.",
                modifier = Modifier.weight(1f)
            )
            if (notificationHistory.isNotEmpty()) {
                Text(
                    text = "Bersihkan Semua",
                    style = MaterialTheme.typography.labelMedium,
                    color = StatusRed,
                    modifier = Modifier
                        .clickable { onClearAllNotifications() }
                        .padding(8.dp)
                )
            }
        }

        IosCardGroup {
            if (notificationHistory.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Kosong",
                        tint = LeovanTextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada riwayat notifikasi lokal",
                        style = MaterialTheme.typography.bodySmall,
                        color = LeovanTextSecondary
                    )
                }
            } else {
                notificationHistory.take(15).forEachIndexed { idx, record ->
                    if (idx > 0) IosRowDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTriggerSampleNotification(
                                    record.appName,
                                    record.packageName,
                                    record.senderName,
                                    record.messageContent
                                )
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = record.appName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusGreen
                                )
                                Text(
                                    text = "• ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LeovanTextSecondary
                                )
                            }
                            Text(
                                text = record.senderName,
                                style = MaterialTheme.typography.titleSmall,
                                color = LeovanTextPrimary
                            )
                            Text(
                                text = record.messageContent,
                                style = MaterialTheme.typography.bodySmall,
                                color = LeovanTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onDeleteNotificationRecord(record.id) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hapus",
                                tint = LeovanTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddAppDialog) {
        var pkgInput by remember { mutableStateOf("") }
        var nameInput by remember { mutableStateOf("") }
        var catInput by remember { mutableStateOf("Pesan & Sosial") }

        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            containerColor = LeovanCardPrimary,
            titleContentColor = LeovanTextPrimary,
            textContentColor = LeovanTextSecondary,
            title = { Text("Tambah Filter Aplikasi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Aplikasi (mis. Line / Discord)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LeovanTextPrimary,
                            unfocusedBorderColor = LeovanBorder,
                            focusedTextColor = LeovanTextPrimary,
                            unfocusedTextColor = LeovanTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = pkgInput,
                        onValueChange = { pkgInput = it },
                        label = { Text("Nama Paket (mis. com.discord)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LeovanTextPrimary,
                            unfocusedBorderColor = LeovanBorder,
                            focusedTextColor = LeovanTextPrimary,
                            unfocusedTextColor = LeovanTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = catInput,
                        onValueChange = { catInput = it },
                        label = { Text("Kategori") },
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
                        if (nameInput.isNotBlank() && pkgInput.isNotBlank()) {
                            onAddCustomAppRule(pkgInput, nameInput, catInput)
                            showAddAppDialog = false
                        }
                    }
                ) {
                    Text("Simpan", color = LeovanTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAppDialog = false }) {
                    Text("Batal", color = LeovanTextSecondary)
                }
            }
        )
    }

    editingKeywordRule?.let { rule ->
        var keywordInput by remember(rule.packageName) { mutableStateOf(rule.hideSpecificKeywords) }
        AlertDialog(
            onDismissRequest = { editingKeywordRule = null },
            containerColor = LeovanCardPrimary,
            titleContentColor = LeovanTextPrimary,
            textContentColor = LeovanTextSecondary,
            title = { Text("Sembunyikan Notifikasi Tertentu (${rule.appName})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Masukkan kata kunci yang ingin disembunyikan (pisahkan dengan koma, misal: promo, iklan, otp).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = keywordInput,
                        onValueChange = { keywordInput = it },
                        label = { Text("Kata kunci disembunyikan") },
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
                        onUpdateAppRule(rule.copy(hideSpecificKeywords = keywordInput.trim()))
                        editingKeywordRule = null
                    }
                ) {
                    Text("Simpan Filter", color = LeovanTextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingKeywordRule = null }) {
                    Text("Batal", color = LeovanTextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MusicScreen(
    config: IslandConfiguration,
    primaryPayload: IslandPayload,
    secondaryPayload: IslandPayload?,
    displayMode: IslandDisplayMode,
    onUpdateConfig: ((IslandConfiguration) -> IslandConfiguration) -> Unit
) {
    val activeMusic = (primaryPayload as? IslandPayload.MusicPlayback)
        ?: (secondaryPayload as? IslandPayload.MusicPlayback)
        ?: IslandPayload.MusicPlayback(
            title = "Midnight Resonance",
            artist = "Leovan Studio Ensemble",
            album = "Nocturne in Black",
            appName = "Spotify",
            isPlaying = false
        )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        IosSectionHeader(
            title = "Pemutar Musik Universal (MediaSession)",
            subtitle = "Terhubung otomatis ke sesi media aktif seperti Spotify, YouTube Music, Apple Music, dan pemutar musik lokal."
        )

        // Kartu Kontrol Musik Langsung
        val cardShape = RoundedCornerShape(24.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(LeovanCardPrimary)
                .border(1.dp, LeovanBorder, cardShape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(LeovanCardSecondary)
                        .border(1.dp, LeovanBorder, RoundedCornerShape(16.dp))
                ) {
                    if (activeMusic.albumArt != null) {
                        Image(
                            bitmap = activeMusic.albumArt.asImageBitmap(),
                            contentDescription = activeMusic.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_album_cover_1794705107042),
                            contentDescription = activeMusic.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeMusic.appName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusGreen
                    )
                    Text(
                        text = activeMusic.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = LeovanTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${activeMusic.artist} • ${activeMusic.album.ifEmpty { "Single" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LeovanTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                AudioWaveformBars(isPlaying = activeMusic.isPlaying, barCount = 5)
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = activeMusic.progressFraction,
                    onValueChange = { IslandStateCoordinator.seekMusicToFraction(it) },
                    colors = SliderDefaults.colors(
                        thumbColor = LeovanTextPrimary,
                        activeTrackColor = LeovanTextPrimary,
                        inactiveTrackColor = LeovanBorder
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMs(activeMusic.positionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = LeovanTextSecondary
                    )
                    Text(
                        text = formatMs(activeMusic.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = LeovanTextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { IslandStateCoordinator.skipToPreviousTrack() }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Sebelumnya",
                        tint = LeovanTextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Surface(
                    onClick = {
                        IslandStateCoordinator.postPayload(activeMusic.copy(isPlaying = !activeMusic.isPlaying))
                        IslandStateCoordinator.toggleMusicPlayPause()
                    },
                    shape = CircleShape,
                    color = LeovanTextPrimary,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (activeMusic.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Putar / Jeda",
                            tint = LeovanBlack,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                IconButton(onClick = { IslandStateCoordinator.skipToNextTrack() }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Berikutnya",
                        tint = LeovanTextPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IosActionChip(
                    label = "Tampilan Mini (Compact)",
                    isSelected = displayMode == IslandDisplayMode.COMPACT,
                    onClick = {
                        IslandStateCoordinator.postPayload(activeMusic, autoExpand = false)
                        IslandStateCoordinator.collapseToCompact()
                    }
                )
                IosActionChip(
                    label = "Tampilan Penuh (Expanded)",
                    isSelected = displayMode == IslandDisplayMode.EXPANDED && primaryPayload is IslandPayload.MusicPlayback,
                    onClick = {
                        IslandStateCoordinator.postPayload(activeMusic, autoExpand = true)
                    }
                )
            }
        }

        // Pengaturan Pemutar Musik di Dynamic Island
        IosSectionHeader(
            title = "Konfigurasi Tampilan Musik",
            subtitle = "Sesuaikan elemen visual pemutar musik pada mode Compact maupun Expanded."
        )
        IosCardGroup {
            IosToggleRow(
                icon = Icons.Default.MusicNote,
                title = "Aktifkan Pemutar Musik di Island",
                subtitle = "Tampilkan sesi media yang sedang diputar secara otomatis",
                checked = config.musicEnabled,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(musicEnabled = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.Album,
                title = "Tampilkan Cover Album",
                subtitle = "Render sampul album asli dari metadata MediaSession",
                checked = config.showAlbumCover,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(showAlbumCover = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.Equalizer,
                title = "Tampilkan Kontrol Musik (Play/Next/Prev)",
                subtitle = "Sediakan tombol transport media di dalam kartu Expanded",
                checked = config.showMusicControls,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(showMusicControls = enabled) } }
            )
            IosRowDivider()
            IosToggleRow(
                icon = Icons.Default.GraphicEq,
                title = "Animasi Gelombang Musik (Visualizer)",
                subtitle = "Tampilkan animasi bar frekuensi di sisi kanan kapsul",
                checked = config.showMusicVisualizer,
                onCheckedChange = { enabled -> onUpdateConfig { it.copy(showMusicVisualizer = enabled) } }
            )
        }
    }
}

@Composable
private fun MiniRuleChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LeovanCardSecondary)
            .border(
                width = 0.8.dp,
                color = if (active) StatusGreen.copy(alpha = 0.6f) else LeovanBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) LeovanTextPrimary else LeovanTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSecs = (ms / 1000L).coerceAtLeast(0L)
    return String.format(Locale.getDefault(), "%02d:%02d", totalSecs / 60L, totalSecs % 60L)
}
