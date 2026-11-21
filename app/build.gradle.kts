plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ktfmt)
}

android {
  namespace = "com.plexus.iberiamusic"
  compileSdk { version = release(37) }

  defaultConfig {
    applicationId = "com.plexus.iberiamusic"
    minSdk = 26
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      optimization {
        enable = true
        packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
      }
    }

    // The release build (R8 on), signed with the debug key and with its own application id, so it
    // installs next to the debug build and can be tried on a device. Not meant for publishing.
    create("staging") {
      initWith(getByName("release"))
      applicationIdSuffix = ".staging"
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += "release"
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {
  implementation(project(":presentation"))
  implementation(project(":data"))
  implementation(project(":domain"))
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.kotlinx.serialization)
  implementation(libs.okhttp)
  implementation(libs.kotlinx.serialization.json)

  debugImplementation(platform(libs.androidx.compose.bom))
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  // Espresso 3.7 needs a newer concurrent-futures than the app resolves on its own; AGP keeps both
  // classpaths consistent.
  debugImplementation(libs.androidx.concurrent.futures)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4.accessibility)
  androidTestImplementation(libs.mockk.android)
}

// The Accessibility Test Framework brings Guava, which already contains ListenableFuture. Its empty
// "listenablefuture:9999.0" placeholder clashes with the 1.0 pinned by consistent resolution.
configurations
    .matching { it.name.contains("AndroidTest") }
    .configureEach { exclude(group = "com.google.guava", module = "listenablefuture") }
