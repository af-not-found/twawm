package com.appspot.afnf4199ga.twawm;

import net.afnf.and.twawm2.R;

import com.appspot.afnf4199ga.twawm.StateMachine.NETWORK_STATE;
import com.appspot.afnf4199ga.twawm.router.RouterInfo.COM_TYPE;

public class IconSelector {

    public static int selectNotifyIcon(int antennaLevel, int batteryLevel, NETWORK_STATE netState, COM_TYPE comState) {

        int battOffset = battOffset(batteryLevel);

        if (netState == NETWORK_STATE.AP_NOT_FOUND || netState == NETWORK_STATE.NOT_WM_ROUTER) {
            return R.drawable.ntficon_wimax_gray_batt_000 + battOffset;
        }
        else if (netState == NETWORK_STATE.OFFLINE) {
            antennaLevel = 0;
        }

        switch (comState) {
        case HIGH_SPEED:
            switch (antennaLevel) {
            case 1:
                return R.drawable.ntficon_hs_wimax_green_1_batt_000 + battOffset;
            case 2:
                return R.drawable.ntficon_hs_wimax_green_2_batt_000 + battOffset;
            case 3:
                return R.drawable.ntficon_hs_wimax_green_4_batt_000 + battOffset;
            case 4:
            case 5:
            case 6:
                return R.drawable.ntficon_hs_wimax_green_6_batt_000 + battOffset;
            default:
                return R.drawable.ntficon_hs_wimax_white_batt_000 + battOffset;
            }

        case NO_LIMIT:
            switch (antennaLevel) {
            case 1:
                return R.drawable.ntficon_wimax_green_1_batt_000 + battOffset;
            case 2:
                return R.drawable.ntficon_wimax_green_2_batt_000 + battOffset;
            case 3:
                return R.drawable.ntficon_wimax_green_4_batt_000 + battOffset;
            case 4:
                return R.drawable.ntficon_wimax_green_5_batt_000 + battOffset;
            case 5:
            case 6:
                return R.drawable.ntficon_wimax_green_6_batt_000 + battOffset;
            default:
                return R.drawable.ntficon_wimax_white_batt_000 + battOffset;
            }

        case WIFI_SPOT:
            switch (antennaLevel) {
            case 1:
                return R.drawable.ntficon_wifi_green_1_batt_000 + battOffset;
            case 2:
                return R.drawable.ntficon_wifi_green_2_batt_000 + battOffset;
            case 3:
                return R.drawable.ntficon_wifi_green_3_batt_000 + battOffset;
            case 4:
                return R.drawable.ntficon_wifi_green_4_batt_000 + battOffset;
            case 5:
                return R.drawable.ntficon_wifi_green_5_batt_000 + battOffset;
            case 6:
                return R.drawable.ntficon_wifi_green_6_batt_000 + battOffset;
            default:
                return R.drawable.ntficon_wifi_white_batt_000 + battOffset;
            }

            // WMシリーズ
        default:
            switch (antennaLevel) {
            case 1:
                return R.drawable.ntficon_wimax_green_1_batt_000 + battOffset;
            case 2:
                return R.drawable.ntficon_wimax_green_2_batt_000 + battOffset;
            case 3:
                return R.drawable.ntficon_wimax_green_3_batt_000 + battOffset;
            case 4:
                return R.drawable.ntficon_wimax_green_4_batt_000 + battOffset;
            case 5:
                return R.drawable.ntficon_wimax_green_5_batt_000 + battOffset;
            case 6:
                return R.drawable.ntficon_wimax_green_6_batt_000 + battOffset;
            default:
                return R.drawable.ntficon_wimax_white_batt_000 + battOffset;
            }
        }
    }

    public static int selectWdIcon(int antennaLevel, int batteryLevel, NETWORK_STATE netState, COM_TYPE comState) {

        int battOffset = battOffset(batteryLevel);

        if (netState == NETWORK_STATE.AP_NOT_FOUND || netState == NETWORK_STATE.NOT_WM_ROUTER) {
            return R.drawable.icon_wimax_gray_batt_000 + battOffset;
        }
        else if (netState == NETWORK_STATE.OFFLINE) {
            antennaLevel = 0;
        }

        switch (comState) {
        case HIGH_SPEED:
            switch (antennaLevel) {
            case 1:
                return R.drawable.icon_hs_wimax_green_1_batt_000 + battOffset;
            case 2:
                return R.drawable.icon_hs_wimax_green_2_batt_000 + battOffset;
            case 3:
                return R.drawable.icon_hs_wimax_green_4_batt_000 + battOffset;
            case 4:
            case 5:
            case 6:
                return R.drawable.icon_hs_wimax_green_6_batt_000 + battOffset;
            default:
                return R.drawable.icon_hs_wimax_white_batt_000 + battOffset;
            }

        case NO_LIMIT:
            switch (antennaLevel) {
            case 1:
                return R.drawable.icon_wimax_green_1_batt_000 + battOffset;
            case 2:
                return R.drawable.icon_wimax_green_2_batt_000 + battOffset;
            case 3:
                return R.drawable.icon_wimax_green_4_batt_000 + battOffset;
            case 4:
                return R.drawable.icon_wimax_green_5_batt_000 + battOffset;
            case 5:
            case 6:
                return R.drawable.icon_wimax_green_6_batt_000 + battOffset;
            default:
                return R.drawable.icon_wimax_white_batt_000 + battOffset;
            }

        case WIFI_SPOT:
            switch (antennaLevel) {
            case 1:
                return R.drawable.icon_wifi_green_1_batt_000 + battOffset;
            case 2:
                return R.drawable.icon_wifi_green_2_batt_000 + battOffset;
            case 3:
                return R.drawable.icon_wifi_green_3_batt_000 + battOffset;
            case 4:
                return R.drawable.icon_wifi_green_4_batt_000 + battOffset;
            case 5:
                return R.drawable.icon_wifi_green_5_batt_000 + battOffset;
            case 6:
                return R.drawable.icon_wifi_green_6_batt_000 + battOffset;
            default:
                return R.drawable.icon_wifi_white_batt_000 + battOffset;
            }
            // WMシリーズ
        default:
            switch (antennaLevel) {
            case 1:
                return R.drawable.icon_wimax_green_1_batt_000 + battOffset;
            case 2:
                return R.drawable.icon_wimax_green_2_batt_000 + battOffset;
            case 3:
                return R.drawable.icon_wimax_green_3_batt_000 + battOffset;
            case 4:
                return R.drawable.icon_wimax_green_4_batt_000 + battOffset;
            case 5:
                return R.drawable.icon_wimax_green_5_batt_000 + battOffset;
            case 6:
                return R.drawable.icon_wimax_green_6_batt_000 + battOffset;
            default:
                return R.drawable.icon_wimax_white_batt_000 + battOffset;
            }
        }
    }

    private static int battOffset(int batteryLevel) {
        if (batteryLevel >= 95) {
            return 10;
        }
        else if (batteryLevel >= 90) {
            return 9;
        }
        else if (batteryLevel >= 80) {
            return 8;
        }
        else if (batteryLevel >= 70) {
            return 7;
        }
        else if (batteryLevel >= 60) {
            return 6;
        }
        else if (batteryLevel >= 50) {
            return 5;
        }
        else if (batteryLevel >= 40) {
            return 4;
        }
        else if (batteryLevel >= 30) {
            return 3;
        }
        else if (batteryLevel >= 20) {
            return 2;
        }
        else if (batteryLevel >= 10) {
            return 1;
        }
        else if (batteryLevel >= 0) {
            return 0;
        }
        else {
            return 11;
        }
    }
}
