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
  implementation("io.ktor:ktor-client-core:2.3.4")
  implementation("io.ktor:ktor-client-cio:2.3.4")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

java {
  withJavadocJar()
  withSourcesJar()
}