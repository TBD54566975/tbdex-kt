plugins {
  id("org.jetbrains.kotlin.jvm")
  id("java-library")
}

version = "1.0"

repositories {
  mavenCentral()
  maven {
    url = uri("https://repo.danubetech.com/repository/maven-public")
  }
  maven {
    url = uri("https://jitpack.io")
  }
}

dependencies {
  implementation(project(":protocol"))
  implementation("me.lessis:typeid:0.0.2")
  implementation("com.squareup.okhttp3:okhttp:4.9.1")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:crypto:main-SNAPSHOT")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:dids:main-SNAPSHOT")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:common:main-SNAPSHOT")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:credentials:main-SNAPSHOT")
  implementation("decentralized-identity:did-common-java:1.9.0") // would like to grab this via web5 dids
  implementation(project(mapOf("path" to ":protocol")))

  testImplementation(kotlin("test"))
  testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
}

tasks.test {
  useJUnitPlatform()
}

java {
  withJavadocJar()
  withSourcesJar()
}