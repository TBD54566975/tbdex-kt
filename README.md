# tbdex-kt
[![License](https://img.shields.io/github/license/TBD54566975/web5-kt)](https://github.com/TBD54566975/tbdex-kt/blob/main/LICENSE) [![CI](https://github.com/TBD54566975/tbdex-kt/actions/workflows/ci.yaml/badge.svg)](https://github.com/TBD54566975/tbdex-kt/actions/workflows/ci.yaml) [![](https://jitpack.io/v/TBD54566975/tbdex-kt.svg)](https://jitpack.io/#TBD54566975/tbdex-kt)


This repo contains 3 jvm packages:

* [`/protocol`](./protocol/) - create, parse, verify, and validate the tbdex messages and resources defined in the [protocol draft specification](https://github.com/TBD54566975/tbdex/blob/main/README.md)
* [`/httpclient`](./httpclient) - An HTTP client that can be used to send tbdex messages to PFIs
* [`/httpserver`](./httpserver) - A configurable implementation of the [tbdex http api draft specification](https://github.com/TBD54566975/tbdex/blob/main/rest-api/README.md)

# Usage

tbdex sdk is consumable through [JitPack](https://jitpack.io):

```kotlin
repositories {
  maven("https://jitpack.io")
  maven("https://repo.danubetech.com/repository/maven-public/")
}

dependencies {
  implementation("com.github.TBD54566975:tbdex-kt:0.1.0-beta")
}
```

> [!IMPORTANT]
> The repository at `https://repo.danubetech.com/repository/maven-public/` is required for resolving transitive dependencies.


# Other Docs
* [Guidelines](./CONVENTIONS.md)
* [Code of Conduct](./CODE_OF_CONDUCT.md)
* [Governance](./GOVERNANCE.md)
