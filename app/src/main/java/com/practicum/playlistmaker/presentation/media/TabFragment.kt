package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentTabBinding

class TabFragment : Fragment() {
    companion object {
        private const val TAB_TYPE = "tab_type"

        fun newInstance(tab: MediaTab) = TabFragment().apply {
            arguments = Bundle().apply {
                putSerializable(TAB_TYPE, tab)
            }
        }
    }

    private lateinit var binding: FragmentTabBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tab = requireArguments().getSerializable(TAB_TYPE) as MediaTab
        binding.mediaTab.text = when (tab) {
            MediaTab.FAVORITES -> getString(R.string.empty_media)
            MediaTab.PLAYLISTS -> getString(R.string.empty_playlists)
        }
    }
}