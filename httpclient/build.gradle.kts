import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("java-library")
}

repositories {
  mavenCentral()
  // block's cache artifactory for tbd's oss third party dependencies
  // that do not live in maven central
  maven {
    name = "tbd-oss-thirdparty"
    url = uri("https://blockxyz.jfrog.io/artifactory/tbd-oss-thirdparty-maven2/")
    mavenContent {
      releasesOnly()
    }
  }
}

val jackson_version = "2.14.2"

dependencies {

  /**
   * Maintainers - please do not declare versioning here at the module level;
   * dependencies are either pulled in transitively via web5-kt, or
   * centralized for the platform in $projectRoot/gradle/libs.versions.toml
   *
   * Deps are declared in alphabetical order.
   */

  // API
  api(libs.xyzBlockWeb5)

  /*
   * API Leak: https://github.com/TBD54566975/tbdex-kt/issues/161
   *
   * Change and move to "implementation" when completed
   */
  api(libs.deFxlaeTypeId)

  // Project
  implementation(project(":protocol"))

  // Implementation
  implementation(libs.comSquareupOkhttpOkhttp)
  implementation(libs.comFasterXmlJacksonModuleKotlin)

  // Test
  /**
   * Test dependencies may declare direct versions; they are not exported
   * and therefore are within the remit of this module to self-define
   * if desired.
   */
  testImplementation(kotlin("test"))
  testImplementation(libs.comSquareupOkhttpMockwebserver)
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
  testImplementation("io.mockk:mockk:1.13.9")

}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed", "standardOut", "standardError")
    exceptionFormat = TestExceptionFormat.FULL
    showExceptions = true
    showCauses = true
    showStackTraces = true
  }
}

java {
  withJavadocJar()
  withSourcesJar()
}