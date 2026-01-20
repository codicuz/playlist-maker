package com.practicum.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityMainBinding
import com.practicum.playlistmaker.presentation.media.MediaActivity
import com.practicum.playlistmaker.presentation.search.SearchActivity
import com.practicum.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container_view) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)




//        binding.buttonSearch.setOnClickListener {
//            startActivity(
//                Intent(
//                    this, SearchActivity::class.java
//                )
//            )
//        }
//        binding.buttonMedia.setOnClickListener {
//            startActivity(
//                Intent(
//                    this, MediaActivity::class.java
//                )
//            )
//        }
//        binding.buttonSettings.setOnClickListener {
//            startActivity(
//                Intent(
//                    this, SettingsActivity::class.java
//                )
//            )
//        }
    }
}
