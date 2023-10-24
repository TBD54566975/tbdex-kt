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
}

dependencies {
  implementation(project(":protocol"))
  implementation("me.lessis:typeid:0.0.2")
  implementation("com.squareup.okhttp3:okhttp:4.9.1")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.9.8")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
//  implementation("com.github.TBD54566975:web5-kt:0.0.5-beta")
  implementation(files("/Users/jiyoon/.m2/repository/web5/common/0.0.5/common-0.0.5.jar"))
  implementation(files("/Users/jiyoon/.m2/repository/web5/crypto/0.0.5/crypto-0.0.5.jar"))
  implementation(files("/Users/jiyoon/.m2/repository/web5/credentials/0.0.5/credentials-0.0.5.jar"))
  implementation(files("/Users/jiyoon/.m2/repository/web5/dids/0.0.5/dids-0.0.5.jar"))
  implementation("decentralized-identity:did-common-java:1.9.0") // would like to grab this via web5 dids
  implementation("com.nimbusds:nimbus-jose-jwt:9.37")
  api("com.danubetech:verifiable-credentials-java:1.5.0")

  testImplementation(kotlin("test"))
  testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")

}

sourceSets {
  main {
    kotlin {
      srcDir("integration-tests")  // Change this to include your custom folder
    }
  }
}

tasks.test {
  useJUnitPlatform()
}

java {
  withJavadocJar()
  withSourcesJar()
}