package com.appspot.afnf4199ga.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

public class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private static final String LABEL = "##MyUncaughtExceptionHandler";

	public static void init(Context context) {

		// 設定済でなければ
		UncaughtExceptionHandler h = Thread.getDefaultUncaughtExceptionHandler();
		if (h == null || MyStringUtlis.eqauls(LABEL, h.toString()) == false) {

			// UncaughtExceptionHandler設定
			Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());

			// 初回ロギング
			{
				Logger.i("device:" + Build.DEVICE);
				Logger.i("model:" + Build.MODEL);
				Logger.i("release:" + Build.VERSION.RELEASE);
				Logger.i("sdk:" + Build.VERSION.SDK_INT);
				try {
					PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
					Logger.i("appver:" + packageInfo.versionName + "(" + packageInfo.versionCode + ")");
				}
				catch (Throwable e) {
					Logger.w("getPackageInfo failed", e);
				}
			}

			// ログ削除
			Logger.startDeleteOldFileThread();
		}
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Logger.e("uncaughtException", ex);
		Logger.startFlushThread(true);
	}

	@Override
	public String toString() {
		return LABEL;
	}
}
