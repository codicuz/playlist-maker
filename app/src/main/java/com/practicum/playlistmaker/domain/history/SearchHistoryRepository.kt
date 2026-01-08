package com.practicum.playlistmaker.domain.history

import com.practicum.playlistmaker.domain.track.Track

interface SearchHistoryRepository {

    fun getHistory(): List<Track>

    fun addTrack(track: Track)

    fun clearHistory()
}