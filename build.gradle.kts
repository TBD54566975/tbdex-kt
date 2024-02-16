import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URL

plugins {
  id("org.jetbrains.kotlin.jvm") version "1.9.22"
  id("base")
  id("io.gitlab.arturbosch.detekt") version "1.23.1"
  `maven-publish`
  id("org.jetbrains.dokka") version "1.9.0"
  id("org.jetbrains.kotlinx.kover") version "0.7.3"
  signing
  id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
  id("version-catalog")
}

repositories {
  mavenCentral()
}

dependencies {
  api(project(":protocol"))
  api(project(":httpclient"))
  api(project(":httpserver"))
  detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")
}

/**
 * Dependency forcing for build dependencies; see docs below on this
 * in the "allprojects" block for explanation why this is necessary.
 */
buildscript {
  configurations.all {
    resolutionStrategy {
      // Addresss https://github.com/TBD54566975/tbdex-kt/issues/167
      force("com.fasterxml.woodstox:woodstox-core:6.4.0")
    }
  }
}

allprojects {

  group = "xyz.block"

  configurations.all {
    /**
     * In this section we address build issues including security vulnerabilities
     * in transitive dependencies we don't explicitly declare in
     * `gradle/libs.versions.toml`. Forced actions taken here will override any
     * declarations we make, so use with care. Also note: these are in place for a
     * point in time. As we maintain this software, the manual forced resolution we do
     * here may:
     *
     * 1) No longer be necessary (if we have removed a dependency path leading to dep)
     * 2) Break an upgrade (if we upgrade a dependency and this forces a lower version
     *    of a transitive dependency it brings in)
     *
     * So we need to exercise care here, and, when upgrading our deps, check to see if
     * these forces aren't breaking things.
     *
     * When adding forces here, please reference the issue which explains why we
     * needed to do this; it will help future maintainers understand if the force
     * is still valid, should be removed, or handled in another way.
     *
     * When in doubt, ask! :)
     */
    resolutionStrategy {
      // Addresss https://github.com/TBD54566975/tbdex-kt/issues/167
      force("com.fasterxml.woodstox:woodstox-core:6.4.0")
      // Addresss https://github.com/TBD54566975/tbdex-kt/issues/168
      force("com.google.guava:guava:32.0.0-android")
      // Addresss https://github.com/TBD54566975/tbdex-kt/issues/169
      force("com.google.protobuf:protobuf-javalite:3.19.6")
    }
  }
}

subprojects {
  repositories {
    mavenCentral()
  }
  apply {
    plugin("io.gitlab.arturbosch.detekt")
    plugin("org.jetbrains.kotlin.jvm")
    plugin("java-library")
    plugin("maven-publish")
    plugin("org.jetbrains.dokka")
    plugin("org.jetbrains.kotlinx.kover")
    plugin("signing")
    plugin("version-catalog")
  }

  tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
  }
  dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")
  }

  detekt {
    config.setFrom("$rootDir/config/detekt.yml")
  }

  kotlin {
    jvmToolchain(11)
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_11)
      apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
      languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
    }
  }

  java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  val publicationName = "${rootProject.name}-${project.name}"
  publishing {
    publications {
      create<MavenPublication>(publicationName) {
        groupId = project.group.toString()
        artifactId = name
        description = name
        version = project.property("version").toString()
        from(components["java"])
      }

      withType<MavenPublication> {
        pom {
          name = publicationName
          packaging = "jar"
          description.set("tbDEX SDK for the JVM")
          url.set("https://github.com/TBD54566975/tbdex-kt")
          inceptionYear.set("2023")
          licenses {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("https://github.com/TBD54566975/tbdex-kt/blob/main/LICENSE")
            }
          }
          developers {
            developer {
              id.set("TBD54566975")
              name.set("Block Inc.")
              email.set("tbd-releases@tbd.email")
            }
          }
          scm {
            connection.set("scm:git:git@github.com:TBD54566975/tbdex-kt.git")
            developerConnection.set("scm:git:ssh:git@github.com:TBD54566975/tbdex-kt.git")
            url.set("https://github.com/TBD54566975/tbdex-kt")
          }
        }
      }
    }

    if (!project.hasProperty("skipSigning") || project.property("skipSigning") != "true") {
      signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications[publicationName])
      }
    }
  }

  tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
      documentedVisibilities.set(
        setOf(
          DokkaConfiguration.Visibility.PUBLIC,
          DokkaConfiguration.Visibility.PROTECTED
        )
      )

      sourceLink {
        val exampleDir = "https://github.com/TBD54566975/tbdex-kt/tree/main"

        localDirectory.set(rootProject.projectDir)
        remoteUrl.set(URL(exampleDir))
        remoteLineSuffix.set("#L")
      }
    }
  }

  tasks.test {
    useJUnitPlatform()
    reports {
      junitXml
    }
    testLogging {
      events("passed", "skipped", "failed", "standardOut", "standardError")
      exceptionFormat = TestExceptionFormat.FULL
      showExceptions = true
      showCauses = true
      showStackTraces = true
    }
  }
}

// Configures only the parent MultiModule task,
// this will not affect subprojects
tasks.dokkaHtmlMultiModule {
  moduleName.set("tbdex SDK Documentation")
}

publishing {
  publications {
    create<MavenPublication>(rootProject.name) {
      groupId = project.group.toString()
      artifactId = name
      description = name
      version = project.property("version").toString()
      from(components["java"])

      pom {
        packaging = "pom"
        name = "tbDEX SDK for the JVM"
        description.set("tbDEX SDK for the JVM")
        url.set("https://github.com/TBD54566975/tbdex-kt")
        inceptionYear.set("2023")
        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("https://github.com/TBD54566975/tbdex-kt/blob/main/LICENSE")
          }
        }
        developers {
          developer {
            id.set("TBD54566975")
            name.set("Block Inc.")
            email.set("tbd-releases@tbd.email")
          }
        }
        scm {
          connection.set("scm:git:git@github.com:TBD54566975/tbdex-kt.git")
          developerConnection.set("scm:git:ssh:git@github.com:TBD54566975/tbdex-kt.git")
          url.set("https://github.com/TBD54566975/tbdex-kt")
        }
      }
    }
  }
}

if (!project.hasProperty("skipSigning") || project.property("skipSigning") != "true") {
  signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["tbdex"])
  }
}

nexusPublishing {
  repositories {
    sonatype {  //only for users registered in Sonatype after 24 Feb 2021
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
    }
  }
}