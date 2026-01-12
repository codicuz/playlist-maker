package com.practicum.playlistmaker.domain.theme

import com.practicum.playlistmaker.data.theme.ThemeRepositoryImpl

class GetThemeUseCase(private val repository: ThemeRepository) {
    fun execute(): Boolean {
        return repository.isDarkMode()
    }
}