package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MediaViewModel : ViewModel() {
    private val _tabs = MutableStateFlow(listOf(MediaTab.FAVORITES, MediaTab.PLAYLISTS))
    val tabs: StateFlow<List<MediaTab>> = _tabs.asStateFlow()
}