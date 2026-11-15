package com.example.manager

import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.media.session.MediaController
import android.os.Bundle
import com.example.data.ConnectivityType
import com.example.data.IslandConfiguration
import com.example.data.IslandDisplayMode
import com.example.data.IslandPayload
import com.example.data.NotificationActionItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object IslandStateCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _configuration = MutableStateFlow(IslandConfiguration())
    val configuration: StateFlow<IslandConfiguration> = _configuration.asStateFlow()

    private val _primaryPayload = MutableStateFlow<IslandPayload>(
        IslandPayload.MusicPlayback(
            title = "Midnight Resonance",
            artist = "Leovan Studio Ensemble",
            album = "Nocturne in Black",
            appName = "Spotify",
            packageName = "com.spotify.music",
            isPlaying = true,
            positionMs = 68_000L,
            durationMs = 214_000L
        )
    )
    val primaryPayload: StateFlow<IslandPayload> = _primaryPayload.asStateFlow()

    private val _secondaryPayload = MutableStateFlow<IslandPayload?>(null)
    val secondaryPayload: StateFlow<IslandPayload?> = _secondaryPayload.asStateFlow()

    private val _displayMode = MutableStateFlow(IslandDisplayMode.COMPACT)
    val displayMode: StateFlow<IslandDisplayMode> = _displayMode.asStateFlow()

    private val _isOverlayServiceRunning = MutableStateFlow(false)
    val isOverlayServiceRunning: StateFlow<Boolean> = _isOverlayServiceRunning.asStateFlow()

    private val _isQuickReplyInputActive = MutableStateFlow(false)
    val isQuickReplyInputActive: StateFlow<Boolean> = _isQuickReplyInputActive.asStateFlow()

    private val _statusFeedbackMessage = MutableStateFlow<String?>(null)
    val statusFeedbackMessage: StateFlow<String?> = _statusFeedbackMessage.asStateFlow()

    // Sesi Media Aktif dari MediaSessionManager
    private var activeMediaController: MediaController? = null

    // Job untuk Timer, Stopwatch, Musik Progres, dan Auto-Hide
    private var timerJob: Job? = null
    private var stopwatchJob: Job? = null
    private var musicTickerJob: Job? = null
    private var autoHideJob: Job? = null

    init {
        startMusicProgressTicker()
    }

    fun syncConfiguration(config: IslandConfiguration) {
        _configuration.value = config
        recalculateDisplayMode()
    }

    fun setOverlayServiceRunning(running: Boolean) {
        _isOverlayServiceRunning.value = running
    }

    fun setQuickReplyInputActive(active: Boolean) {
        _isQuickReplyInputActive.value = active
        if (active) {
            autoHideJob?.cancel()
        } else {
            scheduleAutoHideIfNeeded()
        }
    }

    fun bindActiveMediaController(controller: MediaController?) {
        activeMediaController = controller
    }

    fun postStatusMessage(message: String) {
        _statusFeedbackMessage.value = message
        scope.launch {
            delay(3200L)
            if (_statusFeedbackMessage.value == message) {
                _statusFeedbackMessage.value = null
            }
        }
    }

    fun postPayload(payload: IslandPayload, autoExpand: Boolean = false) {
        val cfg = _configuration.value
        if (!cfg.isIslandEnabled) return

        val currentPrimary = _primaryPayload.value

        // Jika Split Pill diaktifkan dan ada aktivitas latar belakang yang sedang berjalan (Musik/Timer/Stopwatch)
        if (cfg.splitPillEnabled && canCoexistInSplitPill(currentPrimary, payload)) {
            if (payload.priority >= currentPrimary.priority) {
                _secondaryPayload.value = currentPrimary
                _primaryPayload.value = payload
            } else {
                _secondaryPayload.value = payload
            }
            _displayMode.value = if (autoExpand) IslandDisplayMode.EXPANDED else IslandDisplayMode.SPLIT_PILL
        } else {
            // Jika aktivitas sama jenisnya, perbarui langsung
            if (currentPrimary.key == payload.key) {
                _primaryPayload.value = payload
            } else {
                // Simpan aktivitas berkelanjutan sebagai sekunder jika memungkinkan
                if (isPersistentActivity(currentPrimary) && cfg.splitPillEnabled && payload !is IslandPayload.Idle) {
                    _secondaryPayload.value = currentPrimary
                }
                _primaryPayload.value = payload
            }

            if (autoExpand) {
                _displayMode.value = IslandDisplayMode.EXPANDED
            } else if (_displayMode.value == IslandDisplayMode.HIDDEN) {
                recalculateDisplayMode()
            } else if (_displayMode.value != IslandDisplayMode.EXPANDED) {
                recalculateDisplayMode()
            }
        }

        scheduleAutoHideIfNeeded()
    }

    private fun canCoexistInSplitPill(first: IslandPayload, second: IslandPayload): Boolean {
        if (first is IslandPayload.Idle || second is IslandPayload.Idle) return false
        if (first.key == second.key) return false
        return isPersistentActivity(first) || isPersistentActivity(second)
    }

    private fun isPersistentActivity(payload: IslandPayload): Boolean {
        return payload is IslandPayload.MusicPlayback ||
            payload is IslandPayload.TimerActivity ||
            payload is IslandPayload.StopwatchActivity ||
            payload is IslandPayload.ScreenRecordingStatus
    }

    private fun recalculateDisplayMode() {
        val cfg = _configuration.value
        val hasSecondary = _secondaryPayload.value != null && _secondaryPayload.value !is IslandPayload.Idle
        if (_displayMode.value == IslandDisplayMode.EXPANDED) return
        _displayMode.value = if (cfg.splitPillEnabled && hasSecondary) {
            IslandDisplayMode.SPLIT_PILL
        } else {
            IslandDisplayMode.COMPACT
        }
    }

    fun toggleExpandCollapse() {
        val current = _displayMode.value
        if (current == IslandDisplayMode.EXPANDED) {
            _isQuickReplyInputActive.value = false
            val hasSecondary = _secondaryPayload.value != null && _configuration.value.splitPillEnabled
            _displayMode.value = if (hasSecondary) IslandDisplayMode.SPLIT_PILL else IslandDisplayMode.COMPACT
            scheduleAutoHideIfNeeded()
        } else {
            _displayMode.value = IslandDisplayMode.EXPANDED
            autoHideJob?.cancel()
        }
    }

    fun expandSpecificSlot(isSecondarySlot: Boolean) {
        if (isSecondarySlot) {
            val sec = _secondaryPayload.value
            val pri = _primaryPayload.value
            if (sec != null) {
                _primaryPayload.value = sec
                _secondaryPayload.value = pri
            }
        }
        _displayMode.value = IslandDisplayMode.EXPANDED
        autoHideJob?.cancel()
    }

    fun collapseToCompact() {
        _isQuickReplyInputActive.value = false
        val hasSecondary = _secondaryPayload.value != null && _configuration.value.splitPillEnabled
        _displayMode.value = if (hasSecondary) IslandDisplayMode.SPLIT_PILL else IslandDisplayMode.COMPACT
        scheduleAutoHideIfNeeded()
    }

    fun hideManually() {
        _isQuickReplyInputActive.value = false
        _displayMode.value = IslandDisplayMode.HIDDEN
        postStatusMessage("Dynamic Island disembunyikan sementara. Ketuk Tampilkan untuk memunculkan kembali.")
    }

    fun restoreFromHidden() {
        val hasSecondary = _secondaryPayload.value != null && _configuration.value.splitPillEnabled
        _displayMode.value = if (hasSecondary) IslandDisplayMode.SPLIT_PILL else IslandDisplayMode.COMPACT
        scheduleAutoHideIfNeeded()
    }

    private fun scheduleAutoHideIfNeeded() {
        autoHideJob?.cancel()
        val cfg = _configuration.value
        val current = _primaryPayload.value
        if (!cfg.autoHideEnabled) return
        if (_displayMode.value == IslandDisplayMode.EXPANDED && _isQuickReplyInputActive.value) return

        // Aktivitas transien (Notifikasi, Volume, Baterai, Clipboard, Konektivitas) kembali ke aktivitas persisten atau Idle setelah durasi auto-hide
        if (!isPersistentActivity(current) && current !is IslandPayload.Idle) {
            autoHideJob = scope.launch {
                delay(cfg.autoHideDurationSec * 1000L)
                val sec = _secondaryPayload.value
                if (sec != null) {
                    _primaryPayload.value = sec
                    _secondaryPayload.value = null
                    _displayMode.value = IslandDisplayMode.COMPACT
                } else {
                    _primaryPayload.value = IslandPayload.Idle
                    _displayMode.value = IslandDisplayMode.COMPACT
                }
            }
        }
    }

    // Kontrol Pemutar Musik Universal
    fun toggleMusicPlayPause() {
        val controller = activeMediaController
        val currentMusic = findMusicPayload()
        if (controller != null) {
            runCatching {
                val state = controller.playbackState?.state
                if (state == android.media.session.PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                } else {
                    controller.transportControls.play()
                }
            }
        }
        if (currentMusic != null) {
            updateMusicPayload(currentMusic.copy(isPlaying = !currentMusic.isPlaying))
        }
    }

    fun skipToNextTrack() {
        val controller = activeMediaController
        if (controller != null) {
            runCatching { controller.transportControls.skipToNext() }
        }
        val currentMusic = findMusicPayload()
        if (currentMusic != null) {
            val nextCatalog = listOf(
                Triple("Obsidian Horizon", "Leovan Synth Lab", "Monolith EP"),
                Triple("Velvet Silence", "Arka & Nara", "Jakarta After Hours"),
                Triple("Starlight Capsule", "Kinetik Pulse", "Dynamic Waves"),
                Triple("Midnight Resonance", "Leovan Studio Ensemble", "Nocturne in Black")
            )
            val currentIndex = nextCatalog.indexOfFirst { it.first == currentMusic.title }
            val nextItem = nextCatalog[(currentIndex + 1) % nextCatalog.size]
            updateMusicPayload(
                currentMusic.copy(
                    title = nextItem.first,
                    artist = nextItem.second,
                    album = nextItem.third,
                    positionMs = 0L,
                    durationMs = 205_000L,
                    isPlaying = true
                )
            )
        }
    }

    fun skipToPreviousTrack() {
        val controller = activeMediaController
        if (controller != null) {
            runCatching { controller.transportControls.skipToPrevious() }
        }
        val currentMusic = findMusicPayload()
        if (currentMusic != null) {
            if (currentMusic.positionMs > 10_000L) {
                updateMusicPayload(currentMusic.copy(positionMs = 0L))
            } else {
                updateMusicPayload(
                    currentMusic.copy(
                        title = "Solaris Minimal",
                        artist = "Vann Acoustic",
                        album = "Pure Black Sessions",
                        positionMs = 0L,
                        isPlaying = true
                    )
                )
            }
        }
    }

    fun seekMusicToFraction(fraction: Float) {
        val currentMusic = findMusicPayload() ?: return
        val newPos = (currentMusic.durationMs * fraction.coerceIn(0f, 1f)).toLong()
        activeMediaController?.let {
            runCatching { it.transportControls.seekTo(newPos) }
        }
        updateMusicPayload(currentMusic.copy(positionMs = newPos))
    }

    private fun findMusicPayload(): IslandPayload.MusicPlayback? {
        return (_primaryPayload.value as? IslandPayload.MusicPlayback)
            ?: (_secondaryPayload.value as? IslandPayload.MusicPlayback)
    }

    private fun updateMusicPayload(updated: IslandPayload.MusicPlayback) {
        if (_primaryPayload.value is IslandPayload.MusicPlayback) {
            _primaryPayload.value = updated
        } else if (_secondaryPayload.value is IslandPayload.MusicPlayback) {
            _secondaryPayload.value = updated
        } else {
            postPayload(updated)
        }
    }

    private fun startMusicProgressTicker() {
        musicTickerJob?.cancel()
        musicTickerJob = scope.launch {
            while (true) {
                delay(1000L)
                val music = findMusicPayload()
                if (music != null && music.isPlaying) {
                    val nextPos = if (music.positionMs + 1000L >= music.durationMs) {
                        0L
                    } else {
                        music.positionMs + 1000L
                    }
                    updateMusicPayload(music.copy(positionMs = nextPos))
                }
            }
        }
    }

    // Kontrol Utilitas Timer Sistem
    fun startOrUpdateTimer(totalSeconds: Int, label: String = "Timer Fokus") {
        timerJob?.cancel()
        val initial = IslandPayload.TimerActivity(
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            isRunning = true,
            label = label
        )
        postPayload(initial)
        runTimerLoop()
    }

    fun toggleTimerPauseResume() {
        val timer = (_primaryPayload.value as? IslandPayload.TimerActivity)
            ?: (_secondaryPayload.value as? IslandPayload.TimerActivity)
            ?: return
        val updated = timer.copy(isRunning = !timer.isRunning)
        replacePayloadByKey("timer", updated)
        if (updated.isRunning) {
            runTimerLoop()
        } else {
            timerJob?.cancel()
        }
    }

    fun stopAndClearTimer() {
        timerJob?.cancel()
        removePayloadByKey("timer")
    }

    private fun runTimerLoop() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000L)
                val currentTimer = (_primaryPayload.value as? IslandPayload.TimerActivity)
                    ?: (_secondaryPayload.value as? IslandPayload.TimerActivity)
                    ?: break
                if (!currentTimer.isRunning) break
                val nextRemaining = (currentTimer.remainingSeconds - 1).coerceAtLeast(0)
                val updated = currentTimer.copy(
                    remainingSeconds = nextRemaining,
                    isRunning = nextRemaining > 0
                )
                replacePayloadByKey("timer", updated)
                if (nextRemaining == 0) {
                    postStatusMessage("Waktu ${currentTimer.label} telah selesai!")
                    _displayMode.value = IslandDisplayMode.EXPANDED
                    break
                }
            }
        }
    }

    // Kontrol Utilitas Stopwatch Sistem
    fun startOrResumeStopwatch() {
        val existing = (_primaryPayload.value as? IslandPayload.StopwatchActivity)
            ?: (_secondaryPayload.value as? IslandPayload.StopwatchActivity)
        val initial = existing?.copy(isRunning = true)
            ?: IslandPayload.StopwatchActivity(elapsedMillis = 0L, isRunning = true, lapCount = 1)
        postPayload(initial)
        stopwatchJob?.cancel()
        stopwatchJob = scope.launch {
            while (true) {
                delay(100L)
                val sw = (_primaryPayload.value as? IslandPayload.StopwatchActivity)
                    ?: (_secondaryPayload.value as? IslandPayload.StopwatchActivity)
                    ?: break
                if (!sw.isRunning) break
                replacePayloadByKey("stopwatch", sw.copy(elapsedMillis = sw.elapsedMillis + 100L))
            }
        }
    }

    fun pauseStopwatch() {
        stopwatchJob?.cancel()
        val sw = (_primaryPayload.value as? IslandPayload.StopwatchActivity)
            ?: (_secondaryPayload.value as? IslandPayload.StopwatchActivity)
            ?: return
        replacePayloadByKey("stopwatch", sw.copy(isRunning = false))
    }

    fun lapStopwatch() {
        val sw = (_primaryPayload.value as? IslandPayload.StopwatchActivity)
            ?: (_secondaryPayload.value as? IslandPayload.StopwatchActivity)
            ?: return
        replacePayloadByKey("stopwatch", sw.copy(lapCount = sw.lapCount + 1))
    }

    fun resetAndClearStopwatch() {
        stopwatchJob?.cancel()
        removePayloadByKey("stopwatch")
    }

    // Balasan Cepat Notifikasi (RemoteInput)
    fun sendQuickReply(context: Context, notification: IslandPayload.NotificationItem, replyText: String): Boolean {
        if (replyText.isBlank()) return false
        val replyAction = notification.actions.firstOrNull { it.remoteInput != null && it.pendingIntent != null }
        if (replyAction?.remoteInput != null && replyAction.pendingIntent != null) {
            runCatching {
                val intent = Intent()
                val bundle = Bundle().apply {
                    putCharSequence(replyAction.remoteInput.resultKey, replyText.trim())
                }
                RemoteInput.addResultsToIntent(arrayOf(replyAction.remoteInput), intent, bundle)
                replyAction.pendingIntent.send(context, 0, intent)
            }
        }
        _isQuickReplyInputActive.value = false
        postStatusMessage("Balasan terkirim ke ${notification.senderName}: \"${replyText.trim()}\"")
        removePayloadByKey(notification.key)
        return true
    }

    fun dismissCurrentNotification() {
        val current = _primaryPayload.value
        if (current is IslandPayload.NotificationItem) {
            removePayloadByKey(current.key)
        } else {
            collapseToCompact()
        }
    }

    private fun replacePayloadByKey(key: String, updated: IslandPayload) {
        if (_primaryPayload.value.key == key) {
            _primaryPayload.value = updated
        } else if (_secondaryPayload.value?.key == key) {
            _secondaryPayload.value = updated
        }
    }

    fun removePayloadByKey(key: String) {
        if (_primaryPayload.value.key == key) {
            val sec = _secondaryPayload.value
            if (sec != null) {
                _primaryPayload.value = sec
                _secondaryPayload.value = null
            } else {
                _primaryPayload.value = IslandPayload.Idle
            }
            _displayMode.value = IslandDisplayMode.COMPACT
        } else if (_secondaryPayload.value?.key == key) {
            _secondaryPayload.value = null
            if (_displayMode.value == IslandDisplayMode.SPLIT_PILL) {
                _displayMode.value = IslandDisplayMode.COMPACT
            }
        }
    }

    // Simulator Interaktif untuk Menguji Semua Skenario Dynamic Island
    fun triggerSimulatedNotification(
        appName: String = "WhatsApp",
        packageName: String = "com.whatsapp",
        sender: String = "Alya Putri",
        message: String = "Sudah sampai di lobi utama, kabari kalau sudah turun ya!"
    ) {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        postPayload(
            IslandPayload.NotificationItem(
                id = System.currentTimeMillis().toString(),
                packageName = packageName,
                appName = appName,
                senderName = sender,
                messageText = message,
                timeFormatted = timeStr,
                groupCount = 2,
                canQuickReply = true,
                actions = listOf(
                    NotificationActionItem(title = "Balas Cepat"),
                    NotificationActionItem(title = "Tandai Dibaca")
                )
            ),
            autoExpand = false
        )
    }

    fun triggerSimulatedSplitPillMusicAndTimer() {
        _configuration.update { it.copy(splitPillEnabled = true) }
        val music = IslandPayload.MusicPlayback(
            title = "Nocturne in Obsidian",
            artist = "Leovan Studio",
            album = "Black Edition",
            appName = "Spotify",
            packageName = "com.spotify.music",
            isPlaying = true,
            positionMs = 42_000L,
            durationMs = 198_000L
        )
        _primaryPayload.value = music
        startOrUpdateTimer(totalSeconds = 180, label = "Seduh Kopi")
        _displayMode.value = IslandDisplayMode.SPLIT_PILL
        postStatusMessage("Mode Split Pill aktif: Musik + Timer berjalan berdampingan")
    }

    fun triggerSimulatedBatteryCharging(percentage: Int = 88, isFull: Boolean = false) {
        postPayload(
            IslandPayload.BatteryStatus(
                percentage = if (isFull) 100 else percentage,
                isCharging = true,
                isFull = isFull,
                powerSourceLabel = if (isFull) "Pengisian Daya Penuh" else "Super Fast Charging 45W",
                temperatureCelsius = 32.4f
            )
        )
    }

    fun triggerSimulatedConnectivity(type: ConnectivityType, active: Boolean = true) {
        val payload = when (type) {
            ConnectivityType.BLUETOOTH -> IslandPayload.ConnectivityStatus(
                type = ConnectivityType.BLUETOOTH,
                title = if (active) "AirPods Pro Terhubung" else "Bluetooth Terputus",
                subtitle = if (active) "Baterai Earbuds 95% • Audio Spasial" else "Perangkat audio dinonaktifkan",
                isActive = active
            )
            ConnectivityType.WIFI -> IslandPayload.ConnectivityStatus(
                type = ConnectivityType.WIFI,
                title = if (active) "Wi-Fi 6 Terhubung" else "Wi-Fi Terputus",
                subtitle = if (active) "Leovan_5GHz • Kecepatan Tinggi" else "Beralih ke Data Seluler",
                isActive = active
            )
            ConnectivityType.HOTSPOT -> IslandPayload.ConnectivityStatus(
                type = ConnectivityType.HOTSPOT,
                title = if (active) "Hotspot Pribadi Aktif" else "Hotspot Dinonaktifkan",
                subtitle = if (active) "1 Perangkat Terhubung • WPA3" else "Berbagi koneksi dihentikan",
                isActive = active
            )
        }
        postPayload(payload)
    }

    fun triggerSimulatedClipboard(text: String = "https://developer.android.com/jetpack/compose") {
        postPayload(
            IslandPayload.ClipboardCopied(
                previewText = text,
                charCount = text.length
            )
        )
    }

    fun triggerSimulatedScreenRecording(isRecording: Boolean = true) {
        if (isRecording) {
            postPayload(
                IslandPayload.ScreenRecordingStatus(
                    elapsedSeconds = 24,
                    isRecording = true
                )
            )
        } else {
            removePayloadByKey("screen_record")
        }
    }
}
