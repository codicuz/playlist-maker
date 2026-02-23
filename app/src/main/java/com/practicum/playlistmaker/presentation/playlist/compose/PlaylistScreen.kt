package com.practicum.playlistmaker.presentation.playlist.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.playlist.PlaylistUiEvent
import com.practicum.playlistmaker.presentation.playlist.PlaylistViewModel
import com.practicum.playlistmaker.presentation.theme.compose.AppColors
import com.practicum.playlistmaker.presentation.theme.compose.AppTextStyles
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import com.practicum.playlistmaker.presentation.theme.compose.isDarkTheme
import com.practicum.playlistmaker.presentation.util.ResourceProvider
import java.io.File
import kotlin.math.abs
@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel,
    playlistId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (Track) -> Unit,
    onNavigateToEditPlaylist: (Long) -> Unit,
    onShareText: (String) -> Unit,
    onShowDeleteTrackDialog: (Track, () -> Unit) -> Unit,
    onShowDeletePlaylistDialog: (String, () -> Unit) -> Unit,
    onShowToast: (String) -> Unit,
    resourceProvider: ResourceProvider
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDarkTheme = isDarkTheme()
    var showMenuDialog by remember { mutableStateOf(false) }

    // Состояния для диалогов
    var showDeleteTrackDialog by remember { mutableStateOf(false) }
    var trackToDelete by remember { mutableStateOf<Track?>(null) }
    var showDeletePlaylistDialog by remember { mutableStateOf(false) }

    LaunchedEffect(playlistId) {
        viewModel.setPlaylistId(playlistId)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                PlaylistUiEvent.NavigateBack -> onNavigateBack()
                is PlaylistUiEvent.NavigateToPlayer -> onNavigateToPlayer(event.track)
                is PlaylistUiEvent.NavigateToEditPlaylist -> onNavigateToEditPlaylist(event.playlistId)
                is PlaylistUiEvent.ShowDeleteTrackDialog -> {
                    trackToDelete = event.track
                    showDeleteTrackDialog = true
                }
                PlaylistUiEvent.ShowDeletePlaylistDialog -> {
                    showDeletePlaylistDialog = true
                }
                is PlaylistUiEvent.ShowToast -> onShowToast(event.message)
                is PlaylistUiEvent.SharePlaylist -> onShareText(event.text)
                is PlaylistUiEvent.PlaylistUpdated -> {
                    viewModel.loadPlaylist()
                }
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            onShowToast(it)
            viewModel.clearError()
        }
    }

    AppTheme {
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Blue)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDarkTheme) AppColors.Black else AppColors.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(AppColors.White)
                ) {
                    PlaylistHeader(
                        playlist = state.playlist,
                        isDarkTheme = isDarkTheme,
                        onBackClick = { viewModel.onBackClick() }
                    )

                    state.playlist?.let { playlist ->
                        PlaylistInfo(
                            playlist = playlist,
                            trackCount = state.trackCount,
                            totalDurationMinutes = state.totalDurationMinutes,
                            isDarkTheme = isDarkTheme,
                            onShareClick = { viewModel.onShareClick() },
                            onMenuClick = { showMenuDialog = true },
                            resourceProvider = resourceProvider
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                CustomBottomSheet(
                    tracks = state.tracks,
                    onTrackClick = { viewModel.onTrackClick(it) },
                    onTrackLongClick = { viewModel.onTrackLongClick(it) },
                    isDarkTheme = isDarkTheme,
                    resourceProvider = resourceProvider
                )

                if (showMenuDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { showMenuDialog = false }
                    )
                    PlaylistMenuSheet(
                        playlist = state.playlist,
                        onDismiss = { showMenuDialog = false },
                        onShareClick = {
                            showMenuDialog = false
                            viewModel.onShareClick()
                        },
                        onEditClick = {
                            showMenuDialog = false
                            viewModel.onEditClick()
                        },
                        onDeleteClick = {
                            showMenuDialog = false
                            showDeletePlaylistDialog = true
                        },
                        isDarkTheme = isDarkTheme,
                        resourceProvider = resourceProvider
                    )
                }

                // Диалог подтверждения удаления трека
                if (showDeleteTrackDialog && trackToDelete != null) {
                    DeleteTrackDialog(
                        track = trackToDelete!!,
                        onDismiss = { showDeleteTrackDialog = false },
                        onConfirm = {
                            viewModel.confirmDeleteTrack()
                            showDeleteTrackDialog = false
                            trackToDelete = null
                        },
                        resourceProvider = resourceProvider
                    )
                }

                // Диалог подтверждения удаления плейлиста
                if (showDeletePlaylistDialog && state.playlist != null) {
                    DeletePlaylistDialog(
                        playlistName = state.playlist!!.title,
                        onDismiss = { showDeletePlaylistDialog = false },
                        onConfirm = {
                            viewModel.confirmDeletePlaylist()
                            showDeletePlaylistDialog = false
                        },
                        resourceProvider = resourceProvider
                    )
                }
            }
        }
    }
}
@Composable
fun CustomBottomSheet(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
    isDarkTheme: Boolean,
    resourceProvider: ResourceProvider
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val collapsedHeight = screenHeight / 3
    val expandedHeight = screenHeight

    var sheetHeight by remember { mutableStateOf(collapsedHeight) }

    val density = LocalDensity.current

    val draggableState = rememberDraggableState { delta ->
        val newHeight = sheetHeight - with(density) { delta.toDp() }

        sheetHeight = newHeight.coerceIn(
            collapsedHeight,
            expandedHeight
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .align(Alignment.BottomCenter)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = draggableState,
                    onDragStopped = {
                        sheetHeight =
                            if (sheetHeight > screenHeight / 2)
                                expandedHeight
                            else
                                collapsedHeight
                    }
                )
                .background(
                    color = if (isDarkTheme) AppColors.Black else Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(4.dp)
                            .background(
                                if (isDarkTheme) Color.White else AppColors.LightGray,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }

                if (tracks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = resourceProvider.getString(R.string.empty_playlist_message),
                            color = if (isDarkTheme) Color.White else AppColors.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tracks) { track ->
                            TrackItem(
                                track = track,
                                onClick = { onTrackClick(track) },
                                onLongClick = { onTrackLongClick(track) },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistHeader(
    playlist: Playlist?,
    isDarkTheme: Boolean,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(playlist?.coverUri?.let {
                    try {
                        val file = File(it)
                        if (file.exists()) file else R.drawable.ic_no_artwork_image
                    } catch (e: Exception) {
                        R.drawable.ic_no_artwork_image
                    }
                } ?: R.drawable.ic_no_artwork_image)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_no_artwork_image),
            error = painterResource(R.drawable.ic_no_artwork_image)
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(start = 4.dp, top = 4.dp)
                .size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = stringResource(R.string.back),
                tint = Color.Black
            )
        }
    }
}

@Composable
fun PlaylistInfo(
    playlist: Playlist,
    trackCount: Int,
    totalDurationMinutes: Long,
    isDarkTheme: Boolean,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    resourceProvider: ResourceProvider
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 20.dp, bottom = 4.dp),
            text = playlist.title,
            fontSize = 24.sp,
            fontFamily = AppTextStyles.PlaylistTitle.fontFamily,
            color = AppColors.Black
        )

        if (!playlist.description.isNullOrBlank()) {
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = playlist.description,
                fontSize = 18.sp,
                fontFamily = AppTextStyles.PlaylistText.fontFamily,
                color = AppColors.Black
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (totalDurationMinutes == 0L) {
                    resourceProvider.getString(R.string.zero_minutes)
                } else {
                    resourceProvider.getQuantityString(
                        R.plurals.tracks_minutes, totalDurationMinutes.toInt(), totalDurationMinutes
                    )
                },
                fontSize = 18.sp,
                fontFamily = AppTextStyles.PlaylistText.fontFamily,
                color = AppColors.Black
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = AppColors.Black,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = resourceProvider.getQuantityString(
                    R.plurals.tracks_count, trackCount, trackCount
                ),
                fontSize = 18.sp,
                fontFamily = AppTextStyles.PlaylistText.fontFamily,
                color = AppColors.Black
            )
        }

        Row(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            IconButton(
                onClick = onShareClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.share),
                    contentDescription = null,
                    tint = AppColors.Black
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.playlist_hamburger),
                    contentDescription = null,
                    tint = AppColors.Black
                )
            }
        }
    }
}

