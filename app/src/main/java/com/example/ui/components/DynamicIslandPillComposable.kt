package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AnimationStyle
import com.example.data.ConnectivityType
import com.example.data.IslandConfiguration
import com.example.data.IslandDisplayMode
import com.example.data.IslandPayload
import com.example.manager.IslandStateCoordinator
import com.example.ui.theme.LeovanBlack
import com.example.ui.theme.LeovanBorder
import com.example.ui.theme.LeovanCardSecondary
import com.example.ui.theme.LeovanTextPrimary
import com.example.ui.theme.LeovanTextSecondary
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicIslandPillComposable(
    config: IslandConfiguration,
    primaryPayload: IslandPayload,
    secondaryPayload: IslandPayload?,
    displayMode: IslandDisplayMode,
    modifier: Modifier = Modifier,
    isInteractivePreview: Boolean = false
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var dragDistanceX by remember { mutableFloatStateOf(0f) }

    val durationMs = config.animationDurationMs.coerceIn(150, 800)

    val dpAnimationSpec = remember(config.animationStyle, durationMs) {
        when (config.animationStyle) {
            AnimationStyle.SPRING -> spring<Dp>(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
            AnimationStyle.MORPH -> spring<Dp>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
            AnimationStyle.FADE, AnimationStyle.SLIDE -> tween(
                durationMillis = durationMs,
                easing = FastOutSlowInEasing
            )
        }
    }

    val targetWidth = when (displayMode) {
        IslandDisplayMode.HIDDEN -> 0.dp
        IslandDisplayMode.COMPACT -> {
            if (primaryPayload is IslandPayload.Idle) {
                config.compactWidthDp.dp
            } else {
                (config.compactWidthDp + 44).coerceAtMost(240).dp
            }
        }
        IslandDisplayMode.SPLIT_PILL -> (config.compactWidthDp + 96).coerceAtMost(290).dp
        IslandDisplayMode.EXPANDED -> config.expandedWidthDp.dp
    }

    val targetHeight = when (displayMode) {
        IslandDisplayMode.HIDDEN -> 0.dp
        IslandDisplayMode.COMPACT, IslandDisplayMode.SPLIT_PILL -> config.compactHeightDp.dp
        IslandDisplayMode.EXPANDED -> {
            when (primaryPayload) {
                is IslandPayload.NotificationItem -> (config.expandedHeightDp + 16).dp
                is IslandPayload.MusicPlayback -> config.expandedHeightDp.dp
                else -> (config.expandedHeightDp - 24).coerceAtLeast(136).dp
            }
        }
    }

    val animatedWidth by animateDpAsState(targetValue = targetWidth, animationSpec = dpAnimationSpec, label = "island_width")
    val animatedHeight by animateDpAsState(targetValue = targetHeight, animationSpec = dpAnimationSpec, label = "island_height")
    val animatedCorner by animateDpAsState(
        targetValue = if (displayMode == IslandDisplayMode.EXPANDED) {
            (config.cornerRadiusDp + 6).dp
        } else {
            config.cornerRadiusDp.dp
        },
        animationSpec = dpAnimationSpec,
        label = "island_corner"
    )

    val alphaValue by animateFloatAsState(
        targetValue = if (displayMode == IslandDisplayMode.HIDDEN) 0f else (config.opacityPercent / 100f),
        animationSpec = tween(durationMillis = durationMs),
        label = "island_alpha"
    )

    if (displayMode == IslandDisplayMode.HIDDEN && !isInteractivePreview) {
        return
    }

    Box(
        modifier = modifier
            .offset(x = config.offsetXDp.dp, y = config.offsetYDp.dp)
            .alpha(alphaValue)
            .pointerInput(config.swipeToHideEnabled) {
                if (!config.swipeToHideEnabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (kotlin.math.abs(dragDistanceX) > 90f) {
                            if (config.hapticFeedbackEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            if (displayMode == IslandDisplayMode.EXPANDED) {
                                IslandStateCoordinator.collapseToCompact()
                            } else {
                                IslandStateCoordinator.hideManually()
                            }
                        }
                        dragDistanceX = 0f
                    },
                    onDragCancel = { dragDistanceX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragDistanceX += dragAmount
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        if (displayMode == IslandDisplayMode.SPLIT_PILL && secondaryPayload != null) {
            // Mode Split Pill: Kapsul Utama di Kiri + Kapsul Sekunder Bundar di Kanan
            Row(
                modifier = Modifier
                    .width(animatedWidth)
                    .height(animatedHeight)
                    .testTag("dynamic_island_split_pill"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val mainCapsuleShape = RoundedCornerShape(animatedCorner)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(mainCapsuleShape)
                        .background(LeovanBlack)
                        .then(
                            if (config.showBorderStroke) {
                                Modifier.border(1.dp, LeovanBorder, mainCapsuleShape)
                            } else {
                                Modifier
                            }
                        )
                        .combinedClickable(
                            onClick = {
                                if (config.hapticFeedbackEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                IslandStateCoordinator.expandSpecificSlot(isSecondarySlot = false)
                            },
                            onLongClick = {
                                if (config.hapticFeedbackEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                IslandStateCoordinator.expandSpecificSlot(isSecondarySlot = false)
                            }
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompactPillContent(payload = primaryPayload, config = config)
                }

                val secondaryShape = CircleShape
                Box(
                    modifier = Modifier
                        .size(animatedHeight)
                        .clip(secondaryShape)
                        .background(LeovanBlack)
                        .then(
                            if (config.showBorderStroke) {
                                Modifier.border(1.dp, LeovanBorder, secondaryShape)
                            } else {
                                Modifier
                            }
                        )
                        .combinedClickable(
                            onClick = {
                                if (config.hapticFeedbackEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                IslandStateCoordinator.expandSpecificSlot(isSecondarySlot = true)
                            },
                            onLongClick = {
                                if (config.hapticFeedbackEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                IslandStateCoordinator.expandSpecificSlot(isSecondarySlot = true)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SecondaryMiniPillIcon(payload = secondaryPayload)
                }
            }
        } else {
            val pillShape = RoundedCornerShape(animatedCorner)
            Box(
                modifier = Modifier
                    .width(animatedWidth)
                    .height(animatedHeight)
                    .clip(pillShape)
                    .background(LeovanBlack)
                    .then(
                        if (config.showBorderStroke) {
                            Modifier.border(1.dp, LeovanBorder, pillShape)
                        } else {
                            Modifier
                        }
                    )
                    .combinedClickable(
                        onClick = {
                            if (config.hapticFeedbackEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            if (displayMode != IslandDisplayMode.EXPANDED) {
                                IslandStateCoordinator.toggleExpandCollapse()
                            } else if (primaryPayload is IslandPayload.NotificationItem && primaryPayload.contentIntent != null) {
                                runCatching { primaryPayload.contentIntent.send() }
                                IslandStateCoordinator.collapseToCompact()
                            }
                        },
                        onLongClick = {
                            if (config.hapticFeedbackEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            IslandStateCoordinator.toggleExpandCollapse()
                        }
                    )
                    .testTag("dynamic_island_main_pill")
            ) {
                AnimatedContent(
                    targetState = Pair(displayMode, primaryPayload),
                    transitionSpec = {
                        val enter = when (config.animationStyle) {
                            AnimationStyle.SLIDE -> slideInVertically { -it / 2 } + fadeIn(tween(durationMs))
                            AnimationStyle.SPRING, AnimationStyle.MORPH -> scaleIn(initialScale = 0.92f, animationSpec = tween(durationMs)) + fadeIn(tween(durationMs))
                            AnimationStyle.FADE -> fadeIn(tween(durationMs))
                        }
                        val exit = when (config.animationStyle) {
                            AnimationStyle.SLIDE -> slideOutVertically { -it / 2 } + fadeOut(tween(durationMs / 2))
                            AnimationStyle.SPRING, AnimationStyle.MORPH -> scaleOut(targetScale = 0.92f, animationSpec = tween(durationMs / 2)) + fadeOut(tween(durationMs / 2))
                            AnimationStyle.FADE -> fadeOut(tween(durationMs / 2))
                        }
                        (enter togetherWith exit).using(SizeTransform(clip = false))
                    },
                    label = "island_content_transition"
                ) { (mode, payload) ->
                    if (mode == IslandDisplayMode.EXPANDED) {
                        ExpandedIslandContent(
                            payload = payload,
                            secondaryPayload = secondaryPayload,
                            config = config,
                            onSendQuickReply = { notif, text ->
                                IslandStateCoordinator.sendQuickReply(context, notif, text)
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CompactPillContent(payload = payload, config = config)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactPillContent(
    payload: IslandPayload,
    config: IslandConfiguration
) {
    when (payload) {
        is IslandPayload.Idle -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A))
                        .border(1.dp, Color(0xFF2C2C2E), CircleShape)
                )
                Text(
                    text = "LEOVAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = LeovanTextSecondary,
                    letterSpacing = 1.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(StatusGreen)
                )
            }
        }

        is IslandPayload.MusicPlayback -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (config.showAlbumCover) {
                        AlbumThumbnailMini(payload)
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Musik",
                            tint = LeovanTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = payload.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = LeovanTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                if (config.showMusicVisualizer) {
                    AudioWaveformBars(isPlaying = payload.isPlaying)
                }
            }
        }

        is IslandPayload.NotificationItem -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(LeovanCardSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = payload.appName,
                            tint = LeovanTextPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "${payload.senderName}: ${payload.messageText}",
                        style = MaterialTheme.typography.labelMedium,
                        color = LeovanTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (payload.groupCount > 1) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(LeovanCardSecondary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+${payload.groupCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LeovanTextSecondary
                        )
                    }
                }
            }
        }

        is IslandPayload.TimerActivity -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = StatusOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = payload.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = LeovanTextSecondary,
                        maxLines = 1
                    )
                }
                Text(
                    text = formatSeconds(payload.remainingSeconds),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = StatusOrange
                )
            }
        }

        is IslandPayload.StopwatchActivity -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stopwatch",
                    style = MaterialTheme.typography.labelSmall,
                    color = LeovanTextSecondary
                )
                Text(
                    text = formatStopwatchMillis(payload.elapsedMillis),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = StatusGreen
                )
            }
        }

        is IslandPayload.BatteryStatus -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (payload.isFull) "Baterai Penuh" else if (payload.isCharging) "Mengisi Daya" else "Baterai",
                    style = MaterialTheme.typography.labelMedium,
                    color = LeovanTextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${payload.percentage}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusGreen
                    )
                    Icon(
                        imageVector = if (payload.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Status Baterai",
                        tint = StatusGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        is IslandPayload.VolumeChange -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (payload.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Volume",
                    tint = LeovanTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                LinearProgressIndicator(
                    progress = { payload.percent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(4.dp)
                        .clip(CircleShape),
                    color = LeovanTextPrimary,
                    trackColor = LeovanBorder
                )
                Text(
                    text = "${payload.percent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = LeovanTextSecondary
                )
            }
        }

        is IslandPayload.ConnectivityStatus -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (payload.type) {
                    ConnectivityType.WIFI -> Icons.Default.Wifi
                    ConnectivityType.BLUETOOTH -> Icons.Default.Bluetooth
                    ConnectivityType.HOTSPOT -> Icons.Default.WifiTethering
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = payload.type.label,
                        tint = if (payload.isActive) StatusCyan else LeovanTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = payload.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = LeovanTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        is IslandPayload.ClipboardCopied -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Papan Klip",
                        tint = StatusGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Disalin ke Papan Klip",
                        style = MaterialTheme.typography.labelMedium,
                        color = LeovanTextPrimary,
                        maxLines = 1
                    )
                }
            }
        }

        is IslandPayload.ScreenRecordingStatus -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Rekam Layar",
                        tint = StatusRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Merekam Layar",
                        style = MaterialTheme.typography.labelMedium,
                        color = LeovanTextPrimary
                    )
                }
                Text(
                    text = formatSeconds(payload.elapsedSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusRed
                )
            }
        }
    }
}

@Composable
private fun SecondaryMiniPillIcon(payload: IslandPayload) {
    when (payload) {
        is IslandPayload.TimerActivity -> {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Timer Sekunder",
                tint = StatusOrange,
                modifier = Modifier.size(16.dp)
            )
        }
        is IslandPayload.MusicPlayback -> {
            AudioWaveformBars(isPlaying = payload.isPlaying, barCount = 3)
        }
        is IslandPayload.StopwatchActivity -> {
            Text(
                text = "SW",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = StatusGreen
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Aktivitas Sekunder",
                tint = LeovanTextPrimary,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun ExpandedIslandContent(
    payload: IslandPayload,
    secondaryPayload: IslandPayload?,
    config: IslandConfiguration,
    onSendQuickReply: (IslandPayload.NotificationItem, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        when (payload) {
            is IslandPayload.MusicPlayback -> {
                ExpandedMusicView(payload = payload, config = config, secondaryPayload = secondaryPayload)
            }
            is IslandPayload.NotificationItem -> {
                ExpandedNotificationView(
                    payload = payload,
                    config = config,
                    onSendQuickReply = onSendQuickReply
                )
            }
            is IslandPayload.TimerActivity -> {
                ExpandedTimerView(payload = payload)
            }
            is IslandPayload.StopwatchActivity -> {
                ExpandedStopwatchView(payload = payload)
            }
            is IslandPayload.BatteryStatus -> {
                ExpandedBatteryView(payload = payload)
            }
            is IslandPayload.VolumeChange -> {
                ExpandedVolumeView(payload = payload)
            }
            is IslandPayload.ConnectivityStatus -> {
                ExpandedConnectivityView(payload = payload)
            }
            is IslandPayload.ClipboardCopied -> {
                ExpandedClipboardView(payload = payload)
            }
            is IslandPayload.ScreenRecordingStatus -> {
                ExpandedScreenRecordView(payload = payload)
            }
            is IslandPayload.Idle -> {
                ExpandedIdleStatusView(secondaryPayload = secondaryPayload)
            }
        }
    }
}

@Composable
private fun ExpandedMusicView(
    payload: IslandPayload.MusicPlayback,
    config: IslandConfiguration,
    secondaryPayload: IslandPayload?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (config.showAlbumCover) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LeovanCardSecondary)
                    ) {
                        if (payload.albumArt != null) {
                            Image(
                                bitmap = payload.albumArt.asImageBitmap(),
                                contentDescription = payload.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_album_cover_1794705107042),
                                contentDescription = payload.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = payload.appName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = LeovanTextSecondary
                        )
                        if (secondaryPayload != null) {
                            Text(
                                text = "• Ketuk untuk beralih",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusOrange,
                                modifier = Modifier.clickable {
                                    IslandStateCoordinator.expandSpecificSlot(isSecondarySlot = true)
                                }
                            )
                        }
                    }
                    Text(
                        text = payload.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = LeovanTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = payload.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = LeovanTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (config.showMusicVisualizer) {
                AudioWaveformBars(isPlaying = payload.isPlaying, barCount = 5)
            }
        }

        // Bilah Progres Lagu & Durasi
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = payload.progressFraction,
                onValueChange = { IslandStateCoordinator.seekMusicToFraction(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp),
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
                    text = formatDurationMs(payload.positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = LeovanTextSecondary
                )
                Text(
                    text = "-${formatDurationMs((payload.durationMs - payload.positionMs).coerceAtLeast(0L))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LeovanTextSecondary
                )
            }
        }

        // Kontrol Pemutar Musik
        if (config.showMusicControls) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { IslandStateCoordinator.skipToPreviousTrack() },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("music_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Lagu Sebelumnya",
                        tint = LeovanTextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Surface(
                    onClick = { IslandStateCoordinator.toggleMusicPlayPause() },
                    shape = CircleShape,
                    color = LeovanTextPrimary,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("music_play_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (payload.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (payload.isPlaying) "Jeda" else "Putar",
                            tint = LeovanBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { IslandStateCoordinator.skipToNextTrack() },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("music_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Lagu Berikutnya",
                        tint = LeovanTextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = { IslandStateCoordinator.collapseToCompact() },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("island_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Ringkas Island",
                        tint = LeovanTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedNotificationView(
    payload: IslandPayload.NotificationItem,
    config: IslandConfiguration,
    onSendQuickReply: (IslandPayload.NotificationItem, String) -> Unit
) {
    var showReplyField by remember(payload.id) { mutableStateOf(false) }
    var replyText by remember(payload.id) { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(LeovanCardSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = payload.appName,
                        tint = LeovanTextPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = payload.appName,
                    style = MaterialTheme.typography.labelMedium,
                    color = LeovanTextSecondary
                )
                if (payload.groupCount > 1) {
                    Text(
                        text = "• ${payload.groupCount} pesan",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusGreen
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = payload.timeFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = LeovanTextSecondary
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup Notifikasi",
                    tint = LeovanTextSecondary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { IslandStateCoordinator.dismissCurrentNotification() }
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = payload.senderName,
                style = MaterialTheme.typography.titleMedium,
                color = LeovanTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = payload.messageText,
                style = MaterialTheme.typography.bodySmall,
                color = LeovanTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        AnimatedVisibility(visible = showReplyField) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LeovanCardSecondary)
                    .border(1.dp, LeovanBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = LeovanTextPrimary),
                    cursorBrush = SolidColor(LeovanTextPrimary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (replyText.isEmpty()) {
                            Text(
                                text = "Ketik balasan cepat...",
                                style = MaterialTheme.typography.bodySmall,
                                color = LeovanTextSecondary
                            )
                        }
                        innerTextField()
                    }
                )
                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onSendQuickReply(payload, replyText)
                            showReplyField = false
                            replyText = ""
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Kirim Balasan",
                        tint = StatusGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (!showReplyField) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (payload.canQuickReply && config.quickReplyEnabled) {
                    Surface(
                        onClick = {
                            showReplyField = true
                            IslandStateCoordinator.setQuickReplyInputActive(true)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = LeovanCardSecondary,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("notif_quick_reply_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Balas Cepat",
                                style = MaterialTheme.typography.labelMedium,
                                color = LeovanTextPrimary
                            )
                        }
                    }
                }
                Surface(
                    onClick = {
                        payload.contentIntent?.let { runCatching { it.send() } }
                        IslandStateCoordinator.collapseToCompact()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = LeovanCardSecondary,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Buka Aplikasi",
                            style = MaterialTheme.typography.labelMedium,
                            color = LeovanTextPrimary
                        )
                    }
                }
                Surface(
                    onClick = { IslandStateCoordinator.dismissCurrentNotification() },
                    shape = RoundedCornerShape(12.dp),
                    color = LeovanCardSecondary,
                    modifier = Modifier.height(34.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selesai",
                            style = MaterialTheme.typography.labelMedium,
                            color = LeovanTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedTimerView(payload: IslandPayload.TimerActivity) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = StatusOrange,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = payload.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = LeovanTextSecondary
                )
            }
            Text(
                text = formatSeconds(payload.remainingSeconds),
                style = MaterialTheme.typography.displayLarge,
                color = StatusOrange
            )
            Text(
                text = if (payload.remainingSeconds == 0) "Selesai!" else if (payload.isRunning) "Sedang berjalan" else "Dijeda",
                style = MaterialTheme.typography.bodySmall,
                color = LeovanTextSecondary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                progress = { payload.progressFraction },
                modifier = Modifier.size(44.dp),
                color = StatusOrange,
                trackColor = LeovanBorder,
                strokeWidth = 4.dp
            )
            Surface(
                onClick = { IslandStateCoordinator.toggleTimerPauseResume() },
                shape = CircleShape,
                color = LeovanCardSecondary,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (payload.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (payload.isRunning) "Jeda Timer" else "Lanjutkan Timer",
                        tint = LeovanTextPrimary
                    )
                }
            }
            Surface(
                onClick = { IslandStateCoordinator.stopAndClearTimer() },
                shape = CircleShape,
                color = LeovanCardSecondary,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hentikan Timer",
                        tint = StatusRed
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedStopwatchView(payload: IslandPayload.StopwatchActivity) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "STOPWATCH • PUTARAN ${payload.lapCount}",
                style = MaterialTheme.typography.labelSmall,
                color = LeovanTextSecondary
            )
            Text(
                text = formatStopwatchMillis(payload.elapsedMillis),
                style = MaterialTheme.typography.displayLarge,
                color = StatusGreen
            )
            Text(
                text = if (payload.isRunning) "Menghitung waktu aktif" else "Stopwatch dijeda",
                style = MaterialTheme.typography.bodySmall,
                color = LeovanTextSecondary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                onClick = {
                    if (payload.isRunning) IslandStateCoordinator.pauseStopwatch()
                    else IslandStateCoordinator.startOrResumeStopwatch()
                },
                shape = CircleShape,
                color = LeovanCardSecondary,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (payload.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Jeda/Putar Stopwatch",
                        tint = StatusGreen
                    )
                }
            }
            Surface(
                onClick = { IslandStateCoordinator.lapStopwatch() },
                shape = CircleShape,
                color = LeovanCardSecondary,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Tambah Putaran",
                        tint = LeovanTextPrimary
                    )
                }
            }
            Surface(
                onClick = { IslandStateCoordinator.resetAndClearStopwatch() },
                shape = CircleShape,
                color = LeovanCardSecondary,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Reset Stopwatch",
                        tint = StatusRed
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedBatteryView(payload: IslandPayload.BatteryStatus) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (payload.isFull) "PENGISIAN DAYA PENUH" else if (payload.isCharging) "PENGISIAN DAYA CEPAT" else "STATUS BATERAI",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusGreen
                )
                Text(
                    text = "${payload.percentage}% • ${payload.powerSourceLabel}",
                    style = MaterialTheme.typography.titleLarge,
                    color = LeovanTextPrimary
                )
            }
            Icon(
                imageVector = if (payload.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                contentDescription = "Baterai",
                tint = StatusGreen,
                modifier = Modifier.size(32.dp)
            )
        }

        LinearProgressIndicator(
            progress = { payload.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = StatusGreen,
            trackColor = LeovanBorder
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Suhu Perangkat: ${payload.temperatureCelsius}°C",
                style = MaterialTheme.typography.bodySmall,
                color = LeovanTextSecondary
            )
            Text(
                text = "Tutup",
                style = MaterialTheme.typography.labelMedium,
                color = LeovanTextPrimary,
                modifier = Modifier.clickable { IslandStateCoordinator.collapseToCompact() }
            )
        }
    }
}

@Composable
private fun ExpandedVolumeView(payload: IslandPayload.VolumeChange) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (payload.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Volume",
                    tint = LeovanTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = payload.streamLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = LeovanTextPrimary
                    )
                    Text(
                        text = if (payload.isMuted) "Mode Senyap Aktif" else "Level ${payload.currentVolume} dari ${payload.maxVolume}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LeovanTextSecondary
                    )
                }
            }
            Text(
                text = "${payload.percent}%",
                style = MaterialTheme.typography.headlineMedium,
                color = LeovanTextPrimary
            )
        }

        LinearProgressIndicator(
            progress = { payload.percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = LeovanTextPrimary,
            trackColor = LeovanBorder
        )
    }
}

