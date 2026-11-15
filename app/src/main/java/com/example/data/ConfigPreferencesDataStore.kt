package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.leovanDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "leovanisland_preferences"
)

class ConfigPreferencesDataStore(private val context: Context) {

    private object Keys {
        val IS_ENABLED = booleanPreferencesKey("is_island_enabled")
        val COMPACT_WIDTH = intPreferencesKey("compact_width_dp")
        val COMPACT_HEIGHT = intPreferencesKey("compact_height_dp")
        val EXPANDED_WIDTH = intPreferencesKey("expanded_width_dp")
        val EXPANDED_HEIGHT = intPreferencesKey("expanded_height_dp")
        val OFFSET_X = intPreferencesKey("offset_x_dp")
        val OFFSET_Y = intPreferencesKey("offset_y_dp")
        val CORNER_RADIUS = intPreferencesKey("corner_radius_dp")
        val OPACITY = intPreferencesKey("opacity_percent")
        val SHOW_BORDER = booleanPreferencesKey("show_border_stroke")
        val NOTCH_MODE = stringPreferencesKey("notch_mode")

        val ANIMATION_STYLE = stringPreferencesKey("animation_style")
        val ANIMATION_DURATION = intPreferencesKey("animation_duration_ms")
        val AUTO_HIDE_ENABLED = booleanPreferencesKey("auto_hide_enabled")
        val AUTO_HIDE_SEC = intPreferencesKey("auto_hide_duration_sec")
        val SPLIT_PILL_ENABLED = booleanPreferencesKey("split_pill_enabled")
        val SWIPE_HIDE_ENABLED = booleanPreferencesKey("swipe_to_hide_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")

        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SHOW_MESSAGE_PREVIEW = booleanPreferencesKey("show_message_preview")
        val QUICK_REPLY_ENABLED = booleanPreferencesKey("quick_reply_enabled")
        val GROUP_NOTIFICATIONS = booleanPreferencesKey("group_notifications_enabled")
        val HIDE_SENSITIVE_LOCKED = booleanPreferencesKey("hide_sensitive_when_locked")
        val SAVE_NOTIFICATION_HISTORY = booleanPreferencesKey("save_notification_history")

        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val SHOW_ALBUM_COVER = booleanPreferencesKey("show_album_cover")
        val SHOW_MUSIC_CONTROLS = booleanPreferencesKey("show_music_controls")
        val SHOW_MUSIC_VISUALIZER = booleanPreferencesKey("show_music_visualizer")

        val BATTERY_ENABLED = booleanPreferencesKey("battery_alert_enabled")
        val CHARGING_ENABLED = booleanPreferencesKey("charging_indicator_enabled")
        val FULL_CHARGE_ENABLED = booleanPreferencesKey("full_charge_alert_enabled")
        val TIMER_ENABLED = booleanPreferencesKey("timer_island_enabled")
        val STOPWATCH_ENABLED = booleanPreferencesKey("stopwatch_island_enabled")
        val CLIPBOARD_ENABLED = booleanPreferencesKey("clipboard_indicator_enabled")
        val BLUETOOTH_ENABLED = booleanPreferencesKey("bluetooth_indicator_enabled")
        val WIFI_ENABLED = booleanPreferencesKey("wifi_indicator_enabled")
        val HOTSPOT_ENABLED = booleanPreferencesKey("hotspot_indicator_enabled")
        val VOLUME_ENABLED = booleanPreferencesKey("volume_indicator_enabled")
        val SCREEN_REC_ENABLED = booleanPreferencesKey("screen_recording_indicator_enabled")
    }

