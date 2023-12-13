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
  api("de.fxlae:typeid-java-jdk8:0.2.0")
  api("xyz.block:web5:0.0.9-delta")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-json-org:2.11.0")
  implementation("com.networknt:json-schema-validator:1.0.87")
  implementation("com.nimbusds:nimbus-jose-jwt:9.36")
  implementation("decentralized-identity:did-common-java:1.9.0")
  implementation("io.github.erdtman:java-json-canonicalization:1.1")
  testImplementation(kotlin("test"))
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
}

tasks {
  register("syncSchemas", Sync::class) {
    from("../tbdex/json-schemas")
    into("./src/main/resources")
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