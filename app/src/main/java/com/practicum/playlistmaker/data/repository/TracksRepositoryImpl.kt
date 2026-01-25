package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.NetworkClient
import com.practicum.playlistmaker.data.dto.TracksSearchRequest
import com.practicum.playlistmaker.data.dto.TracksSearchResponse
import com.practicum.playlistmaker.domain.track.Track
import com.practicum.playlistmaker.domain.track.TracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(
    private val networkClient: NetworkClient
) : TracksRepository {

    override fun searchTrack(searchString: String): Flow<List<Track>> = flow {
        val response = networkClient.doRequest(TracksSearchRequest(searchString))
        if (response.resultCode == 200) {
            emit(
                (response as TracksSearchResponse).results.map {
                    Track(
                        it.id,
                        it.trackId,
                        it.trackName,
                        it.artistsName,
                        it.trackTimeMillis,
                        it.artworkUrl100,
                        it.previewUrl,
                        it.collectionName,
                        it.releaseDate,
                        it.primaryGenreName,
                        it.country
                    )
                })
        } else {
            throw Exception("Network error: ${response.resultCode}")
        }
    }
}