    val configurationFlow: Flow<IslandConfiguration> = context.leovanDataStore.data.map { prefs ->
        val default = IslandConfiguration()
        val animStyle = prefs[Keys.ANIMATION_STYLE]?.let {
            runCatching { AnimationStyle.valueOf(it) }.getOrNull()
        } ?: default.animationStyle

        val notchMode = prefs[Keys.NOTCH_MODE]?.let {
            runCatching { NotchCompatibilityMode.valueOf(it) }.getOrNull()
        } ?: default.notchMode

        IslandConfiguration(
            isIslandEnabled = prefs[Keys.IS_ENABLED] ?: default.isIslandEnabled,
            compactWidthDp = prefs[Keys.COMPACT_WIDTH] ?: default.compactWidthDp,
            compactHeightDp = prefs[Keys.COMPACT_HEIGHT] ?: default.compactHeightDp,
            expandedWidthDp = prefs[Keys.EXPANDED_WIDTH] ?: default.expandedWidthDp,
            expandedHeightDp = prefs[Keys.EXPANDED_HEIGHT] ?: default.expandedHeightDp,
            offsetXDp = prefs[Keys.OFFSET_X] ?: default.offsetXDp,
            offsetYDp = prefs[Keys.OFFSET_Y] ?: default.offsetYDp,
            cornerRadiusDp = prefs[Keys.CORNER_RADIUS] ?: default.cornerRadiusDp,
            opacityPercent = prefs[Keys.OPACITY] ?: default.opacityPercent,
            showBorderStroke = prefs[Keys.SHOW_BORDER] ?: default.showBorderStroke,
            notchMode = notchMode,
            animationStyle = animStyle,
            animationDurationMs = prefs[Keys.ANIMATION_DURATION] ?: default.animationDurationMs,
            autoHideEnabled = prefs[Keys.AUTO_HIDE_ENABLED] ?: default.autoHideEnabled,
            autoHideDurationSec = prefs[Keys.AUTO_HIDE_SEC] ?: default.autoHideDurationSec,
            splitPillEnabled = prefs[Keys.SPLIT_PILL_ENABLED] ?: default.splitPillEnabled,
            swipeToHideEnabled = prefs[Keys.SWIPE_HIDE_ENABLED] ?: default.swipeToHideEnabled,
            hapticFeedbackEnabled = prefs[Keys.HAPTIC_ENABLED] ?: default.hapticFeedbackEnabled,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: default.notificationsEnabled,
            showMessagePreview = prefs[Keys.SHOW_MESSAGE_PREVIEW] ?: default.showMessagePreview,
            quickReplyEnabled = prefs[Keys.QUICK_REPLY_ENABLED] ?: default.quickReplyEnabled,
            groupNotificationsEnabled = prefs[Keys.GROUP_NOTIFICATIONS] ?: default.groupNotificationsEnabled,
            hideSensitiveWhenLocked = prefs[Keys.HIDE_SENSITIVE_LOCKED] ?: default.hideSensitiveWhenLocked,
            saveNotificationHistory = prefs[Keys.SAVE_NOTIFICATION_HISTORY] ?: default.saveNotificationHistory,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: default.musicEnabled,
            showAlbumCover = prefs[Keys.SHOW_ALBUM_COVER] ?: default.showAlbumCover,
            showMusicControls = prefs[Keys.SHOW_MUSIC_CONTROLS] ?: default.showMusicControls,
            showMusicVisualizer = prefs[Keys.SHOW_MUSIC_VISUALIZER] ?: default.showMusicVisualizer,
            batteryAlertEnabled = prefs[Keys.BATTERY_ENABLED] ?: default.batteryAlertEnabled,
            chargingIndicatorEnabled = prefs[Keys.CHARGING_ENABLED] ?: default.chargingIndicatorEnabled,
            fullChargeAlertEnabled = prefs[Keys.FULL_CHARGE_ENABLED] ?: default.fullChargeAlertEnabled,
            timerIslandEnabled = prefs[Keys.TIMER_ENABLED] ?: default.timerIslandEnabled,
            stopwatchIslandEnabled = prefs[Keys.STOPWATCH_ENABLED] ?: default.stopwatchIslandEnabled,
            clipboardIndicatorEnabled = prefs[Keys.CLIPBOARD_ENABLED] ?: default.clipboardIndicatorEnabled,
            bluetoothIndicatorEnabled = prefs[Keys.BLUETOOTH_ENABLED] ?: default.bluetoothIndicatorEnabled,
            wifiIndicatorEnabled = prefs[Keys.WIFI_ENABLED] ?: default.wifiIndicatorEnabled,
            hotspotIndicatorEnabled = prefs[Keys.HOTSPOT_ENABLED] ?: default.hotspotIndicatorEnabled,
            volumeIndicatorEnabled = prefs[Keys.VOLUME_ENABLED] ?: default.volumeIndicatorEnabled,
            screenRecordingIndicatorEnabled = prefs[Keys.SCREEN_REC_ENABLED] ?: default.screenRecordingIndicatorEnabled
        )
    }

