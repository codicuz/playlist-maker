package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentNewPlaylistBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : BasePlaylistFragment() {

    override val viewModel: NewPlaylistViewModel by viewModel()

    override fun getTitleText(): String = getString(R.string.new_playlist)
    override fun getButtonText(): String = getString(R.string.create_new_playlist_btn)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPress()
        }
    }

    override fun onBackButtonClicked() {
        handleBackPress()
    }

    override fun onCreateButtonClicked() {
        viewModel.save()
    }

    override fun onSuccess(title: String) {
        val message = getString(R.string.playlist_created, title)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        super.onSuccess(title)
    }

    private fun handleBackPress() {
        val currentState = viewModel.state.value
        val hasUnsaved =
            currentState.title.isNotBlank() || currentState.description.isNotBlank() || currentState.coverUri != null

        if (hasUnsaved) {
            MaterialAlertDialogBuilder(requireContext(), R.style.MyDialogButton).setTitle(
                getString(
                    R.string.abort_create_playlist
                )
            ).setMessage(getString(R.string.all_data_will_be_lost))
                .setNegativeButton(getString(R.string.cancel_btn)) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(getString(R.string.finish_btn)) { _, _ ->
                    findNavController().navigateUp()
                }.show()
        } else {
            findNavController().navigateUp()
        }
    }
}