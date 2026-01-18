package com.practicum.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import com.practicum.playlistmaker.presentation.media.FavoritesViewModel
import com.practicum.playlistmaker.presentation.media.MediaViewModel
import com.practicum.playlistmaker.presentation.media.PlaylistsViewModel
import com.practicum.playlistmaker.presentation.player.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { ThemeViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get(), get(), get()) }
    viewModel { MediaViewModel() }
    viewModel { FavoritesViewModel() }
    viewModel { PlaylistsViewModel() }

    factory {
        MediaPlayer()
        val context: Context = get()
        val mpContext = context.createAttributionContext("audioPlayback")
        MediaPlayer(mpContext)
    }
    viewModel { AudioPlayerViewModel(get()) }
}