    suspend fun updateConfiguration(transform: (IslandConfiguration) -> IslandConfiguration) {
        context.leovanDataStore.edit { prefs ->
            val current = IslandConfiguration(
                isIslandEnabled = prefs[Keys.IS_ENABLED] ?: true,
                compactWidthDp = prefs[Keys.COMPACT_WIDTH] ?: 128,
                compactHeightDp = prefs[Keys.COMPACT_HEIGHT] ?: 36,
                expandedWidthDp = prefs[Keys.EXPANDED_WIDTH] ?: 352,
                expandedHeightDp = prefs[Keys.EXPANDED_HEIGHT] ?: 178,
                offsetXDp = prefs[Keys.OFFSET_X] ?: 0,
                offsetYDp = prefs[Keys.OFFSET_Y] ?: 6,
                cornerRadiusDp = prefs[Keys.CORNER_RADIUS] ?: 28,
                opacityPercent = prefs[Keys.OPACITY] ?: 100,
                showBorderStroke = prefs[Keys.SHOW_BORDER] ?: true,
                notchMode = prefs[Keys.NOTCH_MODE]?.let { runCatching { NotchCompatibilityMode.valueOf(it) }.getOrNull() }
                    ?: NotchCompatibilityMode.PUNCH_HOLE_TENGAH,
                animationStyle = prefs[Keys.ANIMATION_STYLE]?.let { runCatching { AnimationStyle.valueOf(it) }.getOrNull() }
                    ?: AnimationStyle.SPRING,
                animationDurationMs = prefs[Keys.ANIMATION_DURATION] ?: 340,
                autoHideEnabled = prefs[Keys.AUTO_HIDE_ENABLED] ?: true,
                autoHideDurationSec = prefs[Keys.AUTO_HIDE_SEC] ?: 6,
                splitPillEnabled = prefs[Keys.SPLIT_PILL_ENABLED] ?: true,
                swipeToHideEnabled = prefs[Keys.SWIPE_HIDE_ENABLED] ?: true,
                hapticFeedbackEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                showMessagePreview = prefs[Keys.SHOW_MESSAGE_PREVIEW] ?: true,
                quickReplyEnabled = prefs[Keys.QUICK_REPLY_ENABLED] ?: true,
                groupNotificationsEnabled = prefs[Keys.GROUP_NOTIFICATIONS] ?: true,
                hideSensitiveWhenLocked = prefs[Keys.HIDE_SENSITIVE_LOCKED] ?: true,
                saveNotificationHistory = prefs[Keys.SAVE_NOTIFICATION_HISTORY] ?: true,
                musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
                showAlbumCover = prefs[Keys.SHOW_ALBUM_COVER] ?: true,
                showMusicControls = prefs[Keys.SHOW_MUSIC_CONTROLS] ?: true,
                showMusicVisualizer = prefs[Keys.SHOW_MUSIC_VISUALIZER] ?: true,
                batteryAlertEnabled = prefs[Keys.BATTERY_ENABLED] ?: true,
                chargingIndicatorEnabled = prefs[Keys.CHARGING_ENABLED] ?: true,
                fullChargeAlertEnabled = prefs[Keys.FULL_CHARGE_ENABLED] ?: true,
                timerIslandEnabled = prefs[Keys.TIMER_ENABLED] ?: true,
                stopwatchIslandEnabled = prefs[Keys.STOPWATCH_ENABLED] ?: true,
                clipboardIndicatorEnabled = prefs[Keys.CLIPBOARD_ENABLED] ?: true,
                bluetoothIndicatorEnabled = prefs[Keys.BLUETOOTH_ENABLED] ?: true,
                wifiIndicatorEnabled = prefs[Keys.WIFI_ENABLED] ?: true,
                hotspotIndicatorEnabled = prefs[Keys.HOTSPOT_ENABLED] ?: true,
                volumeIndicatorEnabled = prefs[Keys.VOLUME_ENABLED] ?: true,
                screenRecordingIndicatorEnabled = prefs[Keys.SCREEN_REC_ENABLED] ?: true
            )
            val updated = transform(current)
            writeConfigToPrefs(prefs, updated)
        }
    }

