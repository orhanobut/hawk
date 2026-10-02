# CHANGELOG

### Unreleased (3.0.0)
- Replace the default encryption with Android Keystore-backed AES-256-GCM; remove Conceal, its native binaries, and its public implementation class.
- Require API 23+ and fail initialization if default encryption is unavailable, without plaintext fallback.
- Introduce a versioned ciphertext format; existing Hawk 2 default ciphertext must be migrated before upgrading.
- Remove the native alignment lint baselines, JSR-305, JetBrains runtime annotations and unused AndroidX JUnit extensions.
- Keep Gson's Error Prone annotations because consumer R8 builds require them.
- Convert library and benchmark code to Kotlin while retaining the remaining public JVM entry points.
- Update the Android build, dependency versions, local publication and GitHub Actions.
- Expose DataInfo for custom Kotlin converters and serializers.
- Report an unbuilt facade consistently between init and build.

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