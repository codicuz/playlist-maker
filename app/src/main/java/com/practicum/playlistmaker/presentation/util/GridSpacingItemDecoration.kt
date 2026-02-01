package com.practicum.playlistmaker.presentation.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spacingPx: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = spacingPx
        outRect.right = spacingPx
        outRect.bottom = spacingPx
        outRect.top = spacingPx
    }
}