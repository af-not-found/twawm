package com.appspot.afnf4199ga.twawm.app;

import android.os.Handler;
import android.widget.Toast;

import com.appspot.afnf4199ga.twawm.router.RouterInfo.COM_TYPE;
import com.appspot.afnf4199ga.utils.AndroidUtils;
import com.appspot.afnf4199ga.utils.Logger;

public class UIAct extends Handler {
    private static UIAct instance = new UIAct();
    private static MainActivity parent;

    private UIAct() {
    }

    public static void init(MainActivity parent) {
        UIAct.parent = parent;
    }

    public static void destroy() {
        UIAct.parent = null;
    }

    static class UpdateActivityButtonRunnable implements Runnable {

        private Boolean enableRouterToggle;
        private Boolean enableWifiToggle;
        private Boolean wifiEnabled;
        private Boolean suppCompleted;
        private Boolean ecoCharge;
        private COM_TYPE comSetting;
        private Boolean wifiSpot;

        public UpdateActivityButtonRunnable(Boolean enableRouterToggle, Boolean enableWifiToggle, Boolean wifiEnabled,
                Boolean suppCompleted, Boolean ecoCharge, COM_TYPE comSetting, Boolean wifiSpot) {
            this.enableRouterToggle = enableRouterToggle;
            this.enableWifiToggle = enableWifiToggle;
            this.wifiEnabled = wifiEnabled;
            this.suppCompleted = suppCompleted;
            this.ecoCharge = ecoCharge;
            this.comSetting = comSetting;
            this.wifiSpot = wifiSpot;
        }

        public void run() {
            if (parent != null) {
                parent.uiactSetRouterToggleButton(enableRouterToggle, suppCompleted);
                parent.uiactSetWifiToggleButton(enableWifiToggle, wifiEnabled);
                parent.uiactSetEcoChargeToggleButton(suppCompleted, ecoCharge);
                parent.uiactSetNadToggleButton(suppCompleted, comSetting, wifiSpot);
            }
        }
    }

    public static void postActivityButton(Boolean enableRouterToggle, Boolean enableWifiToggle, Boolean wifiEnabled,
            Boolean suppCompleted, Boolean ecoCharge, COM_TYPE comSetting, Boolean wifiSpot) {
        if (parent != null) {
            if (AndroidUtils.isUIThread(parent)) {
                parent.uiactSetRouterToggleButton(enableRouterToggle, suppCompleted);
                parent.uiactSetWifiToggleButton(enableWifiToggle, wifiEnabled);
                parent.uiactSetEcoChargeToggleButton(suppCompleted, ecoCharge);
                parent.uiactSetNadToggleButton(suppCompleted, comSetting, wifiSpot);
            }
            else {
                instance.post(new UpdateActivityButtonRunnable(enableRouterToggle, enableWifiToggle, wifiEnabled, suppCompleted,
                        ecoCharge, comSetting, wifiSpot));
            }
        }
    }

    static class UpdateActivityInfoRunnable implements Runnable {

        private Integer wdImageId;
        private String wdText;
        private String trigger;
        private String state;

        public UpdateActivityInfoRunnable(Integer wdImageId, String wdText, String trigger, String state) {
            this.wdImageId = wdImageId;
            this.wdText = wdText;
            this.trigger = trigger;
            this.state = state;
        }

        public void run() {
            if (parent != null) {
                parent.uiactSwitchImage(wdImageId);
                parent.uiactSetMessage(wdText, trigger, state);
            }
        }
    }

    public static void postActivityInfo(Integer wdImageId, String wdText, String trigger, String state) {
        if (parent != null) {
            if (AndroidUtils.isUIThread(parent)) {
                parent.uiactSwitchImage(wdImageId);
                parent.uiactSetMessage(wdText, trigger, state);
            }
            else {
                instance.post(new UpdateActivityInfoRunnable(wdImageId, wdText, trigger, state));
            }
        }
    }

    static class EnableWorkingToggleButtonRunnable implements Runnable {
        public void run() {
            if (parent != null) {
                parent.uiactToggleWorkingToggleButton(true);
            }
        }
    }

    public static void postDelayedEnableWorkingToggleButton() {
        if (parent != null) {
            instance.postDelayed(new EnableWorkingToggleButtonRunnable(), 2000);
        }
    }

    static class EnableComModeToggleButtonRunnable implements Runnable {
        public void run() {
            if (parent != null) {
                parent.uiactToggleComModeToggleButton(true);
            }
        }
    }

    public static void postDelayedEnableComModeToggleButton() {
        if (parent != null) {
            instance.postDelayed(new EnableComModeToggleButtonRunnable(), 8000);
        }
    }

    static class EnableWifiSpotToggleButtonRunnable implements Runnable {
        public void run() {
            if (parent != null) {
                parent.uiactToggleWifiSpotToggleButton(true);
            }
        }
    }

    public static void postDelayedEnableWifiSpotToggleButton() {
        if (parent != null) {
            instance.postDelayed(new EnableWifiSpotToggleButtonRunnable(), 8000);
        }
    }

    static class ToastRunnable implements Runnable {
        private String msg;

        public ToastRunnable(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            try {
                Toast.makeText(parent, msg, Toast.LENGTH_LONG).show();
            }
            catch (Throwable e) {
                Logger.w("toast error", e);
            }
        }
    }

    public static void toast(String msg) {
        if (parent != null) {
            instance.post(new ToastRunnable(msg));
        }
    }
}
