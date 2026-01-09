package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.NetworkClient
import com.practicum.playlistmaker.data.dto.TracksSearchRequest
import com.practicum.playlistmaker.data.dto.TracksSearchResponse
import com.practicum.playlistmaker.domain.track.TracksRepository
import com.practicum.playlistmaker.domain.track.Track
import java.util.Objects

class TracksRepositoryImpl(private val networkClient: NetworkClient) : TracksRepository {
    override fun searchTrack(searchString: String): List<Track> {
        val response = networkClient.doRequest(TracksSearchRequest(searchString))
        if (response.resultCode == 200) {
            return (response as TracksSearchResponse).results.filter { Objects.nonNull(it) }.map {
                Track(
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
            }
        } else {
            throw RuntimeException("Network error")
        }
    }
}