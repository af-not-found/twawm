package com.appspot.afnf4199ga.twawm.app;

import java.io.File;
import java.io.FileInputStream;

import net.afnf.and.twawm2.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;

public class LogSendActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logsend);

        // 下線設定
        TextPaint textPaint = ((TextView) findViewById(R.id.textWhatKind)).getPaint();
        textPaint.setUnderlineText(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // オフライン、またはログ無効ならtoast
        boolean enable = true;
        if (hasConnectivity() == false) {
            enable = false;
            UIAct.toast(getString(R.string.send_log_not_online));
        }
        else if (Const.isPrefLoggingEnabled(this) == false) {
            enable = false;
            UIAct.toast(getString(R.string.send_log_logging_disabled));
        }

        // 有効化切り替え
        ((Spinner) findViewById(R.id.sendlog_report_type)).setEnabled(enable);
        ((EditText) findViewById(R.id.sendlog_message)).setEnabled(enable);
        ((Button) findViewById(R.id.sendLog)).setEnabled(enable);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        int itemId = ((Spinner) findViewById(R.id.sendlog_report_type)).getSelectedItemPosition();
        String msg = ((EditText) findViewById(R.id.sendlog_message)).getText().toString();

        bundle.putInt("sendlog_report_type", itemId);
        bundle.putString("sendlog_message", msg);
    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);

        int itemId = bundle.getInt("sendlog_report_type");
        String msg = bundle.getString("sendlog_message");

        ((Spinner) findViewById(R.id.sendlog_report_type)).setSelection(itemId);
        ((EditText) findViewById(R.id.sendlog_message)).setText(msg);
    }

    public void onWhatKind(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Const.URL_WIKI_LOGSEND_WHAT));
        startActivity(intent);
    }

    public void onReply(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Const.URL_WIKI_LOGSEND_REPLY));
        startActivity(intent);
    }

    public void onSendLog(View view) {

        // ボタン無効化
        Button btn = ((Button) view);
        btn.setEnabled(false);

        // 入力をロギング
        Logger.i("report:" + ((Spinner) findViewById(R.id.sendlog_report_type)).getSelectedItemPosition());
        Logger.i("message:\n" + ((EditText) findViewById(R.id.sendlog_message)).getText());

        // オンラインなら送信
        if (hasConnectivity()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendLog();
                }
            }).start();
        }
        else {
            UIAct.toast(getString(R.string.send_log_not_online));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////

    private boolean hasConnectivity() {
        boolean online = false;
        ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conn != null) {
            NetworkInfo activeNetworkInfo = conn.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                DetailedState detailedState = activeNetworkInfo.getDetailedState();
                if (detailedState == DetailedState.CONNECTED) {
                    online = true;
                }
            }
        }
        return online;
    }

    enum RET {
        OK, NG, NOFILE
    };

    private void sendLog() {

        // フラッシュ
        Logger.startFlushThread(true);

        RET ret = RET.NG;
        try {
            // ログ圧縮
            File archived = Logger.archive();

            if (archived != null) {

                // 圧縮完了toast
                UIAct.toast(getString(R.string.send_log_archived));

                // 3回までリトライ
                for (int i = 0; i < 3; i++) {

                    try {
                        // タイムアウト設定
                        BasicHttpParams httpParams = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
                        HttpConnectionParams.setSoTimeout(httpParams, 120000);

                        // クライアント作成
                        DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
                        HttpPost method = new HttpPost(Const.LOG_SEND_SERVER);
                        InputStreamEntity entity = new InputStreamEntity(new FileInputStream(archived), archived.length());
                        method.setEntity(entity);
                        method.addHeader("x-andreport-appname", Const.LOGTAG);

                        // 送信
                        HttpResponse response = httpClient.execute(method);
                        int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode == HttpStatus.SC_OK) {
                            ret = RET.OK;
                            Logger.i("sending succeeded");
                            break;
                        }
                        else {
                            ret = RET.NG;
                            Logger.w("sending failed, retry");
                        }
                    }
                    catch (Throwable e) {
                        ret = RET.NG;
                        Logger.w("sending failed, retry", e);
                    }

                    // リトライ
                    UIAct.toast(getString(R.string.send_log_retry));

                    // 少し待つ
                    AndroidUtils.sleep(5000 * (1 + i));
                }
            }
            else {
                ret = RET.NOFILE;
                Logger.w("sending nofile");
            }
        }
        catch (Throwable e) {
            ret = RET.NG;
            Logger.w("sending failed", e);
        }

        if (ret == RET.OK) {
            Logger.i("sending succeeded");
            UIAct.toast(getString(R.string.send_log_ok));
        }
        else {
            Logger.w("sending failed");
            UIAct.toast(getString(R.string.send_log_ng));
        }
    }
}