@Composable
private fun ExpandedConnectivityView(payload: IslandPayload.ConnectivityStatus) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when (payload.type) {
            ConnectivityType.WIFI -> Icons.Default.Wifi
            ConnectivityType.BLUETOOTH -> Icons.Default.Bluetooth
            ConnectivityType.HOTSPOT -> Icons.Default.WifiTethering
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(LeovanCardSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = payload.type.label,
                    tint = if (payload.isActive) StatusCyan else LeovanTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = payload.type.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (payload.isActive) StatusCyan else LeovanTextSecondary
                )
                Text(
                    text = payload.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = LeovanTextPrimary
                )
                Text(
                    text = payload.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LeovanTextSecondary
                )
            }
        }
        IconButton(onClick = { IslandStateCoordinator.collapseToCompact() }) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Tutup",
                tint = LeovanTextSecondary
            )
        }
    }
}

@Composable
private fun ExpandedClipboardView(payload: IslandPayload.ClipboardCopied) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Papan Klip",
                    tint = StatusGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Papan Klip Diperbarui (${payload.charCount} karakter)",
                    style = MaterialTheme.typography.labelMedium,
                    color = StatusGreen
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Tersalin",
                tint = StatusGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(LeovanCardSecondary)
                .padding(10.dp)
        ) {
            Text(
                text = payload.previewText,
                style = MaterialTheme.typography.bodySmall,
                color = LeovanTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpandedScreenRecordView(payload: IslandPayload.ScreenRecordingStatus) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = "Rekam Layar",
                    tint = StatusRed,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "PEREKAMAN LAYAR AKTIF",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusRed
                )
            }
            Text(
                text = formatSeconds(payload.elapsedSeconds),
                style = MaterialTheme.typography.headlineLarge,
                color = LeovanTextPrimary
            )
            Text(
                text = "Audio Internal & Layar 60 FPS",
                style = MaterialTheme.typography.bodySmall,
                color = LeovanTextSecondary
            )
        }
        Surface(
            onClick = { IslandStateCoordinator.triggerSimulatedScreenRecording(isRecording = false) },
            shape = RoundedCornerShape(14.dp),
            color = StatusRed.copy(alpha = 0.2f),
            modifier = Modifier.height(40.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Hentikan",
                    style = MaterialTheme.typography.labelLarge,
                    color = StatusRed
                )
            }
        }
    }
}