    suspend fun resetToDefaults() {
        context.leovanDataStore.edit { prefs ->
            prefs.clear()
            writeConfigToPrefs(prefs, IslandConfiguration())
        }
    }

    private fun writeConfigToPrefs(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        config: IslandConfiguration
    ) {
        prefs[Keys.IS_ENABLED] = config.isIslandEnabled
        prefs[Keys.COMPACT_WIDTH] = config.compactWidthDp.coerceIn(88, 220)
        prefs[Keys.COMPACT_HEIGHT] = config.compactHeightDp.coerceIn(26, 56)
        prefs[Keys.EXPANDED_WIDTH] = config.expandedWidthDp.coerceIn(280, 400)
        prefs[Keys.EXPANDED_HEIGHT] = config.expandedHeightDp.coerceIn(130, 260)
        prefs[Keys.OFFSET_X] = config.offsetXDp.coerceIn(-120, 120)
        prefs[Keys.OFFSET_Y] = config.offsetYDp.coerceIn(0, 64)
        prefs[Keys.CORNER_RADIUS] = config.cornerRadiusDp.coerceIn(12, 48)
        prefs[Keys.OPACITY] = config.opacityPercent.coerceIn(60, 100)
        prefs[Keys.SHOW_BORDER] = config.showBorderStroke
        prefs[Keys.NOTCH_MODE] = config.notchMode.name
        prefs[Keys.ANIMATION_STYLE] = config.animationStyle.name
        prefs[Keys.ANIMATION_DURATION] = config.animationDurationMs.coerceIn(150, 800)
        prefs[Keys.AUTO_HIDE_ENABLED] = config.autoHideEnabled
        prefs[Keys.AUTO_HIDE_SEC] = config.autoHideDurationSec.coerceIn(2, 20)
        prefs[Keys.SPLIT_PILL_ENABLED] = config.splitPillEnabled
        prefs[Keys.SWIPE_HIDE_ENABLED] = config.swipeToHideEnabled
        prefs[Keys.HAPTIC_ENABLED] = config.hapticFeedbackEnabled
        prefs[Keys.NOTIFICATIONS_ENABLED] = config.notificationsEnabled
        prefs[Keys.SHOW_MESSAGE_PREVIEW] = config.showMessagePreview
        prefs[Keys.QUICK_REPLY_ENABLED] = config.quickReplyEnabled
        prefs[Keys.GROUP_NOTIFICATIONS] = config.groupNotificationsEnabled
        prefs[Keys.HIDE_SENSITIVE_LOCKED] = config.hideSensitiveWhenLocked
        prefs[Keys.SAVE_NOTIFICATION_HISTORY] = config.saveNotificationHistory
        prefs[Keys.MUSIC_ENABLED] = config.musicEnabled
        prefs[Keys.SHOW_ALBUM_COVER] = config.showAlbumCover
        prefs[Keys.SHOW_MUSIC_CONTROLS] = config.showMusicControls
        prefs[Keys.SHOW_MUSIC_VISUALIZER] = config.showMusicVisualizer
        prefs[Keys.BATTERY_ENABLED] = config.batteryAlertEnabled
        prefs[Keys.CHARGING_ENABLED] = config.chargingIndicatorEnabled
        prefs[Keys.FULL_CHARGE_ENABLED] = config.fullChargeAlertEnabled
        prefs[Keys.TIMER_ENABLED] = config.timerIslandEnabled
        prefs[Keys.STOPWATCH_ENABLED] = config.stopwatchIslandEnabled
        prefs[Keys.CLIPBOARD_ENABLED] = config.clipboardIndicatorEnabled
        prefs[Keys.BLUETOOTH_ENABLED] = config.bluetoothIndicatorEnabled
        prefs[Keys.WIFI_ENABLED] = config.wifiIndicatorEnabled
        prefs[Keys.HOTSPOT_ENABLED] = config.hotspotIndicatorEnabled
        prefs[Keys.VOLUME_ENABLED] = config.volumeIndicatorEnabled
        prefs[Keys.SCREEN_REC_ENABLED] = config.screenRecordingIndicatorEnabled
    }
}
