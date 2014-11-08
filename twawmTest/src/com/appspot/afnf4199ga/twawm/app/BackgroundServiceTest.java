package com.appspot.afnf4199ga.twawm.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.afnf.and.twawm2.DexmakerInstrumentationTestCase;

import org.mockito.Mockito;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;

public class BackgroundServiceTest extends DexmakerInstrumentationTestCase {

    public void testGetSwitchTargetNetworkIdSet_01() {

        Set<Integer> networkIdSet = BackgroundService.getSwitchTargetNetworkIdSet(null, "ssid-dest999;ssid-dest2", null, null);
        assertNull(networkIdSet);
    }

    public void testGetSwitchTargetNetworkIdSet_02() {

        WifiInfo connectionInfo = Mockito.mock(WifiInfo.class);
        Mockito.when(connectionInfo.getSSID()).thenReturn(null);

        Set<Integer> networkIdSet = BackgroundService.getSwitchTargetNetworkIdSet(connectionInfo, "ssid-dest999;ssid-dest2",
                null, null);
        assertNull(networkIdSet);
    }

    public void testGetSwitchTargetNetworkIdSet_03() {

        WifiInfo connectionInfo = Mockito.mock(WifiInfo.class);
        Mockito.when(connectionInfo.getSSID()).thenReturn("ssid-dest2");

        List<ScanResult> scanResults = new ArrayList<ScanResult>();
        for (int i = 0; i < 3; i++) {
            ScanResult scanResult = Mockito.mock(ScanResult.class);
            scanResult.SSID = "ssid-dest" + i;
            scanResults.add(scanResult);
        }

        List<WifiConfiguration> configuredNetworks = new ArrayList<WifiConfiguration>();
        for (int i = 0; i < 3; i++) {
            WifiConfiguration configuredNetwork = Mockito.mock(WifiConfiguration.class);
            configuredNetwork.networkId = i;
            configuredNetwork.SSID = "\"ssid-dest" + (i + 1) + "\"";
            configuredNetworks.add(configuredNetwork);
        }

        Set<Integer> networkIdSet = BackgroundService.getSwitchTargetNetworkIdSet(connectionInfo, null, scanResults,
                configuredNetworks);
        assertNull(networkIdSet);
    }

    public void testGetSwitchTargetNetworkIdSet_04() {

        WifiInfo connectionInfo = Mockito.mock(WifiInfo.class);
        Mockito.when(connectionInfo.getSSID()).thenReturn("ssid-now");

        Set<Integer> networkIdSet = BackgroundService.getSwitchTargetNetworkIdSet(connectionInfo, "ssid-dest2", null, null);
        assertNull(networkIdSet);
    }

    public void testGetSwitchTargetNetworkIdSet_05() {

        WifiInfo connectionInfo = Mockito.mock(WifiInfo.class);
        Mockito.when(connectionInfo.getSSID()).thenReturn("ssid-now");

        List<ScanResult> scanResults = new ArrayList<ScanResult>();
        for (int i = 0; i < 3; i++) {
            ScanResult scanResult = Mockito.mock(ScanResult.class);
            scanResult.SSID = "ssid-dest" + i;
            scanResults.add(scanResult);
        }

        Set<Integer> networkIdSet = BackgroundService
                .getSwitchTargetNetworkIdSet(connectionInfo, "ssid-dest2", scanResults, null);
        assertNull(networkIdSet);
    }

    public void testGetSwitchTargetNetworkIdSet_09() {

        WifiInfo connectionInfo = Mockito.mock(WifiInfo.class);
        Mockito.when(connectionInfo.getSSID()).thenReturn("ssid-dest2");

        List<ScanResult> scanResults = new ArrayList<ScanResult>();
        for (int i = 0; i < 3; i++) {
            ScanResult scanResult = Mockito.mock(ScanResult.class);
            scanResult.SSID = "ssid-dest" + i;
            scanResults.add(scanResult);
        }

        List<WifiConfiguration> configuredNetworks = new ArrayList<WifiConfiguration>();
        for (int i = 0; i < 3; i++) {
            WifiConfiguration configuredNetwork = Mockito.mock(WifiConfiguration.class);
            configuredNetwork.networkId = i;
            configuredNetwork.SSID = "\"ssid-dest" + (i + 1) + "\"";
            configuredNetworks.add(configuredNetwork);
        }

        Set<Integer> networkIdSet = BackgroundService.getSwitchTargetNetworkIdSet(connectionInfo, "ssid-dest2", scanResults,
                configuredNetworks);
        assertNull(networkIdSet);
    }

    public void testGetSwitchTargetNetworkIdSet_11() {

        WifiInfo connectionInfo = Mockito.mock(WifiInfo.class);
        Mockito.when(connectionInfo.getSSID()).thenReturn("ssid-now");

        List<ScanResult> scanResults = new ArrayList<ScanResult>();
        for (int i = 0; i < 3; i++) {
            ScanResult scanResult = Mockito.mock(ScanResult.class);
            scanResult.SSID = "ssid-dest" + i;
            scanResults.add(scanResult);
        }

        List<WifiConfiguration> configuredNetworks = new ArrayList<WifiConfiguration>();
        for (int i = 0; i < 3; i++) {
            WifiConfiguration configuredNetwork = Mockito.mock(WifiConfiguration.class);
            configuredNetwork.networkId = i;
            configuredNetwork.SSID = "\"ssid-dest" + (i + 1) + "\"";
            configuredNetworks.add(configuredNetwork);
        }

        Set<Integer> networkIdSet = BackgroundService.getSwitchTargetNetworkIdSet(connectionInfo, "ssid-dest2", scanResults,
                configuredNetworks);
        assertNotNull(networkIdSet);
        assertEquals(1, networkIdSet.size());

        Iterator<Integer> ite = networkIdSet.iterator();
        assertEquals(Integer.valueOf(1), ite.next());
    }

    public void testGetSwitchTargetNetworkIdSet_12() {

        WifiInfo connectionInfo = Mockito.mock(WifiInfo.class);
        Mockito.when(connectionInfo.getSSID()).thenReturn("ssid-now");

        List<ScanResult> scanResults = new ArrayList<ScanResult>();
        for (int i = 0; i < 3; i++) {
            ScanResult scanResult = Mockito.mock(ScanResult.class);
            scanResult.SSID = "ssid-dest" + i;
            scanResults.add(scanResult);
        }

        List<WifiConfiguration> configuredNetworks = new ArrayList<WifiConfiguration>();
        for (int i = 0; i < 3; i++) {
            WifiConfiguration configuredNetwork = Mockito.mock(WifiConfiguration.class);
            configuredNetwork.networkId = i;
            configuredNetwork.SSID = "\"ssid-dest" + (i + 1) + "\"";
            configuredNetworks.add(configuredNetwork);
        }

        Set<Integer> networkIdSet = BackgroundService.getSwitchTargetNetworkIdSet(connectionInfo, "ssid-dest999", scanResults,
                configuredNetworks);
        assertNull(networkIdSet);
    }
}
