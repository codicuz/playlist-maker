package com.practicum.playlistmaker.presentation.search

import com.practicum.playlistmaker.domain.track.Track

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Content(val tracks: List<Track>) : SearchState()
    object Empty : SearchState()
    object Error : SearchState()
}
