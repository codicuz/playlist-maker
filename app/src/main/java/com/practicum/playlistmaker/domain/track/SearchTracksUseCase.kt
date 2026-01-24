package com.practicum.playlistmaker.domain.track

import kotlinx.coroutines.flow.Flow

class SearchTracksUseCase(private val repository: TracksRepository) {
    fun execute(searchText: String): Flow<List<Track>> {
        return repository.searchTrack(searchText)
    }
}