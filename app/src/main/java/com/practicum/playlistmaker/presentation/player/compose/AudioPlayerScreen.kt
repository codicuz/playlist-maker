package com.practicum.playlistmaker.presentation.player.compose

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.player.AddTrackStatus
import com.practicum.playlistmaker.presentation.player.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.theme.compose.AppColors
import com.practicum.playlistmaker.presentation.theme.compose.AppTextStyles
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import com.practicum.playlistmaker.presentation.theme.compose.isDarkTheme
import com.practicum.playlistmaker.presentation.util.AndroidResourceProvider
import com.practicum.playlistmaker.presentation.util.ResourceProvider
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    viewModel: AudioPlayerViewModel,
    trackId: Int?,
    onNavigateBack: () -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    val context = LocalContext.current
    val resourceProvider = remember(context) {
        AndroidResourceProvider(context)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val addTrackStatus by viewModel.addTrackStatus.collectAsStateWithLifecycle(initialValue = null)
    val playlists by viewModel.playlists.collectAsStateWithLifecycle(initialValue = emptyList())
    val shouldCloseSheet by viewModel.shouldCloseBottomSheet.collectAsStateWithLifecycle(
        initialValue = null
    )

    val isDarkTheme = isDarkTheme()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(trackId) {
        if (trackId != null) {
            viewModel.loadTrack(trackId)
        }
    }

    LaunchedEffect(addTrackStatus) {
        val status = addTrackStatus
        when (status) {
            is AddTrackStatus.Success -> {
                Toast.makeText(
                    context,
                    resourceProvider.getString(R.string.added_to_playlist, status.playlistName),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetAddTrackStatus()
            }
            is AddTrackStatus.AlreadyExists -> {
                Toast.makeText(
                    context,
                    resourceProvider.getString(R.string.allready_exists_in_playlist, status.playlistName),
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetAddTrackStatus()
            }
            is AddTrackStatus.Error -> {
                Toast.makeText(
                    context, status.message, Toast.LENGTH_SHORT
                ).show()
                viewModel.resetAddTrackStatus()
            }
            else -> {}
        }
    }

    LaunchedEffect(shouldCloseSheet) {
        if (shouldCloseSheet == true) {
            showBottomSheet = false
            viewModel.resetShouldCloseBottomSheet()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AppColors.Black else AppColors.White)
    ) {
        AudioPlayerContent(
            track = state.track,
            isPlaying = state.isPlaying,
            currentPosition = state.currentPosition,
            isFavorite = state.isFavorite,
            isDarkTheme = isDarkTheme,
            resourceProvider = resourceProvider,
            onBackClick = onNavigateBack,
            onPlayPauseClick = {
                if (state.isPlaying) {
                    viewModel.pausePlayer()
                } else {
                    viewModel.startPlayer()
                }
            },
            onFavoriteClick = { viewModel.toggleFavorite() },
            onAddToPlaylistClick = { showBottomSheet = true }
        )

        if (showBottomSheet) {
            PlaylistBottomSheet(
                playlists = playlists,
                addTrackStatus = addTrackStatus,
                isDarkTheme = isDarkTheme,
                resourceProvider = resourceProvider,
                onPlaylistClick = { playlist ->
                    state.track?.let { track ->
                        viewModel.addTrackToPlaylist(playlist, track)
                    }
                },
                onCreateNewClick = {
                    showBottomSheet = false
                    onCreatePlaylistClick()
                },
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

@Composable
fun AudioPlayerContent(
    track: Track?,
    isPlaying: Boolean,
    currentPosition: Int,
    isFavorite: Boolean,
    isDarkTheme: Boolean,
    resourceProvider: ResourceProvider,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        IconButton(
            onClick = onBackClick, modifier = Modifier
                .padding(start = 4.dp, top = 4.dp)
                .size(48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = resourceProvider.getString(R.string.back),
                tint = if (isDarkTheme) AppColors.White else AppColors.Black
            )
        }

        track?.let { track ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(track.getConvertArtwork())
                    .crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_no_artwork_image),
                error = painterResource(R.drawable.ic_no_artwork_image)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = track.trackName ?: "-",
                style = AppTextStyles.PlayerMainText,
                color = if (isDarkTheme) AppColors.White else AppColors.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = track.artistsName ?: "-",
                style = AppTextStyles.PlayerArtistName,
                color = if (isDarkTheme) AppColors.White else AppColors.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onAddToPlaylistClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(52.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.btn_aud_add_track),
                            contentDescription = resourceProvider.getString(R.string.add_to_playlist),
                            tint = if (isDarkTheme) AppColors.White else AppColors.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.Center)
                        .clickable { onPlayPauseClick() }, contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            if (isPlaying) R.drawable.btn_aud_pause
                            else R.drawable.btn_aud_play
                        ),
                        contentDescription = if (isPlaying)
                            resourceProvider.getString(R.string.pause)
                        else
                            resourceProvider.getString(R.string.play),
                        modifier = Modifier.size(100.dp)
                    )
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(52.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppColors.Gray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isFavorite) R.drawable.btn_aud_like_true
                                else R.drawable.btn_aud_like_false
                            ),
                            contentDescription = resourceProvider.getString(R.string.add_to_favorites),
                            tint = if (isFavorite && !isDarkTheme) AppColors.Red else Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatTime(currentPosition),
                style = AppTextStyles.PlayerDetailsValue,
                color = if (isDarkTheme) AppColors.White else AppColors.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            TrackDetails(
                track = track,
                isDarkTheme = isDarkTheme,
                resourceProvider = resourceProvider
            )
        }
    }
}

