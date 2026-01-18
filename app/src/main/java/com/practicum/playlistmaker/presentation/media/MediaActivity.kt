package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityMediaBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaBinding
    private val viewModel: MediaViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter =
            MediaViewPagerAdapter(supportFragmentManager, lifecycle, viewModel.tabs)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (viewModel.tabs[position]) {
                MediaTab.FAVORITES -> getString(R.string.favourite_tracks)
                MediaTab.PLAYLISTS -> getString(R.string.playlists)
            }
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.createPlaylistButton.visibility =
                    if (viewModel.tabs[position] == MediaTab.PLAYLISTS) View.VISIBLE else View.INVISIBLE
            }
        })

        binding.mediaHeader.setOnClickListener { finish() }
    }
}