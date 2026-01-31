package com.practicum.playlistmaker.data.playlist

import android.net.Uri
import com.practicum.playlistmaker.data.playlist.db.PlaylistDao
import com.practicum.playlistmaker.data.playlist.entity.PlaylistEntity
import com.practicum.playlistmaker.domain.playlist.NewPlaylistRepository

class NewPlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao
) : NewPlaylistRepository {

    override suspend fun createPlaylist(title: String, description: String?, coverUri: Uri?) {
        val entity = PlaylistEntity(
            title = title, description = description, coverUri = coverUri?.toString()
        )
        playlistDao.addPlaylist(entity)
    }
}
