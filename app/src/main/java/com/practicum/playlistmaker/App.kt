package com.practicum.playlistmaker

import android.app.Application
import com.practicum.playlistmaker.di.appModule
import com.practicum.playlistmaker.domain.theme.ThemeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        // Тема берется один раз системная после устновки приложения, затем сохраняется
        // После, если системная тема поменялась, то приложение живет уже с установленной
        // темой пользователем
        val themeRepository: ThemeRepository =
            org.koin.java.KoinJavaComponent.get(ThemeRepository::class.java)

        themeRepository.applyTheme()
    }
}