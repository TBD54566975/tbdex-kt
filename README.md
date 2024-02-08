# tbdex-kt

[![License](https://img.shields.io/github/license/TBD54566975/tbdex-kt)](https://github.com/TBD54566975/tbdex-kt/blob/main/LICENSE) [![CI](https://github.com/TBD54566975/tbdex-kt/actions/workflows/ci.yaml/badge.svg)](https://github.com/TBD54566975/tbdex-kt/actions/workflows/ci.yaml) [![](https://jitpack.io/v/TBD54566975/tbdex-kt.svg)](https://jitpack.io/#TBD54566975/tbdex-kt) [![Coverage](https://img.shields.io/codecov/c/gh/tbd54566975/tbdex-kt/main?logo=codecov&logoColor=FFFFFF&style=flat-square&token=YI87CKF1LI)](https://codecov.io/github/TBD54566975/tbdex-kt) [![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/TBD54566975/tbdex-kt/badge)](https://securityscorecards.dev/viewer/?uri=github.com/TBD54566975/tbdex-kt)

This repo contains 2 jvm packages:

* [`/protocol`](./protocol/) - create, parse, verify, and validate the tbdex messages and resources defined in
  the [protocol draft specification](https://github.com/TBD54566975/tbdex/blob/main/specs/protocol/README.md)
* [`/httpclient`](./httpclient) - An HTTP client that can be used to send tbdex messages to PFIs

# Usage

tbdex is published to maven central but some additional repositories are needed for transitive dependencies currently: 

## Gradle

```kotlin
repositories {
  maven("https://jitpack.io")
  maven("https://repo.danubetech.com/repository/maven-public/")
}

dependencies {
  implementation("xyz.block:tbdex-httpclient:0.9.0-beta")
  implementation("xyz.block:tbdex-httpserver:0.9.0-beta")
  implementation("xyz.block:tbdex-protocol:0.9.0-beta")
}
```

> [!IMPORTANT]
> The repository at `https://repo.danubetech.com/repository/maven-public/` and `https://jitpack.io` are required for resolving transitive
> dependencies for now, but this should be temporary.
>

## Maven


<details>
  <summary>Expand for complete mvn pom.xml example using kotlin</summary>

  pom.xml:
  ```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
   
    <modelVersion>4.0.0</modelVersion>
    <groupId>website.tbd.developer.site</groupId>
    <artifactId>kotlin-testsuite</artifactId>
    <version>0.1.0-SNAPSHOT</version>

    <name>kotlin-testsuite</name>
    <url>http://developer.tbd.website</url>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <kotlin.jvm.target>17</kotlin.jvm.target>
        <kotlin.compiler.incremental>true</kotlin.compiler.incremental>
        <version.assertj>3.25.2</version.assertj>
        <version.kotlin>1.9.22</version.kotlin>
        <version.kotlin.compiler.incremental>true</version.kotlin.compiler.incremental>
        <version.junit-jupiter>5.10.1</version.junit-jupiter>

        <!-- TBD Dependencies -->
        <!--
        These need to be uniformly updated as part of the
        single dependency script from Nick
        -->
        <version.tbdex>0.9.0-beta</version.tbdex>

    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- External Dependencies -->
            <dependency>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-stdlib</artifactId>
                <version>${version.kotlin}</version>
            </dependency>

            <!-- TBD Dependencies -->
            <dependency>
                <groupId>xyz.block</groupId>
                <artifactId>tbdex-httpclient</artifactId>
                <version>${version.tbdex}</version>
            </dependency>
            <dependency>
                <groupId>xyz.block</groupId>
                <artifactId>tbdex-httpserver</artifactId>
                <version>${version.tbdex}</version>
            </dependency>
            <dependency>
                <groupId>xyz.block</groupId>
                <artifactId>tbdex-protocol</artifactId>
                <version>${version.tbdex}</version>
            </dependency>

            <!-- ALR Dark Arts to Make tbDEX and Web5 Play Nice -->
            <!--
            Will eventually go bye-bye once the
            underlying dep trees are fixed up
            -->
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
        </dependency>

        <!-- TBD Dependencies -->
        <dependency>
            <groupId>xyz.block</groupId>
            <artifactId>tbdex-httpclient</artifactId>
        </dependency>
        <dependency>
            <groupId>xyz.block</groupId>
            <artifactId>tbdex-protocol</artifactId>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>${project.basedir}/src/main/kotlin</sourceDirectory>
        <testSourceDirectory>${project.basedir}/src/test/kotlin</testSourceDirectory>
        <pluginManagement><!-- lock down plugins versions to avoid using Maven defaults (may be moved to parent pom) -->
            <plugins>
                <!-- clean lifecycle, see https://maven.apache.org/ref/current/maven-core/lifecycles.html#clean_Lifecycle -->
                <plugin>
                    <artifactId>maven-clean-plugin</artifactId>
                    <version>3.1.0</version>
                </plugin>
                <!-- default lifecycle, jar packaging: see https://maven.apache.org/ref/current/maven-core/default-bindings.html#Plugin_bindings_for_jar_packaging -->
                <plugin>
                    <artifactId>maven-resources-plugin</artifactId>
                    <version>3.0.2</version>
                </plugin>
                <plugin>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.8.0</version>
                </plugin>
                <plugin>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>2.22.1</version>
                </plugin>
                <plugin>
                    <artifactId>maven-jar-plugin</artifactId>
                    <version>3.0.2</version>
                </plugin>
                <plugin>
                    <artifactId>maven-install-plugin</artifactId>
                    <version>2.5.2</version>
                </plugin>
                <plugin>
                    <artifactId>maven-deploy-plugin</artifactId>
                    <version>2.8.2</version>
                </plugin>
                <!-- site lifecycle, see https://maven.apache.org/ref/current/maven-core/lifecycles.html#site_Lifecycle -->
                <plugin>
                    <artifactId>maven-site-plugin</artifactId>
                    <version>3.7.1</version>
                </plugin>
                <plugin>
                    <artifactId>maven-project-info-reports-plugin</artifactId>
                    <version>3.0.0</version>
                </plugin>
                <plugin>
                    <artifactId>kotlin-maven-plugin</artifactId>
                    <groupId>org.jetbrains.kotlin</groupId>
                    <version>${version.kotlin}</version>
                </plugin>
            </plugins>
        </pluginManagement>
        <plugins>
            <plugin>
                <artifactId>kotlin-maven-plugin</artifactId>
                <groupId>org.jetbrains.kotlin</groupId>
                <extensions>true</extensions>
                <configuration>
                    <jvmTarget>${kotlin.jvm.target}</jvmTarget>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <repositories>
        <repository>
            <id>jitpack</id>
            <name>jitpack</name>
            <url>https://jitpack.io</url>
        </repository>
        <repository>
            <id>danubetech</id>
            <name>danubetech</name>
            <url>https://repo.danubetech.com/repository/maven-public/</url>
        </repository>
    </repositories>
</project>
```

```xml
...

  <repository>
      <id>jitpack</id>
      <name>jitpack</name>
      <url>https://jitpack.io</url>
  </repository>
  <repository>
      <id>danubetech</id>
      <name>danubetech</name>
      <url>https://repo.danubetech.com/repository/maven-public/</url>
  </repository>

...

<dependency>
    <groupId>xyz.block</groupId>
    <artifactId>tbdex-httpclient</artifactId>
    <version>${version.tbdex}</version>
</dependency>
...
```

</details>

## Sample `Main.kt`

```kotlin
import web5.sdk.credentials.VerifiableCredential

fun main() {
    val signedVcJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFZERTQSIsImtpZCI6ImRpZDpkaHQ6a2ZkdGJjbTl6Z29jZjVtYXRmOWZ4dG5uZmZoaHp4YzdtZ2J3cjRrM3gzcXppYXVjcHA0eSMwIn0.eyJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSJdLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiRW1wbG95bWVudENyZWRlbnRpYWwiXSwiaWQiOiJ1cm46dXVpZDo4ZmQ1MjAzMC0xY2FmLTQ5NzgtYTM1ZC1kNDE3ZWI4ZTAwYjIiLCJpc3N1ZXIiOiJkaWQ6ZGh0OmtmZHRiY205emdvY2Y1bWF0ZjlmeHRubmZmaGh6eGM3bWdid3I0azN4M3F6aWF1Y3BwNHkiLCJpc3N1YW5jZURhdGUiOiIyMDIzLTEyLTIxVDE3OjAyOjAxWiIsImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImlkIjoiZGlkOmRodDp5MzltNDhvem9ldGU3ejZmemFhbmdjb3M4N2ZodWgxZHppN2Y3andiamZ0N290c2toOXRvIiwicG9zaXRpb24iOiJTb2Z0d2FyZSBEZXZlbG9wZXIiLCJzdGFydERhdGUiOiIyMDIxLTA0LTAxVDEyOjM0OjU2WiIsImVtcGxveW1lbnRTdGF0dXMiOiJDb250cmFjdG9yIn0sImV4cGlyYXRpb25EYXRlIjoiMjAyMi0wOS0zMFQxMjozNDo1NloifSwiaXNzIjoiZGlkOmRodDprZmR0YmNtOXpnb2NmNW1hdGY5Znh0bm5mZmhoenhjN21nYndyNGszeDNxemlhdWNwcDR5Iiwic3ViIjoiZGlkOmRodDp5MzltNDhvem9ldGU3ejZmemFhbmdjb3M4N2ZodWgxZHppN2Y3andiamZ0N290c2toOXRvIn0.ntcgPOdXOatULWo-q6gkuhKmi5X3bzCONQY38t_rsC1hVhvvdAtmiz-ccoLIYUkjECRHIxO_UZbOKgn0EETBCA"
    val vc = VerifiableCredential.parseJwt(signedVcJwt)
    print(vc)
}
```




# Development

## JSON Schemas

the [tbdex]() repo acts as the source of truth for all json schemas and test vectors. For this reason, the `tbdex` repo is a git
submodule
of this repo. By default, `git clone` does not actually check out the submodule's files. Using `--recurse-submodules`
option when cloning automatically initializes, fetches, and does a checkout of the appropriate commit for the submodule.
If you've already cloned the repo without `--recurse-submodules`, you can do the following:

```bash
git submodule update --init
```

copying the schemas and test vectors into the protocol package's `resources` directory can be done by running `./gradlew syncSchemas` and `./gradlew syncTestVectors` respectively.

# Other Docs

* [Guidelines](./CONVENTIONS.md)
* [Code of Conduct](./CODE_OF_CONDUCT.md)
* [Governance](./GOVERNANCE.md)
