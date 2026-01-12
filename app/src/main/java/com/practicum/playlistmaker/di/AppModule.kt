package com.practicum.playlistmaker.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import com.practicum.playlistmaker.data.NetworkClient
import com.practicum.playlistmaker.data.history.SearchHistoryRepositorImpl
import com.practicum.playlistmaker.data.network.ITunesApi
import com.practicum.playlistmaker.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.data.storage.SharedPrefs
import com.practicum.playlistmaker.data.theme.ThemeRepositoryImpl
import com.practicum.playlistmaker.domain.history.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.domain.history.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.domain.history.GetSearchHistoryUseCase
import com.practicum.playlistmaker.domain.history.SearchHistoryRepository
import com.practicum.playlistmaker.domain.theme.GetThemeUseCase
import com.practicum.playlistmaker.domain.theme.SwitchThemeUseCase
import com.practicum.playlistmaker.domain.theme.ThemeRepository
import com.practicum.playlistmaker.domain.track.SearchTracksUseCase
import com.practicum.playlistmaker.domain.track.TracksRepository
import com.practicum.playlistmaker.presentation.player.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.search.SearchViewModel
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    // Тема
    single<SharedPreferences>(named(Qualifiers.SETTINGS_PREFS)) {
        get<Application>().getSharedPreferences(
            SharedPrefs.PREFS_SETTINGS, Context.MODE_PRIVATE
        )
    }
    single<ThemeRepository> {
        ThemeRepositoryImpl(get(named(Qualifiers.SETTINGS_PREFS)), get<Application>())
    }
    factory { SwitchThemeUseCase(get()) }
    factory { GetThemeUseCase(get()) }
    viewModel { ThemeViewModel(get(), get()) }

    // Аудиоплеер
    viewModel { AudioPlayerViewModel(MediaPlayer()) }

    // История
    single<SharedPreferences>(named(Qualifiers.SEARCH_PREFS)) {
        get<Application>().getSharedPreferences(
            SharedPrefs.PREFS_SEARCH_HISTORY, Context.MODE_PRIVATE
        )
    }
    single<SearchHistoryRepository> {
        SearchHistoryRepositorImpl(get(named(Qualifiers.SEARCH_PREFS)))
    }

    factory { AddTrackToHistoryUseCase(get()) }
    factory { ClearSearchHistoryUseCase(get()) }
    factory { GetSearchHistoryUseCase(get()) }

    // Retrofit + API
    single {
        Retrofit.Builder().baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create()).build()
    }
    single { get<Retrofit>().create(ITunesApi::class.java) }
    single<NetworkClient> { RetrofitNetworkClient(get()) }

    // TracksRepository и use-case
    single<TracksRepository> { TracksRepositoryImpl(get()) }
    factory { SearchTracksUseCase(get()) }

    viewModel { SearchViewModel(get(), get(), get(), get()) }


}