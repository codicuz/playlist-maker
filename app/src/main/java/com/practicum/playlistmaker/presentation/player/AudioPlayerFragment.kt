package com.practicum.playlistmaker.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            track = it.getParcelable(ARG_TRACK)
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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        track?.let {
            outState.putParcelable(ARG_TRACK, it)
        }
        viewModel.savePlaybackState()
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
                            "Добавлено в плейлист ${it.playlistName}",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetAddTrackStatus()
                    }

                    is AddTrackStatus.AlreadyExists -> {
                        Toast.makeText(
                            requireContext(),
                            "Трек уже добавлен в плейлист ${it.playlistName}",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetAddTrackStatus()
                    }

                    is AddTrackStatus.Error -> {
                        Toast.makeText(
                            requireContext(), "Ошибка: ${it.message}", Toast.LENGTH_SHORT
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
        if (!requireActivity().isChangingConfigurations) {
            (requireActivity() as? MainActivity)?.showBottomNav()
        }
        _binding = null
    }

    private fun setupBottomSheet() {
        val bottomSheetContainer = binding.bottomSheet
        val overlay = binding.overlay
        val recyclerView =
            bottomSheetContainer.findViewById<RecyclerView>(R.id.playlistRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PlaylistBottomSheetAdapter(mutableListOf()) { playlist ->
            track?.let { viewModel.addTrackToPlaylist(playlist, it) }
        }
        recyclerView.adapter = adapter

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            adapter.update(playlists)
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            peekHeight = 0
            isHideable = true
            isDraggable = true
        }

        binding.audAddToPlaylist.setOnClickListener {
            toggleBottomSheet()
        }

        overlay.setOnClickListener {
            hideBottomSheet()
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        overlay.visibility = View.GONE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val normalizedOffset = when {
                    slideOffset >= 0 -> slideOffset
                    else -> slideOffset + 1
                }.coerceIn(0f, 1f)

                if (normalizedOffset > 0) {
                    overlay.visibility = View.VISIBLE
                    overlay.alpha = normalizedOffset * 0.6f
                } else {
                    overlay.alpha = 0f
                    overlay.visibility = View.GONE
                }
            }
        })

        bottomSheetContainer.findViewById<Button>(R.id.createNewPlaylistButtonSheet)
            .setOnClickListener {
                hideBottomSheet()
                findNavController().navigate(
                    R.id.action_audioPlayerFragment_to_newPlaylistFragment
                )
            }
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            hideBottomSheet()
        } else {
            showBottomSheet()
        }
    }

    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    companion object {
        private const val ARG_TRACK = "track"

        @JvmStatic
        fun newInstance(track: Track) = AudioPlayerFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_TRACK, track) }
        }
    }
}