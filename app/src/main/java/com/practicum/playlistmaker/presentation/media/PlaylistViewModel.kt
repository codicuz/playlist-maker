package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.playlist.GetPlaylistByIdUseCase
import com.practicum.playlistmaker.domain.playlist.GetTracksForPlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaylistScreenState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val totalDurationMinutes: Long = 0,
    val trackCount: Int = 0
)

class PlaylistViewModel(
    private val getPlaylistByIdUseCase: GetPlaylistByIdUseCase,
    private val getTracksForPlaylistUseCase: GetTracksForPlaylistUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistScreenState())
    val state: StateFlow<PlaylistScreenState> = _state.asStateFlow()

    fun loadPlaylist(playlistId: Long) {
        _state.value = _state.value.copy(isLoading = true)

        viewModelScope.launch {
            val playlist = getPlaylistByIdUseCase.execute(playlistId)
            val tracks = getTracksForPlaylistUseCase.execute(playlistId)

            val totalDurationMinutes = calculateTotalDuration(tracks)
            val trackCount = tracks.size

            _state.value = _state.value.copy(
                playlist = playlist,
                tracks = tracks,
                totalDurationMinutes = totalDurationMinutes,
                trackCount = trackCount,
                isLoading = false
            )
        }
    }

    private fun calculateTotalDuration(tracks: List<Track>): Long {
        if (tracks.isEmpty()) return 0

        val totalMillis = tracks.sumOf { it.trackTimeMillis ?: 0L }
        return totalMillis / (1000 * 60)
    }
}