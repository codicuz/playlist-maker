package com.practicum.playlistmaker.domain.favorites

import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun add(track: Track)
    suspend fun remove(trackId: Int)
    fun getFavorites(): Flow<List<Track>>
    suspend fun isFavorite(trackId: Int): Boolean
}