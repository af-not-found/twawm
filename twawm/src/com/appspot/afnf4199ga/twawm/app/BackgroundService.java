package com.appspot.afnf4199ga.twawm.app;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.afnf.and.twawm2.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;

import com.appspot.afnf4199ga.twawm.BluetoothHelper;
import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.twawm.OnlineChecker;
import com.appspot.afnf4199ga.twawm.StateMachine;
import com.appspot.afnf4199ga.twawm.StateMachine.STATE;
import com.appspot.afnf4199ga.twawm.StateMachine.TRIGGER;
import com.appspot.afnf4199ga.twawm.app.MainActivity.ACTIVITY_FLAG;
import com.appspot.afnf4199ga.twawm.router.EcoModeControl;
import com.appspot.afnf4199ga.twawm.router.RouterControl;
import com.appspot.afnf4199ga.twawm.router.RouterControlByHttp;
import com.appspot.afnf4199ga.twawm.router.RouterControlByHttp.CTRL;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;
import com.appspot.afnf4199ga.utils.MyUncaughtExceptionHandler;

public class BackgroundService extends Service {

    private static BackgroundService instance;
    private WifiManager wifi;
    private ConnectivityManager conn;
    private PowerManager power;
    private WakeLock wakelock = null;

    private Thread watchdogThread;
    private OnlineChecker onlineCheckerThread;
    private StateMachine state = new StateMachine();
    private boolean shortIntervalCheck = false;
    private int shortIntervalCount = 0;
    private boolean receiverRegisted = false;
    private BluetoothHelper btHelper = null;
    private Boolean ecoCharge = null;
    private int onlineCheckCompleteCount = 0;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        Logger.v("BackgroundService onCreate");

        // UncaughtExceptionHandler初期化
        MyUncaughtExceptionHandler.init(this);

        // 初期処理
        super.onCreate();
        instance = this;
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        power = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // 初期状態設定
        boolean enabled = AndroidUtils.isWifiEnabled(wifi);
        state.init(enabled);

