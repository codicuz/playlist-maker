package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.R

class MediaViewModel : ViewModel() {
    val tabs = listOf(MediaTab.FAVORITES, MediaTab.PLAYLISTS)
    private val _currentTab = MutableLiveData(MediaTab.FAVORITES)
    val currentTab: LiveData<MediaTab> = _currentTab

    fun setTab(tab: MediaTab) {
        _currentTab.value = tab
    }
}