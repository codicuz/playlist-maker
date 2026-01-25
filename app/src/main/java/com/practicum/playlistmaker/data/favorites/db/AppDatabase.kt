package com.practicum.playlistmaker.data.favorites.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.practicum.playlistmaker.data.favorites.entity.FavoriteTrackEntity

@Database(
    entities = [FavoriteTrackEntity::class], version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}