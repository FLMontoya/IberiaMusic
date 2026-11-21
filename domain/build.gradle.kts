import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktfmt)
  jacoco
}

jacoco { toolVersion = libs.versions.jacoco.get() }

tasks.test { finalizedBy(tasks.jacocoTestReport) }

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required = true
    html.required = true
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }

dependencies {
  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlinx.coroutines.test)
}
