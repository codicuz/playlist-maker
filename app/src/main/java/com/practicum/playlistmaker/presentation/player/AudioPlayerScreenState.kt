package com.practicum.playlistmaker.presentation.player

import com.practicum.playlistmaker.domain.track.Track

data class AudioPlayerScreenState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0
)