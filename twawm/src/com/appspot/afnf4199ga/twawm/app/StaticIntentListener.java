package com.appspot.afnf4199ga.twawm.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class StaticIntentListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Logger.v("StaticIntentListener intent=" + AndroidUtils.getActionForLog(intent));

        // 動作中の場合
        if (Const.getPrefWorking(context)) {

            BackgroundService service = BackgroundService.getInstance();

            // 端末起動完了
            if (AndroidUtils.isActionEquals(intent, Intent.ACTION_BOOT_COMPLETED)) {
                Logger.v("StaticIntentListener ACTION_BOOT_COMPLETED");

                // サービス開始を試みる
                startServiceIfNeed(context, service);
            }

            // 端末シャットダウン
            else if (AndroidUtils.isActionEquals(intent, Intent.ACTION_SHUTDOWN)) {
                Logger.v("StaticIntentListener ACTION_SHUTDOWN");

                // サービス終了
                if (service != null) {
                    service.stopServiceImmediately();
                }
                else {
                    Logger.startFlushThread(true);
                }
            }
            // Wifiオン
            else if (AndroidUtils.isActionEquals(intent, WifiManager.WIFI_STATE_CHANGED_ACTION)) {

                // 有効化intentなら
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_ENABLED);
                if (state == WifiManager.WIFI_STATE_ENABLED) {

                    // サービス開始を試みる
                    startServiceIfNeed(context, service);
                }
            }

            // Tasker
            else if (AndroidUtils.isActionEquals(intent, Const.INTENT_TASKER)) {
                String act = intent.getStringExtra("act");
                Logger.v("TaskerIntentReceiver act=" + act);
                if (MyStringUtlis.isEmpty(act) == false) {

                    // サービス開始（既に開始済でもこれでOK）
                    Intent srvIntent = new Intent(context, BackgroundService.class);
                    srvIntent.putExtra(Const.INTENT_EX_DO_ACTION, act);
                    context.startService(srvIntent);
                }
            }
        }
    }

    private void startServiceIfNeed(Context context, BackgroundService service) {

        // サービス未起動で
        if (service == null) {

            // 感知有効で
            if (Const.getPrefStartServiceWhenWifiEnabled(context)) {

                // WiFi起動中なら
                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if (AndroidUtils.isWifiEnabledOrEnabling(wifi)) {

                    // サービス起動
                    Logger.v("StaticIntentListener WIFI_STATE_ENABLED");
                    Intent bootIntent = new Intent(context, BackgroundService.class);
                    context.startService(bootIntent);
                }
            }
        }
    }
}
