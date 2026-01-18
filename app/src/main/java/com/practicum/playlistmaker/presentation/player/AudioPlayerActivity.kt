package com.practicum.playlistmaker.presentation.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.AudioPlayerBinding
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.util.Useful
import org.koin.androidx.viewmodel.ext.android.viewModel

class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var binding: AudioPlayerBinding
    private val viewModel: AudioPlayerViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.audBackButton.setOnClickListener { finish() }

        val track: Track? = intent.getParcelableExtra("track", Track::class.java)

        track?.let { viewModel.setTrack(it) } ?: run { finish() }

        viewModel.state.observe(this) { state ->
            state.track?.let { track ->
                binding.mainAlbumText.text = track.trackName ?: "-"
                binding.artistName.text = track.artistsName ?: "-"
                binding.audAlbumNameValue.text = track.collectionName ?: "-"
                binding.audYearValue.text = track.releaseYear ?: "-"
                binding.audGenreValue.text = track.primaryGenreName ?: "-"
                binding.audCountryValue.text = track.country ?: "-"

                Glide.with(this).load(track.getConvertArtwork())
                    .placeholder(R.drawable.ic_no_artwork_image)
                    .transform(RoundedCorners(Useful.dpToPx(8f, this))).into(binding.songPoster)
            }

            binding.currentTrackTime.text = formatTime(state.currentPosition)
            binding.audPlayButton.setImageResource(
                if (state.isPlaying) R.drawable.btn_aud_pause
                else R.drawable.btn_aud_play
            )
        }

        binding.audPlayButton.setOnClickListener {
            val isPlaying = viewModel.state.value?.isPlaying ?: false
            if (isPlaying) viewModel.pausePlayer()
            else viewModel.startPlayer()
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
        viewModel.pausePlayer()
    }
}