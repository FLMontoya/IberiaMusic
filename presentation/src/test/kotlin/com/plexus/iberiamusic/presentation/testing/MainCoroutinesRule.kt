package com.plexus.iberiamusic.presentation.testing

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Replaces Dispatchers.Main with a [StandardTestDispatcher] for the duration of each test, so
 * `viewModelScope` and the test body share one virtual clock. Use [runTest] to run the test body.
 */
class MainCoroutinesRule : TestWatcher() {

  private val dispatcher = StandardTestDispatcher()
  private val testScope = TestScope(dispatcher)

  override fun starting(description: Description) {
    Dispatchers.setMain(dispatcher)
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
  }

  fun runTest(block: suspend TestScope.() -> Unit) =
      testScope.runTest(timeout = 2.seconds, testBody = block)
}
