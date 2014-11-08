package com.appspot.afnf4199ga.twawm.app;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class NetworkSwitcher extends Thread {

    private static Set<String> disabledNetworks = new HashSet<String>();
    private static long btFinTime = -1;
    private static NetworkSwitcher instance = null;

    private long delay = 0;

    /**
     * 初期化
     */
    public static void init() {
        reset();

        // BT完了時刻更新
        btFinTime = System.currentTimeMillis();
    }

    /**
     * 有効化を遅延スタート
     * 
     * @param context
     */
    public static void reEnableWithDelay(Context context) {

        if (disabledNetworks.size() >= 1) {

            instance = new NetworkSwitcher();
            instance.delay = calcWindow(context);

            // スレッド開始
            instance.start();
        }
    }

    /**
     * 有効化を即時スタート
     * 
     * @param context
     */
    public static void reEnableWithoutD() {

        if (disabledNetworks.size() >= 1) {

            if (instance != null) {
                instance.interrupt();
            }

            // 同期実行
            instance = new NetworkSwitcher();
            instance.run();
        }
    }

    /**
     * リセット
     */
    public static void reset() {
        btFinTime = -1;
        disabledNetworks.clear();

        if (instance != null) {
            instance.interrupt();
            instance = null;
        }
    }

    @Override
    public void run() {
        reEnableNetwork();
    }

    /**
     * APタイムアウトなどから、切り替え許可幅を計算。20秒～90秒に制限される。
     * 
     * @param context
     * @return
     */
    public static long calcWindow(Context context) {
        long delay = (Const.getPrefApConnRetryLimit(context) + 1) * Const.getPrefApConnTimeoutMs(context);
        return Math.max(20000, Math.min(delay, 90000));
    }

    /**
     * 対象外AP設定を無効化
     * 
     * @param service
     */
    public static void disableNetwork(BackgroundService service) {

        disabledNetworks.clear();

        // BT完了時刻更新が直近で、かつ他AP無効が有効な場合で
        long diff = System.currentTimeMillis() - btFinTime;
        Logger.v("waitSupplicantComplete diff=" + diff + ", disableOtherApAfterResume="
                + Const.getPrefDisableOtherApAfterResume(service) + ", wmssid=" + Const.getPrefLastTargetSSID(service));
        if (diff <= calcWindow(service) && Const.getPrefDisableOtherApAfterResume(service)) {
            btFinTime = -1;

            // WM SSIDがあり
            String confssid = Const.getPrefLastTargetSSID(service);
            if (MyStringUtlis.isEmpty(confssid) == false) {

                // 設定済ネットワークがあり
                List<WifiConfiguration> configuredNetworks = service.getWifi().getConfiguredNetworks();
                if (configuredNetworks != null && configuredNetworks.size() >= 1) {
                    for (WifiConfiguration wc : configuredNetworks) {

                        // SSIDが一致しない場合
                        //  ※WifiConfiguration#SSIDはダブルクォートで囲まれている
                        String wcssid = MyStringUtlis.trimQuote(wc.SSID);
                        if (MyStringUtlis.eqauls(confssid, wcssid) == false) {
                            Logger.v("NetworkSwitcher disableNetwork=" + wcssid);

                            // ネットワークを無効化
                            service.getWifi().disableNetwork(wc.networkId);

                            // SSIDを記録しておく
                            disabledNetworks.add(wcssid);
                        }
                    }
                }
            }
        }

        // ネットワーク有効化の遅延起動
        NetworkSwitcher.reEnableWithDelay(service);
    }

    /**
     * 有効化の内部処理
     */
    private void reEnableNetwork() {

        // ウェイト
        boolean interrupt = AndroidUtils.sleep(delay);

        // interruptされていなければ実行
        if (interrupt == false) {

            synchronized (NetworkSwitcher.class) {

                if (disabledNetworks.size() >= 1) {

                    BackgroundService service = BackgroundService.getInstance();
                    if (service == null) {
                        Logger.e("service is null on NetworkSwitcher.run#1");
                        return;
                    }

                    WifiManager wifi = service.getWifi();

                    // 設定済ネットワークがあり
                    List<WifiConfiguration> configuredNetworks = wifi.getConfiguredNetworks();
                    if (configuredNetworks != null && configuredNetworks.size() >= 1) {
                        for (WifiConfiguration wc : configuredNetworks) {

                            // 無効化済み集合にこのSSIDが見つかった場合
                            //  ※WifiConfiguration#SSIDはダブルクォートで囲まれている
                            String wcssid = MyStringUtlis.trimQuote(wc.SSID);
                            if (disabledNetworks.contains(wcssid)) {
                                Logger.v("NetworkSwitcher enableNetwork=" + wcssid);

                                // ネットワークを有効化
                                wifi.enableNetwork(wc.networkId, false);
                            }
                        }
                    }
                    disabledNetworks.clear();
                }
            }
        }
    }

}