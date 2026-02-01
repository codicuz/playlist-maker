package com.practicum.playlistmaker.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.audBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
        if (track == null) {
            binding.root.post {
                findNavController().popBackStack()
            }
            return
        }

        viewModel.setTrack(track!!)
        observeViewModel()
        setupPlayerButtons()
        setupFavoriteButton()
        setupBottomSheet()
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

    override fun onStop() {
        super.onStop()
        if (!requireActivity().isChangingConfigurations) {
            viewModel.pausePlayer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as MainActivity).showBottomNav()
        _binding = null
    }

    private fun setupBottomSheet() {
        val bottomSheetContainer = binding.root.findViewById<FrameLayout>(R.id.bottomSheetContainer)
        val overlay = binding.root.findViewById<View>(R.id.overlay)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        overlay.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.audAddToPlaylist.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                overlay.visibility =
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val alpha = ((slideOffset + 1) / 2 * 0.6f).coerceIn(0f, 0.6f)
                overlay.alpha = alpha
            }
        })
    }

    companion object {
        private const val ARG_TRACK = "track"

        @JvmStatic
        fun newInstance(track: Track) = AudioPlayerFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_TRACK, track) }
        }
    }
}
