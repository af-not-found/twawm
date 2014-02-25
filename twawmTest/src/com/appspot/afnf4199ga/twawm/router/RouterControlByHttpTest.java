package com.appspot.afnf4199ga.twawm.router;

import java.net.InetAddress;

import org.mockito.Mockito;

import com.appspot.afnf4199ga.DexmakerInstrumentationTestCase;
import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.utils.MyTestUtils;

public class RouterControlByHttpTest extends DexmakerInstrumentationTestCase {

	public void testGetRouterIpAddr10() throws Throwable {
		InetLookupWrappter mock = Mockito.mock(InetLookupWrappter.class);

		// 実行
		String routerIpAddr = RouterControlByHttp.getRouterIpAddr(mock, "192.168.0.2");
		assertEquals("192.168.0.2", routerIpAddr);

		// 呼び出し無し
		Mockito.verifyNoMoreInteractions(mock);
	}

	/**
	 * confRouterIpAddrがグローバルの場合でもNGとしない
	 */
	public void testGetRouterIpAddr11() throws Throwable {
		InetLookupWrappter mock = Mockito.mock(InetLookupWrappter.class);

		// 実行
		String routerIpAddr = RouterControlByHttp.getRouterIpAddr(mock, "211.168.0.254");
		assertEquals("211.168.0.254", routerIpAddr);

		// 呼び出し無し
		Mockito.verifyNoMoreInteractions(mock);
	}

	/**
	 * confRouterIpAddrが不正
	 */
	public void testGetRouterIpAddr12() throws Throwable {
		InetAddress val = InetAddress.getByName("192.168.0.7");
		InetLookupWrappter mock = Mockito.mock(InetLookupWrappter.class);
		Mockito.when(mock.getByName(Mockito.anyString())).thenReturn(val);

		// 実行
		String routerIpAddr = RouterControlByHttp.getRouterIpAddr(mock, "211.168.0.255");
		assertEquals("192.168.0.7", routerIpAddr);

		// 呼び出し有り
		Mockito.verify(mock).getByName(Const.ROUTER_HOSTNAME);

		// これ以上の呼び出し無し
		Mockito.verifyNoMoreInteractions(mock);
	}

	public void testGetRouterIpAddr20() throws Throwable {
		InetAddress val = InetAddress.getByName("192.168.0.20");
		InetLookupWrappter mock = Mockito.mock(InetLookupWrappter.class);
		Mockito.when(mock.getByName(Mockito.anyString())).thenReturn(val);

		// 実行
		String routerIpAddr = RouterControlByHttp.getRouterIpAddr(mock, "");
		assertEquals("192.168.0.20", routerIpAddr);

		// 呼び出し有り
		Mockito.verify(mock).getByName(Const.ROUTER_HOSTNAME);

		// これ以上の呼び出し無し
		Mockito.verifyNoMoreInteractions(mock);
	}

	/**
	 * 正引きがグローバルの場合はNGとする（モバイルデータ通信環境を想定）
	 */
	public void testGetRouterIpAddr21() throws Throwable {
		InetAddress val = InetAddress.getByName("210.168.0.21");
		InetLookupWrappter mock = Mockito.mock(InetLookupWrappter.class);
		Mockito.when(mock.getByName(Mockito.anyString())).thenReturn(val);

		// 実行
		String routerIpAddr = RouterControlByHttp.getRouterIpAddr(mock, null);
		assertEquals(routerIpAddr, RouterControlByHttp.NOT_SITE_LOCAL_ADDR);

		// 呼び出し有り
		Mockito.verify(mock).getByName(Const.ROUTER_HOSTNAME);

		// これ以上の呼び出し無し
		Mockito.verifyNoMoreInteractions(mock);
	}

	public void testGetRouterIpAddr22() throws Throwable {
		InetLookupWrappter mock = Mockito.mock(InetLookupWrappter.class);
		Mockito.when(mock.getByName(Mockito.anyString())).thenReturn(null);

		// 実行
		String routerIpAddr = RouterControlByHttp.getRouterIpAddr(mock, "");
		assertNull(routerIpAddr);

		// 呼び出し有り
		Mockito.verify(mock).getByName(Const.ROUTER_HOSTNAME);

		// これ以上の呼び出し無し
		Mockito.verifyNoMoreInteractions(mock);
	}

