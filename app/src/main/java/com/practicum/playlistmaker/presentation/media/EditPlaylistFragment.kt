package com.practicum.playlistmaker.presentation.media

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class EditPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditPlaylistViewModel by viewModel()

    private var playlistId: Long = -1

    private var isTitleManuallyChanged = false
    private var isDescriptionManuallyChanged = false

    private val pickCoverLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.onCoverSelected(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getLong("playlistId", -1)
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

        setupUI()
        setupListeners()
        observeViewModel()

        // Загружаем данные плейлиста для редактирования
        viewModel.loadPlaylist(playlistId)

        // Обработчик кнопки Back с проверкой изменений
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (viewModel.hasChanges()) {
                showDiscardChangesDialog()
            } else {
                findNavController().navigateUp()
            }
        }

        (activity as? MainActivity)?.hideBottomNav()
    }

    private fun setupUI() {
        binding.newPlayListBack.text = getString(R.string.edit_playlist)
        binding.createPlaylistButton.text = getString(R.string.save)
    }

    private fun setupListeners() {
        binding.playlistTitle.addTextChangedListener {
            isTitleManuallyChanged = true
            viewModel.onTitleChanged(it.toString())
        }

        binding.playlistDescription.addTextChangedListener {
            isDescriptionManuallyChanged = true
            viewModel.onDescriptionChanged(it.toString())
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
            viewModel.savePlaylist()
        }

        binding.newPlayListBack.setOnClickListener {
            if (viewModel.hasChanges()) {
                showDiscardChangesDialog()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.createPlaylistButton.isEnabled = state.isSaveEnabled

                // Заполняем поля данными плейлиста
                if (!isTitleManuallyChanged && state.title != binding.playlistTitle.text.toString()) {
                    binding.playlistTitle.setText(state.title)
                }

                if (!isDescriptionManuallyChanged && state.description != binding.playlistDescription.text.toString()) {
                    binding.playlistDescription.setText(state.description)
                }

                // Загружаем обложку
                loadCover(state)

                // Показываем ошибки
                state.error?.let { error ->
                    MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Error))
                        .setMessage(error).setPositiveButton(getString(R.string.okay), null).show()
                }

                // Если успешно сохранено, закрываем фрагмент
                if (state.success) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.playlist_updated, state.title),
                        Toast.LENGTH_SHORT
                    ).show()

                    parentFragmentManager.setFragmentResult(
                        "playlist_updated", Bundle().apply {
                            putBoolean("updated", true)
                            putLong("playlist_id", playlistId)
                        })

                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun loadCover(state: EditPlaylistScreenState) {
        // 1. Сначала проверяем новую выбранную обложку
        state.coverUri?.let { uri ->
            Glide.with(requireContext())
                .load(uri)
                .transform(CenterCrop(), RoundedCorners(Useful.dpToPx(8f, requireContext())))
                .into(binding.playlistCover)
            binding.addCoverIcon.visibility = View.GONE
            return
        }

        // 2. Если новой нет, проверяем существующую обложку плейлиста
        state.playlist?.coverUri?.let { path ->
            try {
                val file = File(path)
                if (file.exists()) {
                    Glide.with(requireContext())
                        .load(file)
                        .transform(CenterCrop(), RoundedCorners(Useful.dpToPx(8f, requireContext())))
                        .into(binding.playlistCover)
                    binding.addCoverIcon.visibility = View.GONE
                    return
                }
            } catch (e: Exception) {
                // Если ошибка - показываем плейсхолдер КАК ПРИ СОЗДАНИИ
            }
        }

        // 3. Если ничего нет - показываем плейсхолдер КАК ПРИ СОЗДАНИИ (без скругления!)
        showPlaceholder()
    }

    private fun showPlaceholder() {
        // ВАЖНО: НЕ ИСПОЛЬЗОВАТЬ Glide! Просто оставляем как в макете
        // Если в макете установлен src="@drawable/ic_no_artwork_image",
        // то просто показываем иконку добавления
        binding.addCoverIcon.visibility = View.VISIBLE

        // Очищаем Glide если что-то было загружено
        Glide.with(requireContext()).clear(binding.playlistCover)
    }
    private fun hasGalleryPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        return ContextCompat.checkSelfPermission(
            requireContext(), permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showDiscardChangesDialog() {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.MyDialogButton
        ).setTitle("Отменить изменения?").setMessage("Все внесённые изменения будут потеряны")
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton("Завершить") { _, _ ->
                findNavController().navigateUp()
            }.show()
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