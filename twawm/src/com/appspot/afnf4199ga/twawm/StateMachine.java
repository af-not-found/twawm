package com.appspot.afnf4199ga.twawm;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.afnf.and.twawm2.R;
import android.content.Context;

import com.appspot.afnf4199ga.twawm.app.BackgroundService;
import com.appspot.afnf4199ga.twawm.app.BackgroundService.ConnectivityState;
import com.appspot.afnf4199ga.twawm.app.DefaultWidgetProvider;
import com.appspot.afnf4199ga.twawm.app.UIAct;
import com.appspot.afnf4199ga.twawm.router.RouterInfo;
import com.appspot.afnf4199ga.twawm.router.RouterInfo.COM_TYPE;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class StateMachine {

    // STATE
    private STATE current = null;
    private STATE prevState1 = null;

    // 状態
    private NETWORK_STATE netState;
    private int antennaLevel;
    private int batteryLevel;
    private int apConnFailedCount;
    private long lockTextUntil;
    private int notifyImageId;
    private String triggerName;
    private int wdImageId;
    private String wdText;
    private long lockRouterSwitchUntil;
    private COM_TYPE comState;
    private COM_TYPE comSetting;
    private Boolean wifiSpotEnabled;

    // 放電速度
    protected final int RATE_SIZE = 5;
    protected double currentRate = -1;
    protected double averageRate = -1;
    protected List<Double> pastRates = new ArrayList<Double>();
    protected long startTime = -1;
    protected int startBattLevel = -1;
    protected long prevUpdateTime = -1;
    private static final int BATT_SAVE_THRESHOLD = 15 * 60 * 1000 + 1; // 15分
    private static final double BATT_RATE_THRESHOLD_MIN = 4; // 4%/h → 25h
    private static final double BATT_RATE_THRESHOLD_MAX = 25; // 25%/h → 4h

    public static enum STATE {
        /** online */
        ONLINE,
        /** offline */
        OFFLINE,
        /** notWM */
        NOT_WM,
        /** AP無し */
        AP_NOT_FOUND,
        /** wifi無効 */
        WIFI_DISABLED,
        /** BT無効待ち */
        BT_DISABLE_WAIT,
        /** BT有効待ち */
        BT_ENABLE_WAIT,
        /** BT接続待ち */
        BT_CONN_WAIT,
        /** wifi有効待ち */
        WIFI_ENABLE_WAIT,
        /** supplicant有効待ち */
        SUPPLICANT_ENABLE_WAIT,
        /** online待ち */
        ONLINE_WAIT,
        /** 再接続・wifi無効待ち */
        RECCONECT_WIFI_DISABLE_WAIT,
        /** スタンバイ待ち */
        STANDBY_WAIT,
        /** wifi無効待ち */
        WIFI_DISABLE_WAIT;

        public static STATE ordinalOf(int ordinal) {
            Iterator<STATE> ite = EnumSet.allOf(STATE.class).iterator();
            while (ite.hasNext()) {
                STATE e = ite.next();
                if (e.ordinal() == ordinal)
                    return e;
            }
            return null;
        }
    }

    public enum NETWORK_STATE {
        AP_NOT_FOUND, NOT_WM_ROUTER, OFFLINE, ONLINE
    };

    public static enum TRIGGER {
        /** 起動 */
        BOOT,
        /** リモート起動 + wifi ON */
        BUTTON_BT_RESUME,
        /** wifi ON */
        BUTTON_WIFI_ON,
        /** wifi OFF */
        BUTTON_WIFI_OFF,
        /** BT無効完了 */
        BT_DISABLED,
        /** BT有効完了 */
        BT_ENABLED,
        /** BT接続完了 */
        BT_CONNECTED,
        /** BC：W_Enabled */
        BC_WIFI_ENABLED,
        /** BC：S_Completed */
        BC_SUPPLICANT_COMPLETE,
        /** online */
        ONLINE,
        /** offline */
        OFFLINE,
        /** notWM */
        NOT_WM,
        /** AP無し */
        AP_NOT_FOUND,
        /** watchdogタイムアウト */
        WATCHDOG_TIMEOUT,
        /** スタンバイボタン押下 */
        BUTTON_STANDBY,
        /** スタンバイ成功 */
        STANDBY_OK,
        /** スタンバイ失敗 */
        STANDBY_NG,
        /** supplicant切断 */
        BC_SUPPLICANT_DISCONNECTED,
        /** BC：W_disanabling */
        BC_WIFI_DISABLING,
        /** BC：W_disanabled */
        BC_WIFI_DISABLED;

        public static TRIGGER ordinalOf(int ordinal) {
            Iterator<TRIGGER> ite = EnumSet.allOf(TRIGGER.class).iterator();
            while (ite.hasNext()) {
                TRIGGER e = ite.next();
                if (e.ordinal() == ordinal)
                    return e;
            }
            return null;
        }
    }

    public static final String[] STATE_ARRAY = {
            "ay-d-----0123-c--a-4",
            "ay-d-----0123-c--a-4",
            "ay-d----a0123------4",
            "-5-d----a0123------4",
            "-y8----9------------",
            "----6-8-----------d-",
            "-----78-----------d-",
            "------8-----------d-",
            "-------9----------d-",
            "--------a----z----d-",
            "---------012zz-----4",
            "-------------4-----8",
            "-------------3-x3--4",
            "-------------4-----4" };

    public static final String[] ACTION_ARRAY = {
            "CY-D-----WW##-S--C-A",
            "CY-D-----WW##-S--C-A",
            "CY-D----CWW##------A",
            "-M-D----CWW##------A",
            "-YE----P------------",
            "----N-E-----------#-",
            "-----BE-----------#-",
            "------E-----------#-",
            "-------P----------#-",
            "--------C----Z----#-",
            "---------WW#ZZ-----A",
            "-------------D-----E",
            "-------------#-X#--A",
            "-------------#-----A" };

    public static final int[] ANNTENA_LEVEL_BY_STATE = {
            //
            0, // online（現状使用されない）
            -1, // offline（現状使用されない）
            -2, // notWM（現状使用されない）
            -2, // AP無し（現状使用されない）
            -2, // wifi無効
            -2, // BT無効待ち
            -2, // BT有効待ち
            -2, // BT接続待ち
            -2, // wifi有効待ち
            -2, // supplicant有効待ち
            -2, // online待ち
            -2, // 再接続・wifi無効待ち
            -1, // スタンバイ待ち
            -2 // wifi無効待ち
    };

    public static final int[] WD_TEXT_ARRAY_BY_STATE = {
            //
            R.string.online, // online
            R.string.offline, // offline
            R.string.not_wm, // notWM
            R.string.no_ap, // AP無し
            R.string.wifi_off, // wifi無効
            R.string.processing1, // BT無効待ち
            R.string.processing1, // BT有効待ち
            R.string.processing1, // BT接続待ち
            R.string.processing2, // wifi有効待ち
            R.string.processing3, // supplicant有効待ち
            R.string.processing4, // online待ち
            R.string.processing, // 再接続・wifi無効待ち
            R.string.processing, // スタンバイ待ち
            R.string.processing // wifi無効待ち
    };

    public static final int[] WD_TEXT_ARRAY_BY_TRIGGER = {
            //
            -1, // 起動 
            -1, // リモート起動 + wifi ON
            -1, // wifi ON
            -1, // wifi OFF
            -1, // BT無効完了
            -1, // BT有効完了
            -1, // BT接続完了
            -1, // BC：W_Enabled
            -1, // BC：S_Completed
            -1, // online
            -1, // offline
            R.string.not_wm, // notWM
            -1, // AP無し（リトライ中の可能性があるので）
            R.string.watchdog_timeout, // watchdogタイムアウト
            -1, // スタンバイボタン押下
            R.string.standby_ok, // スタンバイ成功
            R.string.standby_ng, // スタンバイ失敗
            -1, // "BC：S_disconected
            -1, // BC：W_disanabling
            -1 // BC：W_disanabled
    };

    public static final int[] NOTIF_TEXT_ARRAY_BY_TRIGGER = {
            //
            -1, // 起動
            -1, // リモート起動 + wifi ON
            -1, // wifi ON
            -1, // wifi OFF
            -1, // BT無効完了
            -1, // BT有効完了
            -1, // BT接続完了
            -1, // BC：W_Enabled
            -1, // BC：S_Completed
            -1, // online
            -1, // offline
            R.string.not_wm_long, // notWM
            -1, // AP無し（リトライ中の可能性があるので）
            R.string.watchdog_timeout_long, // watchdogタイムアウト
            -1, // スタンバイボタン押下
            R.string.standby_ok_long, // スタンバイ成功
            R.string.standby_ng_long, // スタンバイ失敗
            -1, // "BC：S_disconected
            -1, // BC：W_disanabling
            -1 // BC：W_disanabled
    };

    public void init(boolean wifiEnable) {

        BackgroundService service = BackgroundService.getInstance();
        if (service == null) {
            Logger.e("service is null on StateMachine.init");
            return;
        }

        if (wifiEnable == false) {
            setState(STATE.WIFI_DISABLED);
        }
        else {
            ConnectivityState connectivityState = service.getConnectivityState();
            if (connectivityState == ConnectivityState.NONE) {
                setState(STATE.AP_NOT_FOUND); // SUPWAITでも良いが、watchdogを入れる必要がある
            }
            else {
                setState(STATE.OFFLINE);
            }
        }

        this.apConnFailedCount = 0;
        this.antennaLevel = -2;
        this.batteryLevel = -1;
        this.lockTextUntil = -1;
        this.notifyImageId = R.drawable.ntficon_wimax_gray_batt_na;
        this.triggerName = service.getString(R.string.processing);
        this.wdImageId = R.drawable.icon_wimax_gray_batt_na;
        this.wdText = service.getString(wifiEnable ? R.string.processing : R.string.wifi_off);
        this.lockRouterSwitchUntil = -1;
        this.comState = COM_TYPE.NA;
        this.comSetting = COM_TYPE.NA;
        this.wifiSpotEnabled = null;

    }

    private static HashMap<Character, Integer> stateCharMap = new HashMap<Character, Integer>();
    static {
        stateCharMap.put('0', 0);
        stateCharMap.put('1', 1);
        stateCharMap.put('2', 2);
        stateCharMap.put('3', 3);
        stateCharMap.put('4', 4);
        stateCharMap.put('5', 5);
        stateCharMap.put('6', 6);
        stateCharMap.put('7', 7);
        stateCharMap.put('8', 8);
        stateCharMap.put('9', 9);
        stateCharMap.put('a', 10);
        stateCharMap.put('b', 11);
        stateCharMap.put('c', 12);
        stateCharMap.put('d', 13);
    }

    public void perform(TRIGGER trigger) {
        BackgroundService service = BackgroundService.getInstance();
        if (service == null) {
            Logger.e("service is null on StateMachine.perform");
            return;
        }

        int currentInt = getState().ordinal();
        int triggerInt = trigger.ordinal();
        triggerName = trigger.name();
        char action = ACTION_ARRAY[currentInt].charAt(triggerInt);
        char nextState = STATE_ARRAY[currentInt].charAt(triggerInt);
        int wdTextInt = WD_TEXT_ARRAY_BY_TRIGGER[triggerInt];

        int notifyTextInt = NOTIF_TEXT_ARRAY_BY_TRIGGER[triggerInt];
        String notifyText = null;
        if (notifyTextInt != -1) {
            notifyText = service.getString(notifyTextInt);
        }

        Integer nextStateInt = null;

        // 設定による変換
        if (trigger == TRIGGER.BUTTON_BT_RESUME && nextState == 'y' && action == 'Y') {

            // BT有効化
            String bluetoothAddress = Const.getPrefBluetoothAddress(service);
            if (BluetoothHelper.isValidBluetoothAddress(bluetoothAddress)) {
                nextStateInt = STATE.BT_DISABLE_WAIT.ordinal(); // BT無効待ち
                action = 'M';
            }
            // Wifi有効化
            else {
                nextStateInt = STATE.WIFI_ENABLE_WAIT.ordinal(); // wifi有効待ち
                action = 'E';
            }
        }
        else if ((trigger == TRIGGER.WATCHDOG_TIMEOUT || trigger == TRIGGER.AP_NOT_FOUND) && nextState == 'z' && action == 'Z') {

            // リトライ
            Logger.i("apConnFailedCount = " + apConnFailedCount);
            if (apConnFailedCount++ < Const.getPrefApConnRetryLimit(service)) {
                nextStateInt = STATE.RECCONECT_WIFI_DISABLE_WAIT.ordinal(); // 再接続・wifi無効待ち
                action = 'D';

                // リトライ中のWATCHDOG_TIMEOUTはwdtextを更新したくない
                if (trigger == TRIGGER.WATCHDOG_TIMEOUT) {
                    wdTextInt = -1;
                }
            }

            // 上限到達
            else {
                apConnFailedCount = 0;

                // ロックが誘発される
                wdTextInt = R.string.no_ap;
                notifyText = service.getString(R.string.ap_not_found_long);

                // オンラインチェック失敗後にwifi無効化する場合
                if (Const.getPrefDisableWifiWhenApConnFailed(service)) {
                    nextStateInt = STATE.WIFI_DISABLE_WAIT.ordinal(); // wifi無効待ち
                    action = 'D';
                }
                else {
                    nextStateInt = STATE.AP_NOT_FOUND.ordinal(); // AP無し
                    action = '#';
                }
            }
        }
        else if (trigger == TRIGGER.STANDBY_OK && nextState == 'x' && action == 'X') {

            // 必要があればWiFiを停止
            if (Const.isPrefActionAfterSuspendWifiOFF(service)) {
                nextStateInt = STATE.WIFI_DISABLE_WAIT.ordinal(); // wifi無効待ち
                action = 'D';
                // WiFi停止後にサービスが停止される
            }
            // 状態変更
            else {
                nextStateInt = STATE.AP_NOT_FOUND.ordinal(); // AP無し
                action = '#';

                // サービスを遅延停止
                if (Const.isPrefActionAfterSuspendStopService(service)) {
                    service.stopServiceWithDelay();
                }
            }
        }

        Logger.i("StateMachine perform : " + currentInt + "," + triggerInt + "," + action + ","
                + (nextStateInt != null ? String.valueOf(nextStateInt) : nextState));

        if (nextStateInt == null) {
            nextStateInt = stateCharMap.get(nextState);
            if (nextStateInt == null) {
                return;
            }
        }

        if (action != '-') {

            // スタンバイからWifiOFF
            boolean stanbyToWifioff = prevState1 == STATE.STANDBY_WAIT && nextStateInt == STATE.WIFI_DISABLED.ordinal();

            // 状態更新
            setState(STATE.ordinalOf(nextStateInt));

            // 次状態がオンラインチェック状態でない場合は更新
            if (isOnlineCheckableState() == false) {
                antennaLevel = ANNTENA_LEVEL_BY_STATE[nextStateInt];
                notifyImageId = IconSelector.selectNotifyIcon(antennaLevel, batteryLevel, netState, comState);
                wdImageId = IconSelector.selectWdIcon(antennaLevel, batteryLevel, netState, comState);
            }

            // ロック開始
            if (wdTextInt != -1) {
                lockText();
                wdText = service.getString(wdTextInt);
                // 通知
                service.postNotify(notifyImageId, notifyText);
            }
            // ロック外
            else if (isTextLocked() == false) {
                // onlineの場合はバッテリー表示したいので、ここでは更新しない
                if (getState() != STATE.ONLINE) {
                    wdText = service.getString(WD_TEXT_ARRAY_BY_STATE[nextStateInt]);
                }
            }

            // UI更新
            reflesh(stanbyToWifioff);

            switch (action) {
            case 'E':
                service.enableWifi();
                break;

            case 'C':
                service.checkOnline();
                break;

            case 'W':
                service.checkOnlineWithDelay();
                break;

            case 'D':
                service.disableWifi();
                break;

            case 'S':
                service.standby();
                break;

            case 'P':
                service.waitSupplicantComplete();
                break;

            case 'A':
                service.wifiDisabled();
                break;

            case 'M':
                service.disableBT();
                break;

            case 'N':
                service.enableBT();
                break;

            case 'B':
                service.bluetoothResume();
                break;

            case '#':
                // do nothing
                break;
            }
        }
    }

    public void reflesh(boolean donotUpdateWidget) {
        BackgroundService service = BackgroundService.getInstance();
        if (service == null) {
            Logger.e("service is null on StateMachine.reflesh");
            return;
        }

        if (donotUpdateWidget == false) {
            DefaultWidgetProvider.update(service);
        }

        UIAct.postActivityInfo(wdImageId, wdText, triggerName, getState().name());
        boolean wifiEnabled = AndroidUtils.isWifiEnabled(service.getWifi());
        boolean suppCompleted = wifiEnabled && service.isSupplicantCompleted();
        UIAct.postActivityButton(isStableState(), isStableState(), wifiEnabled, suppCompleted, service.getEcoCharge(),
                comSetting, wifiSpotEnabled);
    }

    public void setStateToWifiRestart() {
        BackgroundService service = BackgroundService.getInstance();
        if (service == null) {
            Logger.e("service is null on StateMachine.setStateToWifiRestart");
            return;
        }

        batteryLevel = -1;
        apConnFailedCount = 0;
        setState(STATE.RECCONECT_WIFI_DISABLE_WAIT);
        wdText = service.getString(R.string.processing);
        wdImageId = R.drawable.icon_wimax_gray_batt_na;
        notifyImageId = R.drawable.ntficon_wimax_gray_batt_na;

        comState = COM_TYPE.NA;
        comSetting = COM_TYPE.NA;
        wifiSpotEnabled = null;

        service.disableWifi();
        reflesh(false);
    }

    public void onOnlineCheckFinished(boolean inetReachable, boolean routerReachable, RouterInfo routerInfo) {

        // オンラインチェック受け入れ可能状態でなければ抜ける
        if (isOnlineCheckAcceptableState() == false) {
            Logger.i("onOnlineCheckFinished skipped");
            return;
        }

        BackgroundService service = BackgroundService.getInstance();
        if (service == null) {
            Logger.e("service is null on StateMachine.onOnlineCheckFinished");
            return;
        }

        boolean prevOnline = netState == NETWORK_STATE.ONLINE;

        // antennaLevel解釈
        antennaLevel = -2;
        if (routerInfo != null) {
            antennaLevel = routerInfo.antennaLevel;
        }

        // 通信モード、Wi-Fiスポット使用
        comState = COM_TYPE.NA;
        if (routerInfo != null) {
            if (routerInfo.comState != COM_TYPE.NA) {
                comState = routerInfo.comState;
            }
            if (routerInfo.nad) {
                if (routerInfo.comSetting != COM_TYPE.NA) {
                    comSetting = routerInfo.comSetting;
                }
                if (routerInfo.wifiSpotEnabled != null) {
                    wifiSpotEnabled = routerInfo.wifiSpotEnabled;
                }
            }
        }

        // ConnectivityState.COMPLETE_WIFI以外ならAP無し
        ConnectivityState connectivityState = service.getConnectivityState();
        if (connectivityState != ConnectivityState.COMPLETE_WIFI) {
            netState = NETWORK_STATE.AP_NOT_FOUND;
        }
        else {
            if (routerReachable == false) {
                netState = NETWORK_STATE.NOT_WM_ROUTER;
            }
            else {
                // inetReachableかつ、アンテナ1以上の場合だけオンラインにする
                if (inetReachable && antennaLevel >= 1) {
                    netState = NETWORK_STATE.ONLINE;
                }
                else {
                    netState = NETWORK_STATE.OFFLINE;
                }
            }
        }

        boolean becomeOnline = false;
        if (prevOnline == false && netState == NETWORK_STATE.ONLINE) {
            becomeOnline = true;
        }

        // online状況
        TRIGGER nextTrigger;
        String notifyText;
        if (netState == NETWORK_STATE.AP_NOT_FOUND) {
            notifyText = service.getString(R.string.no_ap);
            nextTrigger = TRIGGER.AP_NOT_FOUND;
        }
        else {
            apConnFailedCount = 0;

            if (netState == NETWORK_STATE.NOT_WM_ROUTER) {
                notifyText = service.getString(R.string.not_wm);
                nextTrigger = TRIGGER.NOT_WM;
            }
            else if (netState == NETWORK_STATE.OFFLINE) {
                notifyText = service.getString(R.string.offline);
                nextTrigger = TRIGGER.OFFLINE;
            }
            else { // netState == NETWORK_STATE.ONLINE
                notifyText = service.getString(R.string.online);
                nextTrigger = TRIGGER.ONLINE;
            }
        }

        // 放電速度更新
        long now = System.currentTimeMillis();
        String remain = "";
        if (routerInfo != null && routerInfo.charging == false) {
            updateBattCalc(routerInfo.battery, now, service);
            remain = createRemainText(routerInfo.battery, averageRate);
        }
        else {
            stopBattCalc(service, now);
        }

        if (routerInfo != null) {
            // N/A以外なら、通信モード追加
            if (routerInfo.comState != COM_TYPE.NA) {
                notifyText += "(" + routerInfo.comState.toString() + ")";
            }
        }

        // ロック外ならwdTextを更新
        batteryLevel = -1;
        String battNotifyText = "";
        if (isTextLocked() == false) {
            if (routerInfo == null) {
                wdText = "batt=N/A";
                battNotifyText = wdText;
            }
            else {
                batteryLevel = routerInfo.battery;
                if (MyStringUtlis.isEmpty(remain)) {
                    wdText = "batt=" + routerInfo.getBatteryText();
                    battNotifyText = wdText;
                }
                else {
                    wdText = routerInfo.getBatteryText() + remain;
                    battNotifyText = "batt=" + wdText;
                }
            }
        }
        notifyText += ", " + battNotifyText;

        // アンテナテキスト
        int max = 6; // WMシリーズはmax6
        if (routerInfo != null && routerInfo.nad) {
            if (comState == COM_TYPE.HIGH_SPEED) {
                max = 4;
            }
            if (comState == COM_TYPE.NO_LIMIT || comState == COM_TYPE.WIFI_SPOT) {
                max = 5;
            }
        }
        if (0 <= antennaLevel && antennaLevel <= 6) {
            notifyText += ", ant=" + antennaLevel + "/" + max;
        }
        else {
            notifyText += ", ant=N/A";
        }

        // アイコン作成
        notifyImageId = IconSelector.selectNotifyIcon(antennaLevel, batteryLevel, netState, comState);
        wdImageId = IconSelector.selectWdIcon(antennaLevel, batteryLevel, netState, comState);

        // オンラインチェック完了をコールバック
        service.onOnlineCheckComplete(nextTrigger, becomeOnline);

        // ロック外なら通知。本来なら、再接続する場合はperformに任せたいが、画面OFFでnoAPになると通知が更新されなくなってしまう。
        if (isTextLocked() == false) {
            service.postNotify(notifyImageId, notifyText);
        }

        // UI更新
        reflesh(false);
    }

    protected static String createRemainText(int battery, double rate) {

        if (rate == 0) {
            return "";
        }

        double remain = (double) battery / rate;
        String remainText = "(";
        if (remain >= 100) {
            remainText += "99h)";
        }
        else if (remain >= 10) {
            remainText += (int) remain + "h)";
        }
        else if (remain >= 1.6) { // 96
            remainText += (Math.round(remain * 10.0) / 10.0) + "h)";
        }
        else if (remain > 0) {
            remainText += (int) Math.round(remain * 60.0) + "m)";
        }
        else {
            remainText = "";
        }

        return remainText;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized void startBattCalc(Context context) {
        currentRate = -1;
        averageRate = -1;
        startTime = -1;
        startBattLevel = -1;
        prevUpdateTime = -1;

        // 復元
        if (context != null) {
            pastRates = Const.getPrefBattRates(context);
        }

        // averageRate更新
        updateAverage(null);
    }

    protected synchronized void updateBattCalc(int battery, long now, Context context) {

        if (startBattLevel == -1) {
            startBattCalc(context);
            startBattLevel = battery;
        }
        else {
            if (startTime == -1 && startBattLevel > battery) {
                startTime = now;
                startBattLevel = battery;
                prevUpdateTime = now;
            }
            else if (startTime != -1) {

                long deltaTimeMs = now - startTime;
                int deltaBattLevel = startBattLevel - battery;
                if (deltaTimeMs > 0 && deltaBattLevel > 0) {
                    currentRate = (double) deltaBattLevel / deltaTimeMs * 1000 * 60 * 60;

                    // averageRate更新
                    updateAverage(currentRate);

                    Logger.v("startBattLevel=" + startBattLevel + ", currentRate=" + MyStringUtlis.round3(currentRate)
                            + ", averageRate=" + MyStringUtlis.round3(averageRate) + ", pastRates=" + pastRates.size());

                    // 前回保存から15分経過していれば保存
                    if (now - prevUpdateTime > BATT_SAVE_THRESHOLD) {
                        savePastRates(context, now);
                    }
                }
            }
        }
    }

    public synchronized void stopBattCalc(Context context) {
        stopBattCalc(context, System.currentTimeMillis());
    }

    public synchronized void stopBattCalc(Context context, long now) {

        if (startTime != -1) {
            savePastRates(context, now);
        }

        currentRate = -1;
        averageRate = -1;
        startTime = -1;
        startBattLevel = -1;
        prevUpdateTime = -1;
    }

    protected void savePastRates(Context context, long now) {

        if (BATT_RATE_THRESHOLD_MIN <= currentRate && currentRate <= BATT_RATE_THRESHOLD_MAX) {
            prevUpdateTime = now;

            int size = pastRates.size();
            if (size >= RATE_SIZE) {
                pastRates = pastRates.subList(size - RATE_SIZE + 1, size);
            }
            pastRates.add(currentRate);

            // 保存
            if (context != null) {
                Const.updatePrefBattRates(context, pastRates);
            }
        }
    }

    protected void updateAverage(Double currentRate) {
        int size = pastRates.size();
        averageRate = 0;
        if (currentRate != null && BATT_RATE_THRESHOLD_MIN <= currentRate.doubleValue()
                && currentRate.doubleValue() <= BATT_RATE_THRESHOLD_MAX) {
            size++;
            averageRate = currentRate;
        }
        if (size > 0) {
            // averageRate更新
            for (Double d : pastRates) {
                averageRate += d;
            }
            averageRate /= size;
        }
    }

    public static void resetBatt(Context context) {
        BackgroundService service = BackgroundService.getInstance();
        if (service != null) {
            StateMachine sm = service.getStateMachine();
            sm.pastRates = new ArrayList<Double>();
            sm.startTime = -1; // saveさせないために、事前に-1にする
            sm.stopBattCalc(context);
        }
        Const.updatePrefBattRates(context, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void resetTextLock() {
        lockTextUntil = -1;
    }

    public void lockText() {
        lockTextUntil = System.currentTimeMillis() + Const.WD_TEXT_LOCK_TIME_MS;
    }

    public boolean isTextLocked() {
        return lockTextUntil > System.currentTimeMillis();
    }

    public void resetRouterSwitchLock() {
        lockRouterSwitchUntil = -1;
    }

    public void lockRouterSwitch() {
        lockRouterSwitchUntil = System.currentTimeMillis() + Const.ROUTER_SWITCH_LOCK_TIME_MS;
    }

    public boolean isRouterSwitchLocked() {
        return lockRouterSwitchUntil > System.currentTimeMillis();
    }

    public int getWdImageId() {
        return wdImageId;
    }

    public String getWdText() {
        return wdText;
    }

    public boolean isSupplicantEnableWaitState() {
        return current == STATE.NOT_WM || current == STATE.AP_NOT_FOUND || current == STATE.SUPPLICANT_ENABLE_WAIT;
    }

    public boolean isStandbyWaitState() {
        return current == STATE.STANDBY_WAIT;
    }

    public boolean isStableState() {
        return current == STATE.ONLINE || current == STATE.OFFLINE || current == STATE.NOT_WM || current == STATE.AP_NOT_FOUND
                || current == STATE.WIFI_DISABLED;
    }

    public boolean isOnlineCheckableState() {
        return current == STATE.ONLINE || current == STATE.OFFLINE;
    }

    private boolean isOnlineCheckAcceptableState() {
        return current == STATE.ONLINE || current == STATE.OFFLINE || current == STATE.NOT_WM || current == STATE.AP_NOT_FOUND
                || current == STATE.ONLINE_WAIT;
    }

    public boolean isWmReachableState() {
        return current == STATE.ONLINE || current == STATE.OFFLINE || current == STATE.ONLINE_WAIT;
    }

    private void setState(STATE state) {
        prevState1 = current;
        current = state;
    }

    public STATE getState() {
        return current;
    }

    public boolean isOnline() {
        return netState == NETWORK_STATE.ONLINE; // NETWORK_STATE.NOT_WM_ROUTERはオンラインにしない 
    }

    public void setNetStateToNoAP() {
        netState = NETWORK_STATE.AP_NOT_FOUND;
    }

    public void setOfflineTemporarily() {
        netState = NETWORK_STATE.OFFLINE;
        batteryLevel = -1;
        current = STATE.OFFLINE;
        wdImageId = R.drawable.icon_wimax_white_batt_na;
        comState = COM_TYPE.NA;
        comSetting = COM_TYPE.NA;
        wifiSpotEnabled = null;

        // UI更新
        reflesh(false);
    }

    public boolean isComSettingHS() {
        return comSetting == COM_TYPE.HIGH_SPEED;
    }

    public boolean isWifiSpotEnabled() {
        return wifiSpotEnabled != null && wifiSpotEnabled == true;
    }

    //	public int getNotifyImageId() {
    //		return notifyImageId;
    //	}
    //	public String getNotifyText() {
    //		return notifyText;
    //	}
}
