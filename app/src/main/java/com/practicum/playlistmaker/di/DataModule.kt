package com.practicum.playlistmaker.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.practicum.playlistmaker.data.NetworkClient
import com.practicum.playlistmaker.data.history.SearchHistoryRepositorImpl
import com.practicum.playlistmaker.data.network.ITunesApi
import com.practicum.playlistmaker.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.data.storage.SharedPrefs
import com.practicum.playlistmaker.data.theme.ThemeRepositoryImpl
import com.practicum.playlistmaker.domain.history.SearchHistoryRepository
import com.practicum.playlistmaker.domain.theme.ThemeRepository
import com.practicum.playlistmaker.domain.track.TracksRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {
    // SharedPreferences
    single<SharedPreferences>(named(Qualifiers.SETTINGS_PREFS)) {
        get<Application>().getSharedPreferences(
            SharedPrefs.PREFS_SETTINGS, Context.MODE_PRIVATE
        )
    }

    single<SharedPreferences>(named(Qualifiers.SEARCH_PREFS)) {
        get<Application>().getSharedPreferences(
            SharedPrefs.PREFS_SEARCH_HISTORY, Context.MODE_PRIVATE
        )
    }

    // Theme
    single<ThemeRepository> {
        ThemeRepositoryImpl(get(named(Qualifiers.SETTINGS_PREFS)), get<Application>())
    }

    // History
    single<SearchHistoryRepository> {
        SearchHistoryRepositorImpl(get(named(Qualifiers.SEARCH_PREFS)))
    }

    // Retrofit
    single {
        Retrofit.Builder().baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create()).build()
    }
    single { get<Retrofit>().create(ITunesApi::class.java) }
    single<NetworkClient> { RetrofitNetworkClient(get()) }

    // Tracks
    single<TracksRepository> { TracksRepositoryImpl(get()) }

    // MediaPlayer


}