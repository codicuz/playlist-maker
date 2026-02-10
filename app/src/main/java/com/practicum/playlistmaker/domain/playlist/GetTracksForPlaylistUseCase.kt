package com.practicum.playlistmaker.domain.playlist

import com.practicum.playlistmaker.domain.track.Track

class GetTracksForPlaylistUseCase(
    private val repository: PlaylistTracksRepository
) {
    suspend fun execute(playlistId: Long): List<Track> {
        return repository.getTracksOnce(playlistId)
    }
}