package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.domain.favorites.AddToFavoritesUseCase
import com.practicum.playlistmaker.domain.favorites.GetFavoritesUseCase
import com.practicum.playlistmaker.domain.favorites.IsFavoriteUseCase
import com.practicum.playlistmaker.domain.favorites.RemoveFromFavoritesUseCase
import com.practicum.playlistmaker.domain.history.AddTrackToHistoryUseCase
import com.practicum.playlistmaker.domain.history.ClearSearchHistoryUseCase
import com.practicum.playlistmaker.domain.history.GetSearchHistoryUseCase
import com.practicum.playlistmaker.domain.playlist.AddTrackToPlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.CreatePlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.DeletePlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.DeleteTrackFromPlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.GetPlaylistByIdUseCase
import com.practicum.playlistmaker.domain.playlist.GetPlaylistsUseCase
import com.practicum.playlistmaker.domain.playlist.GetTracksForPlaylistUseCase
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

    // Favorites
    factory { AddToFavoritesUseCase(get()) }
    factory { RemoveFromFavoritesUseCase(get()) }
    factory { GetFavoritesUseCase(get()) }
    factory { IsFavoriteUseCase(get()) }

    // Playlist
    factory { CreatePlaylistUseCase(get()) }
    factory { GetPlaylistsUseCase(get()) }
    factory { AddTrackToPlaylistUseCase(get(), get()) }
    factory { GetPlaylistByIdUseCase(get()) }
    factory { GetTracksForPlaylistUseCase(get()) }
    factory { DeleteTrackFromPlaylistUseCase(get(), get()) }

    factory { DeletePlaylistUseCase(get(), get()) }
}