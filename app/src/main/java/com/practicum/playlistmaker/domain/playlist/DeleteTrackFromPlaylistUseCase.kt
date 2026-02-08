package com.practicum.playlistmaker.domain.playlist

class DeleteTrackFromPlaylistUseCase(
    private val repository: PlaylistTracksRepository,
    private val playlistRepository: NewPlaylistRepository
) {
    suspend fun execute(playlistId: Long, trackId: Int?) {
        if (trackId == null) return

        // Удаляем трек из плейлиста
        repository.removeTrack(playlistId, trackId)

        // Обновляем количество треков в плейлисте
        val tracks = repository.getTracksOnce(playlistId)
        playlistRepository.updateTrackCount(playlistId, tracks.size)

        // Примечание: Проверка орфанов теперь происходит внутри repository.removeTrack()
    }
}