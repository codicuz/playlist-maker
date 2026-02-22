package com.practicum.playlistmaker.presentation.playlist.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class)
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
                    onShowDeleteTrackDialog(event.track) {
                        viewModel.confirmDeleteTrack()
                    }
                }
                PlaylistUiEvent.ShowDeletePlaylistDialog -> {
                    state.playlist?.let { playlist ->
                        onShowDeletePlaylistDialog(playlist.title) {
                            viewModel.confirmDeletePlaylist()
                        }
                    }
                }
                is PlaylistUiEvent.ShowToast -> onShowToast(event.message)
                is PlaylistUiEvent.SharePlaylist -> onShareText(event.text)
                is PlaylistUiEvent.PlaylistUpdated -> {
                    // Обновляем данные плейлиста при изменении
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
                    .background(if (isDarkTheme) AppColors.Black else AppColors.White),
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
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlaylistHeader(
                        playlist = state.playlist,
                        isDarkTheme = isDarkTheme,
                        onBackClick = { viewModel.onBackClick() })

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

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.33f)
                        .align(Alignment.BottomCenter)
                        .background(
                            color = if (isDarkTheme) AppColors.Black else Color.White,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                        }) {
                    TrackBottomSheetContent(
                        tracks = state.tracks,
                        onTrackClick = { viewModel.onTrackClick(it) },
                        onTrackLongClick = { viewModel.onTrackLongClick(it) },
                        isDarkTheme = isDarkTheme,
                        resourceProvider = resourceProvider
                    )
                }

                if (showMenuDialog) {
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
                            viewModel.onDeletePlaylistClick()
                        },
                        isDarkTheme = isDarkTheme,
                        resourceProvider = resourceProvider
                    )
                }
            }
        }
    }
}


@Composable
fun TrackBottomSheetContent(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
    isDarkTheme: Boolean,
    resourceProvider: ResourceProvider
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(4.dp)
                    .background(
                        color = if (isDarkTheme) Color.White else AppColors.LightGray,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }

        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = resourceProvider.getString(R.string.empty_playlist_message),
                    style = AppTextStyles.MediaText,
                    color = if (isDarkTheme) Color.White else AppColors.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
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

@Composable
fun PlaylistHeader(
    playlist: Playlist?, isDarkTheme: Boolean, onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
    ) {
        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(playlist?.coverUri?.let {
            try {
                val file = File(it)
                if (file.exists()) file else R.drawable.ic_no_artwork_image
            } catch (e: Exception) {
                R.drawable.ic_no_artwork_image
            }
        } ?: R.drawable.ic_no_artwork_image).crossfade(true).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop)

        IconButton(
            onClick = onBackClick, modifier = Modifier
                .padding(start = 4.dp, top = 4.dp)
                .size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = stringResource(R.string.back),
                tint = if (isDarkTheme) Color.White else Color.Black
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
            text = playlist.title,
            fontSize = 24.sp,
            fontFamily = AppTextStyles.PlaylistTitle.fontFamily,
            color = if (isDarkTheme) Color.White else Color.Black
        )

        if (!playlist.description.isNullOrBlank()) {
            Text(
                text = playlist.description,
                fontSize = 18.sp,
                fontFamily = AppTextStyles.PlaylistText.fontFamily,
                color = if (isDarkTheme) Color.White else AppColors.Gray
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
                color = if (isDarkTheme) Color.White else AppColors.Gray
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = if (isDarkTheme) Color.White else AppColors.Gray,
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
                color = if (isDarkTheme) Color.White else AppColors.Gray
            )
        }

        Row(
            modifier = Modifier.padding(top = 8.dp)
        ) {
            IconButton(
                onClick = onShareClick, modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.share),
                    contentDescription = null,
                    tint = if (isDarkTheme) Color.White else AppColors.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = onMenuClick, modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.playlist_hamburger),
                    contentDescription = null,
                    tint = if (isDarkTheme) Color.White else AppColors.Gray
                )
            }
        }
    }
}

