package com.practicum.playlistmaker.domain.theme

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.data.storage.SharedPrefs

class ThemeSwitcher : Application() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(SharedPrefs.Companion.PREFS_SETTINGS, MODE_PRIVATE)
        applyThemeAccordingToUserOrSystem()
    }

    fun switchTheme(darkMode: Boolean) {
        prefs.edit().putBoolean(SharedPrefs.Companion.DARK_MODE_KEY, darkMode).apply()
        applyTheme(darkMode)
    }

    private fun applyTheme(darkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun isDarkMode(): Boolean {
        return if (prefs.contains(SharedPrefs.Companion.DARK_MODE_KEY)) {
            prefs.getBoolean(SharedPrefs.Companion.DARK_MODE_KEY, false)
        } else {
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
        }
    }

    private fun applyThemeAccordingToUserOrSystem() {
        if (prefs.contains(SharedPrefs.Companion.DARK_MODE_KEY)) {
            val isDarkMode = prefs.getBoolean(SharedPrefs.Companion.DARK_MODE_KEY, false)
            applyTheme(isDarkMode)
        } else {
            val isSystemDark =
                (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES
            applyTheme(isSystemDark)
        }
    }
}