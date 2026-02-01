package com.practicum.playlistmaker.presentation.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.PlaylistItemBinding
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.util.Useful

class PlaylistViewHolder(
    private val binding: PlaylistItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Playlist) {

        binding.playlistTitle.text = item.title
        binding.tracksCount.text = "0 трэков"

        val radius = Useful.dpToPx(8f, itemView.context)

        Glide.with(itemView.context)
            .load(item.coverUri)
            .placeholder(R.drawable.ic_no_artwork_image)
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(radius)))
            .into(binding.playlistCover)
    }
}