plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.ktfmt)
}

android {
  namespace = "com.plexus.iberiamusic.presentation"
  compileSdk { version = release(37) }

  defaultConfig { minSdk = 26 }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {
  implementation(project(":domain"))
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
}
