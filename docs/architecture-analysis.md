# Hawk Next - Architecture Analysis (Phase 1)

This document summarises the analysis of the original
[`orhanobut/hawk`](https://github.com/orhanobut/hawk) repository that was performed
before any code was changed. It identifies the architecture, the classes that need
to be changed for Hawk Next, and the constraints that drive the implementation.

## 1. Repository layout

```text
hawk/                       Android library module (main sources are Java, tests are Kotlin)
benchmark/                  Demo / benchmark application (Android support library)
gradle/maven_push.gradle    Maven publishing (Sonatype)
checkstyle.xml              Checkstyle rules for the library
build.gradle                Root build script (AGP 3.1.0, Kotlin 1.2.31, jcenter + google)
```

The original project was built with Gradle 4.4, Android Gradle Plugin 3.1.0,
Kotlin 1.2.31, `compileSdk 27`, `minSdk 15`, Java 7 source/target compatibility,
and `jcenter()` as a repository. `jcenter` is no longer operational, so a build
modernisation is required before anything else.

## 2. Public API surface

| Type | Kind | Notes |
| --- | --- | --- |
| `Hawk` | final class, static facade | `init/build/put/get/get(key,default)/count/deleteAll/delete/contains/isBuilt/destroy` |
| `HawkBuilder` | builder | `setStorage/setParser/setSerializer/setLogInterceptor/setConverter/setEncryption/build` |
| `HawkFacade` | interface | internal plumbing, includes `EmptyHawkFacade` |
| `Storage` | interface | `put/get/delete/deleteAll/count/contains` |
| `Converter` | interface | `toString/fromString` |
| `Parser` | interface | `toJson/fromJson` |
| `Serializer` | interface | `serialize/deserialize` |
| `Encryption` | interface | `init/encrypt/decrypt` |
| `LogInterceptor` | interface | `onLog` |

Concrete implementations: `SharedPreferencesStorage`, `HawkConverter`,
`GsonParser`, `HawkSerializer`, `ConcealEncryption`, `NoEncryption`,
`DefaultHawkFacade`. `DataInfo` is package-private and describes the stored type
metadata (`TYPE_OBJECT/TYPE_LIST/TYPE_MAP/TYPE_SET` plus class names).

Note: the original `Hawk` has no `keys()` method. The Hawk Next requirements
explicitly require `Hawk.keys()`, so that is a deliberate, additive API change.

## 3. Data flow

Write path (`DefaultHawkFacade.put`):

```text
Object -> Converter (Gson JSON) -> Encryption.encrypt -> Serializer (type metadata + ciphertext)
       -> Storage.put(key, serializedText)
```

Read path (`DefaultHawkFacade.get`):

```text
Storage.get(key) -> Serializer.deserialize -> DataInfo -> Encryption.decrypt -> Converter.fromString
```

The serialized text format produced by `HawkSerializer` is
`keyClassName#valueClassName#<dataType>V@<cipherText>` where `dataType` is one of
`0/1/2/3` (object/list/map/set). This format is part of Hawk's on-disk data
compatibility and must not change.

## 4. Storage layer (original)

`SharedPreferencesStorage`:

- SharedPreferences file name: `"Hawk2"` (`HawkBuilder.STORAGE_TAG_DO_NOT_CHANGE`).
- All Hawk entries are stored as `String` values via `putString(...).commit()`.
- `count()` = `preferences.getAll().size()`.
- `deleteAll()` = `editor.clear().commit()`.
- Reads/writes are synchronous and thread-safe through the SharedPreferences
  implementation.

The original default storage is therefore SharedPreferences. Hawk Next replaces
it with Preferences DataStore while keeping the `Storage` contract.

## 5. Encryption layer (original)

`ConcealEncryption` (Facebook Conceal 1.1.3):

- `SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256)` generates and stores a
  256-bit AES master key in a separate SharedPreferences file managed by Conceal.
- `Crypto.encrypt(plainText, Entity.create(key))` encrypts with AES-256-CTR and
  authenticates with HMAC-SHA256; the output is Base64 encoded (`NO_WRAP`) and
  stored as the `cipherText` segment of the serialized text.
- The Hawk entry key is bound to the entity used for encryption/decryption.
- If Conceal reports it is not available, `HawkBuilder` falls back to
  `NoEncryption` (Base64 only).

Hawk Next removes Conceal entirely and replaces it with Android Keystore +
AES-GCM, keeping the same `Encryption` interface and the same fallback pattern.

## 6. Dependency graph (original)

```text
com.orhanobut:hawk:2.1.0
  +- api com.facebook.conceal:conceal:1.1.3@aar   (native .so library)
  +- implementation com.google.code.gson:gson:2.8.2

test: junit, truth, robolectric 3.3, mockito 2.8.9, kotlin-stdlib-jdk7
androidTest: junit, truth, android support test runner/rules, kotlin-stdlib
```

Conceal ships native libraries (`libconceal.so`), which is the source of the
16 KB page-size problem on modern Android. Removing Conceal removes the only
native dependency.

## 7. Classes that need to change

| Class | Change |
| --- | --- |
| `HawkBuilder` | Default storage -> `DataStoreStorage`; default encryption -> `KeystoreAesGcmEncryption` (fallback `NoEncryption`); add legacy-migration toggle |
| `DefaultHawkFacade` | Trigger one-time legacy migration; delegate `keys()` |
| `Hawk` / `HawkFacade` | Add `keys()` |
| `Storage` | Add `keys()` (one deliberate API addition required by the spec) |
| `SharedPreferencesStorage` | Keep (package-private) as the legacy migration reader; add `keys()` |
| `ConcealEncryption` | Delete; replaced by `KeystoreAesGcmEncryption` |
| `ConcealTest`, `ConcealEncryptionTest` | Delete/replace with Keystore tests |
| New: `DataStoreStorage` | Preferences DataStore backend, synchronous bridge, single instance per file |
| New: `KeystoreAesGcmEncryption` | Android Keystore + AES-GCM, no native code |
| New: `LegacyMigrator` | One-time SharedPreferences -> DataStore migration |
| `HawkBuilderTest`, `SharedPreferencesStorageTest`, android tests | Updated for the new defaults and Keystore |

Unchanged by design: `Converter`, `Parser`, `Serializer`, `DataInfo`, `HawkConverter`,
`GsonParser`, `HawkSerializer`, `NoEncryption`, `Encryption`, `LogInterceptor`,
package names, and the on-disk serialized format.

## 8. Migration requirements

- Read all entries from the legacy `"Hawk2"` SharedPreferences file.
- Write them into Preferences DataStore in a single transactional `edit {}`.
- Mark migration as complete, then clear the legacy file.
- Never delete legacy data before a successful migration.
- Legacy data written with Conceal encryption cannot be decrypted after Conceal
  is removed. Hawk Next preserves the original blobs verbatim and documents an
  explicit migration procedure for encrypted values (see the migration guide);
  it never treats them as plaintext.

## 9. Key constraints and decisions

1. Preferences DataStore (not Proto DataStore) is the storage backend.
2. DataStore 1.1.x declares `minSdk 21` in its manifest, which raises the original
   `minSdk 15` (forced by the dependency, documented in the README). DataStore 1.1.x is used
   instead of 1.2.x because 1.2.x requires `minSdk 23`.
3. The Hawk Next AAR contains no native code. DataStore 1.1+ ships Google's
   `libdatastore_shared_counter.so` (multi-process support) into consumer APKs; it is not
   Hawk-originated, contains no Conceal code, and its ELF load segments are 16 KB-aligned
   (verified during development), so 16 KB page-size compatibility holds.
3. DataStore is asynchronous; Hawk's API is synchronous. A synchronous bridge is
   required (details in the architecture notes of the README and migration doc).
4. Android Keystore + AES-GCM uses platform crypto only, no native libraries,
   no hard-coded keys.
5. Existing tests are kept and updated; new tests cover storage, encryption,
   migration, concurrency, and edge cases.
