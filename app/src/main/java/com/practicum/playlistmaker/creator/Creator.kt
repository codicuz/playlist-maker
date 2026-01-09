package com.practicum.playlistmaker.creator

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import com.practicum.playlistmaker.data.history.SearchHistoryRepositorImpl
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
import com.practicum.playlistmaker.domain.track.SearchTracksUseCase
import com.practicum.playlistmaker.domain.track.TracksRepository
import com.practicum.playlistmaker.presentation.player.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import com.practicum.playlistmaker.presentation.theme.ThemeViewModelFactory

object Creator {

    private var mediaPlayerInstance: MediaPlayer? = null

    // TracksRepository через RetrofitNetworkClient
    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideSearchTracksUseCase(): SearchTracksUseCase {
        return SearchTracksUseCase(getTracksRepository())
    }

    // MediaPlayer
    fun provideMediaPlayer(): MediaPlayer {
        if (mediaPlayerInstance == null) {
            mediaPlayerInstance = MediaPlayer()
        }
        return mediaPlayerInstance!!
    }

    fun provideAudioPlayerViewModel(): AudioPlayerViewModel {
        return AudioPlayerViewModel(provideMediaPlayer())
    }

    // ThemeViewModel
    fun provideThemeViewModel(application: Application): ThemeViewModel {
        val prefs = application.getSharedPreferences(
            SharedPrefs.PREFS_SETTINGS, Context.MODE_PRIVATE
        )
        val repository = ThemeRepositoryImpl(prefs, application)
        repository.applyThemeAccordingToUserOrSystem()
        val switchThemeUseCase = SwitchThemeUseCase(repository)
        val getThemeUseCase = GetThemeUseCase(repository)
        val factory = ThemeViewModelFactory(switchThemeUseCase, getThemeUseCase)
        return factory.create(ThemeViewModel::class.java)
    }

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
}
