package com.appspot.afnf4199ga.wmgraph.app;

import android.os.Handler;
import android.widget.Toast;

import com.appspot.afnf4199ga.twawm.router.RouterInfo;
import com.appspot.afnf4199ga.utils.Logger;

public class UIAct extends Handler {
	private static UIAct instance = new UIAct();
	private static MainActivity parent;
	private static long lastToast = -1;

	private UIAct() {
	}

	public static void init(MainActivity parent) {
		UIAct.parent = parent;
	}

	public static void destroy() {
		UIAct.parent = null;
	}

	static class ToastRunnable implements Runnable {
		private String msg;

		public ToastRunnable(String msg) {
			this.msg = msg;
		}

		@Override
		public void run() {
			try {
				long now = System.currentTimeMillis();
				if (now - lastToast > 6000) {
					lastToast = now;
					Toast.makeText(parent, msg, Toast.LENGTH_LONG).show();
				}
			}
			catch (Throwable e) {
				Logger.w("toast error", e);
			}
		}
	}

	public static void resetLastToast() {
		lastToast = -1;
	}

	public static void toast(String msg) {
		if (parent != null) {
			instance.post(new ToastRunnable(msg));
		}
	}

	static class GraphRunnable implements Runnable {
		private RouterInfo routerInfo;

		public GraphRunnable(RouterInfo routerInfo) {
			this.routerInfo = routerInfo;
		}

		@Override
		public void run() {
			parent.repaint(routerInfo);
		}
	}

	public static void postRepaint(RouterInfo routerInfo) {
		instance.post(new GraphRunnable(routerInfo));
	}
}
