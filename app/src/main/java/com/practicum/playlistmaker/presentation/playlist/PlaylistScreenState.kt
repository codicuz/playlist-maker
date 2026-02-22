package com.practicum.playlistmaker.presentation.playlist

import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.domain.track.Track

data class PlaylistScreenState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val totalDurationMinutes: Long = 0,
    val trackCount: Int = 0,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val bottomSheetState: BottomSheetState = BottomSheetState.HIDDEN
)

enum class BottomSheetState {
    HIDDEN, COLLAPSED, EXPANDED
}

sealed class PlaylistUiEvent {
    object NavigateBack : PlaylistUiEvent()
    data class NavigateToPlayer(val track: Track) : PlaylistUiEvent()
    data class NavigateToEditPlaylist(val playlistId: Long) : PlaylistUiEvent()
    data class ShowDeleteTrackDialog(val track: Track) : PlaylistUiEvent()
    object ShowDeletePlaylistDialog : PlaylistUiEvent()
    data class ShowToast(val message: String) : PlaylistUiEvent()
    data class SharePlaylist(val text: String) : PlaylistUiEvent()
    data class PlaylistUpdated(val playlistId: Long) : PlaylistUiEvent() // Добавлено
}