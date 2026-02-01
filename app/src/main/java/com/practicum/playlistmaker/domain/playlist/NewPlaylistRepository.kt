package com.practicum.playlistmaker.domain.playlist

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface NewPlaylistRepository {
    suspend fun createPlaylist(
        title: String, description: String?, coverUri: Uri?
    )

    fun getPlaylists(): Flow<List<Playlist>>
}