package com.practicum.playlistmaker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.practicum.playlistmaker.presentation.media.EditPlaylistViewModel
import com.practicum.playlistmaker.presentation.media.NewPlaylistViewModel
import com.practicum.playlistmaker.presentation.media.compose.CreatePlaylistScreen
import com.practicum.playlistmaker.presentation.media.compose.MediaScreen
import com.practicum.playlistmaker.presentation.player.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.player.compose.AudioPlayerScreen
import com.practicum.playlistmaker.presentation.playlist.PlaylistViewModel
import com.practicum.playlistmaker.presentation.playlist.compose.PlaylistScreen
import com.practicum.playlistmaker.presentation.search.compose.SearchScreen
import com.practicum.playlistmaker.presentation.settings.compose.SettingsScreen
import com.practicum.playlistmaker.presentation.util.AndroidResourceProvider
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NavGraph(
    navController: NavHostController, startDestination: String = Screen.Search.route
) {
    val context = LocalContext.current

    NavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable(Screen.Search.route) {
            SearchScreen(
                viewModel = koinViewModel(), onTrackClick = { track ->
                    navController.navigate(Screen.Player.passTrackId(track.trackId ?: 0))
                })
        }

        composable(Screen.Media.route) {
            MediaScreen(
                viewModel = koinViewModel(),
                onPlaylistClick = { playlistId ->
                    navController.navigate(Screen.Playlist.passPlaylistId(playlistId))
                },
                onCreatePlaylistClick = {
                    navController.navigate(Screen.NewPlaylist.route)
                },
                onTrackClick = { track ->
                    navController.navigate(Screen.Player.passTrackId(track.trackId ?: 0))
                })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = koinViewModel(),
                onNavigateBack = {
                    navController.popBackStack()
                })
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("trackId") { type = NavType.IntType })
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getInt("trackId") ?: 0
            val viewModel: AudioPlayerViewModel = koinViewModel()

            // Убрали resourceProvider из параметров
            AudioPlayerScreen(
                viewModel = viewModel,
                trackId = trackId,
                onNavigateBack = { navController.popBackStack() },
                onCreatePlaylistClick = {
                    navController.navigate(Screen.NewPlaylist.route)
                })
        }

        composable(Screen.NewPlaylist.route) {
            val viewModel: NewPlaylistViewModel = koinViewModel()
            CreatePlaylistScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPlaylistCreated = { title ->
                    navController.popBackStack()
                })
        }

        composable(
            route = Screen.Playlist.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            val viewModel: PlaylistViewModel = koinViewModel()
            val resourceProvider = AndroidResourceProvider(context)

            PlaylistScreen(
                viewModel = viewModel,
                playlistId = playlistId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlayer = { track ->
                    navController.navigate(Screen.Player.passTrackId(track.trackId ?: 0))
                },
                onNavigateToEditPlaylist = { id ->
                    navController.navigate(Screen.EditPlaylist.passPlaylistId(id))
                },
                onShareText = { text -> shareText(context, text) },
                onShowDeleteTrackDialog = { track, onConfirm -> /* диалог */ },
                onShowDeletePlaylistDialog = { playlistName, onConfirm -> /* диалог */ },
                onShowToast = { message -> /* тост */ },
                resourceProvider = resourceProvider)
        }

        composable(
            route = Screen.EditPlaylist.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            val viewModel: EditPlaylistViewModel = koinViewModel()

            // Загружаем плейлист при входе на экран
            LaunchedEffect(playlistId) {
                viewModel.loadPlaylist(playlistId)
            }

            CreatePlaylistScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPlaylistCreated = { title ->
                    navController.popBackStack()
                })
        }
    }
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    context.startActivity(android.content.Intent.createChooser(intent, null))
}