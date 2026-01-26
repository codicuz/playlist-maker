package com.practicum.playlistmaker.data.favorites

import com.practicum.playlistmaker.data.favorites.db.FavoritesDao
import com.practicum.playlistmaker.data.favorites.mapper.toEntity
import com.practicum.playlistmaker.data.favorites.mapper.toTrack
import com.practicum.playlistmaker.domain.favorites.FavoritesRepository
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoritesDao
) : FavoritesRepository {

    override suspend fun add(track: Track) {
        dao.addTrack(track.toEntity())
    }

    override suspend fun remove(trackId: Int) {
        dao.removeTrack(trackId)
    }

    override fun getFavorites(): Flow<List<Track>> =
        dao.getFavorites().map { list -> list.map { it.toTrack() } }

    override suspend fun isFavorite(trackId: Int): Boolean = dao.isFavorite(trackId)
}