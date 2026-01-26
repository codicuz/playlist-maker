package com.practicum.playlistmaker.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.history.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.domain.history.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.domain.history.GetSearchHistoryUseCase
import com.practicum.playlistmaker.domain.track.SearchTracksUseCase
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val getHistoryUseCase: GetSearchHistoryUseCase,
    private val addTrackUseCase: AddTrackToHistoryUseCase,
    private val clearHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel() {

    private var searchFlowJob: Job? = null
    private var clickJob: Job? = null
    private val CLICK_DEBOUNCE_DELAY = 500L

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    private var searchJob: Job? = null

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 500L
    }

    init {
        loadHistory()
    }

    fun onQueryChanged(query: String) {
        if (query.isBlank()) {
            searchJob?.cancel()
            _state.value = _state.value.copy(
                tracks = emptyList(), hasSearched = false, isError = false, isLoading = false
            )
            loadHistory()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            searchTracks(query)
        }
    }

    fun onSearchDone(query: String) {
        searchJob?.cancel()
        _state.value = _state.value.copy(isLoading = true)
        searchTracks(query)
    }

    private fun searchTracks(query: String) {
        searchFlowJob?.cancel()

        _state.value = _state.value.copy(isLoading = true)

        searchFlowJob = searchTracksUseCase.execute(query).catch {
                _state.value = _state.value.copy(
                    tracks = emptyList(), hasSearched = true, isError = true, isLoading = false
                )
            }.onEach { tracks ->
                _state.value = _state.value.copy(
                    tracks = tracks, hasSearched = true, isError = false, isLoading = false
                )
            }.launchIn(viewModelScope)
    }

    fun loadHistory() {
        _state.value = _state.value.copy(
            history = getHistoryUseCase.execute()
        )
    }

    fun addTrackToHistory(track: Track) {
        addTrackUseCase.execute(track)
        loadHistory()
    }

    fun clearHistory() {
        clearHistoryUseCase.execute()
        loadHistory()
    }

    fun clearSearchResults() {
        searchJob?.cancel()
        _state.value = _state.value.copy(
            tracks = emptyList(), hasSearched = false, isError = false
        )
        loadHistory()
    }


    fun onTrackClicked(
        track: Track, openPlayer: (Track) -> Unit
    ) {
        clickJob?.cancel()
        clickJob = viewModelScope.launch {
            delay(CLICK_DEBOUNCE_DELAY)
            addTrackToHistory(track)
            openPlayer(track)
        }
    }
}