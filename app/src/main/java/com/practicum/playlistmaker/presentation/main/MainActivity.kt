package com.practicum.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.media.MediaActivity
import com.practicum.playlistmaker.presentation.search.SearchActivity
import com.practicum.playlistmaker.presentation.settings.SettingsActivity
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val themeViewModel: ThemeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSearch = findViewById<Button>(R.id.buttonSearch)
        val buttonMedia = findViewById<Button>(R.id.buttonMedia)
        val buttonSettings = findViewById<Button>(R.id.buttonSettings)

        buttonSearch.setOnClickListener { startActivity(Intent(this, SearchActivity::class.java)) }
        buttonMedia.setOnClickListener { startActivity(Intent(this, MediaActivity::class.java)) }
        buttonSettings.setOnClickListener {
            startActivity(
                Intent(
                    this, SettingsActivity::class.java
                )
            )
        }

        themeViewModel.state.observe(this) { state ->
            state.isDarkMode
        }
    }
}
