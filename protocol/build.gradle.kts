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
}

dependencies {
  implementation("net.pwall.json:json-kotlin-schema:0.41")
  implementation("me.lessis:typeid:0.0.2")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.+")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}