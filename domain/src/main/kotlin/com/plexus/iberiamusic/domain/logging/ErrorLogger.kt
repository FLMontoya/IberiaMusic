package com.plexus.iberiamusic.domain.logging

/**
 * Where unexpected problems are reported. The domain and data layers only know this contract; the
 * app decides where the reports go (Logcat, a crash reporter...), so they stay free of Android.
 */
fun interface ErrorLogger {
  fun log(message: String, cause: Throwable)
}
