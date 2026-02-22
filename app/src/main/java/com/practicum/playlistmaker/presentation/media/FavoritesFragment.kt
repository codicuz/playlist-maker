package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.main.MainActivity
import com.practicum.playlistmaker.presentation.media.compose.FavoritesTab
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private val viewModel: FavoritesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    FavoritesTab(
                        viewModel = viewModel,
                        onTrackClick = { track ->
                            openPlayer(track)
                        }
                    )
                }
            }
        }
    }

    private fun openPlayer(track: Track) {
        (requireActivity() as? MainActivity)?.hideBottomNav()
        val bundle = Bundle().apply { putParcelable("track", track) }
        findNavController().navigate(R.id.action_mediaFragment_to_audioPlayerFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showBottomNav()
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}