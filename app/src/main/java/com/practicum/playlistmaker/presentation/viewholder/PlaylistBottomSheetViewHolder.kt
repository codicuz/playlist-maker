package com.practicum.playlistmaker.presentation.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.BottomSheetPlaylistItemBinding
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.util.Useful

class PlaylistBottomSheetViewHolder(
    private val binding: BottomSheetPlaylistItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Playlist) {
        binding.playlistTitle.text = item.title

        binding.tracksCount.text = "0 треков"

        val radius = Useful.dpToPx(2f, itemView.context)
        Glide.with(itemView.context).load(item.coverUri).placeholder(R.drawable.ic_no_artwork_image)
            .transform(RoundedCorners(radius)).into(binding.playlistCover)
    }
}
