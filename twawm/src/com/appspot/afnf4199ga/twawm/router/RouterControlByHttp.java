package com.appspot.afnf4199ga.twawm.router;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.Context;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.twawm.router.MyHttpClient.AuthType;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class RouterControlByHttp {

    public static final int CTRL_OK = 0;
    public static final int CTRL_ROUTER_IP_IS_NOT_SITE_LOCAL = 12;
    public static final int CTRL_PASS_NOT_INITIALIZED = 20;
    public static final int CTRL_UNAUTHORIZED = 21;
    public static final int CTRL_STANBY_FAILED = 33;

    protected static RouterInfo prevRouterInfo = null;
    protected static long lastUpdate;
    protected static HashMap<String, String> hiddenMap = new HashMap<String, String>();

    public static enum CTRL {
        GET_INFO, GET_INFO_FORCE_RMTMAIN, GET_INFO_FORCE_INFOBTN, STANDBY, WIMAX_DISCN, WIMAX_CONN, REBOOT_WM, CHECK_WM
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void resetPrevious() {
        hiddenMap.clear();
        lastUpdate = 0;
    }

    protected static boolean hasBluetooth(RouterInfo routerInfo) {
        if (routerInfo != null && routerInfo.routerName != null) {
            String tmp = routerInfo.routerName.toLowerCase(Locale.US);
            if (tmp.length() >= 5 && tmp.indexOf("wm3500r") == -1 && tmp.indexOf("wm3600r") == -1) {
                return true;
            }
        }
        return false;
    }

    protected static boolean canStandyByRmtMain(RouterInfo routerInfo) {
        if (routerInfo == null || (routerInfo.hasStandbyButton && routerInfo.rmtMain)) {
            return true;
        }
        else {
            return false;
        }
    }

    protected static boolean hasStandbyButton(RouterInfo routerInfo) {
        return routerInfo != null && routerInfo.hasStandbyButton;
    }

    public static boolean isFirmwareVersionOld() {
        return hasStandbyButton(prevRouterInfo) && canStandyByRmtMain(prevRouterInfo);
    }

    protected static boolean isGetInfoCtrl(CTRL ctrl) {
        return ctrl == CTRL.GET_INFO || ctrl == CTRL.GET_INFO_FORCE_RMTMAIN || ctrl == CTRL.GET_INFO_FORCE_INFOBTN;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("DefaultLocale")
    public static int exec(Context context, CTRL ctrl, RouterInfo routerInfo) {

        boolean isGetInfo = isGetInfoCtrl(ctrl);

        MyHttpClient httpClient = null;
        try {
            // ルーターIPアドレスを取得
            String routerIpAddr = MyHttpClient.getRouterIpAddr(new InetLookupWrappter(), Const.getPrefApIpAddr(context));
            if (routerIpAddr == null) {
                return 11;
            }
            // ルーターIPアドレスがPrivateIPでない
            else if (routerIpAddr == MyHttpClient.NOT_SITE_LOCAL_ADDR) {
                return CTRL_ROUTER_IP_IS_NOT_SITE_LOCAL; //12
            }

            // httpClient作成
            httpClient = MyHttpClient.createClient(context);

            // ルーター情報取得、またはスタンバイ実行時のセッションID再取得、またはスタンバイボタン未発見時の情報再取得
            boolean communicated = false;
            long now = System.currentTimeMillis();
            if (ctrl != CTRL.CHECK_WM
                    && (isGetInfo || now - lastUpdate >= Const.ROUTER_SESSION_TIMEOUT || hasStandbyButton(prevRouterInfo) == false)) {

                try {
                    // アクセス先決定
                    String path = null;
                    if (ctrl == CTRL.WIMAX_CONN) {
                        path = Const.ROUTER_URL_WIMAX_CONN_INFOBTN;
                    }
                    else {
                        if (ctrl == CTRL.GET_INFO_FORCE_RMTMAIN) {
                            routerInfo.rmtMain = true;
                        }
                        else if (ctrl == CTRL.GET_INFO_FORCE_INFOBTN) {
                            routerInfo.rmtMain = false;
                        }
                        else {
                            // 起動直後はtrue
                            routerInfo.rmtMain = canStandyByRmtMain(prevRouterInfo);
                        }
                        path = routerInfo.rmtMain ? Const.ROUTER_URL_INFO_RMTMAIN : Const.ROUTER_URL_INFO_INFOBTN;
                    }
                    Logger.i("RouterControlByHttp GET_INFO path=" + path);

                    // ルーター情報取得
                    HttpGet method = new HttpGet("http://" + routerIpAddr + path);
                    HttpResponse response = httpClient.executeWithAuth(method, AuthType.DEFAULT);
                    int statusCode = HttpStatus.SC_UNAUTHORIZED;
                    HttpEntity entity = null;

                    // レスポンス無しをSC_UNAUTHORIZEDにマッピングする工夫
                    if (response != null && response.getStatusLine() != null) {
                        statusCode = response.getStatusLine().getStatusCode();
                        entity = response.getEntity();
                    }

                    // 成功時
                    if (entity != null && statusCode == HttpStatus.SC_OK) {
                        String content = EntityUtils.toString(entity, Const.ROUTER_PAGE_CHARSET);
                        entity.consumeContent();
                        parseContent(content, routerInfo);

                        // 管理画面パスワード未設定時
                        if (routerInfo.notInitialized) {
                            Logger.w(" GET_INFO warning, password not initialized");
                            return CTRL_PASS_NOT_INITIALIZED; // 20
                        }

                        lastUpdate = now;
                        communicated = true;
                        prevRouterInfo = routerInfo;
                    }
                    // 認証失敗
                    else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                        Logger.w(" GET_INFO warning, unauthorized");
                        MyHttpClient.discardContent(response);
                        return CTRL_UNAUTHORIZED; // 21
                    }
                    else {
                        Logger.w(" GET_INFO error, statusCode=" + statusCode);
                        MyHttpClient.discardContent(response);
                        return 22;
                    }
                }
                catch (Throwable e) {
                    Logger.w(" GET_INFO error, e=" + e.toString(), e);
                    return 23;
                }
            }

            // 操作実行
            if (isGetInfo == false) {
                String logkey = "RouterControlByHttp " + ctrl;

                // 通信済ならなにもしない
                if (communicated) {
                    Logger.i(logkey + " start (check skipped)");
                }
                // ルーターへの簡易到達確認を行う
                else {
                    long start = System.currentTimeMillis();
                    try {
                        // 401:UNAUTHORIZEDが返ってこない場合はエラー
                        HttpGet method = new HttpGet("http://" + routerIpAddr + "/");
                        HttpResponse response = httpClient.executeWithAuth(method, AuthType.NONE);
                        int statusCode = response.getStatusLine().getStatusCode();
                        MyHttpClient.discardContent(response);
                        if (statusCode != HttpStatus.SC_UNAUTHORIZED) {
                            Logger.e(logkey + " check failed, communication failed, statusCode=" + statusCode);
                            return 31;
                        }
                    }
                    catch (Throwable e) {
                        Logger.e(logkey + " check failed, router unreachable, e=" + e.toString());
                        return 32;
                    }
                    Logger.i(logkey + " start (check " + (System.currentTimeMillis() - start) + "ms)");
                }

                if (ctrl != CTRL.CHECK_WM) {

                    // 宛先生成
                    String path = createPath(ctrl);
                    Logger.i(logkey + " path=" + path);

                    // スタンバイ・ルーター再起動の場合、成功時に例外が発生する
                    boolean standby_reboot = ctrl == CTRL.STANDBY || ctrl == CTRL.REBOOT_WM;
                    try {
                        // 実行
                        HttpResponse response = httpClient.executeWithAuth(createMethod(ctrl, routerIpAddr, path),
                                AuthType.DEFAULT);
                        int statusCode = response.getStatusLine().getStatusCode();
                        Logger.i(logkey + " statusCode=" + statusCode);

                        // スタンバイ・ルーター再起動の場合、例外が飛ぶのが正常
                        if (standby_reboot) {
                            MyHttpClient.discardContent(response);
                            return CTRL_STANBY_FAILED; // 33
                        }
                        // それ以外は正常
                        else {
                            HttpEntity entity = response.getEntity();
                            entity.consumeContent();
                            return CTRL_OK;
                        }
                    }
                    catch (Throwable e) {

                        // スタンバイ・ルーター再起動の場合、例外が飛ぶのが正常
                        if (standby_reboot) {
                            // NoHttpResponseException (WM3800R Android4.2)
                            // SocketTimeoutException (WM3600R Android2.2.2)
                            // SocketException (WM3600R Android4.0.4)
                            if (e instanceof NoHttpResponseException || e instanceof SocketTimeoutException
                                    || e instanceof SocketException) {
                                return CTRL_OK;
                            }
                        }
                        else {
                            Logger.w(logkey + " error", e);
                            return 34;
                        }
                    }
                }
            }

            // GET_INFOもここにくる
            return CTRL_OK;
        }
        catch (Throwable e) {
            Logger.w("RouterControlByHttp error", e);
            return 101;
        }
        finally {
            MyHttpClient.close(httpClient);
        }
    }

    protected static String createPath(CTRL ctrl) {
        boolean rmtMain = canStandyByRmtMain(prevRouterInfo);

        if (ctrl == CTRL.STANDBY) {
            if (hasBluetooth(prevRouterInfo) == false) {
                return rmtMain ? Const.ROUTER_URL_STANDBY_RMTMAIN : Const.ROUTER_URL_STANDBY_INFOBTN;
            }
            else {
                return rmtMain ? Const.ROUTER_URL_STANDBY_BT_RMTMAIN : Const.ROUTER_URL_STANDBY_BT_INFOBTN;
            }
        }
        else if (ctrl == CTRL.WIMAX_CONN) {
            return Const.ROUTER_URL_WIMAX_CONN_INFOBTN;
        }
        else if (ctrl == CTRL.WIMAX_DISCN) {
            return Const.ROUTER_URL_WIMAX_DISCN_INFOBTN;
        }
        else if (ctrl == CTRL.REBOOT_WM) {
            return rmtMain ? Const.ROUTER_URL_REBOOT_WM_RMTMAIN : Const.ROUTER_URL_REBOOT_WM_INFOBTN;
        }

        return null;
    }

    protected static HttpRequestBase createMethod(CTRL ctrl, String routerIpAddr, String path)
            throws UnsupportedEncodingException {

        HttpPost method = new HttpPost("http://" + routerIpAddr + path);

        // bodyパラメータ
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        // WiMAX接続用の追加パラメータ
        boolean filterHidden = false;
        if (ctrl == CTRL.WIMAX_CONN) {
            filterHidden = true;
            params.add(new BasicNameValuePair("WIMAX_CMD_ISSUE", "YES"));
            params.add(new BasicNameValuePair("CHECK_ACTION_MODE", "1"));
        }

        // セッションIDだけを設定
        if (filterHidden) {
            String sid = hiddenMap.get(Const.ROUTER_PAGE_SESSIONID_NAME);
            params.add(new BasicNameValuePair(Const.ROUTER_PAGE_SESSIONID_NAME, sid));
        }
        // セッションID等を設定
        else {
            Iterator<String> iterator = hiddenMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                params.add(new BasicNameValuePair(key, hiddenMap.get(key)));
            }
        }

        // body設定
        method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        return method;
    }

    /**
     * htmlをパースし、routerInfoとhiddenMapを更新する
     * 
     * @param content
     */
    protected static void parseContent(String content, RouterInfo routerInfo) {

        // 前回情報初期化
        hiddenMap.clear();

        if (content == null) {
            Logger.w("RouterControlByHttp failed, content is empty");
            return;
        }
        else if (content.indexOf("管理者パスワードの初期設定") != -1) {
            Logger.w("RouterControlByHttp failed, not initialized");
            routerInfo.notInitialized = true;
            return;
        }
        else {

            try {
                Document doc = Jsoup.parse(content);

                // ルーター情報
                {
                    Elements trs = doc.select(".table_common .small_item_info_tr");
                    if (trs != null) {
                        Iterator<Element> iterator = trs.iterator();
                        while (iterator.hasNext()) {
                            Element tr = (Element) iterator.next();
                            if (tr != null) {
                                Elements tds = tr.getElementsByTag("td");
                                if (tds != null && tds.size() == 2) {
                                    Element td0 = tds.get(0);
                                    Element td1 = tds.get(1);

                                    if (td0 != null && td1 != null) {
                                        String td0txt = MyStringUtlis.normalize(td0.text()).toLowerCase(Locale.US);
                                        String td1txt = MyStringUtlis.normalize(td1.text());

                                        if (td0txt.indexOf("電波状態") != -1) {
                                            routerInfo.antennaLevel = MyStringUtlis.toInt(
                                                    MyStringUtlis.normalize(td1txt.replace("レベル：", "")), -1);
                                        }
                                        else if (td0txt.indexOf("rssi") != -1) {
                                            routerInfo.rssiText = MyStringUtlis.normalize(MyStringUtlis.subStringBefore(td1txt,
                                                    "("));
                                        }
                                        else if (td0txt.indexOf("cinr") != -1) {
                                            routerInfo.cinrText = MyStringUtlis.normalize(MyStringUtlis.subStringBefore(td1txt,
                                                    "("));
                                        }
                                        else if (td0txt.indexOf("macアドレス(bluetooth)") != -1) {
                                            routerInfo.bluetoothAddress = td1txt;
                                        }
                                        else if (td0txt.indexOf("電池残量") != -1) {
                                            int battery = -1;
                                            if (td1txt.indexOf("充電中") != -1) {
                                                routerInfo.charging = true;
                                                int level = MyStringUtlis.count(td1txt, '■');
                                                if (level >= 1) {
                                                    level--;
                                                }
                                                battery = level * 10;
                                            }
                                            else {
                                                routerInfo.charging = false;
                                                String tmp = td1txt;
                                                int index = -1;
                                                index = td1txt.indexOf("（");
                                                if (index != -1) {
                                                    tmp = tmp.substring(index + 1);
                                                    index = tmp.indexOf("％");
                                                    if (index != -1) {
                                                        tmp = tmp.substring(0, index);
                                                        battery = MyStringUtlis.toInt(MyStringUtlis.normalize(tmp), -1);
                                                    }
                                                }
                                            }
                                            routerInfo.battery = battery;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // hidden値
                {
                    Elements hiddens = doc.select("input[type=hidden]");
                    if (hiddens != null) {
                        Iterator<Element> iterator = hiddens.iterator();
                        while (iterator.hasNext()) {
                            Element e = (Element) iterator.next();
                            String name = MyStringUtlis.normalize(e.attr("name"));
                            if (MyStringUtlis.isEmpty(name) == false) {
                                String value = MyStringUtlis.normalize(e.attr("value"));
                                hiddenMap.put(name, value);
                            }
                        }
                    }
                }

                // ルーター名
                {
                    Elements e = doc.select(".product span");
                    if (e != null) {
                        String newRouterName = MyStringUtlis.normalize(e.text());
                        if (MyStringUtlis.isEmpty(newRouterName) == false) {
                            routerInfo.routerName = newRouterName;
                        }
                    }
                }

                // スタンバイボタンがあるかどうか
                {
                    Element standbyButton = doc.getElementById("REMOOTE_STANDBY");
                    if (standbyButton != null && MyStringUtlis.eqauls(standbyButton.tagName().toLowerCase(Locale.US), "input")) {
                        routerInfo.hasStandbyButton = true;
                    }
                    else {
                        routerInfo.hasStandbyButton = false;
                    }
                }
            }
            catch (Throwable e) {
                Logger.w("RouterControlByHttp parsing failed", e);
            }
        }
    }

    /**
     * htmlをパースし、WMルーターの未認証画面かどうかを判定します。
     * 
     * @deprecated 現状未使用
     * @param content
     */
    protected static boolean isNotAuthedOfWmRouter(String content) {

        if (MyStringUtlis.isEmpty(content)) {
            return false;
        }

        // WM3800R
        {
            boolean notAuthedOfWmRouter = true;
            String[] wm3800r_keys = { "[認証エラー]", "/common/set.css", "contents_single", "現在のページの位置", "本文ここから", "トップページへ戻る", };
            for (String key : wm3800r_keys) {
                if (content.indexOf(key) == -1) {
                    notAuthedOfWmRouter = false;
                    break;
                }
            }
            if (notAuthedOfWmRouter) {
                return true;
            }
        }

        // WM3600R
        {
            boolean notAuthedOfWmRouter = true;
            String[] wm3600r_keys = { "xxxxxxxxxxxxxxxxxxxxx", "yyyyyyyyyyyyyyyyyyyyyyyyy", };
            for (String key : wm3600r_keys) {
                if (content.indexOf(key) == -1) {
                    notAuthedOfWmRouter = false;
                    break;
                }
            }
            if (notAuthedOfWmRouter) {
                return true;
            }
        }

        return false;
    }
}
