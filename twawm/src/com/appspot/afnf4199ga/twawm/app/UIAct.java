package com.appspot.afnf4199ga.twawm.app;

import android.os.Handler;
import android.widget.Toast;

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

        public UpdateActivityButtonRunnable(Boolean enableRouterToggle, Boolean enableWifiToggle, Boolean wifiEnabled,
                Boolean suppCompleted, Boolean ecoCharge) {
            this.enableRouterToggle = enableRouterToggle;
            this.enableWifiToggle = enableWifiToggle;
            this.wifiEnabled = wifiEnabled;
            this.suppCompleted = suppCompleted;
            this.ecoCharge = ecoCharge;
        }

        public void run() {
            if (parent != null) {
                parent.uiactSetRouterToggleButton(enableRouterToggle, suppCompleted);
                parent.uiactSetWifiToggleButton(enableWifiToggle, wifiEnabled);
                parent.uiactSetEcoChargeToggleButton(ecoCharge);
            }
        }
    }

    public static void postActivityButton(Boolean enableRouterToggle, Boolean enableWifiToggle, Boolean wifiEnabled,
            Boolean suppCompleted, Boolean ecoCharge) {
        if (parent != null) {
            if (AndroidUtils.isUIThread(parent)) {
                parent.uiactSetRouterToggleButton(enableRouterToggle, suppCompleted);
                parent.uiactSetWifiToggleButton(enableWifiToggle, wifiEnabled);
                parent.uiactSetEcoChargeToggleButton(ecoCharge);
            }
            else {
                instance.post(new UpdateActivityButtonRunnable(enableRouterToggle, enableWifiToggle, wifiEnabled, suppCompleted,
                        ecoCharge));
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AndroidUtils.sleep(2000);
                    instance.post(new EnableWorkingToggleButtonRunnable());
                }
            }).start();
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
