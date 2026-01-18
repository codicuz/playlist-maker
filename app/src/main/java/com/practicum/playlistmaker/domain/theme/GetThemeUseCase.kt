package com.practicum.playlistmaker.domain.theme

class GetThemeUseCase(private val repository: ThemeRepository) {
    fun execute(): Boolean {
        return repository.isDarkMode()
    }
}