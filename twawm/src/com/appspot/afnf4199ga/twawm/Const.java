package com.appspot.afnf4199ga.twawm;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import net.afnf.and.twawm2.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;

import com.appspot.afnf4199ga.twawm.TwawmUtils.BT_RESUME_TYPE;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class Const {

    public static final String LOGTAG = "twawm";
    public static final String LOGDIR = "twawm";

    public static final String INTENT_PREFIX = Const.class.getPackage().getName() + ".INTENT";
    public static final String INTENT_WD_CLICKED = INTENT_PREFIX + ".WD_CLICKED";
    public static final String INTENT_WD_UPDATE = INTENT_PREFIX + ".WD_UPDATED";
    public static final String INTENT_WD_CHANGE_STYLE = INTENT_PREFIX + ".WD_CHANGE_STYLE";
    public static final String INTENT_DELETED = INTENT_PREFIX + ".WD_DELETED";
    public static final String INTENT_APP_CLOSED = INTENT_PREFIX + ".APP_CLOSED";
    public static final String INTENT_ROUTER_INFO_FETCHED = INTENT_PREFIX + ".ROUTER_INFO_FETCHED";
    public static final String INTENT_EX_ACTION_SELECT = INTENT_PREFIX + ".EX_ACTION_SELECT";
    public static final String INTENT_EX_BT_ENABLING = INTENT_PREFIX + ".EX_BT_ENABLING";
    public static final String INTENT_EX_TOGGLE_WIFI = INTENT_PREFIX + ".EX_TOGGLE_WIFI";
    public static final String INTENT_EX_TOGGLE_ROUTER = INTENT_PREFIX + ".EX_TOGGLE_ROUTER";
    public static final String INTENT_EX_DO_ACTION = INTENT_PREFIX + ".EX_DO_ACTION";
    public static final String INTENT_EX_INIT_WIZARD = INTENT_PREFIX + ".PASS_NOT_INITIALIZED";
    public static final String INTENT_TASKER = "net.afnf.and.twawm2.TaskerIntent";

    public static final long ONLINE_CHECK_DELAY_AFTER_CTRL = 7000;
    public static final int WATCH_DOG_TIMEOUT_MS = 15000;
    public static final long WD_TEXT_LOCK_TIME_MS = 10000;
    public static final long ROUTER_SWITCH_LOCK_TIME_MS = 60000;
    public static final long WIFI_SCAN_DELAY_AFTER_RESUME = 10000;
    public static final long NOTIFY_DELAY_AFTER_BT_DISABLING = 2000;
    public static final long SERVICE_STOP_DELAY_MS = 5000;

    public static final String ROUTER_HOSTNAME = "aterm.me";
    public static final String ROUTER_IPADDR_DEFAULT = "192.168.179.1";
    public static final int ROUTER_PORT = 80;
    public static final int ROUTER_HTTP_TIMEOUT = 12000;
    public static final int ROUTER_SESSION_TIMEOUT = 260000;
    public static final String ROUTER_URL_INFO_RMTMAIN = "/index.cgi/info_remote_main";
    public static final String ROUTER_URL_INFO_INFOBTN = "/index.cgi/info_btn";
    public static final String ROUTER_URL_INFO_IDXCT = "/index.cgi/index_contents_pass";
    public static final String ROUTER_URL_INFO_STS_XML = "/index.cgi/status_get.xml";
    public static final String ROUTER_URL_STANDBY_RMTMAIN = "/index.cgi/info_remote_main_standby";
    public static final String ROUTER_URL_STANDBY_INFOBTN = "/index.cgi/info_btn_standby";
    public static final String ROUTER_URL_STANDBY_BT_RMTMAIN = "/index.cgi/info_remote_main_btstandby";
    public static final String ROUTER_URL_STANDBY_BT_INFOBTN = "/index.cgi/info_btn_btstandby";
    public static final String ROUTER_URL_WIMAX_DISCN_INFOBTN = "/index.cgi/info_btn_wimax";
    public static final String ROUTER_URL_WIMAX_CONN_GETINFO = "/index.cgi/index_contents_local";
    public static final String ROUTER_URL_WIMAX_CONN_INFOBTN = "/index.cgi/index_contents_local";
    public static final String ROUTER_URL_REBOOT_WM_RMTMAIN = "/index.cgi/info_remote_reboot";
    public static final String ROUTER_URL_REBOOT_WM_INFOBTN = "/index.cgi/info_btn_reboot";
    public static final String ROUTER_URL_ECO_MAIN = "/index.cgi/eco_mode_main";
    public static final String ROUTER_URL_ECO_POST = "/index.cgi/eco_mode_main_set";
    public static final String ROUTER_URL_NAD_LOCAL_SET = "/index.cgi/index_contents_local_set";

    public static final String ROUTER_PAGE_CHARSET_WM = "euc-jp";
    public static final String ROUTER_PAGE_CHARSET_NAD = "UTF-8";
    public static final String ROUTER_PAGE_SESSIONID_NAME = "SESSION_ID";
    public static final String ROUTER_BASIC_AUTH_USERNAME = "smart-user";
    public static final String ROUTER_BASIC_AUTH_PASSWORD = "smart-user";
    public static final String ROUTER_BASIC_AUTH_USERNAME2 = "admin";

    public static final UUID BLUETOOTH_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int NOTIF_ID_MAIN = 1;
    public static final int NOTIF_ID_EX = 2;
    public static final int REQUEST_ENABLE_BT = 1;

    public static final String LOG_SEND_SERVER = "https://afnf4199ga.appspot.com/andreport/upload";
    public static final String URL_WIKI_LOGSEND_WHAT = "http://w.livedoor.jp/twawm/lite/d/%a5%ed%a5%b0%a4%ce%c1%f7%bf%ae%a4%cb%a4%c4%a4%a4%a4%c6";
    public static final String URL_WIKI_LOGSEND_REPLY = "http://w.livedoor.jp/twawm/bbs/16128/l50";
    public static final String URL_WIKI_NOT_WORKS = "http://w.livedoor.jp/twawm/lite/d/FAQ";

    /**
     * 初期化
     * 
     * @param context Context
     * @return 初回起動であればtrueを返す
     */
    public static boolean init(Context context) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean firstBoot = true;
        Map<String, ?> all = pref.getAll();
        if (all != null && all.size() >= 1) {
            firstBoot = false;
        }

        // デフォルト値を設定するために予め呼んでおく
        getPrefBtResumeType(context);
        getPrefActionAfterSuspend(context);

        return firstBoot;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // ステータスチェック設定
    ///////////////////////////////////////////////////////////////////////////////////////

    /** ステータスチェック間隔 */
    public static int getPrefOnlineCheckIntervalMs(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return AndroidUtils.getPrefInt(pref, "menu_online_check_interval_sec",
                context.getString(R.string.dv_menu_online_check_interval_sec)) * 1000;
    }

    /** DNSタイムアウト */
    public static int getPrefOnlineCheckDnsTimeoutMs(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return AndroidUtils.getPrefInt(pref, "menu_online_check_dns_timeout_sec",
                context.getString(R.string.dv_menu_online_check_dns_timeout_sec)) * 1000;
    }

    /** オフライン後チェック間隔 */
    public static int getPrefOnlineCheckCountAfterOffline(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return AndroidUtils.getPrefInt(pref, "menu_online_check_count_after_offline",
                context.getString(R.string.dv_menu_online_check_count_after_offline));
    }

    /** オフライン後チェック回数 */
    public static int getPrefOnlineCheckIntervalMsAfterOffline(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return AndroidUtils.getPrefInt(pref, "menu_online_check_interval_sec_after_offline",
                context.getString(R.string.dv_menu_online_check_interval_sec_after_offline)) * 1000;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // ウィジェット設定
    ///////////////////////////////////////////////////////////////////////////////////////

    /** ウィジェット文字色 */
    public static String getPrefWidgetStrColor(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(context.getString(R.string.menu_key_widget_str_color),
                context.getString(R.string.dv_menu_widget_str_color));
    }

    /** ウィジェット背景色 */
    public static String getPrefWidgetBackground(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(context.getString(R.string.menu_key_widget_background),
                context.getString(R.string.dv_menu_widget_background));
    }

    /** WiFi無効時タップ */
    public static String getPrefWidgetClickActionWifiDisabled(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(context.getString(R.string.menu_key_widget_click_action_wifi_disabled),
                context.getString(R.string.dv_menu_widget_click_action_wifi_disabled));
    }

    /** オンライン時タップ */
    public static String getPrefWidgetClickActionOnline(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(context.getString(R.string.menu_key_widget_click_action_online),
                context.getString(R.string.dv_menu_widget_click_action_online));
    }

    /** その他タップ */
    public static String getPrefWidgetClickActionOffline(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(context.getString(R.string.menu_key_widget_click_action_offline),
                context.getString(R.string.dv_menu_widget_click_action_offline));
    }

    /** ウィジェット一覧カスタマイズ */
    public static String getPrefWidgetClickActionCustomizedData(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("menu_widget_click_action_customized_data", "");
    }

    /** ウィジェット一覧カスタマイズ更新 */
    public static void updatePrefWidgetClickActionCustomizedData(Context context, String data) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();
        edit.putString("menu_widget_click_action_customized_data", data);
        edit.commit();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // ルーター設定
    ///////////////////////////////////////////////////////////////////////////////////////

    /** ルーターIPアドレス */
    public static String getPrefApIpAddr(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(context.getString(R.string.menu_key_ap_ip_addr), "");
    }

    /** ルーターIPアドレス更新 */
    public static void updatePrefApIpAddr(Context context, String apIpAddr) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();
        edit.putString(context.getString(R.string.menu_key_ap_ip_addr), apIpAddr);
        edit.commit();
    }

    /** 接続タイムアウト */
    public static int getPrefApConnTimeoutMs(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return AndroidUtils.getPrefInt(pref, "menu_ap_conn_timeout_sec", context.getString(R.string.dv_menu_ap_conn_timeout_sec)) * 1000;
    }

    /** 再接続回数 */
    public static int getPrefApConnRetryLimit(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return AndroidUtils.getPrefInt(pref, "menu_ap_conn_retry_limit", context.getString(R.string.dv_menu_ap_conn_retry_limit));
    }

    /** 接続失敗時にWiFi無効 */
    public static boolean getPrefDisableWifiWhenApConnFailed(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("menu_disable_wifi_when_ap_conn_failed",
                Boolean.parseBoolean(context.getString(R.string.dv_menu_disable_wifi_when_ap_conn_failed)));
    }

    /** ウィザードを自動起動する */
    public static boolean getPrefStartWizardAutomatically(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("menu_start_wizard_automatically",
                Boolean.parseBoolean(context.getString(R.string.dv_menu_start_wizard_automatically)));
    }

    /** ウィザードを自動起動する 更新 */
    public static void updatePrefStartWizardAutomatically(Context context, boolean enable) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();
        edit.putBoolean("menu_start_wizard_automatically", enable);
        edit.commit();
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // サービス設定
    ///////////////////////////////////////////////////////////////////////////////////////

    /** スタンバイ後の挙動 */
    public static String getPrefActionAfterSuspend(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.menu_key_action_after_suspend);
        String value = AndroidUtils.getPrefString(pref, key);

        // 値がない場合
        if (value == null) {
            boolean suspend = true;
            try {
                suspend = pref.getBoolean("menu_disable_wifi_after_suspend", true);
            }
            catch (Throwable e) {
                Logger.w("error on pref.getBoolean(menu_disable_wifi_after_suspend)", e);
            }
            int valueKey = suspend ? R.string.menu_action_after_suspend__wifioff_stop_service
                    : R.string.menu_action_after_suspend__stop_service;
            value = context.getString(valueKey);
            Editor edit = pref.edit();
            edit.putString(key, value);
            edit.commit();
        }

        return value;
    }

    /** スタンバイ後の挙動がWiFiOFFかどうか */
    public static boolean isPrefActionAfterSuspendWifiOFF(Context context) {
        String actionAfterSuspend = Const.getPrefActionAfterSuspend(context);
        String WIFIOFF = context.getString(R.string.menu_action_after_suspend__wifioff_stop_service);
        return MyStringUtlis.eqauls(actionAfterSuspend, WIFIOFF);
    }

    /** スタンバイ後の挙動がWiFiOFFかどうか */
    public static boolean isPrefActionAfterSuspendStopService(Context context) {
        String actionAfterSuspend = Const.getPrefActionAfterSuspend(context);
        String STOP = context.getString(R.string.menu_action_after_suspend__stop_service);
        return MyStringUtlis.eqauls(actionAfterSuspend, STOP);
    }

    /** ステータスバー通知 */
    public static String getPrefStatusBarNotify(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("menu_statusbar_notify", context.getString(R.string.dv_menu_statusbar_notify));
    }

    /** ステータスバー通知 常に通知 */
    public static boolean isStatusBarNotifyAlways(Context context) {
        return MyStringUtlis.eqauls(context.getString(R.string.menu_statusbar_notify__always),
                Const.getPrefStatusBarNotify(context));
    }

    /** ステータスバー通知 通知しない */
    public static boolean isStatusBarNotifyNever(Context context) {
        return MyStringUtlis.eqauls(context.getString(R.string.menu_statusbar_notify__never),
                Const.getPrefStatusBarNotify(context));
    }

    /** ステータスバー通知 有効 */
    public static boolean getPrefStartServiceWhenWifiEnabled(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("menu_start_service_when_wifi_enabled",
                Boolean.parseBoolean(context.getString(R.string.dv_menu_start_service_when_wifi_enabled)));
    }

    /** 対応外ルーターに接続した場合の挙動が、サービス停止系かどうか */
    public static boolean getPrefNonTargetRouterActionStopService(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String val = pref
                .getString("menu_non_target_router_action", context.getString(R.string.dv_menu_non_target_router_action));
        String s1 = context.getString(R.string.menu_non_target_router_action__notify_stop);
        String s2 = context.getString(R.string.menu_non_target_router_action__stop);
        return MyStringUtlis.eqauls(val, s1) || MyStringUtlis.eqauls(val, s2);
    }

    /** ログ出力を有効にする */
    public static boolean isPrefLoggingEnabled(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("menu_enable_logging", Boolean.parseBoolean(context.getString(R.string.dv_menu_enable_logging)));
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // リモート起動設定
    ///////////////////////////////////////////////////////////////////////////////////////

    /** リモート起動方法 */
    public static BT_RESUME_TYPE getPrefBtResumeType(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        // 値がある場合
        String value = AndroidUtils.getPrefString(pref, "menu_bt_resume_type");
        if (value != null) {
            int v = Integer.parseInt(value);
            return BT_RESUME_TYPE.ordinalOf(v);
        }

        // 201Fのみ、デフォルトで「強制接続」にする
        value = (String) AndroidUtils.updatePrefValueByModel(pref, "menu_bt_resume_type", Build.MODEL, "#201F#",
                context.getString(R.string.menu_bt_resume_type__normal_force),
                context.getString(R.string.menu_bt_resume_type__fast));

        int v = Integer.parseInt(value);
        return BT_RESUME_TYPE.ordinalOf(v);
    }

    public static boolean isPrefBtRestartType(Context context) {
        BT_RESUME_TYPE type = getPrefBtResumeType(context);
        return type == BT_RESUME_TYPE.BT_RESTART;
    }

    public static boolean isPrefBtDiscoveringType(Context context) {
        BT_RESUME_TYPE type = getPrefBtResumeType(context);
        return type != BT_RESUME_TYPE.FAST;
    }

    /** BT接続タイムアウト */
    public static int getPrefBtConnectionTimeoutMs(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return AndroidUtils.getPrefInt(pref, "menu_bt_connection_timeout_sec",
                context.getString(R.string.dv_menu_bt_connection_timeout_sec)) * 1000;
    }

    /** Bluetoothアドレス */
    public static String getPrefBluetoothAddress(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString("menu_bt_address", "").toUpperCase(Locale.US);
    }

    /** Bluetoothアドレス更新 */
    public static void updatePrefBluetoothAddress(Context context, String bluetoothAddress) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();
        edit.putString("menu_bt_address", bluetoothAddress);
        edit.commit();
    }

    /** リモート起動後に他AP無効化 */
    public static boolean getPrefDisableOtherApAfterResume(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("menu_disable_other_ap_after_resume",
                Boolean.parseBoolean(context.getString(R.string.dv_menu_disable_other_ap_after_resume)));
    }

    /** リモート起動後のウェイト */
    public static int getPrefWaitAfterResumeMs(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return AndroidUtils.getPrefInt(pref, "menu_wait_after_resume", context.getString(R.string.dv_menu_wait_after_resume)) * 1000;
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // 設定画面にない保存値
    ///////////////////////////////////////////////////////////////////////////////////////

    /** 動作中 */
    public static boolean getPrefWorking(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean("menu_working", true);
    }

    /** 動作中更新 */
    public static void updatePrefWorking(Context context, boolean working) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();
        edit.putBoolean("menu_working", working);
        edit.commit();
    }

    /** 最後に接続した対象ルータのSSID */
    public static String getPrefLastTargetSSID(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return AndroidUtils.getPrefString(pref, "menu_lastTargetSSID");
    }

    /** 最後に接続した対象ルータ情報を更新 */
    public static void updatePrefLastTargetRouterInfo(Context context, String ssid) {

        // JBからクオートされるようになった模様
        ssid = MyStringUtlis.trimQuote(ssid);

        boolean ne1 = MyStringUtlis.isEmpty(ssid) == false;
        boolean ch1 = ne1 && MyStringUtlis.eqauls(ssid, getPrefLastTargetSSID(context)) == false;

        if (ch1) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            Editor edit = pref.edit();
            if (ne1) {
                edit.putString("menu_lastTargetSSID", ssid);
            }
            edit.commit();
        }
    }

    /** ルーターパスワード */
    public static String getPrefRouterControlPassword(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(context.getString(R.string.menu_key_router_control_password), "");
    }

    /** ルーターパスワード更新 */
    public static void updatePrefRouterControlPassword(Context context, String routerControlPassword) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();
        edit.putString(context.getString(R.string.menu_key_router_control_password), routerControlPassword);
        edit.commit();
    }

    /** 放電速度 */
    public static List<Double> getPrefBattRates(Context context) {
        List<Double> pastRates = new ArrayList<Double>();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String rawstr = pref.getString("menu_batt_rates", "");
        Logger.v("getPrefBattRates : " + rawstr);

        if (MyStringUtlis.isEmpty(rawstr) == false) {
            try {
                String[] values = rawstr.split(",");
                for (String v : values) {
                    if (MyStringUtlis.isEmpty(v) == false) {
                        double d = Integer.parseInt(v) / 1000.0;
                        pastRates.add(d);
                    }
                }
            }
            catch (Throwable e) {
                pastRates.clear();
            }
        }
        return pastRates;
    }

    /** 放電速度更新 */
    public static void updatePrefBattRates(Context context, List<Double> pastRates) {
        StringBuilder sb = new StringBuilder();
        if (pastRates != null) {
            for (Double d : pastRates) {
                sb.append((int) (d * 1000));
                sb.append(",");
            }
        }
        String rawstr = sb.toString();
        Logger.v("updatePrefBattRates : " + rawstr);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();
        edit.putString("menu_batt_rates", rawstr);
        edit.commit();
    }
}
