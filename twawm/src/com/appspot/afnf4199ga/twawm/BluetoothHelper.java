package com.appspot.afnf4199ga.twawm;

import java.lang.reflect.Method;
import java.util.Locale;

import net.afnf.and.twawm2.R;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.appspot.afnf4199ga.twawm.TwawmUtils.BT_RESUME_TYPE;
import com.appspot.afnf4199ga.twawm.app.BackgroundService;
import com.appspot.afnf4199ga.twawm.app.MainActivity;
import com.appspot.afnf4199ga.twawm.app.MainActivity.ACTIVITY_FLAG;
import com.appspot.afnf4199ga.twawm.app.UIAct;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class BluetoothHelper {

    protected BackgroundService service;
    protected BluetoothAdapter bt = null;
    protected String bluetoothAddress = null;
    protected BluetoothDevice remoteDevice = null;
    protected boolean btAlreadyEnabled = false;
    protected boolean success = false;
    protected boolean executing = false;
    protected BluetoothSocket bluetoothSocket = null;
    protected BroadcastReceiver discoveryBcastReceiver = null;
    protected boolean discovering = false;

    public BluetoothHelper(BackgroundService service) {
        this.service = service;

        bluetoothAddress = Const.getPrefBluetoothAddress(service);
        btAlreadyEnabled = false;
        success = false;
        executing = true;
        bt = BluetoothAdapter.getDefaultAdapter();
    }

    public void disable() {
        if (isValidState() == false) {
            return;
        }

        if (bt.isEnabled()) {
            btAlreadyEnabled = true;

            // 無効化実行
            Logger.i("Bluetooth enabled, disabling");
            bt.disable();
        }
        else {
            Logger.i("Bluetooth originally disabled");
            service.onBluetoothDisabled();
        }
    }

    public void enable() {
        if (isValidState() == false) {
            return;
        }

        if (bt.isEnabled()) {
            // Bluetoothを再起動しない場合
            if (Const.isPrefBtRestartType(service) == false) {
                btAlreadyEnabled = true;
                Logger.i("Bluetooth originally enabled");
            }
            else {
                Logger.w("Bluetooth disabling failed?");
            }
            service.onBluetoothEnabled();
        }
        else {
            // Bluetoothを再起動しない場合
            if (Const.isPrefBtRestartType(service) == false) {
                // BT有効化（UIスレッドから呼ぶとブロックされるよう）
                Logger.i("Bluetooth enabling");
                bt.enable();
            }
            else {
                // Bluetooth有効化リクエスト
                Logger.i("Bluetooth enabling manually");
                MainActivity.startActivity(service, ACTIVITY_FLAG.BT_ENABLING);
            }
        }
    }

    public void connect() {
        if (isValidState() == false) {
            return;
        }

        if (bt.isEnabled() == false) {
            Logger.w("Bluetooth enabling failed?");
            service.onBluetoothConnected(false);
            return;
        }

        // BT有効
        startDiscoveringAndConnectingByThread();
    }

    protected void startDiscoveringAndConnectingByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startDiscoveringAndConnecting();
            }
        }).start();
    }

    protected void startDiscoveringAndConnecting() {
        if (isValidState() == false) {
            return;
        }

        BackgroundService service = BackgroundService.getInstance();

        // Discoveryを実行しない場合
        if (Const.isPrefBtDiscoveringType(service) == false) {
            startConnecting();
        }

        // Discoveryを実行する場合
        else {
            // broadcastReceiver構築
            discoveryBcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    // 発見
                    if (AndroidUtils.isActionEquals(intent, BluetoothDevice.ACTION_FOUND)) {
                        try {
                            BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            if (foundDevice != null) {
                                // アドレスが一致した場合
                                String addr = foundDevice.getAddress();
                                if (MyStringUtlis.isEmpty(addr) == false
                                        && MyStringUtlis.eqauls(addr.toUpperCase(Locale.US), bluetoothAddress)) {
                                    Logger.i("Bluetooth device found, discovery canceled");
                                    // 停止して接続
                                    remoteDevice = foundDevice;
                                    discovering = false;
                                    bt.cancelDiscovery();
                                    startConnectingByThread();
                                }
                            }
                        }
                        catch (Throwable e) {
                            Logger.w("Bluetooth BluetoothDevice.ACTION_FOUND failed", e);
                        }
                    }
                    // 終了
                    else if (AndroidUtils.isActionEquals(intent, BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                        Logger.i("Bluetooth discovery finished");
                        if (discovering) {
                            discovering = false;
                            startConnectingByThread();
                        }
                    }
                }
            };

            // broadcastReceiver登録
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            service.registerReceiver(discoveryBcastReceiver, filter);

            // Discovery開始
            try {
                bt.cancelDiscovery();
                discovering = bt.startDiscovery();
                if (discovering == false) {
                    Logger.w("Bluetooth startDiscovery failed");
                    startConnecting();
                }
            }
            catch (Throwable e) {
                Logger.w("Bluetooth startDiscovery failed, e=" + e.toString());
                startConnecting();
            }
        }
    }

    protected void startConnectingByThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startConnecting();
            }
        }).start();
    }

    @SuppressLint({ "DefaultLocale", "NewApi" })
    protected void startConnecting() {
        if (isValidState() == false) {
            return;
        }

        BackgroundService service = BackgroundService.getInstance();

        if (discoveryBcastReceiver != null) {
            try {
                service.unregisterReceiver(discoveryBcastReceiver);
                discoveryBcastReceiver = null;
            }
            catch (Throwable e) {
            }
        }

        // リトライ回数設定
        BT_RESUME_TYPE btResumeType = Const.getPrefBtResumeType(service);
        int retry = TwawmUtils.getBtRetryCount(btResumeType);
        Logger.i("Bluetooth startConnecting, type=" + btResumeType.name() + ", retry=" + retry);

        for (int i = 0; i < retry; i++) {
            Logger.i("tring=" + i);

            try {
                // 必要があればremoteDevice取得
                if (remoteDevice == null) {
                    remoteDevice = bt.getRemoteDevice(bluetoothAddress);
                }

                // 初回はSSP
                if (i == 0) {
                    bluetoothSocket = remoteDevice.createRfcommSocketToServiceRecord(Const.BLUETOOTH_UUID);
                }

                // 2回目はSDP lookup無しでやってみる
                else if (i == 1) {

                    // bluetoothSocketが取れるまでループを回す
                    int channel = 0;
                    try {
                        Method m = remoteDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
                        while (bluetoothSocket == null && channel < 10) {
                            bluetoothSocket = (BluetoothSocket) m.invoke(remoteDevice, ++channel);
                        }
                    }
                    catch (Throwable e) {
                        Logger.i(" error=" + e.toString());
                    }

                    if (bluetoothSocket != null) {
                        Logger.i(" channel=" + channel);
                    }
                    // 取れなければSSPを使う
                    else {
                        Logger.i(" using SSP");
                        bluetoothSocket = remoteDevice.createRfcommSocketToServiceRecord(Const.BLUETOOTH_UUID);
                    }
                }

                // 3回目はinsecure
                else if (i == 2) {

                    // createInsecureRfcommSocketToServiceRecordはAPI10から
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
                        bluetoothSocket = remoteDevice.createInsecureRfcommSocketToServiceRecord(Const.BLUETOOTH_UUID);
                    }
                    // API9以下ならcreateInsecureRfcommSocketを使う
                    else {
                        // bluetoothSocketが取れるまでループを回す
                        Method m = remoteDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[] { int.class });
                        int channel = 0;
                        while (bluetoothSocket == null && channel < 30) {
                            bluetoothSocket = (BluetoothSocket) m.invoke(remoteDevice, ++channel);
                        }
                        Logger.i(" channel=" + channel);
                    }
                }

                // 見つからない場合は次のループへ
                if (bluetoothSocket == null) {
                    Logger.w(" bluetoothSocket is null, continue");
                    continue;
                }
            }
            catch (Throwable e1) {
                Logger.w(" bluetoothSocket creation failed, e=" + e1.toString());
                // 次へ
                continue;
            }

            ClosingThread closingThread = new ClosingThread();
            try {

                // Discovery中断
                // isDiscoveringは不必要とのこと(http://developer.android.com/intl/ja/guide/topics/connectivity/bluetooth.html)
                bt.cancelDiscovery();

                // 接続中断スレッド開始
                closingThread.start();

                // 接続開始
                bluetoothSocket.connect();

                Logger.i(" pairing succeeded, continue");
            }
            catch (Throwable e) {
                // unable to startなら警告
                String exception = e.toString().toLowerCase();
                if (exception.indexOf("unable to start service discovery") != -1) {
                    UIAct.toast(service.getString(R.string.bt_error_need_to_reboot));
                }
                Logger.i(" resume complete?, e=" + exception);
            }
            finally {
                try {
                    closingThread.interrupt();
                    synchronized (BluetoothHelper.class) {
                        if (bluetoothSocket != null) {
                            Logger.i(" closing bluetoothSocket on finally");
                            bluetoothSocket.close();
                            bluetoothSocket = null;
                        }
                    }
                }
                catch (Throwable e2) {
                    Logger.w(" finalize error", e2);
                }
            }
        }

        // 終了処理
        try {

            // ここまで来た場合、全て成功として扱う
            success = true;

            long beforeDisabling = System.currentTimeMillis();

            // 無効化前のコールバック
            service.onBluetoothConnectedBeforeDisabling();

            // 元々有効な場合、無効化しない
            if (btAlreadyEnabled) {
                Logger.i("Bluetooth keep enabled");
            }
            // 無効化
            else {
                // BT無効化
                Logger.i("Bluetooth disabling");
                bt.disable();
            }

            // NAD11ならディレイを伸ばす
            long afterDisabling = System.currentTimeMillis();
            long diff = afterDisabling - beforeDisabling;
            long wait = Const.getPrefWaitAfterResumeMs(service);
            final long delay = Const.NOTIFY_DELAY_AFTER_BT_DISABLING + Math.max(0, wait - diff);

            // 遅延コールバック
            new Thread(new Runnable() {
                @Override
                public void run() {

                    // 遅延
                    AndroidUtils.sleep(delay);

                    // サービス取得
                    BackgroundService service = BackgroundService.getInstance();
                    if (service == null) {
                        Logger.e("service is null on BluetoothHelper.startConnecting().callback");
                        return;
                    }

                    // コールバック
                    service.onBluetoothConnected(success);
                }
            }).start();
        }
        catch (Throwable e) {
            Logger.w("Bluetooth connecting failed", e);
        }
    }

    protected class ClosingThread extends Thread {
        public void run() {
            try {
                Thread.sleep(Const.getPrefBtConnectionTimeoutMs(service));

                synchronized (BluetoothHelper.class) {
                    if (bluetoothSocket != null) {
                        try {
                            Logger.i(" closing bluetoothSocket from thread");
                            // APIドキュメントのconnect部分より : "close() can be used to abort this call from another thread."
                            bluetoothSocket.close();
                            bluetoothSocket = null;
                        }
                        catch (Throwable e) {
                        }
                    }
                }
            }
            catch (InterruptedException t) {
                // interruptされるのは正常
            }
        }
    }

    public void finish() {
        executing = false;

        if (btAlreadyEnabled == false) {
            BackgroundService service = BackgroundService.getInstance();
            if (service == null) {
                Logger.e("service is null on BluetoothHelper.finish");
                return;
            }
            // broadcastReceiver解除
            if (discoveryBcastReceiver != null) {
                service.unregisterReceiver(discoveryBcastReceiver);
                discoveryBcastReceiver = null;
            }
        }
    }

    private boolean isValidState() {

        BackgroundService service = BackgroundService.getInstance();
        if (service == null) {
            Logger.e("service is null on BluetoothHelper");
            return false;
        }

        if (bt == null) {
            Logger.e("Bluetooth unnavigable");
            service.onBluetoothConnected(false);
            return false;
        }

        if (executing == false) {
            Logger.e("executing is false on BluetoothHelper");
            return false;
        }

        return true;
    }

    public static boolean isValidBluetoothAddress(String address) {
        if (MyStringUtlis.isEmpty(address) == false) {
            address = address.toUpperCase(Locale.US);
            if (BluetoothAdapter.checkBluetoothAddress(address)) {
                return true;
            }
        }
        return false;
    }
}
