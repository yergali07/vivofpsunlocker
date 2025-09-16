package com.dezory.vivofpsunlocker

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.Choreographer
import androidx.core.content.ContextCompat

class FpsOverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: TextView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private var frameCount: Int = 0
    private var lastTimeNs: Long = 0L
    private var lastUiUpdateNs: Long = 0L
    private var fps: Double = 0.0

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (lastTimeNs == 0L) {
                lastTimeNs = frameTimeNanos
                lastUiUpdateNs = frameTimeNanos
            }
            frameCount++

            val elapsedNs = frameTimeNanos - lastTimeNs
            if (elapsedNs >= 1_000_000_000L) {
                fps = frameCount * (1_000_000_000.0 / elapsedNs)
                frameCount = 0
                lastTimeNs = frameTimeNanos
            }

            // Update UI about twice a second to reduce churn
            if (frameTimeNanos - lastUiUpdateNs >= 500_000_000L) {
                lastUiUpdateNs = frameTimeNanos
                updateOverlayText()
            }

            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // Foreground notification to keep service alive
        val channelId = "FpsOverlayChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, "FPS Overlay", android.app.NotificationManager.IMPORTANCE_LOW)
            getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(channel)
            val notification = Notification.Builder(this, channelId)
                .setContentTitle("FPS Overlay")
                .setContentText("Showing FPS over apps")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .build()
            startForeground(2, notification)
        } else {
            val notification = Notification.Builder(this)
                .setContentTitle("FPS Overlay")
                .setContentText("Showing FPS over apps")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .build()
            startForeground(2, notification)
        }

        setupOverlay()
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun setupOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 12
            y = 12
        }

        overlayView = TextView(this).apply {
            textSize = 11f
            setPadding(0, 0, 0, 0)
            setTextColor(0xFFFFFFFF.toInt())
            background = ContextCompat.getDrawable(this@FpsOverlayService, R.drawable.bg_overlay_bubble)
            contentDescription = "FPS overlay"
        }

        // Allow dragging the overlay
        makeDraggable(overlayView!!)

        windowManager?.addView(overlayView, layoutParams)
        updateOverlayText()
    }

    private fun makeDraggable(view: View) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        view.setOnTouchListener { _, event ->
            val params = layoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (kotlin.math.abs(dx) > touchSlop || kotlin.math.abs(dy) > touchSlop) {
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(view, params)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun updateOverlayText() {
        val text = if (fps.isFinite()) "FPS: ${"%d".format(fps.roundToInt())}" else "FPS: —"
        overlayView?.text = text
    }

    private fun Double.roundToInt(): Int = kotlin.math.round(this).toInt()

    override fun onDestroy() {
        super.onDestroy()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        overlayView?.let { v ->
            try { windowManager?.removeView(v) } catch (_: Exception) {}
        }
        overlayView = null
        windowManager = null
    }
}