@Composable
private fun ExpandedIdleStatusView(secondaryPayload: IslandPayload?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LEOVANISLAND AKTIF",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusGreen
                )
                Text(
                    text = "Siap Menampilkan Aktivitas",
                    style = MaterialTheme.typography.titleMedium,
                    color = LeovanTextPrimary
                )
            }
            IconButton(onClick = { IslandStateCoordinator.collapseToCompact() }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Tutup",
                    tint = LeovanTextSecondary
                )
            }
        }
        Text(
            text = if (secondaryPayload != null) {
                "1 aktivitas latar belakang berjalan. Ketuk tombol di bawah untuk memutar musik atau timer."
            } else {
                "Notifikasi pintar, kontrol musik universal, dan pemantau sistem berjalan dalam mode siaga hemat baterai."
            },
            style = MaterialTheme.typography.bodySmall,
            color = LeovanTextSecondary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = { IslandStateCoordinator.triggerSimulatedNotification() },
                shape = RoundedCornerShape(12.dp),
                color = LeovanCardSecondary,
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Uji Notifikasi", style = MaterialTheme.typography.labelMedium, color = LeovanTextPrimary)
                }
            }
            Surface(
                onClick = { IslandStateCoordinator.triggerSimulatedSplitPillMusicAndTimer() },
                shape = RoundedCornerShape(12.dp),
                color = LeovanCardSecondary,
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Split Pill", style = MaterialTheme.typography.labelMedium, color = LeovanTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun AlbumThumbnailMini(payload: IslandPayload.MusicPlayback) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(LeovanCardSecondary)
    ) {
        if (payload.albumArt != null) {
            Image(
                bitmap = payload.albumArt.asImageBitmap(),
                contentDescription = payload.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.img_album_cover_1794705107042),
                contentDescription = payload.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun AudioWaveformBars(
    isPlaying: Boolean,
    barCount: Int = 4,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_transition")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val animatedScale by infiniteTransition.animateFloat(
                initialValue = 0.28f,
                targetValue = if (isPlaying) 1.0f else 0.32f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 360 + (index * 110),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((14 * animatedScale).dp)
                    .clip(CircleShape)
                    .background(LeovanTextPrimary)
            )
        }
    }
}

private fun formatDurationMs(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val mins = totalSeconds / 60L
    val secs = totalSeconds % 60L
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}

private fun formatSeconds(seconds: Int): String {
    val clamped = seconds.coerceAtLeast(0)
    val mins = clamped / 60
    val secs = clamped % 60
    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}

private fun formatStopwatchMillis(millis: Long): String {
    val totalTenths = (millis / 100L).coerceAtLeast(0L)
    val tenths = totalTenths % 10L
    val totalSecs = totalTenths / 10L
    val secs = totalSecs % 60L
    val mins = totalSecs / 60L
    return String.format(Locale.getDefault(), "%02d:%02d.%d", mins, secs, tenths)
}
