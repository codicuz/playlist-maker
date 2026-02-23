package com.practicum.playlistmaker.presentation.media.compose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.media.BasePlaylistViewModel
import com.practicum.playlistmaker.presentation.media.NewPlaylistViewModel
import com.practicum.playlistmaker.presentation.theme.compose.AppColors
import com.practicum.playlistmaker.presentation.theme.compose.AppTextStyles
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import com.practicum.playlistmaker.presentation.theme.compose.isDarkTheme
import com.practicum.playlistmaker.presentation.util.PermissionUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistScreen(
    viewModel: BasePlaylistViewModel,
    onNavigateBack: () -> Unit,
    onPlaylistCreated: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDarkTheme = isDarkTheme()

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), onResult = { uri ->
            uri?.let {
                viewModel.onCoverSelected(it)
            }
        })

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    fun handleCoverClick() {
        if (PermissionUtils.hasGalleryPermission(context)) {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } else {
            permissionLauncher.launch(PermissionUtils.getGalleryPermission())
        }
    }


    LaunchedEffect(state.success) {
        if (state.success) {
            onPlaylistCreated(state.title)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                Text(
                    text = if (viewModel is NewPlaylistViewModel) stringResource(R.string.new_playlist)
                    else stringResource(R.string.edit_playlist),
                    style = AppTextStyles.ActivityTitle
                )
            }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = stringResource(R.string.back),
                        tint = if (isDarkTheme) AppColors.White else AppColors.Black
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (isDarkTheme) AppColors.Black else AppColors.White,
                titleContentColor = if (isDarkTheme) AppColors.White else AppColors.Black,
                navigationIconContentColor = if (isDarkTheme) AppColors.White else AppColors.Black
            )
            )
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkTheme) AppColors.Black else AppColors.White)
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center), color = AppColors.Blue
                )
            } else {
                CreatePlaylistContent(
                    state = state,
                    onTitleChanged = { viewModel.onTitleChanged(it) },
                    onDescriptionChanged = { viewModel.onDescriptionChanged(it) },
                    onCoverClick = { handleCoverClick() },
                    onSaveClick = { viewModel.save() },
                    isSaving = state.isCreating,
                    saveButtonEnabled = state.isCreateEnabled && !state.isCreating,
                    saveButtonText = if (viewModel is NewPlaylistViewModel) stringResource(R.string.create_new_playlist_btn)
                    else stringResource(R.string.save)
                )
            }
        }
    }
}

