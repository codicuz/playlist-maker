package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.model.Track

class AddTrackToHistoryUseCase(
    private val repository: SearchHistory
) {
    fun execute(track: Track) {
        repository.addTrack(track)
    }
}
