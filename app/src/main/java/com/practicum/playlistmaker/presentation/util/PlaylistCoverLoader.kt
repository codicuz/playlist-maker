package com.practicum.playlistmaker.presentation.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.playlist.Playlist
import java.io.File

object PlaylistCoverLoader {

    fun loadInto(playlist: Playlist, imageView: ImageView, radiusPx: Int) {
        val model = getModel(playlist)

        Glide.with(imageView.context)
            .load(model)
            .placeholder(R.drawable.ic_no_artwork_image)
            .transform(CenterCrop(), RoundedCorners(radiusPx))
            .into(imageView)
    }

    private fun getModel(playlist: Playlist): Any {
        return if (!playlist.coverUri.isNullOrEmpty()) {
            try {
                val file = File(playlist.coverUri)
                if (file.exists()) file else R.drawable.ic_no_artwork_image
            } catch (e: Exception) {
                R.drawable.ic_no_artwork_image
            }
        } else {
            R.drawable.ic_no_artwork_image
        }
    }
}