package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistBinding
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.main.MainActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private var playlistId: Long = -1
    private val viewModel: PlaylistViewModel by viewModel()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var trackAdapter: TrackAdapter

    companion object {
        private const val ARG_PLAYLIST_ID = "playlistId"

        fun newInstance(playlistId: Long) = PlaylistFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_PLAYLIST_ID, playlistId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getLong(ARG_PLAYLIST_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.overlay.isVisible = true
        binding.overlay.alpha = 0.6f

        binding.playlistBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.playlistShareButton.setOnClickListener {
            // TODO: реализовать в будущем
        }

        binding.playlistHamburgerButton.setOnClickListener {
            // TODO:
        }

        setupRecyclerView()
        setupBottomSheet()
        observeViewModel()

        if (playlistId != -1L) {
            viewModel.loadPlaylist(playlistId)
        }
    }

    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter(
            onTrackClick = { track ->
                navigateToPlayer(track)
            },
            onTrackLongClick = { track ->
                showDeleteDialog(track)
            }
        )

        binding.playlistRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
    }

    private fun navigateToPlayer(track: Track) {
        (requireActivity() as? MainActivity)?.hideBottomNav()
        val bundle = Bundle().apply { putParcelable("track", track) }
        findNavController().navigate(
            R.id.action_playlistFragment_to_audioPlayerFragment,
            bundle
        )
    }

    private fun showDeleteDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_track_title))
            .setMessage(getString(R.string.delete_track_message))
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                viewModel.deleteTrackFromPlaylist(track)
                dialog.dismiss()
            }
            .show()
    }

    private fun setupBottomSheet() {
        val bottomSheetContainer = binding.bottomSheet

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            isHideable = false
            isDraggable = true

            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            peekHeight = (screenHeight * 0.4).toInt()
        }

        binding.dragHandle.setOnClickListener {
            toggleBottomSheet()
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.isVisible = false
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.overlay.isVisible = true
                        binding.overlay.alpha = 0.6f
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.overlay.isVisible = true
                        binding.overlay.alpha = 0.6f
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                when {
                    slideOffset <= 0 -> {
                        val progress = (slideOffset + 1).coerceIn(0f, 1f)
                        if (progress > 0) {
                            binding.overlay.visibility = View.VISIBLE
                            binding.overlay.alpha = progress * 0.6f
                        } else {
                            binding.overlay.visibility = View.GONE
                        }
                    }

                    else -> {
                        binding.overlay.visibility = View.VISIBLE
                        binding.overlay.alpha = 0.6f
                    }
                }
            }
        })
    }

    private fun toggleBottomSheet() {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_HIDDEN -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                binding.overlay.isVisible = true
                binding.overlay.alpha = 0.6f
            }

            BottomSheetBehavior.STATE_COLLAPSED -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }

            BottomSheetBehavior.STATE_EXPANDED -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.progressBar.isVisible = state.isLoading
                binding.contentContainer.isVisible = !state.isLoading

                state.playlist?.let { playlist ->
                    binding.playlistTitle.text = playlist.title

                    if (!playlist.description.isNullOrBlank()) {
                        binding.playlistDescription.text = playlist.description
                        binding.playlistDescription.visibility = View.VISIBLE
                    } else {
                        binding.playlistDescription.visibility = View.GONE
                    }

                    loadPlaylistCover(playlist.coverUri)

                    binding.playlistTracksCount.text = resources.getQuantityString(
                        R.plurals.tracks_count, state.trackCount, state.trackCount
                    )

                    val minutesText = if (state.totalDurationMinutes == 0L) {
                        getString(R.string.zero_minutes)
                    } else {
                        resources.getQuantityString(
                            R.plurals.tracks_minutes,
                            state.totalDurationMinutes.toInt(),
                            state.totalDurationMinutes
                        )
                    }
                    binding.playlistMinutesCount.text = minutesText

                    trackAdapter.submitList(state.tracks)
                }
            }
        }
    }

    private fun loadPlaylistCover(coverUri: String?) {
        if (!coverUri.isNullOrEmpty()) {
            try {
                val file = File(coverUri)
                if (file.exists()) {
                    binding.playlistCover.scaleType = ImageView.ScaleType.CENTER_CROP
                    binding.playlistCover.setPadding(0, 0, 0, 0)

                    Glide.with(requireContext()).load(file).into(binding.playlistCover)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        loadDefaultCover()
    }

    private fun loadDefaultCover() {
        val paddingPx = (60 * resources.displayMetrics.density).toInt()
        binding.playlistCover.scaleType = ImageView.ScaleType.CENTER_INSIDE
        binding.playlistCover.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

        Glide.with(requireContext()).load(R.drawable.ic_no_artwork_image)
            .into(binding.playlistCover)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}