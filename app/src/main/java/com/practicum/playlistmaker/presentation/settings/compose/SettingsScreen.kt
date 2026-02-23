package com.practicum.playlistmaker.presentation.settings.compose

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import com.practicum.playlistmaker.presentation.theme.compose.AppColors
import com.practicum.playlistmaker.presentation.theme.compose.AppTextStyles
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: ThemeViewModel = koinViewModel(), onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDarkMode = state.isDarkMode

    viewModel.uiEvent.observeAsState().value?.let { event ->
        when (event) {
            is com.practicum.playlistmaker.presentation.settings.SettingsUiEvent.OpenPracticumOffer -> {
                openPracticumOffer(context)
                viewModel.resetUiEvent()
            }

            is com.practicum.playlistmaker.presentation.settings.SettingsUiEvent.SendToHelpdesk -> {
                openHelpdeskEmail(context)
                viewModel.resetUiEvent()
            }

            is com.practicum.playlistmaker.presentation.settings.SettingsUiEvent.ShareApp -> {
                shareApp(context)
                viewModel.resetUiEvent()
            }

            is com.practicum.playlistmaker.presentation.settings.SettingsUiEvent.ShowError -> {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                viewModel.resetUiEvent()
            }

            else -> {}
        }
    }

    SettingsContent(
        isDarkMode = isDarkMode,
        onThemeSwitch = { viewModel.switchTheme(it) },
        onPracticumOfferClick = { viewModel.onPracticumOfferClicked() },
        onSendToHelpdeskClick = { viewModel.onSendToHelpdeskClicked() },
        onShareAppClick = { viewModel.onShareAppClicked() },
        onBackClick = onNavigateBack
    )
}

@Composable
fun SettingsContent(
    isDarkMode: Boolean,
    onThemeSwitch: (Boolean) -> Unit,
    onPracticumOfferClick: () -> Unit,
    onSendToHelpdeskClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) AppColors.Black else AppColors.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) { onBackClick() }
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.settings),
                style = AppTextStyles.ActivityTitle,
                color = if (isDarkMode) AppColors.White else AppColors.Black,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        SettingsSwitchItem(
            text = stringResource(R.string.dark_theme),
            checked = isDarkMode,
            onCheckedChange = onThemeSwitch,
            isDarkMode = isDarkMode,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 21.dp)
        )

        SettingsClickableItem(
            text = stringResource(R.string.app_share),
            icon = R.drawable.share,
            onClick = onShareAppClick,
            isDarkMode = isDarkMode,
            iconWidth = 18.dp,
            iconHeight = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 21.dp)
        )

        SettingsClickableItem(
            text = stringResource(R.string.helpdesk),
            icon = R.drawable.helpdesk,
            onClick = onSendToHelpdeskClick,
            isDarkMode = isDarkMode,
            iconWidth = 24.dp,
            iconHeight = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 21.dp)
        )

        SettingsClickableItem(
            text = stringResource(R.string.agreement),
            icon = R.drawable.agreement,
            onClick = onPracticumOfferClick,
            isDarkMode = isDarkMode,
            iconWidth = 24.dp,
            iconHeight = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 21.dp)
        )
    }
}

@Composable
fun SettingsSwitchItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = AppTextStyles.SettingsButtonText,
            color = if (isDarkMode) AppColors.White else AppColors.Black
        )

        SmallSwitch(
            checked = checked, onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SmallSwitch(
    checked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    val trackWidth = 40.dp
    val trackHeight = 16.dp
    val thumbSize = 24.dp

    val offset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize
        else 0.dp, label = ""
    )

    Box(
        modifier = Modifier
            .width(trackWidth)
            .height(thumbSize)
            .clickable { onCheckedChange(!checked) }, contentAlignment = Alignment.CenterStart
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(trackHeight / 2))
                .background(
                    if (checked) AppColors.LightBlue
                    else AppColors.LightGray
                )
        )

        Box(
            modifier = Modifier
                .offset(x = offset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(
                    if (checked) AppColors.Blue
                    else AppColors.Gray
                )
        )
    }
}

@Composable
fun SettingsClickableItem(
    text: String,
    icon: Int,
    onClick: () -> Unit,
    isDarkMode: Boolean,
    iconWidth: androidx.compose.ui.unit.Dp = 24.dp,
    iconHeight: androidx.compose.ui.unit.Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = AppTextStyles.SettingsButtonText,
            color = if (isDarkMode) AppColors.White else AppColors.Black
        )

        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (isDarkMode) AppColors.White else AppColors.Gray,
            modifier = Modifier.size(width = iconWidth, height = iconHeight)
        )
    }
}

private fun openPracticumOffer(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = context.getString(R.string.practicum_license).toUri()
    }
    startSafe(intent, context)
}

private fun openHelpdeskEmail(context: android.content.Context) {
    val email = context.getString(R.string.email)
    val subject = context.getString(R.string.email_subject)
    val body = context.getString(R.string.email_text)
    val uri = "mailto:$email?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
    val intent = Intent(Intent.ACTION_SENDTO, uri.toUri())
    startSafe(intent, context)
}

private fun shareApp(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            context.getString(R.string.https_practicum_yandex_ru_android_developer)
        )
    }
    startSafe(Intent.createChooser(intent, null), context)
}

private fun startSafe(intent: Intent, context: android.content.Context) {
    try {
        ContextCompat.startActivity(context, intent, null)
    } catch (e: Exception) {
        Toast.makeText(
            context, context.getString(R.string.no_intent_handle), Toast.LENGTH_LONG
        ).show()
    }
}

@Preview
@Composable
private fun SettingsScreenLightPreview() {
    AppTheme(darkTheme = false) {
        SettingsContent(
            isDarkMode = false,
            onThemeSwitch = {},
            onPracticumOfferClick = {},
            onSendToHelpdeskClick = {},
            onShareAppClick = {},
            onBackClick = {})
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    SettingsContent(
        isDarkMode = true,
        onThemeSwitch = {},
        onPracticumOfferClick = {},
        onSendToHelpdeskClick = {},
        onShareAppClick = {},
        onBackClick = {})
}