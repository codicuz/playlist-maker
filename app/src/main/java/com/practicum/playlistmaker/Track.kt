package com.practicum.playlistmaker

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Track(
    val trackId: Int?,
    val trackName: String?,
    @SerializedName("artistName") val artistsName: String?,
    private val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?
) {
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

//    companion object {
//        fun getMockTrackList(): ArrayList<Track> = arrayListOf(
//            Track(
//                1,
//                "Smells Like Teen Spirit",
//                "Nirvana",
//                293000,
//                "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg",
//                "Smells Like Teen Spirit",
//                1999,
//                "Rock",
//                "Russia"
//            ),
//            Track(
//                2,
//                "Billie Jean",
//                "Michael Jackson",
//                303001,
//                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg",
//                "Smells Like Teen Spirit",
//                1999,
//                "Rock",
//                "Russia"
//            ),
//            Track(
//                3,
//                "Stayin' Alive",
//                "Bee Gees",
//                304002,
//                "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg",
//                "Smells Like Teen Spirit",
//                1999,
//                "Rock",
//                "Russia"
//            ),
//            Track(
//                4,
//                "Whole Lotta Love",
//                "Led Zeppelin",
//                305003,
//                "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg",
//                "Smells Like Teen Spirit",
//                1999,
//                "Rock",
//                "Russia"
//            ),
//            Track(
//                5,
//                "Sweet Child O'Mine",
//                "Guns N'Roses",
//                306004,
//                "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg",
//                "Smells Like Teen Spirit",
//                1999,
//                "Rock",
//                "Russia"
//            ),
//            Track(
//                6,
//                "Очень длинная песня. Аты-баты шли солдаты, аты-баты на войну. Шли солдаты аты-баты, шли солдаты на войну",
//                "Русская народная песня во исполнении русских народных исполнителей",
//                307005,
//                "https://some-artists-picture.ru",
//                "Smells Like Teen Spirit",
//                1999,
//                "Rock",
//                "Russia"
//            )
//        )
//    }
}
