package com.practicum.playlistmaker.domain.track

import com.practicum.playlistmaker.domain.track.Track

interface TracksRepository {
    fun searchTrack(searchString: String): List<Track>
}