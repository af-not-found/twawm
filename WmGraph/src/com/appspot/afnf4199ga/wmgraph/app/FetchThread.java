package com.appspot.afnf4199ga.wmgraph.app;

import com.appspot.afnf4199ga.twawm.router.RouterControl;
import com.appspot.afnf4199ga.twawm.router.RouterInfo;
import com.appspot.afnf4199ga.utils.Logger;

public class FetchThread extends Thread {

	private long lastUpdate = -1;
	boolean working = true;

	@Override
	public void run() {
		Logger.i("FetchThread started");

		try {
			while (working) {

				// 実行中なら
				boolean success = false;
				if (working) {

					// execFetchInfo実行
					RouterInfo routerInfo = RouterControl.execFetchInfo();
					if (routerInfo != null) {
						success = true;
					}

					// 実行中ならrepaint
					if (working) {
						UIAct.postRepaint(routerInfo);
					}
					lastUpdate = System.currentTimeMillis();
				}

				// ウェイト
				long tmp = MainActivity.interval;
				if (success == false) {
					tmp = 5000;
				}
				else if (System.currentTimeMillis() - lastUpdate <= 500) {
					tmp = 500;
				}
				Thread.sleep(tmp);
			}
		}
		catch (Throwable e) {
		}

		Logger.i("FetchThread finished");
	}
}