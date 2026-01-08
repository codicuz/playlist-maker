package com.practicum.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.history.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.domain.history.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.domain.history.GetSearchHistoryUseCase
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.domain.usecase.SearchTracksUseCase

class SearchViewModel(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val getHistoryUseCase: GetSearchHistoryUseCase,
    private val clearHistoryUseCase: ClearSearchHistoryUseCase,
    private val addTrackUseCase: AddTrackToHistoryUseCase,

    ) : ViewModel() {

    private val _historyLiveData = MutableLiveData<List<Track>>()
    val historyLiveData: LiveData<List<Track>> = _historyLiveData

    init {
        refreshHistory()
    }

    fun searchTracks(query: String): List<Track> {
        return searchTracksUseCase.execute(query)
    }

    fun getHistory(): List<Track> = getHistoryUseCase.execute()

    fun addTrackToHistory(track: Track) {
        addTrackUseCase.execute(track)
        refreshHistory()
    }

    fun clearHistory() {
        clearHistoryUseCase.execute()
        refreshHistory()
    }

    private fun refreshHistory() {
        _historyLiveData.value = getHistoryUseCase.execute()
    }
}
