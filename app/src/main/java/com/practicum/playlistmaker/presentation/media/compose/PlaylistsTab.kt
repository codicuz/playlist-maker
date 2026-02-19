package com.practicum.playlistmaker.presentation.media.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.media.PlaylistsViewModel
import com.practicum.playlistmaker.presentation.theme.compose.AppColors
import com.practicum.playlistmaker.presentation.theme.compose.AppTextStyles
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import com.practicum.playlistmaker.presentation.theme.compose.isDarkTheme
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun PlaylistsTab(
    viewModel: PlaylistsViewModel = koinViewModel(),
    onPlaylistClick: (Long) -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle(initialValue = emptyList())

    PlaylistsContent(
        playlists = playlists,
        onPlaylistClick = onPlaylistClick,
        onCreatePlaylistClick = onCreatePlaylistClick
    )
}

@Composable
fun PlaylistsContent(
    playlists: List<Playlist>, onPlaylistClick: (Long) -> Unit, onCreatePlaylistClick: () -> Unit
) {
    val isDarkTheme = isDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AppColors.Black else AppColors.White)
    ) {
        Button(
            onClick = onCreatePlaylistClick,
            modifier = Modifier
                .padding(top = 24.dp, bottom = 46.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDarkTheme) AppColors.White else AppColors.Black,
                contentColor = if (isDarkTheme) AppColors.Black else AppColors.White
            )
        ) {
            Text(
                text = stringResource(R.string.new_playlist),
                style = AppTextStyles.MediaText.copy(fontSize = 14.sp)
            )
        }

        if (playlists.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.not_found),
                    contentDescription = stringResource(R.string.not_found),
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.empty_playlists),
                    style = AppTextStyles.MediaText,
                    color = if (isDarkTheme) AppColors.White else AppColors.Black,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistGridItem(
                        playlist = playlist, onClick = { onPlaylistClick(playlist.id) })
                }
            }
        }
    }
}

@Composable
fun PlaylistGridItem(
    playlist: Playlist, onClick: () -> Unit
) {
    val isDarkTheme = isDarkTheme()
    val context = LocalContext.current
    var imageModel by remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(playlist.coverUri) {
        imageModel = if (!playlist.coverUri.isNullOrEmpty()) {
            try {
                val file = File(playlist.coverUri)
                if (file.exists()) file else R.drawable.ic_no_artwork_image
            } catch (e: Exception) {
                R.drawable.ic_no_artwork_image
            }
        } else {
            R.drawable.ic_no_artwork_image
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }) {
        AsyncImage(
            model = imageModel,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_no_artwork_image),
            error = painterResource(R.drawable.ic_no_artwork_image)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = playlist.title,
            style = AppTextStyles.PlaylistTitle.copy(fontSize = 12.sp),
            color = if (isDarkTheme) AppColors.White else AppColors.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = context.resources.getQuantityString(
                R.plurals.tracks_count, playlist.trackCount, playlist.trackCount
            ),
            style = AppTextStyles.TrackArtistTime,
            color = if (isDarkTheme) AppColors.White else AppColors.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, name = "Playlists Tab - Empty", showSystemUi = false)
@Composable
fun PlaylistsTabEmptyPreview() {
    AppTheme {
        PlaylistsContent(playlists = emptyList(), onPlaylistClick = {}, onCreatePlaylistClick = {})
    }
}

@Preview(showBackground = true, name = "Playlists Tab - Empty", showSystemUi = false)
@Composable
fun PlaylistsTabEmptyPreviewDark() {
    AppTheme(darkTheme = true) {
        PlaylistsContent(playlists = emptyList(), onPlaylistClick = {}, onCreatePlaylistClick = {})
    }
}

@Preview(showBackground = true, name = "Playlists Tab - With Items")
@Composable
fun PlaylistsTabWithItemsPreview() {
    AppTheme {
        PlaylistsContent(
            playlists = listOf(
            Playlist(
                id = 1,
                title = "Rock Classics",
                description = "Best rock songs",
                coverUri = null,
                tracksCount = listOf(),
                trackCount = 15
            ), Playlist(
                id = 2,
                title = "Chill Vibes",
                description = "Relaxing music",
                coverUri = null,
                tracksCount = listOf(),
                trackCount = 8
            )
        ), onPlaylistClick = {}, onCreatePlaylistClick = {})
    }
}

@Preview(showBackground = true, name = "Playlists Tab - With Items")
@Composable
fun PlaylistsTabWithItemsPreviewDark() {
    AppTheme(darkTheme = true) {
        PlaylistsContent(
            playlists = listOf(
            Playlist(
                id = 1,
                title = "Rock Classics",
                description = "Best rock songs",
                coverUri = null,
                tracksCount = listOf(),
                trackCount = 15
            ), Playlist(
                id = 2,
                title = "Chill Vibes",
                description = "Relaxing music",
                coverUri = null,
                tracksCount = listOf(),
                trackCount = 8
            )
        ), onPlaylistClick = {}, onCreatePlaylistClick = {})
    }
}


