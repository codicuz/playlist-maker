package com.practicum.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {
    private var searchText: String = SEARCH_TEXT

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
        val trackRcView = findViewById<RecyclerView>(R.id.rcTrackData)

        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_KEY, SEARCH_TEXT)
        }

        clearButton.setOnClickListener {
            inputEditText.text?.clear()
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

        trackRcView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        trackRcView.adapter = TrackAdapter(Track.getMockTrackList())
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}
