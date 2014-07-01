package com.appspot.afnf4199ga.twawm.app;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.afnf.and.twawm2.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

import com.appspot.afnf4199ga.twawm.BluetoothHelper;
import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.twawm.StateMachine;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

@SuppressWarnings("deprecation")
public class MyPreferenceActivity extends PreferenceActivity {

    private static AlertDialog dialog;
    private static MyPreferenceActivity activity;

    private ListPreference prefListOnline;
    private ListPreference prefListOffline;
    private ListPreference prefListWifiOff;
    private ListPreference prefListWdStrColor;
    private ListPreference prefListWdBackground;
    private ListPreference prefListActionAfterSuspend;

    private static final String SUB = "([01]?\\d\\d?|2[0-4]\\d|25[0-4])";
    static public Pattern IP_ADDR_PATTERN = Pattern.compile("^" + SUB + "\\." + SUB + "\\." + SUB + "\\." + SUB + "$");

    static private InputFilter macFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F') || c == ':' || c == '@') {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    };

    static private InputFilter ipAddrFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (('0' <= c && c <= '9') || c == '.') {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    };

    static private OnPreferenceChangeListener btAddrPrefListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (newValue instanceof String) {
                String v = (String) newValue;
                if (MyStringUtlis.isEmpty(v) == false) {
                    if (BluetoothHelper.isValidBluetoothAddress(v) == false) {
                        UIAct.toast(preference.getContext().getString(R.string.invalid_format));
                    }
                }
            }
            return true;
        }
    };

    static private OnPreferenceChangeListener ipAddrPrefListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (newValue instanceof String) {
                String v = (String) newValue;
                if (MyStringUtlis.isEmpty(v) == false) {
                    Matcher m = IP_ADDR_PATTERN.matcher(v);
                    if (m.matches() == false) {
                        UIAct.toast(preference.getContext().getString(R.string.invalid_format));
                    }
                }
            }
            return true;
        }
    };

    static private OnPreferenceClickListener resetBattHistoryPrefListener = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Context context = preference.getContext();
            preference.setSummary("");
            StateMachine.resetBatt(context);
            UIAct.toast(preference.getContext().getString(R.string.menu_reset_batt_history_toast));
            return true;
        }
    };

    static private OnPreferenceClickListener backupPrefListener = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Context context = preference.getContext();
            dialog = new AlertDialog.Builder(context).setTitle(R.string.menu_backup_title)
                    .setMessage(context.getString(R.string.menu_backup_summary) + AndroidUtils.CONFIG_FILE.getAbsolutePath())
                    .setPositiveButton(R.string.exec, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            boolean success = AndroidUtils.savePreferencesToFile(activity);
                            int msgid = success ? R.string.backup_succeeded : R.string.backup_failed;
                            UIAct.toast(activity.getString(msgid));
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            return true;
        }
    };

    static private OnPreferenceClickListener restorePrefListener = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Context context = preference.getContext();
            dialog = new AlertDialog.Builder(context).setTitle(R.string.menu_restore_title)
                    .setMessage(context.getString(R.string.menu_restore_summary) + AndroidUtils.CONFIG_FILE.getAbsolutePath())
                    .setPositiveButton(R.string.exec, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            boolean success = AndroidUtils.loadPreferencesFromFile(activity);
                            int msgid = success ? R.string.restore_succeeded : R.string.restore_failed;
                            UIAct.toast(activity.getString(msgid));
                            if (success) {
                                activity.finish();
                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            return true;
        }
    };

    static private OnPreferenceClickListener resetSettingPrefListener = new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Context context = preference.getContext();
            dialog = new AlertDialog.Builder(context).setTitle(R.string.menu_reset_settings_title)
                    .setMessage(R.string.menu_reset_settings_summay)
                    .setPositiveButton(R.string.exec, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AndroidUtils.deleteAllPreference(activity);
                            Const.init(activity);
                            UIAct.toast(activity.getString(R.string.reset_settings_succeeded));
                            activity.finish();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
            return true;
        }
    };

    static private InputFilter maxlength18Filter = new InputFilter.LengthFilter(18);
    static private InputFilter maxlength15Filter = new InputFilter.LengthFilter(15);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
        activity = this;

        EditTextPreference btAddrPref = (EditTextPreference) findPreference(getText(R.string.menu_key_bt_address));
        EditTextPreference ipAddrPref = (EditTextPreference) findPreference(getText(R.string.menu_key_ap_ip_addr));
        EditText btAddr = btAddrPref.getEditText();
        EditText ipAddr = ipAddrPref.getEditText();
        btAddr.setFilters(new InputFilter[] { macFilter, maxlength18Filter });
        ipAddr.setFilters(new InputFilter[] { ipAddrFilter, maxlength15Filter });
        btAddrPref.setOnPreferenceChangeListener(btAddrPrefListener);
        ipAddrPref.setOnPreferenceChangeListener(ipAddrPrefListener);

        Preference resetBattHistoryPref = (Preference) findPreference(getText(R.string.menu_key_reset_batt_history));
        Preference backupPref = (Preference) findPreference(getText(R.string.menu_key_backup));
        Preference restorePref = (Preference) findPreference(getText(R.string.menu_key_restore));
        Preference resetSettingPref = (Preference) findPreference(getText(R.string.menu_key_reset_settings));
        resetBattHistoryPref.setOnPreferenceClickListener(resetBattHistoryPrefListener);
        backupPref.setOnPreferenceClickListener(backupPrefListener);
        restorePref.setOnPreferenceClickListener(restorePrefListener);
        resetSettingPref.setOnPreferenceClickListener(resetSettingPrefListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity = this;

        // タップアクションのsummaryを更新
        {
            // ListPreferenceを取得
            String keyWifiOff = getString(R.string.menu_key_widget_click_action_wifi_disabled);
            String keyOnline = getString(R.string.menu_key_widget_click_action_online);
            String keyOffline = getString(R.string.menu_key_widget_click_action_offline);
            prefListWifiOff = (ListPreference) findPreference(keyWifiOff);
            prefListOnline = (ListPreference) findPreference(keyOnline);
            prefListOffline = (ListPreference) findPreference(keyOffline);

            // 更新実行
            String[] entries = getResources().getStringArray(R.array.entries_menu_widget_click_action);
            String[] entryValues = getResources().getStringArray(R.array.entryValues_menu_widget_click_action);
            updateSummary(prefListWifiOff, entries, entryValues);
            updateSummary(prefListOnline, entries, entryValues);
            updateSummary(prefListOffline, entries, entryValues);
        }

        // ウィジェット文字色のsummaryを更新
        {
            // ListPreferenceを取得
            String key = getString(R.string.menu_key_widget_str_color);
            prefListWdStrColor = (ListPreference) findPreference(key);

            // 更新実行
            String[] entries = getResources().getStringArray(R.array.entries_menu_widget_str_color);
            String[] entryValues = getResources().getStringArray(R.array.entryValues_menu_widget_str_color);
            updateSummary(prefListWdStrColor, entries, entryValues);
        }

        // ウィジェット背景色のsummaryを更新
        {
            // ListPreferenceを取得
            String key = getString(R.string.menu_key_widget_background);
            prefListWdBackground = (ListPreference) findPreference(key);

            // 更新実行
            String[] entries = getResources().getStringArray(R.array.entries_menu_widget_background);
            String[] entryValues = getResources().getStringArray(R.array.entryValues_menu_widget_background);
            updateSummary(prefListWdBackground, entries, entryValues);
        }

        // 「スタンバイ後の挙動」のsummaryを更新
        {
            // ListPreferenceを取得
            String key = getString(R.string.menu_key_action_after_suspend);
            prefListActionAfterSuspend = (ListPreference) findPreference(key);

            // 更新実行
            String[] entries = getResources().getStringArray(R.array.entries_menu_action_after_suspend);
            String[] entryValues = getResources().getStringArray(R.array.entryValues_menu_action_after_suspend);
            updateSummary(prefListActionAfterSuspend, entries, entryValues);
        }

        // 放電履歴のsummaryを更新
        {
            Preference resetBattHistoryPref = (Preference) findPreference(getText(R.string.menu_key_reset_batt_history));

            List<Double> pastRates = Const.getPrefBattRates(this);
            int size = pastRates.size();
            double averageRate = 0;
            if (size > 0) {
                for (Double d : pastRates) {
                    averageRate += d;
                }
                averageRate /= size;
                String averageRateStr = "" + MyStringUtlis.round3(averageRate);
                resetBattHistoryPref.setSummary(getString(R.string.menu_reset_batt_history_summary, averageRateStr));
            }
            else {
                resetBattHistoryPref.setSummary("");
            }
        }

        // ログ有効化フラグのsummaryを更新
        {
            CheckBoxPreference enableLogging = (CheckBoxPreference) findPreference("menu_enable_logging");
            enableLogging.setSummary(getString(R.string.menu_enable_logging_summary)
                    + new File(Environment.getExternalStorageDirectory(), Const.LOGDIR).getAbsolutePath());
        }

        // ウィザード自動起動のチェックボックスを更新（ウィザード側から値が変更される可能性がある）
        CheckBoxPreference wizardCheckBox = (CheckBoxPreference) findPreference("menu_start_wizard_automatically");
        wizardCheckBox.setChecked(Const.getPrefStartWizardAutomatically(this));

        // リスナー登録
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(lister);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(lister);

        // ダイアログを閉じる（メモリリーク防止）
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private OnSharedPreferenceChangeListener lister = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (key != null) {

                if (MyStringUtlis.eqauls(key, "menu_enable_logging")) {
                    CheckBoxPreference enableLogging = (CheckBoxPreference) findPreference("menu_enable_logging");
                    Logger.setEnableLogging(enableLogging.isChecked());
                }
                else {

                    boolean keyTapActionOnline = MyStringUtlis.eqauls(key,
                            getString(R.string.menu_key_widget_click_action_online));
                    boolean keyTapActionOffline = MyStringUtlis.eqauls(key,
                            getString(R.string.menu_key_widget_click_action_offline));
                    boolean keyTapActionWifiOff = MyStringUtlis.eqauls(key,
                            getString(R.string.menu_key_widget_click_action_wifi_disabled));

                    boolean keyWdStrColor = MyStringUtlis.eqauls(key, getString(R.string.menu_key_widget_str_color));
                    boolean keyWdBackground = MyStringUtlis.eqauls(key, getString(R.string.menu_key_widget_background));

                    // タップアクションのsummaryを更新
                    if (keyTapActionOnline || keyTapActionOffline || keyTapActionWifiOff) {
                        String[] entries = getResources().getStringArray(R.array.entries_menu_widget_click_action);
                        String[] entryValues = getResources().getStringArray(R.array.entryValues_menu_widget_click_action);
                        if (keyTapActionOnline) {
                            updateSummary(prefListOnline, entries, entryValues);
                        }
                        else if (keyTapActionOffline) {
                            updateSummary(prefListOffline, entries, entryValues);
                        }
                        else if (keyTapActionWifiOff) {
                            updateSummary(prefListWifiOff, entries, entryValues);
                        }
                    }

                    // ウィジェット背景色・文字色のsummaryを更新
                    else if (keyWdBackground || keyWdStrColor) {
                        if (keyWdStrColor) {
                            String[] entries = getResources().getStringArray(R.array.entries_menu_widget_str_color);
                            String[] entryValues = getResources().getStringArray(R.array.entryValues_menu_widget_str_color);
                            updateSummary(prefListWdStrColor, entries, entryValues);
                        }
                        else if (keyWdBackground) {
                            String[] entries = getResources().getStringArray(R.array.entries_menu_widget_background);
                            String[] entryValues = getResources().getStringArray(R.array.entryValues_menu_widget_background);
                            updateSummary(prefListWdBackground, entries, entryValues);
                        }

                        // WidgetStyle変更を通知
                        DefaultWidgetProvider.changeStyle(getApplicationContext());
                    }

                    // 「スタンバイ後の挙動」のsummaryを更新
                    else if (MyStringUtlis.eqauls(key, getString(R.string.menu_key_action_after_suspend))) {

                        // 更新実行
                        String[] entries = getResources().getStringArray(R.array.entries_menu_action_after_suspend);
                        String[] entryValues = getResources().getStringArray(R.array.entryValues_menu_action_after_suspend);
                        updateSummary(prefListActionAfterSuspend, entries, entryValues);
                    }
                }
            }
        }
    };

    private void updateSummary(ListPreference prefList, String[] entries, String[] entryValues) {
        int index = AndroidUtils.indexOf(entryValues, prefList.getValue());
        if (index != -1) {
            prefList.setSummary(entries[index]);
        }
    }

    /**
     * 入れ子のPreferenceにテーマが設定されないバグを回避 <br>
     * 
     * http://code.google.com/p/android/issues/detail?id=4611
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        if (Build.VERSION.SDK_INT <= 10) {
            if (preference != null)
                if (preference instanceof PreferenceScreen)
                    if (((PreferenceScreen) preference).getDialog() != null) {
                        Drawable drawable = this.getWindow().getDecorView().getBackground().getConstantState().newDrawable();
                        ((PreferenceScreen) preference).getDialog().getWindow().getDecorView().setBackgroundDrawable(drawable);
                    }
        }
        return false;
    }
}
