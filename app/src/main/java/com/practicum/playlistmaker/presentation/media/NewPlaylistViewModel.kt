package com.practicum.playlistmaker.presentation.media

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.playlist.CreatePlaylistUseCase
import kotlinx.coroutines.launch

class NewPlaylistViewModel(
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : BasePlaylistViewModel() {

    override fun save() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.value = current.copy(error = getString(R.string.error_playlist_title_empty))
            return
        }

        _state.value = current.copy(isCreating = true, error = null)

        viewModelScope.launch {
            try {
                createPlaylistUseCase.execute(
                    title = current.title,
                    description = current.description,
                    coverUri = current.coverUri
                )
                _state.value = current.copy(isCreating = false, success = true)
            } catch (e: Exception) {
                _state.value = current.copy(isCreating = false, error = getString(R.string.unknown_error))
            }
        }
    }

    private fun getString(resId: Int): String {
        return when (resId) {
            R.string.error_playlist_title_empty -> "Название плейлиста не может быть пустым"
            R.string.unknown_error -> "Неизвестная ошибка"
            else -> ""
        }
    }
}