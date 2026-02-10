package com.practicum.playlistmaker.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.databinding.RecordItemBinding
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.viewholder.TrackViewHolder

class TrackAdapter(
    private val onTrackClick: (Track) -> Unit,
    private val onTrackLongClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackViewHolder>() {
    private val items = mutableListOf<Track>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<Track>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = RecordItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TrackViewHolder(binding, onTrackClick, onTrackLongClick)
    }


    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}