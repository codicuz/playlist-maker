package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.history.SearchHistoryRepository

class ClearSearchHistoryUseCase(
    private val repository: SearchHistoryRepository
) {
    fun execute() {
        repository.clearHistory()
    }
}
