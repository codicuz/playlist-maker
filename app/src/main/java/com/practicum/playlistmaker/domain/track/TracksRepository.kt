package com.practicum.playlistmaker.domain.track

import kotlinx.coroutines.flow.Flow

interface TracksRepository {
    fun searchTrack(searchString: String): Flow<List<Track>>
}