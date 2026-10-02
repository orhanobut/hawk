# Maintaining Hawk

## Build and verification

Use JDK 21 and an Android SDK containing `platforms;android-37.0` and `build-tools;36.0.0`. Set `ANDROID_HOME` outside version control. Gradle 9.8.0 and AGP 9.4.1 use AGP's built-in Kotlin support; the buildscript dependency selects Kotlin 2.4.20 without applying another Android Kotlin plugin. Java and Kotlin bytecode targets are both 8.

```sh
./gradlew :hawk:testDebugUnitTest :hawk:lintRelease :hawk:assembleRelease \
  :hawk:assembleDebugAndroidTest :benchmark:lintDebug :benchmark:assembleDebug \
  :hawk:publishAllPublicationsToVerificationRepository
python3 scripts/verify_artifacts.py
```

Local verification writes unsigned artifacts under `hawk/build/repository` and never uploads them. The inspection script checks AAR contents, minimum SDK, Kotlin sources, HTML documentation, POM metadata, Gradle dependency metadata, and local README links. For snapshots it resolves timestamped filenames using Maven metadata.

The JVM suite exercises serialization, collection round trips, failure paths, deletion, defaults, and facade delegation using small fakes. No Android default-returning stubs, Robolectric, Truth, Mockito, or Checkstyle are used. Device tests exercise actual SharedPreferences, Android Base64, builder configuration, and native Conceal. Run `:hawk:connectedDebugAndroidTest` with a device or emulator; the GitHub Actions device job uses API 23/x86_64. This is not coverage of API 21, every architecture, or 16 KB devices.

## Dependencies and compatibility

Consumer runtime dependencies are Conceal 2.0.2, Gson 2.14.0 and Kotlin stdlib 2.4.20. Gson and Conceal use `api` because `GsonParser(Gson)` and protected `ConcealEncryption(Crypto/KeyChain)` constructors expose their types. The resolved graph also contains JSR-305 3.0.2, Error Prone annotations 2.48.0 and JetBrains annotations 13.0. These vendor-declared dependencies are retained without exclusions. JUnit 4.13.2 (including its required Hamcrest 1.3) is JVM-test-only. AndroidX runner 1.7.0 and ext JUnit 1.3.0 and their transitive dependencies are device-test-only. AGP, Kotlin's compiler, Dokka and Vanniktech are build plugins, not AAR dependencies.

The minimum SDK changes from this checkout's API 15 to API 21. Current Gson officially supports API 21+, so keeping API 15 while updating Gson would claim unsupported compatibility. The published Hawk 2.0.1 AAR declares API 10; README installation still points to that real release, not the unpublished snapshot. The namespace and coordinates remain `com.orhanobut.hawk` and `com.orhanobut:hawk`.

The Kotlin conversion retains the original public/protected JVM method descriptors, static Hawk entry points, fluent setters, default constructors and protected Conceal constructors. Public Java classes that supported inheritance remain open, including their overridable methods. `DataInfo`, previously package-private despite appearing in public interfaces, is now public and retains its fields via `@JvmField` so custom Kotlin converters and serializers can use it. Kotlin companions and internal implementation visibility introduce additional JVM members; these are not new extension contracts.

Nullability is now explicit: reads and failure-producing pipeline methods may return null, null values delete entries, and null builder setters reset defaults. Existing Kotlin implementations of interfaces must adjust their parameter types to the nullable declarations; synthetic Java property access such as `facade.isBuilt` becomes `facade.isBuilt()`. Non-null Kotlin parameters now reject invalid Java null inputs at entry; validation messages for null context, put keys, parser and serializer values remain covered. Between `Hawk.init` and `build`, the unbuilt facade now consistently reports false and throws `IllegalStateException` instead of dereferencing a null facade. `destroy()` retains its existing no-op behavior after build.

The `Hawk2` preferences name, metadata type markers, class names, and Base64 modes remain unchanged. Concrete Gson TypeToken arguments replace captured type variables, which current Gson rejects. JVM regression tests cover legacy metadata and collection representations. Native encrypted-data upgrades from Conceal 1.1.3 to 2.0.2 still require Android device testing; successful compilation and method-descriptor checks do not establish full binary or persisted-data compatibility.

## Known native dependency limitation

Conceal was archived in 2020. Its latest Central artifact remains 2.0.2. Android lint reports its `x86_64/libconcealjni.so` as not aligned for 16 KB pages. Each module's narrow lint baseline records only this dependency's `Aligned16KB` findings; other errors and warnings still fail lint. A passing lint task therefore does **not** certify 16 KB compatibility or Play eligibility. Do not broaden the baseline or silently replace the default encryption: doing so risks existing ciphertext, keys and public constructors. Inspection of the original 1.1.3 and updated 2.0.2 AARs confirms that both have 4 KB-aligned x86_64 LOAD segments; this limitation predates the update. A future native rebuild or an explicit encryption/data migration must resolve this before claiming support.

