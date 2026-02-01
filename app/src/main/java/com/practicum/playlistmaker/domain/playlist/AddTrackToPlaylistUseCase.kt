package com.practicum.playlistmaker.domain.playlist

import com.practicum.playlistmaker.domain.track.Track

class AddTrackToPlaylistUseCase(
    private val repository: PlaylistTracksRepository,
    private val playlistRepository: NewPlaylistRepository
) {
    suspend fun execute(playlistId: Long, track: Track): AddTrackResult {
        val playlist = playlistRepository.getPlaylistById(playlistId)
        val playlistName = playlist?.title ?: "Unknown"

        val existingTracks = repository.getTracksOnce(playlistId)
        val isAlreadyInPlaylist = existingTracks.any { it.trackId == track.trackId }

        if (isAlreadyInPlaylist) {
            return AddTrackResult.AlreadyExists(playlistName)
        }

        try {
            repository.addTrack(playlistId, track)
            val tracks = repository.getTracksOnce(playlistId)
            playlistRepository.updateTrackCount(playlistId, tracks.size)
            return AddTrackResult.Success(playlistName)
        } catch (e: Exception) {
            return AddTrackResult.Error(e.message ?: "Неизвестная ошибка")
        }
    }
}

sealed class AddTrackResult {
    data class Success(val playlistName: String) : AddTrackResult()
    data class AlreadyExists(val playlistName: String) : AddTrackResult()
    data class Error(val message: String) : AddTrackResult()
}