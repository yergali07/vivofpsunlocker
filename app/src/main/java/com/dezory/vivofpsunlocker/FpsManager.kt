package com.dezory.vivofpsunlocker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast

object FpsManager {
    const val ACTION_FPS_CHANGED = "com.dezory.vivofpsunlocker.ACTION_FPS_CHANGED"
    const val EXTRA_ENABLED = "enabled"
    private const val KEY_PREFS = "FpsPrefs"

    fun isEnabled(context: Context): Boolean {
        return try {
            val v = Settings.System.getString(context.contentResolver, "gamecube_frame_interpolation_for_sr")
            v == "1:1::72:144"
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "isEnabled err: ${e.message}", e)
            false
        }
    }

    fun canWriteSettings(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(context)
    }

    fun setEnabled(context: Context, enabled: Boolean, showToast: Boolean = true): Boolean {
        return try {
            if (!canWriteSettings(context)) return false
            val value = if (enabled) "1:1::72:144" else "0:-1:0:0:0"
            Settings.System.putString(context.contentResolver, "gamecube_frame_interpolation_for_sr", value)

            context.getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean("fps_enabled", enabled).apply()

            if (enabled) context.startService(Intent(context, FpsService::class.java))
            else context.stopService(Intent(context, FpsService::class.java))

            if (showToast) Toast.makeText(context, if (enabled) "144 FPS on!" else "144 FPS off", Toast.LENGTH_SHORT).show()

            // Notify UI listeners
            context.sendBroadcast(Intent(ACTION_FPS_CHANGED).putExtra(EXTRA_ENABLED, enabled))

            // Ask system to refresh QS tile state soon
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                TileService.requestListeningState(
                    context,
                    ComponentName(context, FpsTileService::class.java)
                )
            }
            true
        } catch (e: SecurityException) {
            Log.e("FPSUnlocker", "setEnabled SecurityException: ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "setEnabled err: ${e.message}", e)
            false
        }
    }
}

