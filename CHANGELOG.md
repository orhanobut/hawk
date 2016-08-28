# CHANGELOG

### 2.0.0-Alpha
- Rx support is removed
- Chain option is removed
- Facebook conceal is added as crypto provider
- Async operations are removed
- Encryption methods are removed, as default it's encrypted and fallback to no encryption mode if the crypto is not available
- LogLevel is removed. All log messages are delegated to LogInterceptor, thus you can intercept and print it. Otherwise any log message will be ignored.
- All abstraction layers are pluggable.
- Sqlite option is removed.
- Init is super fast now, no need to async operation.