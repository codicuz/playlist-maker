package com.practicum.playlistmaker.presentation.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EditPlaylistFragmentArgs(
    val playlistId: Long
) : Parcelable