package com.plexus.iberiamusic

import android.app.Application
import com.plexus.iberiamusic.di.AppContainer

/** Composition root: owns the [AppContainer] for the lifetime of the process. */
class IberiaMusicApplication : Application() {

  val container: AppContainer by lazy { AppContainer() }
}
