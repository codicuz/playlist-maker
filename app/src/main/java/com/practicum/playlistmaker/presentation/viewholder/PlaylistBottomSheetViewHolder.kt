package com.practicum.playlistmaker.presentation.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.BottomSheetPlaylistItemBinding
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.util.Useful
import java.io.File

class PlaylistBottomSheetViewHolder(
    private val binding: BottomSheetPlaylistItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Playlist) {
        binding.playlistTitle.text = item.title
        binding.tracksCount.text = "${item.trackCount} трэков"

        val radius = Useful.dpToPx(2f, itemView.context)

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