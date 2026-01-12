package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.domain.history.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.domain.history.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.domain.history.GetSearchHistoryUseCase
import com.practicum.playlistmaker.domain.theme.GetThemeUseCase
import com.practicum.playlistmaker.domain.theme.SwitchThemeUseCase
import com.practicum.playlistmaker.domain.track.SearchTracksUseCase
import org.koin.dsl.module

val domainModule = module {
    // Theme
    factory { GetThemeUseCase(get()) }
    factory { SwitchThemeUseCase(get()) }

    // History
    factory { AddTrackToHistoryUseCase(get()) }
    factory { ClearSearchHistoryUseCase(get()) }
    factory { GetSearchHistoryUseCase(get()) }

    // Search
    factory { SearchTracksUseCase(get()) }
}