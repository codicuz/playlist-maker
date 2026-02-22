package com.practicum.playlistmaker.presentation.player

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
import com.practicum.playlistmaker.presentation.player.compose.AudioPlayerScreen
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import com.practicum.playlistmaker.presentation.util.ResourceProvider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AudioPlayerFragment : Fragment() {

    private val viewModel: AudioPlayerViewModel by viewModel()
    private val resourceProvider: ResourceProvider by inject { parametersOf(requireContext()) }
    private var track: Track? = null

    companion object {
        private const val ARG_TRACK = "track"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            track = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_TRACK, Track::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable(ARG_TRACK)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    AudioPlayerScreen(
                        viewModel = viewModel,
                        resourceProvider = resourceProvider,
                        trackId = track?.trackId,
                        onNavigateBack = {
                            findNavController().navigateUp()
                        },
                        onCreatePlaylistClick = {
                            findNavController().navigate(R.id.action_audioPlayerFragment_to_newPlaylistFragment)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? MainActivity)?.hideBottomNav()

        track?.let {
            viewModel.bindService(requireContext())
            viewModel.setTrack(it)
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.hideBottomNav()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppBackgrounded()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.unbindService(requireContext())
    }
}