# Maintaining Hawk

## Build and verification

Use JDK 21 and an Android SDK containing `platforms;android-37.0` and `build-tools;36.0.0`. Set `ANDROID_HOME` outside version control. Gradle 9.8.0 and AGP 9.4.1 use AGP's built-in Kotlin support; the buildscript dependency selects Kotlin 2.4.20 without applying another Android Kotlin plugin. Java and Kotlin bytecode targets are both 8.

```sh
./gradlew :hawk:testDebugUnitTest :hawk:lintRelease :hawk:assembleRelease \
  :hawk:assembleDebugAndroidTest :benchmark:lintDebug :benchmark:assembleDebug \
  :benchmark:assembleRelease :hawk:publishAllPublicationsToVerificationRepository
python3 scripts/verify_artifacts.py
```

Local verification writes unsigned artifacts under `hawk/build/repository` and never uploads them. The inspection script checks AAR contents, minimum SDK, Kotlin sources, HTML documentation, POM dependency metadata and exclusions, both API/runtime Gradle publication variants, local README links, and the absence of bundled native binaries in both sample APKs. Snapshots resolve timestamped filenames using Maven metadata.

Keep tests focused on data loss, compatibility and encryption failures. The suite has two JVM test classes and one Android integration class, all using concrete implementations:

- `SerializationTest`: literal metadata fixtures verify the persisted format and typed collection conversion with real Gson, converter and serializer implementations.
- `AesGcmCipherTest`: the actual JCA cipher, fresh nonces, authentication of storage keys and ciphertext, and malformed envelopes.
- `HawkAndroidTest`: six integration tests cover the complete default pipeline, model/collection round trips, deletion/defaults, failed reads/writes, non-exportable keys/authentication/key loss, initialization failure without a plaintext fallback, and explicitly selected legacy Base64 encoding. Failures are triggered by deleting an actual Keystore key, generating an incompatible key, or changing a stored model type.

Separate utility, delegation, converter and serializer tests were removed where they repeated these scenarios or asserted trivial implementation details. Handwritten storage/commit fakes and injected failure stubs were also removed; synchronous commit failure is no longer simulated. Android coverage is retained because the JVM cannot execute Android Keystore or verify the platform's preferences and Base64 behavior. There are no mocks, stubs, fakes or mocking resources. Robolectric, Truth, Mockito and Checkstyle are not used. Run `:hawk:connectedDebugAndroidTest` with a device or emulator. GitHub Actions runs device tests on API 23 and 35/x86_64.

The sample release uses R8 and resource shrinking to verify that consumers can build with the reduced dependency graph. The sample excludes `Hawk2.xml` from cloud backup and device transfer, since the corresponding Keystore key is not backed up.

## Dependencies

The only direct consumer dependencies are Gson 2.14.0 and Kotlin stdlib 2.4.20. Both use `api`: Gson appears in `GsonParser(Gson)`, and stdlib is required by the compiled Kotlin code. Gson retains Error Prone annotations 2.48.0 as its sole transitive consumer dependency. Removing that annotation artifact was tested and broke R8 with a missing `CanIgnoreReturnValue` class; keep it rather than requiring consumers to suppress missing-class errors.

JetBrains annotations are excluded specifically from the stdlib dependency, in both POM and Gradle metadata. They are not used as executable code or public method parameter/return types. JVM tests, lint, Android tests, a shrunk sample assembly, and Java interoperability checks verify the removal. No required runtime types are disguised as `compileOnly` dependencies. Conceal, all of its native binaries, JSR-305, the unused AndroidX ext JUnit dependency and the native-alignment lint baselines are removed. Lint is strict with no baseline or native-alignment suppression.

JUnit 4.13.2 and its required Hamcrest 1.3 are test-only. AndroidX runner 1.7.0 and its monitor/platform dependencies are device-test-only. Tests use ordinary JUnit 4 discovery without the AndroidX JUnit extension. AGP, Kotlin's compiler, Dokka and Vanniktech are build plugins, not consumer dependencies. Gson remains necessary to preserve arbitrary model serialization and the public parser constructor; removing stdlib would break compiled Kotlin code.

## Encryption and compatibility

The development version is `3.0.0-SNAPSHOT`, reflecting a breaking change to the default encryption and removal of the public `ConcealEncryption` class and its constructors. The namespace and coordinates remain `com.orhanobut.hawk` and `com.orhanobut:hawk`. The minimum SDK is API 23, required by Keystore AES key generation with `KeyGenParameterSpec`. Published 2.0.1 still declares API 10 and still uses its legacy encryption; README installation intentionally points to that available release.

`KeystoreEncryption()` uses the stable app-owned alias `com.orhanobut.hawk.aes-gcm`. A custom stable alias can be supplied through `KeystoreEncryption(alias)`. AES-256 keys are generated inside Android Keystore with encrypt/decrypt purposes, GCM mode, no padding and randomized encryption required. Cipher initialization generates a fresh 12-byte nonce. The payload is a `hawk-aes-gcm:` prefix followed by Base64 of a one-byte version, nonce, ciphertext and 128-bit tag. Associated data binds the protocol version and storage key to the value. Cipher instances are per-operation; key creation is synchronized within the process. SharedPreferences still has its existing single-process ownership expectations.

Default initialization fails with `IllegalStateException` if the Keystore cannot initialize. Encryption errors continue to make writes return false, and decryption errors make reads return null or their supplied default. There is no automatic `NoEncryption` fallback. The public `NoEncryption` option remains available only by explicit configuration. Decrypting never creates or replaces a missing key. `deleteAll()` clears values but retains key material; `destroy()` retains its existing no-op behavior. Keystore protection does not imply that every device offers hardware-backed keys.

