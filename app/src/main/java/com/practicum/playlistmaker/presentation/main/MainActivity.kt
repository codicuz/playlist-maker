package com.practicum.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.practicum.playlistmaker.databinding.ActivityMainBinding
import com.practicum.playlistmaker.presentation.media.MediaActivity
import com.practicum.playlistmaker.presentation.search.SearchActivity
import com.practicum.playlistmaker.presentation.settings.SettingsActivity
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val themeViewModel: ThemeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSearch.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SearchActivity::class.java
                )
            )
        }
        binding.buttonMedia.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    MediaActivity::class.java
                )
            )
        }
        binding.buttonSettings.setOnClickListener {
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
