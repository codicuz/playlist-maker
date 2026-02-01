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
    }
}
