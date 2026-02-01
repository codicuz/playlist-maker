package com.practicum.playlistmaker.data.playlist.mapper

import com.practicum.playlistmaker.data.playlist.entity.PlaylistEntity
import com.practicum.playlistmaker.domain.playlist.Playlist

fun PlaylistEntity.toDomain(): Playlist {
    return Playlist(
        id = id,
        title = title,
        description = description,
        coverUri = coverUri
    )
}