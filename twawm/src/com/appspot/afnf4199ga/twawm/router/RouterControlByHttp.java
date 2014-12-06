package com.appspot.afnf4199ga.twawm.router;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.appspot.afnf4199ga.twawm.router.RouterInfo.COM_TYPE;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class RouterControlByHttp {

    public static final int CTRL_OK = 0;
    public static final int CTRL_ROUTER_IP_IS_NOT_SITE_LOCAL = 12;
    public static final int CTRL_PASS_NOT_INITIALIZED = 20;
    public static final int CTRL_UNAUTHORIZED = 21;
    public static final int CTRL_FAILED = 33;

    protected static RouterInfo prevRouterInfo = null;
    protected static long lastUpdate;
    protected static HashMap<String, String> hiddenMap = new HashMap<String, String>();
    protected static long counter = 0;

    private static final Pattern stsPtn = Pattern.compile("[0-9]+");

    public static enum CTRL {
        // 共通
        GET_INFO, GET_INFO_FORCE_RMTMAIN, GET_INFO_FORCE_INFOBTN, STANDBY, REBOOT_WM, CHECK_WM,
        // WM3800R用
        WIMAX_DISCN, WIMAX_CONN,
        // NAD11用
        GET_INFO_FORCE_IDXCT, NAD_COM_HS, NAD_COM_NL, NAD_WIFI_SPOT_ON, NAD_WIFI_SPOT_OFF, NAD_WIMAX_RECONN
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

    /**
     * RmtMainからスタンバイできるかどうか（古いファームウエア）
     */
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

    public static String getPageCharset() {
        if (prevRouterInfo == null) {
            return Const.ROUTER_PAGE_CHARSET_WM;
        }
        else {
            return prevRouterInfo.nad ? Const.ROUTER_PAGE_CHARSET_NAD : Const.ROUTER_PAGE_CHARSET_WM;
        }
    }

    public static boolean isNad() {
        if (prevRouterInfo == null) {
            return false;
        }
        else {
            return prevRouterInfo.nad;
        }
    }

    public static boolean isWm3800FirmwareVersionOld() {
        return hasStandbyButton(prevRouterInfo) && canStandyByRmtMain(prevRouterInfo);
    }

    protected static boolean isGetInfoCtrl(CTRL ctrl) {
        return ctrl == CTRL.GET_INFO || ctrl == CTRL.GET_INFO_FORCE_RMTMAIN || ctrl == CTRL.GET_INFO_FORCE_INFOBTN;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected static boolean checkNad(MyHttpClient httpClient, String routerIpAddr, RouterInfo routerInfo) {

        try {
            HttpGet method = new HttpGet("http://" + routerIpAddr + Const.ROUTER_URL_INFO_STS_XML);
            HttpResponse response = httpClient.executeWithAuth(method, AuthType.NONE);
            if (response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String content = EntityUtils.toString(entity, Const.ROUTER_PAGE_CHARSET_WM); // euc-jpでOK
                        entity.consumeContent();
                        parseContent(content, routerInfo);
                        return true;
                    }
                }
            }
        }
        catch (Throwable e) {
            Logger.i("RouterControlByHttp checkNad failed, e=" + e.toString());
        }

        return false;
    }

    @SuppressLint("DefaultLocale")
    public static int exec(Context context, CTRL ctrl, RouterInfo routerInfo) {
        if (++counter >= 0xffff) {
            counter = 0;
        }

        boolean isGetInfo = isGetInfoCtrl(ctrl);

        MyHttpClient httpClient = null;
        try {
            // ルーターIPアドレスを取得
            String routerIpAddr = MyHttpClient.getRouterIpAddr(new InetLookupWrappter(), Const.getPrefApIpAddr(context));
            if (routerIpAddr == null) {
                return 11;
            }
            // ルーターIPアドレスがPrivateIPでない場合
            else if (routerIpAddr == MyHttpClient.NOT_SITE_LOCAL_ADDR) {
                // IPアドレス推定
                routerIpAddr = MyHttpClient.estimateRouterIpAddr(context);
                // ルーターIPアドレス上書き
                // 対応外ルータで常に実行されてしまうためコメントアウト   Const.updatePrefApIpAddr(context, routerIpAddr);
            }
            routerInfo.ipaddr = routerIpAddr;

            // httpClient作成
            httpClient = MyHttpClient.createClient(context);

            // NAD11チェック
            long now = System.currentTimeMillis();
            routerInfo.nad = checkNad(httpClient, routerIpAddr, routerInfo);

            // NAD11かつ操作系の場合は、常にセッション再取得
            if (routerInfo.nad && isGetInfo == false) {
                lastUpdate = 0;
            }

            // ルーター情報取得、またはスタンバイ実行時のセッションID再取得、またはスタンバイボタン未発見時の情報再取得
            boolean communicated = false;
            if (ctrl != CTRL.CHECK_WM
                    && (isGetInfo || now - lastUpdate >= Const.ROUTER_SESSION_TIMEOUT || hasStandbyButton(prevRouterInfo) == false)) {

                try {
                    // アクセス先決定
                    String path = null;
                    if (ctrl == CTRL.WIMAX_CONN) {
                        path = Const.ROUTER_URL_WIMAX_CONN_INFOBTN;
                    }
                    else {
                        // NADシリーズ
                        if (routerInfo.nad) {

                            // NAD11操作系
                            if (ctrl == CTRL.NAD_COM_HS || ctrl == CTRL.NAD_COM_NL || ctrl == CTRL.NAD_WIFI_SPOT_ON
                                    || ctrl == CTRL.NAD_WIFI_SPOT_OFF || ctrl == CTRL.NAD_WIMAX_RECONN) {
                                path = Const.ROUTER_URL_INFO_IDXCT;
                            }
                            // INFOBTN強制、または操作系、または奇数回ならINFOBTN →定期的にパスワード未設定検出するため
                            else if (ctrl == CTRL.GET_INFO_FORCE_INFOBTN || isGetInfo == false || (counter & 1) == 1) {
                                path = Const.ROUTER_URL_INFO_INFOBTN;
                            }
                            // その他はBluetoothMAC取得（認証不要画面）
                            else {
                                path = Const.ROUTER_URL_INFO_IDXCT;
                            }
                            routerInfo.rmtMain = false;
                        }
                        // WMシリーズ
                        else {
                            if (ctrl == CTRL.GET_INFO_FORCE_RMTMAIN) {
                                routerInfo.rmtMain = true;
                            }
                            else if (ctrl == CTRL.GET_INFO_FORCE_INFOBTN) {
                                routerInfo.rmtMain = false;
                            }
                            else if (ctrl == CTRL.GET_INFO_FORCE_IDXCT) {
                                routerInfo.rmtMain = false;
                            }
                            else {
                                // 起動直後はtrue
                                routerInfo.rmtMain = canStandyByRmtMain(prevRouterInfo);
                            }

                            path = routerInfo.rmtMain ? Const.ROUTER_URL_INFO_RMTMAIN : Const.ROUTER_URL_INFO_INFOBTN;
                        }
                    }
                    Logger.i("RouterControlByHttp GET_INFO path=" + path);

                    // ルーター情報取得
                    HttpGet method = new HttpGet("http://" + routerIpAddr + path);
                    HttpResponse response = httpClient.executeWithAuth(method, AuthType.DEFAULT);
                    int statusCode = HttpStatus.SC_UNAUTHORIZED; // response無しの場合はSC_UNAUTHORIZED
                    HttpEntity entity = null;

                    // statusCode/entity取得
                    if (response != null && response.getStatusLine() != null) {
                        statusCode = response.getStatusLine().getStatusCode();
                        entity = response.getEntity();
                    }

                    // 成功時
                    if (entity != null && statusCode == HttpStatus.SC_OK) {
                        String content = EntityUtils.toString(entity, getPageCharset());
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

                // WMシリーズ
                if (routerInfo.nad == false) {

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
                }
                // NADシリーズは通信済

                if (ctrl != CTRL.CHECK_WM) {

                    // 宛先生成
                    String path = createCtrlPath(ctrl);
                    Logger.i(logkey + " path=" + path);

                    // スタンバイ・ルーター再起動の場合、
                    // またはNAD11で、index_contents_local_setの場合（WiMAX再接続を除く）は例外が飛ぶのが正常
                    boolean expect_exception = ctrl == CTRL.STANDBY
                            || ctrl == CTRL.REBOOT_WM
                            || (routerInfo.nad && MyStringUtlis.eqauls(path, Const.ROUTER_URL_NAD_LOCAL_SET) && ctrl != CTRL.NAD_WIMAX_RECONN);

                    try {
                        // 実行
                        HttpResponse response = httpClient.executeWithAuth(
                                createMethod(ctrl, routerIpAddr, path, routerInfo.nad, routerInfo.profile), AuthType.DEFAULT);
                        int statusCode = response.getStatusLine().getStatusCode();
                        Logger.i(logkey + " statusCode=" + statusCode);

                        // 例外が飛ばなければ失敗
                        if (expect_exception) {
                            MyHttpClient.discardContent(response);
                            return CTRL_FAILED; // 33
                        }
                        // それ以外は正常
                        else {
                            HttpEntity entity = response.getEntity();
                            entity.consumeContent();
                            return CTRL_OK;
                        }
                    }
                    catch (Throwable e) {

                        // 例外が飛ぶのが正常
                        if (expect_exception) {
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

    protected static String createCtrlPath(CTRL ctrl) {
        boolean rmtMain = canStandyByRmtMain(prevRouterInfo);

        switch (ctrl) {
        case STANDBY:
            if (hasBluetooth(prevRouterInfo) == false) {
                return rmtMain ? Const.ROUTER_URL_STANDBY_RMTMAIN : Const.ROUTER_URL_STANDBY_INFOBTN;
            }
            else {
                return rmtMain ? Const.ROUTER_URL_STANDBY_BT_RMTMAIN : Const.ROUTER_URL_STANDBY_BT_INFOBTN;
            }

        case WIMAX_CONN:
            return Const.ROUTER_URL_WIMAX_CONN_INFOBTN;

        case WIMAX_DISCN:
            return Const.ROUTER_URL_WIMAX_DISCN_INFOBTN;

        case REBOOT_WM:
            return rmtMain ? Const.ROUTER_URL_REBOOT_WM_RMTMAIN : Const.ROUTER_URL_REBOOT_WM_INFOBTN;

        case NAD_COM_HS:
        case NAD_COM_NL:
        case NAD_WIFI_SPOT_ON:
        case NAD_WIFI_SPOT_OFF:
        case NAD_WIMAX_RECONN:
            return Const.ROUTER_URL_NAD_LOCAL_SET;

        default:
            return null;
        }
    }

    protected static HttpRequestBase createMethod(CTRL ctrl, String routerIpAddr, String path, boolean nad11, String profile)
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
        // NAD11で、index_contents_local_setの場合
        else if (nad11 && MyStringUtlis.eqauls(path, Const.ROUTER_URL_NAD_LOCAL_SET)) {
            filterHidden = true;
            params.add(new BasicNameValuePair("DISABLED_CHECKBOX", ""));
            params.add(new BasicNameValuePair("CHECK_ACTION_MODE", "1"));

            switch (ctrl) {
            case NAD_COM_HS:
                params.add(new BasicNameValuePair("COM_MODE_SEL", "1"));
                params.add(new BasicNameValuePair("BTN_CLICK", "wan2"));
                break;
            case NAD_COM_NL:
                params.add(new BasicNameValuePair("COM_MODE_SEL", "2"));
                params.add(new BasicNameValuePair("BTN_CLICK", "wan2"));
                break;
            case NAD_WIFI_SPOT_ON:
                params.add(new BasicNameValuePair("WIFI_MODE", "on"));
                params.add(new BasicNameValuePair("BTN_CLICK", "wifi"));
                break;
            case NAD_WIFI_SPOT_OFF:
                params.add(new BasicNameValuePair("BTN_CLICK", "wifi"));
                break;
            case NAD_WIMAX_RECONN:
                params.add(new BasicNameValuePair("SELECT_PROFILE", profile));
                params.add(new BasicNameValuePair("BTN_CLICK", "profile"));
                break;
            default:
                break;
            }
        }

        // セッションIDだけを追加
        if (filterHidden) {
            String sid = hiddenMap.get(Const.ROUTER_PAGE_SESSIONID_NAME);
            params.add(new BasicNameValuePair(Const.ROUTER_PAGE_SESSIONID_NAME, sid));
        }
        // すべてのhidden値を追加
        else {
            Iterator<String> iterator = hiddenMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                params.add(new BasicNameValuePair(key, hiddenMap.get(key)));
                //Logger.v("hidden " + key + ":" + hiddenMap.get(key));
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
        // status_get.xml
        else if (content.indexOf("<body><status>") != -1) {

            final int offset = "<body><status>".length();
            int start = content.indexOf("<body><status>");
            int end = content.indexOf("</status></body>");
            if (start == -1 || end == -1) {
                Logger.w("status_get.xml is broken?");
                return;
            }
            String data = content.substring(start + offset, end);

            Matcher matcher = stsPtn.matcher(data);
            int i = -1;
            boolean find = matcher.find();
            while (find) {
                i++;
                String val = matcher.group();
                switch (i) {
                case 0:
                    routerInfo.battery = MyStringUtlis.toInt(val, -1);
                    break;
                case 1:
                    routerInfo.antennaLevel = MyStringUtlis.toInt(val, -1);
                    break;
                case 2:
                    routerInfo.comState = COM_TYPE.ordinalOf(MyStringUtlis.toInt(val, COM_TYPE.NA.ordinal()));
                    break;
                // 使えない？
                // case 3:
                //    routerInfo.comSetting = COM_TYPE.ordinalOf(MyStringUtlis.toInt(val, COM_TYPE.NA.ordinal()));
                //     break;
                case 4:
                    routerInfo.charging = MyStringUtlis.eqauls(val, "1") == false;
                    break;
                default:
                    break;
                }
                find = matcher.find(matcher.end());
            }

            //    data[0]  ->  バッテリー残 0～100
            //    data[1]  ->  アンテナレベル 0～4(HS)、0～5(NL)
            //    data[2]  ->  WAN通信状態 1:HS, 2:NL, 3:Wi-Fi
            //    data[3]  ->  通信モード設定？ 1:HS, 2:NL
            //    data[4]  ->  0:充電中, 1:放電中
            //    data[5]  ->  Wi-Fiクライアント数
            //    data[6]  ->  NAD11側のWi-Fiが 1:ONか0:OFFか

            return;
        }
        else {

            try {
                Document doc = Jsoup.parse(content);

                // ルーター名
                {
                    // WMシリーズ
                    if (routerInfo.nad == false) {
                        Elements e = doc.select(".product span");
                        if (e != null) {
                            String newRouterName = MyStringUtlis.normalize(e.text());
                            if (MyStringUtlis.isEmpty(newRouterName) == false) {
                                routerInfo.routerName = newRouterName;
                            }
                        }
                    }
                    // NADシリーズ
                    else {
                        Elements lis = doc.select("#show_form li");
                        if (lis != null && lis.size() >= 2) {
                            Element li = (Element) lis.get(1);
                            String newRouterName = MyStringUtlis.normalize(li.text());
                            routerInfo.routerName = newRouterName;
                        }
                    }
                }

                // ルーター情報
                {
                    // WMシリーズ
                    if (routerInfo.nad == false) {

                        // WMシリーズのWiMAXはNAとする
                        routerInfo.comState = COM_TYPE.NA;

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
                                                routerInfo.rssiText = MyStringUtlis.normalize(MyStringUtlis.subStringBefore(
                                                        td1txt, "("));
                                            }
                                            else if (td0txt.indexOf("cinr") != -1) {
                                                routerInfo.cinrText = MyStringUtlis.normalize(MyStringUtlis.subStringBefore(
                                                        td1txt, "("));
                                            }
                                            else if (td0txt.indexOf("macアドレス(bluetooth)") != -1) {
                                                if (MyStringUtlis.isEmpty(td1txt) == false) {
                                                    routerInfo.bluetoothAddress = td1txt.toUpperCase(Locale.US);
                                                }
                                            }
                                            else if (td0txt.indexOf("接続状態") != -1) {
                                                if (MyStringUtlis.isEmpty(td1txt) == false && td1txt.indexOf("公衆無線LAN") != -1) {
                                                    routerInfo.comState = COM_TYPE.WIFI_SPOT;
                                                }
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
                    // NADシリーズ
                    else {

                        // プロファイル
                        List<BasicNameValuePair> pairs = RouterControlByHttp.getPulldownValues(doc, "#SELECT_PROFILE");
                        if (pairs.size() >= 1) {
                            BasicNameValuePair pair = pairs.get(0);
                            routerInfo.profile = pair.getValue();
                        }

                        // 通信モード
                        pairs = RouterControlByHttp.getPulldownValues(doc, "#COM_MODE_SEL");
                        if (pairs.size() >= 1) {
                            BasicNameValuePair pair = pairs.get(0);
                            int intval = MyStringUtlis.toInt(pair.getValue(), COM_TYPE.NA.ordinal());
                            routerInfo.comSetting = COM_TYPE.ordinalOf(intval);
                        }

                        // Wi-Fiスポット使用
                        Element e = doc.getElementById("WIFI_MODE");
                        if (e != null) {
                            routerInfo.wifiSpotEnabled = MyStringUtlis.eqauls("checked", e.attr("checked"));
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

                    // NADシリーズ
                    if (routerInfo.nad) {
                        String btmac = hiddenMap.get("BLUETOOTH_MAC");
                        if (MyStringUtlis.isEmpty(btmac) == false) {
                            routerInfo.bluetoothAddress = btmac.toUpperCase(Locale.US);
                        }
                    }
                }

                // スタンバイボタンがあるかどうか
                {
                    // WMシリーズ
                    if (routerInfo.nad == false) {
                        Element standbyButton = doc.getElementById("REMOOTE_STANDBY");
                        if (standbyButton != null
                                && MyStringUtlis.eqauls(standbyButton.tagName().toLowerCase(Locale.US), "input")) {
                            routerInfo.hasStandbyButton = true;
                        }
                        else {
                            routerInfo.hasStandbyButton = false;
                        }
                    }
                    // NADシリーズ
                    else {
                        routerInfo.hasStandbyButton = true;
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

    public static List<BasicNameValuePair> getPulldownValues(Document doc, String rootSelector) {

        List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();

        Elements inputs = doc.select(rootSelector);
        if (inputs != null) {
            Iterator<Element> iterator = inputs.iterator();
            while (iterator.hasNext()) {
                Element e = (Element) iterator.next();
                String name = MyStringUtlis.normalize(e.attr("name"));
                if (MyStringUtlis.isEmpty(name) == false) {

                    Elements options = e.select("option[selected=selected]");
                    if (options != null && options.size() >= 1) {
                        Element option = options.get(0);
                        String value = MyStringUtlis.normalize(option.attr("value"));
                        pairs.add(new BasicNameValuePair(name, value));
                    }
                }
            }
        }

        return pairs;
    }

}
