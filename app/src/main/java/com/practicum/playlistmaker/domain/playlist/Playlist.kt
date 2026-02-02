package com.practicum.playlistmaker.domain.playlist

import com.practicum.playlistmaker.domain.track.Track


data class Playlist(
    val id: Long,
    val title: String,
    val description: String?,
    val coverUri: String?,
    val tracksCount: List<Track> = emptyList(),
    val trackCount: Int = tracksCount.size
) {
    val trackIds: List<Int>
        get() = tracksCount.mapNotNull { it.trackId }
}