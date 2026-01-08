package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.model.Track

interface SearchHistory {

    fun getHistory(): List<Track>

    fun addTrack(track: Track)

    fun clearHistory()
}