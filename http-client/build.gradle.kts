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
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}

java {
  withJavadocJar()
  withSourcesJar()
}