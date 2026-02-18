package com.practicum.playlistmaker.presentation.theme.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val AppTypography = Typography(
    headlineMedium = AppTextStyles.ActivityTitle,
    headlineSmall = AppTextStyles.PlayerMainText,
    titleLarge = AppTextStyles.PlaylistTitle,
    titleMedium = AppTextStyles.ErrorText,
    bodyLarge = AppTextStyles.TrackTitle,
    bodyMedium = AppTextStyles.TrackArtistTime,
    labelLarge = AppTextStyles.SettingsButtonText
)

val LightThemeColors = lightColorScheme(
    primary = AppColors.Blue,
    onPrimary = AppColors.White,
    primaryContainer = AppColors.LightGray,
    secondary = AppColors.Black,
    onSecondary = AppColors.White,
    background = AppColors.White,
    onBackground = AppColors.Black,
    surface = AppColors.White,
    onSurface = AppColors.Black,
    surfaceVariant = AppColors.LightGray,
    onSurfaceVariant = AppColors.Gray,
    error = AppColors.Red,
    onError = AppColors.White
)

val DarkThemeColors = darkColorScheme(
    primary = AppColors.Blue,
    onPrimary = AppColors.White,
    primaryContainer = AppColors.Black,
    secondary = AppColors.White,
    onSecondary = AppColors.Black,
    background = AppColors.Black,
    onBackground = AppColors.White,
    surface = Color(0xFF2A2B33),
    onSurface = AppColors.White,
    surfaceVariant = Color(0xFF2A2B33),
    onSurfaceVariant = AppColors.Gray,
    error = AppColors.Red,
    onError = AppColors.White
)

val AppShapes = Shapes(
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkThemeColors else LightThemeColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme, typography = AppTypography, shapes = AppShapes, content = content
    )
}

@Composable
fun isDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.background == AppColors.Black
}