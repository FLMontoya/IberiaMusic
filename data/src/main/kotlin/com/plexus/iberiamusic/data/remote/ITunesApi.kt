package com.plexus.iberiamusic.data.remote

import com.plexus.iberiamusic.data.remote.dto.ITunesSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** iTunes Search API (no key required). Restricted to songs and capped at [MAX_RESULTS]. */
interface ITunesApi {

  @GET("search?media=music&entity=song&limit=$MAX_RESULTS")
  suspend fun search(@Query("term") term: String): ITunesSearchResponseDto

  companion object {
    const val BASE_URL = "https://itunes.apple.com/"
    const val MAX_RESULTS = 50
  }
}
