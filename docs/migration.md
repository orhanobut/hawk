# Hawk 2.x to Hawk Next migration

Hawk Next migrates existing Hawk 2.x data automatically. This document explains exactly what
happens, what the legacy data looks like, and what to do when a value cannot be migrated.

## When does migration run?

During `Hawk.init(context).build()`, exactly once per app installation. Success is tracked with a
flag in a small SharedPreferences file named `Hawk2Migration` (key `migrated`). The flag is
written only after the DataStore write has completed, and the legacy `Hawk2` SharedPreferences
file is cleared only after the flag has been written.

Use `HawkBuilder.setLegacyMigrationEnabled(false)` to disable migration (for example when you know
there is no legacy data, or when you prefer to migrate manually).

## What the legacy data looks like

Hawk 2.x stored every entry in the SharedPreferences file `Hawk2` as a single string:

```text
<keyClassName>#<valueClassName>#<dataType>V@<cipherText>
```

where `dataType` is `0` (object), `1` (list), `2` (map) or `3` (set), and `cipherText` depends on
the encryption that was configured:

### NoEncryption (or Conceal unavailable)

`cipherText` = Base64 (Base64.DEFAULT) of the plain text (the Gson JSON of the value).

### Facebook Conceal 1.1.3 (default)

`cipherText` = Base64 (Base64.NO_WRAP) of:

```text
[1 byte serialization version = 0x01]
[1 byte cipher id = 0x02 for KEY_256]
[12 byte AES-GCM IV]
[AES-GCM ciphertext]
[16 byte AES-GCM tag]
```

with AES-256-GCM and additional authenticated data (AAD):

```text
[serialization version byte][cipher id byte][entity bytes]
```

where `entity` is the Hawk entry key encoded as UTF-8 (Conceal `Entity.create(key)`).

The 256-bit AES key was generated with `SecureRandom` by Conceal's
`SharedPrefsBackedKeyChain` and stored Base64-encoded in the SharedPreferences file
`crypto.KEY_256` under the key `cipher_key` (the 128-bit variant uses file `crypto` and cipher
id `0x01`).

These details were verified against the Conceal 1.1.3 source code
(`facebookarchive/conceal`, tag `v.1.1.3`), not guessed.

## What migration does

1. Reads every entry from the `Hawk2` SharedPreferences file.
2. For each entry:
   - If it is a Conceal 1.1.3 payload (starts with the serialization version byte and a known
     cipher id), it is decrypted with the standard AES-GCM layout above using the key material
     that Conceal left on the device, then re-encrypted with the current Hawk Next encryption
     (Android Keystore + AES-GCM by default).
   - Otherwise it is decoded as Base64 plain text (the Hawk 2.x NoEncryption format) and
     re-encrypted with the current encryption.
   - If neither interpretation works, the entry is copied verbatim. The original data is never
     lost, and encrypted data is never treated as plaintext.
3. All migrated entries are written to Preferences DataStore in one transactional write.
4. The migration flag is set, then the legacy `Hawk2` file is cleared.

If the transactional write fails, nothing is deleted: the flag stays unset and the next launch
retries.

## Values that cannot be migrated

A value copied verbatim (corrupted, or encrypted with Conceal but missing its key) remains
present: `Hawk.contains(key)` returns `true`, while `Hawk.get(key)` returns `null` because the
current encryption cannot decrypt it. The `LogInterceptor` logs a warning naming the entry.

To keep such a value:

- Read the value with the old Hawk 2.x before upgrading and write it back with Hawk Next, or
- Re-save the value through the Hawk Next API after upgrading (`Hawk.put(key, value)`).

The old Conceal key material in the `crypto*` SharedPreferences files is deliberately left in
place after migration so nothing is destroyed and rollback remains possible.

## Notes

- The migration flag file and the legacy files are small; no cleanup step is required.
- `Hawk.deleteAll()` only clears the DataStore. It does not re-import or delete legacy files.
