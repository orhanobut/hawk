package com.orhanobut.hawk;

import android.content.Context;
import android.test.InstrumentationTestCase;

/**
 * @author Orhan Obut
 */
public class DataHelperTest extends InstrumentationTestCase {

  Context context;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(
        "dexmaker.dexcache",
        getInstrumentation().getTargetContext().getCacheDir().getPath());
    context = getInstrumentation().getContext();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    context = null;
  }

  public void testNewVersionCheck() {
    DataInfo info = DataHelper.getDataInfo("java.lang.String##00V@asdfjasdf");
    assertTrue(info.isNewVersion());
  }

}
