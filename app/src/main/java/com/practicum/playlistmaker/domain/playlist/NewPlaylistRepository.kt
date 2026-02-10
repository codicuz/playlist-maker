package com.practicum.playlistmaker.domain.playlist

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface NewPlaylistRepository {
    suspend fun createPlaylist(title: String, description: String?, coverUri: Uri?)
    suspend fun updatePlaylist(playlistId: Long, title: String, description: String?, coverUri: Uri?)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(id: Long): Playlist?
    suspend fun updateTrackCount(id: Long, count: Int)
    suspend fun deletePlaylist(playlistId: Long)
}