package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.track.Track
class AddTrackToHistoryUseCase(
    private val repository: SearchHistoryRepository
) {
    fun execute(track: Track) {
        repository.addTrack(track)
    }
}
