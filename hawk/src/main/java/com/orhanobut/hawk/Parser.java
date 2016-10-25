package com.orhanobut.hawk;

import java.lang.reflect.Type;

@SuppressWarnings("WeakerAccess")
public interface Parser {

  <T> T fromJson(String content, Type type) throws Exception;

  String toJson(Object body);

}
