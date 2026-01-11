package com.practicum.playlistmaker.domain.history

class ClearSearchHistoryUseCase(
    private val repository: SearchHistoryRepository
) {
    fun execute() {
        repository.clearHistory()
    }
}
