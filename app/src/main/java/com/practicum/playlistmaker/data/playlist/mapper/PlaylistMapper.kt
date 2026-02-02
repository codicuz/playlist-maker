package com.practicum.playlistmaker.data.playlist.mapper

import com.practicum.playlistmaker.data.playlist.entity.PlaylistEntity
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.domain.track.Track

fun PlaylistEntity.toDomain(tracks: List<Track> = emptyList()): Playlist {
    return Playlist(
        id = id, title = title, description = description, coverUri = coverUri, tracksCount = tracks
    )
}