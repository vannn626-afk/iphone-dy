package com.example.service

import android.app.KeyguardManager
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.IslandPayload
import com.example.data.LeovanDatabase
import com.example.data.NotificationActionItem
import com.example.data.NotificationRecordEntity
import com.example.manager.IslandStateCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LeovanNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mediaSessionManager: MediaSessionManager? = null
    private var currentController: MediaController? = null

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            pushMediaUpdateFromController(currentController)
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            pushMediaUpdateFromController(currentController)
        }
    }

    private val sessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        bindPrimaryMediaController(controllers)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        setupMediaSessionListener()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        teardownMediaSessionListener()
    }

    override fun onDestroy() {
        teardownMediaSessionListener()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun setupMediaSessionListener() {
        runCatching {
            val manager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
            mediaSessionManager = manager
            val componentName = ComponentName(this, LeovanNotificationListenerService::class.java)
            val initialControllers = manager.getActiveSessions(componentName)
            bindPrimaryMediaController(initialControllers)
            manager.addOnActiveSessionsChangedListener(sessionsChangedListener, componentName)
        }
    }

    private fun teardownMediaSessionListener() {
        runCatching {
            currentController?.unregisterCallback(mediaControllerCallback)
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        }
        currentController = null
        IslandStateCoordinator.bindActiveMediaController(null)
    }

    private fun bindPrimaryMediaController(controllers: List<MediaController>?) {
        currentController?.unregisterCallback(mediaControllerCallback)
        val active = controllers?.firstOrNull { ctrl ->
            val state = ctrl.playbackState?.state
            state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED || state == PlaybackState.STATE_BUFFERING
        } ?: controllers?.firstOrNull()

        currentController = active
        IslandStateCoordinator.bindActiveMediaController(active)
        if (active != null) {
            active.registerCallback(mediaControllerCallback)
            pushMediaUpdateFromController(active)
        }
    }

    private fun pushMediaUpdateFromController(controller: MediaController?) {
        val cfg = IslandStateCoordinator.configuration.value
        if (!cfg.musicEnabled || controller == null) return

        val metadata = controller.metadata ?: return
        val playbackState = controller.playbackState

        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: return
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: "Artis"
        val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
        val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).coerceAtLeast(1000L)
        val art = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        val position = playbackState?.position?.coerceIn(0L, duration) ?: 0L
        val pkg = controller.packageName ?: ""
        val appLabel = resolveAppName(pkg)

        IslandStateCoordinator.postPayload(
            IslandPayload.MusicPlayback(
                title = title,
                artist = artist,
                album = album,
                appName = appLabel,
                packageName = pkg,
                isPlaying = isPlaying,
                positionMs = position,
                durationMs = duration,
                albumArt = art
            )
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val statusNotif = sbn ?: return
        val pkg = statusNotif.packageName ?: return
        if (pkg == applicationContext.packageName) return
        if (statusNotif.isOngoing) return

        val cfg = IslandStateCoordinator.configuration.value
        if (!cfg.notificationsEnabled) return

        val notification = statusNotif.notification ?: return
        val extras = notification.extras ?: return

        val rawTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: return
        val rawText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: ""

        if (rawTitle.isBlank() && rawText.isBlank()) return

        serviceScope.launch {
            val dao = LeovanDatabase.getInstance(applicationContext).leovanDao()
            val rule = dao.getAppFilterRule(pkg)
            if (rule != null && !rule.isAllowed) return@launch

            if (rule != null && rule.hideSpecificKeywords.isNotBlank()) {
                val keywords = rule.hideSpecificKeywords.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                val combined = "$rawTitle $rawText".lowercase()
                if (keywords.any { combined.contains(it) }) return@launch
            }

            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            val isDeviceLocked = keyguardManager?.isKeyguardLocked == true
            val allowPreview = cfg.showMessagePreview &&
                (rule?.showMessagePreview ?: true) &&
                !(isDeviceLocked && cfg.hideSensitiveWhenLocked)

            val displayedMessage = if (allowPreview) {
                rawText.ifBlank { "Ketuk untuk melihat detail notifikasi" }
            } else {
                "Isi pesan disembunyikan untuk menjaga privasi"
            }

            val actionItems = mutableListOf<NotificationActionItem>()
            var supportsQuickReply = false

            notification.actions?.forEach { action ->
                val actionTitle = action.title?.toString() ?: return@forEach
                val remoteInput = action.remoteInputs?.firstOrNull()
                if (remoteInput != null) {
                    supportsQuickReply = true
                }
                actionItems.add(
                    NotificationActionItem(
                        title = actionTitle,
                        pendingIntent = action.actionIntent,
                        remoteInput = remoteInput,
                        resultKey = remoteInput?.resultKey
                    )
                )
            }

            val canQuickReply = supportsQuickReply &&
                cfg.quickReplyEnabled &&
                (rule?.allowQuickReply ?: true)

            val appName = rule?.appName ?: resolveAppName(pkg)
            val timeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(statusNotif.postTime))

            val groupCount = if (cfg.groupNotificationsEnabled) {
                runCatching {
                    activeNotifications?.count { it.packageName == pkg } ?: 1
                }.getOrDefault(1).coerceAtLeast(1)
            } else {
                1
            }

            if (cfg.saveNotificationHistory) {
                dao.insertNotificationRecord(
                    NotificationRecordEntity(
                        packageName = pkg,
                        appName = appName,
                        senderName = rawTitle,
                        messageContent = displayedMessage,
                        timestamp = statusNotif.postTime,
                        canQuickReply = canQuickReply,
                        isGrouped = groupCount > 1
                    )
                )
            }

            IslandStateCoordinator.postPayload(
                IslandPayload.NotificationItem(
                    id = statusNotif.key ?: statusNotif.id.toString(),
                    packageName = pkg,
                    appName = appName,
                    senderName = rawTitle,
                    messageText = displayedMessage,
                    timeFormatted = timeFormatted,
                    groupCount = groupCount,
                    canQuickReply = canQuickReply,
                    contentIntent = notification.contentIntent,
                    actions = actionItems
                )
            )
        }
    }

    private fun resolveAppName(packageName: String): String {
        return runCatching {
            val pm = applicationContext.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        }.getOrDefault(
            packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        )
    }
}
