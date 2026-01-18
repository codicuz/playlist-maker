package com.practicum.playlistmaker.presentation.media

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class MediaViewPagerAdapter(
    fragmentManager: FragmentManager, lifecycle: Lifecycle, private val tabs: List<MediaTab>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount() = tabs.size

    override fun createFragment(position: Int): Fragment {
        return TabFragment.newInstance(tabs[position])
    }
}
