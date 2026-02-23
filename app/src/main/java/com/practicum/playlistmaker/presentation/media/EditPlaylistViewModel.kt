package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.playlist.EditPlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.GetPlaylistByIdUseCase
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.util.ResourceProvider
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    private val getPlaylistByIdUseCase: GetPlaylistByIdUseCase,
    private val editPlaylistUseCase: EditPlaylistUseCase,
    private val resources: ResourceProvider
) : BasePlaylistViewModel() {

    private var originalTitle = ""
    private var originalDescription = ""
    private var playlist: Playlist? = null

    fun loadPlaylist(playlistId: Long) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val loadedPlaylist = getPlaylistByIdUseCase.execute(playlistId)
                if (loadedPlaylist != null) {
                    playlist = loadedPlaylist
                    originalTitle = loadedPlaylist.title
                    originalDescription = loadedPlaylist.description ?: ""

                    _state.value = _state.value.copy(
                        title = originalTitle,
                        description = originalDescription,
                        originalCoverUri = loadedPlaylist.coverUri,
                        isCreateEnabled = originalTitle.isNotBlank(),
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

    private fun hasChanges(): Boolean {
        return _state.value.title != originalTitle ||
                _state.value.description != originalDescription ||
                _state.value.coverUri != null
    }

    override fun save() {
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

        _state.value = current.copy(isCreating = true, error = null)

        viewModelScope.launch {
            try {
                playlist?.let { loadedPlaylist ->
                    editPlaylistUseCase.execute(
                        playlistId = loadedPlaylist.id,
                        title = current.title,
                        description = current.description,
                        coverUri = current.coverUri
                    )
                    _state.value = current.copy(isCreating = false, success = true)
                } ?: run {
                    _state.value = current.copy(
                        isCreating = false, error = resources.getString(R.string.playlist_not_found)
                    )
                }
            } catch (e: Exception) {
                _state.value = current.copy(
                    isCreating = false,
                    error = resources.getString(R.string.save_error, e.message ?: "")
                )
            }
        }
    }
}