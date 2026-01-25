package com.practicum.playlistmaker.data.favorites.mapper

import com.practicum.playlistmaker.data.favorites.entity.FavoriteTrackEntity
import com.practicum.playlistmaker.domain.track.Track


fun FavoriteTrackEntity.toTrack(): Track =
    Track(
        trackId,
        trackName,
        artistsName,
        trackTimeMillis,
        artworkUrl100,
        previewUrl,
        collectionName,
        releaseDate,
        primaryGenreName,
        country
    )

fun Track.toEntity(): FavoriteTrackEntity =
    FavoriteTrackEntity(
        trackId!!,
        trackName,
        artistsName,
        trackTimeMillis,
        artworkUrl100,
        previewUrl,
        collectionName,
        releaseDate,
        primaryGenreName,
        country
    )