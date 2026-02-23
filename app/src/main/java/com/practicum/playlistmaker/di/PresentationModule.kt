package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.presentation.media.EditPlaylistViewModel
import com.practicum.playlistmaker.presentation.media.FavoritesViewModel
import com.practicum.playlistmaker.presentation.media.MediaViewModel
import com.practicum.playlistmaker.presentation.media.NewPlaylistViewModel
import com.practicum.playlistmaker.presentation.media.PlaylistsViewModel
import com.practicum.playlistmaker.presentation.player.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.playlist.PlaylistViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    single { ThemeViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get(), get(), get()) }
    viewModel { MediaViewModel() }
    viewModel { FavoritesViewModel(get()) }
    viewModel { PlaylistsViewModel(get()) }

    viewModel {
        AudioPlayerViewModel(
            get(), get(), get(), get(), get(), get()
        )
    }

    viewModel { NewPlaylistViewModel(get()) }
    viewModel { PlaylistViewModel(get(), get(), get(), get(), get()) }
    viewModel { EditPlaylistViewModel(get(), get(), get()) }
}