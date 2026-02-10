package com.practicum.playlistmaker.domain.playlist

class DeletePlaylistUseCase(
    private val playlistRepository: NewPlaylistRepository,
    private val playlistTracksRepository: PlaylistTracksRepository
) {
    suspend fun execute(playlistId: Long) {
        playlistTracksRepository.deleteTracksForPlaylist(playlistId)
        playlistRepository.deletePlaylist(playlistId)
    }
}