@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.artworkUrl100,
            contentDescription = null,
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(2.dp)),
            placeholder = painterResource(R.drawable.ic_no_artwork_image),
            error = painterResource(R.drawable.ic_no_artwork_image)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = track.trackName ?: "-",
                fontSize = 16.sp,
                fontFamily = AppTextStyles.TrackTitle.fontFamily,
                color = if (isDarkTheme) Color.White else Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(1.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.artistsName ?: "-",
                    fontSize = 11.sp,
                    fontFamily = AppTextStyles.TrackArtistTime.fontFamily,
                    color = if (isDarkTheme) Color.White else AppColors.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = if (isDarkTheme) AppColors.White else AppColors.Gray,
                            shape = RoundedCornerShape(2.dp)
                        )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = track.trackTime ?: "-",
                    fontSize = 11.sp,
                    fontFamily = AppTextStyles.TrackArtistTime.fontFamily,
                    color = if (isDarkTheme) Color.White else AppColors.Gray,
                    maxLines = 1
                )
            }
        }

        Icon(
            painter = painterResource(R.drawable.agreement),
            contentDescription = null,
            modifier = Modifier.size(8.dp, 14.dp),
            tint = if (isDarkTheme) AppColors.White else AppColors.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistMenuSheet(
    playlist: Playlist?,
    onDismiss: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDarkTheme: Boolean,
    resourceProvider: ResourceProvider
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDarkTheme) AppColors.Black else Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(4.dp)
                    .padding(top = 8.dp)
                    .background(
                        color = if (isDarkTheme) Color.White else AppColors.LightGray,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.53f)
                .padding(top = 12.dp)
        ) {
            playlist?.let { pl ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(pl.coverUri?.let {
                                try {
                                    val file = File(it)
                                    if (file.exists()) file else R.drawable.ic_no_artwork_image
                                } catch (e: Exception) {
                                    R.drawable.ic_no_artwork_image
                                }
                            } ?: R.drawable.ic_no_artwork_image)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.ic_no_artwork_image),
                        error = painterResource(R.drawable.ic_no_artwork_image)
                    )

                    Spacer(modifier = Modifier.width(13.dp))

                    Column {
                        Text(
                            text = pl.title,
                            fontSize = 16.sp,
                            fontFamily = AppTextStyles.TrackTitle.fontFamily,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = resourceProvider.getQuantityString(
                                R.plurals.tracks_count, pl.trackCount, pl.trackCount
                            ),
                            fontSize = 11.sp,
                            fontFamily = AppTextStyles.TrackArtistTime.fontFamily,
                            color = if (isDarkTheme) Color.White else AppColors.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            MenuItem(
                text = resourceProvider.getString(R.string.share),
                onClick = {
                    onDismiss()
                    onShareClick()
                },
                isDarkTheme = isDarkTheme
            )

            MenuItem(
                text = resourceProvider.getString(R.string.edit_information),
                onClick = {
                    onDismiss()
                    onEditClick()
                },
                isDarkTheme = isDarkTheme
            )

            MenuItem(
                text = resourceProvider.getString(R.string.delete_playlist),
                onClick = {
                    onDismiss()
                    onDeleteClick()
                },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
fun MenuItem(
    text: String,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontFamily = AppTextStyles.SettingsButtonText.fontFamily,
        color = if (isDarkTheme) Color.White else Color.Black,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 21.dp)
    )
}

private val previewTracks = listOf(
    Track(
        id = 1,
        trackId = 1,
        trackName = "Bohemian Rhapsody",
        artistsName = "Queen",
        trackTimeMillis = 354000,
        artworkUrl100 = "",
        previewUrl = null,
        collectionName = "A Night at the Opera",
        releaseDate = "1975-10-31",
        primaryGenreName = "Rock",
        country = "UK"
    ),
    Track(
        id = 2,
        trackId = 2,
        trackName = "Imagine",
        artistsName = "John Lennon",
        trackTimeMillis = 183000,
        artworkUrl100 = "",
        previewUrl = null,
        collectionName = "Imagine",
        releaseDate = "1971-09-09",
        primaryGenreName = "Rock",
        country = "UK"
    )
)


@Composable
fun DeleteTrackDialog(
    track: Track,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    resourceProvider: ResourceProvider
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(4.dp),
        title = {
            Text(
                text = resourceProvider.getString(R.string.delete_track_title),
                style = AppTextStyles.BottomSheetTitle
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onConfirm
            ) {
                Text(resourceProvider.getString(R.string.yes))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss
            ) {
                Text(resourceProvider.getString(R.string.no))
            }
        }
    )
}

