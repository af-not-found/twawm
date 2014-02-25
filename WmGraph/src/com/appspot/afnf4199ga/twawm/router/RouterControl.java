package com.appspot.afnf4199ga.twawm.router;

import com.appspot.afnf4199ga.twawm.router.RouterControlByHttp.CTRL;
import com.appspot.afnf4199ga.utils.Logger;
import com.appspot.afnf4199ga.wmgraph.app.MainActivity;

public class RouterControl {

	public static RouterInfo execFetchInfo() {

		RouterInfo routerInfo = new RouterInfo();
		int ret = RouterControlByHttp.exec(CTRL.GET_INFO, routerInfo);

		if (ret == RouterControlByHttp.CTRL_OK) {

			Logger.i("router GET_INFO, ret=" + ret);
			//Logger.i("router GET_INFO, ret=" + ret + ", batt=" + routerInfo.batteryText + ", ant=" + routerInfo.antennaLevelText);
		}
		// 管理画面パスワード未設定時
		else if (ret == RouterControlByHttp.CTRL_PASS_NOT_INITIALIZED) {
			MainActivity.getInstance().passNotInitialized();
		}
		else {
			Logger.w("router GET_INFO failed, ret=" + ret);
			routerInfo = null;
		}

		return routerInfo;
	}
}
