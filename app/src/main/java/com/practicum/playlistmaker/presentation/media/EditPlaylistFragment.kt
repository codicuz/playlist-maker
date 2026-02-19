package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.main.MainActivity
import com.practicum.playlistmaker.presentation.media.compose.CreatePlaylistScreen
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : Fragment() {

    private val viewModel: EditPlaylistViewModel by viewModel()
    private var playlistId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getLong("playlistId", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    CreatePlaylistScreen(viewModel = viewModel, onNavigateBack = {
                        findNavController().navigateUp()
                    }, onPlaylistCreated = { title ->
                        val message = getString(R.string.playlist_updated, title)
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                        parentFragmentManager.setFragmentResult(
                            "playlist_updated", Bundle().apply {
                                putBoolean("updated", true)
                                putLong("playlist_id", playlistId)
                            })

                        findNavController().navigateUp()
                    })
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        if (playlistId != -1L) {
            viewModel.loadPlaylist(playlistId)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideBottomNav()
    }
}