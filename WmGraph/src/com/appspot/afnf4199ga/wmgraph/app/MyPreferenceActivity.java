package com.appspot.afnf4199ga.wmgraph.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

import com.appspot.afnf4199ga.utils.MyStringUtlis;
import com.appspot.afnf4199ga.wmgraph.R;

@SuppressWarnings("deprecation")
public class MyPreferenceActivity extends PreferenceActivity {

	static public Pattern IP_ADDR_PATTERN = Pattern
			.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-4])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-4])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-4])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-4])$");

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

	static private InputFilter maxlength15Filter = new InputFilter.LengthFilter(15);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		EditTextPreference ipAddrPref = (EditTextPreference) findPreference(getText(R.string.menu_key_ap_ip_addr));
		EditText ipAddr = ipAddrPref.getEditText();
		ipAddr.setFilters(new InputFilter[] { ipAddrFilter, maxlength15Filter });

		ipAddrPref.setOnPreferenceChangeListener(ipAddrPrefListener);
	}

	/**
	 * 入れ子のPreferenceにテーマが設定されないバグを回避
	 */
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);
		if (preference != null)
			if (preference instanceof PreferenceScreen)
				if (((PreferenceScreen) preference).getDialog() != null)
					((PreferenceScreen) preference)
							.getDialog()
							.getWindow()
							.getDecorView()
							.setBackgroundDrawable(
									this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
		return false;
	}

}
//