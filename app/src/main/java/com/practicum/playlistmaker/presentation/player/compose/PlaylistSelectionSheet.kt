package com.practicum.playlistmaker.presentation.player.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.presentation.theme.compose.AppTextStyles
import com.practicum.playlistmaker.presentation.util.ResourceProvider
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSelectionSheet(
    playlists: List<Playlist>,
    resourceProvider: ResourceProvider,
    onPlaylistClick: (Playlist) -> Unit,
    onCreateNewClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = resourceProvider.getString(R.string.add_to_playlist),
                style = AppTextStyles.BottomSheetTitle,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn {
                items(playlists) { playlist ->
                    PlaylistRow(
                        playlist = playlist,
                        resourceProvider = resourceProvider,
                        onClick = { onPlaylistClick(playlist) }
                    )
                }
                item {
                    Button(
                        onClick = onCreateNewClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(resourceProvider.getString(R.string.new_playlist))
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistRow(
    playlist: Playlist,
    resourceProvider: ResourceProvider,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val imageModel = remember(playlist.coverUri) {
        if (!playlist.coverUri.isNullOrEmpty()) {
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageModel)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(4.dp)),
            placeholder = painterResource(R.drawable.ic_no_artwork_image),
            error = painterResource(R.drawable.ic_no_artwork_image)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = playlist.title,
                style = AppTextStyles.TrackTitle
            )
            Text(
                text = resourceProvider.getQuantityString(
                    R.plurals.tracks_count,
                    playlist.trackCount,
                    playlist.trackCount
                ),
                style = AppTextStyles.TrackArtistTime
            )
        }
    }
}