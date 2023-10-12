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
  api("me.lessis:typeid:0.0.2")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-json-org:2.11.0")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:common:main-SNAPSHOT")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:credentials:main-SNAPSHOT")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:crypto:main-SNAPSHOT")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:dids:main-SNAPSHOT")
  implementation("com.github.erosb:everit-json-schema:1.14.2")
  implementation("decentralized-identity:did-common-java:1.9.0")

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
}

java {
  withJavadocJar()
  withSourcesJar()
}