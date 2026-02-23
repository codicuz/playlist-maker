package com.practicum.playlistmaker.presentation.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.navigation.Screen
import com.practicum.playlistmaker.presentation.theme.compose.AppColors
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import com.practicum.playlistmaker.presentation.theme.compose.isDarkTheme

@Composable
fun BottomNavigationBar(
    currentRoute: String?, onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Search, BottomNavItem.Media, BottomNavItem.Settings
    )

    val isDarkTheme = isDarkTheme()

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        containerColor = if (isDarkTheme) AppColors.Black else AppColors.White,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen.route) },
                icon = {
                    Icon(
                        painter = painterResource(
                            if (isSelected) item.iconSelected else item.iconUnselected
                        ),
                        contentDescription = stringResource(item.titleResId),
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.titleResId),
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Blue,
                    selectedTextColor = AppColors.Blue,
                    unselectedIconColor = AppColors.Gray,
                    unselectedTextColor = AppColors.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

data class BottomNavItem(
    val screen: Screen, val iconSelected: Int, val iconUnselected: Int, val titleResId: Int
) {
    companion object {
        val Search = BottomNavItem(
            screen = Screen.Search,
            iconSelected = R.drawable.search,
            iconUnselected = R.drawable.search,
            titleResId = R.string.search
        )

        val Media = BottomNavItem(
            screen = Screen.Media,
            iconSelected = R.drawable.media,
            iconUnselected = R.drawable.media,
            titleResId = R.string.media
        )

        val Settings = BottomNavItem(
            screen = Screen.Settings,
            iconSelected = R.drawable.settings,
            iconUnselected = R.drawable.settings,
            titleResId = R.string.settings
        )
    }
}

@Preview(
    name = "Bottom Navigation", showBackground = true
)
@Composable
private fun BottomNavigationBarCombinedPreview() {
    Column {
        AppTheme(darkTheme = false) {
            BottomNavigationBarRow(
                title = "Light - Search", initialRoute = Screen.Search.route
            )

            BottomNavigationBarRow(
                title = "Light - Media", initialRoute = Screen.Media.route
            )

            BottomNavigationBarRow(
                title = "Light - Settings", initialRoute = Screen.Settings.route
            )
        }

        AppTheme(darkTheme = true) {
            BottomNavigationBarRow(
                title = "Dark - Search", initialRoute = Screen.Search.route
            )

            BottomNavigationBarRow(
                title = "Dark - Media", initialRoute = Screen.Media.route
            )

            BottomNavigationBarRow(
                title = "Dark - Settings", initialRoute = Screen.Settings.route
            )
        }
    }
}

@Composable
fun BottomNavigationBarRow(
    title: String, initialRoute: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title, modifier = Modifier.padding(8.dp)
        )

        BottomNavigationBar(
            currentRoute = initialRoute, onNavigate = {})

        Spacer(modifier = Modifier.height(8.dp))
    }
}