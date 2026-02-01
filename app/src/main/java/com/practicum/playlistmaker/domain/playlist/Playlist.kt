package com.practicum.playlistmaker.domain.playlist


data class Playlist(
    val id: Long,
    val title: String,
    val description: String?,
    val coverUri: String?
)