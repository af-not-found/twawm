package com.appspot.afnf4199ga.twawm.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.twawm.WifiNetworkConfig;
import com.appspot.afnf4199ga.twawm.WifiNetworkConfig.SsidFilterAction;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;

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
                boolean started = startServiceOnWifiStarted(context, service);
                if (started == false) {
                    startServiceOnSupplicantCompleted(context, service);
                }
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
                    startServiceOnWifiStarted(context, service);
                }
            }
            // supplicant状態更新
            else if (AndroidUtils.isActionEquals(intent, WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {

                // 完了intentなら
                SupplicantState newstate = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                if (newstate != null && newstate == SupplicantState.COMPLETED) {

                    // サービス開始を試みる
                    startServiceOnSupplicantCompleted(context, service);
                }
            }
        }
    }

    private boolean startServiceOnWifiStarted(Context context, BackgroundService service) {

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

                    return true;
                }
            }
        }

        return false;
    }

    private void startServiceOnSupplicantCompleted(Context context, BackgroundService service) {

        // サービス未起動で
        if (service == null) {

            // SSIDフィルタをチェック
            SsidFilterAction config = WifiNetworkConfig.getSsidFilterActionForCurrentAP(context, null);
            if (config == SsidFilterAction.START) {

                // サービス起動
                Logger.v("StaticIntentListener SUPPLICANT_STATE_CHANGED_ACTION");
                Intent bootIntent = new Intent(context, BackgroundService.class);
                context.startService(bootIntent);
            }
        }
    }

}
