# tbdex-kt

[![License](https://img.shields.io/github/license/TBD54566975/tbdex-kt)](https://github.com/TBD54566975/tbdex-kt/blob/main/LICENSE) [![CI](https://github.com/TBD54566975/tbdex-kt/actions/workflows/ci.yaml/badge.svg)](https://github.com/TBD54566975/tbdex-kt/actions/workflows/ci.yaml) [![](https://jitpack.io/v/TBD54566975/tbdex-kt.svg)](https://jitpack.io/#TBD54566975/tbdex-kt) [![Coverage](https://img.shields.io/codecov/c/gh/tbd54566975/tbdex-kt/main?logo=codecov&logoColor=FFFFFF&style=flat-square&token=YI87CKF1LI)](https://codecov.io/github/TBD54566975/tbdex-kt)

This repo contains 2 jvm packages:

* [`/protocol`](./protocol/) - create, parse, verify, and validate the tbdex messages and resources defined in
  the [protocol draft specification](https://github.com/TBD54566975/tbdex/blob/main/specs/protocol/README.md)
* [`/httpclient`](./httpclient) - An HTTP client that can be used to send tbdex messages to PFIs

# Usage

tbdex sdk is consumable through [JitPack](https://jitpack.io):

```kotlin
repositories {
  maven("https://jitpack.io")
  maven("https://repo.danubetech.com/repository/maven-public/")
}

dependencies {
  implementation("com.github.TBD54566975:tbdex-kt:0.4.0-beta")
}
```

> [!IMPORTANT]
> The repository at `https://repo.danubetech.com/repository/maven-public/` is required for resolving transitive
> dependencies.

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
