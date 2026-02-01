package com.practicum.playlistmaker.domain.playlist

class GetPlaylistsUseCase(
    private val repository: NewPlaylistRepository
) {
    fun execute() = repository.getPlaylists()
}