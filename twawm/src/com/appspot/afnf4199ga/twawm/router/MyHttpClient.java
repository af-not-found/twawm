package com.appspot.afnf4199ga.twawm.router;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.regex.Matcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.twawm.app.MyPreferenceActivity;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;

public class MyHttpClient extends DefaultHttpClient {

    static final String NOT_SITE_LOCAL_ADDR = "NOT_SITE_LOCAL_ADDR";

    private Context context;

    public MyHttpClient(HttpParams httpParams) {
        super(httpParams);
    }

    static MyHttpClient createClient(Context context) {

        // タイムアウト設定
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, Const.ROUTER_HTTP_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, Const.ROUTER_HTTP_TIMEOUT);

        // インスタンス生成
        MyHttpClient httpClient = new MyHttpClient(httpParams);
        httpClient.context = context;

        // リダイレクトを拒否するリダイレクトハンドラ設定
        httpClient.setRedirectHandler(new RedirectHandler() {
            @Override
            public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                return false;
            }

            @Override
            public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
                return null;
            }
        });

        // リトライが有効になっていると、NAD11のスタンバイ後のBT復帰で、即スタンバイに入ってしまう
        // リトライ分がリセットされずに残っていて、復帰後に処理されてしまう模様
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));

        return httpClient;
    }

    enum AuthType {
        NONE, DEFAULT
    }

    public HttpResponse executeWithAuth(HttpRequestBase method, AuthType authType) throws ClientProtocolException, IOException {

        // 認証有り
        if (authType == AuthType.DEFAULT) {

            // smart-user判断
            boolean smart = false;
            URI uri = method.getURI();
            String path = uri.getPath();

            // RMTMAINに対するアクセスの場合は、smart-user
            if (isRmtMainPath(path)) {
                smart = true;
            }

            // Credentials生成
            String user, pass;
            if (smart) {
                user = Const.ROUTER_BASIC_AUTH_USERNAME;
                pass = Const.ROUTER_BASIC_AUTH_PASSWORD;
            }
            else {
                user = Const.ROUTER_BASIC_AUTH_USERNAME2;
                pass = Const.getPrefRouterControlPassword(context);
            }
            Credentials credentials = new UsernamePasswordCredentials(user, pass);

            // Basic認証設定
            AuthScope scope = new AuthScope(uri.getHost(), Const.ROUTER_PORT);
            getCredentialsProvider().setCredentials(scope, credentials);
        }
        // 認証無し
        else {
            getCredentialsProvider().clear();
        }

        // 実行
        return execute(method);
    }

    static void close(MyHttpClient client) {
        try {
            if (client != null && client.getConnectionManager() != null) {
                client.getConnectionManager().shutdown();
            }
        }
        catch (Throwable e) {
            // do nothing
        }
    }

    static String getRouterIpAddr(InetLookupWrappter wrapper, String confRouterIpAddr) throws Throwable {
        String routerIpAddr = null;

        // ルーターIPアドレスが設定済ならそっちを優先
        if (MyStringUtlis.isEmpty(confRouterIpAddr) == false) {
            Matcher m = MyPreferenceActivity.IP_ADDR_PATTERN.matcher(confRouterIpAddr);
            if (m.matches()) {
                routerIpAddr = confRouterIpAddr;
                // サイトローカルかどうかのチェックはしない
            }
            else {
                Logger.w("RouterControlByHttp confRouterIpAddr format is invalid");
                // 下のifブロックに入る
            }
        }

        // 未設定の場合
        if (MyStringUtlis.isEmpty(routerIpAddr)) {

            // まずは正引き
            InetAddress address = null;
            try {
                address = wrapper.getByName(Const.ROUTER_HOSTNAME);
            }
            catch (Throwable e) {
                // do nothing
            }

            // nullの場合はNG
            if (address == null) {
                Logger.w("RouterControlByHttp resolved IP Addr is null");
                return null;
            }
            // サイトローカルでない場合はNG（モバイルデータ通信環境を想定）
            else if (address.isSiteLocalAddress() == false) {
                Logger.w("RouterControlByHttp " + Const.ROUTER_HOSTNAME + " is not private address");
                return NOT_SITE_LOCAL_ADDR;
            }
            // OKならrouterIpAddrを更新
            else {
                routerIpAddr = address.getHostAddress();
            }
        }

        return routerIpAddr;
    }

    static String estimateRouterIpAddr(Context context) {
        try {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifi != null) {
                String ipaddr = estimateRouterIpAddr(wifi);
                if (ipaddr != null) {
                    return ipaddr;
                }
            }
        }
        catch (Exception e) {
            Logger.w("estimateRouterIpAddr failed", e);
        }

        return Const.ROUTER_IPADDR_DEFAULT;
    }

    static String estimateRouterIpAddr(WifiManager wifi) {

        // ゲートウェイIPアドレス取得
        DhcpInfo dhcpInfo = wifi.getDhcpInfo();
        if (dhcpInfo != null) {
            String ipaddr = AndroidUtils.intToIpaddr(dhcpInfo.gateway);
            //Logger.i("estimateRouterIpAddr 1 " + ipaddr);
            return ipaddr;
        }

        // クライアントIPアドレス取得
        WifiInfo connectionInfo = wifi.getConnectionInfo();
        if (connectionInfo != null) {
            String ipaddr = AndroidUtils.intToIpaddr(connectionInfo.getIpAddress());
            if (MyStringUtlis.isEmpty(ipaddr) == false) {
                int index = ipaddr.lastIndexOf(".");
                if (index != -1) {
                    ipaddr = ipaddr.substring(0, index) + ".1";
                    //Logger.i("estimateRouterIpAddr 2 " + ipaddr);
                    return ipaddr;
                }
            }
        }

        return null;
    }

    static void discardContent(HttpResponse response) {
        try {
            if (response != null) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    entity.consumeContent();
                }
            }
        }
        catch (Throwable e) {
            // do nothing
        }
    }

    protected static boolean isRmtMainPath(String path) {
        return path != null && path.indexOf("/info_remote_") != -1;
    }
}
