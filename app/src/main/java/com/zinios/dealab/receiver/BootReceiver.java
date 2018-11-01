package com.zinios.dealab.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.zinios.dealab.DealabApplication;
import com.zinios.dealab.service.DeviceLocationService;

/**
 * Created by vihanga on 5/11/18
 */
public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!DealabApplication.getInstance().isServiceRunning(DeviceLocationService.class)) {
			Log.d(BootReceiver.class.getSimpleName(), "DeviceLocationService starting...");
			Intent service = new Intent(context, DeviceLocationService.class);
			Bundle bundle = new Bundle();
			service.putExtras(bundle);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(service);
			} else {
				context.startService(service);
			}
		} else {
			Log.d(BootReceiver.class.getSimpleName(), "DeviceLocationService is already running...");
		}
	}
}
