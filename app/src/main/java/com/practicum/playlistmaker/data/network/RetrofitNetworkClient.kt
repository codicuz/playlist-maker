package com.practicum.playlistmaker.data.network

import com.practicum.playlistmaker.data.NetworkClient
import com.practicum.playlistmaker.data.dto.Response
import com.practicum.playlistmaker.data.dto.TracksSearchRequest

class RetrofitNetworkClient(private val iTunesApi: ITunesApi) : NetworkClient {

    override suspend fun doRequest(dto: Any): Response {
        return if (dto is TracksSearchRequest) {
            val response = iTunesApi.findSong(dto.expression)
            response.apply { resultCode = 200 }
        } else {
            Response().apply { resultCode = 400 }
        }
    }
}
