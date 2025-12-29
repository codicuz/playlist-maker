package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.model.Track

interface TracksRepository {
    fun searchTrack(searchString: String): List<Track>
}