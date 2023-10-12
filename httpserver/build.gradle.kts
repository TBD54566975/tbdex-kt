plugins {
  id("org.jetbrains.kotlin.jvm")
  id("java-library")
  id("idea")
}

version = "1.0"
var ktorVersion = "2.3.4"

repositories {
  mavenCentral()
  maven {
    url = uri("https://repo.danubetech.com/repository/maven-public")
  }
}

dependencies {
  testImplementation(kotlin("test"))
  testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
  implementation("io.ktor:ktor-server-core:$ktorVersion")
  implementation("io.ktor:ktor-server-netty:$ktorVersion")
  implementation("io.ktor:ktor-serialization:$ktorVersion")


}


tasks.test {
  useJUnitPlatform()
}

java {
  withJavadocJar()
  withSourcesJar()
}