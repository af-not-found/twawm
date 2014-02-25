package com.appspot.afnf4199ga.wmgraph.app;

import com.appspot.afnf4199ga.twawm.router.InetLookupWrappter;
import com.appspot.afnf4199ga.utils.Logger;

public class InetLookupThread extends Thread {

	private InetLookupWrappter inetLookupWrappter = new InetLookupWrappter();
	public boolean working = true;

	@Override
	public void run() {
		Logger.i("InetLookupThread started");

		// 寝る
		try {
			while (working) {

				// ウェイト
				long tmp = Math.max(3000, MainActivity.interval);
				Thread.sleep(tmp);

				// 実行中なら
				if (working) {

					// DNS lookup
					String host = "www." + (10 + (int) (Math.random() * 10000)) + ".com";
					boolean success = inetLookupWrappter.getByName(host) != null;
					Logger.i("lookuped : " + host + ", " + (success ? "success" : "failure"));
				}
			}
		}
		catch (Throwable e) {
		}

		Logger.i("InetLookupThread finished");
	}
}
