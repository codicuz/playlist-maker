package com.practicum.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    private var searchText: String = SEARCH_TEXT
    private var lastSearchQuery: String = ""

    private lateinit var adapter: TrackAdapter
    private lateinit var trackRcView: RecyclerView
    private lateinit var stubEmptySearch: View
    private lateinit var stubServerError: View
    private lateinit var refreshButton: Button

    private val tracks = ArrayList<Track>()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val ITunesService = retrofit.create(ITunesApi::class.java)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_KEY, searchText)
    }

    companion object {
        const val SEARCH_KEY = "SEARCH_KEY"
        const val SEARCH_TEXT = ""
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_KEY, SEARCH_TEXT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val buttonBack = findViewById<TextView>(R.id.searchHeader)
        val clearButton = findViewById<ImageView>(R.id.clearButton)
        val inputEditText = findViewById<EditText>(R.id.inputEditText)

        trackRcView = findViewById(R.id.rcTrackData)
        stubEmptySearch = findViewById(R.id.stubEmptySearch)
        stubServerError = findViewById(R.id.stubNoInternet)
        refreshButton = findViewById(R.id.buttonRetry)

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_KEY, SEARCH_TEXT)
        }

        clearButton.setOnClickListener {
            inputEditText.text?.clear()
            clearButton.visibility = View.GONE
            trackRcView.visibility = View.GONE
            stubEmptySearch.visibility = View.GONE
            stubServerError.visibility = View.GONE
            tracks.clear()
            adapter.notifyDataSetChanged()
            inputEditText.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
        }

        buttonBack.setOnClickListener {
            finish()
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // empty
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
                searchText = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // empty
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)

        adapter = TrackAdapter(tracks)
        trackRcView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        trackRcView.adapter = adapter

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = inputEditText.text.toString().trim()
                lastSearchQuery = query
                searchTrack(query)
            }
            false
        }

        refreshButton.setOnClickListener {
            if (lastSearchQuery.isNotBlank()) {
                searchTrack(lastSearchQuery)
            }
        }
    }

    private fun searchTrack(query: String) {
        stubServerError.visibility = View.GONE
        stubEmptySearch.visibility = View.GONE
        trackRcView.visibility = View.GONE

        ITunesService.findSong(query).enqueue(object : Callback<ITunesResponse> {
            override fun onResponse(
                call: Call<ITunesResponse>,
                response: Response<ITunesResponse>
            ) {
                if (response.isSuccessful) {
                    tracks.clear()
                    response.body()?.results?.let { tracks.addAll(it) }
                    adapter.notifyDataSetChanged()

                    if (tracks.isEmpty()) {
                        stubEmptySearch.visibility = View.VISIBLE
                    } else {
                        trackRcView.visibility = View.VISIBLE
                    }
                } else {
                    Log.w("SearchActivity", "Response error: ${response.code()}")
                    showServerErrorPlaceholder()
                }
            }

            override fun onFailure(call: Call<ITunesResponse>, t: Throwable) {
                Log.e("SearchActivity", "Network error", t)
                showServerErrorPlaceholder()
            }
        })
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun showServerErrorPlaceholder() {
        stubServerError.visibility = View.VISIBLE
        trackRcView.visibility = View.GONE
        stubEmptySearch.visibility = View.GONE
    }
}
