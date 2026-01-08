package com.practicum.playlistmaker.domain.theme

interface ThemeRepository {
    fun switchTheme(darkMode: Boolean)
    fun isDarkMode(): Boolean
    fun applyThemeAccordingToUserOrSystem()
}
