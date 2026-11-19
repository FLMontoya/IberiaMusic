package com.plexus.iberiamusic.data.time

/** Source of the current time, injectable so time-dependent logic can be tested. */
interface TimeProvider {
  fun nowMillis(): Long
}

/** [TimeProvider] backed by the system wall clock. */
object SystemTimeProvider : TimeProvider {
  override fun nowMillis(): Long = System.currentTimeMillis()
}
