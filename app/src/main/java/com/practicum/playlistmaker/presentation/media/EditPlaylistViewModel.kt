package com.practicum.playlistmaker.presentation.media

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.playlist.EditPlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.GetPlaylistByIdUseCase
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.util.ResourceProvider
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
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class EditPlaylistViewModel(
    private val getPlaylistByIdUseCase: GetPlaylistByIdUseCase,
    private val editPlaylistUseCase: EditPlaylistUseCase,
    private val resources: ResourceProvider
) : ViewModel() {

    private val _state = MutableStateFlow(EditPlaylistScreenState())
    val state: StateFlow<EditPlaylistScreenState> = _state.asStateFlow()

    private var originalTitle = ""
    private var originalDescription = ""
    private var originalCoverUri: Uri? = null

    fun loadPlaylist(playlistId: Long) {
        _state.value = _state.value.copy(isLoading = true, error = null)

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
                        isSaveEnabled = originalTitle.isNotBlank(),
                        isLoading = false
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false, error = resources.getString(R.string.playlist_not_found)
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = resources.getString(R.string.error_download_playlist, e.message ?: "")
                )
            }
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
            _state.value = current.copy(
                error = resources.getString(R.string.no_empty_playlist_title)
            )
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
                        isSaving = false, error = resources.getString(R.string.playlist_not_found)
                    )
                }
            } catch (e: Exception) {
                _state.value = current.copy(
                    isSaving = false,
                    error = resources.getString(R.string.save_error, e.message ?: "")
                )
            }
        }
    }
}