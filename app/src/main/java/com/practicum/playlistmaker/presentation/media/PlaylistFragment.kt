package com.practicum.playlistmaker.presentation.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    private lateinit var bottomSheetDialog: BottomSheetDialog

    companion object {
        private const val ARG_PLAYLIST_ID = "playlistId"

        fun newInstance(playlistId: Long) = PlaylistFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_PLAYLIST_ID, playlistId)
            }
        }
    }

    private fun setupPlaylistMenu() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_playlist_menu, null)
        bottomSheetDialog.setContentView(view)

        bottomSheetDialog.behavior.peekHeight = resources.displayMetrics.heightPixels / 2
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        setupMenuContent(view)

        setupMenuClickListeners(view)

        binding.playlistHamburgerButton.setOnClickListener {
            showPlaylistMenu()
        }
    }

    private fun setupMenuContent(view: View) {
        viewModel.state.value.playlist?.let { playlist ->
            view.findViewById<TextView>(R.id.playlistMenuTitle).text = playlist.title

            val trackCountText = resources.getQuantityString(
                R.plurals.tracks_count,
                playlist.trackCount,
                playlist.trackCount
            )
            view.findViewById<TextView>(R.id.playlistMenuTrackCount).text = trackCountText

            loadMenuCover(playlist.coverUri, view.findViewById(R.id.playlistMenuCover))
        }
    }

    private fun loadMenuCover(coverUri: String?, imageView: ImageView) {
        if (!coverUri.isNullOrEmpty()) {
            try {
                val file = File(coverUri)
                if (file.exists()) {
                    Glide.with(requireContext())
                        .load(file)
                        .centerCrop()
                        .into(imageView)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Glide.with(requireContext())
            .load(R.drawable.ic_no_artwork_image)
            .centerCrop()
            .into(imageView)
    }

    private fun setupMenuClickListeners(view: View) {
        view.findViewById<TextView>(R.id.menuShare).setOnClickListener {
            sharePlaylist()
            bottomSheetDialog.dismiss()
        }

        view.findViewById<TextView>(R.id.menuEdit).setOnClickListener {
            editPlaylist()
            bottomSheetDialog.dismiss()
        }

        view.findViewById<TextView>(R.id.menuDelete).setOnClickListener {
            deletePlaylist()
            bottomSheetDialog.dismiss()
        }
    }

    private fun showPlaylistMenu() {
        setupMenuContent(bottomSheetDialog.findViewById(android.R.id.content)!!)
        bottomSheetDialog.show()
    }

    private fun sharePlaylist() {
        // TODO: Реализовать функционал "Поделиться плейлистом"
        Toast.makeText(requireContext(), "Поделиться плейлистом", Toast.LENGTH_SHORT).show()
    }

    private fun editPlaylist() {
        // TODO: Реализовать редактирование плейлиста
        Toast.makeText(requireContext(), "Редактировать плейлист", Toast.LENGTH_SHORT).show()
    }

    private fun deletePlaylist() {
        Toast.makeText(requireContext(), "Удалить плейлист", Toast.LENGTH_SHORT).show()
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

        binding.overlay.isVisible = false
        binding.overlay.alpha = 0.6f

        binding.playlistBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.playlistShareButton.setOnClickListener {
            // TODO: реализовать в будущем
        }

        setupRecyclerView()
        setupBottomSheet()
        observeViewModel()
        setupPlaylistMenu()

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
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            peekHeight = (screenHeight * 0.35).toInt()
            maxHeight = screenHeight

            isHideable = true
            isDraggable = true
            isFitToContents = false
            expandedOffset = 0
            skipCollapsed = false

            state = BottomSheetBehavior.STATE_HIDDEN
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

        binding.dragHandle.setOnClickListener {
            val hasTracks = viewModel.state.value.tracks.isNotEmpty()
            if (hasTracks) {
                when (bottomSheetBehavior.state) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                    else -> {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }
        }
    }

    private fun toggleBottomSheet() {
        val hasTracks = viewModel.state.value.tracks.isNotEmpty()
        if (!hasTracks) return

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

                    updateBottomSheetVisibility(state.tracks)
                }
            }
        }
    }

    private fun updateBottomSheetVisibility(tracks: List<Track>) {
        val hasTracks = tracks.isNotEmpty()

        if (::bottomSheetBehavior.isInitialized) {
            if (hasTracks) {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    binding.overlay.isVisible = true
                    binding.overlay.alpha = 0.6f
                }
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                binding.overlay.isVisible = false
            }
        }

        binding.dragHandle.isVisible = hasTracks

        binding.dragHandle.isClickable = hasTracks
        binding.dragHandle.isEnabled = hasTracks
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
        binding.playlistCover.scaleType = ImageView.ScaleType.FIT_CENTER
        binding.playlistCover.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)

        Glide.with(requireContext()).load(R.drawable.ic_no_artwork_image)
            .into(binding.playlistCover)
    }

    override fun onResume() {
        super.onResume()

        if (::bottomSheetBehavior.isInitialized) {
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            val peekHeight = (screenHeight * 0.35).toInt()

            bottomSheetBehavior.peekHeight = peekHeight

            val hasTracks = viewModel.state.value.tracks.isNotEmpty()
            if (hasTracks) {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                binding.overlay.isVisible = true
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                binding.overlay.isVisible = false
            }
        }

        (activity as? MainActivity)?.showBottomNav()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}