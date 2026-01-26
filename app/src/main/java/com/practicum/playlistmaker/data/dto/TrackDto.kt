package com.practicum.playlistmaker.data.dto

import com.google.gson.annotations.SerializedName

data class TrackDto(
    val id: Long,
    val trackId: Int?,
    val trackName: String?,
    @SerializedName("artistName") val artistsName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val previewUrl: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?
)
