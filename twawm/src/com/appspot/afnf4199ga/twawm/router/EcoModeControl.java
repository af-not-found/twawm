package com.appspot.afnf4199ga.twawm.router;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
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

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.twawm.app.BackgroundService;
import com.appspot.afnf4199ga.twawm.router.MyHttpClient.AuthType;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class EcoModeControl extends Thread {

    public static void changeEcoMode(Boolean nextEcoCharge) {
        new EcoModeControl(nextEcoCharge).start();
    }

    protected Boolean currentEcoCharge = null;
    protected List<NameValuePair> currentValues;
    protected Boolean nextEcoCharge;

    public EcoModeControl(Boolean nextEcoCharge) {
        this.nextEcoCharge = nextEcoCharge;
    }

    public void run() {
        Logger.v("EcoModeControl started, next=" + nextEcoCharge);

        BackgroundService service = BackgroundService.getInstance();
        if (service == null) {
            Logger.e("service is null on EcoModeControl");
            return;
        }

        MyHttpClient httpClient = null;
        try {
            // ルーターIPアドレスを取得
            String routerIpAddr = MyHttpClient.getRouterIpAddr(new InetLookupWrappter(), Const.getPrefApIpAddr(service));
            if (routerIpAddr == null || routerIpAddr == MyHttpClient.NOT_SITE_LOCAL_ADDR) {
                Logger.w("EcoModeControl getRouterIpAddr failed");
                service.onEcoModeControlFinished(null);
                return;
            }

            // httpClient作成
            httpClient = MyHttpClient.createClient(service);

            // 現在情報取得
            {
                HttpGet method = new HttpGet("http://" + routerIpAddr + Const.ROUTER_URL_ECO_MAIN);
                if (request(httpClient, method) == false) {
                    service.onEcoModeControlFinished(null);
                    return;
                }
            }
            Logger.v("EcoModeControl current=" + currentEcoCharge);

            // 取得のみまたは変更不要の場合は、そのまま抜ける
            if (nextEcoCharge == null || currentEcoCharge == null || currentEcoCharge == nextEcoCharge) {
                service.onEcoModeControlFinished(currentEcoCharge);
                return;
            }

            // 設定変更
            {
                if (nextEcoCharge) {
                    currentValues.add(new BasicNameValuePair("ECO_CHARGE", "0"));
                }

                HttpPost method = new HttpPost("http://" + routerIpAddr + Const.ROUTER_URL_ECO_POST);
                method.setEntity(new UrlEncodedFormEntity(currentValues, "UTF-8"));
                if (request(httpClient, method) == false) {
                    service.onEcoModeControlFinished(null);
                    return;
                }
            }
            Logger.v("EcoModeControl changed=" + currentEcoCharge);

            service.onEcoModeControlFinished(currentEcoCharge);
        }
        catch (Throwable e) {
            Logger.w("EcoModeControl error", e);
            service.onEcoModeControlFinished(null);
        }
        finally {
            MyHttpClient.close(httpClient);
        }
    }

    protected boolean request(MyHttpClient httpClient, HttpRequestBase method) throws Exception {

        HttpResponse response = httpClient.executeWithAuth(method, AuthType.DEFAULT);
        boolean success = false;
        int statusCode = -1;

        if (response != null && response.getStatusLine() != null) {
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();

            // 成功時
            if (entity != null && statusCode == HttpStatus.SC_OK) {
                String content = EntityUtils.toString(entity, RouterControlByHttp.getPageCharset());
                entity.consumeContent();
                success = parseContent(content);
            }
        }

        if (success == false) {
            Logger.w("EcoModeControl " + statusCode);
            MyHttpClient.discardContent(response);
        }

        return success;
    }

    protected boolean parseContent(String content) {

        currentEcoCharge = null;
        currentValues = new ArrayList<NameValuePair>();

        if (content == null) {
            Logger.w("EcoModeControl failed, content is empty");
            return false;
        }
        else if (content.indexOf("管理者パスワードの初期設定") != -1) {
            Logger.w("EcoModeControl failed, not initialized");
            return false;
        }
        else {

            try {
                Document doc = Jsoup.parse(content);

                // input
                {
                    Elements inputs = doc.select("#eco_mode_main input");
                    if (inputs != null) {
                        Iterator<Element> iterator = inputs.iterator();
                        while (iterator.hasNext()) {
                            Element e = (Element) iterator.next();
                            String name = MyStringUtlis.normalize(e.attr("name"));

                            if (MyStringUtlis.eqauls("ECO_CHARGE", name)) {
                                name = null;
                                currentEcoCharge = MyStringUtlis.eqauls("checked", e.attr("checked"));
                            }
                            else if (MyStringUtlis.eqauls("submit", e.attr("type"))
                                    || MyStringUtlis.eqauls("button", e.attr("type"))) {
                                name = null;
                            }
                            else if (MyStringUtlis.eqauls("checkbox", e.attr("type"))
                                    && MyStringUtlis.eqauls("checked", e.attr("checked")) == false) {
                                name = null;
                            }

                            if (MyStringUtlis.isEmpty(name) == false) {
                                String value = MyStringUtlis.normalize(e.attr("value"));
                                //Logger.i(name + ":" + value);
                                currentValues.add(new BasicNameValuePair(name, value));
                            }
                        }
                    }
                }

                // select
                currentValues.addAll(RouterControlByHttp.getPulldownValues(doc, "#eco_mode_main select"));

                return true;
            }
            catch (Throwable e) {
                Logger.w("EcoModeControl parsing failed", e);
            }
        }

        return false;
    }
}
