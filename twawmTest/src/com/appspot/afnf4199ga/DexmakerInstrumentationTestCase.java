package com.appspot.afnf4199ga;

import android.test.InstrumentationTestCase;

/**
 * Android4.3で発生する問題を回避するためのInstrumentationTestCase<BR>
 * http://code.google.com/p/dexmaker/issues/detail?id=2
 */
public class DexmakerInstrumentationTestCase extends InstrumentationTestCase {

	@Override
	public void setUp() throws Exception {
		super.setUp();
		System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
	}
}
