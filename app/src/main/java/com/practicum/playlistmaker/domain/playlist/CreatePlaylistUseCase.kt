package com.practicum.playlistmaker.domain.playlist

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreatePlaylistUseCase(
    private val repository: NewPlaylistRepository
) {
    suspend fun execute(
        title: String, description: String?, coverUri: Uri?
    ) {
        withContext(Dispatchers.IO) {
            repository.createPlaylist(
                title = title, description = description, coverUri = coverUri
            )
        }
    }
}