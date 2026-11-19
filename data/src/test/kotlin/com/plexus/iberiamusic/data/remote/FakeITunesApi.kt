package com.plexus.iberiamusic.data.remote

import com.plexus.iberiamusic.data.remote.dto.ITunesSearchResponseDto

/** Test double that counts calls and answers with a configurable response or failure. */
class FakeITunesApi(
    var response: ITunesSearchResponseDto = ITunesSearchResponseDto(),
    var failure: Throwable? = null,
) : ITunesApi {

  var callCount = 0
    private set

  override suspend fun search(term: String): ITunesSearchResponseDto {
    callCount++
    failure?.let { throw it }
    return response
  }
}
