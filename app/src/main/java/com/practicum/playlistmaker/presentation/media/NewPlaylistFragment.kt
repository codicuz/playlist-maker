package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.practicum.playlistmaker.presentation.util.Useful
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewPlaylistViewModel by viewModel()


    private val pickCoverLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.onCoverSelected(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPress()
        }
    }

    private fun setupListeners() {
        binding.playlistTitle.addTextChangedListener {
            viewModel.onTitleChanged(it.toString())
        }

        binding.playlistDescription.addTextChangedListener {
            viewModel.onDescriptionChanged(it.toString())
        }

        binding.newPlaylistImage.setOnClickListener {
            pickCoverLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.createPlaylistButton.setOnClickListener {
            viewModel.createPlaylist()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.createPlaylistButton.isEnabled = state.isCreateEnabled
                state.coverUri?.let { uri ->
                    Glide.with(requireContext()).load(uri).transform(
                        CenterCrop(), RoundedCorners(Useful.dpToPx(8f, requireContext()))
                    ).into(binding.playlistCover)
                    binding.addCoverIcon.visibility = View.GONE
                }

                if (state.success) {
                    findNavController().navigateUp()
                }

                state.error?.let { error ->
                    MaterialAlertDialogBuilder(requireContext()).setTitle("Ошибка")
                        .setMessage(error).setPositiveButton("ОК", null).show()
                    viewModel.onDescriptionChanged(state.description) // сброс ошибки
                }
            }
        }
    }

    private fun handleBackPress() {
        val currentState = viewModel.state.value
        val hasUnsaved =
            currentState.title.isNotBlank() || currentState.description.isNotBlank() || currentState.coverUri != null

        if (hasUnsaved) {
            MaterialAlertDialogBuilder(requireContext()).setTitle("Завершить создание плейлиста?")
                .setMessage("Все несохраненные данные будут потеряны")
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("Завершить") { _, _ -> findNavController().navigateUp() }.show()
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
