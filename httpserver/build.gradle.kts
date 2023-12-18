import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("java-library")
  id("io.ktor.plugin") version "2.3.7"
}

repositories {
  mavenCentral()
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
  implementation(project(":protocol"))
  implementation(project(":httpclient"))
  implementation("de.fxlae:typeid-java-jdk8:0.2.0")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.0")
  implementation("com.github.TBD54566975:web5-kt:v0.0.9-gamma")
  implementation("decentralized-identity:did-common-java:1.9.0") // would like to grab this via web5 dids
  implementation("io.ktor:ktor-server-core")
  implementation("io.ktor:ktor-server-netty")
  implementation("io.ktor:ktor-server-content-negotiation")
  implementation("io.ktor:ktor-serialization-jackson")

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