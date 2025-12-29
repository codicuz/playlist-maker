package com.practicum.playlistmaker.domain.usecase

import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.model.Track

class SearchTracksUseCase(
    private val repository: TracksRepository
) {

    fun execute(searchText: String): List<Track> {
        return repository.searchTrack(searchText)
    }
}
