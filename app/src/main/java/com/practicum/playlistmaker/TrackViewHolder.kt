package com.practicum.playlistmaker

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.activity_record_item, parent, false)
) {
    private val trackName: TextView = itemView.findViewById(R.id.track_name)
    private val artistsName: TextView = itemView.findViewById(R.id.artists_name)
    private val trackTime: TextView = itemView.findViewById(R.id.trackTime)
    private val artworkImage: ImageView = itemView.findViewById(R.id.artwork_image)

    fun bind(item: Track) {
        trackName.text = item.trackName
        artistsName.text = item.artistsName
        trackTime.text = item.trackTime

        Glide.with(itemView)
            .load(item.artworkUrl100)
            .placeholder(R.drawable.ic_no_artwork_image)
            .fitCenter()
            .transform(RoundedCorners(dpToPx(2f, itemView.context)))
            .into(artworkImage)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics).toInt()
    }
}
