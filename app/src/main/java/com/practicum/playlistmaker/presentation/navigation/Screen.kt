package com.practicum.playlistmaker.presentation.navigation

sealed class Screen(val route: String) {
    object Search : Screen("search")
    object Media : Screen("media")
    object Settings : Screen("settings")
    object Player : Screen("player/{trackId}") {
        fun passTrackId(trackId: Int): String = "player/$trackId"
    }
    object NewPlaylist : Screen("new_playlist")
    object Playlist : Screen("playlist/{playlistId}") {
        fun passPlaylistId(playlistId: Long): String = "playlist/$playlistId"
    }
    object EditPlaylist : Screen("edit_playlist/{playlistId}") {
        fun passPlaylistId(playlistId: Long): String = "edit_playlist/$playlistId"
    }
}