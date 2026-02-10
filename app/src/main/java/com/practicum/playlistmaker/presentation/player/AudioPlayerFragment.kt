package com.practicum.playlistmaker.presentation.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.main.MainActivity
import com.practicum.playlistmaker.presentation.util.Useful
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AudioPlayerViewModel by viewModel()

    private var track: Track? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var adapter: PlaylistBottomSheetAdapter

    private var savedBottomSheetState: Int? = null
    private var savedOverlayVisible: Boolean = false

    private var returningFromNewPlaylist: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            track = it.getParcelable(ARG_TRACK)
        }

        savedInstanceState?.let {
            savedBottomSheetState =
                it.getInt(KEY_BOTTOM_SHEET_STATE, BottomSheetBehavior.STATE_HIDDEN)
            savedOverlayVisible = it.getBoolean(KEY_OVERLAY_VISIBLE, false)
            returningFromNewPlaylist = it.getBoolean(KEY_RETURNING_FROM_NEW_PLAYLIST, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioPlayerBinding.inflate(inflater, container, false)
        binding.audBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? MainActivity)?.hideBottomNav()

        binding.audBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        if (track == null) {
            track = arguments?.getParcelable(ARG_TRACK)
            if (track == null && savedInstanceState != null) {
                track = savedInstanceState.getParcelable(ARG_TRACK)
            }
            if (track == null) {
                binding.root.post {
                    findNavController().popBackStack()
                }
                return
            }
        }

        viewModel.setTrack(track!!)
        observeViewModel()
        setupPlayerButtons()
        setupFavoriteButton()
        setupBottomSheet()
        restoreBottomSheetState()
    }

    private fun restoreBottomSheetState() {
        if (returningFromNewPlaylist) {
            savedBottomSheetState = BottomSheetBehavior.STATE_HIDDEN
            savedOverlayVisible = false
            returningFromNewPlaylist = false

            binding.root.post {
                binding.overlay.isVisible = false
            }
        }

        savedBottomSheetState?.let { savedState ->
            if (isValidBottomSheetState(savedState)) {
                binding.root.post {
                    if (::bottomSheetBehavior.isInitialized && bottomSheetBehavior.state != savedState) {
                        try {
                            bottomSheetBehavior.state = savedState
                        } catch (e: Exception) {
                            Log.e("AudioPlayer", "Error restoring bottom sheet state", e)
                        }
                    }
                }
            } else {
                savedBottomSheetState = BottomSheetBehavior.STATE_HIDDEN
                binding.root.post {
                    if (::bottomSheetBehavior.isInitialized) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                    binding.overlay.isVisible = false
                }
            }
        }

        if (savedOverlayVisible && savedBottomSheetState != BottomSheetBehavior.STATE_HIDDEN) {
            binding.root.post {
                binding.overlay.isVisible = true
                binding.overlay.alpha = 0.6f
            }
        }
    }


    private fun isValidBottomSheetState(state: Int): Boolean {
        return state == BottomSheetBehavior.STATE_HIDDEN || state == BottomSheetBehavior.STATE_COLLAPSED || state == BottomSheetBehavior.STATE_EXPANDED || state == BottomSheetBehavior.STATE_HALF_EXPANDED || state == BottomSheetBehavior.STATE_DRAGGING
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        track?.let {
            outState.putParcelable(ARG_TRACK, it)
        }
        viewModel.savePlaybackState()

        val stateToSave = if (::bottomSheetBehavior.isInitialized) {
            val currentState = bottomSheetBehavior.state
            if (isValidBottomSheetState(currentState) && currentState != BottomSheetBehavior.STATE_SETTLING) {
                currentState
            } else {
                savedBottomSheetState ?: BottomSheetBehavior.STATE_HIDDEN
            }
        } else {
            savedBottomSheetState ?: BottomSheetBehavior.STATE_HIDDEN
        }

        outState.putInt(KEY_BOTTOM_SHEET_STATE, stateToSave)
        outState.putBoolean(KEY_OVERLAY_VISIBLE, binding.overlay.isVisible)
        outState.putBoolean(KEY_RETURNING_FROM_NEW_PLAYLIST, returningFromNewPlaylist)
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            state.track?.let { track ->
                binding.mainAlbumText.text = track.trackName ?: "-"
                binding.artistName.text = track.artistsName ?: "-"
                binding.audAlbumNameValue.text = track.collectionName ?: "-"
                binding.audYearValue.text = track.releaseYear ?: "-"
                binding.audGenreValue.text = track.primaryGenreName ?: "-"
                binding.audCountryValue.text = track.country ?: "-"

                Glide.with(requireContext()).load(track.getConvertArtwork())
                    .placeholder(R.drawable.ic_no_artwork_image)
                    .transform(RoundedCorners(Useful.dpToPx(8f, requireContext())))
                    .into(binding.songPoster)
            }

            binding.currentTrackTime.text = formatTime(state.currentPosition)
            binding.audPlayButton.setImageResource(
                if (state.isPlaying) R.drawable.btn_aud_pause
                else R.drawable.btn_aud_play
            )

            binding.audFavoriteButton.setImageResource(
                if (state.isFavorite) R.drawable.btn_aud_like_true
                else R.drawable.btn_aud_like_false
            )
        }

        viewModel.addTrackStatus.observe(viewLifecycleOwner) { status ->
            status?.let {
                when (it) {
                    is AddTrackStatus.Success -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.added_to_playlist, it.playlistName),
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetAddTrackStatus()
                    }

                    is AddTrackStatus.AlreadyExists -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.allready_exists_in_playlist, it.playlistName),
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetAddTrackStatus()
                    }

                    is AddTrackStatus.Error -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error, it.message),
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetAddTrackStatus()
                    }
                }
            }
        }

        viewModel.shouldCloseBottomSheet.observe(viewLifecycleOwner) { shouldClose ->
            if (shouldClose == true) {
                hideBottomSheet()
                viewModel.resetShouldCloseBottomSheet()
            }
        }
    }

    private fun setupPlayerButtons() {
        binding.audPlayButton.setOnClickListener {
            val isPlaying = viewModel.state.value?.isPlaying ?: false
            if (isPlaying) viewModel.pausePlayer()
            else viewModel.startPlayer()
        }
    }

    private fun setupFavoriteButton() {
        binding.audFavoriteButton.setOnClickListener {
            viewModel.toggleFavorite()
        }
    }

    private fun formatTime(ms: Int): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onPause() {
        super.onPause()
        if (!requireActivity().isChangingConfigurations) {
            viewModel.state.value?.isPlaying?.let { isPlaying ->
                if (isPlaying) {
                    viewModel.pausePlayer()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!requireActivity().isChangingConfigurations) {
            viewModel.savePlaybackState()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        returningFromNewPlaylist = false
    }

    private fun setupBottomSheet() {
        val bottomSheetContainer = binding.bottomSheet
        val overlay = binding.overlay
        val recyclerView = binding.playlistRecyclerView
        val dragHandle = binding.dragHandle

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PlaylistBottomSheetAdapter(mutableListOf()) { playlist ->
            track?.let { viewModel.addTrackToPlaylist(playlist, it) }
        }
        recyclerView.adapter = adapter

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            adapter.update(playlists)
        }

        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val peekHeightPx = (screenHeight * 0.40).toInt()



        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            peekHeight = peekHeightPx
            isHideable = true
            isDraggable = true
            isFitToContents = false
            halfExpandedRatio = 0.40f
            expandedOffset = 0
            skipCollapsed = false
        }

        hideBottomSheet()

        binding.audAddToPlaylist.setOnClickListener {
            toggleBottomSheet()
        }

        overlay.setOnClickListener {
            hideBottomSheet()
        }

        dragHandle.setOnClickListener {
            when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }

                BottomSheetBehavior.STATE_EXPANDED -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                else -> {
                    showBottomSheet()
                }
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState != BottomSheetBehavior.STATE_SETTLING) {
                    savedBottomSheetState = newState
                }

                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.isVisible = false
                        savedOverlayVisible = false
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.overlay.isVisible = true
                        savedOverlayVisible = true
                        binding.overlay.alpha = 0.6f
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.overlay.isVisible = true
                        savedOverlayVisible = true
                        binding.overlay.alpha = 0.6f
                    }

                    else -> {
                        savedOverlayVisible = binding.overlay.isVisible
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                when {
                    slideOffset <= 0 -> {
                        val progress = (slideOffset + 1).coerceIn(0f, 1f)
                        if (progress > 0) {
                            overlay.visibility = View.VISIBLE
                            overlay.alpha = progress * 0.6f
                        } else {
                            overlay.visibility = View.GONE
                        }
                    }

                    else -> {
                        overlay.visibility = View.VISIBLE
                        overlay.alpha = 0.6f
                    }
                }
            }
        })

        binding.createNewPlaylistButtonSheet.setOnClickListener {
            returningFromNewPlaylist = true
            hideBottomSheet()
            findNavController().navigate(
                R.id.action_audioPlayerFragment_to_newPlaylistFragment
            )
        }

    }

    private fun toggleBottomSheet() {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_HIDDEN -> {
                showBottomSheet()
            }

            BottomSheetBehavior.STATE_COLLAPSED, BottomSheetBehavior.STATE_EXPANDED -> {
                hideBottomSheet()
            }

            else -> {
                hideBottomSheet()
            }
        }
    }

    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    companion object {
        private const val ARG_TRACK = "track"
        private const val KEY_BOTTOM_SHEET_STATE = "bottom_sheet_state"
        private const val KEY_OVERLAY_VISIBLE = "overlay_visible"
        private const val KEY_RETURNING_FROM_NEW_PLAYLIST = "returning_from_new_playlist"

        @JvmStatic
        fun newInstance(track: Track) = AudioPlayerFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_TRACK, track) }
        }
    }
}