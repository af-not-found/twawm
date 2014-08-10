package com.appspot.afnf4199ga.twawm;

import java.security.MessageDigest;

import junit.framework.TestCase;

public class WifiNetworkConfigTest extends TestCase {

    public void testHashSsid() throws Exception {

        assertEquals("XNTCej_O", WifiNetworkConfig.hashSsid("ssid", null));

        MessageDigest digest = MessageDigest.getInstance("sha1");
        assertEquals("XNTCej_O", WifiNetworkConfig.hashSsid("ssid", digest));
        assertEquals("UEklfCwC", WifiNetworkConfig.hashSsid("ssid2", digest));
        assertEquals("XNTCej_O", WifiNetworkConfig.hashSsid("ssid", digest));

        WifiNetworkConfig.cache.clear();

        assertEquals("XNTCej_O", WifiNetworkConfig.hashSsid("ssid", digest));
        assertEquals("UEklfCwC", WifiNetworkConfig.hashSsid("ssid2", digest));
        assertEquals("XNTCej_O", WifiNetworkConfig.hashSsid("ssid", digest));
    }

    public void testTrimSsidDquote() {
        assertEquals("ssid", WifiNetworkConfig.trimSsidDquote("ssid"));
        assertEquals("ssid", WifiNetworkConfig.trimSsidDquote("\"ssid"));
        assertEquals("ssid", WifiNetworkConfig.trimSsidDquote("ssid\""));
        assertEquals("ssid", WifiNetworkConfig.trimSsidDquote("\"ssid\""));
        assertEquals("ss\"id", WifiNetworkConfig.trimSsidDquote("ss\"id"));

        assertEquals(null, WifiNetworkConfig.trimSsidDquote(null));
        assertEquals("", WifiNetworkConfig.trimSsidDquote(""));
        assertEquals("\"", WifiNetworkConfig.trimSsidDquote("\""));
        assertEquals("", WifiNetworkConfig.trimSsidDquote("\"\""));
        assertEquals("s", WifiNetworkConfig.trimSsidDquote("\"s"));
        assertEquals("s", WifiNetworkConfig.trimSsidDquote("s\""));
        assertEquals("ss", WifiNetworkConfig.trimSsidDquote("ss"));
    }
}
