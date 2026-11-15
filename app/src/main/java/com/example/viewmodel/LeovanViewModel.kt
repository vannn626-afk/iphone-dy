package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppFilterRuleEntity
import com.example.data.ConfigPreferencesDataStore
import com.example.data.IslandConfiguration
import com.example.data.IslandDisplayMode
import com.example.data.IslandPayload
import com.example.data.IslandPresetEntity
import com.example.data.LeovanDatabase
import com.example.data.LeovanRepository
import com.example.data.NotificationRecordEntity
import com.example.data.PermissionStatusSnapshot
import com.example.data.SystemTelemetrySnapshot
import com.example.manager.IslandStateCoordinator
import com.example.manager.PermissionManager
import com.example.manager.SystemActivityMonitor
import com.example.service.IslandOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LeovanSection(val title: String) {
    BERANDA("Beranda"),
    TAMPILAN("Tampilan Island"),
    NOTIFIKASI("Notifikasi"),
    MUSIK("Musik"),
    FITUR_SISTEM("Fitur Sistem"),
    PERSONALISASI("Personalisasi"),
    PRIVASI("Privasi"),
    TENTANG("Tentang")
}

class LeovanViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val database = LeovanDatabase.getInstance(appContext)
    private val dataStore = ConfigPreferencesDataStore(appContext)
    private val repository = LeovanRepository(dataStore, database.leovanDao())
    private val systemMonitor = SystemActivityMonitor(appContext)

    private val _currentSection = MutableStateFlow(LeovanSection.BERANDA)
    val currentSection: StateFlow<LeovanSection> = _currentSection.asStateFlow()

    private val _permissions = MutableStateFlow(PermissionManager.checkPermissions(appContext))
    val permissions: StateFlow<PermissionStatusSnapshot> = _permissions.asStateFlow()

    val configuration: StateFlow<IslandConfiguration> = repository.configurationFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = IslandConfiguration()
        )

    val notificationHistory: StateFlow<List<NotificationRecordEntity>> = repository.notificationHistoryFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val appFilterRules: StateFlow<List<AppFilterRuleEntity>> = repository.appFilterRulesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val presets: StateFlow<List<IslandPresetEntity>> = repository.presetsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val primaryPayload: StateFlow<IslandPayload> = IslandStateCoordinator.primaryPayload
    val secondaryPayload: StateFlow<IslandPayload?> = IslandStateCoordinator.secondaryPayload
    val displayMode: StateFlow<IslandDisplayMode> = IslandStateCoordinator.displayMode
    val isOverlayServiceRunning: StateFlow<Boolean> = IslandStateCoordinator.isOverlayServiceRunning
    val statusFeedbackMessage: StateFlow<String?> = IslandStateCoordinator.statusFeedbackMessage
    val telemetry: StateFlow<SystemTelemetrySnapshot> = systemMonitor.telemetry

    init {
        systemMonitor.startMonitoring()
        viewModelScope.launch {
            repository.configurationFlow.collectLatest { cfg ->
                IslandStateCoordinator.syncConfiguration(cfg)
                if (cfg.isIslandEnabled && PermissionManager.checkPermissions(appContext).canDrawOverlays) {
                    IslandOverlayService.startOrUpdateOverlay(appContext)
                } else if (!cfg.isIslandEnabled) {
                    IslandOverlayService.stopOverlay(appContext)
                }
            }
        }
    }

    override fun onCleared() {
        systemMonitor.stopMonitoring()
        super.onCleared()
    }

    fun selectSection(section: LeovanSection) {
        _currentSection.value = section
    }

    fun refreshPermissionsAndTelemetry() {
        val currentPerms = PermissionManager.checkPermissions(appContext)
        _permissions.value = currentPerms
        systemMonitor.refreshDisplayMetrics()
        if (configuration.value.isIslandEnabled && currentPerms.canDrawOverlays) {
            IslandOverlayService.startOrUpdateOverlay(appContext)
        }
    }

    fun updateConfiguration(transform: (IslandConfiguration) -> IslandConfiguration) {
        viewModelScope.launch {
            repository.updateConfiguration(transform)
        }
    }

    fun toggleMasterIslandEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateConfiguration { it.copy(isIslandEnabled = enabled) }
            if (enabled) {
                if (_permissions.value.canDrawOverlays) {
                    IslandOverlayService.startOrUpdateOverlay(appContext)
                    IslandStateCoordinator.postStatusMessage("Dynamic Island & Layanan Overlay diaktifkan")
                } else {
                    IslandStateCoordinator.postStatusMessage("Pratinjau Dynamic Island aktif. Izinkan Overlay untuk tampil di atas aplikasi lain.")
                }
            } else {
                IslandOverlayService.stopOverlay(appContext)
                IslandStateCoordinator.postStatusMessage("Dynamic Island dinonaktifkan")
            }
        }
    }

    fun resetToDefaultSettings() {
        viewModelScope.launch {
            repository.resetToDefaultConfiguration()
            IslandStateCoordinator.postStatusMessage("Seluruh pengaturan telah dipulihkan ke nilai awal")
        }
    }

    fun applyPreset(preset: IslandPresetEntity) {
        viewModelScope.launch {
            repository.applyPreset(preset)
            IslandStateCoordinator.postStatusMessage("Preset \"${preset.name}\" berhasil diterapkan")
        }
    }

    fun saveCustomPreset(name: String, description: String) {
        viewModelScope.launch {
            repository.saveCurrentAsPreset(name, description, configuration.value)
            IslandStateCoordinator.postStatusMessage("Preset kustom \"$name\" disimpan secara lokal")
        }
    }

    fun deleteCustomPreset(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomPreset(id)
            IslandStateCoordinator.postStatusMessage("Preset kustom dihapus")
        }
    }

    fun updateAppFilterRule(rule: AppFilterRuleEntity) {
        viewModelScope.launch {
            repository.updateAppFilterRule(rule)
        }
    }

    fun addCustomAppRule(packageName: String, appName: String, category: String) {
        if (packageName.isBlank() || appName.isBlank()) return
        viewModelScope.launch {
            repository.addOrUpdateAppFilterRule(
                AppFilterRuleEntity(
                    packageName = packageName.trim(),
                    appName = appName.trim(),
                    categoryLabel = category.trim().ifEmpty { "Aplikasi Pengguna" },
                    isAllowed = true,
                    showMessagePreview = true,
                    allowQuickReply = true
                )
            )
            IslandStateCoordinator.postStatusMessage("Aplikasi \"$appName\" ditambahkan ke daftar filter")
        }
    }

    fun deleteNotificationRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteNotificationRecord(id)
        }
    }

    fun clearAllNotificationRecords() {
        viewModelScope.launch {
            repository.clearAllNotifications()
            IslandStateCoordinator.postStatusMessage("Riwayat notifikasi lokal telah dibersihkan")
        }
    }

    fun triggerSimulatedNotificationAndSave(
        appName: String,
        packageName: String,
        sender: String,
        message: String
    ) {
        IslandStateCoordinator.triggerSimulatedNotification(
            appName = appName,
            packageName = packageName,
            sender = sender,
            message = message
        )
        if (configuration.value.saveNotificationHistory) {
            viewModelScope.launch {
                repository.recordNotification(
                    NotificationRecordEntity(
                        packageName = packageName,
                        appName = appName,
                        senderName = sender,
                        messageContent = message,
                        canQuickReply = true
                    )
                )
            }
        }
    }

    fun setMediaVolume(volume: Int) {
        systemMonitor.adjustMediaVolume(volume)
    }
}
