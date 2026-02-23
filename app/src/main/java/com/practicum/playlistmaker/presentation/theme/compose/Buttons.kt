package com.practicum.playlistmaker.presentation.theme.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NoInternetButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, text: String
) {
    val isDarkTheme = isDarkTheme()

    val buttonColors = if (isDarkTheme) {
        ButtonDefaults.buttonColors(
            containerColor = AppColors.ButtonBackgroundDark, contentColor = AppColors.ButtonTextDark
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = AppColors.ButtonBackgroundLight,
            contentColor = AppColors.ButtonTextLight
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .width(150.dp)
            .height(42.dp),
        shape = RoundedCornerShape(54.dp),
        colors = buttonColors
    ) {
        Text(
            text = text,
            style = AppTextStyles.ErrorText.copy(fontSize = 14.sp),
            color = if (isDarkTheme) AppColors.Black else AppColors.White
        )
    }
}

@Composable
fun ClearHistoryButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, text: String
) {
    val isDarkTheme = isDarkTheme()

    val buttonColors = if (isDarkTheme) {
        ButtonDefaults.buttonColors(
            containerColor = AppColors.ButtonBackgroundDark, contentColor = AppColors.ButtonTextDark
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = AppColors.ButtonBackgroundLight,
            contentColor = AppColors.ButtonTextLight
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .wrapContentWidth()
            .height(42.dp),
        shape = RoundedCornerShape(54.dp),
        colors = buttonColors
    ) {
        Text(
            text = text,
            style = AppTextStyles.ErrorText.copy(fontSize = 14.sp),
            color = if (isDarkTheme) AppColors.Black else AppColors.White
        )
    }
}

@Composable
fun MediaButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, text: String
) {
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = AppColors.Blue, contentColor = AppColors.White
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .wrapContentWidth()
            .height(36.dp),
        shape = RoundedCornerShape(54.dp),
        colors = buttonColors
    ) {
        Text(
            text = text, style = AppTextStyles.MediaText.copy(fontSize = 14.sp)
        )
    }
}

@Composable
fun NewPlaylistButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, text: String, enabled: Boolean = true
) {
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = if (enabled) AppColors.Blue else AppColors.Gray,
        contentColor = AppColors.White
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(42.dp),
        shape = RoundedCornerShape(8.dp),
        colors = buttonColors,
        enabled = enabled
    ) {
        Text(
            text = text, style = AppTextStyles.ErrorText.copy(fontSize = 16.sp)
        )
    }
}

@Composable
fun SettingsSwitchItem(
    text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    val isDarkTheme = isDarkTheme()

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onCheckedChange(!checked) }
        .padding(horizontal = 16.dp, vertical = 21.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = AppTextStyles.SettingsButtonText,
            color = if (isDarkTheme) AppColors.White else AppColors.Black
        )

        Switch(
            checked = checked, onCheckedChange = null, colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.White,
                checkedTrackColor = AppColors.Blue,
                uncheckedThumbColor = AppColors.Gray,
                uncheckedTrackColor = AppColors.LightGray
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    text: String, icon: Int, onClick: () -> Unit
) {
    val isDarkTheme = isDarkTheme()

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(horizontal = 16.dp, vertical = 21.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = AppTextStyles.SettingsButtonText,
            color = if (isDarkTheme) AppColors.White else AppColors.Black
        )

        Icon(
            painter = painterResource(icon), contentDescription = null, tint = AppColors.Gray
        )
    }
}