package com.zinios.dealab.util;

import android.content.ComponentName;
import android.content.Intent;

public class Constants {

	public static final String NOTIFICATION_CHANNEL_ID = "107";
	public static final long[] VIBRATE_IN_APP = {30, 30};

	public static final Intent[] POWER_MANAGER_INTENTS = {
			new Intent().setComponent(new ComponentName("com.miui.securitycenter",
					"com.miui.permcenter.autostart.AutoStartManagementActivity")),
			new Intent().setComponent(new ComponentName("com.letv.android.letvsafe",
					"com.letv.android.letvsafe.AutobootManageActivity")),
			new Intent().setComponent(new ComponentName("com.huawei.systemmanager",
					"com.huawei.systemmanager.optimize.process.ProtectActivity")),
			new Intent().setComponent(new ComponentName("com.coloros.safecenter",
					"com.coloros.safecenter.permission.startup.StartupAppListActivity")),
			new Intent().setComponent(new ComponentName("com.coloros.safecenter",
					"com.coloros.safecenter.startupapp.StartupAppListActivity")),
			new Intent().setComponent(new ComponentName("com.oppo.safe",
					"com.oppo.safe.permission.startup.StartupAppListActivity")),
			new Intent().setComponent(new ComponentName("com.iqoo.secure",
					"com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
			new Intent().setComponent(new ComponentName("com.iqoo.secure",
					"com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
			new Intent().setComponent(new ComponentName("com.vivo.permissionmanager",
					"com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
			new Intent().setComponent(new ComponentName("com.asus.mobilemanager",
					"com.asus.mobilemanager.MainActivity"))
	};

	public static final String NEAR_BY_SPOTS_LAT = "d_lat";
	public static final String NEAR_BY_SPOTS_LNG = "d_lng";
}
