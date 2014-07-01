package com.appspot.afnf4199ga.twawm.app;

import java.util.EnumSet;
import java.util.Iterator;

import net.afnf.and.twawm2.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appspot.afnf4199ga.twawm.Const;
import com.appspot.afnf4199ga.twawm.router.RouterControlByHttp;
import com.appspot.afnf4199ga.twawm.router.RouterControlByHttp.CTRL;
import com.appspot.afnf4199ga.twawm.router.RouterInfo;
import com.appspot.afnf4199ga.utils.Logger;

public class InitialConfigurationWizardActivity extends Activity {

    private ProgressDialog progressDialog;
    private PAGE page = PAGE.INIT;
    private boolean infoBtn = false;

    enum PAGE {
        INIT, CONNECTION, INPUT_ROUTER_IP, PASSWORD_NOT_INIT, INPUT_PASSWORD1, INPUT_PASSWORD2, INVALID_PASSWORD, FIN;

        public static PAGE ordinalOf(int ordinal) {
            Iterator<PAGE> ite = EnumSet.allOf(PAGE.class).iterator();
            while (ite.hasNext()) {
                PAGE e = ite.next();
                if (e.ordinal() == ordinal)
                    return e;
            }
            return null;
        }
    }

    public void onCancel(View view) {
        if (page == PAGE.INIT) {
            Const.updatePrefStartWizardAutomatically(this, false);
        }

        MainActivity.setInitWizardDisplayed(false);
        finish();
    }

    public void onSubButton(View view) {

        switch (page) {

        case CONNECTION:
            Intent it1 = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
            startActivity(it1);
            break;

        case PASSWORD_NOT_INIT:
            // ブラウザ起動
            Intent it2 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + Const.ROUTER_HOSTNAME
                    + (RouterControlByHttp.isNad() ? Const.ROUTER_URL_INFO_IDXCT : "/")));
            startActivity(it2);
            break;

