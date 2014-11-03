package com.appspot.afnf4199ga.twawm.app;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;

import com.appspot.afnf4199ga.twawm.Const;

public class MainApp extends Application {

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Logger.v("MainApp onConfigurationChanged");

        Intent intent = new Intent(Const.INTENT_WD_CHANGE_STYLE, null, this, DefaultWidgetProvider.class);

        AppWidgetManager appWidgetMgr = AppWidgetManager.getInstance(this);
        ComponentName cm = new ComponentName(this, DefaultWidgetProvider.class);
        int[] appWidgetIds = appWidgetMgr.getAppWidgetIds(cm);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        sendBroadcast(intent);
    }
}
