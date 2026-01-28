package com.practicum.playlistmaker.data.playlist

import android.net.Uri
import com.practicum.playlistmaker.domain.playlist.NewPlaylistRepository

class NewPlaylistImpl: NewPlaylistRepository {
    override suspend fun createPlaylist(
        title: String,
        description: String?,
        coverUri: Uri?
    ) {
        TODO("Not yet implemented")
    }
}