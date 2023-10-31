plugins {
  id("java")
}

group = "org.example"
version = "0.3.0"

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.github.f4b6a3:uuid-creator:5.3.3")
  testImplementation(platform("org.junit:junit-bom:5.9.1"))
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
  testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  testImplementation("com.willowtreeapps.assertk:assertk:0.27.0")
}

tasks.test {
  useJUnitPlatform()
}