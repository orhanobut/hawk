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

    public void testGetDataInfoShouldReturnNull_null() {
        try {
            DataHelper.getDataInfo(null);
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testGetDataInfoShouldThrowException_NotContainDelimiter() {
        try {
            DataHelper.getDataInfo("324234");
            assertTrue(false);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testGetDataInfoShouldReturnNotNull() {
        DataInfo info = DataHelper.getDataInfo("String11@asdfjasdf");
        assertNotNull(info);
    }

    public void testGetDataInfoShouldBeSerializable() {
        DataInfo info = DataHelper.getDataInfo("String11@asdfjasdf");
        assertTrue(info.isSerializable());
    }

    public void testGetDataInfoShouldNotBeSerializable() {
        DataInfo info = DataHelper.getDataInfo("String10@asdfjasdf");
        assertFalse(info.isSerializable());
    }

    public void testGetDataInfoShouldBeList() {
        DataInfo info = DataHelper.getDataInfo("String11@asdfjasdf");
        assertTrue(info.isSerializable());
    }

    public void testGetDataInfoShouldNotBeList() {
        DataInfo info = DataHelper.getDataInfo("String00@asdfjasdf");
        assertFalse(info.isSerializable());
    }


}
