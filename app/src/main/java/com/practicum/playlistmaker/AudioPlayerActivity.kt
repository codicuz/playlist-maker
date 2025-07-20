package com.practicum.playlistmaker

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

class AudioPlayerActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var playButton: ImageButton
    private lateinit var currentTrackTime: TextView
    private var updateTimeRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.audio_player)

        val trackName = intent.getStringExtra("trackName") ?: ""
        val artistName = intent.getStringExtra("artistName") ?: ""
        val collectionName = intent.getStringExtra("collectionName") ?: ""
        val releaseDate = intent.getStringExtra("releaseDate") ?: ""
        val primaryGenreName = intent.getStringExtra("primaryGenreName") ?: ""
        val country = intent.getStringExtra("country") ?: ""
        val trackTime = intent.getStringExtra("trackTime") ?: ""
        val artworkUrl100 = intent.getStringExtra("artworkUrl100") ?: ""
        val previewUrl = intent.getStringExtra("previewUrl") ?: ""

        findViewById<ImageView>(R.id.audBackButton).setOnClickListener { finish() }

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

        currentTrackTime = findViewById(R.id.currentTrackTime)
        playButton = findViewById(R.id.audPlayButton)

        trackNameTextView.text = trackName
        artistNameTextView.text = artistName
        collectionNameTextView.text = collectionName
        releaseDateTextView.text = releaseDate
        genreTextView.text = primaryGenreName
        countryTextView.text = country
        trackTimeTextView.text = trackTime

        setVisibilityBasedOnText(collectionNameTextView, collectionNameTextViewKey, collectionName)
        setVisibilityBasedOnText(releaseDateTextView, releaseDateTextViewKey, releaseDate)

        currentTrackTime.text = "00:00"

        Glide.with(this).load(artworkUrl100).placeholder(R.drawable.ic_no_artwork_image)
            .transform(RoundedCorners(Useful.dpToPx(8f, this))).into(artworkImageView)

        if (previewUrl.isNotEmpty()) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    Log.d("AudioPlayer", "MediaPlayer готов к воспроизведению")
                    playButton.isEnabled = true
                }
                setOnCompletionListener {
                    stopPlayer()
                }
            }
            playButton.isEnabled = false
        } else {
            playButton.isEnabled = false
            Log.d("AudioPlayer", "Нет ссылки для воспроизведения произведения")
            currentTrackTime.text = "-----" // Оставлено на случай, если по каким-то причинам previewUrl не пришел из интернета. Реализовал так для сигнализации пустого previewUrl.
        }

        playButton.setOnClickListener {
            Log.d("AudioPlayer", "Preview URL: $previewUrl")
            Log.d("AudioPlayer", "Play button clicked")
            if (isPlaying) {
                pausePlayer()
            } else {
                startPlayer()
            }
        }
    }

    private fun setVisibilityBasedOnText(valueView: View, keyView: View, text: String) {
        if (text.isBlank()) {
            valueView.visibility = View.GONE
            keyView.visibility = View.GONE
        } else {
            valueView.visibility = View.VISIBLE
            keyView.visibility = View.VISIBLE
        }
    }

    private fun startPlayer() {
        if (mediaPlayer == null) {
            Toast.makeText(this, "MediaPlayer не готов", Toast.LENGTH_SHORT).show()
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
                Log.d("AudioPlayer", "Updating time: $currentPosition ms")
                currentTrackTime.text = formatTime(currentPosition)
                handler.postDelayed(this, 500)
            }
        }
        handler.post(updateTimeRunnable!!)
    }

    private fun stopUpdatingTime() {
        updateTimeRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun formatTime(ms: Int): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onPause() {
        super.onPause()
        Log.d("AudioPlayerActivity", "onPause: приложение уходит в фон")
        if (isPlaying) {
            pausePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("AudioPlayerActivity", "Приложение вернулось из фона, аудиоплеер активен")
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        stopUpdatingTime()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("trackName", findViewById<TextView>(R.id.mainAlbumText).text.toString())
        outState.putString("artistName", findViewById<TextView>(R.id.artistName).text.toString())
        outState.putString(
            "collectionName", findViewById<TextView>(R.id.audAlbumNameValue).text.toString()
        )
        outState.putString("releaseDate", findViewById<TextView>(R.id.audYearValue).text.toString())
        outState.putString(
            "primaryGenreName", findViewById<TextView>(R.id.audGenreValue).text.toString()
        )
        outState.putString("country", findViewById<TextView>(R.id.audCountryValue).text.toString())
        outState.putString(
            "trackTime", findViewById<TextView>(R.id.audTrackTimeValue).text.toString()
        )
        outState.putString("artworkUrl100", intent.getStringExtra("artworkUrl100"))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        findViewById<TextView>(R.id.mainAlbumText).text = savedInstanceState.getString("trackName")
        findViewById<TextView>(R.id.artistName).text = savedInstanceState.getString("artistName")
        findViewById<TextView>(R.id.audAlbumNameValue).text =
            savedInstanceState.getString("collectionName")
        findViewById<TextView>(R.id.audYearValue).text = savedInstanceState.getString("releaseDate")
        findViewById<TextView>(R.id.audGenreValue).text =
            savedInstanceState.getString("primaryGenreName")
        findViewById<TextView>(R.id.audCountryValue).text = savedInstanceState.getString("country")
        findViewById<TextView>(R.id.audTrackTimeValue).text =
            savedInstanceState.getString("trackTime")

        val artworkUrl100 = savedInstanceState.getString("artworkUrl100")
        if (!artworkUrl100.isNullOrEmpty()) {
            Glide.with(this).load(artworkUrl100).placeholder(R.drawable.ic_no_artwork_image)
                .transform(RoundedCorners(Useful.dpToPx(8f, this)))
                .into(findViewById(R.id.songPoster))
        }
    }
}
