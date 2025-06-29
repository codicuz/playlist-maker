package com.practicum.playlistmaker

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder(
    parent: ViewGroup,
    private val searchHistory: SearchHistory
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.activity_record_item, parent, false)
) {
    private val trackName: TextView = itemView.findViewById(R.id.track_name)
    private val artistsName: TextView = itemView.findViewById(R.id.artists_name)
    private val trackTime: TextView = itemView.findViewById(R.id.trackTime)
    private val artworkImage: ImageView = itemView.findViewById(R.id.artwork_image)

    fun bind(item: Track) {
        trackName.text = item.trackName
        artistsName.setText("")
        artistsName.text = item.artistsName
        trackTime.text = item.trackTime

        Glide.with(itemView)
            .load(item.artworkUrl100)
            .placeholder(R.drawable.ic_no_artwork_image)
            .fitCenter()
            .transform(RoundedCorners(Useful.dpToPx(2f, itemView.context)))
            .into(artworkImage)

        itemView.setOnClickListener {
            searchHistory.addTrack(item)
            val context = itemView.context
            val intent = Intent(context, AudioPlayerActivity::class.java).apply {
                putExtra("trackName", item.trackName)
                putExtra("artistName", item.trackName)
                putExtra("collectionName", item.collectionName)
                putExtra("releaseDate", item.releaseYear)
                putExtra("primaryGenreName", item.primaryGenreName)
                putExtra("trackTime", item.trackTime)
                putExtra("country", item.country)
                putExtra("artworkUrl100", item.getConvertArtwork())
            }
            context.startActivity(intent)
        }
    }
}
