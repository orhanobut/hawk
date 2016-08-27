package com.orhanobut.hawk;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;

public class HawkBuilder {

  /**
   * NEVER ever change STORAGE_TAG_DO_NOT_CHANGE and TAG_INFO.
   * It will break backward compatibility in terms of keeping previous data
   */
  private static final String STORAGE_TAG_DO_NOT_CHANGE = "Hawk2";

  private Context context;
  private String password;
  private Storage cryptoStorage;
  private Converter converter;
  private Parser parser;
  private Encryption encryption;
  private Serializer serializer;

  public HawkBuilder(Context context) {
    HawkUtils.checkNull("Context", context);

    this.context = context.getApplicationContext();
  }

  public HawkBuilder setPassword(String password) {
    if (TextUtils.isEmpty(password)) {
      throw new NullPointerException("Password should not be null or empty");
    }
    this.password = password;
    return this;
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

  HawkBuilder setConverter(Converter converter) {
    this.converter = converter;
    return this;
  }

  HawkBuilder setEncryption(Encryption encryption) {
    this.encryption = encryption;
    return this;
  }

  String getPassword() {
    return password;
  }

  Storage getStorage() {
    if (cryptoStorage == null) {
      cryptoStorage = new SharedPreferencesStorage(context, STORAGE_TAG_DO_NOT_CHANGE);
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
      encryption = new ConcealEncryption(context);
      if (!encryption.init()) {
        encryption = new Base64Encryption();
      }
    }
    return encryption;
  }

  Serializer getSerializer() {
    if (serializer == null) {
      serializer = new HawkSerializer();
    }
    return serializer;
  }

  public void build() {
    Hawk.build(this);
  }
}
