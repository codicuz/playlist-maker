package com.practicum.playlistmaker.data.dto

data class TrackDto(
    val trackId: Int?,
    val trackName: String?,
    val artistsName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val previewUrl: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?
)
