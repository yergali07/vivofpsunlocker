package com.dezory.vivofpsunlocker

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log

class FpsService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "FpsServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, "FPS Unlocker Service", android.app.NotificationManager.IMPORTANCE_LOW)
            getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(channel)
            val notification = Notification.Builder(this, channelId)
                .setContentTitle("FPS Unlocker")
                .setContentText("Maintaining 144 FPS")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .build()
            startForeground(1, notification)
        } else {
            val notification = Notification.Builder(this)
                .setContentTitle("FPS Unlocker")
                .setContentText("Maintaining 144 FPS")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .build()
            startForeground(1, notification)
        }

        val prefs = getSharedPreferences("FpsPrefs", MODE_PRIVATE)
        val handler = Handler(Looper.getMainLooper())
        val refreshRunnable = object : Runnable {
            override fun run() {
                if (prefs.getBoolean("fps_enabled", false)) {
                    try {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(this@FpsService)) {
                            Settings.System.putString(contentResolver, "gamecube_frame_interpolation_for_sr", "1:1::72:144")
                            Log.d("FPSUnlocker", "Refreshed setting to '1:1::72:144'")
                        } else {
                            Log.w("FPSUnlocker", "Missing WRITE_SETTINGS permission; skipping refresh")
                        }
                    } catch (e: SecurityException) {
                        Log.e("FPSUnlocker", "SecurityException: ${e.message}", e)
                    } catch (e: Exception) {
                        Log.e("FPSUnlocker", "err: ${e.message}", e)
                    }
                }
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(refreshRunnable)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
    }
}
