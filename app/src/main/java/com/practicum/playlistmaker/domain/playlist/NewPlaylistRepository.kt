package com.practicum.playlistmaker.domain.playlist

import android.net.Uri

interface NewPlaylistRepository {
    suspend fun createPlaylist(
        title: String,
        description: String?,
        coverUri: Uri?
    )
}