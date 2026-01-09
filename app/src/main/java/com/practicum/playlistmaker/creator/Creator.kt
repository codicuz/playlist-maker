package com.practicum.playlistmaker.creator

import android.app.Application
import com.practicum.playlistmaker.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.data.storage.SharedPrefs
import com.practicum.playlistmaker.data.theme.ThemeRepositoryImpl
import com.practicum.playlistmaker.domain.theme.GetThemeUseCase
import com.practicum.playlistmaker.domain.theme.SwitchThemeUseCase
import com.practicum.playlistmaker.domain.track.SearchTracksUseCase
import com.practicum.playlistmaker.domain.track.TracksRepository
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import com.practicum.playlistmaker.presentation.theme.ThemeViewModelFactory

object Creator {

    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideSearchTracksUseCase(): SearchTracksUseCase {
        return SearchTracksUseCase(getTracksRepository())
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
