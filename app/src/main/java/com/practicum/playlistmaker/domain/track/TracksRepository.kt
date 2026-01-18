package com.practicum.playlistmaker.domain.track

interface TracksRepository {
    fun searchTrack(searchString: String): List<Track>
}