@Composable
fun TrackBottomSheet(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onTrackLongClick: (Track) -> Unit,
    isDarkTheme: Boolean,
    resourceProvider: ResourceProvider
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = if (isDarkTheme) AppColors.Black else Color.White,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(4.dp)
                        .background(
                            color = if (isDarkTheme) Color.White else AppColors.LightGray,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            if (tracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = resourceProvider.getString(R.string.empty_playlist_message),
                        style = AppTextStyles.MediaText,
                        color = if (isDarkTheme) Color.White else AppColors.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
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
@Composable
fun TrackItem(
    track: Track,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isDarkTheme: Boolean
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        )
        .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
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
                            color = AppColors.Gray, shape = RoundedCornerShape(2.dp)
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
            tint = AppColors.Gray
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
        skipPartiallyExpanded = false
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
                    .background(
                        color = if (isDarkTheme) Color.White else AppColors.LightGray,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }) {
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
                        } ?: R.drawable.ic_no_artwork_image).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        contentScale = ContentScale.Crop)

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
                text = resourceProvider.getString(R.string.share), onClick = {
                    onDismiss()
                    onShareClick()
                }, isDarkTheme = isDarkTheme
            )

            MenuItem(
                text = resourceProvider.getString(R.string.edit_information), onClick = {
                    onDismiss()
                    onEditClick()
                }, isDarkTheme = isDarkTheme
            )

            MenuItem(
                text = resourceProvider.getString(R.string.delete_playlist), onClick = {
                    onDismiss()
                    onDeleteClick()
                }, isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
fun MenuItem(
    text: String, onClick: () -> Unit, isDarkTheme: Boolean
) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontFamily = AppTextStyles.SettingsButtonText.fontFamily,
        color = if (isDarkTheme) Color.White else Color.Black,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 21.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Playlist Screen Light")
@Composable
fun PlaylistScreenLightPreview() {
    AppTheme(darkTheme = false) {
        PlaylistScreenPreview(
            isDarkTheme = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Playlist Screen Dark")
@Composable
fun PlaylistScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        PlaylistScreenPreview(
            isDarkTheme = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Playlist Screen Empty Light")
@Composable
fun PlaylistScreenEmptyLightPreview() {
    AppTheme(darkTheme = false) {
        PlaylistScreenPreview(
            isEmpty = true, isDarkTheme = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Playlist Screen Empty Dark")
@Composable
fun PlaylistScreenEmptyDarkPreview() {
    AppTheme(darkTheme = true) {
        PlaylistScreenPreview(
            isEmpty = true, isDarkTheme = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreenPreview(
    isEmpty: Boolean = false, isDarkTheme: Boolean = false
) {
    val mockPlaylist = if (!isEmpty) {
        Playlist(
            id = 1,
            title = "Мой любимый плейлист",
            description = "Лучшие треки для хорошего настроения",
            coverUri = null,
            tracksCount = mockTracks,
            trackCount = mockTracks.size
        )
    } else {
        Playlist(
            id = 1,
            title = "Пустой плейлист",
            description = "Здесь пока нет треков",
            coverUri = null,
            tracksCount = emptyList(),
            trackCount = 0
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AppColors.Black else AppColors.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PlaylistHeader(
                playlist = mockPlaylist, isDarkTheme = isDarkTheme, onBackClick = {})

            PlaylistInfo(
                playlist = mockPlaylist,
                trackCount = mockPlaylist.trackCount,
                totalDurationMinutes = if (!isEmpty) 45 else 0,
                isDarkTheme = isDarkTheme,
                onShareClick = {},
                onMenuClick = {},
                resourceProvider = object : ResourceProvider {
                    override fun getString(resId: Int): String = "Preview String"
                    override fun getString(resId: Int, vararg args: Any): String = "Preview String"
                    override fun getQuantityString(
                        resId: Int, quantity: Int, vararg args: Any
                    ): String = when (resId) {
                        R.plurals.tracks_count -> "$quantity треков"
                        R.plurals.tracks_minutes -> "$quantity минут"
                        else -> "Preview"
                    }
                })
        }

        TrackBottomSheet(
            tracks = if (!isEmpty) mockTracks else emptyList(),
            onTrackClick = {},
            onTrackLongClick = {},
            isDarkTheme = isDarkTheme,
            resourceProvider = object : ResourceProvider {
                override fun getString(resId: Int): String = "Preview String"
                override fun getString(resId: Int, vararg args: Any): String = "Preview String"
                override fun getQuantityString(
                    resId: Int, quantity: Int, vararg args: Any
                ): String = when (resId) {
                    R.plurals.tracks_count -> "$quantity треков"
                    R.plurals.tracks_minutes -> "$quantity минут"
                    else -> "Preview"
                }
            })
    }
}

val mockTracks = listOf(
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
    ), Track(
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
    ), Track(
        id = 3,
        trackId = 3,
        trackName = "Hotel California",
        artistsName = "Eagles",
        trackTimeMillis = 390000,
        artworkUrl100 = "",
        previewUrl = null,
        collectionName = "Hotel California",
        releaseDate = "1976-12-08",
        primaryGenreName = "Rock",
        country = "USA"
    ), Track(
        id = 4,
        trackId = 4,
        trackName = "Stairway to Heaven",
        artistsName = "Led Zeppelin",
        trackTimeMillis = 482000,
        artworkUrl100 = "",
        previewUrl = null,
        collectionName = "Led Zeppelin IV",
        releaseDate = "1971-11-08",
        primaryGenreName = "Rock",
        country = "UK"
    )
)