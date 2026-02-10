package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.practicum.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : BasePlaylistFragment() {

    override val viewModel: EditPlaylistViewModel by viewModel()

    private var playlistId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getLong("playlistId", -1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.loadPlaylist(playlistId)
    }

    override fun getTitleText(): String = getString(R.string.edit_playlist)
    override fun getButtonText(): String = getString(R.string.save)

    override fun onBackButtonClicked() {
        findNavController().navigateUp()
    }

    override fun onCreateButtonClicked() {
        viewModel.save()
    }

    override fun onSuccess(title: String) {
        val message = getString(R.string.playlist_updated, title)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

        parentFragmentManager.setFragmentResult(
            "playlist_updated", Bundle().apply {
                putBoolean("updated", true)
                putLong("playlist_id", playlistId)
            })

        super.onSuccess(title)
    }
}