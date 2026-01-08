package com.practicum.playlistmaker.domain.history

class ClearSearchHistoryUseCase(
    private val repository: SearchHistory
) {
    fun execute() {
        repository.clearHistory()
    }
}
