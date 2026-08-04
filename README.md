# Hawk Next

> A modern, actively maintained fork of [Hawk](https://github.com/orhanobut/hawk) -
> secure, simple key-value storage for Android.

Hawk Next keeps Hawk's public API and usage model as compatible as possible, while replacing the
legacy storage and encryption stack with modern Android components:

```text
Hawk
  +-- Gson
  +-- Preferences DataStore
  +-- Android Keystore + AES-GCM (NoEncryption fallback)
```

Hawk 2.x used:

```text
Hawk
  +-- Gson
  +-- Facebook Conceal (native .so library)
  +-- SharedPreferences
```

## Why Hawk Next

| Feature         | Hawk 2.x          | Hawk Next                  |
| --------------- | ----------------- | -------------------------- |
| Storage         | SharedPreferences | Preferences DataStore      |
| Encryption      | Facebook Conceal  | Android Keystore + AES-GCM |
| Native library  | Yes               | No                         |
| 16 KB page size | Problematic       | Supported                  |
| Migration       | -                 | Automatic (one-time)       |
| Java API        | Yes               | Yes                        |
| Kotlin API      | Yes               | Yes                        |

## Download

Publishing is not enabled yet; confirm the final Maven coordinates before releasing.
The planned coordinates are:

```gradle
implementation("io.github.Ium-Lab:hawk-next:3.0.0")
```

## Initialize

```java
Hawk.init(context).build();
```

## Usage

Save any type (any object, primitives, lists, sets, maps ...):

```java
Hawk.put("username", "Jack");
Hawk.put("age", 30);
Hawk.put("enabled", true);
Hawk.put("user", userObject);
Hawk.put("users", userList);
```

Get the original value with the original type:

```java
String username = Hawk.get("username");
User user = Hawk.get("user");
User user = Hawk.get("user", defaultUser);
```

Delete, inspect and manage entries:

```java
Hawk.delete("user");
Hawk.deleteAll();
Hawk.contains("user");
Hawk.count();
Hawk.keys();
```

All existing Hawk code keeps working - only the Maven dependency changes.

## Builder options

Everything stays pluggable, exactly like Hawk 2.x:

```java
Hawk.init(context)
    .setEncryption(new NoEncryption())           // disable encryption
    .setLegacyMigrationEnabled(false)            // disable the one-time legacy migration
    .setLogInterceptor(new MyLogInterceptor())
    .setConverter(new MyConverter())
    .setParser(new MyParser())
    .setStorage(new DataStoreStorage(context))   // custom Storage implementations are supported
    .build();
```

## How Hawk Next works

```text
Object
  -> Converter (Gson JSON)
  -> Encryption (Android Keystore + AES-GCM)
  -> Serializer (type metadata + cipher text)
  -> DataStoreStorage (Preferences DataStore)
```

## Migration from Hawk 2.x

Hawk Next automatically migrates data written by Hawk 2.x (SharedPreferences `"Hawk2"`) into
Preferences DataStore on the first `Hawk.init(...).build()` after upgrading:

1. All legacy entries are read from SharedPreferences.
2. Plain-text (NoEncryption) values are re-encrypted with the current encryption.
3. Values encrypted with Facebook Conceal 1.1.3 are decrypted using the documented legacy
   AES-GCM format and the key material Conceal stored on the device, then re-encrypted with the
   current encryption. No Conceal code or native library is involved.
4. Everything is written in one transactional DataStore write, the migration is marked as done,
   and only then is the legacy file cleared.

Migration runs exactly once, never deletes data before it succeeds, and never treats encrypted
data as plaintext. See [docs/migration.md](docs/migration.md) for the full details, including the
verified legacy format and the explicit procedure for values that cannot be migrated.

## Synchronous API

Hawk's API remains synchronous (`Hawk.put/get/delete/...`). Preferences DataStore is asynchronous
under the hood, so every call bridges synchronously: the caller waits for the result while the
DataStore work itself runs on DataStore's IO dispatcher. This matches the blocking behaviour of
the old `SharedPreferences.commit()` calls. DataStore serialises writes internally, so concurrent
access from multiple threads cannot lose updates.

## Requirements

- `minSdk 21` - required by Preferences DataStore 1.1.x (the original Hawk supported API 15; the
  raise is forced by the storage dependency). DataStore 1.1.x is used instead of 1.2.x because
  1.2.x requires `minSdk 23`.
- Android 15 (API 35) and Android 16 (API 36) are supported (`compileSdk 36`).
- No Hawk-originated native code: the Hawk Next AAR contains no `.so` files and no JNI. The only
  native code that can appear in a consumer APK is Google's `libdatastore_shared_counter.so`
  (shipped by DataStore 1.1+ for multi-process support); it contains no Conceal code and its ELF
  load segments are aligned to 16 KB, so it is compatible with 16 KB page-size devices.

## Sample application

The `sample/` module is a small Java app demonstrating `init`, `put`, `get`, `delete`, `contains`,
`count`, `keys`, `deleteAll` and the one-time migration.

## License

Apache License 2.0, same as the original Hawk. This is a community-maintained fork and is not an
official release from the original author.
