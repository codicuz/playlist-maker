package com.practicum.playlistmaker.domain.playlist

class GetPlaylistByIdUseCase(
    private val repository: NewPlaylistRepository
) {
    suspend fun execute(playlistId: Long): Playlist? {
        return repository.getPlaylistById(playlistId)
    }
}