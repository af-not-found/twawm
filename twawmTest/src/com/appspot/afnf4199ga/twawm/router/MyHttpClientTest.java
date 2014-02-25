package com.appspot.afnf4199ga.twawm.router;

import com.appspot.afnf4199ga.DexmakerInstrumentationTestCase;
import com.appspot.afnf4199ga.twawm.Const;

public class MyHttpClientTest extends DexmakerInstrumentationTestCase {

	public void testIsRmtMainPath() {
		assertEquals(true, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_INFO_RMTMAIN));
		assertEquals(true, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_STANDBY_RMTMAIN));
		assertEquals(true, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_STANDBY_BT_RMTMAIN));
		assertEquals(true, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_REBOOT_WM_RMTMAIN));

		assertEquals(false, MyHttpClient.isRmtMainPath(null));
		assertEquals(false, MyHttpClient.isRmtMainPath(""));
		assertEquals(false, MyHttpClient.isRmtMainPath("aa"));

		assertEquals(false, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_INFO_INFOBTN));
		assertEquals(false, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_STANDBY_INFOBTN));
		assertEquals(false, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_STANDBY_BT_INFOBTN));
		assertEquals(false, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_WIMAX_DISCN_INFOBTN));
		assertEquals(false, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_WIMAX_CONN_GETINFO));
		assertEquals(false, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_WIMAX_CONN_INFOBTN));
		assertEquals(false, MyHttpClient.isRmtMainPath(Const.ROUTER_URL_REBOOT_WM_INFOBTN));
	}

}