@Composable
fun DeletePlaylistDialog(
    playlistName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    resourceProvider: ResourceProvider
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(4.dp),
        title = {
            Text(
                text = resourceProvider.getString(R.string.delete_playlist_title, playlistName),
                style = AppTextStyles.BottomSheetTitle
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = onConfirm
            ) {
                Text(resourceProvider.getString(R.string.yes))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss
            ) {
                Text(resourceProvider.getString(R.string.no))
            }
        }
    )
}

@Preview(
    name = "Delete Track Dialog - Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 300,
    widthDp = 360
)
@Composable
fun DeleteTrackDialogLightPreview() {
    val track = Track(
        id = 1,
        trackId = 1,
        trackName = "Bohemian Rhapsody",
        artistsName = "Queen",
        trackTimeMillis = 354000,
        artworkUrl100 = "",
        previewUrl = null,
        collectionName = "A Night at the Opera",
        releaseDate = "1975-10-31",
        primaryGenreName = "Rock",
        country = "UK"
    )

    val resourceProvider = object : ResourceProvider {
        override fun getString(resId: Int): String = when (resId) {
            R.string.delete_track_title -> "Удалить трек"
            R.string.yes -> "Да"
            R.string.no -> "Нет"
            else -> "Строка $resId"
        }
        override fun getString(resId: Int, vararg args: Any): String = when (resId) {
            R.string.delete_track_title -> "Удалить трек \"${args[0]}\"?"
            else -> "Строка $resId"
        }
        override fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String = "Количество: $quantity"
    }

    AppTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DeleteTrackDialog(
                track = track,
                onDismiss = {},
                onConfirm = {},
                resourceProvider = resourceProvider
            )
        }
    }
}

