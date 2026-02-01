package com.practicum.playlistmaker.data.playlist

import android.net.Uri
import com.practicum.playlistmaker.data.playlist.db.PlaylistDao
import com.practicum.playlistmaker.data.playlist.entity.PlaylistEntity
import com.practicum.playlistmaker.data.playlist.mapper.toDomain
import com.practicum.playlistmaker.domain.playlist.NewPlaylistRepository
import com.practicum.playlistmaker.domain.playlist.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewPlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao
) : NewPlaylistRepository {

    override suspend fun createPlaylist(title: String, description: String?, coverUri: Uri?) {
        val entity = PlaylistEntity(
            title = title, description = description, coverUri = coverUri?.toString()
        )
        playlistDao.addPlaylist(entity)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylists().map { list -> list.map { it.toDomain() } }
    }
}
