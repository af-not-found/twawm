package com.appspot.afnf4199ga.twawm.router;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;
import com.appspot.afnf4199ga.wmgraph.app.MainActivity;
import com.appspot.afnf4199ga.wmgraph.app.MyPreferenceActivity;

public class RouterControlByHttp {

	public static final int CTRL_OK = 0;
	public static final int CTRL_STANBY_FAILED = 33;
	public static final int CTRL_PASS_NOT_INITIALIZED = 23;

	protected static String routerName = null;
	protected static long lastUpdate;
	protected static HashMap<String, String> hiddenMap = new HashMap<String, String>();

	enum CTRL {
		GET_INFO, STANDBY
	}

	public static int exec(CTRL ctrl, RouterInfo routerInfo) {
		MyHttpClient httpClient = null;
		try {
			MainActivity service = MainActivity.getInstance();
			if (service == null) {
				Logger.e("service is null on RouterControlByHttp.exec");
				return 10;
			}

			// ルーターIPアドレスを取得
			String routerIpAddr = getRouterIpAddr(new InetLookupWrappter(), Const.getPrefApIpAddr(service));
			if (routerIpAddr == null) {
				return 11;
			}

			// httpClient作成
			httpClient = MyHttpClient.createClient(routerIpAddr);

			// ルーター情報取得、またはスタンバイ実行時のセッションID再取得
			boolean communicated = false;
			long now = System.currentTimeMillis();
			if (ctrl == CTRL.GET_INFO || now - lastUpdate >= Const.ROUTER_SESSION_TIMEOUT) {
				//Logger.i("RouterControlByHttp GET_INFO");

				try {
					// ルーター情報取得
					HttpGet method = new HttpGet("http://" + routerIpAddr + Const.ROUTER_URL_INFO);
					//method.setHeader("Connection", "close");
					HttpResponse response = httpClient.execute(method);
					int statusCode = response.getStatusLine().getStatusCode();
					HttpEntity entity = response.getEntity();

					// 成功時
					if (statusCode == HttpStatus.SC_OK && entity != null) {
						String content = EntityUtils.toString(entity, Const.ROUTER_PAGE_CHARSET);
						entity.consumeContent();
						if (routerInfo != null) {
							updateRouterInfo(content, routerInfo);
						}
						lastUpdate = now;
						communicated = true;

						// 管理画面パスワード未設定時
						if (routerInfo.notInitialized) {
							return CTRL_PASS_NOT_INITIALIZED; // 20
						}
					}
					else {
						Logger.w("RouterControlByHttp GET_INFO error, statusCode=" + statusCode);
						return 21;
					}
				}
				catch (Throwable e) {
					Logger.w("RouterControlByHttp GET_INFO error, e=" + e.toString());
					return 22;
				}
			}

			// スタンバイ実行
			if (ctrl == CTRL.STANDBY) {

				// 通信済ならなにもしない
				if (communicated) {
					Logger.i("RouterControlByHttp STANDBY start (check skipped)");
				}
				// ルーターへの簡易到達確認を行う
				else {
					long start = System.currentTimeMillis();
					try {
						// 認証を一旦無効化
						httpClient.disableCredentialsProvider();

						// 401:UNAUTHORIZEDが返ってこない場合はエラー
						HttpGet method = new HttpGet("http://" + routerIpAddr + "/");
						HttpResponse response = httpClient.execute(method);
						int statusCode = response.getStatusLine().getStatusCode();
						if (statusCode != HttpStatus.SC_UNAUTHORIZED) {
							Logger.e("RouterControlByHttp STANDBY check failed, communication failed, statusCode=" + statusCode);
							return 31;
						}

						// 認証を再度有効にする
						httpClient.enableCredentialsProvider();
					}
					catch (Throwable e) {
						Logger.e("RouterControlByHttp STANDBY check failed, router unreachable, e=" + e.toString());
						return 32;
					}
					Logger.i("RouterControlByHttp STANDBY start (check " + (System.currentTimeMillis() - start) + "ms)");
				}

				// スタンバイ実行
				{
					// TODO 3800ならROUTER_URL_BTSTANDBYを使う
					HttpPost method = new HttpPost("http://" + routerIpAddr + Const.ROUTER_URL_STANDBY);

					try {
						// セッションID等を設定
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						Iterator<String> iterator = hiddenMap.keySet().iterator();
						while (iterator.hasNext()) {
							String key = iterator.next();
							params.add(new BasicNameValuePair(key, hiddenMap.get(key)));
						}
						method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

						// スタンバイ実行
						HttpResponse response = httpClient.execute(method);

						// 成功してしまった場合はエラーであるはず
						int statusCode = response.getStatusLine().getStatusCode();
						Logger.e("RouterControlByHttp STANDBY error, statusCode=" + statusCode);
						return CTRL_STANBY_FAILED; // 33
					}
					catch (NoHttpResponseException e) {
						// success (WM3800R Android4.2)
					}
					catch (SocketTimeoutException e) {
						// success (WM3600R Android2.2.2)
					}
					catch (SocketException e) {
						// success (WM3600R Android4.0.4)
					}
					catch (Throwable e) {
						Logger.e("RouterControlByHttp STANDBY error", e);
						return 34;
					}
				}
			}

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

	protected static String getRouterIpAddr(InetLookupWrappter wrapper, String confRouterIpAddr) throws Throwable {
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
				return null;
			}
			// OKならrouterIpAddrを更新
			else {
				routerIpAddr = address.getHostAddress();
			}
		}

		return routerIpAddr;
	}