@Preview(
    name = "Delete Track Dialog - Dark",
    showBackground = true,
    backgroundColor = 0xFF1A1B22,
    heightDp = 300,
    widthDp = 360
)
@Composable
fun DeleteTrackDialogDarkPreview() {
    val track = Track(
        id = 1,
        trackId = 1,
        trackName = "Bohemian Rhapsody",
        artistsName = "Queen",
        trackTimeMillis = 354000,
        artworkUrl100 = "",
        previewUrl = null,
        collectionName = "A Night at the Opera",
        releaseDate = "1975-10-31",
        primaryGenreName = "Rock",
        country = "UK"
    )

    val resourceProvider = object : ResourceProvider {
        override fun getString(resId: Int): String = when (resId) {
            R.string.delete_track_title -> "Удалить трек"
            R.string.yes -> "Да"
            R.string.no -> "Нет"
            else -> "Строка $resId"
        }
        override fun getString(resId: Int, vararg args: Any): String = when (resId) {
            R.string.delete_track_title -> "Удалить трек \"${args[0]}\"?"
            else -> "Строка $resId"
        }
        override fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String = "Количество: $quantity"
    }

    AppTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Black)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DeleteTrackDialog(
                track = track,
                onDismiss = {},
                onConfirm = {},
                resourceProvider = resourceProvider
            )
        }
    }
}

