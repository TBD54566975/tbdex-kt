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
}

val okHttpVersion = "4.9.1"

dependencies {
  implementation(project(":protocol"))
  implementation("de.fxlae:typeid-java-jdk8:0.2.0")
  implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
  implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:$okHttpVersion")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
  implementation("com.github.TBD54566975:web5-kt:0.0.9-beta")
  implementation("decentralized-identity:did-common-java:1.9.0") // would like to grab this via web5 dids

  testImplementation(kotlin("test"))
  testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")

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