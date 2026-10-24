package com.kratos.fflauncher

import android.app.Notification
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
import kotlin.math.min

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: DemoOverlayView? = null
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        isRunning = true
        val channelId = "fflauncher_hud"
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "FFLauncher HUD Lab", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("FFLauncher HUD Lab")
            .setContentText("A mostrar indicadores de demonstração fictícios")
            .setOngoing(true).build()
        startForeground(4201, notification)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val prefs = getSharedPreferences("fflauncher", MODE_PRIVATE)
        overlayView = DemoOverlayView(this, prefs.getBoolean("showLines", true), prefs.getBoolean("showBoxes", true), prefs.getBoolean("showObstacles", true))
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, min((resources.displayMetrics.heightPixels * .48f).toInt(), 600), type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        try { windowManager.addView(overlayView, params) } catch (_: Exception) { stopSelf() }
    }
    override fun onDestroy() {
        try { overlayView?.let { windowManager.removeView(it) } } catch (_: Exception) {}
        overlayView = null; isRunning = false; super.onDestroy()
    }
    companion object { @Volatile var isRunning: Boolean = false }
}

private class DemoOverlayView(context: Context, private val lines: Boolean, private val boxes: Boolean, private val obstacles: Boolean) : View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var downX = 0f
    private var downY = 0f
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        p.style = Paint.Style.FILL; p.color = 0xB8000D18.toInt(); canvas.drawRoundRect(12f, 12f, w - 12f, h - 12f, 20f, 20f, p)
        p.color = 0xFF58E6C5.toInt(); p.textSize = 27f; p.style = Paint.Style.FILL
        canvas.drawText("FFL • HUD LAB / SIMULAÇÃO", 30f, 48f, p)
        p.textSize = 20f; p.color = 0xFFEAF1FF.toInt(); canvas.drawText("Toque na faixa superior para fechar", 30f, 76f, p)
        val pts = listOf(floatArrayOf(w * .24f, h * .46f), floatArrayOf(w * .73f, h * .48f), floatArrayOf(w * .5f, h * .34f))
        if (lines) { p.color = 0xCC58E6C5.toInt(); p.strokeWidth = 2.5f; pts.forEach { canvas.drawLine(w / 2f, h - 20f, it[0], it[1], p) } }
        if (obstacles) { p.color = 0x995A6678.toInt(); canvas.drawRect(w * .43f, h * .48f, w * .59f, h * .8f, p); p.color = 0xFFFFB86B.toInt(); p.strokeWidth = 3f; canvas.drawLine(w * .43f, h * .48f, w * .59f, h * .48f, p) }
        pts.forEachIndexed { i, pt ->
            val blocked = obstacles && i == 1; p.color = if (blocked) 0xFFFFB86B.toInt() else 0xFF58E6C5.toInt(); p.strokeWidth = 3f; p.style = Paint.Style.STROKE
            if (boxes) canvas.drawRect(pt[0] - 24f, pt[1] - 44f, pt[0] + 24f, pt[1] + 40f, p)
            p.style = Paint.Style.FILL; canvas.drawCircle(pt[0], pt[1] - 49f, 6f, p)
            p.textSize = 17f; canvas.drawText(if (blocked) "TESTE • BLOQUEADO" else "ALVO FICTÍCIO", pt[0] - 54f, pt[1] + 60f, p)
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { downX = event.x; downY = event.y; return true }
            MotionEvent.ACTION_UP -> { if (kotlin.math.abs(event.x - downX) < 12 && kotlin.math.abs(event.y - downY) < 12 && event.y < 85f) { context.stopService(Intent(context, OverlayService::class.java)) }; return true }
        }
        return true
    }
}
