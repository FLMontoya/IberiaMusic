package com.plexus.iberiamusic.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ITunesSearchResponseDto(
    val resultCount: Int = 0,
    val results: List<ITunesTrackDto> = emptyList(),
)

/** One result of the iTunes Search API. Every field is nullable because the API may omit it. */
@Serializable
data class ITunesTrackDto(
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val collectionName: String? = null,
    val artworkUrl100: String? = null,
    val primaryGenreName: String? = null,
    val trackTimeMillis: Long? = null,
    val releaseDate: String? = null,
)
