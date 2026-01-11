package com.practicum.playlistmaker.domain.theme

class SwitchThemeUseCase(private val repository: ThemeRepository) {
    fun execute(darkMode: Boolean) {
        repository.switchTheme(darkMode)
    }
}