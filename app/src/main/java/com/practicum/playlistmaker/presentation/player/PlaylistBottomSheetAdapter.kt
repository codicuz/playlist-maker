package com.practicum.playlistmaker.presentation.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.databinding.BottomSheetPlaylistItemBinding
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.viewholder.PlaylistBottomSheetViewHolder

class PlaylistBottomSheetAdapter(
    private val playlists: MutableList<Playlist>, private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistBottomSheetViewHolder>() {

    fun update(newList: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistBottomSheetViewHolder {
        val binding = BottomSheetPlaylistItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlaylistBottomSheetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistBottomSheetViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener { onClick(playlist) }
    }

    override fun getItemCount(): Int = playlists.size
}

