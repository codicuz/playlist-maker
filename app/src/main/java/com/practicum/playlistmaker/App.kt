package com.practicum.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.practicum.playlistmaker.di.appModule
import com.practicum.playlistmaker.domain.theme.ThemeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.java.KoinJavaComponent.get

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        getThemeRepository().applyTheme()
    }

    private fun getThemeRepository(): ThemeRepository =
        get(ThemeRepository::class.java)
}