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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val getHistoryUseCase: GetSearchHistoryUseCase,
    private val addTrackUseCase: AddTrackToHistoryUseCase,
    private val clearHistoryUseCase: ClearSearchHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SearchScreenState())
    val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    private var searchFlowJob: Job? = null
    private var clickJob: Job? = null

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 500L
        private const val CLICK_DEBOUNCE_DELAY = 500L
    }

    init {
        observeQuery()
        loadHistory()
    }

    private fun observeQuery() {
        viewModelScope.launch {
            state.map { it.query }.distinctUntilChanged().debounce(SEARCH_DEBOUNCE_DELAY)
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _state.update {
                            it.copy(
                                tracks = emptyList(),
                                hasSearched = false,
                                isError = false,
                                isLoading = false
                            )
                        }
                        loadHistory()
                    } else {
                        searchTracks(query)
                    }
                }
        }
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun onSearchDone(query: String) {
        _state.update { it.copy(query = query) }
        searchTracks(query)
    }

    private fun searchTracks(query: String) {
        searchFlowJob?.cancel()

        _state.update { it.copy(isLoading = true) }

        searchFlowJob = searchTracksUseCase.execute(query).catch {
                _state.update {
                    it.copy(
                        tracks = emptyList(), hasSearched = true, isError = true, isLoading = false
                    )
                }
            }.onEach { tracks ->
                _state.update {
                    it.copy(
                        tracks = tracks, hasSearched = true, isError = false, isLoading = false
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun loadHistory() {
        _state.update {
            it.copy(history = getHistoryUseCase.execute())
        }
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
        _state.update {
            it.copy(
                tracks = emptyList(), hasSearched = false, isError = false, isLoading = false
            )
        }
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