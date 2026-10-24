package com.kratos.fflauncher.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fflauncher", Context.MODE_PRIVATE)

    var autoLaunch: Boolean
        get() = prefs.getBoolean(KEY_AUTO_LAUNCH, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_LAUNCH, value).apply()

    var showLines: Boolean
        get() = prefs.getBoolean(KEY_SHOW_LINES, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_LINES, value).apply()

    var showBoxes: Boolean
        get() = prefs.getBoolean(KEY_SHOW_BOXES, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_BOXES, value).apply()

    var showObstacles: Boolean
        get() = prefs.getBoolean(KEY_SHOW_OBSTACLES, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_OBSTACLES, value).apply()

    companion object {
        private const val KEY_AUTO_LAUNCH = "autoLaunch"
        private const val KEY_SHOW_LINES = "showLines"
        private const val KEY_SHOW_BOXES = "showBoxes"
        private const val KEY_SHOW_OBSTACLES = "showObstacles"
    }
}
