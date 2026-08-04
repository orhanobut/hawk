package com.orhanobut.hawk;

import android.content.Context;

import com.google.gson.Gson;

public class HawkBuilder {

  /**
   * NEVER ever change STORAGE_TAG_DO_NOT_CHANGE and TAG_INFO.
   * It will break backward compatibility in terms of keeping previous data
   */
  private static final String STORAGE_TAG_DO_NOT_CHANGE = "Hawk2";

  private Context context;
  private Storage cryptoStorage;
  private Converter converter;
  private Parser parser;
  private Encryption encryption;
  private Serializer serializer;
  private LogInterceptor logInterceptor;
  private boolean legacyMigrationEnabled = true;

  public HawkBuilder(Context context) {
    HawkUtils.checkNull("Context", context);

    this.context = context.getApplicationContext();
  }

  public HawkBuilder setStorage(Storage storage) {
    this.cryptoStorage = storage;
    return this;
  }

  public HawkBuilder setParser(Parser parser) {
    this.parser = parser;
    return this;
  }

  public HawkBuilder setSerializer(Serializer serializer) {
    this.serializer = serializer;
    return this;
  }

  public HawkBuilder setLogInterceptor(LogInterceptor logInterceptor) {
    this.logInterceptor = logInterceptor;
    return this;
  }

  public HawkBuilder setConverter(Converter converter) {
    this.converter = converter;
    return this;
  }

  public HawkBuilder setEncryption(Encryption encryption) {
    this.encryption = encryption;
    return this;
  }

  /**
   * Enables or disables the one-time migration of Hawk 2.x SharedPreferences data into
   * Preferences DataStore. Migration is enabled by default and is only relevant when the default
   * {@link DataStoreStorage} is used.
   *
   * @param enabled true to migrate legacy data on build (default)
   */
  public HawkBuilder setLegacyMigrationEnabled(boolean enabled) {
    this.legacyMigrationEnabled = enabled;
    return this;
  }

  LogInterceptor getLogInterceptor() {
    if (logInterceptor == null) {
      logInterceptor = new LogInterceptor() {
        @Override public void onLog(String message) {
          //empty implementation
        }
      };
    }
    return logInterceptor;
  }

  Storage getStorage() {
    if (cryptoStorage == null) {
      cryptoStorage = new DataStoreStorage(context);
    }
    return cryptoStorage;
  }

  Converter getConverter() {
    if (converter == null) {
      converter = new HawkConverter(getParser());
    }
    return converter;
  }

  Parser getParser() {
    if (parser == null) {
      parser = new GsonParser(new Gson());
    }
    return parser;
  }

  Encryption getEncryption() {
    if (encryption == null) {
      encryption = new KeystoreAesGcmEncryption();
      if (!encryption.init()) {
        encryption = new NoEncryption();
      }
    }
    return encryption;
  }

  Serializer getSerializer() {
    if (serializer == null) {
      serializer = new HawkSerializer(getLogInterceptor());
    }
    return serializer;
  }

  public void build() {
    if (legacyMigrationEnabled && getStorage() instanceof DataStoreStorage) {
      new LegacyMigrator(context, getLogInterceptor(), getEncryption())
          .migrate(((DataStoreStorage) getStorage()).dataStore());
    }
    Hawk.build(this);
  }
}
