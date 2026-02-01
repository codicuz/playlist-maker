package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.playlist.GetPlaylistsUseCase
import com.practicum.playlistmaker.domain.playlist.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val getPlaylistsUseCase: GetPlaylistsUseCase
) : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    fun loadPlaylists() {
        viewModelScope.launch {
            getPlaylistsUseCase.execute().collect { list ->
                _playlists.value = list
            }
        }
    }
}