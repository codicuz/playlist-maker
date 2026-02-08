package com.practicum.playlistmaker.domain.playlist

class DeleteTrackFromPlaylistUseCase(
    private val repository: PlaylistTracksRepository,
    private val playlistRepository: NewPlaylistRepository
) {
    suspend fun execute(playlistId: Long, trackId: Int?) {
        if (trackId == null) return

        repository.removeTrack(playlistId, trackId)

        val tracks = repository.getTracksOnce(playlistId)
        playlistRepository.updateTrackCount(playlistId, tracks.size)
    }
}