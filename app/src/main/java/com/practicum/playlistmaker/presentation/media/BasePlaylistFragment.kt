package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.practicum.playlistmaker.presentation.main.MainActivity
import com.practicum.playlistmaker.presentation.util.Useful
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

abstract class BasePlaylistFragment : Fragment() {

    protected var _binding: FragmentNewPlaylistBinding? = null
    protected val binding get() = _binding!!

    protected abstract val viewModel: BasePlaylistViewModel

    protected val pickCoverLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.onCoverSelected(uri)
            }
        }

    protected abstract fun getTitleText(): String
    protected abstract fun getButtonText(): String
    protected abstract fun onBackButtonClicked()
    protected abstract fun onCreateButtonClicked()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
        observeViewModel()

        (activity as? MainActivity)?.hideBottomNav()
    }

    protected fun setupUI() {
        binding.newPlayListBack.text = getTitleText()
        binding.createPlaylistButton.text = getButtonText()
    }

    protected fun setupListeners() {
        var previousTitle = ""
        var previousDescription = ""

        binding.playlistTitle.addTextChangedListener {
            val currentText = it.toString()
            if (currentText != previousTitle) {
                viewModel.onTitleChanged(currentText)
                previousTitle = currentText
            }
        }

        binding.playlistDescription.addTextChangedListener {
            val currentText = it.toString()
            if (currentText != previousDescription) {
                viewModel.onDescriptionChanged(currentText)
                previousDescription = currentText
            }
        }

        binding.newPlaylistImage.setOnClickListener {
            if (hasGalleryPermission()) {
                pickCoverLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.no_photo_access), Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.createPlaylistButton.setOnClickListener {
            onCreateButtonClicked()
        }

        binding.newPlayListBack.setOnClickListener {
            onBackButtonClicked()
        }
    }

    protected fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.progressBar.isVisible = state.isLoading
                binding.contentContainer.isVisible = !state.isLoading

                binding.createPlaylistButton.isEnabled = state.isCreateEnabled && !state.isCreating
                binding.createPlaylistButton.text = if (state.isCreating) {
                    getString(R.string.saving)
                } else {
                    getButtonText()
                }

                if (!binding.playlistTitle.hasFocus()) {
                    binding.playlistTitle.setText(state.title)
                }

                if (!binding.playlistDescription.hasFocus()) {
                    binding.playlistDescription.setText(state.description)
                }

                loadCover(state)

                state.error?.let { error ->
                    MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Error))
                        .setMessage(error).setPositiveButton(getString(R.string.okay), null).show()
                }

                if (state.success) {
                    onSuccess(state.title)
                }
            }
        }
    }

    protected open fun onSuccess(title: String) {
        findNavController().navigateUp()
    }

    protected fun loadCover(state: BasePlaylistScreenState) {
        state.coverUri?.let { uri ->
            Glide.with(requireContext()).load(uri)
                .transform(CenterCrop(), RoundedCorners(Useful.dpToPx(8f, requireContext())))
                .into(binding.playlistCover)
            binding.addCoverIcon.visibility = View.GONE
            return
        }

        state.originalCoverUri?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    Glide.with(requireContext()).load(file).transform(
                        CenterCrop(), RoundedCorners(Useful.dpToPx(8f, requireContext()))
                    ).into(binding.playlistCover)
                    binding.addCoverIcon.visibility = View.GONE
                    return
                }
            } catch (e: Exception) {
            }
        }

        showPlaceholder()
    }

    protected fun showPlaceholder() {
        binding.addCoverIcon.visibility = View.VISIBLE
        Glide.with(requireContext()).clear(binding.playlistCover)
    }

    protected fun hasGalleryPermission(): Boolean {
        return (activity as? MainActivity)?.requestGalleryPermission() ?: false
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.hideBottomNav()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}