package com.practicum.playlistmaker.data.history

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.data.storage.SharedPrefs
import com.practicum.playlistmaker.domain.history.SearchHistoryRepository
import com.practicum.playlistmaker.domain.track.Track

class SearchHistoryRepositorImpl(private val sharedPreferences: SharedPreferences) : SearchHistoryRepository {

    private val gson = Gson()
    private val historyKey = SharedPrefs.PREFS_SEARCH_HISTORY
    private val maxHistorySize = 10

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(historyKey, null) ?: return emptyList()
        val type = object : TypeToken<List<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun addTrack(track: Track) {
        val history = getHistory().toMutableList()

        history.removeAll { it.trackId == track.trackId }

        history.add(0, track)

        if (history.size > maxHistorySize) {
            history.subList(maxHistorySize, history.size).clear()
        }

        saveHistory(history)
    }

    override fun clearHistory() {
        sharedPreferences.edit { remove(historyKey) }
    }

    private fun saveHistory(history: List<Track>) {
        val limitedHistory =
            if (history.size > maxHistorySize) history.subList(0, maxHistorySize) else history
        val json = gson.toJson(limitedHistory)
        sharedPreferences.edit { putString(historyKey, json) }
    }
}