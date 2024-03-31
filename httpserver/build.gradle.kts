import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("java-library")
  id("io.ktor.plugin") version "2.3.7"
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
application {
  mainClass.set("tbdex.sdk.httpserver.TbdexHttpServerKt")
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

  /*
   * API Leak: https://github.com/TBD54566975/tbdex-kt/issues/161
   *
   * Change and move to "implementation" when completed
   */
  api(libs.deFxlaeTypeId)

  // Project
  implementation(project(":protocol"))
  implementation(project(":httpclient"))

  // Implementation
  implementation(libs.comFasterXmlJacksonDatatypeJsr310)
  implementation(libs.ioKtorClientAuth)
  implementation(libs.ioKtorSerializationJackson)
  implementation(libs.ioKtorServerContentNegotiation)
  implementation(libs.ioKtorServerNetty)

  // Test
  /**
   * Test dependencies may declare direct versions; they are not exported
   * and therefore are within the remit of this module to self-define
   * if desired.
   */
  testImplementation(kotlin("test"))
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
  testImplementation("io.ktor:ktor-server-test-host")
  testImplementation("io.ktor:ktor-client-content-negotiation")
  testImplementation("io.mockk:mockk:1.11.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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