	public void testUpdateRouterInfo1() {
		String content = MyTestUtils.getResourceAsString("/test-data/info_remote_main/3600_1.htm");
		RouterInfo routerInfo = new RouterInfo();
		RouterControlByHttp.updateRouterInfo(content, routerInfo);

		assertEquals("Aterm WM3600R", routerInfo.routerName);
		assertEquals(0, routerInfo.battery);
		assertEquals(true, routerInfo.charging);
		assertEquals(4, routerInfo.antennaLevel);
		assertEquals("-67", routerInfo.rssiText);
		assertEquals("22", routerInfo.cinrText);
		assertEquals("12:34:56:78:90:ab:cd", routerInfo.bluetoothAddress);
		assertEquals("C5479B515C1AA739336C801F2E1B4FEF", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
		assertEquals(false, routerInfo.notInitialized);
		assertEquals(true, routerInfo.hasStandbyButton);
	}

	public void testUpdateRouterInfo2() {
		String content = MyTestUtils.getResourceAsString("/test-data/info_remote_main/3600_2.htm");
		RouterInfo routerInfo = new RouterInfo();
		RouterControlByHttp.updateRouterInfo(content, routerInfo);

		assertEquals("Aterm WM3600R", routerInfo.routerName);
		assertEquals(44, routerInfo.battery);
		assertEquals(false, routerInfo.charging);
		assertEquals(0, routerInfo.antennaLevel);
		assertEquals("-7", routerInfo.rssiText);
		assertEquals("232", routerInfo.cinrText);
		assertNull(routerInfo.bluetoothAddress);
		assertEquals("C5479B515C1AA739336C801F2E1B4F00", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
		assertEquals(false, routerInfo.notInitialized);
		assertEquals(true, routerInfo.hasStandbyButton);
	}

	public void testUpdateRouterInfo3() {
		String content = MyTestUtils.getResourceAsString("/test-data/info_remote_main/3800_1.htm");
		RouterInfo routerInfo = new RouterInfo();
		RouterControlByHttp.updateRouterInfo(content, routerInfo);

		assertEquals("Aterm WM3800R", routerInfo.routerName);
		assertEquals(90, routerInfo.battery);
		assertEquals(true, routerInfo.charging);
		assertEquals(4, routerInfo.antennaLevel);
		assertEquals("67", routerInfo.rssiText);
		assertEquals("-332", routerInfo.cinrText);
		assertNull(routerInfo.bluetoothAddress);
		assertEquals("C5479B515C1AA739336C801F2E1B4FEF", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
		assertEquals(false, routerInfo.notInitialized);
		assertEquals(false, routerInfo.hasStandbyButton);
	}

	public void testUpdateRouterInfo4() {
		String content = MyTestUtils.getResourceAsString("/test-data/info_btn/3800_1.htm");
		RouterInfo routerInfo = new RouterInfo();
		RouterControlByHttp.updateRouterInfo(content, routerInfo);

		assertEquals("Aterm WM3800R", routerInfo.routerName);
		assertEquals(66, routerInfo.battery);
		assertEquals(false, routerInfo.charging);
		assertEquals(4, routerInfo.antennaLevel);
		assertEquals("-65", routerInfo.rssiText);
		assertEquals("25", routerInfo.cinrText);
		assertEquals("1a:88:72:63:62:ab", routerInfo.bluetoothAddress);
		assertEquals("7E27D5C477DC911EF64659825B5D3552", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
		assertEquals(false, routerInfo.notInitialized);
		assertEquals(true, routerInfo.hasStandbyButton);
	}

	public void testUpdateRouterInfo5() {
		String content = MyTestUtils.getResourceAsString("/test-data/pass_not_init/3800_1.htm");
		RouterInfo routerInfo = new RouterInfo();
		RouterControlByHttp.updateRouterInfo(content, routerInfo);

		assertEquals(null, routerInfo.routerName);
		assertEquals(0, routerInfo.battery);
		assertEquals(false, routerInfo.charging);
		assertEquals(0, routerInfo.antennaLevel);
		assertEquals(null, routerInfo.rssiText);
		assertEquals(null, routerInfo.cinrText);
		assertEquals(null, routerInfo.bluetoothAddress);
		assertEquals(null, RouterControlByHttp.hiddenMap.get("SESSION_ID"));
		assertEquals(true, routerInfo.notInitialized);
		assertEquals(true, routerInfo.hasStandbyButton);
	}

	public void testIsNotAuthedOfWmRouter() {
		assertTrue(RouterControlByHttp.isNotAuthedOfWmRouter(MyTestUtils.getResourceAsString("/test-data/not_authed/3800_1.htm")));
		assertTrue(RouterControlByHttp.isNotAuthedOfWmRouter(MyTestUtils.getResourceAsString("/test-data/not_authed/3600_1.htm")));
		assertFalse(RouterControlByHttp.isNotAuthedOfWmRouter(MyTestUtils
				.getResourceAsString("/test-data/info_remote_main/3800_1.htm")));
		assertFalse(RouterControlByHttp.isNotAuthedOfWmRouter(MyTestUtils
				.getResourceAsString("/test-data/info_remote_main/3600_1.htm")));
		assertFalse(RouterControlByHttp.isNotAuthedOfWmRouter(MyTestUtils.getResourceAsString("/test-data/info_btn/3800_1.htm")));
	}

	public void testHasBluetooth() {

		RouterInfo r = null;
		{
			assertEquals(false, RouterControlByHttp.hasBluetooth(null));
		}

		r = new RouterInfo();
		{
			r.routerName = null;
			assertEquals(false, RouterControlByHttp.hasBluetooth(r));
		}
		{
			r.routerName = "";
			assertEquals(false, RouterControlByHttp.hasBluetooth(r));
		}
		{
			r.routerName = "aaaWM3500rbbb";
			assertEquals(false, RouterControlByHttp.hasBluetooth(r));
		}
		{
			r.routerName = "aaawm3600rbbb";
			assertEquals(false, RouterControlByHttp.hasBluetooth(r));
		}
		{
			r.routerName = "wm3600R";
			assertEquals(false, RouterControlByHttp.hasBluetooth(r));
		}
		{
			r.routerName = "WM3800R";
			assertEquals(true, RouterControlByHttp.hasBluetooth(r));
		}
		{
			r.routerName = "aaaWM4000rbbb";
			assertEquals(true, RouterControlByHttp.hasBluetooth(r));
		}
		{
			r.routerName = "ccccccccccccccccc";
			assertEquals(true, RouterControlByHttp.hasBluetooth(r));
		}
	}

	public void testCanStandyByRmtMain() {

		RouterInfo r = null;
		{
			assertEquals(true, RouterControlByHttp.canStandyByRmtMain(r));
		}

		r = new RouterInfo();
		{
			r.rmtMain = true;
			r.hasStandbyButton = true;
			assertEquals(true, RouterControlByHttp.canStandyByRmtMain(r));
		}
		{
			r.rmtMain = true;
			r.hasStandbyButton = false;
			assertEquals(false, RouterControlByHttp.canStandyByRmtMain(r));
		}
		{
			r.rmtMain = false;
			r.hasStandbyButton = true;
			assertEquals(false, RouterControlByHttp.canStandyByRmtMain(r));
		}
		{
			r.rmtMain = false;
			r.hasStandbyButton = false;
			assertEquals(false, RouterControlByHttp.canStandyByRmtMain(r));
		}
	}

	public void testHasStandbyButton() {

		RouterInfo r = null;
		{
			assertEquals(false, RouterControlByHttp.hasStandbyButton(r));
		}

		r = new RouterInfo();
		{
			r.hasStandbyButton = true;
			assertEquals(true, RouterControlByHttp.hasStandbyButton(r));
		}
		{
			r.hasStandbyButton = false;
			assertEquals(false, RouterControlByHttp.hasStandbyButton(r));
		}
	}
}
