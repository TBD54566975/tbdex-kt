plugins {
  id("java")
}

group = "tbdex"
version = "0.0.0"

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
  implementation(project(":httpclient"))
  implementation("me.lessis:typeid:0.0.2")
  implementation("com.github.TBD54566975:web5-kt:main-SNAPSHOT")
  testImplementation(platform("org.junit:junit-bom:5.9.1"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}