@Preview(
    name = "Delete Playlist Dialog - Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    heightDp = 300,
    widthDp = 360
)
@Composable
fun DeletePlaylistDialogLightPreview() {
    val resourceProvider = object : ResourceProvider {
        override fun getString(resId: Int): String = when (resId) {
            R.string.delete_playlist_title -> "Удалить плейлист"
            R.string.yes -> "Да"
            R.string.no -> "Нет"
            else -> "Строка $resId"
        }
        override fun getString(resId: Int, vararg args: Any): String = when (resId) {
            R.string.delete_playlist_title -> "Удалить плейлист \"${args[0]}\"?"
            else -> "Строка $resId"
        }
        override fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String = "Количество: $quantity"
    }

    AppTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DeletePlaylistDialog(
                playlistName = "Мой любимый плейлист",
                onDismiss = {},
                onConfirm = {},
                resourceProvider = resourceProvider
            )
        }
    }
}

@Preview(
    name = "Delete Playlist Dialog - Dark",
    showBackground = true,
    backgroundColor = 0xFF1A1B22,
    heightDp = 300,
    widthDp = 360
)
@Composable
fun DeletePlaylistDialogDarkPreview() {
    val resourceProvider = object : ResourceProvider {
        override fun getString(resId: Int): String = when (resId) {
            R.string.delete_playlist_title -> "Удалить плейлист"
            R.string.yes -> "Да"
            R.string.no -> "Нет"
            else -> "Строка $resId"
        }
        override fun getString(resId: Int, vararg args: Any): String = when (resId) {
            R.string.delete_playlist_title -> "Удалить плейлист \"${args[0]}\"?"
            else -> "Строка $resId"
        }
        override fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String = "Количество: $quantity"
    }

    AppTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Black)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            DeletePlaylistDialog(
                playlistName = "Мой любимый плейлист",
                onDismiss = {},
                onConfirm = {},
                resourceProvider = resourceProvider
            )
        }
    }
}

// Комбинированный превью для всех диалогов сразу
@Preview(
    name = "All Dialogs Preview",
    showBackground = true,
    heightDp = 800,
    widthDp = 400
)
@Composable
fun AllDialogsPreview() {
    val track = Track(
        id = 1,
        trackId = 1,
        trackName = "Bohemian Rhapsody",
        artistsName = "Queen",
        trackTimeMillis = 354000,
        artworkUrl100 = "",
        previewUrl = null,
        collectionName = "A Night at the Opera",
        releaseDate = "1975-10-31",
        primaryGenreName = "Rock",
        country = "UK"
    )

    val resourceProvider = object : ResourceProvider {
        override fun getString(resId: Int): String = when (resId) {
            R.string.delete_track_title -> "Удалить трек"
            R.string.delete_playlist_title -> "Удалить плейлист"
            R.string.yes -> "Да"
            R.string.no -> "Нет"
            else -> "Строка $resId"
        }
        override fun getString(resId: Int, vararg args: Any): String = when (resId) {
            R.string.delete_track_title -> "Удалить трек \"${args[0]}\"?"
            R.string.delete_playlist_title -> "Удалить плейлист \"${args[0]}\"?"
            else -> "Строка $resId"
        }
        override fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String = "Количество: $quantity"
    }

    Column {
        AppTheme(darkTheme = false) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Light Theme",
                    style = AppTextStyles.ActivityTitle,
                    color = AppColors.Black,
                    modifier = Modifier.padding(8.dp)
                )

                DeleteTrackDialog(
                    track = track,
                    onDismiss = {},
                    onConfirm = {},
                    resourceProvider = resourceProvider
                )

                Spacer(modifier = Modifier.height(16.dp))

                DeletePlaylistDialog(
                    playlistName = "Мой любимый плейлист",
                    onDismiss = {},
                    onConfirm = {},
                    resourceProvider = resourceProvider
                )
            }
        }

        AppTheme(darkTheme = true) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Black)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Dark Theme",
                    style = AppTextStyles.ActivityTitle,
                    color = AppColors.White,
                    modifier = Modifier.padding(8.dp)
                )

                DeleteTrackDialog(
                    track = track,
                    onDismiss = {},
                    onConfirm = {},
                    resourceProvider = resourceProvider
                )

                Spacer(modifier = Modifier.height(16.dp))

                DeletePlaylistDialog(
                    playlistName = "Мой любимый плейлист",
                    onDismiss = {},
                    onConfirm = {},
                    resourceProvider = resourceProvider
                )
            }
        }
    }
}