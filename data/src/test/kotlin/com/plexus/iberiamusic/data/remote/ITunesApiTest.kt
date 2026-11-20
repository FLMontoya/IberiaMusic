package com.plexus.iberiamusic.data.remote

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Checks the real Retrofit contract against a local server: request shape and JSON parsing. */
class ITunesApiTest {

  private val server = MockWebServer()
  private lateinit var api: ITunesApi

  @BeforeTest
  fun setUp() {
    server.start()
    api =
        Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }
                    .asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(ITunesApi::class.java)
  }

  @AfterTest
  fun tearDown() {
    server.close()
  }

  @Test
  fun `asks for songs only, capped, with the term encoded`() = runTest {
    server.enqueue(MockResponse.Builder().body("""{"resultCount":0,"results":[]}""").build())

    api.search("queen & bowie")

    val url = server.takeRequest().url
    assertEquals("/search", url.encodedPath)
    assertEquals("queen & bowie", url.queryParameter("term"))
    assertEquals("music", url.queryParameter("media"))
    assertEquals("song", url.queryParameter("entity"))
    assertEquals(ITunesApi.MAX_RESULTS.toString(), url.queryParameter("limit"))
  }

  @Test
  fun `parses a response, ignoring fields it does not know and tolerating missing ones`() =
      runTest {
        server.enqueue(
            MockResponse.Builder()
                .addHeader("Content-Type", "text/javascript; charset=utf-8")
                .body(
                    """
                    {"resultCount":2,"results":[
                      {"trackId":1,"trackName":"Song","artistName":"Artist",
                       "collectionName":"Album","trackTimeMillis":210000,"kind":"song"},
                      {"trackId":2}
                    ]}
                    """
                        .trimIndent(),
                )
                .build(),
        )

        val response = api.search("queen")

        assertEquals(2, response.results.size)
        assertEquals("Album", response.results[0].collectionName)
        assertEquals(210_000L, response.results[0].trackTimeMillis)
        assertEquals(null, response.results[1].trackName)
      }
}
