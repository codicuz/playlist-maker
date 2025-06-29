package com.practicum.playlistmaker

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class AudioPlayerActivity : AppCompatActivity() {
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

//        val collectionName = ""
//        val releaseDate = ""

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

        val currentTrackTime: TextView = findViewById(R.id.currentTrackTime)
        val artworkImageView: ImageView = findViewById(R.id.songPoster)

        trackNameTextView.text = trackName
        artistNameTextView.text = artistName
        collectionNameTextView.text = collectionName
        releaseDateTextView.text = releaseDate
        genreTextView.text = primaryGenreName
        countryTextView.text = country
        trackTimeTextView.text = trackTime

        currentTrackTime.text = "00:11" // Пока установлено так, ибо это мы еще не проходили

        setVisibilityBasedOnText(collectionNameTextView, collectionNameTextViewKey, collectionName)
        setVisibilityBasedOnText(releaseDateTextView, releaseDateTextViewKey, releaseDate)

        Glide.with(this)
            .load(artworkUrl100)
            .placeholder(R.drawable.ic_no_artwork_image)
            .transform(RoundedCorners(Useful.dpToPx(8f, this)))
            .into(artworkImageView)
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

    override fun onResume() {
        super.onResume()
        Log.d("AudioPlayerActivity", "Приложение вернулось из фона, аудиоплеер активен")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("trackName", findViewById<TextView>(R.id.mainAlbumText).text.toString())
        outState.putString("artistName", findViewById<TextView>(R.id.artistName).text.toString())
        outState.putString(
            "collectionName",
            findViewById<TextView>(R.id.audAlbumNameValue).text.toString()
        )
        outState.putString("releaseDate", findViewById<TextView>(R.id.audYearValue).text.toString())
        outState.putString(
            "primaryGenreName",
            findViewById<TextView>(R.id.audGenreValue).text.toString()
        )
        outState.putString("country", findViewById<TextView>(R.id.audCountryValue).text.toString())
        outState.putString(
            "trackTime",
            findViewById<TextView>(R.id.audTrackTimeValue).text.toString()
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
            Glide.with(this)
                .load(artworkUrl100)
                .placeholder(R.drawable.ic_no_artwork_image)
                .transform(RoundedCorners(Useful.dpToPx(8f, this)))
                .into(findViewById(R.id.songPoster))
        }
    }
}
