package com.practicum.playlistmaker.data.theme

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.practicum.playlistmaker.data.storage.SharedPrefs
import com.practicum.playlistmaker.domain.theme.ThemeRepository

class ThemeRepositoryImpl(
    private val sharedPreferences: SharedPreferences, private val app: Application
) : ThemeRepository {

    private val themeKey = SharedPrefs.DARK_MODE_KEY

    override fun isDarkMode(): Boolean {
        if (sharedPreferences.contains(themeKey)) {
            return sharedPreferences.getBoolean(themeKey, false)
        }
        val uiMode = app.resources.configuration.uiMode
        val systemDarkMode =
            (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        sharedPreferences.edit {
            putBoolean(themeKey, systemDarkMode)
        }

        return systemDarkMode
    }

    override fun switchTheme(darkMode: Boolean) {
        sharedPreferences.edit {
            putBoolean(themeKey, darkMode)
        }

        applyThemeInternal(darkMode)
    }

    override fun applyTheme() {
        applyThemeInternal(isDarkMode())
    }

    private fun applyThemeInternal(darkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