Hawk's historical fallback to Base64-only `NoEncryption` is retained. Applications needing guaranteed encryption must validate and explicitly select a supported implementation. Conceal stores keys in app-private preferences, not Android Keystore. Switching encryption does not migrate stored data.

## Publishing

Vanniktech 0.37.0 publishes the release AAR, dependency metadata, sources, and Dokka 2.2.0 HTML documentation through Sonatype Central Portal. Remote publication requires signing; the local verification task skips signing only when no Maven Central publication task is in the task graph. No publishing credentials are used in CI.

Keep `mavenCentralUsername`, `mavenCentralPassword`, `signingInMemoryKey` and `signingInMemoryKeyPassword` in user Gradle properties or their `ORG_GRADLE_PROJECT_...` environment equivalents. Obtain the username/password token from the Central Portal. The repository version is an unreleased snapshot so ordinary work does not impersonate a released 2.1.0. Only an authorized release should change that version and invoke remote publishing. Local verification does not validate real credentials, signing keys, or Portal ownership.

## Retiring remote Travis

The checkout's `.travis.yml` and README badge have been removed. GitHub Actions exposes `Build and verify` and `Android device tests`. The repository is `orhanobut/hawk`, with the original default branch `master`.

GitHub hooks and `branches/master/protection` API requests returned HTTP 401 without authenticated credentials. No `gh` CLI is available, and Safari computer-use access was not approved. The public default-branch status API returned no status contexts; that does not establish whether triggers are disabled. Remote integrations, rulesets and required checks could not be inspected or changed; local deletion does not deactivate Travis.

An administrator should:

1. Run the new workflow on `master` and verify both jobs pass.
2. Open [branch rules](https://github.com/orhanobut/hawk/settings/branches) and [rulesets](https://github.com/orhanobut/hawk/settings/rules). Replace required `continuous-integration/travis-ci`, `continuous-integration/travis-ci/pr` and/or `continuous-integration/travis-ci/push` contexts **only if present**, with the observed GitHub Actions check names `Build and verify` and `Android device tests`.
3. Inspect [installed GitHub Apps](https://github.com/orhanobut/hawk/settings/installations) and [webhooks](https://github.com/orhanobut/hawk/settings/hooks). Remove this repository from Travis CI's app access or deactivate its Travis webhook, without affecting other repositories.
4. In Travis CI's repository settings for `orhanobut/hawk`, deactivate the repository and disable push/PR triggers. Inspect both legacy travis-ci.org and travis-ci.com integrations if present. Verify a subsequent push/PR produces no Travis check and retains the new required checks.

## Verified version sources

Versions were checked against official release information and Maven metadata on 2026-10-02:

- [Gradle current release](https://services.gradle.org/versions/current), [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes), [Google Maven AGP versions](https://dl.google.com/dl/android/maven2/com/android/tools/build/gradle/maven-metadata.xml).
- [Built-in Kotlin migration](https://developer.android.com/build/migrate-to-built-in-kotlin), [Kotlin releases](https://kotlinlang.org/docs/releases.html).
- [Gson releases and requirements](https://github.com/google/gson), [Conceal releases](https://github.com/facebookarchive/conceal/releases), [Conceal Central metadata](https://repo.maven.apache.org/maven2/com/facebook/conceal/conceal/maven-metadata.xml).
- [Vanniktech Central Portal configuration](https://vanniktech.github.io/gradle-maven-publish-plugin/central/), [Dokka 2.2.0](https://github.com/Kotlin/dokka/releases/tag/v2.2.0).
- [Checkout releases](https://github.com/actions/checkout/releases), [setup-java releases](https://github.com/actions/setup-java/releases), [Gradle actions releases](https://github.com/gradle/actions/releases), [emulator-runner releases](https://github.com/ReactiveCircus/android-emulator-runner/releases).

## Results of this modernization

58 JVM tests passed; library release assembly, sample debug assembly, device-test APK compilation, Dokka generation and local publication passed. Library release and sample debug lint passed with only the documented Conceal alignment findings baselined. The POM and Gradle module metadata contain exactly the three direct consumer dependencies listed above. The AAR, Kotlin sources and HTML documentation archives were inspected. The complete wrapper was regenerated and its JAR SHA-256 matched Gradle's official checksum.

All original public/protected method descriptors were compared with the compiled Kotlin classes, and existing overridable public methods remained overridable. A small Java consumer compiled against the original Java sources linked and ran against the Kotlin library, covering static facade calls, subclassing and Gson parsing. This is limited compatibility evidence, not a full binary-compatibility certification. README external links and local asset links were checked; an obsolete Hawk 1 branch link was removed.

No Android device was connected, so the device tests and native ciphertext upgrade were not executed locally. Remote signing and publication were not attempted. Gradle reports a deprecated configuration-visibility call from the publishing plugin; no migration opt-outs were introduced to accommodate it.
