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

    private val _state = MutableLiveData(SearchScreenState())
    val state: LiveData<SearchScreenState> = _state

    private val _hasSearched = MutableLiveData(false)

    fun searchTracks(query: String) {
        _hasSearched.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tracks = searchTracksUseCase.execute(query)
                _state.postValue(
                    _state.value?.copy(
                        tracks = tracks,
                        isError = false
                    )
                )
            } catch (e: Exception) {
                _state.postValue(
                    _state.value?.copy(
                        tracks = emptyList(),
                        isError = true
                    )
                )
            }
        }
    }

    fun loadHistory() {
        val history = getHistoryUseCase.execute()
        _state.value = _state.value?.copy(history = history)
    }

    fun clearSearchResults() {
        _state.value = _state.value?.copy(tracks = emptyList())
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
        val history = getHistoryUseCase.execute()
        _state.value = _state.value?.copy(history = history)
    }
}
