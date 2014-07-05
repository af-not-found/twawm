package com.appspot.afnf4199ga.twawm;

import net.afnf.and.twawm2.R;

import com.appspot.afnf4199ga.twawm.router.RouterInfo.COM_TYPE;

public class IconSelector {

    public static int selectNotifyIcon(int antennaLevel, int batteryLevel, boolean inetReachable, COM_TYPE comState) {
        switch (comState) {
        case HIGH_SPEED:
            switch (antennaLevel) {
            case 1:
                return R.drawable.ntficon_hs_wimax_green_1_batt_000 + battOffset(batteryLevel);
            case 2:
                return R.drawable.ntficon_hs_wimax_green_2_batt_000 + battOffset(batteryLevel);
            case 3:
                return R.drawable.ntficon_hs_wimax_green_3_batt_000 + battOffset(batteryLevel);
            case 4:
                return R.drawable.ntficon_hs_wimax_green_4_batt_000 + battOffset(batteryLevel);
            case 5:
                return R.drawable.ntficon_hs_wimax_green_5_batt_000 + battOffset(batteryLevel);
            case 6:
                return R.drawable.ntficon_hs_wimax_green_6_batt_000 + battOffset(batteryLevel);
            default:
                return R.drawable.ntficon_hs_wimax_white_batt_000 + battOffset(batteryLevel);
            }

        case WIFI_SPOT:
            if (inetReachable) {
                return R.drawable.ntficon_wifi_green_batt_000 + battOffset(batteryLevel);
            }
            else {
                return R.drawable.ntficon_wifi_white_batt_000 + battOffset(batteryLevel);
            }

        default:
            switch (antennaLevel) {
            case 1:
                return R.drawable.ntficon_wimax_green_1_batt_000 + battOffset(batteryLevel);
            case 2:
                return R.drawable.ntficon_wimax_green_2_batt_000 + battOffset(batteryLevel);
            case 3:
                return R.drawable.ntficon_wimax_green_3_batt_000 + battOffset(batteryLevel);
            case 4:
                return R.drawable.ntficon_wimax_green_4_batt_000 + battOffset(batteryLevel);
            case 5:
                return R.drawable.ntficon_wimax_green_5_batt_000 + battOffset(batteryLevel);
            case 6:
                return R.drawable.ntficon_wimax_green_6_batt_000 + battOffset(batteryLevel);
            default:
                return R.drawable.ntficon_wimax_white_batt_000 + battOffset(batteryLevel);
            }
        }
    }

    public static int selectWdIcon(int antennaLevel, int batteryLevel, boolean inetReachable, COM_TYPE comState) {
        switch (comState) {
        case HIGH_SPEED:
            switch (antennaLevel) {
            case 1:
                return R.drawable.icon_hs_wimax_green_1_batt_000 + battOffset(batteryLevel);
            case 2:
                return R.drawable.icon_hs_wimax_green_2_batt_000 + battOffset(batteryLevel);
            case 3:
                return R.drawable.icon_hs_wimax_green_3_batt_000 + battOffset(batteryLevel);
            case 4:
                return R.drawable.icon_hs_wimax_green_4_batt_000 + battOffset(batteryLevel);
            case 5:
                return R.drawable.icon_hs_wimax_green_5_batt_000 + battOffset(batteryLevel);
            case 6:
                return R.drawable.icon_hs_wimax_green_6_batt_000 + battOffset(batteryLevel);
            default:
                return R.drawable.icon_hs_wimax_white_batt_000 + battOffset(batteryLevel);
            }

        case WIFI_SPOT:
            if (inetReachable) {
                return R.drawable.icon_wifi_green_batt_000 + battOffset(batteryLevel);
            }
            else {
                return R.drawable.icon_wifi_white_batt_000 + battOffset(batteryLevel);
            }

        default:
            switch (antennaLevel) {
            case 1:
                return R.drawable.icon_wimax_green_1_batt_000 + battOffset(batteryLevel);
            case 2:
                return R.drawable.icon_wimax_green_2_batt_000 + battOffset(batteryLevel);
            case 3:
                return R.drawable.icon_wimax_green_3_batt_000 + battOffset(batteryLevel);
            case 4:
                return R.drawable.icon_wimax_green_4_batt_000 + battOffset(batteryLevel);
            case 5:
                return R.drawable.icon_wimax_green_5_batt_000 + battOffset(batteryLevel);
            case 6:
                return R.drawable.icon_wimax_green_6_batt_000 + battOffset(batteryLevel);
            default:
                return R.drawable.icon_wimax_white_batt_000 + battOffset(batteryLevel);
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