        default:
            // do nothing
            break;
        }
    }

    public static void startWizard(Context context) {
        Intent intent = new Intent(context, InitialConfigurationWizardActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onPause() {
        //Logger.v("WizardActivity onPause");
        super.onPause();

        // ダイアログを閉じる（メモリリーク防止）
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        //Logger.v("WizardActivity onDestroy");
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Logger.v("WizardActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard);

        Button nextButton = (Button) findViewById(R.id.wizardNextButton);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = InitialConfigurationWizardActivity.this;

                // 終了画面ならそのまま抜ける
                if (page == PAGE.FIN) {
                    // オンラインチェック再開
                    BackgroundService service = BackgroundService.getInstance();
                    if (service != null) {
                        service.startOnlineCheck(0);
                    }
                    // ウィザード終了
                    MainActivity.setInitWizardDisplayed(false);
                    finish();
                    return;
                }
                else {

                    // パスワード、ルーターIP保存
                    EditText editText = (EditText) findViewById(R.id.wizardEditText);
                    if (editText != null) {
                        if (editText.getText() != null) {
                            String editTextStr = editText.getText().toString();
                            switch (page) {
                            case INPUT_ROUTER_IP:
                                Const.updatePrefApIpAddr(context, editTextStr);
                                break;
                            case INPUT_PASSWORD1:
                            case INPUT_PASSWORD2:
                            case INVALID_PASSWORD:
                                Const.updatePrefRouterControlPassword(context, editTextStr);
                                break;
                            default:
                                break;
                            }
                        }
                    }

                    // WiFi未接続なら、接続を促す
                    int nextInt;
                    if (isSupplicantCompleted() == false) {
                        nextInt = PAGE.CONNECTION.ordinal();

                        // 次画面遷移
                        nextActivity(nextInt);
                    }

                    // WiFi接続済ならルーターチェック
                    else {

                        // ProgressDialog表示
                        progressDialog = new ProgressDialog(context);
                        progressDialog.setMessage(getString(R.string.wizard_dialog_loading));
                        progressDialog.setCancelable(false);
                        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        progressDialog = null;
                                    }
                                });
                        progressDialog.show();

                        // ルーター状態チェック開始
                        new CheckRouterThread().start();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        //Logger.v("WizardActivity onStart");
        super.onStart();
        MainActivity.setInitWizardDisplayed(true);

        // ページ情報を取得
        Intent intent = getIntent();
        int pageInt = intent.getIntExtra(Const.INTENT_EX_ACTION_SELECT, PAGE.INIT.ordinal());
        page = PAGE.ordinalOf(pageInt);
        Logger.i("InitialConfigurationWizard, page=" + page);

        int progress = 0;
        int mainTextId = R.string.wizard_text_init;
        boolean dispEditText = false;
        String editTextValue = null;
        int subButtonTextId = -1;
        int nextButtonTextId = -1;
        int cancelButtonTextId = R.string.cancel;

        switch (page) {
        case INIT:
            infoBtn = false;
            cancelButtonTextId = R.string.wizard_button_dont_start_wizard_automatically;
            break;

        case CONNECTION:
            mainTextId = R.string.wizard_text_connection;
            subButtonTextId = R.string.wizard_button_wifi;
            progress = 20;
            break;

        case INPUT_ROUTER_IP:
            mainTextId = R.string.wizard_text_input_router_ip;
            dispEditText = true;
            editTextValue = Const.getPrefApIpAddr(this);
            progress = 40;
            break;

        case PASSWORD_NOT_INIT:
            mainTextId = R.string.wizard_text_password_not_init;
            subButtonTextId = R.string.wizard_button_router;
            progress = 50;
            break;

        case INPUT_PASSWORD1:
            mainTextId = R.string.wizard_text_input_password1;
            dispEditText = true;
            progress = 60;
            break;

        case INPUT_PASSWORD2:
            mainTextId = R.string.wizard_text_input_password2;
            dispEditText = true;
            progress = 80;
            break;

        case INVALID_PASSWORD:
            mainTextId = R.string.wizard_text_invalid_password;
            dispEditText = true;
            progress = 90;
            break;

        case FIN:
            mainTextId = R.string.wizard_text_fin;
            cancelButtonTextId = -1;
            progress = 100;
            nextButtonTextId = R.string.wizard_button_fin;
            break;

        default:
            break;
        }

        // 進捗
        TextView progressText = (TextView) findViewById(R.id.wizardProgressText);
        progressText.setText(progress + "%");
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(progress);

        // メインテキスト
        TextView mainText = (TextView) findViewById(R.id.wizardMainText);
        mainText.setText(getString(mainTextId));

        // 入力
        EditText editText = (EditText) findViewById(R.id.wizardEditText);
        if (dispEditText == false) {
            editText.setVisibility(View.INVISIBLE);
        }
        else if (editTextValue != null) {
            editText.setText(editTextValue);
        }

        // サブボタン
        Button subButton = (Button) findViewById(R.id.wizardSubButton);
        if (subButtonTextId != -1) {
            subButton.setText(getString(subButtonTextId));
        }
        else {
            subButton.setVisibility(View.INVISIBLE);
        }

        // 次ボタン
        if (nextButtonTextId != -1) {
            Button nextButton = (Button) findViewById(R.id.wizardNextButton);
            nextButton.setText(getString(nextButtonTextId));
        }

        // キャンセルボタン
        Button cancelButton = (Button) findViewById(R.id.wizardCancelButton);
        if (cancelButtonTextId != -1) {
            cancelButton.setText(getString(cancelButtonTextId));
        }
        else {
            cancelButton.setVisibility(View.INVISIBLE);
        }
    }

    private void nextActivity(int nextInt) {

        // 次画面遷移
        Intent intent = new Intent(this, InitialConfigurationWizardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Const.INTENT_EX_ACTION_SELECT, nextInt);
        startActivity(intent);
    }

    private class CheckRouterThread extends Thread {
        @Override
        public void run() {
            int nextInt = checkRouter();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
                // 次画面遷移
                nextActivity(nextInt);
            }
        }
    }

    private boolean isSupplicantCompleted() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled() && BackgroundService.isSupplicantCompleted(wifi);
    }

    private int checkRouter() {
        RouterInfo routerInfo = new RouterInfo();

        // ルーターにアクセス
        CTRL ctrl = infoBtn ? CTRL.GET_INFO_FORCE_RMTMAIN : CTRL.GET_INFO_FORCE_INFOBTN;
        int ret = RouterControlByHttp.exec(this, ctrl, routerInfo);
        Logger.i("checkRouter, ret=" + ret + ", stby=" + routerInfo.hasStandbyButton);

        // 正常
        if (ret == RouterControlByHttp.CTRL_OK) {
            // スタンバイボタン有り
            if (routerInfo.hasStandbyButton) {
                return PAGE.FIN.ordinal();
            }
            // RMTMAINにスタンバイボタンが無い場合、INFOBTNにアクセスする
            else if (infoBtn) {
                infoBtn = false;
                return checkRouter();
            }
        }

        // パスワード未設定
        if (ret == RouterControlByHttp.CTRL_PASS_NOT_INITIALIZED) {
            return PAGE.PASSWORD_NOT_INIT.ordinal();
        }
        // 認証失敗
        else if (ret == RouterControlByHttp.CTRL_UNAUTHORIZED) {
            if (page == PAGE.INPUT_PASSWORD1 || page == PAGE.INPUT_PASSWORD2 || page == PAGE.INVALID_PASSWORD) {
                return PAGE.INVALID_PASSWORD.ordinal();
            }
            else if (page == PAGE.PASSWORD_NOT_INIT) {
                return PAGE.INPUT_PASSWORD1.ordinal();
            }
            else {
                return PAGE.INPUT_PASSWORD2.ordinal();
            }
        }
        // それ以外
        else {
            // ルーターIP入力
            return PAGE.INPUT_ROUTER_IP.ordinal();
        }
    }
}
