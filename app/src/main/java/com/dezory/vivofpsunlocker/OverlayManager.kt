package com.dezory.vivofpsunlocker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService
import android.widget.Toast

object OverlayManager {
    const val ACTION_OVERLAY_CHANGED = "com.dezory.vivofpsunlocker.ACTION_OVERLAY_CHANGED"
    const val EXTRA_ENABLED = "enabled"
    private const val KEY_PREFS = "FpsPrefs"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE)
            .getBoolean("overlay_enabled", false)

    fun canDrawOverlays(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

    fun setEnabled(context: Context, enabled: Boolean, showToast: Boolean = true): Boolean {
        if (enabled && !canDrawOverlays(context)) return false

        if (enabled) context.startService(Intent(context, FpsOverlayService::class.java))
        else context.stopService(Intent(context, FpsOverlayService::class.java))

        context.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("overlay_enabled", enabled).apply()

        if (showToast) Toast.makeText(context, if (enabled) "Overlay on" else "Overlay off", Toast.LENGTH_SHORT).show()

        // Broadcast to update any UI, and refresh QS tile
        context.sendBroadcast(Intent(ACTION_OVERLAY_CHANGED).putExtra(EXTRA_ENABLED, enabled))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TileService.requestListeningState(
                context,
                ComponentName(context, OverlayTileService::class.java)
            )
        }
        return true
    }
}

