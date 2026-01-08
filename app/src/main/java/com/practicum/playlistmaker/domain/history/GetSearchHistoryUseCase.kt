package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.model.Track

class GetSearchHistoryUseCase(
    private val repository: SearchHistory
) {
    fun execute(): List<Track> = repository.getHistory()
}
