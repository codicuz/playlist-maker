package com.practicum.playlistmaker.ui.player

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.presentation.util.Useful

class AudioPlayerActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var playButton: ImageButton
    private lateinit var currentTrackTime: TextView
    private var updateTimeRunnable: Runnable? = null

    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.audio_player)

        track = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("track", Track::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra<Track>("track")
        } ?: run {
            finish()
            return
        }


        currentTrackTime = findViewById(R.id.currentTrackTime)
        playButton = findViewById(R.id.audPlayButton)
        val trackNameTextView: TextView = findViewById(R.id.mainAlbumText)
        val artistNameTextView: TextView = findViewById(R.id.artistName)
        val collectionNameTextView: TextView = findViewById(R.id.audAlbumNameValue)
        val collectionNameTextViewKey: TextView = findViewById(R.id.audAlbumNameKey)
        val releaseDateTextView: TextView = findViewById(R.id.audYearValue)
        val releaseDateTextViewKey: TextView = findViewById(R.id.audYearKey)
        val genreTextView: TextView = findViewById(R.id.audGenreValue)
        val countryTextView: TextView = findViewById(R.id.audCountryValue)
        val trackTimeTextView: TextView = findViewById(R.id.audTrackTimeValue)
        val artworkImageView: ImageView = findViewById(R.id.songPoster)
        val backButton: ImageView = findViewById(R.id.audBackButton)

        backButton.setOnClickListener { finish() }

        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistsName
        collectionNameTextView.text = track.collectionName
        releaseDateTextView.text = track.releaseYear
        genreTextView.text = track.primaryGenreName
        countryTextView.text = track.country
        trackTimeTextView.text = track.trackTime

        setVisibilityBasedOnText(
            collectionNameTextView, collectionNameTextViewKey, track.collectionName
        )
        setVisibilityBasedOnText(releaseDateTextView, releaseDateTextViewKey, track.releaseYear)

        currentTrackTime.text = formatTime(0)

        Glide.with(this).load(track.getConvertArtwork()).placeholder(R.drawable.ic_no_artwork_image)
            .transform(RoundedCorners(Useful.dpToPx(8f, this))).into(artworkImageView)

        val previewUrl = track.previewUrl
        if (!previewUrl.isNullOrEmpty()) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    Log.d("AudioPlayer", getString(R.string.mp_not_ready_to_play))
                    playButton.isEnabled = true
                }
                setOnCompletionListener { stopPlayer() }
            }
            playButton.isEnabled = false
        } else {
            playButton.isEnabled = false
            Log.d("AudioPlayer", getString(R.string.no_url_to_play))
            currentTrackTime.text = getString(R.string.no_preview_url)
        }

        playButton.setOnClickListener {
            if (isPlaying) pausePlayer() else startPlayer()
        }
    }

    private fun setVisibilityBasedOnText(valueView: View, keyView: View, text: String?) {
        if (text.isNullOrBlank()) {
            valueView.visibility = View.GONE
            keyView.visibility = View.GONE
        } else {
            valueView.visibility = View.VISIBLE
            keyView.visibility = View.VISIBLE
        }
    }

    private fun startPlayer() {
        if (mediaPlayer == null) {
            Toast.makeText(this, getString(R.string.mp_not_ready), Toast.LENGTH_SHORT).show()
            return
        }
        mediaPlayer?.start()
        isPlaying = true
        playButton.setImageResource(R.drawable.btn_aud_pause)
        startUpdatingTime()
    }

    private fun pausePlayer() {
        mediaPlayer?.pause()
        isPlaying = false
        playButton.setImageResource(R.drawable.btn_aud_play)
        stopUpdatingTime()
    }

    @SuppressLint("SetTextI18n")
    private fun stopPlayer() {
        mediaPlayer?.seekTo(0)
        mediaPlayer?.pause()
        isPlaying = false
        playButton.setImageResource(R.drawable.btn_aud_play)
        currentTrackTime.text = "00:00"
        stopUpdatingTime()
    }

    private fun startUpdatingTime() {
        updateTimeRunnable = object : Runnable {
            override fun run() {
                val currentPosition = mediaPlayer?.currentPosition ?: 0
                currentTrackTime.text = formatTime(currentPosition)
                handler.postDelayed(this, 500)
            }
        }.also { handler.post(it) }
    }

    private fun stopUpdatingTime() {
        updateTimeRunnable?.let { handler.removeCallbacks(it) }
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(ms: Int): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        stopUpdatingTime()
    }
}
