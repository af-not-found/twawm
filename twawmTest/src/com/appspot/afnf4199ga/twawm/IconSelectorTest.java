package com.appspot.afnf4199ga.twawm;

import junit.framework.TestCase;
import net.afnf.and.twawm2.R;

import com.appspot.afnf4199ga.twawm.StateMachine.NETWORK_STATE;
import com.appspot.afnf4199ga.twawm.router.RouterInfo.COM_TYPE;

public class IconSelectorTest extends TestCase {

    public void testSelectNotifyIcon_hs() {
        COM_TYPE ot = COM_TYPE.HIGH_SPEED;

        {
            NETWORK_STATE ns = NETWORK_STATE.ONLINE;
            assertEquals(R.drawable.ntficon_hs_wimax_green_1_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_green_1_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_green_1_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_green_2_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_green_4_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_green_6_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_green_6_batt_080, IconSelector.selectNotifyIcon(4, 89, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_green_6_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_green_6_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }

        {
            NETWORK_STATE ns = NETWORK_STATE.OFFLINE;
            assertEquals(R.drawable.ntficon_hs_wimax_white_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_white_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_white_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_white_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_white_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_white_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_white_batt_080, IconSelector.selectNotifyIcon(4, 89, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_white_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_hs_wimax_white_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }

        for (int i = 0; i < 2; i++) {
            NETWORK_STATE ns = i == 0 ? NETWORK_STATE.AP_NOT_FOUND : NETWORK_STATE.NOT_WM_ROUTER;
            assertEquals(R.drawable.ntficon_wimax_gray_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot)); // WiMAXアイコンで正解
            assertEquals(R.drawable.ntficon_wimax_gray_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_080, IconSelector.selectNotifyIcon(5, 89, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }
    }

    public void testSelectNotifyIcon_nl() {
        COM_TYPE ot = COM_TYPE.NO_LIMIT;

        {
            NETWORK_STATE ns = NETWORK_STATE.ONLINE;
            assertEquals(R.drawable.ntficon_wimax_green_1_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_1_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_1_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_2_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_4_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_5_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_6_batt_080, IconSelector.selectNotifyIcon(5, 89, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_6_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_6_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }

        {
            NETWORK_STATE ns = NETWORK_STATE.OFFLINE;
            assertEquals(R.drawable.ntficon_wimax_white_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_080, IconSelector.selectNotifyIcon(5, 89, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }

        for (int i = 0; i < 2; i++) {
            NETWORK_STATE ns = i == 0 ? NETWORK_STATE.AP_NOT_FOUND : NETWORK_STATE.NOT_WM_ROUTER;
            assertEquals(R.drawable.ntficon_wimax_gray_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_080, IconSelector.selectNotifyIcon(5, 89, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }
    }

    public void testSelectNotifyIcon_na() {
        COM_TYPE ot = COM_TYPE.NA;

        {
            NETWORK_STATE ns = NETWORK_STATE.ONLINE;
            assertEquals(R.drawable.ntficon_wimax_green_1_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_1_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_1_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_2_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_3_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_4_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_5_batt_080, IconSelector.selectNotifyIcon(5, 89, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_6_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_green_6_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }

        {
            NETWORK_STATE ns = NETWORK_STATE.OFFLINE;
            assertEquals(R.drawable.ntficon_wimax_white_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_080, IconSelector.selectNotifyIcon(5, 89, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_white_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }

        for (int i = 0; i < 2; i++) {
            NETWORK_STATE ns = i == 0 ? NETWORK_STATE.AP_NOT_FOUND : NETWORK_STATE.NOT_WM_ROUTER;
            assertEquals(R.drawable.ntficon_wimax_gray_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_080, IconSelector.selectNotifyIcon(5, 89, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }
    }

    public void testSelectNotifyIcon_spot() {
        COM_TYPE ot = COM_TYPE.WIFI_SPOT;

        {
            NETWORK_STATE ns = NETWORK_STATE.ONLINE;
            assertEquals(R.drawable.ntficon_wifi_green_1_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_green_1_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_green_1_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_green_2_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_green_3_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_green_4_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_green_5_batt_080, IconSelector.selectNotifyIcon(5, 89, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_green_6_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_green_6_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }

        {
            NETWORK_STATE ns = NETWORK_STATE.OFFLINE;
            assertEquals(R.drawable.ntficon_wifi_white_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_white_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_white_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_white_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_white_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_white_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_white_batt_080, IconSelector.selectNotifyIcon(5, 89, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_white_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_wifi_white_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }

        for (int i = 0; i < 2; i++) {
            NETWORK_STATE ns = i == 0 ? NETWORK_STATE.AP_NOT_FOUND : NETWORK_STATE.NOT_WM_ROUTER;
            assertEquals(R.drawable.ntficon_wimax_gray_batt_000, IconSelector.selectNotifyIcon(1, 0, ns, ot)); // WiMAXアイコンで正解
            assertEquals(R.drawable.ntficon_wimax_gray_batt_010, IconSelector.selectNotifyIcon(1, 10, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_020, IconSelector.selectNotifyIcon(1, 20, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_040, IconSelector.selectNotifyIcon(2, 40, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_060, IconSelector.selectNotifyIcon(3, 60, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_070, IconSelector.selectNotifyIcon(4, 71, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_080, IconSelector.selectNotifyIcon(5, 89, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_100, IconSelector.selectNotifyIcon(6, 100, ns, ot));
            assertEquals(R.drawable.ntficon_wimax_gray_batt_na, IconSelector.selectNotifyIcon(6, -1, ns, ot));
        }
    }

    public void testSelectWdIcon_hs() {
        COM_TYPE ot = COM_TYPE.HIGH_SPEED;

        {
            NETWORK_STATE ns = NETWORK_STATE.ONLINE;
            assertEquals(R.drawable.icon_hs_wimax_green_1_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_green_1_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_green_1_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_green_2_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_green_4_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_green_6_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_green_6_batt_080, IconSelector.selectWdIcon(4, 89, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_green_6_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_green_6_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }

        {
            NETWORK_STATE ns = NETWORK_STATE.OFFLINE;
            assertEquals(R.drawable.icon_hs_wimax_white_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_white_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_white_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_white_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_white_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_white_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_white_batt_080, IconSelector.selectWdIcon(4, 89, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_white_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_hs_wimax_white_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }

        for (int i = 0; i < 2; i++) {
            NETWORK_STATE ns = i == 0 ? NETWORK_STATE.AP_NOT_FOUND : NETWORK_STATE.NOT_WM_ROUTER;
            assertEquals(R.drawable.icon_wimax_gray_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot)); // WiMAXアイコンで正解
            assertEquals(R.drawable.icon_wimax_gray_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_080, IconSelector.selectWdIcon(5, 89, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }
    }

    public void testSelectWdIcon_nl() {
        COM_TYPE ot = COM_TYPE.NO_LIMIT;

        {
            NETWORK_STATE ns = NETWORK_STATE.ONLINE;
            assertEquals(R.drawable.icon_wimax_green_1_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_1_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_1_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_2_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_4_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_5_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_6_batt_080, IconSelector.selectWdIcon(5, 89, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_6_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_6_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }

        {
            NETWORK_STATE ns = NETWORK_STATE.OFFLINE;
            assertEquals(R.drawable.icon_wimax_white_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_080, IconSelector.selectWdIcon(5, 89, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }

        for (int i = 0; i < 2; i++) {
            NETWORK_STATE ns = i == 0 ? NETWORK_STATE.AP_NOT_FOUND : NETWORK_STATE.NOT_WM_ROUTER;
            assertEquals(R.drawable.icon_wimax_gray_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_080, IconSelector.selectWdIcon(5, 89, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }
    }

    public void testSelectWdIcon_na() {
        COM_TYPE ot = COM_TYPE.NA;

        {
            NETWORK_STATE ns = NETWORK_STATE.ONLINE;
            assertEquals(R.drawable.icon_wimax_green_1_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_1_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_1_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_2_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_3_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_4_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_5_batt_080, IconSelector.selectWdIcon(5, 89, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_6_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_wimax_green_6_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }

        {
            NETWORK_STATE ns = NETWORK_STATE.OFFLINE;
            assertEquals(R.drawable.icon_wimax_white_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_080, IconSelector.selectWdIcon(5, 89, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_wimax_white_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }

        for (int i = 0; i < 2; i++) {
            NETWORK_STATE ns = i == 0 ? NETWORK_STATE.AP_NOT_FOUND : NETWORK_STATE.NOT_WM_ROUTER;
            assertEquals(R.drawable.icon_wimax_gray_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_080, IconSelector.selectWdIcon(5, 89, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }
    }

    public void testSelectWdIcon_spot() {
        COM_TYPE ot = COM_TYPE.WIFI_SPOT;

        {
            NETWORK_STATE ns = NETWORK_STATE.ONLINE;
            assertEquals(R.drawable.icon_wifi_green_1_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot));
            assertEquals(R.drawable.icon_wifi_green_1_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_wifi_green_1_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_wifi_green_2_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_wifi_green_3_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_wifi_green_4_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_wifi_green_5_batt_080, IconSelector.selectWdIcon(5, 89, ns, ot));
            assertEquals(R.drawable.icon_wifi_green_6_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_wifi_green_6_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }

        {
            NETWORK_STATE ns = NETWORK_STATE.OFFLINE;
            assertEquals(R.drawable.icon_wifi_white_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot));
            assertEquals(R.drawable.icon_wifi_white_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_wifi_white_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_wifi_white_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_wifi_white_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_wifi_white_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_wifi_white_batt_080, IconSelector.selectWdIcon(5, 89, ns, ot));
            assertEquals(R.drawable.icon_wifi_white_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_wifi_white_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }

        for (int i = 0; i < 2; i++) {
            NETWORK_STATE ns = i == 0 ? NETWORK_STATE.AP_NOT_FOUND : NETWORK_STATE.NOT_WM_ROUTER;
            assertEquals(R.drawable.icon_wimax_gray_batt_000, IconSelector.selectWdIcon(1, 0, ns, ot)); // WiMAXアイコンで正解
            assertEquals(R.drawable.icon_wimax_gray_batt_010, IconSelector.selectWdIcon(1, 10, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_020, IconSelector.selectWdIcon(1, 20, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_040, IconSelector.selectWdIcon(2, 40, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_060, IconSelector.selectWdIcon(3, 60, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_070, IconSelector.selectWdIcon(4, 71, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_080, IconSelector.selectWdIcon(5, 89, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_100, IconSelector.selectWdIcon(6, 100, ns, ot));
            assertEquals(R.drawable.icon_wimax_gray_batt_na, IconSelector.selectWdIcon(6, -1, ns, ot));
        }
    }
}
