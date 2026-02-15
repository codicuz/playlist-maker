package com.practicum.playlistmaker.presentation.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.PlaylistItemBinding
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.util.PlaylistCoverLoader
import com.practicum.playlistmaker.presentation.util.Useful

class PlaylistViewHolder(
    private val binding: PlaylistItemBinding, private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Playlist) {
        binding.playlistTitle.text = item.title

        val tracksCount = item.trackCount
        binding.tracksCount.text = itemView.context.resources.getQuantityString(
            R.plurals.tracks_count, tracksCount, tracksCount
        )

        val radius = Useful.dpToPx(8f, itemView.context)
        PlaylistCoverLoader.loadInto(item, binding.playlistCover, radius)

        itemView.setOnClickListener {
            onPlaylistClick(item)
        }
    }
}