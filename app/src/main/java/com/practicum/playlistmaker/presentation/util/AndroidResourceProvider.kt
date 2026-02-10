package com.practicum.playlistmaker.presentation.util

import android.content.Context
import androidx.annotation.StringRes

class AndroidResourceProvider(private val context: Context) : ResourceProvider {
    override fun getString(@StringRes resId: Int): String = context.getString(resId)

    override fun getString(@StringRes resId: Int, vararg args: Any): String =
        context.getString(resId, *args)
}