import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("org.jetbrains.kotlin.jvm")
  id("java-library")
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
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
  implementation("com.github.TBD54566975:web5-kt:v0.0.9-gamma")
  implementation("decentralized-identity:did-common-java:1.9.0") // would like to grab this via web5 dids
  implementation("io.ktor:ktor-server-core:2.3.4")
  implementation("io.ktor:ktor-server-netty:2.3.4")
  implementation("io.ktor:ktor-server-content-negotiation:2.3.4")
  implementation("io.ktor:ktor-serialization-jackson:2.3.4")

  testImplementation(kotlin("test"))
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
  testImplementation("io.ktor:ktor-server-test-host:2.3.4")
  testImplementation("com.squareup.okhttp3:okhttp:4.11.0")
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