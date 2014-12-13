package com.appspot.afnf4199ga.twawm;

import java.net.InetAddress;

import com.appspot.afnf4199ga.twawm.app.BackgroundService;
import com.appspot.afnf4199ga.twawm.app.BackgroundService.ConnectivityState;
import com.appspot.afnf4199ga.twawm.router.RouterControl;
import com.appspot.afnf4199ga.twawm.router.RouterControlByHttp;
import com.appspot.afnf4199ga.twawm.router.RouterInfo;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;

public class OnlineChecker extends Thread {

    private boolean executing = false;
    private long delay_ms;
    private Boolean inetReachable;
    private Boolean routerReachable;
    private RouterInfo routerInfo;

    public OnlineChecker(long delay_ms) {
        this.delay_ms = delay_ms;
    }

    @Override
    public void run() {
        try {
            executing = true;
            Thread.sleep(delay_ms);

            final BackgroundService service = BackgroundService.getInstance();
            if (service == null) {
                Logger.e("service is null on OnlineChecker.run");
                return;
            }

            // スクリーンがオンの場合、またはwakelockされている場合
            if (service.isScreenOn() || service.isWakeLocked()) {

                // 通信が無効ならスキップ
                ConnectivityState connectivityState = service.getConnectivityState();
                if (connectivityState == ConnectivityState.NONE) {
                    Logger.w("OnlineChecker ConnectivityState.NONE");
                    service.getStateMachine().onOnlineCheckFinished(false, false, null);
                }
                else {
                    // ルーター情報取得
                    if (connectivityState == ConnectivityState.COMPLETE_WIFI) {
                        RouterControl.execFetchInfo(this);
                    }
                    else {
                        onRouterInfoFetched(-1, null);
                    }

                    // Watchdoc起動
                    new Thread(new Runnable() {
                        public void run() {
                            AndroidUtils.sleep(Const.getPrefOnlineCheckDnsTimeoutMs(service));
                            onCompleteParticialy(TYPE.WATCHDOG);
                        }
                    }).start();

                    // 外部ホスト名検索
                    String hostname = HostnameList.getNext();
                    try {
                        InetAddress address = InetAddress.getByName(hostname);
                        inetReachable = false;
                        // Watchdoc側で停止されているとexecuting == falseになる
                        if (executing == false) {
                            Logger.i("dns lookup timeout, name=" + hostname);
                        }
                        else if (address != null) {
                            inetReachable = true;
                        }
                    }
                    catch (Throwable e) {
                        inetReachable = false;
                        Logger.i("dns lookup failed, name=" + hostname);
                    }

                    // 外部ホスト名検索のコールバック
                    onCompleteParticialy(TYPE.DNS);
                }
            }
            else {
                Logger.i("screenOff, OnlineCheck skipped");
            }
        }
        catch (InterruptedException e) {
            // interruptされるのは正常
        }
        catch (Throwable e) {
            // do nothing
            Logger.e("OnlineChecker error", e);
        }
    }

    enum TYPE {
        ROUTER, DNS, WATCHDOG
    }

    private synchronized void onCompleteParticialy(TYPE type) {
        //Logger.v("OnlineChecker onCompleteParticialy, type=" + type + ", exe=" + executing);

        // 外部ホスト名検索キャンセル
        if (type == TYPE.WATCHDOG && inetReachable == null) {
            inetReachable = false;
        }

        // 両方終わった場合
        if (routerReachable != null && inetReachable != null) {

            // 外部から停止されていた場合、executing==falseとなる
            if (executing) {
                executing = false;
                Logger.i("OnlineChecker finished, inet=" + inetReachable + ", router=" + routerReachable);

                BackgroundService service = BackgroundService.getInstance();
                if (service == null) {
                    Logger.e("service is null on OnlineChecker.onCompleteParticialy");
                    return;
                }
                service.getStateMachine().onOnlineCheckFinished(inetReachable, routerReachable, routerInfo);
            }
        }
    }

    public void onRouterInfoFetched(int ret, RouterInfo newRouterInfo) {
        routerReachable = ret == RouterControlByHttp.CTRL_OK;
        routerInfo = newRouterInfo;
        onCompleteParticialy(TYPE.ROUTER);
    }

    public boolean isExecuting() {
        return executing;
    }

    public void stopThread() {
        if (executing) {
            executing = false;
            interrupt();
        }
    }
}