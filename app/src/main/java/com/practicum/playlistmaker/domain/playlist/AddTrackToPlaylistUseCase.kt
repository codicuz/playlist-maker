package com.practicum.playlistmaker.domain.playlist

import com.practicum.playlistmaker.domain.track.Track

class AddTrackToPlaylistUseCase(private val repository: PlaylistTracksRepository) {
    suspend fun execute(playlistId: Long, track: Track) {
        repository.addTrack(playlistId, track)
    }
}
