package com.practicum.playlistmaker.data.theme

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.data.storage.SharedPrefs
import com.practicum.playlistmaker.domain.theme.ThemeRepository

class ThemeRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val app: Application
) : ThemeRepository {

    private val themeKey = SharedPrefs.DARK_MODE_KEY

    override fun switchTheme(darkMode: Boolean) {
        sharedPreferences.edit().putBoolean(themeKey, darkMode).apply()
        applyTheme(darkMode)
    }

    override fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(themeKey, false)
    }

    override fun applyThemeAccordingToUserOrSystem() {
        val darkMode = if (sharedPreferences.contains(themeKey)) {
            sharedPreferences.getBoolean(themeKey, false)
        } else {
            val uiMode = app.resources.configuration.uiMode
            (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
        applyTheme(darkMode)
    }

    private fun applyTheme(darkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun isDarkModeApplied(): Boolean {
        if (sharedPreferences.contains(themeKey)) {
            return sharedPreferences.getBoolean(themeKey, false)
        }
        val uiMode = app.resources.configuration.uiMode
        return (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
