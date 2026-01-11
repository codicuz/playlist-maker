package com.practicum.playlistmaker.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.practicum.playlistmaker.data.storage.SharedPrefs
import com.practicum.playlistmaker.data.theme.ThemeRepositoryImpl
import com.practicum.playlistmaker.domain.theme.GetThemeUseCase
import com.practicum.playlistmaker.domain.theme.SwitchThemeUseCase
import com.practicum.playlistmaker.domain.theme.ThemeRepository
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single<SharedPreferences>(named("settingsPrefs")) {
        get<Application>().getSharedPreferences(
            SharedPrefs.PREFS_SETTINGS, Context.MODE_PRIVATE
        )
    }
    single<ThemeRepository> {
        ThemeRepositoryImpl(get(named("settingsPrefs")), get<Application>())
    }
    single { SwitchThemeUseCase(get()) }
    single { GetThemeUseCase(get()) }
    viewModel { ThemeViewModel(get(), get()) }
}