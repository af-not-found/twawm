package com.appspot.afnf4199ga.wmgraph.app;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.appspot.afnf4199ga.twawm.router.RouterInfo;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.utils.MyStringUtlis;
import com.appspot.afnf4199ga.wmgraph.R;

public class MainActivity extends Activity {

	private static MainActivity instance;
	private static boolean passNotInitializedAlreadyWarned = false;

	private XYMultipleSeriesDataset dataset;
	private XYMultipleSeriesRenderer renderer;
	private GraphicalView cubeLineChartView;
	private FetchThread updateThread;
	private InetLookupThread lookupThread;
	private int selection = 1;
	private int itemCount = 0;
	private boolean threadStarting = false;

	public static int interval;

	public static MainActivity getInstance() {
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		instance = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// グラフ初期化
		initGraph();

		// ロード中表示
		loadingToast();

		// Spinner初期化
		Spinner spinner_interval_type = (Spinner) findViewById(R.id.spinner_interval_type);
		spinner_interval_type.setSelection(selection);
		spinner_interval_type.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selection = position;
				delayedIntervalChange();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		// テキスト更新
		String ant = getString(R.string.ant_label);
		ant = MyStringUtlis.replaceFirst(ant, "N/A", getString(R.string.loading));
		((TextView) findViewById(R.id.sc_text_ant)).setText(ant);
	}

	@Override
	protected void onStart() {
		instance = this;
		super.onStart();
		UIAct.init(this);

		// onIntervalChangedを遅延実行
		delayedIntervalChange();

		// ロード中表示
		loadingToast();
	}

