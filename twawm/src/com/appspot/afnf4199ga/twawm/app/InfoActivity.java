package com.appspot.afnf4199ga.twawm.app;

import net.afnf.and.twawm2.R;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.TextView;

import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;

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

        ((TextView) findViewById(R.id.buildDate)).setText("BuildDate : " + AndroidUtils.getBuildDate(this));
    }

}
