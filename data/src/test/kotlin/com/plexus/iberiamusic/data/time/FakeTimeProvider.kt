package com.plexus.iberiamusic.data.time

/** Manually controlled clock for tests. */
class FakeTimeProvider(private var now: Long = 0L) : TimeProvider {

  override fun nowMillis(): Long = now

  fun advanceBy(millis: Long) {
    now += millis
  }
}
