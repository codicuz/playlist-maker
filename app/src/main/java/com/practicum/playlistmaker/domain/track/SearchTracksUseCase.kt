package com.practicum.playlistmaker.domain.track

class SearchTracksUseCase(
    private val repository: TracksRepository
) {

    fun execute(searchText: String): List<Track> {
        return repository.searchTrack(searchText)
    }
}