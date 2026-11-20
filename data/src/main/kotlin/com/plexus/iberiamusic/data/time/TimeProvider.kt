package com.plexus.iberiamusic.data.time

/**
 * Source of a monotonic time, injectable so time-dependent logic can be tested. Values are only
 * meaningful relative to each other, not as a calendar date.
 */
interface TimeProvider {
  fun nowMillis(): Long
}

/** [TimeProvider] backed by the JVM monotonic clock, unaffected by changes to the device date. */
object SystemTimeProvider : TimeProvider {
  override fun nowMillis(): Long = System.nanoTime() / NANOS_PER_MILLI

  private const val NANOS_PER_MILLI = 1_000_000L
}
