import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("java-library")
}

repositories {
  mavenCentral()
  // temp maven repo for danubetech
  maven {
    name = "tbd-danubetech-temp"
    url = uri("https://blockxyz.jfrog.io/artifactory/danubetech-temp/")
    mavenContent {
      releasesOnly()
    }
  }
  maven {
    url = uri("https://repo.danubetech.com/repository/maven-public")
  }
  maven {
    url = uri("https://jitpack.io")
  }
  maven {
    url = uri("https://repository.jboss.org/nexus/content/repositories/thirdparty-releases/")
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
  /*
   * API Leak: https://github.com/TBD54566975/tbdex-kt/issues/161
   *
   * Change and move to "implementation" when completed
   */
  api(libs.deFxlaeTypeId)

  // Project

  // Implementation
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