package com.example.data

import kotlinx.coroutines.flow.Flow

class LeovanRepository(
    private val dataStore: ConfigPreferencesDataStore,
    private val dao: LeovanDao
) {
    val configurationFlow: Flow<IslandConfiguration> = dataStore.configurationFlow
    val notificationHistoryFlow: Flow<List<NotificationRecordEntity>> = dao.observeNotificationRecords()
    val appFilterRulesFlow: Flow<List<AppFilterRuleEntity>> = dao.observeAppFilterRules()
    val presetsFlow: Flow<List<IslandPresetEntity>> = dao.observePresets()

    suspend fun updateConfiguration(transform: (IslandConfiguration) -> IslandConfiguration) {
        dataStore.updateConfiguration(transform)
    }

    suspend fun resetToDefaultConfiguration() {
        dataStore.resetToDefaults()
    }

    suspend fun applyPreset(preset: IslandPresetEntity) {
        val style = runCatching { AnimationStyle.valueOf(preset.animationStyleName) }
            .getOrDefault(AnimationStyle.SPRING)
        dataStore.updateConfiguration { current ->
            current.copy(
                compactWidthDp = preset.compactWidthDp,
                compactHeightDp = preset.compactHeightDp,
                expandedWidthDp = preset.expandedWidthDp,
                expandedHeightDp = preset.expandedHeightDp,
                offsetXDp = preset.offsetXDp,
                offsetYDp = preset.offsetYDp,
                cornerRadiusDp = preset.cornerRadiusDp,
                animationStyle = style,
                animationDurationMs = preset.animationDurationMs,
                autoHideDurationSec = preset.autoHideSeconds,
                opacityPercent = preset.opacityPercent
            )
        }
    }

    suspend fun saveCurrentAsPreset(name: String, description: String, config: IslandConfiguration) {
        dao.insertPreset(
            IslandPresetEntity(
                name = name.trim().ifEmpty { "Preset Kustom" },
                description = description.trim().ifEmpty { "Profil kustom pengguna" },
                compactWidthDp = config.compactWidthDp,
                compactHeightDp = config.compactHeightDp,
                expandedWidthDp = config.expandedWidthDp,
                expandedHeightDp = config.expandedHeightDp,
                offsetXDp = config.offsetXDp,
                offsetYDp = config.offsetYDp,
                cornerRadiusDp = config.cornerRadiusDp,
                animationStyleName = config.animationStyle.name,
                animationDurationMs = config.animationDurationMs,
                autoHideSeconds = config.autoHideDurationSec,
                opacityPercent = config.opacityPercent,
                isBuiltIn = false
            )
        )
    }

    suspend fun deleteCustomPreset(id: Int) {
        dao.deleteCustomPreset(id)
    }

    suspend fun recordNotification(record: NotificationRecordEntity) {
        dao.insertNotificationRecord(record)
    }

    suspend fun deleteNotificationRecord(id: Long) {
        dao.deleteNotificationById(id)
    }

    suspend fun clearAllNotifications() {
        dao.clearAllNotificationRecords()
    }

    suspend fun updateAppFilterRule(rule: AppFilterRuleEntity) {
        dao.updateAppFilterRule(rule)
    }

    suspend fun addOrUpdateAppFilterRule(rule: AppFilterRuleEntity) {
        dao.upsertAppFilterRules(listOf(rule))
    }

    suspend fun getAppFilterRule(packageName: String): AppFilterRuleEntity? {
        return dao.getAppFilterRule(packageName)
    }
}
