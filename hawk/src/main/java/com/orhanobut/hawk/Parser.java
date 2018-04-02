package com.orhanobut.hawk;

import java.lang.reflect.Type;

/**
 * Intermediate layer that handles serialization/deserialization for the end result.
 * This is not the same as {@link Serializer}. This interface is only used to convert the intermediate value
 * into String or vice-versa to be used for {@link Storage}
 *
 * <p>Use custom implementation if built-in implementation is not enough.</p>
 *
 * @see GsonParser
 */
public interface Parser {

  /**
   * Deserialize the given text for the given type and returns it.
   *
   * @param content is the value that will be deserialized.
   * @param type is the object type which value will be converted to.
   * @param <T> is the expected type.
   * @return the expected type.
   * @throws Exception if the operation is not successful.
   */
  <T> T fromJson(String content, Type type) throws Exception;

  /**
   * Serialize the given object to String.
   *
   * @param body is the object that will be serialized.
   * @return the serialized text.
   */
  String toJson(Object body);

}
