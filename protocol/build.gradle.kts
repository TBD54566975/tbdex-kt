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


  // Project

  // Implementation
  implementation(libs.deFxlaeTypeId)
  implementation(libs.comFasterXmlJacksonModuleKotlin)
  implementation(libs.comFasterXmlJacksonDatatypeJsr310)
  implementation(libs.comNetworkntJsonSchemaValidator)

  // Test
  /**
   * Test dependencies may declare direct versions; they are not exported
   * and therefore are within the remit of this module to self-define
   * if desired.
   */
  testImplementation(kotlin("test"))
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
}

sourceSets {
  val test by getting {
    val resourceDirs = listOf(
      "../tbdex/hosted/test-vectors/protocol/vectors",
    )
    resources.setSrcDirs(resourceDirs)
  }

  main {
    val resourceDirs = listOf(
      "../tbdex/hosted/json-schemas"
    )

    resources.setSrcDirs(resourceDirs)
  }
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