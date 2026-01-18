package com.practicum.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import com.practicum.playlistmaker.presentation.media.MediaViewModel
import com.practicum.playlistmaker.presentation.player.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Решение проблемы с Error attributionTag audioPlayback not declared in manifest of в логах
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
val presentationModule = module {
    viewModel { ThemeViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get(), get(), get()) }
    viewModel { MediaViewModel() }

    factory {
        val context: Context = get()
        val mpContext =
            context.createAttributionContext("audioPlayback")
        MediaPlayer(mpContext)
    }
    viewModel { AudioPlayerViewModel(get()) }
}