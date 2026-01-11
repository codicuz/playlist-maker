package com.practicum.playlistmaker.creator

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.practicum.playlistmaker.data.history.SearchHistoryRepositorImpl
import com.practicum.playlistmaker.data.network.ITunesApi
import com.practicum.playlistmaker.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.data.storage.SharedPrefs
import com.practicum.playlistmaker.domain.history.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.domain.history.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.domain.history.GetSearchHistoryUseCase
import com.practicum.playlistmaker.domain.history.SearchHistoryRepository
import com.practicum.playlistmaker.domain.track.SearchTracksUseCase
import com.practicum.playlistmaker.domain.track.TracksRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {

    fun provideSearchHistoryRepository(application: Application): SearchHistoryRepository {
        val prefs: SharedPreferences = application.getSharedPreferences(
            SharedPrefs.PREFS_SEARCH_HISTORY, Context.MODE_PRIVATE
        )
        return SearchHistoryRepositorImpl(prefs)
    }

    fun provideAddTrackToHistoryUseCase(application: Application): AddTrackToHistoryUseCase {
        return AddTrackToHistoryUseCase(provideSearchHistoryRepository(application))
    }

    fun provideClearSearchHistoryUseCase(application: Application): ClearSearchHistoryUseCase {
        return ClearSearchHistoryUseCase(provideSearchHistoryRepository(application))
    }

    fun provideGetSearchHistoryUseCase(application: Application): GetSearchHistoryUseCase {
        return GetSearchHistoryUseCase(provideSearchHistoryRepository(application))
    }

    private const val ITUNES_BASE_URL = "https://itunes.apple.com"

    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(ITUNES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    fun provideITunesApi(): ITunesApi {
        return provideRetrofit().create(ITunesApi::class.java)
    }

    fun provideTracksRepository(): TracksRepository {
        val networkClient = RetrofitNetworkClient(provideITunesApi())
        return TracksRepositoryImpl(networkClient)
    }

    fun provideSearchTracksUseCase(): SearchTracksUseCase {
        return SearchTracksUseCase(provideTracksRepository())
    }
}
