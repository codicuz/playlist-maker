package com.practicum.playlistmaker.presentation.playlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.main.MainActivity
import com.practicum.playlistmaker.presentation.playlist.compose.PlaylistScreen
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import com.practicum.playlistmaker.presentation.util.ResourceProvider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlaylistFragment : Fragment() {

    private val viewModel: PlaylistViewModel by viewModel()
    private val resourceProvider: ResourceProvider by inject { parametersOf(requireContext()) }
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
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    PlaylistScreen(
                        viewModel = viewModel,
                        playlistId = playlistId,
                        onNavigateBack = {
                            activity?.onBackPressed()
                        },
                        onNavigateToPlayer = { track ->
                            openPlayer(track)
                        },
                        onNavigateToEditPlaylist = { id ->
                            val bundle = Bundle().apply {
                                putLong("playlistId", id)
                            }
                            findNavController().navigate(
                                R.id.action_playlistFragment_to_editPlaylistFragment, bundle
                            )
                        },
                        onShareText = { text ->
                            sharePlaylist(text)
                        },
                        onShowDeleteTrackDialog = { track, onConfirm ->
                            showDeleteTrackDialog(track, onConfirm)
                        },
                        onShowDeletePlaylistDialog = { playlistName, onConfirm ->
                            showDeletePlaylistDialog(playlistName, onConfirm)
                        },
                        onShowToast = { message ->
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        },
                        resourceProvider = resourceProvider
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            "playlist_updated", viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean("updated")) {
                val updatedPlaylistId = bundle.getLong("playlist_id", -1)
                if (updatedPlaylistId == playlistId) {
                    viewModel.loadPlaylist()
                }
            }
        }
    }

    private fun openPlayer(track: Track) {
        (requireActivity() as? MainActivity)?.hideBottomNav()
        val bundle = Bundle().apply { putParcelable("track", track) }
        findNavController().navigate(
            R.id.action_playlistFragment_to_audioPlayerFragment, bundle
        )
    }

    private fun sharePlaylist(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        try {
            startActivity(Intent.createChooser(intent, null))
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(), getString(R.string.no_intent_handle), Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showDeleteTrackDialog(track: Track, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(
            requireContext(), R.style.MyDialogButton
        ).setTitle(getString(R.string.delete_track_title))
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }.show()
    }

    private fun showDeletePlaylistDialog(playlistName: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(
            requireContext(), R.style.MyDialogButton
        ).setTitle(getString(R.string.delete_playlist_title, playlistName))
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }.show()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideBottomNav()
    }
}