@Composable
fun TrackDetails(
    track: Track,
    isDarkTheme: Boolean,
    resourceProvider: ResourceProvider
) {
    val textColor = if (isDarkTheme) AppColors.White else AppColors.Black
    val valueColor = if (isDarkTheme) AppColors.White else AppColors.Gray

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        DetailRow(
            label = resourceProvider.getString(R.string.play_time),
            value = track.trackTime ?: "-",
            textColor = textColor,
            valueColor = valueColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailRow(
            label = resourceProvider.getString(R.string.aud_album),
            value = track.collectionName ?: "-",
            textColor = textColor,
            valueColor = valueColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailRow(
            label = resourceProvider.getString(R.string.aud_year),
            value = track.releaseYear ?: "-",
            textColor = textColor,
            valueColor = valueColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailRow(
            label = resourceProvider.getString(R.string.aud_genre),
            value = track.primaryGenreName ?: "-",
            textColor = textColor,
            valueColor = valueColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailRow(
            label = resourceProvider.getString(R.string.aud_country),
            value = track.country ?: "-",
            textColor = textColor,
            valueColor = valueColor
        )
    }
}

@Composable
fun DetailRow(
    label: String, value: String, textColor: Color, valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label, style = AppTextStyles.PlayerDetailsValue, color = textColor
        )
        Text(
            text = value,
            style = AppTextStyles.PlayerDetailsValue,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistBottomSheet(
    playlists: List<com.practicum.playlistmaker.domain.playlist.Playlist>,
    addTrackStatus: AddTrackStatus?,
    isDarkTheme: Boolean,
    resourceProvider: ResourceProvider,
    onPlaylistClick: (com.practicum.playlistmaker.domain.playlist.Playlist) -> Unit,
    onCreateNewClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false // Изменено на false, чтобы можно было разворачивать
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDarkTheme) AppColors.Black else Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            ) {
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(4.dp) // Уменьшена высота для более аккуратного вида
                        .background(
                            color = if (isDarkTheme) Color.White else AppColors.LightGray,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = resourceProvider.getString(R.string.add_to_playlist),
                style = AppTextStyles.BottomSheetTitle,
                color = if (isDarkTheme) Color.White else Color.Black,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .padding(top = 32.dp)
            )

            Button(
                onClick = onCreateNewClick,
                modifier = Modifier
                    .height(42.dp),
                shape = RoundedCornerShape(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkTheme) AppColors.White else AppColors.Black,
                    contentColor = if (isDarkTheme) AppColors.Black else AppColors.White
                )
            ) {
                Text(
                    text = resourceProvider.getString(R.string.new_playlist),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (playlists.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(playlists) { playlist ->
                        PlaylistBottomSheetItem(
                            playlist = playlist,
                            isDarkTheme = isDarkTheme,
                            resourceProvider = resourceProvider,
                            onClick = { onPlaylistClick(playlist) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PlaylistBottomSheetItem(
    playlist: com.practicum.playlistmaker.domain.playlist.Playlist,
    isDarkTheme: Boolean,
    resourceProvider: ResourceProvider,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var imageModel by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(playlist.coverUri) {
        imageModel = if (!playlist.coverUri.isNullOrEmpty()) {
            try {
                val file = java.io.File(playlist.coverUri)
                if (file.exists()) file else R.drawable.ic_no_artwork_image
            } catch (e: Exception) {
                R.drawable.ic_no_artwork_image
            }
        } else {
            R.drawable.ic_no_artwork_image
        }
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(imageModel).crossfade(true).build(),
            contentDescription = null,
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_no_artwork_image),
            error = painterResource(R.drawable.ic_no_artwork_image)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = playlist.title,
                style = AppTextStyles.TrackTitle,
                color = if (isDarkTheme) Color.White else Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = resourceProvider.getQuantityString(
                    R.plurals.tracks_count, playlist.trackCount, playlist.trackCount
                ),
                style = AppTextStyles.TrackArtistTime,
                color = if (isDarkTheme) Color.White else AppColors.Gray
            )
        }
    }
}

private fun formatTime(ms: Int): String {
    val seconds = ms / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Preview(showBackground = true, name = "Audio Player Light")
@Composable
fun AudioPlayerScreenLightPreview() {
    val track = Track(
        id = 1,
        trackId = 1,
        trackName = "Yesterday (Remastered 2009)",
        artistsName = "The Beatles",
        trackTimeMillis = 125000,
        artworkUrl100 = "",
        previewUrl = null,
        collectionName = "Yesterday (Remastered 2009)",
        releaseDate = "1965-08-06",
        primaryGenreName = "Rock",
        country = "UK"
    )

    val resourceProvider = object : ResourceProvider {
        override fun getString(resId: Int): String = "Строка $resId"
        override fun getString(resId: Int, vararg args: Any): String = "Строка $resId"
        override fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String =
            "Количество: $quantity"
    }

    AppTheme(darkTheme = false) {
        AudioPlayerContent(
            track = track,
            isPlaying = false,
            currentPosition = 30000,
            isFavorite = false,
            isDarkTheme = false,
            resourceProvider = resourceProvider,
            onBackClick = {},
            onPlayPauseClick = {},
            onFavoriteClick = {},
            onAddToPlaylistClick = {})
    }
}

@Preview(showBackground = true, name = "Audio Player Dark")
@Composable
fun AudioPlayerScreenDarkPreview() {
    val track = Track(
        id = 1,
        trackId = 1,
        trackName = "Yesterday (Remastered 2009)",
        artistsName = "The Beatles",
        trackTimeMillis = 125000,
        artworkUrl100 = "",
        previewUrl = null,
        collectionName = "Yesterday (Remastered 2009)",
        releaseDate = "1965-08-06",
        primaryGenreName = "Rock",
        country = "UK"
    )

    val resourceProvider = object : ResourceProvider {
        override fun getString(resId: Int): String = "Строка $resId"
        override fun getString(resId: Int, vararg args: Any): String = "Строка $resId"
        override fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String =
            "Количество: $quantity"
    }

    AppTheme(darkTheme = true) {
        AudioPlayerContent(
            track = track,
            isPlaying = false,
            currentPosition = 30000,
            isFavorite = false,
            isDarkTheme = true,
            resourceProvider = resourceProvider,
            onBackClick = {},
            onPlayPauseClick = {},
            onFavoriteClick = {},
            onAddToPlaylistClick = {})
    }
}