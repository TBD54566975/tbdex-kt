# ⚠️ Deprecated Repository ⚠️
This repository is deprecated.

The last version of tbdex-kt that used this repository is 3.0.1.

Starting from version 4.0.0, the project has transitioned to using the tbdex-rs kotlin bindings repository found here: https://github.com/TBD54566975/tbdex-rs/tree/main/bound/kt.

## tbdex-sdk

[![License](https://img.shields.io/github/license/TBD54566975/tbdex-kt)](https://github.com/TBD54566975/tbdex-kt/blob/main/LICENSE) [![SDK Kotlin CI](https://github.com/TBD54566975/tbdex-kt/actions/workflows/ci.yml/badge.svg)](https://github.com/TBD54566975/tbdex-kt/actions/workflows/ci.yml) [![Coverage](https://img.shields.io/codecov/c/gh/tbd54566975/tbdex-kt/main?logo=codecov&logoColor=FFFFFF&style=flat-square&token=YI87CKF1LI)](https://codecov.io/github/TBD54566975/tbdex-kt) [![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/TBD54566975/tbdex-kt/badge)](https://securityscorecards.dev/viewer/?uri=github.com/TBD54566975/tbdex-kt)

This repo contains packages:

* [`/protocol`](./protocol/) - create, parse, verify, and validate the tbdex messages and resources defined in
  the [protocol draft specification](https://github.com/TBD54566975/tbdex/blob/main/specs/protocol/README.md)
* [`/httpclient`](./httpclient) - An HTTP client that can be used to send tbdex messages to PFIs
* [`/httpserver`](./httpserver) - Base implementation of a tbDEX HTTP server responsible for handling RFQs, orders, and other interactions
* [`/distribution`](./distribution) - The full tbDEX Platform

# Usage

tbDEX is available
[from Maven Central](https://central.sonatype.com/artifact/xyz.block/tbdex). Instructions for
adding the dependency in a variety of build tools including Maven and Gradle are linked there.

> [!IMPORTANT]
> tbDEX contains transitive dependencies not
> found in Maven Central. To resolve these, add the
> [TBD thirdparty repository](https://blockxyz.jfrog.io/artifactory/tbd-oss-thirdparty-maven2/)
> to your Maven or Gradle config.
>
> For instance, in your Maven `pom.xml`:
>
> ```shell
> <repositories>
>   <repository>
>     <id>tbd-oss-thirdparty</id>
>     <name>tbd-oss-thirdparty</name>
>     <releases>
>       <enabled>true</enabled>
>     </releases>
>     <snapshots>
>       <enabled>false</enabled>
>     </snapshots>
>     <url>https://blockxyz.jfrog.io/artifactory/tbd-oss-thirdparty-maven2/</url>
>   </repository>
> </repositories>
> ```
>
> ...or in your `gradle.settings.kts`:
>
> ```shell
> dependencyResolutionManagement {
>   repositories {
>       mavenCentral()
>       // Thirdparty dependencies of TBD projects not in Maven Central
>       maven("https://blockxyz.jfrog.io/artifactory/tbd-oss-thirdparty-maven2/")
> }
> ```

# Development

## Prerequisites

### Cloning

This repository uses git submodules. To clone this repo with submodules

```sh
git clone --recurse-submodules git@github.com:TBD54566975/tbdex-kt.git
```

Or to add submodules after cloning

```sh
git submodule update --init
```

### Hermit

This project uses hermit to manage tooling like Maven and Java versions.
See [this page](https://cashapp.github.io/hermit/usage/get-started/) to set up Hermit on your machine - make sure to
download the open source build and activate it for the project.

Once you've installed Hermit and before running builds on this repo,
run from the root:

```shell
source ./bin/activate-hermit
```

This will set your environment up correctly in the
terminal emulator you're on.

## Building with Maven

This project is built with the
[Maven Project Management](https://maven.apache.org/) tool.
It is installed via Hermit above.

If you want to build an artifact on your local filesystem, you can do so by running the
following command - either at the top level or in
any of the subprojects:

```shell
mvn clean verify
```

This will first clean all previous builds and compiled code, then:
compile, test, and build the artifacts in each of the submodules
of this project in the `$moduleName/target` directory, for example:

```shell
ls -l httpserver/target
```

You should see similar to:

```shell
total 240
drwxr-xr-x@  4 alr  staff    128 Apr  4 00:29 classes
drwxr-xr-x@  4 alr  staff    128 Apr  4 00:29 generated-sources
drwxr-xr-x@  4 alr  staff    128 Apr  4 00:29 kaptStubs
drwxr-xr-x@  4 alr  staff    128 Apr  4 00:29 kotlin-ic
drwxr-xr-x@  3 alr  staff     96 Apr  4 00:30 kover
drwxr-xr-x@  3 alr  staff     96 Apr  4 00:30 maven-archiver
drwxr-xr-x@  3 alr  staff     96 Apr  4 00:29 maven-status
drwxr-xr-x@  3 alr  staff     96 Apr  4 00:30 site
drwxr-xr-x@ 18 alr  staff    576 Apr  4 00:30 surefire-reports
-rw-r--r--@  1 alr  staff  17889 Apr  4 00:30 tbdex-httpserver-0.0.0-main-SNAPSHOT-sources.jar
-rw-r--r--@  1 alr  staff  99334 Apr  4 00:30 tbdex-httpserver-0.0.0-main-SNAPSHOT.jar
drwxr-xr-x@ 11 alr  staff    352 Apr  4 00:29 test-classes
drwxr-xr-x@  4 alr  staff    128 Apr  4 00:30 tmp
```

If you'd like to skip packaging and test only, run:

```shell
mvn test
```

You may also run a single test; `cd` into the submodule of choice,
then use the `-Dtest=` parameter to denote which test to run, for example:

```shell
cd httpclient; \
mvn test -Dtest=TestClassName
```

To install builds into your local Maven repository, run from the root:

```shell
mvn install
```

For more, see the documentation on [Maven Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).

## Generating API Docs Locally

We use [Dokka](https://kotlinlang.org/docs/dokka-cli.html) to create the
HTML API Documentation for this project. This is done using the Dokka CLI
because the [Dokka Maven Plugin](https://kotlinlang.org/docs/dokka-maven.html)
does not yet support multimodule builds. To run locally, obtain the Dokka CLI.
Run from the root of this repo:

```shell
# it will download the jars into the `target/dokka-cli` folder and generate the docs
./scripts/dokka.sh
```

These will be available in `target/apidocs`.

This step is handled during releases and published via GitHub Actions.

## Publishing Docs

API reference documentation is automatically updated are available
at [https://tbd54566975.github.io/tbdex-kt/docs/](https://tbd54566975.github.io/tbdex-kt/docs/)
following each automatically generated release.

## Dependency Management

As tbDEX is a platform intended to run alongside Web5 in a single `ClassLoader`,
versions and dependencies must be aligned among the subprojects
(sometimes called modules) of this project. To address, we declare
versions in `pom.xml`'s `<dependencyManagement>` section and
import references defined there in the subproject `pom.xml`s' `<dependencies>`
sections. Versions themselves are defined as properties in the root `pom.xml`.
See further documentation on versioning and dependency management there.

The root `pom.xml` may also be imported in projects building atop
tbDEX in `import` scope to respect these dependency declarations.

### Updating the Web5 Dependency

This build extends from the Web5 build. Therefore, updates to Web5 must be 
done in 2 places in the root `pom.xml`:

1. In `<properties>`, where `<version.xyz.block.web5>` is defined.
2. In the `<parent>`, where the `tbdex-parent` has a 
   parent of `web5-parent`. This version number cannot be referenced from
   the version property defined by 1., so it must be updated in tandem.

## Release Guidelines

### Pre-releases

In Kotlin we use the SNAPSHOT convention to build and publish a pre-release package that can be consumed for preview/testing/development purposes.

These SNAPSHOTs are generated and published whenever there's a new push to `main`. If you want to manually kick that off to preview some changes introduced in a PR branch:

1. Open the [SDK Kotlin CI Workflow](https://github.com/TBD54566975/tbdex-kt/actions/workflows/ci.yml), press the **Run workflow button** selecting the branch you want to generate the snapshot from.

2. In the version field, insert the current version, a short meaningful identifier and the `-SNAPSHOT` suffix, ie:

  - 0.11.0.pr123-SNAPSHOT
  - 0.11.0.shortsha-SNAPSHOT
  - 0.11.0.fixsomething-SNAPSHOT

3. Run workflow!

You **MUST** use the `-SNAPSHOT` suffix, otherwise it's not a valid preview `SNAPSHOT` and it will be rejected.

`SNAPSHOT`s will be available in [TBD's Artifactory `tbd-oss-snapshots-maven2` Repository](https://blockxyz.jfrog.io/artifactory/tbd-oss-snapshots-maven2).

### Releasing New Versions

To release a new version, execute the following steps:

1. Open the [Release and Publish](https://github.com/TBD54566975/tbdex-kt/actions/workflows/release.yml), press the **Run workflow button** selecting the branch you want to generate the snapshot from.

2. In the version field, declare the version to be released. ie:

  - 0.15.2
  - 0.17.0-alpha-3
  - 1.6.3

  - **Choose an appropriate version number based on semver rules. Remember that versions are immutable once published to Maven Central; they cannot be altered or removed.**

3. Press the **Run workflow button** and leave the main branch selected (unless its a rare case where you don't want to build from the main branch for the release).

4. Run workflow! This:

- Builds
- Tests
- Creates artifacts for binaries and sources
- Signs artifacts
- Uploads artifacts to TBD Artifactory
- Tags git with release number "v$version"
- Keeps development version in the pom.xml to 0.0.0-main-SNAPSHOT
- Pushes changes to git
- Triggers job to:
  - Build from tag and upload to Maven Central
  - Create GitHub Release "v$version"built and **published to maven central**, **docs will be published** (see below) and **the GitHub release will be automatically generated**!
  - Publish API Docs

### Publishing a `SNAPSHOT` from a Local Dev Machine

Please take care to only publish `-SNAPSHOT` builds (ie.
when the `<version>` field of the `pom.xml` ends in
`-SNAPSHOT`.) unless there's good reason
to deploy a non-`SNAPSHOT` release. Releases are typically handled via automation
in GitHub Actions s documented above.

To deploy to TBD's Artifactory instance for sharing with others, you
need your Artifactory username and password handy (available to TBD-employed engineers).
Set environment variables:

```shell
export ARTIFACTORY_USERNAME=yourUsername; \
export ARTIFACTORY_PASSWORD=yourPassword
```

...then run:

```shell
mvn deploy --settings .maven_settings.xml
```
## Working with the `tbdex` submodule

### Pulling

You may need to update the `tbdex` submodule after pulling.

```sh
git pull
git submodule update
```
### Pushing

If you have made changes to the `tbdex` submodule, you should push your changes to the `tbdex` remote as well as
pushing changes to `tbdex-kt`.

```sh
cd tbdex
git checkout main
git checkout -b my-branch
git add .
git commit -m "your commit message"
git push
cd ..
git add .
git commit -m "updating tbdex submodule"
git push
```

## Sample `Main.kt`

```kotlin
import web5.sdk.credentials.VerifiableCredential

fun main() {
    val signedVcJwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFZERTQSIsImtpZCI6ImRpZDpkaHQ6a2ZkdGJjbTl6Z29jZjVtYXRmOWZ4dG5uZmZoaHp4YzdtZ2J3cjRrM3gzcXppYXVjcHA0eSMwIn0.eyJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSJdLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiRW1wbG95bWVudENyZWRlbnRpYWwiXSwiaWQiOiJ1cm46dXVpZDo4ZmQ1MjAzMC0xY2FmLTQ5NzgtYTM1ZC1kNDE3ZWI4ZTAwYjIiLCJpc3N1ZXIiOiJkaWQ6ZGh0OmtmZHRiY205emdvY2Y1bWF0ZjlmeHRubmZmaGh6eGM3bWdid3I0azN4M3F6aWF1Y3BwNHkiLCJpc3N1YW5jZURhdGUiOiIyMDIzLTEyLTIxVDE3OjAyOjAxWiIsImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImlkIjoiZGlkOmRodDp5MzltNDhvem9ldGU3ejZmemFhbmdjb3M4N2ZodWgxZHppN2Y3andiamZ0N290c2toOXRvIiwicG9zaXRpb24iOiJTb2Z0d2FyZSBEZXZlbG9wZXIiLCJzdGFydERhdGUiOiIyMDIxLTA0LTAxVDEyOjM0OjU2WiIsImVtcGxveW1lbnRTdGF0dXMiOiJDb250cmFjdG9yIn0sImV4cGlyYXRpb25EYXRlIjoiMjAyMi0wOS0zMFQxMjozNDo1NloifSwiaXNzIjoiZGlkOmRodDprZmR0YmNtOXpnb2NmNW1hdGY5Znh0bm5mZmhoenhjN21nYndyNGszeDNxemlhdWNwcDR5Iiwic3ViIjoiZGlkOmRodDp5MzltNDhvem9ldGU3ejZmemFhbmdjb3M4N2ZodWgxZHppN2Y3andiamZ0N290c2toOXRvIn0.ntcgPOdXOatULWo-q6gkuhKmi5X3bzCONQY38t_rsC1hVhvvdAtmiz-ccoLIYUkjECRHIxO_UZbOKgn0EETBCA"
    val vc = VerifiableCredential.parseJwt(signedVcJwt)
    print(vc)
}
```

# Other Docs

* [API Reference Guide](https://tbd54566975.github.io/tbdex-kt/docs/)
* [Developer Docs](https://developer.tbd.website/docs/tbdex/)
* [Coding Guidelines](./CONVENTIONS.md)
* [Code of Conduct](./CODE_OF_CONDUCT.md)
* [Governance](./GOVERNANCE.md)
