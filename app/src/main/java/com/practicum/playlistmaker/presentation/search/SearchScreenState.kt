package com.practicum.playlistmaker.presentation.search

import com.practicum.playlistmaker.domain.track.Track

data class SearchScreenState(
    val tracks: List<Track> = emptyList(),
    val history: List<Track> = emptyList()
)
