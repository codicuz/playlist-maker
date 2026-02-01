package com.practicum.playlistmaker.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.databinding.PlaylistItemBinding
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.viewholder.PlaylistViewHolder

class PlaylistAdapter : RecyclerView.Adapter<PlaylistViewHolder>() {

    private val items = mutableListOf<Playlist>()

    fun submitList(list: List<Playlist>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = PlaylistItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}