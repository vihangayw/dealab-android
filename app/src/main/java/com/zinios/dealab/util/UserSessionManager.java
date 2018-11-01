package com.zinios.dealab.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.zinios.dealab.BuildConfig;
import com.zinios.dealab.DealabApplication;

public class UserSessionManager {

	private static final String PREF_NAME = BuildConfig.APPLICATION_ID + ".pref";
	private static final String KEY_AUTH_TOKEN = "AuthToken";
	private static final String KEY_USER = "User";
	private static final String KEY_IS_LOGIN = "IsLogin";
	private static final String IS_PM_ALLOWED = "PackageManagerAllowed";
	private static final String LAST_LAT = "LastLat";
	private static final String LAST_LNG = "LastLng";

	private final static UserSessionManager instance =
			new UserSessionManager(DealabApplication.getInstance().getApplicationContext());

	private final SharedPreferences pref;
	private final SharedPreferences.Editor editor;

	private UserSessionManager(Context context) {
		int PRIVATE_MODE = 0;
		pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}

	public static UserSessionManager getInstance() {
		return instance;
	}

	public void createUser(String json, String token) {
		editor.putString(KEY_USER, json);
		editor.putString(KEY_AUTH_TOKEN, token);
		editor.putBoolean(KEY_IS_LOGIN, true);
		editor.commit();
	}

	public void saveLastNotificationLocation(double lat, double lng) {
		editor.putString(LAST_LAT, String.valueOf(lat));
		editor.putString(LAST_LNG, String.valueOf(lng));
		editor.commit();
	}

	public LatLng getLastNotificationLocation() {
		String lat = pref.getString(LAST_LAT, null);
		String lng = pref.getString(LAST_LNG, null);

		if (lat == null || lng == null) return null;

		try {
			return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	//	public User getUser() {
//		String modulePref = getUserPref();
//		if (!TextUtils.isEmpty(modulePref)) {
//			try {
//				return new ObjectMapper().readValue(modulePref, User.class);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return null;
//	}
	public void setPowerManagerAllowed() {
		editor.putBoolean(IS_PM_ALLOWED, true);
		editor.commit();
	}

	public boolean isPowerManagerAllowed() {
		return pref.getBoolean(IS_PM_ALLOWED, false);
	}

	public void clearPref() {
		editor.clear();
		editor.commit();
	}

	private String getUserPref() {
		return pref.getString(KEY_USER, null);
	}

	public String getAuthToken() {
		return pref.getString(KEY_AUTH_TOKEN, "");
	}

	public boolean isLogin() {
		return pref.getBoolean(KEY_IS_LOGIN, false);
	}

}
