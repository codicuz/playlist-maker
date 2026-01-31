package com.practicum.playlistmaker.data.favorites.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.practicum.playlistmaker.data.favorites.entity.FavoriteTrackEntity
import com.practicum.playlistmaker.data.playlist.db.PlaylistDao
import com.practicum.playlistmaker.data.playlist.entity.PlaylistEntity

@Database(
    entities = [FavoriteTrackEntity::class, PlaylistEntity::class], version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun playlistDao(): PlaylistDao
}