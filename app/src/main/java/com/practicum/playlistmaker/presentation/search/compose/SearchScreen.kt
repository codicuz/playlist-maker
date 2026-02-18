package com.practicum.playlistmaker.presentation.search.compose

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.presentation.search.SearchScreenState
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.theme.compose.*
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onTrackClick: (Track) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppTheme {
        SearchContent(
            state = state,
            onQueryChanged = { query -> viewModel.onQueryChanged(query) },
            onSearchDone = { query -> viewModel.onSearchDone(query) },
            onTrackClick = onTrackClick,
            onClearHistory = { viewModel.clearHistory() },
            onAddToHistory = { track -> viewModel.addTrackToHistory(track) },
            onClearSearchResults = { viewModel.clearSearchResults() },
            onLoadHistory = { viewModel.loadHistory() },
            onTrackClicked = { track, openPlayer -> viewModel.onTrackClicked(track, openPlayer) }
        )
    }
}

@Composable
fun SearchContent(
    state: SearchScreenState,
    onQueryChanged: (String) -> Unit,
    onSearchDone: (String) -> Unit,
    onTrackClick: (Track) -> Unit,
    onClearHistory: () -> Unit,
    onAddToHistory: (Track) -> Unit,
    onClearSearchResults: () -> Unit,
    onLoadHistory: () -> Unit,
    onTrackClicked: (Track, (Track) -> Unit) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDarkTheme = MaterialTheme.colorScheme.background == AppColors.Black
    val view = LocalView.current

    var query by rememberSaveable { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var hasSearchExecuted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onLoadHistory()
    }

    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            delay(500)
            onSearchDone(query)
            hasSearchExecuted = true
        } else {
            hasSearchExecuted = false
        }
    }

    LaunchedEffect(view) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val bottomNav = v.rootView.findViewById<View>(R.id.bottomNavigationView)
            bottomNav?.isVisible = !isImeVisible
            insets
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) AppColors.Black else AppColors.White)
            .clickable {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(R.string.search),
                style = AppTextStyles.ActivityTitle,
                color = if (isDarkTheme) AppColors.White else AppColors.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 14.dp, bottom = 16.dp),
                textAlign = TextAlign.Start
            )

            SearchBar(
                query = query,
                onQueryChange = { newQuery ->
                    query = newQuery
                    onQueryChanged(newQuery)
                },
                onSearchDone = {
                    if (query.isNotBlank()) {
                        onSearchDone(query)
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        hasSearchExecuted = true
                    }
                },
                onClearClick = {
                    query = ""
                    onQueryChanged("")
                    onClearSearchResults()
                    onLoadHistory()
                    hasSearchExecuted = false
                },
                isFocused = isSearchFocused,
                onFocusChange = { focused ->
                    isSearchFocused = focused
                    if (focused) {
                        onLoadHistory()
                    }
                },
                focusRequester = focusRequester,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    state.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center).size(48.dp),
                        color = AppColors.Blue
                    )

                    state.isError -> ErrorPlaceholder(
                        onRetry = { if (query.isNotBlank()) onSearchDone(query) }
                    )

                    isSearchFocused && !hasSearchExecuted && state.history.isNotEmpty() -> SearchHistory(
                        history = state.history,
                        onTrackClick = { track ->
                            onAddToHistory(track)
                            onTrackClick(track)
                        },
                        onClearHistory = onClearHistory
                    )

                    state.tracks.isNotEmpty() -> TrackList(
                        tracks = state.tracks,
                        onTrackClick = { track ->
                            onTrackClicked(track, onTrackClick)
                        }
                    )

                    hasSearchExecuted && state.tracks.isEmpty() && !state.isLoading -> EmptySearchPlaceholder()
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchDone: () -> Unit,
    onClearClick: () -> Unit,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isDarkTheme()

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(
                    color = if (isDarkTheme) AppColors.White else AppColors.LightGray,
                    shape = RoundedCornerShape(8.dp)
                )
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChange(it.isFocused) }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isDarkTheme) AppColors.Black else AppColors.Gray
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = AppTextStyles.TrackTitle.copy(
                            color = AppColors.Black
                        ),
                        cursorBrush = SolidColor(AppColors.Blue),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onSearchDone() }
                        )
                    )

                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search),
                            style = AppTextStyles.TrackArtistTime.copy(fontSize = 16.sp),
                            color = if (isDarkTheme) AppColors.Gray else AppColors.Gray
                        )
                    }
                }

                if (query.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        painter = painterResource(R.drawable.clear_button),
                        contentDescription = stringResource(R.string.todo),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onClearClick() },
                        tint = if (isDarkTheme) AppColors.Black else AppColors.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun TrackList(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(tracks) { track ->
            TrackItem(track = track, onClick = { onTrackClick(track) })
        }
    }
}

