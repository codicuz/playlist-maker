package com.practicum.playlistmaker.presentation.player

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.practicum.playlistmaker.R
import kotlin.math.min

class PlaybackButtonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isPlaying = false
    private var playDrawable: Drawable? = null
    private var pauseDrawable: Drawable? = null
    private val rect = RectF()

    init {
        isClickable = true

        context.theme.obtainStyledAttributes(
            attrs, R.styleable.PlaybackButtonView, defStyleAttr, 0
        ).apply {
            try {
                val playIconResId = getResourceId(R.styleable.PlaybackButtonView_playIcon, 0)
                val pauseIconResId = getResourceId(R.styleable.PlaybackButtonView_pauseIcon, 0)

                if (playIconResId != 0) {
                    playDrawable = AppCompatResources.getDrawable(context, playIconResId)
                }

                if (pauseIconResId != 0) {
                    pauseDrawable = AppCompatResources.getDrawable(context, pauseIconResId)
                }

                if (playDrawable == null) {
                    playDrawable = AppCompatResources.getDrawable(context, R.drawable.btn_aud_play)
                }
                if (pauseDrawable == null) {
                    pauseDrawable =
                        AppCompatResources.getDrawable(context, R.drawable.btn_aud_pause)
                }

                playDrawable?.setBounds(
                    0,
                    0,
                    playDrawable?.intrinsicWidth ?: 0,
                    playDrawable?.intrinsicHeight ?: 0
                )
                pauseDrawable?.setBounds(
                    0,
                    0,
                    pauseDrawable?.intrinsicWidth ?: 0,
                    pauseDrawable?.intrinsicHeight ?: 0
                )

            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateRect()
    }

    private fun calculateRect() {
        val drawable = if (isPlaying) pauseDrawable else playDrawable
        drawable?.let {
            val drawableWidth = it.intrinsicWidth
            val drawableHeight = it.intrinsicHeight

            if (drawableWidth <= 0 || drawableHeight <= 0) return

            val viewWidth = width.toFloat()
            val viewHeight = height.toFloat()

            val scale = min(viewWidth / drawableWidth, viewHeight / drawableHeight)
            val scaledWidth = drawableWidth * scale
            val scaledHeight = drawableHeight * scale

            val left = (viewWidth - scaledWidth) / 2f
            val top = (viewHeight - scaledHeight) / 2f
            val right = left + scaledWidth
            val bottom = top + scaledHeight

            rect.set(left, top, right, bottom)

            it.setBounds(
                left.toInt(), top.toInt(), right.toInt(), bottom.toInt()
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val drawable = if (isPlaying) pauseDrawable else playDrawable
        drawable?.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }

            MotionEvent.ACTION_UP -> {
                toggleState()
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setPlaying(playing: Boolean) {
        if (isPlaying != playing) {
            isPlaying = playing
            calculateRect()
            invalidate()
        }
    }

    fun toggleState() {
        isPlaying = !isPlaying
        calculateRect()
        invalidate()
    }
}