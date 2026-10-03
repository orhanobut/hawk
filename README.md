<img align="right" src="art/hawk-logo.png" width="128" height="128" alt="Hawk logo" />

# Hawk

> **Hawk 3 is coming soon!** Android Keystore-backed AES-GCM encryption, fewer dependencies, and a modern Kotlin codebase are on the way. Hawk 3 is not released yet; the current published version is 2.0.1.

Simple, pluggable key-value storage for Android. Save a value with a key and read it back without writing a database schema.

- Store primitives, strings, custom objects, lists, sets, and maps.
- Persist values in app-private SharedPreferences.
- Customize parsing, conversion, encryption, serialization, and storage.
- Check, count, and delete entries through a small API.

## Install

Use Maven Central and the available release:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.orhanobut:hawk:2.0.1")
}
```

The examples below use APIs available in 2.0.1. The next version (3.0) is unreleased and requires Android 6.0 (API 23) or newer; the published 2.0.1 artifact declares API 10 as its minimum.

## Quick start

Initialize once using an application context, before reading or writing:

```kotlin
Hawk.init(applicationContext).build()

val saved = Hawk.put("name", "Ada")
val name: String? = Hawk.get("name")
val visits: Int = Hawk.get("visits", 0)

Hawk.put("tags", listOf("android", "kotlin"))
val tags: List<String>? = Hawk.get("tags")

Hawk.contains("name")
Hawk.count()
Hawk.delete("name")
Hawk.deleteAll()
```

`put` returns whether the value was saved. Passing a null value deletes the key. A missing key returns null, or the supplied default. `deleteAll` clears stored values but retains encryption keys. Reads and writes are synchronous; perform substantial work away from the main thread.

## Configuration

Gson is the default parser. Supply a configured parser for custom type adapters, or replace any pipeline interface with your own implementation:

```kotlin
Hawk.init(applicationContext)
    .setParser(GsonParser(GsonBuilder().create()))
    .setStorage(myStorage)
    .setEncryption(myEncryption)
    .build()
```

`NoEncryption()` is available for data that needs no confidentiality. It only Base64-encodes values.

## Compatibility and limitations

The unreleased Hawk 3.0 uses AES-256-GCM with an application-owned Android Keystore key. Each write uses a fresh nonce and binds the ciphertext to its storage key. Initialization fails if Keystore is unavailable; it never falls back to `NoEncryption`. `deleteAll()` retains the Keystore key so other stored values are not invalidated. Keep a custom `KeystoreEncryption("your-stable-alias")` alias unchanged while its data is retained.

Published Hawk 2.0.1 still uses legacy Conceal and may fall back to Base64-only storage. Its encryption is not changed by the installation snippet above.

Hawk 3.0 cannot read Hawk 2's default ciphertext. Before upgrading, read and migrate the values using the old application/library, then save them with the new encryption. `ConcealEncryption` has been removed. Unmigrated values remain stored, but `get` returns null (or your default) when decryption fails; they are not automatically cleared or converted. Applications storing non-sensitive data with `NoEncryption` can explicitly keep that implementation.

Keystore keys are not restored with preferences or transferred to another installation. Exclude `Hawk2.xml` from both cloud backup and device transfer using your app's backup rules; otherwise restored values cannot be decrypted. Handle key loss by recovering data from an application-specific trusted source. Hawk never regenerates a key while decrypting an existing value. Avoid logging sensitive data through a `LogInterceptor`.

Use the same type when reading a key as when saving it. Collections record the first element's class, so use homogeneous, non-nested collections whose first element (and first map key/value) is non-null. Model changes can prevent older data from being read. Gson does not enforce Kotlin constructor defaults or non-null properties.

For apps using R8, keep the names and fields of models persisted with Hawk: the format stores class names and Gson uses reflection. For example, with your own package:

```proguard
-keep class com.example.app.storage.model.** { *; }
```

Changing encryption, parsing, or storage implementations does not migrate existing data. Hawk 2 data is incompatible with Hawk 1.

## How values are stored

![Hawk storage pipeline: conversion, encryption, serialization and persistence](art/how-hawk-works.png)

## License

[Apache License 2.0](LICENSE). Copyright Orhan Obut.
