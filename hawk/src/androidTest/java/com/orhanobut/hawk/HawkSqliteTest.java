package com.orhanobut.hawk;

/**
 * @author Orhan Obut
 */
public class HawkSqliteTest extends HawkTest {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(
        "dexmaker.dexcache",
        getInstrumentation().getTargetContext().getCacheDir().getPath());
    context = getInstrumentation().getContext();
    Hawk.init(context)
        .setStorage(HawkBuilder.newSqliteStorage(context))
        .build();
  }

  public void testHugeData() {
    for (int i = 0; i < 10000; i++) {
      Hawk.put("" + i, "" + i);
    }
    assertTrue(true);
  }

  public void testHugeDataWithBulk() {
    Hawk.Chain chain = Hawk.chain();
    for (int i = 0; i < 10000; i++) {
      chain.put("" + i, "" + i);
    }
    chain.commit();
    assertTrue(true);
  }
}
