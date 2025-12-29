package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.data.network.RetrofitNetworkClient
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.usecase.SearchTracksUseCase

object Creator {

    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideSearchTracksUseCase(): SearchTracksUseCase {
        return SearchTracksUseCase(getTracksRepository())
    }
}
