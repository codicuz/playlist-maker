package com.practicum.playlistmaker.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Parcelize
data class Track(
    val trackId: Int?,
    val trackName: String?,
    val artistsName: String?,
    private val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val previewUrl: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?
) : Parcelable {
    val trackTime: String?
        get() = trackTimeMillis?.let {
            SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date(it))
        }

    val releaseYear: String?
        get() = releaseDate?.let {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val date = inputFormat.parse(it)
                val outputFormat = SimpleDateFormat("yyyy", Locale.getDefault())
                outputFormat.format(date!!)
            } catch (e: Exception) {
                null
            }
        }

    fun getConvertArtwork(): String {
        return artworkUrl100?.replaceAfterLast("/", "512x512bb.jpg") ?: ""
    }
}
