# CHANGELOG

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