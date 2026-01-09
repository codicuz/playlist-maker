package com.practicum.playlistmaker.ui.player

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.player.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.util.Useful

class AudioPlayerActivity : AppCompatActivity() {

    private val viewModel: AudioPlayerViewModel by viewModels()

    private lateinit var playButton: ImageButton
    private lateinit var currentTrackTime: TextView
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var albumNameTextView: TextView
    private lateinit var releaseYearTextView: TextView
    private lateinit var genreTextView: TextView
    private lateinit var countryTextView: TextView
    private lateinit var artworkImageView: ImageView
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.audio_player)

        playButton = findViewById(R.id.audPlayButton)
        currentTrackTime = findViewById(R.id.currentTrackTime)
        trackNameTextView = findViewById(R.id.mainAlbumText)
        artistNameTextView = findViewById(R.id.artistName)
        albumNameTextView = findViewById(R.id.audAlbumNameValue)
        releaseYearTextView = findViewById(R.id.audYearValue)
        genreTextView = findViewById(R.id.audGenreValue)
        countryTextView = findViewById(R.id.audCountryValue)
        artworkImageView = findViewById(R.id.songPoster)
        backButton = findViewById(R.id.audBackButton)

        backButton.setOnClickListener { finish() }

        val track: Track? =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("track", Track::class.java)
            } else {
                @Suppress("DEPRECATION") intent.getParcelableExtra("track")
            }

        track?.let { viewModel.setTrack(it) } ?: run { finish() }

        viewModel.trackLiveData.observe(this) { track ->
            trackNameTextView.text = track.trackName ?: "-"
            artistNameTextView.text = track.artistsName ?: "-"
            albumNameTextView.text = track.collectionName ?: "-"
            releaseYearTextView.text = track.releaseYear ?: "-"
            genreTextView.text = track.primaryGenreName ?: "-"
            countryTextView.text = track.country ?: "-"

            Glide.with(this).load(track.getConvertArtwork())
                .placeholder(R.drawable.ic_no_artwork_image)
                .transform(RoundedCorners(Useful.dpToPx(8f, this))).into(artworkImageView)

            currentTrackTime.text = formatTime(0)
        }

        viewModel.currentPosition.observe(this) { pos ->
            currentTrackTime.text = formatTime(pos)
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            playButton.setImageResource(
                if (isPlaying) R.drawable.btn_aud_pause else R.drawable.btn_aud_play
            )
        }

        playButton.setOnClickListener {
            if (viewModel.isPlaying.value == true) viewModel.pausePlayer()
            else viewModel.startPlayer()
        }
    }

    private fun formatTime(ms: Int): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}
