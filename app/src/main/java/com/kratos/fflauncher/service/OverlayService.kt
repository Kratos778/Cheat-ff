package com.kratos.fflauncher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.kratos.fflauncher.data.AppPreferences
import kotlin.math.abs

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: DemoOverlayView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        val channelId = "fflauncher_hud" 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "FFLauncher HUD Lab",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("FFLauncher HUD Lab")
            .setContentText("Mostrando overlay visual de demonstração.")
            .setOngoing(true)
            .build()

        startForeground(4201, notification)

        val prefs = AppPreferences(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = DemoOverlayView(
            context = this,
            lines = prefs.showLines,
            boxes = prefs.showBoxes,
            obstacles = prefs.showObstacles
        )

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.48f).toInt().coerceAtMost(600),
            type,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL

        try {
            windowManager.addView(overlayView, params)
        } catch (_: Exception) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        try {
            overlayView?.let { windowManager.removeView(it) }
        } catch (_: Exception) {
        }
        overlayView = null
        isRunning = false
        super.onDestroy()
    }

    companion object {
        @Volatile
        var isRunning: Boolean = false
    }
}

private class DemoOverlayView(
    context: Context,
    private val lines: Boolean,
    private val boxes: Boolean,
    private val obstacles: Boolean
) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var downX = 0f
    private var downY = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        paint.style = Paint.Style.FILL
        paint.color = 0xB8000D18.toInt()
        canvas.drawRoundRect(12f, 12f, width - 12f, height - 12f, 20f, 20f, paint)

        paint.color = 0xFF58E6C5.toInt()
        paint.textSize = 26f
        paint.style = Paint.Style.FILL
        canvas.drawText("FFL • HUD LAB / SIMULAÇÃO", 30f, 50f, paint)

        paint.textSize = 18f
        paint.color = 0xFFEAF1FF.toInt()
        canvas.drawText("Toque na faixa superior para fechar", 30f, 80f, paint)

        val targets = listOf(
            floatArrayOf(width * 0.24f, height * 0.46f),
            floatArrayOf(width * 0.73f, height * 0.48f),
            floatArrayOf(width * 0.5f, height * 0.34f)
        )

        if (lines) {
            paint.color = 0xCC58E6C5.toInt()
            paint.strokeWidth = 2.5f
            paint.style = Paint.Style.STROKE
            targets.forEach { canvas.drawLine(width / 2f, height - 12f, it[0], it[1], paint) }
        }

        if (obstacles) {
            paint.color = 0x995A6678.toInt()
            paint.style = Paint.Style.FILL
            canvas.drawRect(width * 0.43f, height * 0.48f, width * 0.59f, height * 0.8f, paint)
            paint.color = 0xFFFFB86B.toInt()
            paint.strokeWidth = 3f
            paint.style = Paint.Style.STROKE
            canvas.drawLine(width * 0.43f, height * 0.4f, width * 0.59f, height * 0.4f, paint)
        }

        targets.forEachIndexed { index, point ->
            val blocked = obstacles && index == 1
            paint.color = if (blocked) 0xFFFFB86B.toInt() else 0xFF58E6C5.toInt()
            paint.strokeWidth = 3f
            paint.style = Paint.Style.STROKE

            if (boxes) {
                canvas.drawRect(
                    point[0] - 24f,
                    point[1] - 44f,
                    point[0] + 24f,
                    point[1] + 40f,
                    paint
                )
            }

            paint.style = Paint.Style.FILL
            canvas.drawCircle(point[0], point[1] - 49f, 6f, paint)

            paint.textSize = 17f
            canvas.drawText(
                if (blocked) "TESTE • BLOQUEADO" else "ALVO FICTÍCIO",
                point[0] - 52f,
                point[1] + 60f,
                paint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (abs(event.x - downX) < 12 && abs(event.y - downY) < 12 && event.y < 85f) {
                    context.stopService(Intent(context, OverlayService::class.java))
                    return true
                }
            }
        }
        return true
    }
}
