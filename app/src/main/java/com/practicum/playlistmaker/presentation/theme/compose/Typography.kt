package com.practicum.playlistmaker.presentation.theme.compose

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.practicum.playlistmaker.R

val YsDisplayRegular = FontFamily(
    Font(R.font.ys_display_regular, FontWeight.Normal)
)

val YsDisplayMedium = FontFamily(
    Font(R.font.ys_display_medium, FontWeight.Medium)
)

val YsDisplayBold = FontFamily(
    Font(R.font.ys_display_bold, FontWeight.Bold)
)

object AppTextStyles {
    val ActivityTitle = TextStyle(
        fontFamily = YsDisplayMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )

    val PlaylistTitle = TextStyle(
        fontFamily = YsDisplayMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )

    val PlaylistText = TextStyle(
        fontFamily = YsDisplayRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )

    val TrackTitle = TextStyle(
        fontFamily = YsDisplayRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )

    val TrackArtistTime = TextStyle(
        fontFamily = YsDisplayRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 13.sp
    )

    val ErrorText = TextStyle(
        fontFamily = YsDisplayMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 19.sp,
        lineHeight = 24.sp
    )

    val BottomSheetTitle = TextStyle(
        fontFamily = YsDisplayMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 19.sp,
        lineHeight = 24.sp
    )

    val SettingsButtonText = TextStyle(
        fontFamily = YsDisplayRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )

    val MediaText = TextStyle(
        fontFamily = YsDisplayMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 19.sp,
        lineHeight = 24.sp
    )

    val PlayerMainText = TextStyle(
        fontFamily = YsDisplayMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )

    val PlayerArtistName = TextStyle(
        fontFamily = YsDisplayRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )
    val PlayerDetailsValue = TextStyle(
        fontFamily = YsDisplayRegular,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
}