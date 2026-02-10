package com.practicum.playlistmaker.presentation.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.practicum.playlistmaker.R
import java.io.File

object CoverLoader {

    fun loadCover(
        coverUri: String?,
        imageView: ImageView,
        scaleType: ImageView.ScaleType = ImageView.ScaleType.CENTER_CROP,
        padding: Int = 0
    ) {
        if (!coverUri.isNullOrEmpty()) {
            try {
                val file = File(coverUri)
                if (file.exists()) {
                    imageView.scaleType = scaleType
                    imageView.setPadding(padding, padding, padding, padding)
                    Glide.with(imageView.context)
                        .load(file)
                        .centerCrop()
                        .into(imageView)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        loadDefaultCover(imageView, scaleType, padding)
    }

    fun loadMenuCover(coverUri: String?, imageView: ImageView) {
        loadCover(coverUri, imageView, ImageView.ScaleType.CENTER_CROP, 0)
    }

    fun loadPlaylistCover(coverUri: String?, imageView: ImageView) {
        if (!coverUri.isNullOrEmpty()) {
            try {
                val file = File(coverUri)
                if (file.exists()) {
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    imageView.setPadding(0, 0, 0, 0)
                    Glide.with(imageView.context)
                        .load(file)
                        .into(imageView)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        loadDefaultCoverWithPadding(imageView)
    }

    private fun loadDefaultCover(imageView: ImageView, scaleType: ImageView.ScaleType, padding: Int) {
        imageView.scaleType = scaleType
        imageView.setPadding(padding, padding, padding, padding)
        Glide.with(imageView.context)
            .load(R.drawable.ic_no_artwork_image)
            .centerCrop()
            .into(imageView)
    }

    private fun loadDefaultCoverWithPadding(imageView: ImageView) {
        val paddingPx = (60 * imageView.context.resources.displayMetrics.density).toInt()
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        Glide.with(imageView.context)
            .load(R.drawable.ic_no_artwork_image)
            .into(imageView)
    }
}