@Composable
fun CreatePlaylistContent(
    state: com.practicum.playlistmaker.presentation.media.BasePlaylistScreenState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCoverClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaving: Boolean,
    saveButtonEnabled: Boolean,
    saveButtonText: String
) {
    val isDarkTheme = isDarkTheme()
    val scrollState = rememberScrollState()
    var title by remember { mutableStateOf(state.title) }
    var description by remember { mutableStateOf(state.description) }

    LaunchedEffect(state.title) {
        if (title != state.title) {
            title = state.title
        }
    }

    LaunchedEffect(state.description) {
        if (description != state.description) {
            description = state.description
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        PlaylistCoverImage(
            coverUri = state.coverUri,
            originalCoverUri = state.originalCoverUri,
            onClick = onCoverClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onCoverClick() })

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                onTitleChanged(it)
            },
            label = { Text(stringResource(R.string.pl_title)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = AppTextStyles.PlaylistText,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isDarkTheme) AppColors.White else AppColors.Gray,
                unfocusedBorderColor = if (isDarkTheme) AppColors.White else AppColors.Gray,
                focusedLabelColor = if (isDarkTheme) AppColors.White else AppColors.Gray,
                unfocusedLabelColor = if (isDarkTheme) AppColors.White else AppColors.Black,
                cursorColor = AppColors.Blue,
                focusedTextColor = if (isDarkTheme) AppColors.White else AppColors.Black,
                unfocusedTextColor = if (isDarkTheme) AppColors.White else AppColors.Black
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                onDescriptionChanged(it)
            },
            label = { Text(stringResource(R.string.pl_description)) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = AppTextStyles.PlaylistText,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isDarkTheme) AppColors.White else AppColors.Gray,
                unfocusedBorderColor = if (isDarkTheme) AppColors.White else AppColors.Gray,
                focusedLabelColor = if (isDarkTheme) AppColors.White else AppColors.Gray,
                unfocusedLabelColor = if (isDarkTheme) AppColors.White else AppColors.Black,
                cursorColor = AppColors.Blue,
                focusedTextColor = if (isDarkTheme) AppColors.White else AppColors.Black,
                unfocusedTextColor = if (isDarkTheme) AppColors.White else AppColors.Black
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            enabled = saveButtonEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Blue,
                contentColor = AppColors.White,
                disabledContainerColor = AppColors.Gray,
                disabledContentColor = AppColors.White

            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp), color = AppColors.White, strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = saveButtonText, style = AppTextStyles.ErrorText.copy(fontSize = 16.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PlaylistCoverImage(
    coverUri: Uri?, originalCoverUri: String?, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasCover = coverUri != null || !originalCoverUri.isNullOrEmpty()

    Box(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {
        if (hasCover) {
            val imageModel = remember(coverUri, originalCoverUri) {
                when {
                    coverUri != null -> coverUri
                    !originalCoverUri.isNullOrEmpty() -> {
                        try {
                            val file = File(originalCoverUri)
                            if (file.exists()) file else R.drawable.ic_no_artwork_image
                        } catch (e: Exception) {
                            R.drawable.ic_no_artwork_image
                        }
                    }

                    else -> R.drawable.ic_no_artwork_image
                }
            }

            AsyncImage(
                model = ImageRequest.Builder(context).data(imageModel).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_no_artwork_image),
                error = painterResource(R.drawable.ic_no_artwork_image)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .drawWithContent {
                        drawContent()
                        val pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(100f, 100f), phase = 0f
                        )
                        drawRoundRect(
                            color = AppColors.Gray, style = Stroke(
                                width = 2.dp.toPx(), pathEffect = pathEffect
                            ), cornerRadius = CornerRadius(8.dp.toPx())
                        )
                    }, contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.add_play_list_img),
                    contentDescription = "",
                    modifier = Modifier.size(80.dp),

                    )
            }
        }
    }
}

@Preview(showBackground = true, name = "New Playlist - Light")
@Composable
private fun NewPlaylistLightPreview() {
    AppTheme(darkTheme = false) {
        CreatePlaylistContent(
            state = com.practicum.playlistmaker.presentation.media.BasePlaylistScreenState(),
            onTitleChanged = {},
            onDescriptionChanged = {},
            onCoverClick = {},
            onSaveClick = {},
            isSaving = false,
            saveButtonEnabled = false,
            saveButtonText = "Создать"
        )
    }
}

@Preview(showBackground = true, name = "New Playlist - Light")
@Composable
private fun NewPlaylistLightPreviewSbTrue() {
    AppTheme(darkTheme = false) {
        CreatePlaylistContent(
            state = com.practicum.playlistmaker.presentation.media.BasePlaylistScreenState(),
            onTitleChanged = {},
            onDescriptionChanged = {},
            onCoverClick = {},
            onSaveClick = {},
            isSaving = false,
            saveButtonEnabled = true,
            saveButtonText = "Создать"
        )
    }
}

@Preview(showBackground = true, name = "New Playlist - Dark")
@Composable
private fun NewPlaylistDarkPreview() {
    AppTheme(darkTheme = true) {
        CreatePlaylistContent(
            state = com.practicum.playlistmaker.presentation.media.BasePlaylistScreenState(),
            onTitleChanged = {},
            onDescriptionChanged = {},
            onCoverClick = {},
            onSaveClick = {},
            isSaving = false,
            saveButtonEnabled = false,
            saveButtonText = "Создать"
        )
    }
}

