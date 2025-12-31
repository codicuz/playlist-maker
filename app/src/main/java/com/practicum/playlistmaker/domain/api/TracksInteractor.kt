package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.model.Track

interface TracksInteractor {
    fun searchTracks(searchStr: String, consumer: TracksConsumer)

    interface TracksConsumer {
        fun consume(foundTracks: List<Track>)
    }
}