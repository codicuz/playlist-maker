package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.ViewModel

class MediaViewModel : ViewModel() {
    val tabs = listOf(MediaTab.FAVORITES, MediaTab.PLAYLISTS)
}