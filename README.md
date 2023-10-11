# tbdex-kt
This repo contains 3 jvm packages:

* [`/protocol`](./protocol/) - create, parse, verify, and validate the tbdex messages and resources defined in the [protocol draft specification](https://github.com/TBD54566975/tbdex-protocol/blob/main/README.md)
* [`/http-client`](./http-client) - An HTTP client that can be used to send tbdex messages to PFIs
* [`/http-server`](./http-server) - A configurable implementation of the [tbdex http api draft specification](https://github.com/TBD54566975/tbdex-protocol/blob/main/rest-api/README.md)

## tbDEX Schemas
> **Warning**  
> Until `tbdex` is stable, do not sync schemas into `tbdex-kt`!

Make sure to import/update the JSON schemas which define the tbDEX message and resource formats.

Navigate to the `tbdex` directory and run:
```
git submodule update --init --recursive
```

Navigate back to root directory and run:
```
./gradlew syncSchemas
```


# Other Docs
* [Guidelines](./CONVENTIONS.md)
* [Code of Conduct](./CODE_OF_CONDUCT.md)
* [Governance](./GOVERNANCE.md)
