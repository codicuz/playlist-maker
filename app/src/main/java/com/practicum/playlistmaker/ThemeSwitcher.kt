package com.practicum.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeSwitcher : Application() {

    companion object {
        private const val PREFS_NAME = "settings"
        private const val DARK_MODE_KEY = "dark_mode"
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean(DARK_MODE_KEY, false)
        applyTheme(isDarkMode)
    }

    fun switchTheme(darkMode: Boolean) {
        prefs.edit().putBoolean(DARK_MODE_KEY, darkMode).apply()
        applyTheme(darkMode)
    }

    private fun applyTheme(darkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(DARK_MODE_KEY, false)
    }
}