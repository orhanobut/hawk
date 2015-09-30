package com.orhanobut.hawk;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

public final class GsonParser implements Parser {

  private final Gson gson;

  public GsonParser(Gson gson) {
    this.gson = gson;
  }

  @Override public <T> T fromJson(String content, Type type) throws JsonSyntaxException {
    if (TextUtils.isEmpty(content)) {
      return null;
    }
    return gson.fromJson(content, type);
  }

  @Override public String toJson(Object body) {
    return gson.toJson(body);
  }

}
