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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.databinding.EmptySearchBinding
import com.practicum.playlistmaker.databinding.NoInternetBinding
import com.practicum.playlistmaker.databinding.SearchHistoryBinding
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.player.AudioPlayerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val searchViewModel: SearchViewModel by viewModel()
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var emptySearchBinding: EmptySearchBinding
    private lateinit var noInternetBinding: NoInternetBinding
    private lateinit var searchHistoryBinding: SearchHistoryBinding
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
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emptySearchBinding = EmptySearchBinding.bind(binding.stubEmptySearchInc.root)
        noInternetBinding = NoInternetBinding.bind(binding.stubNoInternetInc.root)
        searchHistoryBinding = SearchHistoryBinding.bind(binding.stubSearchHistoryInc.root)

        initViews()
        setupAdapters()

        searchViewModel.state.observe(this) { state ->

            if (state.tracks.isNotEmpty()) {
                adapter.submitList(state.tracks)
                binding.rcTrackData.visibility = View.VISIBLE
                emptySearchBinding.root.visibility = View.GONE
            } else {
                binding.rcTrackData.visibility = View.GONE

                emptySearchBinding.root.visibility =
                    if (state.hasSearched && !state.isError) View.VISIBLE
                    else View.GONE
            }

            historyAdapter.submitList(state.history)
            updateSearchHistoryVisibility(state.history)

            noInternetBinding.root.visibility = if (state.isError) View.VISIBLE else View.GONE

            binding.progressBar.visibility = View.GONE
        }

        setupListeners()

        currentQuery = savedInstanceState?.getString(SEARCH_KEY) ?: ""
        if (currentQuery.isNotEmpty()) {
            binding.searchEditText.setText(currentQuery)
            searchViewModel.searchTracks(currentQuery)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_KEY, currentQuery)
    }

    private fun initViews() {
        historyRecyclerView = searchHistoryBinding.rcTrackDataHistory
        binding.searchHeader.setOnClickListener { finish() }
    }

    private fun setupAdapters() {
        adapter = TrackAdapter { track ->
            searchViewModel.addTrackToHistory(track)
            openPlayer(track)
        }
        binding.rcTrackData.layoutManager = LinearLayoutManager(this)
        binding.rcTrackData.adapter = adapter

        historyAdapter = TrackAdapter { track ->
            searchViewModel.addTrackToHistory(track)
            openPlayer(track)
        }

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        binding.clearButton.setOnClickListener { clearSearch() }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE

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
                        searchHistoryBinding.root.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE
                        searchViewModel.searchTracks(query)
                        hideKeyboard()
                    }

                    handler.postDelayed(searchRunnableOnTextChanged!!, debounceDelayTextChanged)
                }
            }
        })

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchViewModel.loadHistory()
            } else {
                hideAllContent()
            }
        }



        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchRunnableOnTextChanged?.let { handler.removeCallbacks(it) }
                    searchRunnableOnDone?.let { handler.removeCallbacks(it) }
                    searchRunnableOnDone = Runnable {
                        searchHistoryBinding.root.visibility = View.GONE
                        binding.progressBar.visibility = View.VISIBLE
                        searchViewModel.searchTracks(query)
                        hideKeyboard()
                    }
                    handler.postDelayed(searchRunnableOnDone!!, debounceDelayDone)
                }
                true
            } else false
        }

        noInternetBinding.buttonRetry.setOnClickListener {
            if (currentQuery.isNotBlank()) {
                binding.progressBar.visibility = View.VISIBLE
                searchViewModel.searchTracks(currentQuery)
            }
        }

        searchHistoryBinding.buttonClearSearchHistory.setOnClickListener {
            searchViewModel.clearHistory()
        }
    }

    private fun hideAllContent() {
        searchHistoryBinding.root.visibility = View.GONE
        historyRecyclerView.visibility = View.GONE
        searchHistoryBinding.buttonClearSearchHistory.visibility = View.GONE

        binding.rcTrackData.visibility = View.GONE
        emptySearchBinding.root.visibility = View.GONE
        noInternetBinding.root.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun updateSearchHistoryVisibility(history: List<Track>) {
        val isEmptyQuery = binding.searchEditText.text.isEmpty()
        val hasFocus = binding.searchEditText.hasFocus()

        if (hasFocus && isEmptyQuery && history.isNotEmpty()) {
            searchHistoryBinding.root.visibility = View.VISIBLE
            historyRecyclerView.visibility = View.VISIBLE
            searchHistoryBinding.buttonClearSearchHistory.visibility = View.VISIBLE
        } else {
            searchHistoryBinding.root.visibility = View.GONE
            historyRecyclerView.visibility = View.GONE
            searchHistoryBinding.buttonClearSearchHistory.visibility = View.GONE
        }
    }

    private fun clearSearch() {
        binding.searchEditText.text?.clear()
        binding.clearButton.visibility = View.GONE
        searchViewModel.clearSearchResults()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun openPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java).apply {
            putExtra("track", track)
        }
        startActivity(intent)
    }
}