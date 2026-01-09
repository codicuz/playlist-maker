package com.practicum.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.history.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.domain.history.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.domain.history.GetSearchHistoryUseCase
import com.practicum.playlistmaker.domain.track.SearchTracksUseCase
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val getHistoryUseCase: GetSearchHistoryUseCase,
    private val addTrackUseCase: AddTrackToHistoryUseCase,
    private val clearHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel() {

    private val _historyLiveData = MutableLiveData<List<Track>>()
    val historyLiveData: LiveData<List<Track>> = _historyLiveData

    private val _tracksLiveData = MutableLiveData<List<Track>>()
    val tracksLiveData: LiveData<List<Track>> = _tracksLiveData

    init {
        refreshHistory()
    }

    fun searchTracks(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = searchTracksUseCase.execute(query)
            _tracksLiveData.postValue(result)
        }
    }

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
