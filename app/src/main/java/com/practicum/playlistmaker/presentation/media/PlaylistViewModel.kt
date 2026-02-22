package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.playlist.DeletePlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.DeleteTrackFromPlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.GetPlaylistByIdUseCase
import com.practicum.playlistmaker.domain.playlist.GetTracksForPlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.util.ResourceProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaylistScreenState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val totalDurationMinutes: Long = 0,
    val trackCount: Int = 0,
    val isDeleting: Boolean = false,
    val deletionSuccess: Boolean = false,
    val error: String? = null
)

class PlaylistViewModel(
    private val getPlaylistByIdUseCase: GetPlaylistByIdUseCase,
    private val getTracksForPlaylistUseCase: GetTracksForPlaylistUseCase,
    private val deleteTrackFromPlaylistUseCase: DeleteTrackFromPlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val resources: ResourceProvider
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistScreenState())
    val state: StateFlow<PlaylistScreenState> = _state.asStateFlow()

    private val _deletionEvent = MutableStateFlow<DeletionEvent?>(null)
    val deletionEvent: StateFlow<DeletionEvent?> = _deletionEvent.asStateFlow()

    private val _playlistUpdatedEvent = MutableSharedFlow<Long>()
    val playlistUpdatedEvent: SharedFlow<Long> = _playlistUpdatedEvent.asSharedFlow()

    sealed class DeletionEvent {
        object Success : DeletionEvent()
        data class Error(val message: String) : DeletionEvent()
    }

    fun loadPlaylist(playlistId: Long) {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val playlist = getPlaylistByIdUseCase.execute(playlistId)
                val tracks = getTracksForPlaylistUseCase.execute(playlistId)

                val totalDurationMinutes = calculateTotalDuration(tracks)
                val trackCount = tracks.size

                _state.update {
                    it.copy(
                        playlist = playlist,
                        tracks = tracks,
                        totalDurationMinutes = totalDurationMinutes,
                        trackCount = trackCount,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false, error = e.message ?: resources.getString(
                            R.string.error_download_playlist, e.message ?: ""
                        )
                    )
                }
            }
        }
    }

    fun deleteTrackFromPlaylist(track: Track) {
        viewModelScope.launch {
            _state.value.playlist?.let { playlist ->
                try {
                    deleteTrackFromPlaylistUseCase.execute(playlist.id, track.trackId)

                    _state.update { currentState ->
                        val updatedTracks =
                            currentState.tracks.filter { it.trackId != track.trackId }
                        val totalDurationMinutes = calculateTotalDuration(updatedTracks)

                        val updatedPlaylist = currentState.playlist?.let { pl ->
                            pl.copy(tracksCount = updatedTracks)
                        }

                        currentState.copy(
                            playlist = updatedPlaylist,
                            tracks = updatedTracks,
                            totalDurationMinutes = totalDurationMinutes,
                            trackCount = updatedTracks.size
                        )
                    }

                    // Отправляем событие об обновлении плейлиста
                    _playlistUpdatedEvent.emit(playlist.id)

                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            error = resources.getString(
                                R.string.error_deleting_track, e.message ?: ""
                            )
                        )
                    }
                }
            }
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            _state.value.playlist?.let { playlist ->
                _state.update { it.copy(isDeleting = true, error = null) }

                try {
                    deletePlaylistUseCase.execute(playlist.id)
                    _deletionEvent.value = DeletionEvent.Success
                } catch (e: Exception) {
                    _state.update { it.copy(isDeleting = false) }
                    _deletionEvent.value = DeletionEvent.Error(
                        e.message
                            ?: resources.getString(R.string.unknown_error_over_deleting_playlist)
                    )
                }
            }
        }
    }

    fun resetDeletionEvent() {
        _deletionEvent.value = null
    }

    private fun calculateTotalDuration(tracks: List<Track>): Long {
        if (tracks.isEmpty()) return 0

        val totalMillis = tracks.sumOf { it.trackTimeMillis ?: 0L }
        return totalMillis / (1000 * 60)
    }
}