package com.appspot.afnf4199ga.twawm;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import net.afnf.and.twawm2.DexmakerInstrumentationTestCase;
import android.os.Build;

import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class HostnameListTest extends DexmakerInstrumentationTestCase {

	public void testResolve() throws InterruptedException {

		int failed = 0;

		// エミュレーターだと止まってしまうので実行しない
		if (MyStringUtlis.eqauls(Build.DEVICE, "generic") == false) {

			Collections.sort(HostnameList.hosts);

			for (String hostname : HostnameList.hosts) {
				InetAddress inetAddress = null;
				try {
					inetAddress = InetAddress.getByName(hostname);
				}
				catch (UnknownHostException e) {
					failed++;
				}

				if (inetAddress == null) {
					Logger.w(hostname + " failed");
				}
				else {
					Logger.i(hostname + " " + inetAddress.getHostAddress());
				}
			}
		}

		assertEquals(0, failed);
	}
}
