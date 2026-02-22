package com.practicum.playlistmaker.presentation.util

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

class AndroidResourceProvider(private val context: Context) : ResourceProvider {

    override fun getString(@StringRes resId: Int): String = context.getString(resId)

    override fun getString(@StringRes resId: Int, vararg args: Any): String =
        context.getString(resId, *args)

    override fun getQuantityString(
        @PluralsRes resId: Int,
        quantity: Int,
        vararg args: Any
    ): String = context.resources.getQuantityString(resId, quantity, *args)
}