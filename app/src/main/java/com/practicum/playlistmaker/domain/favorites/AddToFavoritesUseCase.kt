package com.practicum.playlistmaker.domain.favorites

import com.practicum.playlistmaker.domain.track.Track

class AddToFavoritesUseCase(private val repo: FavoritesRepository) {
    suspend fun execute(track: Track) = repo.add(track)
}