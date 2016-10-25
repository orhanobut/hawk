package com.orhanobut.hawk;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.fail;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class HawkUtilsTest {

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