	@Override
	protected void onResume() {
		instance = this;
		super.onResume();
		UIAct.init(this);

		// onIntervalChangedを遅延実行
		delayedIntervalChange();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopThread();
		Logger.i("onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopThread();
		Logger.i("onStop");

		Logger.startFlushThread(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopThread();
		Logger.i("onDestroy");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int i = 1;
		menu.add(Menu.NONE, i++, Menu.NONE, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences)
				.setIntent(new Intent(this, MyPreferenceActivity.class));
		menu.add(Menu.NONE, i++, Menu.NONE, R.string.info).setIcon(android.R.drawable.ic_menu_info_details)
				.setIntent(new Intent(this, InfoActivity.class));

		return super.onCreateOptionsMenu(menu);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static float SCALE = 1.0f;
	private static final int LIGHT_BLUE = Color.BLUE; //Color.rgb(0x99, 0x99, 0xff);

	private void initGraph() {

		// scaledDensity取得
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Logger.v("scaledDensity:" + metrics.scaledDensity);
		SCALE = metrics.scaledDensity;

		int[] colors = new int[] { LIGHT_BLUE, Color.RED };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT, PointStyle.POINT };
		renderer = new XYMultipleSeriesRenderer(2);
		renderer.setAxisTitleTextSize(16 * SCALE);
		renderer.setChartTitleTextSize(20 * SCALE);
		renderer.setLabelsTextSize(15 * SCALE);
		renderer.setShowLegend(false);
		renderer.setPointSize(5f * SCALE);
		renderer.setMargins(new int[] { (int) (10 * SCALE), (int) (40 * SCALE), (int) (5 * SCALE), (int) (30 * SCALE) }); // top, left, bottom, right
		renderer.setXAxisMin(1);
		renderer.setXAxisMax(10);
		renderer.setYAxisMin(1);
		renderer.setYAxisMax(10);
		renderer.setAxesColor(Color.GRAY);
		renderer.setGridColor(Color.LTGRAY);
		renderer.setXLabels(6);
		renderer.setYLabels(8);
		renderer.setShowGrid(true);
		renderer.setXLabelsAlign(Align.CENTER);
		renderer.setYLabelsAlign(Align.RIGHT, 0);
		renderer.setYLabelsAlign(Align.LEFT, 1);
		renderer.setZoomButtonsVisible(false);
		renderer.setApplyBackgroundColor(true);
		renderer.setBackgroundColor(Color.WHITE);
		renderer.setMarginsColor(Color.WHITE);
		renderer.setLabelsColor(Color.BLACK);
		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYTitle("RSSI", 0);
		renderer.setYAxisAlign(Align.LEFT, 0);
		renderer.setYTitle("CINR", 1);
		renderer.setYAxisAlign(Align.RIGHT, 1);

		// dataset構築
		dataset = new XYMultipleSeriesDataset();
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			r.setLineWidth(3f * SCALE);
			renderer.addSeriesRenderer(r);
			renderer.setYLabelsColor(i, colors[i]);

			XYSeries series = new XYSeries("", i);
			dataset.addSeries(series);
		}

		// グラフ追加
		cubeLineChartView = ChartFactory.getCubeLineChartView(this, dataset, renderer, 0.3f);
		cubeLineChartView.setBackgroundColor(Color.WHITE);
		LinearLayout graph_layout = (LinearLayout) findViewById(R.id.graph_layout);
		graph_layout.addView(cubeLineChartView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	public void repaint(RouterInfo routerInfo) {

		boolean valid = false;
		double y0 = 0;
		double y1 = 0;

		String antennaLevelText = "N/A";
		String rssiText = "N/A";
		String cinrText = "N/A";

		if (routerInfo != null) {
			antennaLevelText = routerInfo.antennaLevelText + "/6";

			String rssiTextTmp = routerInfo.rssiText;
			String cinrTextTmp = routerInfo.cinrText;
			if (MyStringUtlis.isEmpty(rssiTextTmp) == false && MyStringUtlis.isEmpty(cinrTextTmp) == false) {
				try {
					y0 = Integer.parseInt(rssiTextTmp);
					y1 = Integer.parseInt(cinrTextTmp);
					valid = true;
					rssiText = rssiTextTmp;
					cinrText = cinrTextTmp;
				}
				catch (NumberFormatException e) {
					Logger.w("NumberFormatException, rssiText=" + rssiText + ", cinrText" + cinrText);
				}
			}

			// アイテムカウントによるロード中表示
			if (valid) {
				loadingToast();
			}
			// 失敗した場合はロード中表示
			else {
				UIAct.toast(getString(R.string.loading));
			}
		}
		else {
			UIAct.toast(getString(R.string.failed));
		}

		// テキスト更新
		{
			String ant = getString(R.string.ant_label);
			String rssi = getString(R.string.rssi_label);
			String cinr = getString(R.string.cinr_label);
			ant = MyStringUtlis.replaceFirst(ant, "N/A", antennaLevelText);
			rssi = MyStringUtlis.replaceFirst(rssi, "N/A", rssiText);
			cinr = MyStringUtlis.replaceFirst(cinr, "N/A", cinrText);
			((TextView) findViewById(R.id.sc_text_ant)).setText(ant);
			((TextView) findViewById(R.id.sc_text_rssi)).setText(rssi);
			((TextView) findViewById(R.id.sc_text_cinr)).setText(cinr);
		}

		// 有効ならグラフ更新
		if (valid) {

			// 値の追加
			itemCount++;
			XYSeries series0 = dataset.getSeriesAt(0);
			XYSeries series1 = dataset.getSeriesAt(1);
			series0.add(itemCount, y0);
			series1.add(itemCount, y1);

			// X軸表示域設定
			int maxX = itemCount + 1;
			renderer.setXAxisMax(maxX, 0);
			renderer.setXAxisMax(maxX, 1);
			int minX = itemCount > 40 ? itemCount - 40 : 0;
			renderer.setXAxisMin(minX, 0);
			renderer.setXAxisMin(minX, 1);

			// Y軸表示域設定
			double minY0 = Integer.MAX_VALUE, maxY0 = Integer.MIN_VALUE;
			double minY1 = Integer.MAX_VALUE, maxY1 = Integer.MIN_VALUE;
			for (int i = minX; i < itemCount; i++) {
				double iy0 = series0.getY(i);
				if (minY0 > iy0) {
					minY0 = iy0;
				}
				if (maxY0 < iy0) {
					maxY0 = iy0;
				}
				double iy1 = series1.getY(i);
				if (minY1 > iy1) {
					minY1 = iy1;
				}
				if (maxY1 < iy1) {
					maxY1 = iy1;
				}
			}
			renderer.setYAxisMin((int) (minY0 - 5), 0);
			renderer.setYAxisMax((int) (maxY0 + 3), 0);
			renderer.setYAxisMin((int) (minY1 - 1), 1);
			renderer.setYAxisMax((int) (maxY1 + 2), 1);

			// 再描画
			cubeLineChartView.repaint();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private synchronized void stopThread() {
		if (updateThread != null) {
			updateThread.working = false;
			updateThread.interrupt();
			updateThread = null;
		}
		if (lookupThread != null) {
			lookupThread.working = false;
			lookupThread.interrupt();
			lookupThread = null;
		}
	}

	private synchronized void onIntervalChanged() {
		stopThread();

		final String[] entryValues = getResources().getStringArray(R.array.interval_type_values);
		interval = Integer.parseInt(entryValues[selection]) * 1000;

		if (interval >= 0) {
			updateThread = new FetchThread();
			updateThread.start();
			lookupThread = new InetLookupThread();
			lookupThread.start();
		}
	}

	private synchronized void delayedIntervalChange() {

		if (threadStarting == false) {
			threadStarting = true;

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					}
					catch (InterruptedException e) {
					}
					onIntervalChanged();
					threadStarting = false;
				}
			}).start();
		}
	}

	private void loadingToast() {
		if (itemCount <= 1) {
			UIAct.toast(getString(R.string.loading));
		}
	}

	public void passNotInitialized() {

		if (passNotInitializedAlreadyWarned == false) {
			passNotInitializedAlreadyWarned = true;
			// toast
			UIAct.resetLastToast();
			UIAct.toast(getString(R.string.pass_not_initialized));
			// ブラウザ起動
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.0.1/"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
}
