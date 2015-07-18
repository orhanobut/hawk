package com.orhanobut.hawk;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Orhan Obut
 */
public class HawkEncoderTest extends InstrumentationTestCase {

  Context context;
  Encoder encoder;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(
        "dexmaker.dexcache",
        getInstrumentation().getTargetContext().getCacheDir().getPath());
    context = getInstrumentation().getContext();

    encoder = new HawkEncoder(
        new GsonParser(new Gson())
    );
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    context = null;
  }

  public void testEncodeShouldReturnNull() {
    Object object = encoder.encode(null);
    assertNull(object);
  }

  public void testDecodeShouldReturnNull() {
    Object result;
    try {
      result = encoder.decode(null, null);
    } catch (Exception e) {
      result = null;
    }

    assertNull(result);
  }

  public void testConstructorShouldThrowNPE() {
    try {
      new HawkEncoder(null);
      assertTrue(false);
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  public void testEncodeShouldNotReturnNull_string() {
    byte[] result = encoder.encode("asdf");
    assertNotNull(result);
  }

  public void testEncodeShouldNotReturnNull_primitive() {
    assertNotNull(encoder.encode(0));
    assertNotNull(encoder.encode(true));
    assertNotNull(encoder.encode('c'));
  }

  public void testEncodeShouldNotReturnNull_list() {
    List<String> list = new ArrayList<>();
    list.add("asdfdsf");
    list.add("asdfdsf");
    list.add("asdfdsf");

    assertNotNull(encoder.encode(list));
  }

}
