package com.appspot.afnf4199ga.twawm.app;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.twawm.R;
import com.appspot.afnf4199ga.twawm.ctl.CustomizeActionsActivity;
import com.appspot.afnf4199ga.twawm.ctl.ListItem;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;
import com.appspot.afnf4199ga.utils.MyUncaughtExceptionHandler;

public class MainActivity extends Activity {

	private AlertDialog dialog;
	private WifiManager wifi;
	private static boolean initWizardDisplayed = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Logger.v("MainActivity onCreate");

		// UncaughtExceptionHandler初期化
		MyUncaughtExceptionHandler.init(this);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 下線設定
		TextPaint textPaint = ((TextView) findViewById(R.id.textNotWorksFine)).getPaint();
		textPaint.setUnderlineText(true);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// UI初期化
		UIAct.init(this);

		// ウィジェット初期化（changeStyleで代用）
		DefaultWidgetProvider.changeStyle(this);

		// WifiManager初期化
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		boolean wifiEnabled = AndroidUtils.isWifiEnabled(wifi);
		boolean suppCompleted = wifiEnabled && BackgroundService.isSupplicantCompleted(wifi);

		BackgroundService service = BackgroundService.getInstance();

		// 動作中
		boolean working = Const.getPrefWorking(this);
		if (working) {

			// サービスがある場合
			if (service != null) {
				// UIリフレッシュ
				service.getStateMachine().reflesh(false);
			}
			// サービスが無く、かつWiFiが有効ならサービス起動
			else if (wifiEnabled) {
				Intent srvIntent = new Intent(this, BackgroundService.class);
				startService(srvIntent);
			}

			UIAct.postActivityButton(true, true, wifiEnabled, suppCompleted);
		}
		// 一時停止中
		else {
			// UI更新
			updateAsWorkingOrPausing(this, false);
		}

		// toggleWorking
		ToggleButton btn = (ToggleButton) findViewById(R.id.toggleWorking);
		btn.setChecked(working);

