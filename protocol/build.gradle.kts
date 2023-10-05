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
  implementation("net.pwall.json:json-kotlin-schema:0.41")
  implementation("me.lessis:typeid:0.0.2")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.15.2")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:common:0.0.0-alpha")
  testImplementation(kotlin("test"))
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
}

tasks.test {
  useJUnitPlatform()
}

java {
  withJavadocJar()
  withSourcesJar()
}