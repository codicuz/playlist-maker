package com.practicum.playlistmaker.presentation.adapter

import android.content.SharedPreferences
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.domain.model.Track
import com.practicum.playlistmaker.presentation.viewholder.TrackViewHolder
import com.practicum.playlistmaker.presentation.ui.SearchHistory

class TrackAdapter(
    sharedPreferences: SharedPreferences
) : RecyclerView.Adapter<TrackViewHolder>() {

    private val items = mutableListOf<Track>()
    private val searchHistory = SearchHistory(sharedPreferences)

    fun submitList(newList: List<Track>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent, searchHistory)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
