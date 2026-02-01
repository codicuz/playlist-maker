package com.practicum.playlistmaker.domain.playlist

import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistTracksRepository {
    suspend fun addTrack(playlistId: Long, track: Track)
    fun getTracks(playlistId: Long): Flow<List<Track>>
    suspend fun getTracksOnce(playlistId: Long): List<Track>
    suspend fun removeTrack(playlistId: Long, trackId: Int)

}