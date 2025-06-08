package com.practicum.playlistmaker

import android.content.SharedPreferences
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(
    private val items: List<Track>,
    sharedPreferences: SharedPreferences
) : RecyclerView.Adapter<TrackViewHolder>() {

    private val searchHistory = SearchHistory(sharedPreferences)

    fun submitList(newList: List<Track>) {
//        items.clear()
//        items.addAll(newList)
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