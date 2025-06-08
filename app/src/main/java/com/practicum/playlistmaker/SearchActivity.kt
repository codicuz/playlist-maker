package com.practicum.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

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

    private lateinit var searchHistory: SearchHistory

    private val tracks = mutableListOf<Track>()
    private var currentQuery = ""

    private val iTunesService by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)
    }

    companion object {
        private const val SEARCH_KEY = "SEARCH_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupAdapters()
        setupListeners()

        currentQuery = savedInstanceState?.getString(SEARCH_KEY) ?: ""
        if (currentQuery.isNotEmpty()) {
            searchEditText.setText(currentQuery)
            searchTrack(currentQuery)
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

        findViewById<TextView>(R.id.searchHeader).setOnClickListener { finish() }

        val sharedPrefs = getSharedPreferences(SharedPrefs.PREFS_NAME, MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPrefs)
    }

    private fun setupAdapters() {
        adapter = TrackAdapter(tracks, getSharedPreferences(SharedPrefs.PREFS_NAME, MODE_PRIVATE))
        trackRecyclerView.layoutManager = LinearLayoutManager(this)
        trackRecyclerView.adapter = adapter

        val history = searchHistory.getHistory()
        historyAdapter = TrackAdapter(history, getSharedPreferences(SharedPrefs.PREFS_NAME, MODE_PRIVATE))
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
        historyAdapter.submitList(history)
    }

    private fun setupListeners() {
        clearButton.setOnClickListener {
            clearSearch()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isEmpty = s.isNullOrEmpty()
                clearButton.visibility = if (isEmpty) View.GONE else View.VISIBLE
                currentQuery = s.toString()

                searchHistoryView.visibility = if (searchEditText.hasFocus() && isEmpty) View.VISIBLE else View.GONE
            }
        })

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            val isEmpty = searchEditText.text.isEmpty()
            searchHistoryView.visibility = if (hasFocus && isEmpty) View.VISIBLE else View.GONE
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    currentQuery = query
                    searchHistoryView.visibility = View.GONE
                    searchTrack(query)
                    hideKeyboard()
                }
                true
            } else false
        }

        refreshButton.setOnClickListener {
            if (currentQuery.isNotBlank()) searchTrack(currentQuery)
        }

        clearHistoryButton.setOnClickListener {
            searchHistory.clear()
            historyAdapter.submitList(emptyList())
            historyRecyclerView.visibility = View.GONE
        }
    }

    private fun clearSearch() {
        searchEditText.text?.clear()
        clearButton.visibility = View.GONE
        hideAllResults()
        searchHistoryView.visibility = if (searchEditText.hasFocus()) View.VISIBLE else View.GONE
        tracks.clear()
        adapter.notifyDataSetChanged()
        searchEditText.clearFocus()
        hideKeyboard()
    }

    private fun hideAllResults() {
        stubServerError.visibility = View.GONE
        stubEmptySearch.visibility = View.GONE
        trackRecyclerView.visibility = View.GONE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    private fun searchTrack(query: String) {
        hideAllResults()
        searchHistoryView.visibility = View.GONE

        iTunesService.findSong(query).enqueue(object : Callback<ITunesResponse> {
            override fun onResponse(call: Call<ITunesResponse>, response: Response<ITunesResponse>) {
                if (response.isSuccessful) {
                    tracks.clear()
                    response.body()?.results?.let { tracks.addAll(it) }
                    adapter.notifyDataSetChanged()

                    if (tracks.isEmpty()) {
                        stubEmptySearch.visibility = View.VISIBLE
                        trackRecyclerView.visibility = View.GONE
                    } else {
                        trackRecyclerView.visibility = View.VISIBLE
                    }
                } else {
                    showServerErrorPlaceholder()
                }
            }

            override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                showServerErrorPlaceholder()
            }
        })
    }

    private fun showServerErrorPlaceholder() {
        stubServerError.visibility = View.VISIBLE
        stubEmptySearch.visibility = View.GONE
        trackRecyclerView.visibility = View.GONE
    }
}
