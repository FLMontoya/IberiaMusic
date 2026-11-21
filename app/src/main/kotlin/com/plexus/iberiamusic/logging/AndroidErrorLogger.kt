package com.plexus.iberiamusic.logging

import android.util.Log
import com.plexus.iberiamusic.domain.logging.ErrorLogger

/** Sends the reports to Logcat. A crash reporter would be plugged in here. */
object AndroidErrorLogger : ErrorLogger {

  private const val TAG = "IberiaMusic"

  override fun log(message: String, cause: Throwable) {
    Log.w(TAG, message, cause)
  }
}