The `Hawk2` preferences name and serializer's type metadata remain unchanged, but the encrypted payload is new. Old default ciphertext and Base64-only values are rejected by the new default implementation, not silently converted. Before upgrading, an application must read values using its previous implementation and migrate them through a secure application-owned process, then write them using the new encryption. Do not clear stored values automatically: unmigrated values remain present even though reads cannot decrypt them. Applications explicitly using `NoEncryption` may continue using it for non-sensitive data.

Keystore keys are bound to an application installation and cannot be restored from preference backups. Exclude `Hawk2.xml` from cloud backup and device transfer in both legacy full-backup and modern data-extraction rules. Handle lost keys through application-specific recovery; do not substitute plaintext storage. Apps choosing custom encryption remain responsible for its initialization and migration requirements.

The remaining public/protected JVM entry points, static Hawk methods, fluent setters and overridable methods are retained. `DataInfo` remains public with `@JvmField` fields for Kotlin extension implementations. Nullability is explicit: reads/failure results may be null, null values delete entries, and null builder setters restore defaults. Existing Kotlin implementations may need nullable parameter declarations, and former Java synthetic property access such as `facade.isBuilt` becomes `facade.isBuilt()`. `Hawk.init` installs an unbuilt facade until `build` completes. This is not a claim of full binary or stored-data compatibility.

## Publishing

Vanniktech 0.37.0 publishes the release AAR, dependency metadata, sources, and Dokka 2.2.0 HTML documentation through Sonatype Central Portal. Remote publication requires signing; the local verification task skips signing only when no Maven Central publication task is in the graph. CI uses no publishing credentials.

Keep `mavenCentralUsername`, `mavenCentralPassword`, `signingInMemoryKey` and `signingInMemoryKeyPassword` in user Gradle properties or their `ORG_GRADLE_PROJECT_...` environment equivalents. Obtain the publishing token from Central Portal. Only an authorized release should change the snapshot version or invoke remote publication. Local verification does not validate real credentials, signing keys or Portal ownership.

## Retiring remote Travis

The checkout's Travis configuration and badge are removed. The repository is `orhanobut/hawk` and its default branch is `master`. The workflow defines `Build and verify`, `Android device tests (API 23)` and `Android device tests (API 35)`; confirm the actual check names after its first remote run.

Previous GitHub hooks and `branches/master/protection` API requests returned HTTP 401. No `gh` CLI was available and Safari access was not approved. The public status API returned no contexts, which does not establish that triggers are disabled. Remote integrations, rulesets and required checks remain unverified; local deletion does not deactivate Travis.

An administrator should:

1. Run the new workflow on `master` and verify all jobs pass.
2. Inspect [branch rules](https://github.com/orhanobut/hawk/settings/branches) and [rulesets](https://github.com/orhanobut/hawk/settings/rules). Replace required `continuous-integration/travis-ci`, `continuous-integration/travis-ci/pr` and/or `continuous-integration/travis-ci/push` contexts only if present, with the confirmed GitHub Actions check names.
3. Inspect [GitHub Apps](https://github.com/orhanobut/hawk/settings/installations) and [webhooks](https://github.com/orhanobut/hawk/settings/hooks). Remove this repository from Travis app access or deactivate its Travis webhook without affecting other repositories.
4. Disable push/PR triggers and deactivate `orhanobut/hawk` in Travis settings. Check both legacy travis-ci.org and travis-ci.com integrations if present. Verify subsequent pushes/PRs produce the new required checks and no Travis checks.

## Verification results

After test simplification and removal of all test doubles, all 4 JVM tests and 6 Android integration tests passed on an API 35 ARM64 emulator, and library release lint passed without baselines. Test sources and both resolved test runtime dependency graphs contain no Mockito or other mocking framework. The unused `src/test/resources/mockito-extensions` directory and its empty `resources` parent were removed. The suite shrank from 10 files/961 lines to 3 files/208 lines.

For the unchanged production code, previous verification also passed sample debug lint, library release/sample debug/R8-resource-shrunk release assemblies, Dokka generation and local publication. The shrunk sample was signed with a disposable local test key, installed, and successfully exercised default initialization, put, get, contains, count and delete. Java consumers compiled against both the original and current API executed against the current library without JetBrains annotations. Published dependency declarations/exclusions and AAR/source/documentation archives were inspected. No native binaries remain in the sample APKs.

API 23 and API 35 x86_64 execution is configured in CI but was not run locally. Hardware-backed Keystore behavior, every device/vendor, and legacy-data migration are not certified by these tests. No remote signing, release or upload was attempted. A publishing-plugin configuration-visibility deprecation remains; no migration opt-outs were introduced.

## Official references

- [Android cryptography guidance](https://developer.android.com/privacy-and-security/cryptography), [Android Keystore](https://developer.android.com/privacy-and-security/keystore), [KeyGenParameterSpec](https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec).
- [Gradle current release](https://services.gradle.org/versions/current), [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes), [built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin), [Kotlin releases](https://kotlinlang.org/docs/releases.html).
- [Gson requirements](https://github.com/google/gson), [Gson dependency metadata](https://github.com/google/gson/blob/main/gson/pom.xml).
- [Central Portal publishing](https://vanniktech.github.io/gradle-maven-publish-plugin/central/), [Dokka 2.2.0](https://github.com/Kotlin/dokka/releases/tag/v2.2.0).
