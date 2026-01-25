package com.practicum.playlistmaker.domain.favorites

class GetFavoritesUseCase(private val repo: FavoritesRepository) {
    fun execute() = repo.getFavorites()
}