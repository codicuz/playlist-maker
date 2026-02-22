package com.practicum.playlistmaker.presentation.playlist

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

class PlaylistViewModel(
    private val getPlaylistByIdUseCase: GetPlaylistByIdUseCase,
    private val getTracksForPlaylistUseCase: GetTracksForPlaylistUseCase,
    private val deleteTrackFromPlaylistUseCase: DeleteTrackFromPlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val resources: ResourceProvider
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistScreenState())
    val state: StateFlow<PlaylistScreenState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<PlaylistUiEvent>()
    val uiEvent: SharedFlow<PlaylistUiEvent> = _uiEvent.asSharedFlow()

    private var playlistId: Long = -1
    private var trackToDelete: Track? = null

    fun setPlaylistId(id: Long) {
        if (playlistId != id) {
            playlistId = id
            loadPlaylist()
        }
    }

    fun loadPlaylist() {
        if (playlistId == -1L) return

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

    fun onTrackClick(track: Track) {
        viewModelScope.launch {
            _uiEvent.emit(PlaylistUiEvent.NavigateToPlayer(track))
        }
    }

    fun onTrackLongClick(track: Track) {
        trackToDelete = track
        viewModelScope.launch {
            _uiEvent.emit(PlaylistUiEvent.ShowDeleteTrackDialog(track))
        }
    }

    fun confirmDeleteTrack() {
        val track = trackToDelete ?: return
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
                    trackToDelete = null

                    _uiEvent.emit(PlaylistUiEvent.PlaylistUpdated(playlist.id))

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

    fun onShareClick() {
        val tracks = _state.value.tracks
        val playlist = _state.value.playlist

        if (tracks.isEmpty()) {
            viewModelScope.launch {
                _uiEvent.emit(
                    PlaylistUiEvent.ShowToast(
                        resources.getString(R.string.no_shareable_playlist)
                    )
                )
            }
            return
        }

        val shareText = buildShareText(playlist, tracks)
        viewModelScope.launch {
            _uiEvent.emit(PlaylistUiEvent.SharePlaylist(shareText))
        }
    }

    fun onEditClick() {
        _state.value.playlist?.let { playlist ->
            viewModelScope.launch {
                _uiEvent.emit(PlaylistUiEvent.NavigateToEditPlaylist(playlist.id))
            }
        }
    }

    fun onDeletePlaylistClick() {
        viewModelScope.launch {
            _uiEvent.emit(PlaylistUiEvent.ShowDeletePlaylistDialog)
        }
    }

    fun confirmDeletePlaylist() {
        viewModelScope.launch {
            _state.value.playlist?.let { playlist ->
                _state.update { it.copy(isDeleting = true, error = null) }

                try {
                    deletePlaylistUseCase.execute(playlist.id)
                    _uiEvent.emit(PlaylistUiEvent.NavigateBack)
                } catch (e: Exception) {
                    _state.update { it.copy(isDeleting = false) }
                    _uiEvent.emit(
                        PlaylistUiEvent.ShowToast(
                            e.message
                                ?: resources.getString(R.string.unknown_error_over_deleting_playlist)
                        )
                    )
                }
            }
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _uiEvent.emit(PlaylistUiEvent.NavigateBack)
        }
    }

    private fun buildShareText(playlist: Playlist?, tracks: List<Track>): String {
        val builder = StringBuilder()

        playlist?.title?.let { title ->
            builder.append(title).append("\n")
        }

        playlist?.description?.takeIf { it.isNotBlank() }?.let { description ->
            builder.append(description).append("\n")
        }

        val trackCount = tracks.size
        val tracksText = resources.getQuantityString(
            R.plurals.tracks_count, trackCount, trackCount
        )
        builder.append("$tracksText\n\n")

        tracks.forEachIndexed { index, track ->
            val trackNumber = index + 1
            val artist = track.artistsName ?: resources.getString(R.string.unknown_artist)
            val trackName = track.trackName ?: resources.getString(R.string.unknown_track_name)
            val duration = track.trackTime ?: resources.getString(R.string.unknown_track_time)

            builder.append("$trackNumber. $artist - $trackName ($duration)\n")
        }

        return builder.toString()
    }

    private fun calculateTotalDuration(tracks: List<Track>): Long {
        if (tracks.isEmpty()) return 0
        val totalMillis = tracks.sumOf { it.trackTimeMillis ?: 0L }
        return totalMillis / (1000 * 60)
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}