@Composable
fun SearchHistory(
    history: List<Track>,
    onTrackClick: (Track) -> Unit,
    onClearHistory: () -> Unit
) {
    val isDarkTheme = MaterialTheme.colorScheme.background == AppColors.Black

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.you_searched),
            style = AppTextStyles.ErrorText,
            color = if (isDarkTheme) AppColors.White else AppColors.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 42.dp, bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(history) { track ->
                TrackItem(track = track, onClick = { onTrackClick(track) })
            }
        }

        ClearHistoryButton(
            onClick = onClearHistory,
            text = stringResource(R.string.clear_history)
        )
    }
}

@Composable
fun EmptySearchPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 102.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            painter = painterResource(R.drawable.not_found),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.nothing_found),
            style = AppTextStyles.ErrorText,
            color = AppColors.Gray
        )
    }
}

@Composable
fun ErrorPlaceholder(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 102.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            painter = painterResource(R.drawable.no_internet),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_internet),
            style = AppTextStyles.ErrorText,
            color = AppColors.Gray,
            modifier = Modifier.padding(horizontal = 24.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        NoInternetButton(
            onClick = onRetry,
            text = stringResource(R.string.update_btn)
        )
    }
}

@Preview(showBackground = true, name = "Search Screen - Empty")
@Composable
fun SearchScreenEmptyPreview() {
    AppTheme {
        SearchContent(
            state = SearchScreenState(
                tracks = emptyList(),
                history = emptyList(),
                isError = false,
                hasSearched = false,
                isLoading = false
            ),
            onQueryChanged = {},
            onSearchDone = {},
            onTrackClick = {},
            onClearHistory = {},
            onAddToHistory = {},
            onClearSearchResults = {},
            onLoadHistory = {},
            onTrackClicked = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Search Screen - With Results")
@Composable
fun SearchScreenWithResultsPreview() {
    AppTheme {
        SearchContent(
            state = SearchScreenState(
                tracks = listOf(
                    Track(
                        id = 1,
                        trackId = 1,
                        trackName = "Bohemian Rhapsody",
                        artistsName = "Queen",
                        trackTimeMillis = 354000,
                        artworkUrl100 = "",
                        previewUrl = null,
                        collectionName = "",
                        releaseDate = "",
                        primaryGenreName = "",
                        country = ""
                    ),
                    Track(
                        id = 2,
                        trackId = 2,
                        trackName = "Imagine",
                        artistsName = "John Lennon",
                        trackTimeMillis = 183000,
                        artworkUrl100 = "",
                        previewUrl = null,
                        collectionName = "",
                        releaseDate = "",
                        primaryGenreName = "",
                        country = ""
                    )
                ),
                history = emptyList(),
                isError = false,
                hasSearched = true,
                isLoading = false
            ),
            onQueryChanged = {},
            onSearchDone = {},
            onTrackClick = {},
            onClearHistory = {},
            onAddToHistory = {},
            onClearSearchResults = {},
            onLoadHistory = {},
            onTrackClicked = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Search Screen - Loading")
@Composable
fun SearchScreenLoadingPreview() {
    AppTheme {
        SearchContent(
            state = SearchScreenState(
                tracks = emptyList(),
                history = emptyList(),
                isError = false,
                hasSearched = false,
                isLoading = true
            ),
            onQueryChanged = {},
            onSearchDone = {},
            onTrackClick = {},
            onClearHistory = {},
            onAddToHistory = {},
            onClearSearchResults = {},
            onLoadHistory = {},
            onTrackClicked = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Search Screen - Error")
@Composable
fun SearchScreenErrorPreview() {
    AppTheme {
        SearchContent(
            state = SearchScreenState(
                tracks = emptyList(),
                history = emptyList(),
                isError = true,
                hasSearched = true,
                isLoading = false
            ),
            onQueryChanged = {},
            onSearchDone = {},
            onTrackClick = {},
            onClearHistory = {},
            onAddToHistory = {},
            onClearSearchResults = {},
            onLoadHistory = {},
            onTrackClicked = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Search Screen - History")
@Composable
fun SearchScreenHistoryPreview() {
    AppTheme {
        SearchContent(
            state = SearchScreenState(
                tracks = emptyList(),
                history = listOf(
                    Track(
                        id = 1,
                        trackId = 1,
                        trackName = "Bohemian Rhapsody",
                        artistsName = "Queen",
                        trackTimeMillis = 354000,
                        artworkUrl100 = "",
                        previewUrl = null,
                        collectionName = "",
                        releaseDate = "",
                        primaryGenreName = "",
                        country = ""
                    ),
                    Track(
                        id = 2,
                        trackId = 2,
                        trackName = "Imagine",
                        artistsName = "John Lennon",
                        trackTimeMillis = 183000,
                        artworkUrl100 = "",
                        previewUrl = null,
                        collectionName = "",
                        releaseDate = "",
                        primaryGenreName = "",
                        country = ""
                    )
                ),
                isError = false,
                hasSearched = false,
                isLoading = false
            ),
            onQueryChanged = {},
            onSearchDone = {},
            onTrackClick = {},
            onClearHistory = {},
            onAddToHistory = {},
            onClearSearchResults = {},
            onLoadHistory = {},
            onTrackClicked = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Track Item")
@Composable
fun TrackItemPreview() {
    AppTheme {
        TrackItem(
            track = Track(
                id = 1,
                trackId = 1,
                trackName = "Bohemian Rhapsody",
                artistsName = "Queen",
                trackTimeMillis = 354000,
                artworkUrl100 = "",
                previewUrl = null,
                collectionName = "",
                releaseDate = "",
                primaryGenreName = "",
                country = ""
            ),
            onClick = {}
        )
    }
}


@Preview(showBackground = true, name = "Search History")
@Composable
fun SearchHistoryPreview() {
    AppTheme {
        SearchHistory(
            history = listOf(
                Track(
                    id = 1,
                    trackId = 1,
                    trackName = "Bohemian Rhapsody",
                    artistsName = "Queen",
                    trackTimeMillis = 354000,
                    artworkUrl100 = "",
                    previewUrl = null,
                    collectionName = "",
                    releaseDate = "",
                    primaryGenreName = "",
                    country = ""
                ),
                Track(
                    id = 2,
                    trackId = 2,
                    trackName = "Imagine",
                    artistsName = "John Lennon",
                    trackTimeMillis = 183000,
                    artworkUrl100 = "",
                    previewUrl = null,
                    collectionName = "",
                    releaseDate = "",
                    primaryGenreName = "",
                    country = ""
                )
            ),
            onTrackClick = {},
            onClearHistory = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty Placeholder")
@Composable
fun EmptyPlaceholderPreview() {
    AppTheme {
        EmptySearchPlaceholder()
    }
}

@Preview(showBackground = true, name = "Error Placeholder")
@Composable
fun ErrorPlaceholderPreview() {
    AppTheme {
        ErrorPlaceholder(
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Track List")
@Composable
fun TrackListPreview() {
    AppTheme {
        TrackList(
            tracks = listOf(
                Track(
                    id = 1,
                    trackId = 1,
                    trackName = "Bohemian Rhapsody",
                    artistsName = "Queen",
                    trackTimeMillis = 354000,
                    artworkUrl100 = "",
                    previewUrl = null,
                    collectionName = "",
                    releaseDate = "",
                    primaryGenreName = "",
                    country = ""
                ),
                Track(
                    id = 2,
                    trackId = 2,
                    trackName = "Imagine",
                    artistsName = "John Lennon",
                    trackTimeMillis = 183000,
                    artworkUrl100 = "",
                    previewUrl = null,
                    collectionName = "",
                    releaseDate = "",
                    primaryGenreName = "",
                    country = ""
                )
            ),
            onTrackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Search Content - Light")
@Composable
fun SearchContentLightPreview() {
    AppTheme(darkTheme = false) {
        SearchContent(
            state = SearchScreenState(
                tracks = listOf(
                    Track(
                        id = 1,
                        trackId = 1,
                        trackName = "Bohemian Rhapsody",
                        artistsName = "Queen",
                        trackTimeMillis = 354000,
                        artworkUrl100 = "",
                        previewUrl = null,
                        collectionName = "",
                        releaseDate = "",
                        primaryGenreName = "",
                        country = ""
                    )
                ),
                history = emptyList(),
                isError = false,
                hasSearched = true,
                isLoading = false
            ),
            onQueryChanged = {},
            onSearchDone = {},
            onTrackClick = {},
            onClearHistory = {},
            onAddToHistory = {},
            onClearSearchResults = {},
            onLoadHistory = {},
            onTrackClicked = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Search Content - Dark")
@Composable
fun SearchContentDarkPreview() {
    AppTheme(darkTheme = true) {
        SearchContent(
            state = SearchScreenState(
                tracks = listOf(
                    Track(
                        id = 1,
                        trackId = 1,
                        trackName = "Bohemian Rhapsody",
                        artistsName = "Queen",
                        trackTimeMillis = 354000,
                        artworkUrl100 = "",
                        previewUrl = null,
                        collectionName = "",
                        releaseDate = "",
                        primaryGenreName = "",
                        country = ""
                    )
                ),
                history = emptyList(),
                isError = false,
                hasSearched = true,
                isLoading = false
            ),
            onQueryChanged = {},
            onSearchDone = {},
            onTrackClick = {},
            onClearHistory = {},
            onAddToHistory = {},
            onClearSearchResults = {},
            onLoadHistory = {},
            onTrackClicked = { _, _ -> }
        )
    }
}