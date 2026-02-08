package com.practicum.playlistmaker.data.playlist

import com.practicum.playlistmaker.data.playlist.db.PlaylistTrackDao
import com.practicum.playlistmaker.data.playlist.mapper.toEntity
import com.practicum.playlistmaker.data.playlist.mapper.toTrack
import com.practicum.playlistmaker.domain.playlist.PlaylistTracksRepository
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistTracksRepositoryImpl(
    private val dao: PlaylistTrackDao
) : PlaylistTracksRepository {

    override suspend fun addTrack(playlistId: Long, track: Track) {
        val entity = track.toEntity(playlistId)
        dao.addTrackToPlaylist(entity)
    }

    override fun getTracks(playlistId: Long): Flow<List<Track>> =
        dao.getTracksFromPlaylist(playlistId).map { list ->
            list.map { it.toTrack() }
        }

    override suspend fun removeTrack(playlistId: Long, trackId: Int) {
        dao.removeTrackFromPlaylist(playlistId, trackId)

        // Проверяем, используется ли трек в других плейлистах
        cleanupOrphanedTracks(trackId)
    }

    override suspend fun getTracksOnce(playlistId: Long): List<Track> {
        return dao.getTracksFromPlaylistOnce(playlistId).map { it.toTrack() }
    }

    override suspend fun deleteTracksForPlaylist(playlistId: Long) {
        // Получаем все треки этого плейлиста
        val tracks = dao.getTracksFromPlaylistOnce(playlistId)

        // Удаляем связи плейлиста с треками
        dao.deleteTracksFromPlaylist(playlistId)

        // Для каждого трека проверяем, используется ли он еще где-то
        tracks.forEach { trackEntity ->
            cleanupOrphanedTracks(trackEntity.trackId)
        }
    }

    // Новый метод для очистки орфанов
    private suspend fun cleanupOrphanedTracks(trackId: Int) {
        val usageCount = dao.getTrackUsageCount(trackId)
        if (usageCount == 0) {
            // Трек не используется ни в одном плейлисте - удаляем его
            dao.deleteTrackFromAllPlaylists(trackId)
        }
    }
}