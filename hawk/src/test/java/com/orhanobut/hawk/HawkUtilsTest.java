package com.orhanobut.hawk;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;


@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class HawkUtilsTest {

  Context context;

  @Before public void setup() {
    context = RuntimeEnvironment.application;
  }

  @Test public void hasRxJavaOnClasspath() throws Exception {
    //TODO
  }

  @Test public void checkRx() throws Exception {
    //TODO
  }

  @Test public void validateBuild() throws Exception {
    try {
      Hawk.init(context);
      HawkUtils.validateBuild();
      fail("should throw exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Hawk is not built. " +
          "Please call build() and wait the initialisation finishes.");
    }
  }

  @Test public void checkNullShouldDoNothing() {
    try {
      HawkUtils.checkNull("foo", "test");
    } catch (Exception e) {
      fail("it should not throw exception");
    }
  }

  @Test public void checkNullShouldThrowException() throws Exception {
    try {
      HawkUtils.checkNull("foo", null);
      fail("should throw exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("foo should not be null");
    }
  }

  @Test public void checkNullOrEmptyThrowException() throws Exception {
    try {
      HawkUtils.checkNullOrEmpty("foo", null);
      fail("should throw exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("foo should not be null or empty");
    }
  }

  @Test public void checkNullOrEmptyShouldDoNothing() throws Exception {
    try {
      HawkUtils.checkNullOrEmpty("foo", "bar");
    } catch (Exception e) {
      fail("should not throw exception");
    }
  }

  @Test public void isEmpty() throws Exception {
    assertThat(HawkUtils.isEmpty(null)).isTrue();
    assertThat(HawkUtils.isEmpty("")).isTrue();
    assertThat(HawkUtils.isEmpty(" ")).isTrue();
    assertThat(HawkUtils.isEmpty("foo")).isFalse();
  }
}