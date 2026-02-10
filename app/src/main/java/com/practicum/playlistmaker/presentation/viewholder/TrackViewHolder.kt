package com.practicum.playlistmaker.presentation.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.RecordItemBinding
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.util.Useful

class TrackViewHolder(
    private val binding: RecordItemBinding,
    private val onTrackClick: (Track) -> Unit,
    private val onTrackLongClick: (Track) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Track) {
        binding.trackName.text = item.trackName
        binding.artistsName.text = item.artistsName
        binding.trackTime.text = item.trackTime

        Glide.with(itemView).load(item.artworkUrl100).placeholder(R.drawable.ic_no_artwork_image)
            .fitCenter().transform(RoundedCorners(Useful.dpToPx(2f, itemView.context)))
            .into(binding.artworkImage)

        itemView.setOnClickListener {
            onTrackClick(item)
        }

        itemView.setOnLongClickListener {
            onTrackLongClick(item)
            true
        }
    }
}
