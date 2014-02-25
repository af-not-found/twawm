package com.appspot.afnf4199ga.wmgraph.app;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.TextView;

import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.wmgraph.R;

public class InfoActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);

		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			TextView appVerText = (TextView) findViewById(R.id.appVerText);
			appVerText.setText(getString(R.string.app_version) + packageInfo.versionName);
		}
		catch (Throwable e) {
			Logger.w("getPackageInfo failed", e);
		}

		try {
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
			ZipFile zf = new ZipFile(ai.sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			long time = ze.getTime();
			String buildDate = "BuildDate : " + SimpleDateFormat.getDateTimeInstance().format(new java.util.Date(time));
			((TextView) findViewById(R.id.buildDate)).setText(buildDate);
		}
		catch (Exception e) {
			Logger.w("getBuildDate failed", e);
		}
	}
}
