package com.practicum.playlistmaker.presentation.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.playlist.EditPlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.GetPlaylistByIdUseCase
import com.practicum.playlistmaker.domain.playlist.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class EditPlaylistScreenState(
    val playlist: Playlist? = null,
    val title: String = "",
    val description: String = "",
    val coverUri: Uri? = null,
    val isSaveEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class EditPlaylistViewModel(
    private val getPlaylistByIdUseCase: GetPlaylistByIdUseCase,
    private val editPlaylistUseCase: EditPlaylistUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditPlaylistScreenState())
    val state: StateFlow<EditPlaylistScreenState> = _state.asStateFlow()

    private var originalTitle = ""
    private var originalDescription = ""
    private var originalCoverUri: Uri? = null

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                val playlist = getPlaylistByIdUseCase.execute(playlistId)
                if (playlist != null) {
                    originalTitle = playlist.title
                    originalDescription = playlist.description ?: ""

                    val coverUri = playlist.coverUri?.let {
                        val file = File(it)
                        if (file.exists()) Uri.fromFile(file) else null
                    }
                    originalCoverUri = coverUri

                    _state.value = _state.value.copy(
                        playlist = playlist,
                        title = originalTitle,
                        description = originalDescription,
                        coverUri = coverUri,
                        isSaveEnabled = originalTitle.isNotBlank()
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Ошибка загрузки плейлиста: ${e.message}"
                )
            }
        }
    }

    private fun getCoverUriFromPath(path: String?, context: Context): Uri? {
        return if (!path.isNullOrEmpty()) {
            try {
                val file = File(path)
                if (file.exists()) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun onTitleChanged(title: String) {
        _state.value = _state.value.copy(
            title = title, isSaveEnabled = title.isNotBlank()
        )
    }

    fun onDescriptionChanged(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun onCoverSelected(uri: Uri) {
        _state.value = _state.value.copy(coverUri = uri)
    }

    fun hasChanges(): Boolean {
        return _state.value.title != originalTitle || _state.value.description != originalDescription || _state.value.coverUri != originalCoverUri
    }

    fun savePlaylist() {
        val current = _state.value
        if (current.title.isBlank()) {
            _state.value = current.copy(error = "Название плейлиста не может быть пустым")
            return
        }

        if (!hasChanges()) {
            _state.value = current.copy(success = true)
            return
        }

        _state.value = current.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                current.playlist?.let { playlist ->
                    editPlaylistUseCase.execute(
                        playlistId = playlist.id,
                        title = current.title,
                        description = current.description,
                        coverUri = current.coverUri
                    )
                    _state.value = current.copy(isSaving = false, success = true)
                } ?: run {
                    _state.value = current.copy(
                        isSaving = false, error = "Плейлист не найден"
                    )
                }
            } catch (e: Exception) {
                _state.value = current.copy(
                    isSaving = false, error = "Ошибка сохранения: ${e.message}"
                )
            }
        }
    }
}