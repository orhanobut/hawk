package com.orhanobut.hawk;

public class HawkInternal {

  private Storage cryptoStorage;
  private Encoder encoder;
  private Encryption encryption;
  private LogLevel logLevel;

  HawkInternal(HawkBuilder hawkBuilder) {
    cryptoStorage = hawkBuilder.getStorage();
    encoder = hawkBuilder.getEncoder();
    encryption = hawkBuilder.getEncryption();
    logLevel = hawkBuilder.getLogLevel();
  }

  Storage getStorage() {
    return cryptoStorage;
  }

  Encoder getEncoder() {
    return encoder;
  }

  Encryption getEncryption() {
    return encryption;
  }

  LogLevel getLogLevel() {
    return logLevel;
  }

}
