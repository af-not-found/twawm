package com.appspot.afnf4199ga.twawm.router;

import java.net.InetAddress;

import net.afnf.and.twawm2.DexmakerInstrumentationTestCase;

import org.mockito.Mockito;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

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

    public void testGetRouterIpAddr10() throws Throwable {
        InetLookupWrappter mock = Mockito.mock(InetLookupWrappter.class);

        // 実行
        String routerIpAddr = MyHttpClient.getRouterIpAddr(mock, "192.168.0.2");
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
        String routerIpAddr = MyHttpClient.getRouterIpAddr(mock, "211.168.0.254");
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
        String routerIpAddr = MyHttpClient.getRouterIpAddr(mock, "211.168.0.255");
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
        String routerIpAddr = MyHttpClient.getRouterIpAddr(mock, "");
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
        String routerIpAddr = MyHttpClient.getRouterIpAddr(mock, null);
        assertEquals(routerIpAddr, MyHttpClient.NOT_SITE_LOCAL_ADDR);

        // 呼び出し有り
        Mockito.verify(mock).getByName(Const.ROUTER_HOSTNAME);

        // これ以上の呼び出し無し
        Mockito.verifyNoMoreInteractions(mock);
    }

    public void testGetRouterIpAddr22() throws Throwable {
        InetLookupWrappter mock = Mockito.mock(InetLookupWrappter.class);
        Mockito.when(mock.getByName(Mockito.anyString())).thenReturn(null);

        // 実行
        String routerIpAddr = MyHttpClient.getRouterIpAddr(mock, "");
        assertNull(routerIpAddr);

        // 呼び出し有り
        Mockito.verify(mock).getByName(Const.ROUTER_HOSTNAME);

        // これ以上の呼び出し無し
        Mockito.verifyNoMoreInteractions(mock);
    }

    //
    //        // ゲートウェイIPアドレス取得
    //        DhcpInfo dhcpInfo = wifi.getDhcpInfo();
    //        if (dhcpInfo != null) {
    //            return AndroidUtils.intToIpaddr(dhcpInfo.gateway);
    //        }
    //
    //        // クライアントIPアドレス取得
    //        WifiInfo connectionInfo = wifi.getConnectionInfo();
    //        if (connectionInfo != null) {
    //            String ipaddr = AndroidUtils.intToIpaddr(connectionInfo.getIpAddress());
    //            if (ipaddr.indexOf(Const.ROUTER_IPADDR_NAD_PREFIX) == 0) {
    //                return Const.ROUTER_IPADDR_NAD_PREFIX + "1";
    //            }
    //            else if (ipaddr.indexOf(Const.ROUTER_IPADDR_WM_PREFIX) == 0) {
    //                return Const.ROUTER_IPADDR_WM_PREFIX + "1";
    //            }
    //        }

    public void testEstimateRouterIpAddr1() {
        WifiManager wifi = Mockito.mock(WifiManager.class);
        Mockito.when(wifi.getDhcpInfo()).thenReturn(null);
        Mockito.when(wifi.getConnectionInfo()).thenReturn(null);
        assertEquals(null, MyHttpClient.estimateRouterIpAddr(wifi));
    }

    public void testEstimateRouterIpAddr2() {
        DhcpInfo dhcpInfo = new DhcpInfo();
        dhcpInfo.gateway = 0x04030201;

        WifiManager wifi = Mockito.mock(WifiManager.class);
        Mockito.when(wifi.getDhcpInfo()).thenReturn(dhcpInfo);
        Mockito.when(wifi.getConnectionInfo()).thenReturn(null);
        assertEquals("1.2.3.4", MyHttpClient.estimateRouterIpAddr(wifi));
    }

    public void testEstimateRouterIpAddr3() {
        WifiInfo wi = Mockito.mock(WifiInfo.class);
        Mockito.when(wi.getIpAddress()).thenReturn(0x05030201); // 1.2.3.5

        WifiManager wifi = Mockito.mock(WifiManager.class);
        Mockito.when(wifi.getDhcpInfo()).thenReturn(null);
        Mockito.when(wifi.getConnectionInfo()).thenReturn(wi);
        assertEquals("1.2.3.1", MyHttpClient.estimateRouterIpAddr(wifi));
    }

    public void testEstimateRouterIpAddr4() {
        WifiInfo wi = Mockito.mock(WifiInfo.class);
        Mockito.when(wi.getIpAddress()).thenReturn(0x05b3a8c0); // 192.168.179.5

        WifiManager wifi = Mockito.mock(WifiManager.class);
        Mockito.when(wifi.getDhcpInfo()).thenReturn(null);
        Mockito.when(wifi.getConnectionInfo()).thenReturn(wi);
        assertEquals("192.168.179.1", MyHttpClient.estimateRouterIpAddr(wifi));
    }
}
