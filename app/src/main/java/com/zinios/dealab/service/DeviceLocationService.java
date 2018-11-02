package com.zinios.dealab.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.zinios.dealab.DealabApplication;
import com.zinios.dealab.R;
import com.zinios.dealab.api.APIHelper;
import com.zinios.dealab.api.request.helper.impl.LocationRequestHelperImpl;
import com.zinios.dealab.api.response.Ancestor;
import com.zinios.dealab.api.response.Error;
import com.zinios.dealab.api.response.LocationListResponse;
import com.zinios.dealab.model.MapLocation;
import com.zinios.dealab.ui.activity.DashboardActivity;
import com.zinios.dealab.util.Constants;
import com.zinios.dealab.util.UserSessionManager;

import java.util.List;

import static com.zinios.dealab.util.Constants.NOTIFICATION_CHANNEL_ID;


/**
 * Created by vihanga on 5/11/18
 */
public class DeviceLocationService extends Service implements LocationListener,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private static final String TAG = DeviceLocationService.class.getSimpleName();

	private static final int NOTIFICATION_ID = 516;
	private static final String ANDROID_CHANNEL_ID = "LocationObserveService";
	private GoogleApiClient mGoogleApiClient;

	public DeviceLocationService() {

	}

	@Override
	public void onCreate() {
		super.onCreate();
		createLocationRequest();
	}

	private void startLocationUpdates() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			return;
		}
		LocationServices.getFusedLocationProviderClient(this);
		LocationRequest currentLocationRequest = new LocationRequest();
		currentLocationRequest.setInterval(30000)
				.setFastestInterval(30000)
				.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			if (mGoogleApiClient.isConnected())
				LocationServices.getFusedLocationProviderClient(this)
						.requestLocationUpdates(currentLocationRequest, new LocationCallback() {
							@Override
							public void onLocationResult(LocationResult locationResult) {
								super.onLocationResult(locationResult);
								onLocationChanged(locationResult.getLastLocation());
							}
						}, Looper.myLooper());
		}
	}

	boolean isGPSEnabled() {
		LocationManager locationManager = (LocationManager)
				getSystemService(LOCATION_SERVICE);
		return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	protected void createLocationRequest() {
		LocationRequest mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(30000);
		mLocationRequest.setFastestInterval(30000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		mGoogleApiClient.connect();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
					.setContentTitle(getString(R.string.app_name))
					.setContentText("Dealab is running")
					.setTimeoutAfter(1000 * 5)//5 sec
					.setAutoCancel(true);
			Notification notification = builder.build();
			startForeground(NOTIFICATION_ID, notification);
		} else {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
					.setContentTitle(getString(R.string.app_name))
					.setContentText("Dealab is running")
					.setTimeoutAfter(1000 * 5)//5 sec
					.setPriority(NotificationCompat.PRIORITY_DEFAULT)
					.setAutoCancel(true);
			Notification notification = builder.build();
			startForeground(NOTIFICATION_ID, notification);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public void onLocationChanged(Location location) {
		if (location != null && DealabApplication.getInstance() != null) {
			UserSessionManager instance = UserSessionManager.getInstance();
			LatLng latLng = instance.getLastNotificationLocation();
			if (latLng == null) {
				instance.saveLastNotificationLocation(location.getLatitude(), location.getLongitude());
			} else {
				proceedLocation(latLng, new LatLng(location.getLatitude(), location.getLongitude()));
				instance.saveLastNotificationLocation(location.getLatitude(), location.getLongitude());
			}
		}
	}

	private void proceedLocation(LatLng oldLocation, LatLng newLocation) {
		if (distanceBetween(oldLocation.latitude, oldLocation.longitude,
				newLocation.latitude, newLocation.longitude) > 500) { //more than 500m
			if (DealabApplication.getInstance().isNetworkConnected()) {
				requestDeals(newLocation.latitude, newLocation.longitude);
			}
		}
	}

	private void requestDeals(double lat, double lng) {
		new LocationRequestHelperImpl().locationsBoundary(lat, lng, new APIHelper.PostManResponseListener() {
			@Override
			public void onResponse(Ancestor ancestor) {
				if (ancestor instanceof LocationListResponse) {
					List<MapLocation> data = ((LocationListResponse) ancestor).getData();
					if (data != null) {
						int dealCount = 0;
						for (MapLocation mapLocation : data) {
							dealCount += mapLocation.getDealCount();
						}
						String formatText = "Tap to view " + dealCount + " promotions around you";
						showNotification(DeviceLocationService.this, formatText);
					}
				}
			}

			@Override
			public void onError(Error error) {

			}
		});
	}

	/**
	 * Calculate the the approximate distance in meters between this location and the given location.
	 *
	 * @param lat1 latitude of the 1st position
	 * @param lng1 longitude of the 1st position
	 * @param lat2 latitude of the 2nd position
	 * @param lng2 longitude of the 2nd position
	 * @return Returns the approximate distance in meters between 2 locations.
	 */
	private float distanceBetween(double lat1, double lng1, double lat2, double lng2) {
		Location loc1 = new Location("");
		loc1.setLatitude(lat1);
		loc1.setLongitude(lng1);
		loc1.setLongitude(lng1);

		Location loc2 = new Location("");
		loc2.setLatitude(lat2);
		loc2.setLongitude(lng2);
		float distanceTo = loc1.distanceTo(loc2);
		Log.i(TAG, "Location diff = " + distanceTo);
		return distanceTo;
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Log.e(TAG, "onConnected ");
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			startLocationUpdates();
			Log.d(TAG, "Location update started...");
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.e(TAG, "onConnectionSuspended " + i);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.e(TAG, connectionResult.getErrorMessage());
	}

	/**
	 * Display notification in the application when deals are avaialble
	 *
	 * @param context    - application context
	 * @param formatText
	 */
	private void showNotification(Context context, String formatText) {
		Intent intent = new Intent(context, DashboardActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		Drawable drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round_);
		if (drawable == null) return;
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if (mNotificationManager == null) return;
			CharSequence name = "deal.lab";

			NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
			mChannel.setDescription("Dealab notification");
			mChannel.enableLights(true);
			mChannel.setLightColor(Color.WHITE);
			mChannel.enableVibration(true);
			mChannel.setVibrationPattern(Constants.VIBRATE_IN_APP);
			mNotificationManager.createNotificationChannel(mChannel);
			mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			if (mNotificationManager == null) return;
			Notification notification = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
					.setContentTitle(getString(R.string.notification_text))
					.setContentText(formatText)
					.setSmallIcon(R.drawable.notification)
					.setChannelId(NOTIFICATION_CHANNEL_ID)
					.setTimeoutAfter(1000 * 60 * 30)//30min
					.setColorized(true)
					.setContentIntent(pendingIntent)
					.setLargeIcon(bitmap)
					.setAutoCancel(true)
					.setColor(ContextCompat.getColor(context, R.color.colorPrimary))
					.build();
			mNotificationManager.notify(NOTIFICATION_ID, notification);
		} else {
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
					.setSmallIcon(R.drawable.notification)
					.setContentTitle(getString(R.string.notification_text))
					.setContentText(formatText)
					.setPriority(NotificationCompat.PRIORITY_DEFAULT)
					.setColor(ContextCompat.getColor(context, R.color.colorPrimary))//app name color
					.setColorized(true)
					.setLargeIcon(bitmap)
					.setLights(Color.WHITE, 500, 2000)
					.setVibrate(Constants.VIBRATE_IN_APP)
					//                .setSubText(getCurrentTime())
					.setTimeoutAfter(1000 * 60 * 30)//30min
					.setContentIntent(pendingIntent)
					.setAutoCancel(true);
			android.app.Notification notification = mBuilder.build();
			notification.flags |= android.app.Notification.FLAG_SHOW_LIGHTS;
			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
			notificationManager.notify(NOTIFICATION_ID, notification);
		}

	}
}