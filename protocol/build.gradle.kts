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
  implementation("net.pwall.json:json-kotlin-schema:0.42")
  implementation("me.lessis:typeid:0.0.2")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:common:0.0.0-alpha")
//  implementation("com.github.erosb:everit-json-schema:1.14.2")
//  implementation("com.worldturner.medeia:medeia-validator-jackson:1.1.0")
  testImplementation(kotlin("test"))
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
}

tasks {
  register("copySchemas", Copy::class) {
    from("../tbdex/json-schemas")
    into("./src/main/resources")
  }
}

tasks.test {
  useJUnitPlatform()
}

java {
  withJavadocJar()
  withSourcesJar()
}