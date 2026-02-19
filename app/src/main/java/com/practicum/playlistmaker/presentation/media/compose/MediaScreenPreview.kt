package com.practicum.playlistmaker.presentation.media.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.practicum.playlistmaker.presentation.theme.compose.AppColors
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import com.practicum.playlistmaker.presentation.theme.compose.isDarkTheme

@Preview(
    name = "Media Screen Light - Preview",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 800,
    widthDp = 360
)
@Composable
fun MediaScreenLightPreview() {
    AppTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkTheme()) AppColors.Black else AppColors.White)
        ) {
            Text(
                text = "Media Screen Preview", modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview(
    name = "Media Screen Dark - Preview",
    showBackground = true,
    backgroundColor = 0xFF1A1B22,
    heightDp = 800,
    widthDp = 360
)
@Composable
fun MediaScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkTheme()) AppColors.Black else AppColors.White)
        ) {
            Text(
                text = "Media Screen Preview",
                color = AppColors.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}