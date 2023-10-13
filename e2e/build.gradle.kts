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
  implementation("com.github.TBD54566975.web5-sdk-kotlin:common:main-SNAPSHOT")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:credentials:main-SNAPSHOT")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:crypto:main-SNAPSHOT")
  implementation("com.github.TBD54566975.web5-sdk-kotlin:dids:main-SNAPSHOT")
  testImplementation(platform("org.junit:junit-bom:5.9.1"))
  testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}