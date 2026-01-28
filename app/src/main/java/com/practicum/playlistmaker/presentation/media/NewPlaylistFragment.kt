package com.practicum.playlistmaker.presentation.media

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.practicum.playlistmaker.presentation.util.Useful

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    private var selectedCoverUri: Uri? = null

    private val pickCoverLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

            if (uri != null) {

                selectedCoverUri = uri

                Glide.with(requireContext()).load(uri).transform(
                    CenterCrop(), RoundedCorners(Useful.dpToPx(8f, requireContext()))
                ).into(binding.playlistCover)

                binding.addCoverIcon.visibility = View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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

        setupCoverPicker()

        binding.playlistTitle.addTextChangedListener {
            updateCreateButtonState()
        }

        binding.createPlaylistButton.setOnClickListener {
            Log.d("Curr", "Trololo")
        }

        binding.newPlayList.setOnClickListener {
            handleBackPress()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleBackPress()
        }
        updateCreateButtonState()
    }

    private fun handleBackPress() {
        val titleNotEmpty = binding.playlistTitle.text.toString().isNotBlank()
        val descriptionNotEmpty = binding.playlistDescription.text.toString().isNotBlank()
        val imageSelected = selectedCoverUri != null

        if (titleNotEmpty || descriptionNotEmpty || imageSelected) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Завершить создание плейлиста?")
                .setMessage("Все несохраненные данные будут потеряны")
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Завершить") { _, _ ->
                    findNavController().navigateUp()
                }
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun updateCreateButtonState() {
        val title = binding.playlistTitle.text.toString()

        val isEnabled = title.isNotBlank()

        binding.createPlaylistButton.isEnabled = isEnabled
    }

    private fun setupCoverPicker() {
        binding.newPlaylistImage.setOnClickListener {
            Log.d("CURR", "setupCoverPicker")

            pickCoverLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = NewPlaylistFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}