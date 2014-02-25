package com.appspot.afnf4199ga.twawm.router;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.RedirectHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.appspot.afnf4199ga.twawm.Const;

public class MyHttpClient extends DefaultHttpClient {

	private Credentials credentials;

	private AuthScope scope;

	public MyHttpClient(HttpParams httpParams) {
		super(httpParams);
	}

	static MyHttpClient createClient(String authScopeIpAddr) {

		// タイムアウト設定
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, Const.ROUTER_HTTP_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, Const.ROUTER_HTTP_TIMEOUT);

		// Basic認証設定
		MyHttpClient httpClient = new MyHttpClient(httpParams);
		httpClient.credentials = new UsernamePasswordCredentials(Const.ROUTER_BASIC_AUTH_USERNAME,
				Const.ROUTER_BASIC_AUTH_PASSWORD);
		httpClient.scope = new AuthScope(authScopeIpAddr, Const.ROUTER_PORT);
		httpClient.getCredentialsProvider().setCredentials(httpClient.scope, httpClient.credentials);

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

	void disableCredentialsProvider() {
		getCredentialsProvider().clear();
	}

	void enableCredentialsProvider() {
		getCredentialsProvider().setCredentials(scope, credentials);
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
}
