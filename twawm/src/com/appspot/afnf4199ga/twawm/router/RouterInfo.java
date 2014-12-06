package com.appspot.afnf4199ga.twawm.router;

import java.util.EnumSet;
import java.util.Iterator;

import com.appspot.afnf4199ga.twawm.BluetoothHelper;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class RouterInfo {

    public static enum COM_TYPE {
        /** WMシリーズのWiMAXはNAとなる */
        NA,
        /** NAD HS */
        HIGH_SPEED,
        /** NAD NL */
        NO_LIMIT,
        /** WM/NAD Wi-Fiスポット */
        WIFI_SPOT;

        public static COM_TYPE ordinalOf(int ordinal) {
            Iterator<COM_TYPE> ite = EnumSet.allOf(COM_TYPE.class).iterator();
            while (ite.hasNext()) {
                COM_TYPE e = ite.next();
                if (e.ordinal() == ordinal)
                    return e;
            }
            return null;
        }

        @Override
        public String toString() {
            switch (this) {
            case HIGH_SPEED:
                return "HS";
            case NO_LIMIT:
                return "NL";
            case WIFI_SPOT:
                return "SPOT";
            default:
                return "NA";
            }
        }
    }

    public boolean rmtMain = true;
    public String routerName;
    public int battery = -1;
    public boolean charging = false;
    public int antennaLevel;
    public String rssiText;
    public String cinrText;
    public String bluetoothAddress;
    public boolean notInitialized = false;
    public boolean hasStandbyButton = true;
    public boolean nad = false;
    public COM_TYPE comState = COM_TYPE.NA;
    public COM_TYPE comSetting = COM_TYPE.NA;
    public Boolean wifiSpotEnabled = null;
    public String ipaddr;
    public String profile;

    public String getBatteryText() {
        if (battery < 0) {
            return "N/A";
        }
        else {
            return battery + (charging ? "+" : "%");
        }
    }

    @Override
    public String toString() {

        String btAddrForLog;
        if (MyStringUtlis.isEmpty(bluetoothAddress)) {
            btAddrForLog = "<null>";
        }
        else if (BluetoothHelper.isValidBluetoothAddress(bluetoothAddress)) {
            btAddrForLog = "<valid>";
        }
        else {
            btAddrForLog = "<invalid>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ant=");
        sb.append(antennaLevel);
        sb.append(", ");
        if (nad) {
            sb.append(comState);
            sb.append("/");
            sb.append(comSetting);
            if (wifiSpotEnabled != null && wifiSpotEnabled.booleanValue() == true) {
                sb.append("(spot)");
            }
            sb.append(", ");
        }
        sb.append(getBatteryText());
        sb.append(", bt=");
        sb.append(btAddrForLog);

        return sb.toString();
    }
}
