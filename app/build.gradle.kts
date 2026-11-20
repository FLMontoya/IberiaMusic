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
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(project(":presentation"))
  implementation(project(":data"))
  implementation(project(":domain"))
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.kotlinx.serialization)
  implementation(libs.okhttp)
  implementation(libs.kotlinx.serialization.json)

  testImplementation(platform(libs.androidx.compose.bom))
  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(platform(libs.androidx.compose.bom))
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
}
