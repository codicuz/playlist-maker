package com.practicum.playlistmaker.presentation.theme.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.track.Track

@Composable
fun TrackItem(
    track: Track, onClick: () -> Unit
) {
    val isDarkTheme = isDarkTheme()

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
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
                style = AppTextStyles.TrackTitle,
                color = if (isDarkTheme) AppColors.White else AppColors.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(1.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.artistsName ?: "-",
                    style = AppTextStyles.TrackArtistTime,
                    color = if (isDarkTheme()) AppColors.White else AppColors.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(
                        1f, fill = false
                    )
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
                    style = AppTextStyles.TrackArtistTime,
                    color = if (isDarkTheme()) AppColors.White else AppColors.Gray,
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