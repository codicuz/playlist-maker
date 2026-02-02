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
import java.io.File

class PlaylistViewHolder(
    private val binding: PlaylistItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Playlist) {
        binding.playlistTitle.text = item.title

        val tracksCount = item.trackCount
        binding.tracksCount.text = itemView.context.resources.getQuantityString(
            R.plurals.tracks_count,
            tracksCount,
            tracksCount
        )

        val radius = Useful.dpToPx(8f, itemView.context)

        if (!item.coverUri.isNullOrEmpty()) {
            try {
                val file = File(item.coverUri)
                if (file.exists()) {
                    Glide.with(itemView.context).load(file)
                        .placeholder(R.drawable.ic_no_artwork_image)
                        .transform(MultiTransformation(CenterCrop(), RoundedCorners(radius)))
                        .into(binding.playlistCover)
                } else {
                    loadDefaultCover(radius)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadDefaultCover(radius)
            }
        } else {
            loadDefaultCover(radius)
        }
    }

    private fun loadDefaultCover(radius: Int) {
        Glide.with(itemView.context).load(R.drawable.ic_no_artwork_image)
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(radius)))
            .into(binding.playlistCover)
    }
}