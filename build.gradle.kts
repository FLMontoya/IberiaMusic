// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kotlin.compose) apply false
}

tasks.register("coverage") {
  group = "verification"
  description = "Runs the unit tests and builds the JaCoCo coverage report of each module."
  dependsOn(
      ":domain:jacocoTestReport",
      ":data:jacocoTestReport",
      ":presentation:createDebugUnitTestCoverageReport",
  )
}