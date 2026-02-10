package com.practicum.playlistmaker.presentation.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacingPx: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        val effectiveSpacing = spacingPx / 2

        outRect.left = column * effectiveSpacing / spanCount
        outRect.right = effectiveSpacing - (column + 1) * effectiveSpacing / spanCount

        if (position >= spanCount) {
            outRect.top = effectiveSpacing
        }

        outRect.bottom = effectiveSpacing
    }
}