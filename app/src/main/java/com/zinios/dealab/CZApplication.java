package com.zinios.dealab;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zinios.dealab.api.response.Error;
import com.zinios.dealab.listener.NetworkListener;
import com.zinios.dealab.util.UserSessionManager;

import java.util.ArrayList;
import java.util.List;

public class CZApplication extends MultiDexApplication {
	private static final String TAG = CZApplication.class.getSimpleName();
	private static CZApplication application;
	private final List<NetworkListener> networkListeners = new ArrayList<>();

	public static CZApplication getInstance() {
		return application;
	}

	/**
	 * Called when the application is starting, before any activity, service,
	 * or receiver objects (excluding content providers) have been created.
	 * Implementations should be as quick as possible (for example using
	 * lazy initialization of state) since the time spent in this function
	 * directly impacts the performance of starting the first activity,
	 * service, or receiver in a process.
	 * If you override this method, be sure to call super.onCreate().
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		application = this;
		MultiDex.install(this);
	}

	public void addNetworkListener(NetworkListener listener) {
		networkListeners.add(listener);
	}

	public void removeNetworkListeners() {
		networkListeners.clear();
	}

	public List<NetworkListener> getNetworkListeners() {
		return networkListeners;
	}

	public String getVersion() {
		String versionName = null;
		try {
			versionName = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e("Error", e.getMessage());
		}
		return versionName;
	}

	public void restartApplication(boolean clearPref) {
		if (clearPref)
			UserSessionManager.getInstance().clearPref();
		Intent i = getBaseContext().getPackageManager()
				.getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}

	public boolean checkNetwork() {
		if (isNetworkConnected()) {
			return true;
		}
		Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
		return false;
	}

	public boolean isNetworkConnected() {
		ConnectivityManager cm =
				(ConnectivityManager) CZApplication.getInstance().getApplicationContext()
						.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			return activeNetwork != null &&
					activeNetwork.isConnectedOrConnecting();
		}
		return false;
	}

	public boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		if (manager == null)
			return false;
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void showError(Error error) {
		if (error != null) {
			if (!TextUtils.isEmpty(error.getMessage()))
				Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
			else if (!TextUtils.isEmpty(error.getData()))
				Toast.makeText(this, error.getData(), Toast.LENGTH_SHORT).show();
		}
	}

}
