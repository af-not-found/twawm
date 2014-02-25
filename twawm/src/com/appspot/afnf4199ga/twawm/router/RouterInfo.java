package com.appspot.afnf4199ga.twawm.router;

public class RouterInfo {

	public boolean rmtMain = true;
	public String routerName;
	public int battery;
	public boolean charging = false;
	public int antennaLevel;
	public String rssiText;
	public String cinrText;
	public String bluetoothAddress;
	public boolean notInitialized = false;
	public boolean hasStandbyButton = true;

	public String getBatteryText() {
		if (battery < 0) {
			return "N/A";
		}
		else {
			return battery + (charging ? "+" : "%");
		}
	}
}
