package com.practicum.playlistmaker.presentation.media.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.presentation.media.MediaTab
import com.practicum.playlistmaker.presentation.media.MediaViewModel
import com.practicum.playlistmaker.presentation.theme.compose.AppColors
import com.practicum.playlistmaker.presentation.theme.compose.AppTextStyles
import com.practicum.playlistmaker.presentation.theme.compose.AppTheme
import com.practicum.playlistmaker.presentation.theme.compose.isDarkTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MediaScreen(
    viewModel: MediaViewModel = koinViewModel(),
    onPlaylistClick: (Long) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onTrackClick: (com.practicum.playlistmaker.domain.track.Track) -> Unit
) {
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()

    AppTheme {
        MediaContent(
            tabs = tabs,
            onPlaylistClick = onPlaylistClick,
            onCreatePlaylistClick = onCreatePlaylistClick,
            onTrackClick = onTrackClick
        )
    }
}

@Composable
fun MediaContent(
    tabs: List<MediaTab>,
    onPlaylistClick: (Long) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onTrackClick: (com.practicum.playlistmaker.domain.track.Track) -> Unit
) {
    val isDarkTheme = isDarkTheme()
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AppColors.Black else AppColors.White)
    ) {
        Text(
            text = stringResource(R.string.media),
            style = AppTextStyles.ActivityTitle,
            color = if (isDarkTheme) AppColors.White else AppColors.Black,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 16.dp)
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = if (isDarkTheme) AppColors.Black else AppColors.White,
            contentColor = if (isDarkTheme) AppColors.White else AppColors.Black,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 2.dp,
                    color = AppColors.Blue
                )
            },
            divider = {}) {
            tabs.forEachIndexed { index, tab ->
                Tab(selected = pagerState.currentPage == index, onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }, text = {
                    Text(
                        text = when (tab) {
                            MediaTab.FAVORITES -> stringResource(R.string.favourite_tracks)
                            MediaTab.PLAYLISTS -> stringResource(R.string.playlists)
                        },
                        style = AppTextStyles.MediaText,
                        color = if (pagerState.currentPage == index) {
                            if (isDarkTheme) AppColors.White else AppColors.Black
                        } else {
                            if (isDarkTheme) AppColors.White else AppColors.Black
                        }
                    )
                })
            }
        }

        HorizontalPager(
            state = pagerState, modifier = Modifier.weight(1f)
        ) { page ->
            when (tabs[page]) {
                MediaTab.FAVORITES -> FavoritesTab(
                    onTrackClick = onTrackClick
                )

                MediaTab.PLAYLISTS -> PlaylistsTab(
                    onPlaylistClick = onPlaylistClick, onCreatePlaylistClick = onCreatePlaylistClick
                )
            }
        }
    }
}