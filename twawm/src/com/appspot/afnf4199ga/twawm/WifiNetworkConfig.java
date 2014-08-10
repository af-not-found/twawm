package com.appspot.afnf4199ga.twawm;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Base64;

import com.appspot.afnf4199ga.twawm.app.BackgroundService;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;

public class WifiNetworkConfig {

    protected static Map<String, String> cache = new HashMap<String, String>();

    public enum SsidFilterAction {
        START, KEEP, STOP;

        public static SsidFilterAction ordinalOf(int ordinal) {
            Iterator<SsidFilterAction> ite = EnumSet.allOf(SsidFilterAction.class).iterator();
            while (ite.hasNext()) {
                SsidFilterAction e = ite.next();
                if (e.ordinal() == ordinal)
                    return e;
            }
            return null;
        }
    }

    public static class WifiNetwork {
        public String ssid;
        public String hash;
    }

    public static SsidFilterAction getSsidFilterActionForCurrentAP(Context context, WifiManager wifi) {

        if (wifi == null) {
            wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }

        // デフォルト値
        SsidFilterAction config = SsidFilterAction.KEEP;

        // Wi-Fi有効で、且つSupplicant完了の場合
        if (AndroidUtils.isWifiEnabled(wifi) && BackgroundService.isSupplicantCompleted(wifi)) {
            WifiInfo connectionInfo = wifi.getConnectionInfo();
            if (connectionInfo != null) {
                String ssid = WifiNetworkConfig.trimSsidDquote(connectionInfo.getSSID());
                config = Const.getPrefSsidFilterConfig(context, ssid);
            }
        }

        return config;
    }

    public static String hashSsid(String ssid, MessageDigest digest) {

        String hash = cache.get(ssid);
        if (hash != null) {
            return hash;
        }

        if (digest == null) {
            try {
                digest = MessageDigest.getInstance("sha1");
            }
            catch (Exception e) {
                Logger.e("hashSsidInternal", e);
                return null;
            }
        }
        else {
            digest.reset();
        }

        try {
            byte[] bytes = digest.digest(ssid.getBytes("UTF-8"));
            hash = Base64.encodeToString(bytes, Base64.URL_SAFE).substring(0, 8);
            cache.put(ssid, hash);
            return hash;
        }
        catch (Exception e) {
            Logger.e("hashSsidInternal failed", e);
            return null;
        }
    }

    public static String trimSsidDquote(String ssid) {
        if (ssid != null) {
            int length = ssid.length();
            if (length >= 2) {
                boolean leading = ssid.startsWith("\"");
                boolean trailing = ssid.endsWith("\"");

                if (leading == false && trailing == false) {
                    return ssid;
                }
                else {
                    return ssid.substring(leading ? 1 : 0, trailing ? length - 1 : length);
                }
            }
        }
        return ssid;
    }

    public static List<WifiNetwork> getNetworks(Context context) {

        List<WifiNetwork> list = new ArrayList<WifiNetworkConfig.WifiNetwork>();

        try {

            // Wi-Fiが無効ならnullを返す
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (AndroidUtils.isWifiDisabled(wifi)) {
                return null;
            }

            MessageDigest digest = MessageDigest.getInstance("sha1");
            List<WifiConfiguration> networks = wifi.getConfiguredNetworks();
            if (networks != null) {
                for (int i = 0; i < networks.size(); i++) {
                    WifiConfiguration configuration = networks.get(i);
                    String ssid = trimSsidDquote(configuration.SSID);
                    String hash = hashSsid(ssid, digest);

                    WifiNetwork e = new WifiNetwork();
                    e.ssid = ssid;
                    e.hash = hash;

                    list.add(e);
                }
            }
        }
        catch (Exception e) {
            Logger.e("init ssid filter failed", e);
        }

        return list;
    }

}
