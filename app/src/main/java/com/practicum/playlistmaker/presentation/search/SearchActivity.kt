package com.practicum.playlistmaker.presentation.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.player.AudioPlayerActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var trackRecyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var stubEmptySearch: View
    private lateinit var stubServerError: View
    private lateinit var refreshButton: Button
    private lateinit var clearHistoryButton: Button
    private lateinit var searchHistoryView: View
    private lateinit var progressBar: ProgressBar

    private var currentQuery = ""

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnableOnDone: Runnable? = null
    private val debounceDelayDone = 500L
    private var searchRunnableOnTextChanged: Runnable? = null
    private val debounceDelayTextChanged = 2000L

    companion object {
        private const val SEARCH_KEY = "SEARCH_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupAdapters()

        val getHistoryUseCase = Creator.provideGetSearchHistoryUseCase(application)
        val addTrackUseCase = Creator.provideAddTrackToHistoryUseCase(application)
        val clearHistoryUseCase = Creator.provideClearSearchHistoryUseCase(application)
        val searchTracksUseCase = Creator.provideSearchTracksUseCase()

        searchViewModel = SearchViewModel(
            searchTracksUseCase, getHistoryUseCase, addTrackUseCase, clearHistoryUseCase
        )

        searchViewModel.state.observe(this) { state ->

            // === РЕЗУЛЬТАТЫ ПОИСКА ===
            if (state.tracks.isNotEmpty()) {
                adapter.submitList(state.tracks)
                trackRecyclerView.visibility = View.VISIBLE
                stubEmptySearch.visibility = View.GONE
            } else {
                trackRecyclerView.visibility = View.GONE
                stubEmptySearch.visibility = View.GONE
            }

            // === ИСТОРИЯ ===
            historyAdapter.submitList(state.history)
            updateSearchHistoryVisibility(state.history)

            // === ОШИБКА ===
            stubServerError.visibility = if (state.isError) View.VISIBLE else View.GONE

            // === ПРОГРЕСС ===
            progressBar.visibility = View.GONE
        }

        setupListeners()

        currentQuery = savedInstanceState?.getString(SEARCH_KEY) ?: ""
        if (currentQuery.isNotEmpty()) {
            searchEditText.setText(currentQuery)
            searchViewModel.searchTracks(currentQuery)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_KEY, currentQuery)
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        trackRecyclerView = findViewById(R.id.rcTrackData)
        historyRecyclerView = findViewById(R.id.rcTrackDataHistory)
        stubEmptySearch = findViewById(R.id.stubEmptySearch)
        stubServerError = findViewById(R.id.stubNoInternet)
        refreshButton = findViewById(R.id.buttonRetry)
        clearHistoryButton = findViewById(R.id.buttonClearSearchHistory)
        searchHistoryView = findViewById(R.id.stubSearchHistory)
        progressBar = findViewById(R.id.progressBar)

        findViewById<TextView>(R.id.searchHeader).setOnClickListener { finish() }
    }

    private fun setupAdapters() {
        adapter = TrackAdapter { track ->
            searchViewModel.addTrackToHistory(track)
            openPlayer(track)
        }
        trackRecyclerView.layoutManager = LinearLayoutManager(this)
        trackRecyclerView.adapter = adapter

        historyAdapter = TrackAdapter { track ->
            searchViewModel.addTrackToHistory(track)
            openPlayer(track)
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        clearButton.setOnClickListener { clearSearch() }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE

                val query = s?.toString()?.trim().orEmpty()

                if (query.isEmpty()) {
                    searchRunnableOnTextChanged?.let { handler.removeCallbacks(it) }
                    searchRunnableOnDone?.let { handler.removeCallbacks(it) }
                    searchViewModel.clearSearchResults()

                    return
                }

                if (query != currentQuery) {
                    currentQuery = query

                    searchRunnableOnTextChanged?.let { handler.removeCallbacks(it) }

                    searchRunnableOnTextChanged = Runnable {
                        searchHistoryView.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        searchViewModel.searchTracks(query)
                        hideKeyboard()
                    }

                    handler.postDelayed(searchRunnableOnTextChanged!!, debounceDelayTextChanged)
                }
            }
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchViewModel.loadHistory()
            } else {
                hideAllContent()
            }
        }



        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchRunnableOnTextChanged?.let { handler.removeCallbacks(it) }
                    searchRunnableOnDone?.let { handler.removeCallbacks(it) }
                    searchRunnableOnDone = Runnable {
                        searchHistoryView.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        searchViewModel.searchTracks(query)
                        hideKeyboard()
                    }
                    handler.postDelayed(searchRunnableOnDone!!, debounceDelayDone)
                }
                true
            } else false
        }

        refreshButton.setOnClickListener {
            if (currentQuery.isNotBlank()) {
                progressBar.visibility = View.VISIBLE
                searchViewModel.searchTracks(currentQuery)
            }
        }

        clearHistoryButton.setOnClickListener {
            searchViewModel.clearHistory()
        }
    }

    private fun hideAllContent() {
        searchHistoryView.visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE

        trackRecyclerView.visibility = View.GONE
        stubEmptySearch.visibility = View.GONE
        stubServerError.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun updateSearchHistoryVisibility(history: List<Track>) {
        val isEmptyQuery = searchEditText.text.isEmpty()
        val hasFocus = searchEditText.hasFocus()

        if (hasFocus && isEmptyQuery && history.isNotEmpty()) {
            searchHistoryView.visibility = View.VISIBLE
            historyRecyclerView.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE
        } else {
            searchHistoryView.visibility = View.GONE
            historyRecyclerView.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE
        }
    }

    private fun clearSearch() {
        searchEditText.text?.clear()
        clearButton.visibility = View.GONE
        searchViewModel.clearSearchResults()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    private fun openPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java).apply {
            putExtra("track", track)
        }
        startActivity(intent)
    }
}