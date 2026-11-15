package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.IslandDisplayMode
import com.example.manager.IslandStateCoordinator
import com.example.ui.components.DynamicIslandPillComposable
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AppearanceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MusicScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PersonalizationScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.screens.SystemFeaturesScreen
import com.example.ui.theme.LeovanBlack
import com.example.ui.theme.LeovanBorder
import com.example.ui.theme.LeovanCardPrimary
import com.example.ui.theme.LeovanCardSecondary
import com.example.ui.theme.LeovanIslandTheme
import com.example.ui.theme.LeovanTextPrimary
import com.example.ui.theme.LeovanTextSecondary
import com.example.ui.theme.StatusGreen
import com.example.viewmodel.LeovanSection
import com.example.viewmodel.LeovanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Black.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Black.toArgb())
        )
        setContent {
            LeovanIslandTheme {
                LeovanIslandApp()
            }
        }
    }
}

@Composable
fun LeovanIslandApp(
    viewModel: LeovanViewModel = viewModel()
) {
    val currentSection by viewModel.currentSection.collectAsStateWithLifecycle()
    val config by viewModel.configuration.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val primaryPayload by viewModel.primaryPayload.collectAsStateWithLifecycle()
    val secondaryPayload by viewModel.secondaryPayload.collectAsStateWithLifecycle()
    val displayMode by viewModel.displayMode.collectAsStateWithLifecycle()
    val isOverlayRunning by viewModel.isOverlayServiceRunning.collectAsStateWithLifecycle()
    val statusFeedback by viewModel.statusFeedbackMessage.collectAsStateWithLifecycle()
    val notificationHistory by viewModel.notificationHistory.collectAsStateWithLifecycle()
    val appFilterRules by viewModel.appFilterRules.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    val postNotifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissionsAndTelemetry()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissions.isPostNotificationsGranted) {
            postNotifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionsAndTelemetry()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = currentSection != LeovanSection.BERANDA) {
        viewModel.selectSection(LeovanSection.BERANDA)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(LeovanBlack),
        containerColor = LeovanBlack,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            LeovanBottomBar(
                currentSection = currentSection,
                onSelectSection = { viewModel.selectSection(it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LeovanBlack)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Area Pratinjau Langsung Dynamic Island di Bagian Atas Aplikasi
            LiveDynamicIslandStage(
                config = config,
                primaryPayload = primaryPayload,
                secondaryPayload = secondaryPayload,
                displayMode = displayMode,
                isOverlayRunning = isOverlayRunning
            )

            // Tab Kategori Horizontal Lengkap (8 Halaman Utama)
            SectionPillStrip(
                currentSection = currentSection,
                onSelectSection = { viewModel.selectSection(it) }
            )

            // Banner Status Umpan Balik Instan
            AnimatedVisibility(
                visible = !statusFeedback.isNullOrBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LeovanCardSecondary)
                        .border(1.dp, LeovanBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = statusFeedback.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusGreen
                    )
                }
            }

            // Konten Halaman Responsif
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (currentSection) {
                    LeovanSection.BERANDA -> {
                        HomeScreen(
                            config = config,
                            permissions = permissions,
                            telemetry = telemetry,
                            displayMode = displayMode,
                            isOverlayRunning = isOverlayRunning,
                            onToggleIslandEnabled = { viewModel.toggleMasterIslandEnabled(it) },
                            onUpdateConfig = { viewModel.updateConfiguration(it) },
                            onNavigateToSection = { viewModel.selectSection(it) },
                            onTriggerNotification = {
                                viewModel.triggerSimulatedNotificationAndSave(
                                    appName = "WhatsApp",
                                    packageName = "com.whatsapp",
                                    sender = "Alya Putri",
                                    message = "Sudah sampai di lobi utama, kabari kalau sudah turun ya!"
                                )
                            }
                        )
                    }

                    LeovanSection.TAMPILAN -> {
                        AppearanceScreen(
                            config = config,
                            telemetry = telemetry,
                            displayMode = displayMode,
                            onUpdateConfig = { viewModel.updateConfiguration(it) },
                            onResetDefaults = { viewModel.resetToDefaultSettings() }
                        )
                    }

                    LeovanSection.NOTIFIKASI -> {
                        NotificationsScreen(
                            config = config,
                            permissions = permissions,
                            appRules = appFilterRules,
                            notificationHistory = notificationHistory,
                            onUpdateConfig = { viewModel.updateConfiguration(it) },
                            onUpdateAppRule = { viewModel.updateAppFilterRule(it) },
                            onAddCustomAppRule = { pkg, name, cat ->
                                viewModel.addCustomAppRule(pkg, name, cat)
                            },
                            onTriggerSampleNotification = { appName, pkg, sender, msg ->
                                viewModel.triggerSimulatedNotificationAndSave(appName, pkg, sender, msg)
                            },
                            onDeleteNotificationRecord = { viewModel.deleteNotificationRecord(it) },
                            onClearAllNotifications = { viewModel.clearAllNotificationRecords() }
                        )
                    }

                    LeovanSection.MUSIK -> {
                        MusicScreen(
                            config = config,
                            primaryPayload = primaryPayload,
                            secondaryPayload = secondaryPayload,
                            displayMode = displayMode,
                            onUpdateConfig = { viewModel.updateConfiguration(it) }
                        )
                    }

                    LeovanSection.FITUR_SISTEM -> {
                        SystemFeaturesScreen(
                            config = config,
                            telemetry = telemetry,
                            primaryPayload = primaryPayload,
                            secondaryPayload = secondaryPayload,
                            onUpdateConfig = { viewModel.updateConfiguration(it) },
                            onSetMediaVolume = { viewModel.setMediaVolume(it) }
                        )
                    }

                    LeovanSection.PERSONALISASI -> {
                        PersonalizationScreen(
                            config = config,
                            presets = presets,
                            onUpdateConfig = { viewModel.updateConfiguration(it) },
                            onApplyPreset = { viewModel.applyPreset(it) },
                            onSaveCustomPreset = { name, desc -> viewModel.saveCustomPreset(name, desc) },
                            onDeleteCustomPreset = { viewModel.deleteCustomPreset(it) },
                            onResetDefaults = { viewModel.resetToDefaultSettings() }
                        )
                    }

                    LeovanSection.PRIVASI -> {
                        PrivacyScreen(
                            config = config,
                            permissions = permissions,
                            notificationHistoryCount = notificationHistory.size,
                            onUpdateConfig = { viewModel.updateConfiguration(it) },
                            onToggleMasterIsland = { viewModel.toggleMasterIslandEnabled(it) },
                            onClearNotificationHistory = { viewModel.clearAllNotificationRecords() },
                            onResetAllSettings = { viewModel.resetToDefaultSettings() }
                        )
                    }

                    LeovanSection.TENTANG -> {
                        AboutScreen()
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun LiveDynamicIslandStage(
    config: com.example.data.IslandConfiguration,
    primaryPayload: com.example.data.IslandPayload,
    secondaryPayload: com.example.data.IslandPayload?,
    displayMode: IslandDisplayMode,
    isOverlayRunning: Boolean
) {
    val stageShape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(stageShape)
            .background(LeovanCardPrimary)
            .border(1.dp, LeovanBorder, stageShape)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (config.isIslandEnabled) StatusGreen else LeovanTextSecondary)
                )
                Text(
                    text = if (isOverlayRunning) {
                        "PRATINJAU & OVERLAY AKTIF"
                    } else {
                        "PRATINJAU INTERAKTIF DYNAMIC ISLAND"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = LeovanTextSecondary
                )
            }

            if (displayMode == IslandDisplayMode.HIDDEN) {
                Text(
                    text = "Tampilkan Kembali",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = StatusGreen,
                    modifier = Modifier.clickable { IslandStateCoordinator.restoreFromHidden() }
                )
            } else {
                Text(
                    text = displayMode.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = LeovanTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Jika layanan WindowManager overlay belum aktif di atas layar, tampilkan kapsul interaktif di panggung pratinjau
        if (!isOverlayRunning) {
            if (config.isIslandEnabled && displayMode != IslandDisplayMode.HIDDEN) {
                DynamicIslandPillComposable(
                    config = config.copy(offsetYDp = 0),
                    primaryPayload = primaryPayload,
                    secondaryPayload = secondaryPayload,
                    displayMode = displayMode,
                    isInteractivePreview = true
                )
            } else if (!config.isIslandEnabled) {
                Text(
                    text = "Dynamic Island sedang dinonaktifkan. Ketuk tombol Aktifkan di Beranda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LeovanTextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Text(
                    text = "Kapsul disembunyikan manual. Ketuk 'Tampilkan Kembali' di sudut kanan atas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LeovanTextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            Text(
                text = "Kapsul Dynamic Island sedang mengambang di bagian atas layar perangkat Anda.",
                style = MaterialTheme.typography.bodySmall,
                color = LeovanTextSecondary,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun SectionPillStrip(
    currentSection: LeovanSection,
    onSelectSection: (LeovanSection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LeovanSection.entries.forEach { section ->
            val selected = currentSection == section
            Surface(
                onClick = { onSelectSection(section) },
                shape = CircleShape,
                color = if (selected) LeovanTextPrimary else LeovanCardPrimary,
                modifier = Modifier
                    .height(34.dp)
                    .border(
                        width = 1.dp,
                        color = if (selected) LeovanTextPrimary else LeovanBorder,
                        shape = CircleShape
                    )
                    .testTag("tab_${section.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = section.icon(),
                        contentDescription = section.title,
                        tint = if (selected) LeovanBlack else LeovanTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (selected) LeovanBlack else LeovanTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun LeovanBottomBar(
    currentSection: LeovanSection,
    onSelectSection: (LeovanSection) -> Unit
) {
    val primaryNavItems = listOf(
        LeovanSection.BERANDA,
        LeovanSection.TAMPILAN,
        LeovanSection.NOTIFIKASI,
        LeovanSection.MUSIK,
        LeovanSection.FITUR_SISTEM
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LeovanCardPrimary)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LeovanBorder)
        )
        NavigationBar(
            containerColor = LeovanCardPrimary,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            primaryNavItems.forEach { section ->
                val isSelected = currentSection == section
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectSection(section) },
                    icon = {
                        Icon(
                            imageVector = section.icon(),
                            contentDescription = section.title
                        )
                    },
                    label = {
                        Text(
                            text = section.shortLabel(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeovanBlack,
                        selectedTextColor = LeovanTextPrimary,
                        indicatorColor = LeovanTextPrimary,
                        unselectedIconColor = LeovanTextSecondary,
                        unselectedTextColor = LeovanTextSecondary
                    ),
                    modifier = Modifier.testTag("bottom_nav_${section.name.lowercase()}")
                )
            }
        }
    }
}

private fun LeovanSection.icon(): ImageVector {
    return when (this) {
        LeovanSection.BERANDA -> Icons.Default.Home
        LeovanSection.TAMPILAN -> Icons.Default.Tune
        LeovanSection.NOTIFIKASI -> Icons.Default.Notifications
        LeovanSection.MUSIK -> Icons.Default.MusicNote
        LeovanSection.FITUR_SISTEM -> Icons.Default.PhoneAndroid
        LeovanSection.PERSONALISASI -> Icons.Default.Palette
        LeovanSection.PRIVASI -> Icons.Default.Security
        LeovanSection.TENTANG -> Icons.Default.Info
    }
}

private fun LeovanSection.shortLabel(): String {
    return when (this) {
        LeovanSection.BERANDA -> "Beranda"
        LeovanSection.TAMPILAN -> "Tampilan"
        LeovanSection.NOTIFIKASI -> "Notifikasi"
        LeovanSection.MUSIK -> "Musik"
        LeovanSection.FITUR_SISTEM -> "Sistem"
        LeovanSection.PERSONALISASI -> "Gaya"
        LeovanSection.PRIVASI -> "Privasi"
        LeovanSection.TENTANG -> "Tentang"
    }
}
