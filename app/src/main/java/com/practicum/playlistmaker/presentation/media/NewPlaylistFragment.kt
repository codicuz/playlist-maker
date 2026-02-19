package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.main.MainActivity
import com.practicum.playlistmaker.presentation.media.compose.CreatePlaylistScreen
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : Fragment() {

    private val viewModel: NewPlaylistViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    CreatePlaylistScreen(viewModel = viewModel, onNavigateBack = {
                        checkUnsavedChangesAndNavigateBack()
                    }, onPlaylistCreated = { title ->
                        val message = getString(R.string.playlist_created, title)
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    })
                }
            }
        }
    }

    private fun checkUnsavedChangesAndNavigateBack() {
        val currentState = viewModel.state.value
        val hasUnsaved =
            currentState.title.isNotBlank() || currentState.description.isNotBlank() || currentState.coverUri != null

        if (hasUnsaved) {
            MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogButton).setTitle(
                getString(
                    R.string.abort_create_playlist
                )
            ).setNegativeButton(getString(R.string.cancel_btn)) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(getString(R.string.finish_btn)) { _, _ ->
                    findNavController().navigateUp()
                }.show()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideBottomNav()
    }
}