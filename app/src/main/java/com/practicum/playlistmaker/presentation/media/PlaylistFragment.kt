package com.practicum.playlistmaker.presentation.media

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.adapter.TrackAdapter
import com.practicum.playlistmaker.presentation.util.CoverLoader
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

    private var savedBottomSheetState: Int? = null

    companion object {
        private const val ARG_PLAYLIST_ID = "playlistId"
        private const val KEY_BOTTOM_SHEET_STATE = "bottom_sheet_state"

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

        savedInstanceState?.let {
            savedBottomSheetState = it.getInt(KEY_BOTTOM_SHEET_STATE, BottomSheetBehavior.STATE_COLLAPSED)
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

        binding.overlay.alpha = 0.6f

        binding.playlistBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.playlistShareButton.setOnClickListener {
            sharePlaylist()
        }

        setupRecyclerView()
        setupBottomSheet()
        observeViewModel()
        setupPlaylistMenu()

        parentFragmentManager.setFragmentResultListener(
            "playlist_updated", viewLifecycleOwner
        ) { _, bundle ->
            val updated = bundle.getBoolean("updated", false)
            val playlistId = bundle.getLong("playlist_id", -1)

            if (updated && playlistId == this.playlistId) {
                viewModel.loadPlaylist(playlistId)
            }
        }

        if (playlistId != -1L) {
            viewModel.loadPlaylist(playlistId)
        }
    }

    private fun setupPlaylistMenu() {
        bottomSheetDialog = BottomSheetDialog(
            requireContext(), R.style.BottomSheetMenuTheme
        )

        val view = layoutInflater.inflate(R.layout.bottom_sheet_playlist_menu, null)
        bottomSheetDialog.setContentView(view)

        view.post {
            val screenHeight = resources.displayMetrics.heightPixels
            val desiredHeight = (screenHeight * 0.53).toInt()

            val parent = view.parent as ViewGroup
            parent.layoutParams = parent.layoutParams.apply {
                height = desiredHeight
            }
        }

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
                R.plurals.tracks_count, playlist.trackCount, playlist.trackCount
            )
            view.findViewById<TextView>(R.id.playlistMenuTrackCount).text = trackCountText

            CoverLoader.loadMenuCover(playlist.coverUri, view.findViewById(R.id.playlistMenuCover))
        }
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

    private fun buildShareText(playlist: Playlist?, tracks: List<Track>): String {
        val builder = StringBuilder()

        playlist?.title?.let { title ->
            builder.append(title).append("\n")
        }

        playlist?.description?.takeIf { it.isNotBlank() }?.let { description ->
            builder.append(description).append("\n")
        }

        val trackCount = tracks.size
        val tracksText = resources.getQuantityString(
            R.plurals.tracks_count, trackCount, trackCount
        )
        builder.append("$tracksText\n\n")

        tracks.forEachIndexed { index, track ->
            val trackNumber = index + 1
            val artist = track.artistsName ?: getString(R.string.unknown_artist)
            val trackName = track.trackName ?: getString(R.string.unknown_track_name)
            val duration = track.trackTime ?: getString(R.string.unknown_track_time)

            builder.append("$trackNumber. $artist - $trackName ($duration)\n")
        }

        return builder.toString()
    }

    private fun sharePlaylist() {
        val tracks = viewModel.state.value.tracks
        val playlist = viewModel.state.value.playlist

        if (tracks.isEmpty()) {
            Toast.makeText(
                requireContext(), getString(R.string.no_shareable_playlist), Toast.LENGTH_SHORT
            ).show()
            return
        }

        val shareText = buildShareText(playlist, tracks)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        try {
            startActivity(Intent.createChooser(intent, null))
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(), getString(R.string.no_intent_handle), Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun editPlaylist() {
        viewModel.state.value.playlist?.let { playlist ->
            val bundle = Bundle().apply {
                putLong("playlistId", playlist.id)
            }
            findNavController().navigate(
                R.id.action_playlistFragment_to_editPlaylistFragment,
                bundle
            )
        }
    }

    private fun deletePlaylist() {
        val playlistName =
            viewModel.state.value.playlist?.title ?: getString(R.string.unknown_playlist)

        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.MyDialogButton
        ).setTitle(getString(R.string.delete_playlist_title, playlistName))
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deletePlaylist()
                }
                dialog.dismiss()
            }.show()
    }

    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter(onTrackClick = { track ->
            navigateToPlayer(track)
        }, onTrackLongClick = { track ->
            showDeleteDialog(track)
        })

        binding.playlistRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
    }

    private fun navigateToPlayer(track: Track) {
        val bundle = Bundle().apply { putParcelable("track", track) }
        findNavController().navigate(
            R.id.action_playlistFragment_to_audioPlayerFragment, bundle
        )
    }

    private fun showDeleteDialog(track: Track) {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.MyDialogButton
        ).setTitle(getString(R.string.delete_track_title))
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                viewModel.deleteTrackFromPlaylist(track)
                dialog.dismiss()
            }.show()
    }

    private fun setupBottomSheet() {
        val bottomSheetContainer = binding.bottomSheet
        val screenHeight = resources.displayMetrics.heightPixels

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            isFitToContents = false
            expandedOffset = 0
            skipCollapsed = false
            peekHeight = (screenHeight * 0.30).toInt()
            halfExpandedRatio = 0.7f
            isHideable = false
            isDraggable = true

            savedBottomSheetState?.let { state ->
                this.state = state
            }
        }

        binding.overlay.isVisible = bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED
        binding.overlay.alpha = 0.6f

        setupBottomSheetListeners()
    }

    private fun setupBottomSheetListeners() {
        binding.overlay.setOnClickListener(null)
        binding.overlay.setOnTouchListener(null)
        binding.overlay.isClickable = false
        binding.overlay.isFocusable = false
        binding.overlay.isFocusableInTouchMode = false

        binding.dragHandle.setOnClickListener {
            val currentHasTracks = viewModel.state.value.tracks.isNotEmpty()
            if (currentHasTracks) {
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

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheet.post {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                    return
                }

                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.overlay.isVisible = true
                        binding.overlay.alpha = 0.6f
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.overlay.isVisible = true
                        binding.overlay.alpha = 0.6f
                    }
                    else -> {
                        binding.overlay.isVisible = true
                        binding.overlay.alpha = 0.6f
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.isVisible = true
                binding.overlay.alpha = 0.6f
            }
        })
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deletionEvent.collectLatest { event ->
                when (event) {
                    is PlaylistViewModel.DeletionEvent.Success -> {
                        parentFragmentManager.setFragmentResult(
                            "playlist_deleted", Bundle().apply {
                                putBoolean("deleted", true)
                                putLong("playlist_id", playlistId)
                            })
                        findNavController().popBackStack()
                        viewModel.resetDeletionEvent()
                    }
                    is PlaylistViewModel.DeletionEvent.Error -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_deleteing, event.message), Toast.LENGTH_LONG
                        ).show()
                        viewModel.resetDeletionEvent()
                    }
                    null -> {
                    }
                }
            }
        }
    }

    private fun updateBottomSheetVisibility(tracks: List<Track>) {
        val hasTracks = tracks.isNotEmpty()

        if (hasTracks) {
            binding.playlistRecyclerView.isVisible = true
            binding.emptyStateTextView.isVisible = false
            binding.dragHandle.isVisible = true
            bottomSheetBehavior.isDraggable = true
        } else {
            binding.playlistRecyclerView.isVisible = false
            binding.emptyStateTextView.isVisible = true
            binding.dragHandle.isVisible = true
            bottomSheetBehavior.isDraggable = false
        }

        binding.bottomSheet.isVisible = true
        binding.overlay.isVisible = true
        binding.overlay.alpha = 0.6f
    }

    private fun loadPlaylistCover(coverUri: String?) {
        CoverLoader.loadPlaylistCover(coverUri, binding.playlistCover)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::bottomSheetBehavior.isInitialized) {
            outState.putInt(KEY_BOTTOM_SHEET_STATE, bottomSheetBehavior.state)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}