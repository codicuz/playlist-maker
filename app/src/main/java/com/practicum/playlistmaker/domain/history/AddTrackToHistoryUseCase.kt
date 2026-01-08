package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.domain.history.SearchHistoryRepository

class AddTrackToHistoryUseCase(
    private val repository: SearchHistoryRepository
) {
    fun execute(track: Track) {
        repository.addTrack(track)
    }
}
