package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.domain.history.SearchHistoryRepository

class GetSearchHistoryUseCase(
    private val repository: SearchHistoryRepository
) {
    fun execute(): List<Track> = repository.getHistory()
}
