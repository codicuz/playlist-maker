package com.practicum.playlistmaker.data.playlist

import android.net.Uri
import com.practicum.playlistmaker.data.playlist.db.PlaylistDao
import com.practicum.playlistmaker.data.playlist.db.PlaylistTrackDao
import com.practicum.playlistmaker.data.playlist.entity.PlaylistEntity
import com.practicum.playlistmaker.data.playlist.mapper.toDomain
import com.practicum.playlistmaker.data.playlist.mapper.toTrack
import com.practicum.playlistmaker.domain.playlist.NewPlaylistRepository
import com.practicum.playlistmaker.domain.playlist.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewPlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTrackDao: PlaylistTrackDao,
    private val coverFileManager: CoverFileManager
) : NewPlaylistRepository {

    override suspend fun createPlaylist(title: String, description: String?, coverUri: Uri?) {
        val internalCoverPath = coverUri?.let { coverFileManager.copyCoverToInternalStorage(it) }

        val entity = PlaylistEntity(
            title = title, description = description, coverUri = internalCoverPath
        )
        playlistDao.addPlaylist(entity)
    }

    override suspend fun updatePlaylist(
        playlistId: Long,
        title: String,
        description: String?,
        coverUri: Uri?
    ) {
        val internalCoverPath = coverUri?.let { coverFileManager.copyCoverToInternalStorage(it) }

        val existingPlaylist = playlistDao.getPlaylistById(playlistId)
        if (existingPlaylist != null) {
            val updatedEntity = existingPlaylist.copy(
                title = title,
                description = description,
                coverUri = internalCoverPath ?: existingPlaylist.coverUri
            )
            playlistDao.updatePlaylist(updatedEntity)
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylists().map { playlistEntities ->
            playlistEntities.map { playlistEntity ->
                val tracks = getTracksForPlaylist(playlistEntity.id)
                playlistEntity.toDomain(tracks)
            }
        }
    }

    override suspend fun getPlaylistById(id: Long): Playlist? {
        val entity = playlistDao.getPlaylistById(id) ?: return null
        val tracks = playlistTrackDao.getTracksFromPlaylistOnce(id).map { it.toTrack() }
        return entity.toDomain(tracks)
    }

    override suspend fun updateTrackCount(id: Long, count: Int) {
        playlistDao.updateTrackCount(id, count)
    }

    private suspend fun getTracksForPlaylist(playlistId: Long): List<com.practicum.playlistmaker.domain.track.Track> {
        return playlistTrackDao.getTracksFromPlaylistOnce(playlistId).map { it.toTrack() }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }
}