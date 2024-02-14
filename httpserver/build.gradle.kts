import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("java-library")
  id("io.ktor.plugin") version "2.3.7"
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

application {
  mainClass.set("tbdex.sdk.httpserver.TbdexHttpServerKt")
}
val jackson_version = "2.14.2"

dependencies {
  api("de.fxlae:typeid-java-jdk8:0.2.0")
  api("xyz.block:web5:0.0.11")

  implementation(project(":protocol"))
  implementation(project(":httpclient"))
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
  implementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")
  implementation("decentralized-identity:did-common-java:1.9.0") // would like to grab this via web5 dids
  implementation("io.ktor:ktor-server-core")
  implementation("io.ktor:ktor-server-netty")
  implementation("io.ktor:ktor-server-content-negotiation")
  implementation("io.ktor:ktor-serialization-jackson")
  implementation("io.ktor:ktor-client-auth:2.3.7")

  testImplementation(kotlin("test"))
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
  testImplementation("io.ktor:ktor-server-test-host")
  testImplementation("io.ktor:ktor-client-content-negotiation")
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