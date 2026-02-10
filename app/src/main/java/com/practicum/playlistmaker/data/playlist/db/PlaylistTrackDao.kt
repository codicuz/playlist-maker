package com.practicum.playlistmaker.data.playlist.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.practicum.playlistmaker.data.playlist.entity.PlaylistTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(track: PlaylistTrackEntity)

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER BY id DESC")
    fun getTracksFromPlaylist(playlistId: Long): Flow<List<PlaylistTrackEntity>>

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Int)

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId ORDER by id DESC")
    suspend fun getTracksFromPlaylistOnce(playlistId: Long): List<PlaylistTrackEntity>

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun deleteTracksFromPlaylist(playlistId: Long)

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun getTrackUsageCount(trackId: Int): Int

    @Query("DELETE FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun deleteTrackFromAllPlaylists(trackId: Int)
}