        // 動作中の場合
        if (Const.getPrefWorking(this)) {

            // registerReceiver
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            receiverRegisted = true;
            registerReceiver(broadcastReceiver, filter);

            // 起動トリガー
            state.perform(TRIGGER.BOOT);

            // 念のためsetClickIntentしておく
            DefaultWidgetProvider.setClickIntent(this);

            // ステータスバーに通知する場合
            if (Const.isStatusBarNotifyNever(this) == false) {

                // フォアグラウンド起動
                startForeground(Const.NOTIF_ID_MAIN,
                        createNotification(R.drawable.ntficon_wimax_gray_batt_na, getString(R.string.service_started_long)));
            }
        }
        // 一時停止中
        else {
            // UIActを初期化するため、画面を表示
            MainActivity.startActivity(this, ACTIVITY_FLAG.NONE);
            // Toast
            UIAct.toast(getString(R.string.pausing_long));
            // サービス停止はActivity側で行う
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // ログ有効化フラグ更新
        Logger.setEnableLogging(Const.isPrefLoggingEnabled(this));
        Logger.v("BackgroundService onStartCommand intent=" + AndroidUtils.getActionForLog(intent));

        if (intent != null) {

            // 動作中の場合
            if (Const.getPrefWorking(this)) {
                state.reflesh(false);
                state.resetTextLock();
                state.resetRouterSwitchLock();

                // ウィジェットCLICKのintentだった場合
                if (AndroidUtils.isActionEquals(intent, Const.INTENT_WD_CLICKED)) {

                    // クリックアニメーション
                    int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    DefaultWidgetProvider.showClickAnimation(this, widgetId);

                    // 状態ごとのアクションを取得
                    String action;

                    // wifi無効時
                    if (isWifiDisabledState()) {
                        action = Const.getPrefWidgetClickActionWifiDisabled(this);
                    }
                    // オンライン時
                    else if (state.isOnline()) {
                        action = Const.getPrefWidgetClickActionOnline(this);
                    }
                    // オフライン時などその他
                    else {
                        action = Const.getPrefWidgetClickActionOffline(this);
                    }

                    // アクション実行
                    doAction(action);
                }
                // ルーター切り替え
                else if (intent.getBooleanExtra(Const.INTENT_EX_TOGGLE_ROUTER, false)) {
                    Logger.v("BackgroundService onStartCommand EX_TOGGLE_ROUTER");
                    toggleRouterFromUI(false);
                }
                // Wifiトグル
                else if (intent.getBooleanExtra(Const.INTENT_EX_TOGGLE_WIFI, false)) {
                    Logger.v("BackgroundService onStartCommand EX_TOGGLE_WIFI");
                    toggleWifiFromUI();
                }
                // アクション実行
                else {
                    String action = intent.getStringExtra(Const.INTENT_EX_DO_ACTION);
                    if (MyStringUtlis.isEmpty(action) == false) {
                        doAction(action);
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLowMemory() {
        // ログ書き出し
        Logger.startFlushThread(false);

        super.onLowMemory();
    }

    @Override
    public void onDestroy() {

        // ログ有効化フラグ更新
        Logger.setEnableLogging(Const.isPrefLoggingEnabled(this));
        Logger.v("BackgroundService onDestroy");

        // 放電速度更新
        getStateMachine().stopBattCalc(this);

        releaseWakeLock();

        // ステータスバーに通知する場合
        if (Const.isStatusBarNotifyNever(this) == false) {
            // フォアグラウンドサービス停止
            stopForeground(true);
        }

        if (receiverRegisted) {
            unregisterReceiver(broadcastReceiver);
            receiverRegisted = false;
        }

        stopWatchdog();
        terminateOnlineCheck();
        instance = null;

        // ログ書き出し
        Logger.startFlushThread(true);

        super.onDestroy();
    }

    public void onBluetoothDisabled() {

        state.perform(TRIGGER.BT_DISABLED);
    }

    public void onBluetoothEnabled() {

        state.perform(TRIGGER.BT_ENABLED);
    }

    public void onBluetoothConnectedBeforeDisabling() {

        // NetworkSwitcher初期化
        NetworkSwitcher.init();
    }

    public void onBluetoothConnected(boolean success) {

        // 失敗した場合は通知
        if (success == false) {
            postNotify(R.drawable.ntficon_wimax_gray_batt_na, getString(R.string.bluetooth_failed_long));
        }

        // 終了処理
        if (btHelper != null) {
            btHelper.finish();
            btHelper = null;
        }

        // 失敗しても完了とする
        state.perform(TRIGGER.BT_CONNECTED);
    }

    public void onStandbyComplete(boolean success) {
        stopWatchdog();
        state.perform(success ? TRIGGER.STANDBY_OK : TRIGGER.STANDBY_NG);
    }

    public void onOnlineCheckComplete(TRIGGER result, boolean becomeOnline) {
        releaseWakeLock();

        // 対象外ルーターで、サービス停止が設定されている場合
        boolean willstop = false;
        if (result == TRIGGER.NOT_WM && Const.getPrefNonTargetRouterActionStopService(this)) {
            willstop = true;

            // サービスを遅延停止
            stopServiceWithDelay();
        }

        // オンラインの場合、またはWMルーターでない場合
        else if (result == TRIGGER.ONLINE || result == TRIGGER.NOT_WM) {

            // オンラインチェック間隔を元に戻す
            resetShortInterval();

            // オンラインの場合、SSIDを更新
            if (result == TRIGGER.ONLINE) {
                String ssid = wifi.getConnectionInfo().getSSID();
                Const.updatePrefLastTargetRouterInfo(this, ssid);
            }
        }

        // それ以外の場合
        else {

            // 規定回数以内の場合 
            if (++shortIntervalCount < Const.getPrefOnlineCheckCountAfterOffline(this)) {
                // オンラインチェック間隔を短くする
                shortIntervalCheck = true;
            }
            // 規定回数を超えたら
            else {
                // オンラインチェック間隔を元に戻す
                resetShortInterval();
            }
        }

        // 状態遷移
        state.perform(result);

        // サービスが停止しない場合
        if (willstop == false) {

            // WMシリーズのみ
            if (RouterControlByHttp.isNad() == false) {
                // 100回ごと、または今回オンラインになった場合に、ロングライフ充電状態を取得
                if (++onlineCheckCompleteCount % 100 == 0 || becomeOnline) {
                    EcoModeControl.changeEcoMode(null);
                }
            }

            // スイッチロック中で、WMルータでなければ
            if (state.isRouterSwitchLocked() && result == TRIGGER.NOT_WM) {
                // リモート起動に時間がかかるのでもう一度ロックしておく			
                state.lockRouterSwitch();
                // スキャン実行（通知したいのでperformの後で行う）
                startScan();
            }
        }
    }

    private void startScan() {

        // WMルータSSIDが設定されていれば
        if (MyStringUtlis.isEmpty(Const.getPrefLastTargetSSID(this)) == false) {

            // 通知
            Logger.i("switching started");
            postNotify(R.drawable.ntficon_wimax_gray_batt_na, getString(R.string.switching_router));

            // 10秒待ってスキャン要求
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AndroidUtils.sleep(Const.WIFI_SCAN_DELAY_AFTER_RESUME);
                    wifi.startScan();
                }
            }).start();
        }
    }

    private void onScanResultsAvailable() {

        // ロック中でないか、NOT_WMでない場合は、抜ける
        if (state.isRouterSwitchLocked() == false || state.getState() != STATE.NOT_WM) {
            return;
        }

        try {
            // チェック
            List<ScanResult> scanResults = wifi.getScanResults();
            if (scanResults == null || scanResults.size() == 0) {
                return;
            }

            // 設定済ネットワークが無ければ抜ける
            List<WifiConfiguration> configuredNetworks = wifi.getConfiguredNetworks();
            if (configuredNetworks == null || configuredNetworks.size() == 0) {
                return;
            }

            // 移動元のネットワークIDを覚えておく
            final int currentNetworkId = wifi.getConnectionInfo().getNetworkId();

            // 切り替え対象のネットワークIDを取得
            Set<Integer> switchTargetNetworkIdSet = getSwitchTargetNetworkIdSet(wifi.getConnectionInfo(),
                    Const.getPrefLastTargetSSID(this), scanResults, configuredNetworks);

            // ネットワーク切り替え
            if (switchTargetNetworkIdSet != null) {
                Iterator<Integer> ite = switchTargetNetworkIdSet.iterator();
                while (ite.hasNext()) {
                    // 切り替えたいので、移動元を無効にする
                    Integer switchTargetNetworkId = ite.next();
                    boolean success = wifi.enableNetwork(switchTargetNetworkId, true);
                    Logger.i("switching network " + (success ? "succeeded" : "failed"));

                    // 成功したら抜ける。失敗するときはむしろ非同期・・・
                    if (success) {

                        // スイッチロック解除
                        state.resetRouterSwitchLock();

                        // ルーターIPアドレス未設定なら警告
                        if (MyStringUtlis.isEmpty(Const.getPrefApIpAddr(this))) {
                            UIAct.toast(getString(R.string.router_ip_addr_not_set));
                        }

                        // 移動元を無効にしてしまったので、20秒後に戻す
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AndroidUtils.sleep(20000);
                                wifi.enableNetwork(currentNetworkId, false);
                            }
                        }).start();

                        return;
                    }
                }
            }
        }
        catch (Throwable e) {
            Logger.w("onScanResultsAvailableWhenNotWM failed", e);
        }
    }

    protected static Set<Integer> getSwitchTargetNetworkIdSet(WifiInfo connectionInfo, String confssid,
            List<ScanResult> scanResults, List<WifiConfiguration> configuredNetworks) {

        // 現在のSSIDが空の場合、抜ける
        if (connectionInfo == null) {
            return null;
        }
        String current_ssid = connectionInfo.getSSID();
        if (MyStringUtlis.isEmpty(current_ssid)) {
            return null;
        }
        current_ssid = MyStringUtlis.trimQuote(current_ssid);

        // チェック
        if (scanResults == null || scanResults.size() == 0) {
            return null;
        }
        if (configuredNetworks == null || configuredNetworks.size() == 0) {
            return null;
        }

        // 設定のSSIDを取得してパース
        if (MyStringUtlis.isEmpty(confssid)) {
            Logger.i("switching terminated : confssid is blank");
            return null;
        }

        // 現在のSSIDが、設定のどれかと一致している場合は、WMルーターに接続していると見なして抜ける
        if (MyStringUtlis.eqauls(confssid, current_ssid)) {
            Logger.i("switching terminated : confssid matched");
            return null;
        }

        // 切り替え対象ネットワークID
        Set<Integer> switchTargetNetworkIdList = new LinkedHashSet<Integer>();

        // スキャン結果でループ
        boolean found = false;
        Iterator<ScanResult> ite = scanResults.iterator();
        SR: while (ite.hasNext()) {
            ScanResult scanResult = (ScanResult) ite.next();

            // スキャン結果に合致する設定SSIDが見つかった場合
            String sssid = MyStringUtlis.trimQuote(scanResult.SSID);
            if (MyStringUtlis.eqauls(sssid, confssid)) {

                // 対応する設定済ネットワークを取得
                for (WifiConfiguration wc : configuredNetworks) {

                    // 設定済ネットワーク発見
                    //  ※wifiConfiguration.SSIDはダブルクォートで囲まれている
                    String wcssid = MyStringUtlis.trimQuote(wc.SSID);
                    if (MyStringUtlis.eqauls(wcssid, confssid)) {

                        switchTargetNetworkIdList.add(wc.networkId);
                        found = true;
                        break SR;
                    }
                }
            }
        }

        if (found) {
            return switchTargetNetworkIdList;
        }
        else {
            Logger.i("switching terminated : target not found");
            return null;
        }
    }

    public void onEcoModeControlFinished(Boolean ecoCharge) {
        //Logger.i("onEcoModeControlFinished : " + ecoMode);
        this.ecoCharge = ecoCharge;

        boolean wifiEnabled = AndroidUtils.isWifiEnabled(getWifi());
        boolean suppCompleted = wifiEnabled && isSupplicantCompleted();
        UIAct.postActivityButton(null, null, wifiEnabled, suppCompleted, ecoCharge, null, null);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void disableBT() {

        synchronized (BackgroundService.class) {
            if (btHelper != null) {
                btHelper.finish();
            }
            btHelper = new BluetoothHelper(this);
        }

        // Bluetoothを再起動しない場合はスキップ
        if (Const.isPrefBtRestartType(this) == false) {
            state.perform(TRIGGER.BT_DISABLED);
        }
        // カスタムなら無効化実行
        else {
            btHelper.disable();
        }
    }

    public void enableBT() {
        if (btHelper == null) {
            Logger.e("btHelper is null on BackgroundService.enableBT");
            return;
        }

        btHelper.enable();
    }

    public void bluetoothResume() {
        if (btHelper == null) {
            Logger.e("btHelper is null on BackgroundService.bluetoothResume");
            return;
        }

        btHelper.connect();
    }

    public void enableWifi() {
        stopWatchdog();

        if (AndroidUtils.isWifiEnabled(wifi)) {
            state.perform(TRIGGER.BC_WIFI_ENABLED);
        }
        else {
            startWatchdogOnApConn();
            wifi.setWifiEnabled(true);
        }
    }

    public void waitSupplicantComplete() {
        stopWatchdog();
        resetShortInterval();

        // ネットワーク無効化
        NetworkSwitcher.disableNetwork(this);

        // 先行してSupplicantStateをチェック
        if (getConnectivityState() == ConnectivityState.COMPLETE_WIFI) {
            state.perform(TRIGGER.BC_SUPPLICANT_COMPLETE);
        }
        else {
            startWatchdogOnApConn();
        }
    }

    public void checkOnline() {

        // ネットワーク有効化
        NetworkSwitcher.reEnableWithoutD();

        state.resetTextLock();
        startOnlineCheck(0);
    }

    public void checkOnlineWithDelay() {
        startOnlineCheck(Const.getPrefOnlineCheckIntervalMs(this));
    }

    public void standby() {

        // 放電速度更新
        getStateMachine().stopBattCalc(this);

        // 失敗に備えて、NetStateを一旦AP無しに変更
        state.setNetStateToNoAP();

        // オンラインチェック停止
        terminateOnlineCheck();

        // スタンバイ実行
        RouterControl.execStandby();

        startWatchdog();
    }

    public void disableWifi() {
        terminateOnlineCheck();

        if (AndroidUtils.isWifiDisabled(wifi)) {
            state.perform(TRIGGER.BC_WIFI_DISABLED);
        }
        else {
            startWatchdog();
            wifi.setWifiEnabled(false);
        }
    }

    public void wifiDisabled() {
        stopServiceImmediately();
    }

    public void stopServiceImmediately() {
        BackgroundService service = BackgroundService.getInstance();
        if (service != null) {
            // サービス停止
            Intent intent = new Intent(this, BackgroundService.class);
            stopService(intent);
            instance = null; // これがないと、一時停止後の再開によるサービス起動が動作しない？

            // NetworkSwitcherリセット
            NetworkSwitcher.reset();
        }
    }

    public void stopServiceWithDelay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AndroidUtils.sleep(Const.SERVICE_STOP_DELAY_MS);
                stopServiceImmediately();
            }
        }).start();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void doAction(String action) {

        // NetworkSwitcherリセット
        NetworkSwitcher.reset();

        // do nothing
        if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__none))) {
        }
        // オンラインチェック
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__check))) {
            resetShortInterval();
            terminateOnlineCheck();
            startOnlineCheck(0);
        }
        // リモート起動 + ルーター切り替え
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__resume_switch))) {
            state.lockRouterSwitch();
            toggleRouterFromUI(true);
        }
        // ルーター切り替え
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__switch))) {
            if (state.getState() == STATE.NOT_WM) {
                state.lockRouterSwitch();
                startScan();
            }
        }
        // リモート起動 + WiFi ON
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__resume))) {
            toggleRouterFromUI(true);
        }
        // スタンバイ
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__standby))) {
            terminateOnlineCheck();
            state.perform(TRIGGER.BUTTON_STANDBY);
        }
        // WiFi ON
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__wifi_on))) {
            state.perform(TRIGGER.BUTTON_WIFI_ON);
        }
        // WiFi OFF
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__wifi_off))) {
            terminateOnlineCheck();
            state.perform(TRIGGER.BUTTON_WIFI_OFF);
        }
        // WiFiリスタート
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__restart))) {
            state.setStateToWifiRestart();
        }
        // WiMAX再接続
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__wimax_recn))) {
            // ファームウェアバージョンが古い場合は実行しない
            if (RouterControlByHttp.isWm3800FirmwareVersionOld()) {
                UIAct.toast(getString(R.string.wimax_recn_failed_frmver));
            }
            else if (state.isWmReachableState()) {
                terminateOnlineCheck();
                RouterControl.execWimaxReconnection();
            }
        }
        // ルーター再起動
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__reboot_wm))) {
            if (state.isWmReachableState()) {
                terminateOnlineCheck();
                RouterControl.execRouterCtrl(CTRL.REBOOT_WM);
            }
        }
        // サービス停止
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__stop_service))) {
            MainActivity.updateAsWorkingOrPausing(this, true);
            stopServiceImmediately();
        }
        // 一覧から選択
        else if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__choose))) {
            MainActivity.startActivity(this, ACTIVITY_FLAG.ACTION_DIALOG);
        }
        // FIXME NAD11アクション追加
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            //Logger.v("BackgroundService broadcastReceiver intent=" + Utils.getActionForLog(intent));

            // 動作中の場合
            if (Const.getPrefWorking(context)) {

                // Bluetooth状態更新
                if (AndroidUtils.isActionEquals(intent, BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    int bt_state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                    if (bt_state == BluetoothAdapter.STATE_ON) {
                        state.perform(TRIGGER.BT_ENABLED);
                    }
                    else if (bt_state == BluetoothAdapter.STATE_OFF) {
                        state.perform(TRIGGER.BT_DISABLED);
                    }
                }

                // WiFi状態更新
                else if (AndroidUtils.isActionEquals(intent, WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                    int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_ENABLING);
                    boolean enabled = wifiState == WifiManager.WIFI_STATE_ENABLED;
                    boolean disabled = wifiState == WifiManager.WIFI_STATE_DISABLED;
                    boolean disabling = wifiState == WifiManager.WIFI_STATE_DISABLING;

                    // 念のためConnectivityManagerを再取得
                    if (enabled) {
                        conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    }

                    if (enabled || disabled) {
                        boolean byThisApp = stopWatchdog();
                        // 外部からのWiFi切り替えの場合、このタイミングでresetTextLockしないとテキストが切り替わらない場合がある
                        if (byThisApp == false) {
                            state.resetTextLock();
                            // 良く分からないタイミングでインテントが飛んでくるので、resetAllとかはしない
                        }
                    }

                    // perform
                    if (enabled) {
                        state.perform(TRIGGER.BC_WIFI_ENABLED);
                    }
                    else if (disabled) {
                        // NetworkSwitcherリセット
                        NetworkSwitcher.reset();
                        state.perform(TRIGGER.BC_WIFI_DISABLED);
                    }
                    else if (disabling) {
                        state.perform(TRIGGER.BC_WIFI_DISABLING);
                    }
                }
                // supplicant状態更新
                else if (AndroidUtils.isActionEquals(intent, WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
                        || AndroidUtils.isActionEquals(intent, ConnectivityManager.CONNECTIVITY_ACTION)) {

                    if (state.isSupplicantEnableWaitState() && getConnectivityState() == ConnectivityState.COMPLETE_WIFI) {
                        stopWatchdog();
                        resetShortInterval();
                        state.perform(TRIGGER.BC_SUPPLICANT_COMPLETE);
                    }
                    else if (AndroidUtils.isActionEquals(intent, WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                        SupplicantState newstate = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                        if (newstate != null && (newstate == SupplicantState.DISCONNECTED || newstate == SupplicantState.DORMANT)) {
                            state.perform(TRIGGER.BC_SUPPLICANT_DISCONNECTED);
                        }
                    }
                }
                // スキャン結果
                else if (AndroidUtils.isActionEquals(intent, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    onScanResultsAvailable();
                }
                // SCREEN_ON
                else if (AndroidUtils.isActionEquals(intent, Intent.ACTION_SCREEN_ON)) {
                    resetShortInterval();
                    state.resetTextLock();
                    state.startBattCalc(context);
                    // WiFi状態はあえて見ない。画面OFFの間にnoAPになった場合、これを検出させるためにオンラインチェックを実行する。
                    startOnlineCheck(0);
                }
                // SCREEN_OFF
                else if (AndroidUtils.isActionEquals(intent, Intent.ACTION_SCREEN_OFF)) {
                    state.stopBattCalc(context);
                    terminateOnlineCheck();
                }
            }
        }
    };

    void startWatchdogOnApConn() {
        //Logger.v("Watchdog started");
        watchdogThread = new WatchdogThread(Const.getPrefApConnTimeoutMs(this));
        watchdogThread.start();
    }

    void startWatchdog() {
        //Logger.v("Watchdog started");
        watchdogThread = new WatchdogThread(Const.WATCH_DOG_TIMEOUT_MS);
        watchdogThread.start();
    }

    /**
     * @return 実際にinterruptした場合、true
     */
    boolean stopWatchdog() {
        if (watchdogThread != null && watchdogThread.isAlive()) {
            //Logger.v("Watchdog stopped");
            watchdogThread.interrupt();
            watchdogThread = null;
            return true;
        }
        return false;
    }

    class WatchdogThread extends Thread {
        long timeout_ms = -1;

        public WatchdogThread(long timeout) {
            this.timeout_ms = timeout;
        }

        @Override
        public void run() {
            try {
                // タイムアウトまでスリープ
                if (timeout_ms > 0) {
                    Thread.sleep(timeout_ms);
                }

                Logger.w("Watchdog timeout error, " + timeout_ms + "ms");

                // 状態遷移
                state.perform(TRIGGER.WATCHDOG_TIMEOUT);
            }
            catch (InterruptedException e) {
                // interruptされるのは正常
            }
            catch (Throwable e) {
                Logger.w("WatchdogThread error", e);
            }
        }
    }

    public void startOnlineCheck(long delay_ms) {
        terminateOnlineCheck();

        if (delay_ms != 0 && shortIntervalCheck) {
            delay_ms = Const.getPrefOnlineCheckIntervalMsAfterOffline(this);
        }

        // オンラインチェック実施
        Logger.v("OnlineChecker started, delay_ms=" + delay_ms);
        onlineCheckerThread = new OnlineChecker(delay_ms);
        onlineCheckerThread.start();
    }

    public void terminateOnlineCheck() {
        if (onlineCheckerThread != null && onlineCheckerThread.isExecuting()) {
            Logger.v("OnlineChecker stopped");
            onlineCheckerThread.stopThread();
            onlineCheckerThread = null;
        }
    }

    public void postNotify(int notifyImageId, String notifyText) {
        Logger.v("BackgroundService postNotify");

        if (Const.isStatusBarNotifyNever(this) == false) {
            NotificationManager nman = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // 常に通知する場合
            if (Const.isStatusBarNotifyAlways(this)) {
                // 一旦フォアグラウンドをキャンセルしてから再度起動
                stopForeground(true);
                startForeground(Const.NOTIF_ID_MAIN, createNotification(notifyImageId, notifyText));
            }
            else {
                // 普通に通知
                nman.notify(Const.NOTIF_ID_MAIN, createNotification(notifyImageId, notifyText));
            }
        }
    }

    private Notification createNotification(int notifyImageId, String notifyText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentIntent(contentIntent);
        builder.setTicker(notifyText);
        builder.setSmallIcon(notifyImageId);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(notifyText);
        builder.setWhen(System.currentTimeMillis());
        return builder.build();
    }

    public void toggleRouterFromUI(boolean forceResume) {
        boolean resume = forceResume || getConnectivityState() != ConnectivityState.COMPLETE_WIFI;

        if (resume) {
            state.resetTextLock();
            String bluetoothAddress = Const.getPrefBluetoothAddress(this);
            if (BluetoothHelper.isValidBluetoothAddress(bluetoothAddress)) {
                getWakeLock();
                state.perform(TRIGGER.BUTTON_BT_RESUME);
            }
            else {
                state.perform(TRIGGER.BUTTON_WIFI_ON);
            }
        }
        else {
            state.perform(TRIGGER.BUTTON_STANDBY);
        }
    }

    public void toggleWifiFromUI() {
        boolean enabled = AndroidUtils.isWifiEnabledOrEnabling(wifi);
        state.perform(enabled ? TRIGGER.BUTTON_WIFI_OFF : TRIGGER.BUTTON_WIFI_ON);
    }

    private synchronized void getWakeLock() {
        if (wakelock == null) {
            Logger.v("getWakeLock");
            wakelock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    Const.LOGTAG);
            wakelock.acquire();

            new Thread() {
                public void run() {
                    AndroidUtils.sleep(60000);
                    releaseWakeLock();
                };
            }.start();
        }
    }

    private synchronized void releaseWakeLock() {
        if (isWakeLocked()) {
            Logger.v("releaseWakeLock");
            wakelock.release();
            wakelock = null;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static enum ConnectivityState {
        COMPLETE_WIFI, COMPLETE_MOBILE, STANDBY_OK, NONE
    };

    public ConnectivityState getConnectivityState() {

        if (isSupplicantCompleted()) {
            ConnectivityState connState = getConnectivityState(conn);
            if (connState != null) {
                return connState;
            }
        }

        if (state.isStandbyWaitState()) {
            return ConnectivityState.STANDBY_OK;
        }

        return ConnectivityState.NONE;
    }

    protected static ConnectivityState getConnectivityState(ConnectivityManager conn) {
        if (conn != null) {
            NetworkInfo activeNetworkInfo = conn.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                DetailedState detailedState = activeNetworkInfo.getDetailedState();
                if (detailedState == DetailedState.CONNECTED) {
                    int type = activeNetworkInfo.getType();
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        return ConnectivityState.COMPLETE_WIFI;
                    }
                    else if (type == ConnectivityManager.TYPE_MOBILE || type == ConnectivityManager.TYPE_MOBILE_DUN
                            || type == ConnectivityManager.TYPE_MOBILE_HIPRI || type == ConnectivityManager.TYPE_MOBILE_MMS
                            || type == ConnectivityManager.TYPE_MOBILE_SUPL) {
                        return ConnectivityState.COMPLETE_MOBILE;
                    }
                }
            }
        }
        return null;
    }

    /**
     * WiFiが無効でもtrueを返す場合がある
     * 
     * @return
     */
    public boolean isSupplicantCompleted() {
        return isSupplicantCompleted(wifi);
    }

    /**
     * WiFiが無効でもtrueを返す場合がある
     * 
     * @param wifi
     * @return
     */
    public static boolean isSupplicantCompleted(WifiManager wifi) {
        return wifi.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED;
    }

    public static BackgroundService getInstance() {
        return instance;
    }

    public boolean isWifiDisabledState() {
        return getStateMachine().getState() == STATE.WIFI_DISABLED;
    }

    public StateMachine getStateMachine() {
        return state;
    }

    public WifiManager getWifi() {
        return wifi;
    }

    public boolean isScreenOn() {
        return power.isScreenOn();
    }

    private void resetShortInterval() {
        shortIntervalCheck = false;
        shortIntervalCount = 0;
    }

    public Boolean getEcoCharge() {
        return ecoCharge;
    }

    public boolean isWakeLocked() {
        return wakelock != null && wakelock.isHeld();
    }
}
