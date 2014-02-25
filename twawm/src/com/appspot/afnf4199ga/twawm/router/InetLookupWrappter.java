package com.appspot.afnf4199ga.twawm.router;

import java.net.InetAddress;

public class InetLookupWrappter {
	public InetAddress getByName(String hostname) {
		try {
			//Logger.i("InetLookupProxy.getByName " + hostname);
			return InetAddress.getByName(hostname);
		}
		catch (Throwable e) {
			return null;
		}
	}
}