		// 特殊インテント
		Intent intent = getIntent();
		if (intent != null) {
			// アクションダイアログ表示
			if (intent.getBooleanExtra(Const.INTENT_EX_ACTION_SELECT, false)) {
				intent.removeExtra(Const.INTENT_EX_ACTION_SELECT); // 1回目しか反応しないように
				showActionDialog();
			}
			// Bluetooth有効化リクエスト
			else if (intent.getBooleanExtra(Const.INTENT_EX_BT_ENABLING, false)) {
				uiactSetRouterToggleButton(false, null);
				uiactSetWifiToggleButton(false, null);
				intent.removeExtra(Const.INTENT_EX_BT_ENABLING); // 1回目しか反応しないように
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, Const.REQUEST_ENABLE_BT);
			}
			// ウィザード起動
			else if (intent.getBooleanExtra(Const.INTENT_EX_INIT_WIZARD, false)) {
				if (initWizardDisplayed == false) {
					// オンラインチェック停止
					if (service != null) {
						service.terminateOnlineCheck();
					}
					// 1回目しか反応しないように→上手く動かない？のでinitWizardDisplayedを導入
					intent.removeExtra(Const.INTENT_EX_INIT_WIZARD);
					// 画面表示
					InitialConfigurationWizardActivity.startWizard(this);
				}
				return;
			}
		}

		initWizardDisplayed = false;
	}

	@Override
	protected void onResume() {
		//Logger.v("MainActivity onResume");
		super.onResume();

		// UI初期化
		UIAct.init(this);
	}

	@Override
	protected void onPause() {
		//Logger.v("MainActivity onPause");
		super.onPause();

		// ダイアログを閉じる（メモリリーク防止）
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	@Override
	protected void onDestroy() {

		// UIAct停止
		UIAct.destroy();

		// WifiManager破棄
		wifi = null;

		// サービスが生きている場合
		BackgroundService service = BackgroundService.getInstance();
		if (service != null) {

			// WiFi無効状態か、一時停止中ならサービス停止
			if (service.isWifiDisabledState() || Const.getPrefWorking(this) == false) {
				service.stopServiceImmediately();
			}
		}
		super.onDestroy();
	}

	public void onToggleRouter(View view) {
		uiactSetRouterToggleButton(false, null);

		// この時点でサービスが起動していない可能性があるので、その場合はstartServiceする
		BackgroundService service = BackgroundService.getInstance();
		if (service != null) {
			service.toggleRouterFromUI(false);
		}
		else {
			Intent srvIntent = new Intent(this, BackgroundService.class);
			srvIntent.putExtra(Const.INTENT_EX_TOGGLE_ROUTER, true);
			startService(srvIntent);
		}
	}

	public void onToggleWifi(View view) {
		uiactSetWifiToggleButton(false, null);

		// この時点でサービスが起動していない可能性があるので、その場合はstartServiceする
		BackgroundService service = BackgroundService.getInstance();
		if (service != null) {
			service.toggleWifiFromUI();
		}
		else {
			Intent srvIntent = new Intent(this, BackgroundService.class);
			srvIntent.putExtra(Const.INTENT_EX_TOGGLE_WIFI, true);
			startService(srvIntent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int i = 1;
		menu.add(Menu.NONE, i++, Menu.NONE, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences)
				.setIntent(new Intent(this, MyPreferenceActivity.class));
		menu.add(Menu.NONE, i++, Menu.NONE, R.string.send_log_short).setIcon(android.R.drawable.ic_menu_upload)
				.setIntent(new Intent(this, LogSendActivity.class));
		menu.add(Menu.NONE, i++, Menu.NONE, R.string.info).setIcon(android.R.drawable.ic_menu_info_details)
				.setIntent(new Intent(this, InfoActivity.class));

		return super.onCreateOptionsMenu(menu);
	}

	public void onWdClicked(View view) {
		showActionDialog();
	}

	public void onToggleWorking(View view) {

		// ボタン無効化
		uiactToggleWorkingToggleButton(false);
		// 2秒後に有効にする
		UIAct.postDelayedEnableWorkingToggleButton();

		// 設定更新
		ToggleButton btn = (ToggleButton) view;
		boolean change_to_working = btn.isChecked();
		Const.updatePrefWorking(this, change_to_working);

		// 動作中へ切り替え
		if (change_to_working) {

			// UI更新
			WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			boolean wifiEnabled = AndroidUtils.isWifiEnabled(wifi);
			boolean suppCompleted = wifiEnabled && BackgroundService.isSupplicantCompleted(wifi);
			updateAsWorking(wifiEnabled, suppCompleted);

			// WiFiが有効ならサービス起動
			if (wifiEnabled) {
				Intent srvIntent = new Intent(this, BackgroundService.class);
				startService(srvIntent);
			}
		}
		// 一時停止中へ切り替え
		else {

			// UI更新
			updateAsWorkingOrPausing(this, false);

			// サービス停止
			BackgroundService service = BackgroundService.getInstance();
			if (service == null) {
				Logger.e("service is null on MainActivity.onToggleWorking(pause)");
				return;
			}
			service.stopServiceImmediately();
		}
	}

	public void onSettings(View view) {
		startActivity(new Intent(this, MyPreferenceActivity.class));
	}

	public void onNotWorksFine(View view) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Const.URL_WIKI_NOT_WORKS));
		startActivity(intent);
	}

	public void onWizard(View view) {
		InitialConfigurationWizardActivity.startWizard(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Const.REQUEST_ENABLE_BT) {

			// キャンセルされた場合
			if (resultCode != RESULT_OK) {
				Logger.i("ACTION_REQUEST_ENABLE canceled");

				BackgroundService service = BackgroundService.getInstance();
				if (service == null) {
					Logger.e("service is null on MainActivity.onActivityResult");
					return;
				}

				// ここで状態を進める
				service.onBluetoothConnected(false);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public enum ACTIVITY_FLAG {
		NONE, ACTION_DIALOG, BT_ENABLING, INIT_WIZARD
	}

	@SuppressLint("InlinedApi")
	public static void startActivity(Context context, ACTIVITY_FLAG flag) {
		Intent actIntent = new Intent(context, MainActivity.class);
		actIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);

		// Bluetooth有効化リクエスト
		if (flag == ACTIVITY_FLAG.BT_ENABLING) {
			actIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			actIntent.putExtra(Const.INTENT_EX_BT_ENABLING, true);
		}
		// アクションダイアログ表示
		else if (flag == ACTIVITY_FLAG.ACTION_DIALOG) {
			actIntent.putExtra(Const.INTENT_EX_ACTION_SELECT, true);
		}
		// 初期設定ウィザード
		else if (flag == ACTIVITY_FLAG.INIT_WIZARD) {
			// 呼び出し済なら起動しない
			if (initWizardDisplayed) {
				return;
			}
			actIntent.putExtra(Const.INTENT_EX_INIT_WIZARD, true);
		}

		context.startActivity(actIntent);
	}

	public void showActionDialog() {

		// 動作中の場合
		if (Const.getPrefWorking(this)) {

			// ラベル構築
			final String[] labels = getClickActionStringArray(true);

			// ダイアログ表示
			dialog = new AlertDialog.Builder(MainActivity.this).setTitle(R.string.select_action)
					.setItems(labels, new DialogInterface.OnClickListener() {

						// 選択時
						public void onClick(DialogInterface dialog, int which) {
							Context context = getApplicationContext();

							// 選択されたアクションを取得
							final String[] values = getClickActionStringArray(false);
							String action = values[which];

							// サービス停止
							if (MyStringUtlis.eqauls(action, getString(R.string.menu_widget_click_action__stop_service))) {
								BackgroundService service = BackgroundService.getInstance();
								if (service != null) {
									updateAsWorkingOrPausing(context, true);
									service.stopServiceImmediately();
								}
							}
							// サービス起動
							else {
								Intent srvIntent = new Intent(context, BackgroundService.class);
								srvIntent.putExtra(Const.INTENT_EX_DO_ACTION, action);
								startService(srvIntent);
							}
						}
					}).show();
		}
		// 一時停止中
		else {
			UIAct.toast(getString(R.string.pausing_long));
		}
	}

	protected String[] getClickActionStringArray(boolean label) {

		int id = label ? R.array.entries_menu_widget_click_action_on_choose
				: R.array.entryValues_menu_widget_click_action_on_choose;

		ArrayList<ListItem> labelList = CustomizeActionsActivity.constructListItemArrayFromCustomizedData(getResources()
				.getStringArray(id), Const.getPrefWidgetClickActionCustomizedData(this));
		int validCount = 0;
		for (ListItem listItem : labelList) {
			if (listItem.checked) {
				validCount++;
			}
		}

		int index = 0;
		String[] labels = new String[validCount];
		for (ListItem listItem : labelList) {
			if (listItem.checked) {
				labels[index++] = listItem.label;
			}
		}

		return labels;
	}

	protected void updateAsWorking(boolean wifiEnabled, boolean suppCompleted) {

		// UI更新
		UIAct.postActivityButton(true, true, wifiEnabled, suppCompleted);
		UIAct.postActivityInfo(R.drawable.antena_gray, getString(R.string.processing), null, null);

		// ウィジェット更新
		DefaultWidgetProvider.updateAsWorkingOrPausing(this, true);
	}

	public static void updateAsWorkingOrPausing(Context context, boolean working) {

		// UI更新
		UIAct.postActivityButton(false, false, null, null);
		UIAct.postActivityInfo(R.drawable.antena_gray, context.getString(working ? R.string.processing : R.string.pausing_en),
				null, null);

		// ウィジェット更新
		DefaultWidgetProvider.updateAsWorkingOrPausing(context, working);
	}

	public static void setInitWizardDisplayed(boolean displayed) {
		initWizardDisplayed = displayed;
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void uiactSetMessage(String wdText, String trigger, String state) {
		if (wdText != null) {
			((TextView) findViewById(R.id.wdText)).setText(wdText);
		}
		// 現状未使用
		//		if (trigger != null) {
		//			((TextView) findViewById(R.id.triggerText)).setText(trigger);
		//		}
		//		if (state != null) {
		//			((TextView) findViewById(R.id.stateText)).setText(state);
		//		}
	}

	public void uiactSwitchImage(Integer wdImageId) {
		if (wdImageId != null) {
			((ImageView) (findViewById(R.id.wdImage))).setImageResource(wdImageId);
		}
	}

	public void uiactSetRouterToggleButton(Boolean enable, Boolean suppCompleted) {
		Button routerButton = ((Button) (findViewById(R.id.toggleRouter)));
		if (enable != null) {
			routerButton.setEnabled(enable);
		}
		if (suppCompleted != null) {
			routerButton.setText(suppCompleted ? R.string.standby : R.string.resume);
		}
	}

	public void uiactSetWifiToggleButton(Boolean enable, Boolean wifiEnabled) {
		ToggleButton wifiToggle = (ToggleButton) findViewById(R.id.toggleWifi);
		if (enable != null) {
			wifiToggle.setEnabled(enable);
		}
		if (wifiEnabled != null) {
			wifiToggle.setChecked(wifiEnabled);
		}
	}

	public void uiactToggleWorkingToggleButton(Boolean enable) {
		ToggleButton workingToggle = (ToggleButton) findViewById(R.id.toggleWorking);
		workingToggle.setEnabled(enable);
	}
}