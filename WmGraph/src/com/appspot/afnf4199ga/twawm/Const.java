package com.appspot.afnf4199ga.twawm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.appspot.afnf4199ga.wmgraph.R;

public class Const {

	public static final String LOGTAG = "wmgraph";
	public static final String LOGDIR = "wmgraph";

	public static final String INTENT_PREFIX = Const.class.getPackage().getName() + ".INTENT";
	public static final String INTENT_WD_CLICKED = INTENT_PREFIX + ".WD_CLICKED";
	public static final String INTENT_UPDATED = INTENT_PREFIX + ".UPDATED";
	public static final String INTENT_DELETED = INTENT_PREFIX + ".WD_DELETED";
	public static final String INTENT_APP_CLOSED = INTENT_PREFIX + ".APP_CLOSED";
	public static final String INTENT_ROUTER_INFO_FETCHED = INTENT_PREFIX + ".ROUTER_INFO_FETCHED";
	public static final String INTENT_EX_ACTION_SELECT = INTENT_PREFIX + ".EX_ACTION_SELECT";
	public static final String INTENT_EX_BT_ENABLING = INTENT_PREFIX + ".EX_BT_ENABLING";
	public static final String INTENT_EX_TOGGLE_WIFI = INTENT_PREFIX + ".EX_TOGGLE_WIFI";
	public static final String INTENT_EX_TOGGLE_ROUTER = INTENT_PREFIX + ".EX_TOGGLE_ROUTER";
	public static final String INTENT_EX_DO_ACTION = INTENT_PREFIX + ".EX_DO_ACTION";

	public static final int ONLINE_CHECK_SHORT_INTERVAL_COUNT_LIMIT = 10;
	public static final int ONLINE_CHECK_SHORT_INTERVAL_MS = 6000;

	public static final String ROUTER_HOSTNAME = "aterm.me";
	public static final int ROUTER_PORT = 80;
	public static final int ROUTER_HTTP_TIMEOUT = 15000;
	public static final int ROUTER_SESSION_TIMEOUT = 260000;
	public static final String ROUTER_URL_INFO = "/index.cgi/info_remote_main";
	public static final String ROUTER_URL_STANDBY = "/index.cgi/info_remote_main_standby";
	public static final String ROUTER_PAGE_CHARSET = "euc-jp";
	public static final String ROUTER_BASIC_AUTH_USERNAME = "smart-user";
	public static final String ROUTER_BASIC_AUTH_PASSWORD = "smart-user";

	public static final String BLUETOOTH_UUID = "00001101-0000-1000-8000-00805F9B34FB";

	public static final int NOTIF_ID_MAIN = 1;
	public static final int NOTIF_ID_EX = 2;

	public static final int REQUEST_ENABLE_BT = 1;

	public static final int DNS_CHECK_DELAY_MS = 300;
	public static final int WATCH_DOG_TIMEOUT_MS = 15000;
	public static final long WD_TEXT_LOCK_TIME_MS = 10000;

	public static final String LOG_SEND_SERVER = "http://afnf4199ga.appspot.com/andreport/upload";
	public static final String URL_WIKI_LOGSEND_WHAT = "http://w.livedoor.jp/twawm/lite/d/%a5%ed%a5%b0%a4%ce%c1%f7%bf%ae%a4%cb%a4%c4%a4%a4%a4%c6";
	public static final String URL_WIKI_NOT_WORKS = "http://w.livedoor.jp/twawm/lite/d/FAQ";

	public static int getPrefWatchDogTimeoutMs(Context context) {
		return WATCH_DOG_TIMEOUT_MS;
	}

	// AP接続設定
	public static String getPrefApIpAddr(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(context.getString(R.string.menu_key_ap_ip_addr), "");
	}

}
