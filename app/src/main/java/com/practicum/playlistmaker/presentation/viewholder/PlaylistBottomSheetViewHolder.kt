package com.practicum.playlistmaker.presentation.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.BottomSheetPlaylistItemBinding
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.util.PlaylistCoverLoader
import com.practicum.playlistmaker.presentation.util.Useful

class PlaylistBottomSheetViewHolder(
    private val binding: BottomSheetPlaylistItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Playlist) {
        binding.playlistTitle.text = item.title

        val tracksCount = item.trackCount
        binding.tracksCount.text = itemView.context.resources.getQuantityString(
            R.plurals.tracks_count, tracksCount, tracksCount
        )

        val radius = Useful.dpToPx(2f, itemView.context)
        PlaylistCoverLoader.loadInto(item, binding.playlistCover, radius)
    }
}