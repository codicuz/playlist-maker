package com.practicum.playlistmaker.domain.favorites

class RemoveFromFavoritesUseCase(private val repo: FavoritesRepository) {
    suspend fun execute(trackId: Int) = repo.remove(trackId)
}