package com.practicum.playlistmaker.domain.playlist

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EditPlaylistUseCase(
    private val repository: NewPlaylistRepository
) {
    suspend fun execute(
        playlistId: Long,
        title: String,
        description: String?,
        coverUri: Uri?
    ) {
        withContext(Dispatchers.IO) {
            repository.updatePlaylist(playlistId, title, description, coverUri)
        }
    }
}