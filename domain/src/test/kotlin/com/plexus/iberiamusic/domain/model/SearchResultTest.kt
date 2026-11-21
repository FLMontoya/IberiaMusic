package com.plexus.iberiamusic.domain.model

import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.junit.Test

class SearchResultTest {

  @Test
  fun `an error says why the search failed`() {
    val error = SearchResult.Error(IOException("offline"), SearchFailure.NETWORK)

    assertEquals(SearchFailure.NETWORK, error.reason)
  }

  @Test
  fun `an error with no stated reason is an unknown one`() {
    val error = SearchResult.Error(IllegalStateException("boom"))

    assertEquals(SearchFailure.UNKNOWN, error.reason)
  }

  @Test
  fun `errors with the same cause but a different reason are not equal`() {
    val cause = IOException("offline")

    assertNotEquals(
        SearchResult.Error(cause, SearchFailure.NETWORK),
        SearchResult.Error(cause, SearchFailure.SERVER),
    )
  }
}