@Preview(showBackground = true, name = "New Playlist - With Cover Light")
@Composable
private fun NewPlaylistWithCoverLightPreview() {
    AppTheme(darkTheme = false) {
        CreatePlaylistContent(
            state = com.practicum.playlistmaker.presentation.media.BasePlaylistScreenState(
            title = "Мой плейлист",
            description = "Описание плейлиста",
            coverUri = null,
            originalCoverUri = "test",
            isCreateEnabled = true
        ),
            onTitleChanged = {},
            onDescriptionChanged = {},
            onCoverClick = {},
            onSaveClick = {},
            isSaving = false,
            saveButtonEnabled = true,
            saveButtonText = "Создать"
        )
    }
}

@Preview(showBackground = true, name = "New Playlist - With Cover Dark")
@Composable
private fun NewPlaylistWithCoverDarkPreview() {
    AppTheme(darkTheme = true) {
        CreatePlaylistContent(
            state = com.practicum.playlistmaker.presentation.media.BasePlaylistScreenState(
            title = "Мой плейлист",
            description = "Описание плейлиста",
            coverUri = null,
            originalCoverUri = "test",
            isCreateEnabled = true
        ),
            onTitleChanged = {},
            onDescriptionChanged = {},
            onCoverClick = {},
            onSaveClick = {},
            isSaving = false,
            saveButtonEnabled = true,
            saveButtonText = "Создать"
        )
    }
}

@Preview(showBackground = true, name = "New Playlist - Filled Light")
@Composable
private fun NewPlaylistFilledLightPreview() {
    AppTheme(darkTheme = false) {
        CreatePlaylistContent(
            state = com.practicum.playlistmaker.presentation.media.BasePlaylistScreenState(
            title = "Мой классный плейлист",
            description = "Здесь собраны лучшие треки для отличного настроения",
            coverUri = null,
            originalCoverUri = null,
            isCreateEnabled = true
        ),
            onTitleChanged = {},
            onDescriptionChanged = {},
            onCoverClick = {},
            onSaveClick = {},
            isSaving = false,
            saveButtonEnabled = true,
            saveButtonText = "Создать"
        )
    }
}

@Preview(showBackground = true, name = "New Playlist - Filled Dark")
@Composable
private fun NewPlaylistFilledDarkPreview() {
    AppTheme(darkTheme = true) {
        CreatePlaylistContent(
            state = com.practicum.playlistmaker.presentation.media.BasePlaylistScreenState(
            title = "Мой классный плейлист",
            description = "Здесь собраны лучшие треки для отличного настроения",
            coverUri = null,
            originalCoverUri = null,
            isCreateEnabled = true
        ),
            onTitleChanged = {},
            onDescriptionChanged = {},
            onCoverClick = {},
            onSaveClick = {},
            isSaving = false,
            saveButtonEnabled = true,
            saveButtonText = "Создать"
        )
    }
}

@Preview(showBackground = true, name = "New Playlist - Saving Light")
@Composable
private fun NewPlaylistSavingLightPreview() {
    AppTheme(darkTheme = false) {
        CreatePlaylistContent(
            state = com.practicum.playlistmaker.presentation.media.BasePlaylistScreenState(
            title = "Сохраняемый плейлист",
            description = "Описание",
            coverUri = null,
            originalCoverUri = null,
            isCreateEnabled = true
        ),
            onTitleChanged = {},
            onDescriptionChanged = {},
            onCoverClick = {},
            onSaveClick = {},
            isSaving = true,
            saveButtonEnabled = false,
            saveButtonText = "Создать"
        )
    }
}

@Preview(showBackground = true, name = "New Playlist - Saving Dark")
@Composable
private fun NewPlaylistSavingDarkPreview() {
    AppTheme(darkTheme = true) {
        CreatePlaylistContent(
            state = com.practicum.playlistmaker.presentation.media.BasePlaylistScreenState(
            title = "Сохраняемый плейлист",
            description = "Описание",
            coverUri = null,
            originalCoverUri = null,
            isCreateEnabled = true
        ),
            onTitleChanged = {},
            onDescriptionChanged = {},
            onCoverClick = {},
            onSaveClick = {},
            isSaving = true,
            saveButtonEnabled = false,
            saveButtonText = "Создать"
        )
    }
}