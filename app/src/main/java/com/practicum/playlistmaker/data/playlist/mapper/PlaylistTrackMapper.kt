package com.practicum.playlistmaker.data.playlist.mapper

import com.practicum.playlistmaker.data.playlist.entity.PlaylistTrackEntity
import com.practicum.playlistmaker.domain.track.Track

fun Track.toEntity(playlistId: Long): PlaylistTrackEntity =
    PlaylistTrackEntity(
        playlistId = playlistId,
        trackId = trackId ?: 0,
        trackName = trackName,
        artistsName = artistsName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        previewUrl = previewUrl,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country
    )

fun PlaylistTrackEntity.toTrack(): Track =
    Track(
        id = id,
        trackId = trackId,
        trackName = trackName,
        artistsName = artistsName,
        trackTimeMillis = trackTimeMillis,
        artworkUrl100 = artworkUrl100,
        previewUrl = previewUrl,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country
    )
