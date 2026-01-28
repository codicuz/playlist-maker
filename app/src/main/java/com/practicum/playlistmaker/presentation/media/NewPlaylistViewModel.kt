package com.practicum.playlistmaker.presentation.media

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.playlist.CreatePlaylistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewPlaylistScreenState(
    val title: String = "",
    val description: String = "",
    val coverUri: Uri? = null,
    val isCreateEnabled: Boolean = false,
    val isCreating: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class NewPlaylistViewModel(
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewPlaylistScreenState())
    val state: StateFlow<NewPlaylistScreenState> = _state.asStateFlow()

    fun onTitleChanged(title: String) {
        _state.update { it.copy(title = title, isCreateEnabled = title.isNotBlank()) }
    }

    fun onDescriptionChanged(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun onCoverSelected(uri: Uri) {
        _state.update { it.copy(coverUri = uri) }
    }

    fun createPlaylist() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.update { it.copy(error = "Название плейлиста не может быть пустым") }
            return
        }

        _state.update { it.copy(isCreating = true, error = null) }

        viewModelScope.launch {
            try {
                createPlaylistUseCase.execute(
                    title = current.title,
                    description = current.description,
                    coverUri = current.coverUri
                )
                _state.update { it.copy(isCreating = false, success = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isCreating = false, error = e.message) }
            }
        }
    }

    private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
        value = block(value)
    }
}
