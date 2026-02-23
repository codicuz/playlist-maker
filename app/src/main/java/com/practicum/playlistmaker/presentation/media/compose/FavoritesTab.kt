package com.practicum.playlistmaker.presentation.media.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.media.FavoritesViewModel
import com.practicum.playlistmaker.presentation.theme.compose.AppColors
import com.practicum.playlistmaker.presentation.theme.compose.AppTextStyles
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import com.practicum.playlistmaker.presentation.theme.compose.TrackItem
import com.practicum.playlistmaker.presentation.theme.compose.isDarkTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavoritesTab(
    viewModel: FavoritesViewModel = koinViewModel(), onTrackClick: (Track) -> Unit
) {
    val favorites by viewModel.favorites.observeAsState(initial = emptyList())
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    LaunchedEffect(lifecycleOwner.lifecycle) {
        viewModel.loadFavorites()
    }

    FavoritesContent(
        tracks = favorites, onTrackClick = onTrackClick
    )
}

@Composable
fun FavoritesContent(
    tracks: List<Track>, onTrackClick: (Track) -> Unit
) {
    val isDarkTheme = isDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AppColors.Black else AppColors.White)
    ) {
        if (tracks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 106.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.not_found),
                    contentDescription = stringResource(R.string.not_found),
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.empty_media),
                    style = AppTextStyles.MediaText,
                    color = if (isDarkTheme) AppColors.White else AppColors.Black,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                items(tracks) { track ->
                    TrackItem(
                        track = track, onClick = { onTrackClick(track) })
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Favorites Tab Empty Preview")
@Composable
private fun FavoritesTabEmptyPreview() {
    AppTheme(darkTheme = false) {
        FavoritesContent(
            tracks = emptyList(), onTrackClick = {})
    }
}

@Preview(showBackground = true, name = "Favorites Tab Empty Preview")
@Composable
private fun FavoritesTabEmptyPreviewDark() {
    AppTheme(darkTheme = true) {
        FavoritesContent(
            tracks = emptyList(), onTrackClick = {})
    }
}

@Preview(showBackground = true, name = "Favorites Tab With Tracks Preview")
@Composable
private fun FavoritesTabWithTracksPreview() {
    AppTheme {
        FavoritesContent(
            tracks = listOf(
                Track(
                    id = 1,
                    trackId = 1,
                    trackName = "Bohemian Rhapsody",
                    artistsName = "Queen",
                    trackTimeMillis = 354000,
                    artworkUrl100 = "",
                    previewUrl = null,
                    collectionName = "A Night at the Opera",
                    releaseDate = "1975-10-31T00:00:00Z",
                    primaryGenreName = "Rock",
                    country = "UK"
                )
            ), onTrackClick = {})
    }
}

@Preview(showBackground = true, name = "Favorites Tab Dark Preview")
@Composable
private fun FavoritesTabDarkPreview() {
    AppTheme(darkTheme = true) {
        FavoritesContent(
            tracks = listOf(
                Track(
                    id = 1,
                    trackId = 1,
                    trackName = "Bohemian Rhapsody",
                    artistsName = "Queen",
                    trackTimeMillis = 354000,
                    artworkUrl100 = "",
                    previewUrl = null,
                    collectionName = "A Night at the Opera",
                    releaseDate = "1975-10-31T00:00:00Z",
                    primaryGenreName = "Rock",
                    country = "UK"
                )
            ), onTrackClick = {})
    }
}