package com.practicum.playlistmaker.di

import android.media.MediaPlayer
import com.practicum.playlistmaker.presentation.player.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { ThemeViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get(), get(), get()) }
    viewModel { AudioPlayerViewModel(MediaPlayer()) }
}