package com.appspot.afnf4199ga.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.afnf.and.twawm2.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.appspot.afnf4199ga.twawm.Const;

public class AndroidUtils {

    public static final File CONFIG_FILE;
    static {
        File outdir = new File(Environment.getExternalStorageDirectory(), Const.LOGDIR);
        CONFIG_FILE = new File(outdir, "_config.bin");
    }

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
        if (val != null && datas != null) {
            for (int i = 0; i < datas.length; i++) {
                if (datas[i] != null && datas[i].equals(val)) {
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

    public static boolean sleep(long ms) {
        try {
            if (ms > 0) {
                Thread.sleep(ms);
            }
            return true;
        }
        catch (InterruptedException e) {
            return false;
        }
    }

    public static String getBuildDate(Context context) {
        ZipFile zf = null;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            return SimpleDateFormat.getDateTimeInstance().format(new java.util.Date(time));
        }
        catch (Exception e) {
            Logger.w("getBuildDate failed", e);
            return "";
        }
        finally {
            if (zf != null) {
                try {
                    zf.close();
                }
                catch (Exception e) {
                }
            }
        }
    }

    public static String getAppVer(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName + "(" + packageInfo.versionCode + ")";
        }
        catch (Throwable e) {
            Logger.w("getPackageInfo failed", e);
            return "";
        }
    }

    public static String intToIpaddr(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }

    /**
     * http://developer.android.com/guide/practices/screens_support.html#dips-pels
     */
    public static int dip2pixel(Context context, int dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 設定系
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static int getPrefInt(SharedPreferences pref, String key, int default_value) {
        Object value = getPrefObject(pref, key);
        int ret = default_value;
        if (value != null && value instanceof String) {
            String str = (String) value;
            if (MyStringUtlis.isEmpty(str) == false) {
                try {
                    ret = Integer.parseInt(str);
                }
                catch (Throwable e) {
                    Logger.w("invalid value, key=" + key + ", value=" + str);
                }
            }
        }
        return ret;
    }

    public static int getPrefInt(SharedPreferences pref, String key, String default_value) {
        int ret = getPrefInt(pref, key, Integer.MIN_VALUE);
        if (ret == Integer.MIN_VALUE) {
            ret = Integer.parseInt(default_value);
        }
        return ret;
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

    @SuppressLint("SimpleDateFormat")
    public static boolean savePreferencesToFile(Context context) {
        boolean success = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE));
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            Map<String, ?> allpref = pref.getAll();
            allpref.remove(context.getString(R.string.menu_key_router_control_password)); // ルーターパスワード削除
            output.writeObject(allpref);
            success = true;
        }
        catch (Throwable e) {
            Logger.e("savePreferencesToFile failed", e);
        }
        finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            }
            catch (Throwable e) {
            }
        }
        return success;
    }

    @SuppressWarnings({ "unchecked" })
    public static boolean loadPreferencesFromFile(Context context) {
        boolean success = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(CONFIG_FILE));
            Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
            edit.clear(); // 一旦破棄
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                if (v instanceof Boolean) {
                    edit.putBoolean(key, ((Boolean) v).booleanValue());
                }
                else if (v instanceof Float) {
                    edit.putFloat(key, ((Float) v).floatValue());
                }
                else if (v instanceof Integer) {
                    edit.putInt(key, ((Integer) v).intValue());
                }
                else if (v instanceof Long) {
                    edit.putLong(key, ((Long) v).longValue());
                }
                else if (v instanceof String) {
                    edit.putString(key, ((String) v));
                }
            }
            edit.commit();
            success = true;
        }
        catch (Throwable e) {
            Logger.e("loadPreferencesFromFile failed", e);
        }
        finally {
            try {
                if (input != null) {
                    input.close();
                }
            }
            catch (Throwable e) {
            }
        }
        return success;
    }

    public static void deleteAllPreference(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Editor edit = pref.edit();
        edit.clear();
        edit.commit();
    }
}
