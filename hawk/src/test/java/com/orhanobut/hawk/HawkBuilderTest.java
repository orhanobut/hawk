package com.orhanobut.hawk;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;

import rx.Observable;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class HawkBuilderTest {

  static final long LATCH_TIMEOUT_IN_SECONDS = 3;

  @Spy HawkBuilder builder;
  Context context;

  static class CustomParser implements Parser {
    private final Gson gson;

    public CustomParser(Gson gson) {
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

  @Before public void setup() {
    context = RuntimeEnvironment.application;
    builder = new HawkBuilder(context);

    initMocks(this);
  }

  @Test public void contextShouldNotBeNullOnInit() {
    try {
      new HawkBuilder(null);
      fail("Context should not be null");
    } catch (Exception e) {
      assertThat(e).hasMessage("Context should not be null");
    }
  }

  @Test public void testDefaultEncryptionMode() {
    assertThat(builder.getEncryptionMethod())
        .isEqualTo(HawkBuilder.EncryptionMethod.MEDIUM);
  }

  @Test public void testNoEncryptionMode() {
    builder.setEncryptionMethod(HawkBuilder.EncryptionMethod.NO_ENCRYPTION)
        .build();

    assertThat(builder.getEncryptionMethod())
        .isEqualTo(HawkBuilder.EncryptionMethod.NO_ENCRYPTION);
  }

  @Test public void highestEncryptionModeShouldHavePassword() {
    try {
      builder.setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
          .build();

      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Password cannot be null " +
          "if encryption mode is highest");
    }
  }

  @Test public void testHighestEncryptionMethod() {
    builder.setEncryptionMethod(HawkBuilder.EncryptionMethod.HIGHEST)
        .setPassword("password")
        .build();

    assertThat(builder.getEncryptionMethod())
        .isEqualTo(HawkBuilder.EncryptionMethod.HIGHEST);
  }

  @Test public void testPassword() {
    try {
      builder.setPassword(null);
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Password should not be null or empty");
    }
    try {
      builder.setPassword("");
      fail();
    } catch (Exception e) {
      assertThat(e).hasMessage("Password should not be null or empty");
    }

    builder.setPassword("password");

    assertThat(builder.getPassword()).isEqualTo("password");
  }

  @Test public void testDefaultLogLevel() {
    builder.build();

    assertThat(builder.getLogLevel()).isEqualTo(LogLevel.NONE);
  }

  @Test public void testCustomLogLevel() {
    builder.setLogLevel(LogLevel.FULL).build();

    assertThat(builder.getLogLevel()).isEqualTo(LogLevel.FULL);
  }

  @Test public void testDefaultStorage() {
    builder.build();

    assertThat(builder.getStorage()).isInstanceOf(SharedPreferencesStorage.class);
  }

  @Test public void testCustomStorage() {
    builder.setStorage(HawkBuilder.newSqliteStorage(context))
        .build();

    assertThat(builder.getStorage()).isInstanceOf(SqliteStorage.class);
  }

  @Test public void testDefaultParser() {
    builder.build();

    assertThat(builder.getParser()).isInstanceOf(GsonParser.class);
  }

  @Test public void testCustomParser() {
    CustomParser parser = new CustomParser(new Gson());
    builder.setParser(parser)
        .build();

    assertThat(builder.getParser()).isInstanceOf(CustomParser.class);
  }

  @Test public void testDefaultEncoded() {
    builder.build();

    assertThat(builder.getEncoder()).isInstanceOf(HawkEncoder.class);
  }

  @Test public void testBuild() {
    builder.build();

    verify(builder).startBuild();
  }

  @Test public void testBuildWithCallback() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    HawkBuilder.Callback callback = new HawkBuilder.Callback() {
      @Override
      public void onSuccess() {
        latch.countDown();
      }

      @Override
      public void onFail(Exception e) {
        latch.countDown();
      }
    };
    builder.build(callback);

    assertThat(latch.await(LATCH_TIMEOUT_IN_SECONDS, SECONDS)).isTrue();
  }

  @Test public void testRxBuild() {
    Observable<Boolean> observable = builder.buildRx();

    assertThat(observable).isNotNull();
  }
}
