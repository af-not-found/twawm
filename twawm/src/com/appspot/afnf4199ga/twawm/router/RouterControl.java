package com.appspot.afnf4199ga.twawm.router;

import net.afnf.and.twawm2.R;

import com.appspot.afnf4199ga.twawm.BluetoothHelper;
import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.twawm.OnlineChecker;
import com.appspot.afnf4199ga.twawm.app.BackgroundService;
import com.appspot.afnf4199ga.twawm.app.MainActivity;
import com.appspot.afnf4199ga.twawm.app.MainActivity.ACTIVITY_FLAG;
import com.appspot.afnf4199ga.twawm.app.UIAct;
import com.appspot.afnf4199ga.twawm.router.RouterControlByHttp.CTRL;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class RouterControl {

    public static void execStandby() {
        Logger.i("RouterControl execStandby");

        Thread thread = new Thread() {
            public void run() {

                BackgroundService service = BackgroundService.getInstance();
                if (service == null) {
                    Logger.e("service is null on RouterControl.execStandby");
                    return;
                }

                // スタンバイ実行
                RouterInfo routerInfo = new RouterInfo();
                int ret = RouterControlByHttp.exec(service, CTRL.STANDBY, routerInfo);

                // 失敗したらリトライ
                if (ret == RouterControlByHttp.CTRL_FAILED) {
                    Logger.i("router STANDBY failed, ret=" + ret + ", retring...");
                    RouterControlByHttp.resetPrevious();
                    ret = RouterControlByHttp.exec(service, CTRL.STANDBY, routerInfo);
                }

                Logger.i("RouterControl execStandby, ret=" + ret);
                service.onStandbyComplete(ret == RouterControlByHttp.CTRL_OK);
            }
        };
        thread.start();
    }

    public static void execFetchInfo(OnlineChecker checker) {
        // ルーター情報取得
        new FetchThread(checker).start();
    }

    static class FetchThread extends Thread {
        OnlineChecker callbackTarget;

        public FetchThread(OnlineChecker checker) {
            this.callbackTarget = checker;
        }

        public void run() {

            BackgroundService service = BackgroundService.getInstance();
            if (service == null) {
                Logger.e("service is null on RouterControl.FetchThread");
                return;
            }

            RouterInfo routerInfo = new RouterInfo();
            int ret = RouterControlByHttp.exec(service, CTRL.GET_INFO, routerInfo);

            // 正常
            switch (ret) {
            case RouterControlByHttp.CTRL_OK:
                Logger.i("router GET_INFO, ret=" + ret + ", " + routerInfo);

                // 有効なアドレスで、かつ設定と異なる場合
                if (BluetoothHelper.isValidBluetoothAddress(routerInfo.bluetoothAddress)
                        && MyStringUtlis.eqauls(routerInfo.bluetoothAddress, Const.getPrefBluetoothAddress(service)) == false) {
                    // BTアドレス更新
                    Logger.v("updatePrefBluetoothAddress called");
                    Const.updatePrefBluetoothAddress(service, routerInfo.bluetoothAddress);
                    // toast
                    UIAct.toast(service.getString(R.string.bt_addr_saved));
                }
                break;

            // パスワード未設定、または認証失敗
            case RouterControlByHttp.CTRL_PASS_NOT_INITIALIZED:
            case RouterControlByHttp.CTRL_UNAUTHORIZED:
                // ウィザード起動
                if (Const.getPrefStartWizardAutomatically(service)) {
                    MainActivity.startActivity(service, ACTIVITY_FLAG.INIT_WIZARD);
                    // callbackしない
                    return;
                }
                // ウィザードを起動しない場合は、breakしない→ログ出力してcallback

                // 異常
            default:
                Logger.w("router GET_INFO failed, ret=" + ret);
            }

            // callbackを行う
            callbackTarget.onRouterInfoFetched(ret, routerInfo);
        }
    }

    public static void execWimaxReconnection() {
        Logger.i("RouterControl execWimaxReconnection");

        Thread thread = new Thread() {
            public void run() {

                try {
                    BackgroundService service = BackgroundService.getInstance();
                    if (service == null) {
                        Logger.e("service is null on RouterControl.execWimaxReconnection");
                        return;
                    }

                    boolean nad11 = RouterControlByHttp.isNad();
                    int ret;
                    RouterInfo routerInfo = new RouterInfo();

                    // 切断通知
                    UIAct.toast(service.getString(R.string.wimax_disconnecting));

                    // 切断実行
                    ret = RouterControlByHttp.exec(service, nad11 ? CTRL.NAD_WIMAX_RECONN : CTRL.WIMAX_DISCN, routerInfo);
                    if (ret != RouterControlByHttp.CTRL_OK) {
                        UIAct.toast(service.getString(R.string.failed));
                    }

                    // 一時的にオフラインにする
                    service.getStateMachine().setOfflineTemporarily();

                    // 待ち
                    AndroidUtils.sleep(2000);

                    // 通知
                    UIAct.toast(service.getString(R.string.wimax_connecting));

                    // WM3800Rの場合だけ再接続実行
                    if (nad11 == false) {

                        // ※INFOBTN系と通常画面はセッションが分離されているので、resetPreviousが必要
                        RouterControlByHttp.resetPrevious();

                        // 接続実行
                        ret = RouterControlByHttp.exec(service, CTRL.WIMAX_CONN, routerInfo);
                        if (ret != RouterControlByHttp.CTRL_OK) {
                            UIAct.toast(service.getString(R.string.failed));
                        }
                        RouterControlByHttp.resetPrevious();
                    }

                    // オンラインチェックを遅延実行
                    service.startOnlineCheck(Const.ONLINE_CHECK_DELAY_AFTER_CTRL);
                }
                catch (Throwable e) {
                    Logger.w("RouterControl execWimaxReconnection failed", e);
                }
            }
        };
        thread.start();
    }

    public static void execRouterCtrl(final CTRL ctrl) {
        Logger.i("RouterControl execRouterCtrl");

        Thread thread = new Thread() {
            public void run() {

                try {
                    BackgroundService service = BackgroundService.getInstance();
                    if (service == null) {
                        Logger.e("service is null on RouterControl.execRouterCtrl");
                        return;
                    }

                    // toast
                    if (ctrl == CTRL.REBOOT_WM) {
                        UIAct.toast(service.getString(R.string.reboot_wmrouter));
                    }

                    // 実行
                    RouterInfo routerInfo = new RouterInfo();
                    int ret = RouterControlByHttp.exec(service, ctrl, routerInfo);
                    if (ret != RouterControlByHttp.CTRL_OK) {
                        UIAct.toast(service.getString(R.string.failed));
                    }

                    // 一時的にオフラインにする
                    service.getStateMachine().setOfflineTemporarily();

                    // オンラインチェックを遅延実行→AP無しになるのを回避するため
                    service.startOnlineCheck(Const.ONLINE_CHECK_DELAY_AFTER_CTRL);
                }
                catch (Throwable e) {
                    Logger.w("RouterControl execRouterReboot failed", e);
                }
            }
        };
        thread.start();
    }
}
