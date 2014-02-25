package com.appspot.afnf4199ga.utils;

import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;

import com.appspot.afnf4199ga.twawm.Const;

public class AndroidUtils {

	private AndroidUtils() {

	}

	public static String getAction(Intent intent) {
		if (intent != null) {
			return intent.getAction();
		}
		return null;
	}

	public static String getActionForLog(Intent intent) {
		String action = getAction(intent);
		if (action != null) {
			if (action.startsWith(Const.INTENT_PREFIX)) {
				return action.substring(Const.INTENT_PREFIX.length());
			}
			else {
				return action;
			}
		}
		return null;
	}

	public static boolean isActionEquals(Intent intent, String expected) {
		if (expected == null) {
			return false;
		}
		else {
			String action = getAction(intent);
			return action != null && action.equals(expected);
		}
	}

	public static boolean isUIThread(Context context) {
		return Thread.currentThread().equals(context.getMainLooper().getThread());
	}

	public static int indexOf(String[] datas, String val) {
		if (val != null) {
			for (int i = 0; i < datas.length; i++) {
				if (datas[i].equals(val)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static boolean isWifiDisabled(WifiManager wifi) {
		return wifi.getWifiState() == WifiManager.WIFI_STATE_DISABLED;
	}

	public static boolean isWifiEnabled(WifiManager wifi) {
		return wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
	}

	public static boolean isWifiEnabledOrEnabling(WifiManager wifi) {
		int wifiState = wifi.getWifiState();
		return wifiState == WifiManager.WIFI_STATE_ENABLED || wifiState == WifiManager.WIFI_STATE_ENABLING;
	}

	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException e) {
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 設定系
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int getIntFromEditPref(SharedPreferences pref, String key, String default_value) {
		String value = pref.getString(key, default_value);
		if (MyStringUtlis.isEmpty(value)) {
			value = default_value;
		}
		return Integer.parseInt(value);
	}

	public static String getPrefString(SharedPreferences pref, String key) {
		Object value = getPrefObject(pref, key);
		if (value instanceof String) {
			String s = (String) value;
			if (MyStringUtlis.isEmpty(s) == false) {
				return s;
			}
		}
		return null;
	}

	public static Boolean getPrefBoolean(SharedPreferences pref, String key) {
		Object value = getPrefObject(pref, key);
		return value != null ? (Boolean) value : null;
	}

	protected static Object getPrefObject(SharedPreferences pref, String key) {

		// 値がある場合
		Map<String, ?> all = pref.getAll();
		if (all != null) {
			Object obj = all.get(key);
			return obj;
		}

		return null;
	}

	public static Object updatePrefValueByModel(SharedPreferences pref, String key, String thisModel, String targetModel,
			Object targetModelValue, Object notTargetModelValue) {

		// 値がない場合は保存
		Object value;
		String mdl = "#" + thisModel.toUpperCase(Locale.US) + "#";
		if (targetModel != null && targetModel.indexOf(mdl) != -1) {
			value = targetModelValue;
		}
		else {
			value = notTargetModelValue;
		}

		if (value != null) {
			Editor edit = pref.edit();
			if (value instanceof String) {
				edit.putString(key, (String) value);
			}
			else if (value instanceof Boolean) {
				edit.putBoolean(key, (Boolean) value);
			}
			edit.commit();
		}

		return value;
	}
}
