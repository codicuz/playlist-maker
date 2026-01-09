package com.practicum.playlistmaker.creator

import android.app.Application
import android.media.MediaPlayer
import com.practicum.playlistmaker.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.data.storage.SharedPrefs
import com.practicum.playlistmaker.data.theme.ThemeRepositoryImpl
import com.practicum.playlistmaker.domain.theme.GetThemeUseCase
import com.practicum.playlistmaker.domain.theme.SwitchThemeUseCase
import com.practicum.playlistmaker.domain.track.SearchTracksUseCase
import com.practicum.playlistmaker.domain.track.TracksRepository
import com.practicum.playlistmaker.presentation.player.AudioPlayerViewModel
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import com.practicum.playlistmaker.presentation.theme.ThemeViewModelFactory

object Creator {

    private var mediaPlayerInstance: MediaPlayer? = null
    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideSearchTracksUseCase(): SearchTracksUseCase {
        return SearchTracksUseCase(getTracksRepository())
    }

    fun provideMediaPlayer(): MediaPlayer {
        if (mediaPlayerInstance == null) {
            mediaPlayerInstance = MediaPlayer()
        }
        return mediaPlayerInstance!!
    }

    fun provideAudioPlayerViewModel(): AudioPlayerViewModel {
        return AudioPlayerViewModel(provideMediaPlayer())
    }

    fun provideThemeViewModel(application: Application): ThemeViewModel {
        val prefs = application.getSharedPreferences(
            SharedPrefs.PREFS_SETTINGS,
            android.content.Context.MODE_PRIVATE
        )

        val repository = ThemeRepositoryImpl(prefs, application)
        repository.applyThemeAccordingToUserOrSystem()

        val switchThemeUseCase = SwitchThemeUseCase(repository)
        val getThemeUseCase = GetThemeUseCase(repository)

        val factory = ThemeViewModelFactory(switchThemeUseCase, getThemeUseCase)
        return factory.create(ThemeViewModel::class.java)
    }
}
