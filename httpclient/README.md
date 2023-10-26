# tbdex-kt httpclient

## How to run E2ETest.kt locally

1. Comment out `@Disabled` annotation above the test method in `E2ETest` class
2. Pull down latest version of [tbdex-mock-pfi](https://github.com/TBD54566975/tbdex-mock-pfi)
3. Read the README of tbdex-mock-pfi to install dependencies, create the database, and run migrations.
4. Run tbdex-mock-pfi by running `npm run server`. The console should log the PFI DID
5. Change `val pfiDid` value in the `tests e2e flow` method with the PFI DID from step 4
6. Run or debug the `tests e2e flow` method.