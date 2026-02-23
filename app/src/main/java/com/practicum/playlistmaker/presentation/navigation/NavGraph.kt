package com.practicum.playlistmaker.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.practicum.playlistmaker.R
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
import com.practicum.playlistmaker.presentation.theme.compose.AppColors
import com.practicum.playlistmaker.presentation.theme.compose.AppTextStyles
import com.practicum.playlistmaker.presentation.util.AndroidResourceProvider
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavGraph(
    navController: NavHostController, startDestination: String = Screen.Media.route
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
            MediaScreen(viewModel = koinViewModel(), onPlaylistClick = { playlistId ->
                navController.navigate(Screen.Playlist.passPlaylistId(playlistId))
            }, onCreatePlaylistClick = {
                navController.navigate(Screen.NewPlaylist.route)
            }, onTrackClick = { track ->
                navController.navigate(Screen.Player.passTrackId(track.trackId ?: 0))
            })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = koinViewModel(), onNavigateBack = {
                    navController.popBackStack()
                })
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("trackId") { type = NavType.IntType })
        ) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getInt("trackId") ?: 0
            val viewModel: AudioPlayerViewModel = koinViewModel()

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
            val state by viewModel.state.collectAsStateWithLifecycle()
            var showExitDialog by remember { mutableStateOf(false) }

            fun checkUnsavedChanges(): Boolean {
                return state.title.isNotBlank() || state.description.isNotBlank() || state.coverUri != null
            }

            BackHandler(enabled = true) {
                if (checkUnsavedChanges()) {
                    showExitDialog = true
                } else {
                    navController.popBackStack()
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                CreatePlaylistScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        if (checkUnsavedChanges()) {
                            showExitDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onPlaylistCreated = { title ->
                        navController.popBackStack()
                    }
                )

                if (showExitDialog) {
                    ExitConfirmationDialog(
                        onConfirm = {
                            showExitDialog = false
                            navController.popBackStack()
                        },
                        onDismiss = { showExitDialog = false }
                    )
                }
            }
        }

        composable(
            route = Screen.Playlist.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            val viewModel: PlaylistViewModel = koinViewModel()
            val resourceProvider = AndroidResourceProvider(context)

            val refreshTrigger = backStackEntry.savedStateHandle.get<Boolean>("refresh") ?: false

            LaunchedEffect(refreshTrigger) {
                if (refreshTrigger) {
                    viewModel.loadPlaylist()
                    backStackEntry.savedStateHandle.set("refresh", false)
                }
            }

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
                onShowDeleteTrackDialog = { track, onConfirm ->
                },
                onShowDeletePlaylistDialog = { playlistName, onConfirm ->
                },
                onShowToast = { message ->
                    android.widget.Toast.makeText(
                        context, message, android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
                resourceProvider = resourceProvider
            )
        }

        composable(
            route = Screen.EditPlaylist.route,
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
            val viewModel: EditPlaylistViewModel = koinViewModel()

            LaunchedEffect(playlistId) {
                viewModel.loadPlaylist(playlistId)
            }

            CreatePlaylistScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPlaylistCreated = { title ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(4.dp),
        containerColor = AppColors.White,
        title = {
            Text(
                text = stringResource(R.string.abort_create_playlist),
                style = AppTextStyles.BottomSheetTitle,
                color = AppColors.Black
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.finish_btn))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel_btn))
            }
        })
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    context.startActivity(android.content.Intent.createChooser(intent, null))
}