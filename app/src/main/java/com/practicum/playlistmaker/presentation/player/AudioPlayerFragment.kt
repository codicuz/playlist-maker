package com.practicum.playlistmaker.presentation.player

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
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

    private val REQUEST_NOTIFICATION_PERMISSION = 1002

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AudioPlayerViewModel by viewModel()

    private var track: Track? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var adapter: PlaylistBottomSheetAdapter

    private var savedBottomSheetState: Int? = null
    private var savedOverlayVisible: Boolean = false
    private var returningFromNewPlaylist: Boolean = false
    private var isExplicitBackPress = false

    companion object {
        private const val ARG_TRACK = "track"
        private const val KEY_BOTTOM_SHEET_STATE = "bottom_sheet_state"
        private const val KEY_OVERLAY_VISIBLE = "overlay_visible"
        private const val KEY_RETURNING_FROM_NEW_PLAYLIST = "returning_from_new_playlist"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable(ARG_TRACK, Track::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelable(ARG_TRACK)
            }
        }

        savedInstanceState?.let {
            savedBottomSheetState = it.getInt(KEY_BOTTOM_SHEET_STATE, BottomSheetBehavior.STATE_HIDDEN)
            savedOverlayVisible = it.getBoolean(KEY_OVERLAY_VISIBLE, false)
            returningFromNewPlaylist = it.getBoolean(KEY_RETURNING_FROM_NEW_PLAYLIST, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? MainActivity)?.hideBottomNav()

        setupClickListeners()

        if (track == null) {
            findNavController().popBackStack()
            return
        }

        viewModel.bindService(requireContext())
        viewModel.setTrack(track!!)

        observeViewModel()
        setupPlayerButtons()
        setupFavoriteButton()
        setupBottomSheet()
        restoreBottomSheetState()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionIfNeeded()
        }
    }

    private fun setupClickListeners() {
        binding.audBackButton.setOnClickListener {
            isExplicitBackPress = true
            handleBackPress()
        }
    }

    private fun handleBackPress() {
        findNavController().popBackStack()
    }

    private fun restoreBottomSheetState() {
        if (returningFromNewPlaylist) {
            savedBottomSheetState = BottomSheetBehavior.STATE_HIDDEN
            savedOverlayVisible = false
            returningFromNewPlaylist = false
            binding.overlay.isVisible = false
        }

        savedBottomSheetState?.let { savedState ->
            if (isValidBottomSheetState(savedState) && ::bottomSheetBehavior.isInitialized) {
                try {
                    bottomSheetBehavior.state = savedState
                } catch (e: Exception) {
                    Log.e("AudioPlayerFragment", "Error: $e")
                }
            }
        }

        if (savedOverlayVisible && savedBottomSheetState != BottomSheetBehavior.STATE_HIDDEN) {
            binding.overlay.isVisible = true
            binding.overlay.alpha = 0.6f
        }
    }

    private fun isValidBottomSheetState(state: Int): Boolean {
        return state == BottomSheetBehavior.STATE_HIDDEN ||
                state == BottomSheetBehavior.STATE_COLLAPSED ||
                state == BottomSheetBehavior.STATE_EXPANDED ||
                state == BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        track?.let {
            outState.putParcelable(ARG_TRACK, it)
        }

        if (::bottomSheetBehavior.isInitialized) {
            outState.putInt(KEY_BOTTOM_SHEET_STATE, bottomSheetBehavior.state)
            outState.putBoolean(KEY_OVERLAY_VISIBLE, binding.overlay.isVisible)
        }
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

                Glide.with(requireContext())
                    .load(track.getConvertArtwork())
                    .placeholder(R.drawable.ic_no_artwork_image)
                    .transform(RoundedCorners(Useful.dpToPx(8f, requireContext())))
                    .into(binding.songPoster)
            }

            binding.currentTrackTime.text = formatTime(state.currentPosition)
            binding.playbackButton.setPlaying(state.isPlaying)

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

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            adapter.update(playlists)
        }
    }

    private fun setupPlayerButtons() {
        binding.playbackButton.setOnClickListener {
            val isPlaying = viewModel.state.value?.isPlaying ?: false
            if (isPlaying) {
                viewModel.pausePlayer()
            } else {
                if (!viewModel.isServiceReady()) {
                    viewModel.bindService(requireContext())
                }
                viewModel.startPlayer()
            }
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
        if (!requireActivity().isChangingConfigurations && !isExplicitBackPress) {
            viewModel.onAppBackgrounded()
            checkNotificationPermissionAndStartForeground()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun checkNotificationPermissionAndStartForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                viewModel.startForegroundMode()
            }
        } else {
            viewModel.startForegroundMode()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        returningFromNewPlaylist = false
        isExplicitBackPress = false
        if (!requireActivity().isChangingConfigurations) {
            viewModel.onAppForegrounded()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (!requireActivity().isChangingConfigurations) {
            if (isExplicitBackPress) {
                viewModel.stopAndUnbindService(requireContext())
            } else {
                viewModel.unbindService(requireContext())
            }
        } else {
            Log.d("AudioPlayerFragment", "Configuration change, keeping service bound")
        }
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!requireActivity().isChangingConfigurations) {
            viewModel.cleanup()
        }
    }

    private fun setupBottomSheet() {
        val bottomSheetContainer = binding.bottomSheet
        val recyclerView = binding.playlistRecyclerView

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PlaylistBottomSheetAdapter(mutableListOf()) { playlist ->
            track?.let { viewModel.addTrackToPlaylist(playlist, it) }
        }
        recyclerView.adapter = adapter

        val screenHeight = resources.displayMetrics.heightPixels
        val peekHeightPx = (screenHeight * 0.40).toInt()

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            peekHeight = peekHeightPx
            isHideable = true
            isDraggable = true
            isFitToContents = false
            halfExpandedRatio = 0.40f
        }

        hideBottomSheet()

        binding.audAddToPlaylist.setOnClickListener {
            toggleBottomSheet()
        }

        binding.overlay.setOnClickListener {
            hideBottomSheet()
        }

        binding.dragHandle.setOnClickListener {
            when (bottomSheetBehavior.state) {
                BottomSheetBehavior.STATE_COLLAPSED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                else -> showBottomSheet()
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                binding.overlay.isVisible = newState != BottomSheetBehavior.STATE_HIDDEN
                if (newState != BottomSheetBehavior.STATE_HIDDEN) {
                    binding.overlay.alpha = 0.6f
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.isVisible = true
                binding.overlay.alpha = 0.6f
            }
        })

        binding.createNewPlaylistButtonSheet.setOnClickListener {
            returningFromNewPlaylist = true
            hideBottomSheet()
            findNavController().navigate(R.id.action_audioPlayerFragment_to_newPlaylistFragment)
        }
    }

    private fun toggleBottomSheet() {
        when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_HIDDEN -> showBottomSheet()
            else -> hideBottomSheet()
        }
    }

    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
}