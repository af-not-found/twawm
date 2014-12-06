package com.appspot.afnf4199ga.twawm.router;

import net.afnf.and.twawm2.DexmakerInstrumentationTestCase;

import com.appspot.afnf4199ga.twawm.router.RouterInfo.COM_TYPE;
import com.appspot.afnf4199ga.utils.MyTestUtils;

public class RouterControlByHttpTest extends DexmakerInstrumentationTestCase {

    public void testUpdateRouterInfo11() {
        String content = MyTestUtils.getResourceAsString("/test-data/info_remote_main/3600_1.htm");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals("Aterm WM3600R", routerInfo.routerName);
        assertEquals(0, routerInfo.battery);
        assertEquals(true, routerInfo.charging);
        assertEquals(4, routerInfo.antennaLevel);
        assertEquals("-67", routerInfo.rssiText);
        assertEquals("22", routerInfo.cinrText);
        assertEquals("12:34:56:78:90:AB:CD", routerInfo.bluetoothAddress);
        assertEquals("C5479B515C1AA739336C801F2E1B4FEF", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NA, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo12() {
        String content = MyTestUtils.getResourceAsString("/test-data/info_remote_main/3600_2.htm");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals("Aterm WM3600R", routerInfo.routerName);
        assertEquals(44, routerInfo.battery);
        assertEquals(false, routerInfo.charging);
        assertEquals(0, routerInfo.antennaLevel);
        assertEquals("-7", routerInfo.rssiText);
        assertEquals("232", routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals("C5479B515C1AA739336C801F2E1B4F00", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NA, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo13() {
        String content = MyTestUtils.getResourceAsString("/test-data/info_remote_main/3800_1.htm");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals("Aterm WM3800R", routerInfo.routerName);
        assertEquals(90, routerInfo.battery);
        assertEquals(true, routerInfo.charging);
        assertEquals(4, routerInfo.antennaLevel);
        assertEquals("67", routerInfo.rssiText);
        assertEquals("-332", routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals("C5479B515C1AA739336C801F2E1B4FEF", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(false, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NA, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo21() {
        String content = MyTestUtils.getResourceAsString("/test-data/info_btn/3800_1.htm");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals("Aterm WM3800R", routerInfo.routerName);
        assertEquals(66, routerInfo.battery);
        assertEquals(false, routerInfo.charging);
        assertEquals(4, routerInfo.antennaLevel);
        assertEquals("-65", routerInfo.rssiText);
        assertEquals("25", routerInfo.cinrText);
        assertEquals("1A:88:72:63:62:AB", routerInfo.bluetoothAddress);
        assertEquals("7E27D5C477DC911EF64659825B5D3552", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NA, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo22() {
        String content = MyTestUtils.getResourceAsString("/test-data/info_btn/3800_2_spot.htm");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals("Aterm WM3800R", routerInfo.routerName);
        assertEquals(65, routerInfo.battery);
        assertEquals(false, routerInfo.charging);
        assertEquals(5, routerInfo.antennaLevel);
        assertEquals("-65", routerInfo.rssiText);
        assertEquals("25", routerInfo.cinrText);
        assertEquals("1A:88:72:63:62:AB", routerInfo.bluetoothAddress);
        assertEquals("7E27D5C477DC911EF64659825B5D3552", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.WIFI_SPOT, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo23() {
        String content = MyTestUtils.getResourceAsString("/test-data/info_btn/nad11.htm", false);
        RouterInfo routerInfo = new RouterInfo();
        routerInfo.nad = true;

        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals(null, routerInfo.routerName);
        assertEquals(-1, routerInfo.battery);
        assertEquals(false, routerInfo.charging);
        assertEquals(0, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals("0000000000000000000000002222222222b", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NA, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo24() {
        String content = MyTestUtils.getResourceAsString("/test-data/index_contents_pass/nad11.htm", false);
        RouterInfo routerInfo = new RouterInfo();
        routerInfo.nad = true;

        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals("NAD11", routerInfo.routerName);
        assertEquals(-1, routerInfo.battery);
        assertEquals(false, routerInfo.charging);
        assertEquals(0, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals("22:33:44:FF:BB:A0", routerInfo.bluetoothAddress);
        assertEquals("0000000000000000000000001111111111a", RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NA, routerInfo.comState);
        assertEquals(COM_TYPE.NO_LIMIT, routerInfo.comSetting);
        assertEquals(Boolean.TRUE, routerInfo.wifiSpotEnabled);
        assertEquals("2", routerInfo.profile);
    }

    public void testUpdateRouterInfo31() {
        String content = MyTestUtils.getResourceAsString("/test-data/pass_not_init/3800_1.htm");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals(null, routerInfo.routerName);
        assertEquals(-1, routerInfo.battery);
        assertEquals(false, routerInfo.charging);
        assertEquals(0, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals(null, RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(true, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NA, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo32() {
        String content = MyTestUtils.getResourceAsString("/test-data/pass_not_init/nad11.htm", false);
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals(null, routerInfo.routerName);
        assertEquals(-1, routerInfo.battery);
        assertEquals(false, routerInfo.charging);
        assertEquals(0, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals(null, RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(true, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NA, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo41() {
        String content = MyTestUtils.getResourceAsString("/test-data/status_get/nad11_hs_ant3.xml");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals(null, routerInfo.routerName);
        assertEquals(64, routerInfo.battery);
        assertEquals(true, routerInfo.charging);
        assertEquals(3, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals(null, RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.HIGH_SPEED, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo42() {
        String content = MyTestUtils.getResourceAsString("/test-data/status_get/nad11_hs_ant4.xml");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals(null, routerInfo.routerName);
        assertEquals(64, routerInfo.battery);
        assertEquals(true, routerInfo.charging);
        assertEquals(4, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals(null, RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.HIGH_SPEED, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo43() {
        String content = MyTestUtils.getResourceAsString("/test-data/status_get/nad11_nl_ant3.xml");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals(null, routerInfo.routerName);
        assertEquals(64, routerInfo.battery);
        assertEquals(true, routerInfo.charging);
        assertEquals(3, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals(null, RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NO_LIMIT, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo44() {
        String content = MyTestUtils.getResourceAsString("/test-data/status_get/nad11_nl_ant4.xml");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals(null, routerInfo.routerName);
        assertEquals(64, routerInfo.battery);
        assertEquals(false, routerInfo.charging);
        assertEquals(4, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals(null, RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NO_LIMIT, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo45() {
        String content = MyTestUtils.getResourceAsString("/test-data/status_get/nad11_nl_ant5.xml");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals(null, routerInfo.routerName);
        assertEquals(8, routerInfo.battery);
        assertEquals(false, routerInfo.charging);
        assertEquals(5, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals(null, RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.NO_LIMIT, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo46() {
        String content = MyTestUtils.getResourceAsString("/test-data/status_get/nad11_spot_ant3.xml");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals(null, routerInfo.routerName);
        assertEquals(64, routerInfo.battery);
        assertEquals(true, routerInfo.charging);
        assertEquals(3, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals(null, RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.WIFI_SPOT, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
    }

    public void testUpdateRouterInfo47() {
        String content = MyTestUtils.getResourceAsString("/test-data/status_get/nad11_spot_ant5.xml");
        RouterInfo routerInfo = new RouterInfo();
        RouterControlByHttp.parseContent(content, routerInfo);

        assertEquals(null, routerInfo.routerName);
        assertEquals(64, routerInfo.battery);
        assertEquals(true, routerInfo.charging);
        assertEquals(5, routerInfo.antennaLevel);
        assertEquals(null, routerInfo.rssiText);
        assertEquals(null, routerInfo.cinrText);
        assertEquals(null, routerInfo.bluetoothAddress);
        assertEquals(null, RouterControlByHttp.hiddenMap.get("SESSION_ID"));
        assertEquals(false, routerInfo.notInitialized);
        assertEquals(true, routerInfo.hasStandbyButton);
        assertEquals(COM_TYPE.WIFI_SPOT, routerInfo.comState);
        assertEquals(COM_TYPE.NA, routerInfo.comSetting);
        assertEquals(null, routerInfo.wifiSpotEnabled);
        assertEquals(null, routerInfo.profile);
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
