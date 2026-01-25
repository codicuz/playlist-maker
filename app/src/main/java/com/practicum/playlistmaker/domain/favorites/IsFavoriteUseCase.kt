package com.practicum.playlistmaker.domain.favorites

class IsFavoriteUseCase(private val repo: FavoritesRepository) {
    suspend fun execute(trackId: Int) = repo.isFavorite(trackId)
}