	/**
	 * info_remote_mainのhtmlをパースし、routerInfoとhiddenMapを更新する
	 * 
	 * @param content
	 */
	@SuppressLint("DefaultLocale")
	protected static void updateRouterInfo(String content, RouterInfo routerInfo) {

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
										String td0txt = MyStringUtlis.normalize(td0.text()).toLowerCase();
										String td1txt = MyStringUtlis.normalize(td1.text());

										if (td0txt.indexOf("電波状態") != -1) {
											routerInfo.antennaLevelText = MyStringUtlis.normalize(td1txt.replace("レベル：", ""));
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
											if (td1txt.indexOf("充電中") != -1) {
												routerInfo.batteryText = "" + (MyStringUtlis.count(td1txt, '■') * 10) + "+";
											}
											else {
												String tmp = td1txt;
												int index = -1;
												index = td1txt.indexOf("（");
												if (index != -1) {
													tmp = tmp.substring(index + 1);
													index = tmp.indexOf("％");
													if (index != -1) {
														tmp = tmp.substring(0, index);
														routerInfo.batteryText = MyStringUtlis.normalize(tmp) + "%";
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}

				//			// hidden値
				//			{
				//				Elements hiddens = doc.select("input[type=hidden]");
				//				if (hiddens != null) {
				//					Iterator<Element> iterator = hiddens.iterator();
				//					while (iterator.hasNext()) {
				//						Element e = (Element) iterator.next();
				//						String name = MyStringUtlis.normalize(e.attr("name"));
				//						if (MyStringUtlis.isEmpty(name) == false) {
				//							String value = MyStringUtlis.normalize(e.attr("value"));
				//							hiddenMap.put(name, value);
				//						}
				//					}
				//				}
				//			}
				//
				//			// ルーター名
				//			if (routerName == null) {
				//				Elements e = doc.select(".product span");
				//				if (e != null) {
				//					String newRouterName = MyStringUtlis.normalize(e.text());
				//					if (MyStringUtlis.isEmpty(newRouterName) == false) {
				//						routerInfo.routerName = newRouterName;
				//						if (MyStringUtlis.eqauls(newRouterName, routerName) == false) {
				//							Logger.i("routerName=" + newRouterName);
				//							routerName = newRouterName;
				//						}
				//					}
				//				}
				//			}
				//			else {
				//				routerInfo.routerName = routerName;
				//			}
			}
			catch (Throwable e) {
				Logger.w("RouterControlByHttp parsing failed", e);
			}
		}
	}

	public static void resetPrevious() {
		hiddenMap.clear();
		lastUpdate = 0;
	}
}
