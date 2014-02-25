package com.appspot.afnf4199ga.twawm.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.twawm.R;
import com.appspot.afnf4199ga.twawm.StateMachine;
import com.appspot.afnf4199ga.twawm.TwawmUtils;
import com.appspot.afnf4199ga.utils.AndroidUtils;

public class DefaultWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		// サービスが無い場合
		BackgroundService service = BackgroundService.getInstance();
		if (service == null) {

			// ※ここではサービス起動しない

			// Wifi無効ならイメージ・テキスト更新
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			if (AndroidUtils.isWifiDisabled(wifi)) {
				RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_default);
				rv.setImageViewResource(R.id.widgetImage, R.drawable.antena_gray);
				rv.setTextViewText(R.id.widgetText, context.getString(R.string.wifi_off));

				// updateAppWidget
				for (int id : appWidgetIds) {
					appWidgetManager.updateAppWidget(id, rv);
				}
			}
		}

		// サービスがある場合
		else {

			// 情報取得
			StateMachine stateMachine = service.getStateMachine();
			String wdText = stateMachine.getWdText();
			int wdImageId = stateMachine.getWdImageId();

			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_default);
			if (wdImageId > 0) {
				rv.setImageViewResource(R.id.widgetImage, wdImageId);
			}
			if (wdText != null) {
				rv.setTextViewText(R.id.widgetText, wdText);
			}

			// updateAppWidget
			for (int id : appWidgetIds) {
				appWidgetManager.updateAppWidget(id, rv);
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		//Logger.v("Widget onReceive intent=" + AndroidUtils.getActionForLog(intent));

		// デフォルトの処理
		super.onReceive(context, intent);

		// click時のIntent設定
		setClickIntent(context);

		// ACTION_APPWIDGET_UPDATEはonReceiveで実行されるので、それ以外
		if (AndroidUtils.isActionEquals(intent, AppWidgetManager.ACTION_APPWIDGET_UPDATE) == false) {
			// onUpdateに委譲
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			int[] appWidgetIds = getWidgetIds(appWidgetManager, context);
			onUpdate(context, appWidgetManager, appWidgetIds);

			// Style変更
			if (AndroidUtils.isActionEquals(intent, Const.INTENT_WD_CHANGE_STYLE)) {
				RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_default);

				int tc = TwawmUtils.getValue2ColorIndex(context, Const.getPrefWidgetStrColor(context));
				rv.setTextColor(R.id.widgetText, context.getResources().getColor(tc));
				int[] bg = TwawmUtils.getValue2ResourceIndex(context, Const.getPrefWidgetBackground(context));
				rv.setInt(R.id.widgetDefaultLayout, "setBackgroundResource", bg[0]);

				// updateAppWidget
				for (int id : appWidgetIds) {
					appWidgetManager.updateAppWidget(id, rv);
				}
			}
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void setClickIntent(Context context) {

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), DefaultWidgetProvider.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		for (int i = 0; i < appWidgetIds.length; i++) {

			Intent intent = new Intent(context, BackgroundService.class);
			intent.setAction(Const.INTENT_WD_CLICKED);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			//intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds); // 未使用

			PendingIntent pendingIntent = PendingIntent.getService(context, appWidgetIds[i], intent, 0); // putExtraする場合、第2引数(requestCode)の指定が必須
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_default);
			//rv.setOnClickPendingIntent(R.id.widgetImage, pendingIntent); // 不要らしい
			//rv.setOnClickPendingIntent(R.id.widgetText, pendingIntent); // 不要らしい
			rv.setOnClickPendingIntent(R.id.widgetDefaultLayout, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
		}
	}

	public static void showClickAnimation(Context context, int widgetId) {
		ClickAnimateThread thread = new ClickAnimateThread();
		thread.context = context;
		thread.widgetId = widgetId;
		thread.start();
	}

	static class ClickAnimateThread extends Thread {
		Context context;
		int widgetId;

		@Override
		public void run() {
			try {
				String bgstr = Const.getPrefWidgetBackground(context);
				int[] bg = TwawmUtils.getValue2ResourceIndex(context, bgstr);
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_default);
				rv.setInt(R.id.widgetDefaultLayout, "setBackgroundResource", bg[1]);
				appWidgetManager.updateAppWidget(widgetId, rv);
				AndroidUtils.sleep(250);
				rv.setInt(R.id.widgetDefaultLayout, "setBackgroundResource", bg[0]);
				appWidgetManager.updateAppWidget(widgetId, rv);
			}
			catch (Throwable e) {
				// do nothing
			}
		}
	}

	public static void update(Context context) {
		Intent intent = new Intent(context, DefaultWidgetProvider.class);
		intent.setAction(Const.INTENT_WD_UPDATE);
		context.sendBroadcast(intent);
	}

	public static void changeStyle(Context context) {
		Intent intent = new Intent(context, DefaultWidgetProvider.class);
		intent.setAction(Const.INTENT_WD_CHANGE_STYLE);
		context.sendBroadcast(intent);
	}

	public static boolean hasWidget(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = getWidgetIds(appWidgetManager, context);
		return (appWidgetIds == null || appWidgetIds.length == 0) == false;
	}

	private static int[] getWidgetIds(AppWidgetManager appWidgetManager, Context context) {
		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), DefaultWidgetProvider.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		return appWidgetIds;
	}

	public static void updateAsWorkingOrPausing(Context context, boolean working) {

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisAppWidget = new ComponentName(context.getPackageName(), DefaultWidgetProvider.class.getName());
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_default);
		rv.setImageViewResource(R.id.widgetImage, R.drawable.antena_gray);
		rv.setTextViewText(R.id.widgetText, context.getString(working ? R.string.processing : R.string.pausing_en));

		// updateAppWidget
		for (int id : appWidgetIds) {
			appWidgetManager.updateAppWidget(id, rv);
		}
	}
}
