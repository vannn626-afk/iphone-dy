package com.example.manager

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.data.PermissionStatusSnapshot
import com.example.service.LeovanNotificationListenerService

object PermissionManager {

    fun checkPermissions(context: Context): PermissionStatusSnapshot {
        val canDrawOverlays = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }

        val isNotificationListenerEnabled = isNotificationServiceEnabled(context)

        val isPostNotificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return PermissionStatusSnapshot(
            canDrawOverlays = canDrawOverlays,
            isNotificationListenerEnabled = isNotificationListenerEnabled,
            isPostNotificationsGranted = isPostNotificationsGranted
        )
    }

    fun isNotificationServiceEnabled(context: Context): Boolean {
        val cn = ComponentName(context, LeovanNotificationListenerService::class.java)
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return !flat.isNullOrEmpty() && flat.contains(cn.flattenToString())
    }

    fun buildOverlayPermissionIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            buildAppDetailsIntent(context)
        }
    }

    fun buildNotificationListenerSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun buildAppDetailsIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun openOverlayPermissionSettings(context: Context) {
        runCatching {
            context.startActivity(buildOverlayPermissionIntent(context))
        }.onFailure {
            runCatching { context.startActivity(buildAppDetailsIntent(context)) }
        }
    }

    fun openNotificationListenerSettings(context: Context) {
        runCatching {
            context.startActivity(buildNotificationListenerSettingsIntent())
        }.onFailure {
            runCatching { context.startActivity(buildAppDetailsIntent(context)) }
        }
    }
}
