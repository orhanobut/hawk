# CHANGELOG

### 3.0.0
- **Hawk Next** - a modernized, community-maintained fork of Hawk.
- Storage is migrated from SharedPreferences to Preferences DataStore
  (`androidx.datastore:datastore-preferences`).
- Facebook Conceal is completely removed. The Hawk Next AAR ships no native code; the only native
  code in consumer APKs is Google's 16 KB-aligned `libdatastore_shared_counter.so` that DataStore
  1.1+ uses for multi-process support, which is compatible with 16 KB memory page devices.
- Encryption is now Android Keystore + AES-GCM (`KeystoreAesGcmEncryption`), with the existing
  `NoEncryption` fallback when the Keystore is unavailable. Encryption stays replaceable.
- Added automatic one-time migration of Hawk 2.x SharedPreferences data into DataStore, including
  a compatible migration path for data encrypted with Conceal 1.1.3 (standard AES-GCM using the
  key material Conceal stored on the device - no Conceal code is shipped).
- Added `Hawk.keys()` (also on `HawkFacade` and `Storage`) as the single deliberate API addition.
- `HawkBuilder.setLegacyMigrationEnabled(boolean)` to opt out of the automatic migration.
- `minSdk` is raised from 15 to 21 (required by DataStore 1.1.x). DataStore 1.1.x is used instead
  of 1.2.x because 1.2.x requires `minSdk 23`.
- Gson is upgraded to 2.13.x (security fixes; collection handling was adapted to Gson's stricter
  `TypeToken` validation).
- Build is modernized: Gradle 9.5, AGP 9.3, Kotlin 2.2 (AGP built-in Kotlin), Java 8 source
  compatibility, `androidx.test` instrumentation runner.
- Tests are updated (Mockito 5, Truth 1.4, Robolectric 4.16) and extended with storage,
  encryption, migration, concurrency and edge-case coverage.
- The `benchmark` module was replaced by a `sample` app demonstrating the full API and migration.

### 2.0.1
- Conceal is updated, with the new version the size is way smaller

### 2.0.0-Alpha
- Rx support is removed
- Chain option is removed
- Facebook conceal is added as crypto provider
- Async operations are removed
- EncryptionMethod is removed, as default it's encrypted and fallback to no encryption mode if the crypto is not available
- NoEncryption option is available through setEncryption out of box
- LogLevel is removed. All log messages are delegated to LogInterceptor, thus you can intercept and print it. Otherwise all log messages will be ignored.
- All abstraction layers are pluggable. (Converter, Parser, Encryption, Serializer, Storage)
- Sqlite option is removed.
- Init is super fast now, no need to async operation.
