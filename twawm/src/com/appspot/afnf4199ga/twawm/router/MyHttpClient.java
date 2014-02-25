package com.appspot.afnf4199ga.twawm.router;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

import com.appspot.afnf4199ga.twawm.Const;

public class MyHttpClient extends DefaultHttpClient {

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

	protected static boolean isRmtMainPath(String path) {
		return path != null && path.indexOf("/info_remote_") != -1;
	}
}
