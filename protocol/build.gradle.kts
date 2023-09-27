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
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}