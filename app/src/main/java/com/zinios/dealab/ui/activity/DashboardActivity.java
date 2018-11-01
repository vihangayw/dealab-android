package com.zinios.dealab.ui.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.zinios.dealab.DealabApplication;
import com.zinios.dealab.R;
import com.zinios.dealab.api.APIHelper;
import com.zinios.dealab.api.request.helper.impl.LocationRequestHelperImpl;
import com.zinios.dealab.api.response.Ancestor;
import com.zinios.dealab.api.response.Error;
import com.zinios.dealab.api.response.LocationListResponse;
import com.zinios.dealab.model.MapLocation;
import com.zinios.dealab.model.MarkerItem;
import com.zinios.dealab.receiver.SensorStartReceiver;
import com.zinios.dealab.service.DeviceLocationService;
import com.zinios.dealab.ui.adapter.PromoAdapter;
import com.zinios.dealab.util.UserSessionManager;
import com.zinios.dealab.util.UtilityManager;
import com.zinios.dealab.widget.LatLngInterpolator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.zinios.dealab.util.Constants.POWER_MANAGER_INTENTS;

public class DashboardActivity extends BaseActivity implements
		OnMapReadyCallback,
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationListener, PromoAdapter.OnComponentClickListener {

	private static final String TAG = DashboardActivity.class.getSimpleName();
	private final int MY_PERMISSIONS_REQUEST_LOCATION = 33;
	private final int LOCATION_ENABLE = 4910;
	private final int PLACE_PICKER_REQUEST = 101;

	BottomSheetBehavior bottomSheetBehavior;
	@BindView(R.id.bottom_sheet)
	View bottomSheet;
	@BindView(R.id.recycler_view)
	RecyclerView recyclerView;
	@BindView(R.id.drawer_layout)
	DrawerLayout drawer;
	@BindView(R.id.coordinator)
	CoordinatorLayout coordinatorLayout;
	@BindView(R.id.txt_pop_bubble)
	TextView txtBubble;
	@BindView(R.id.txt_size)
	TextView txtSize;
	@BindView(R.id.img_arrow)
	ImageView imgArrow;
	@BindView(R.id.fab_location)
	View fabLocatoin;

	private GoogleMap mMap;

	private ClusterManager<MarkerItem> mClusterManager;

	private List<MarkerItem> markerLocations = new ArrayList<>();
	private Map<String, BitmapDescriptor> cache = new HashMap<>();
	private boolean animating;
	private float cameraPos;
	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation;
	private Marker mPositionMarker;
	private LatLng mLatLng;
	private APIHelper.PostManResponseListener locationAPIListener;
	private Handler bubbleHandler = new Handler();
	private Runnable runnableBubble = new Runnable() {
		@Override
		public void run() {
			hidePopBubble();
		}
	};
	private Marker mSearchMarker;
	private PromoAdapter promoAdapter;
	private boolean nearReq;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		initializeViews();
		setListeners();
	}

	/**
	 * Smoothly marker to the current position when a location is received.
	 * In order to animate lat difference & lng difference should be higher than 0.00002 to remove
	 * unnecessary flickering on very tiny location movements.
	 * Animate marker with in 1sec (1000 mills)
	 *
	 * @param marker        marker to animate
	 * @param finalPosition end position
	 */
	public static void animateMarker(final Marker marker, final LatLng finalPosition) {
		if (marker != null) {
			final LatLng startPosition = marker.getPosition();
			final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
			ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
			valueAnimator.setDuration(1000); // duration 1 second
			valueAnimator.setInterpolator(new LinearInterpolator());
			valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					try {
						float v = animation.getAnimatedFraction();
						LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
						marker.setPosition(newPosition);
					} catch (Exception ex) {
						// I don't care atm..
					}
				}
			});
			valueAnimator.start();
		}
	}

	@Override
	void initializeViews() {
		super.initializeViews();
		promoAdapter = new PromoAdapter(this, null, this);
		recyclerView.setLayoutManager(new LinearLayoutManager(this,
				LinearLayoutManager.VERTICAL,
				false));
		recyclerView.setAdapter(promoAdapter);

		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
		bottomSheet.setVisibility(View.GONE);

		txtBubble.setVisibility(View.INVISIBLE);
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		if (mapFragment != null)
			mapFragment.getMapAsync(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			checkLocationPermission();
		} else buildGoogleApiClient();

	}

	@OnClick(R.id.btn_settings)
	void settings() {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	@OnClick(R.id.btn_notifications)
	void notificaitons() {
		startActivity(new Intent(this, NotificationActivity.class));
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!UserSessionManager.getInstance().isPowerManagerAllowed()) {
			for (Intent intent : POWER_MANAGER_INTENTS) {
				if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
					powerSettings(this, intent);
					return;
				}
			}
			UserSessionManager.getInstance().setPowerManagerAllowed();
		}
		if (!DealabApplication.getInstance().isServiceRunning(DeviceLocationService.class)) {
			Log.d(SensorStartReceiver.class.getSimpleName(), "DeviceLocationService starting...");
			Intent service = new Intent(this, DeviceLocationService.class);
			Bundle bundle = new Bundle();
			service.putExtras(bundle);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				startForegroundService(service);
			} else {
				startService(service);
			}
		} else {
			Log.d(SensorStartReceiver.class.getSimpleName(), "DeviceLocationService is already running...");
		}
		enableLocation();
	}

	/**
	 * Show material dialog to go to settings of the power settings of the phone.
	 * Enable the settings to allow the app to run on background.
	 * This setting should be enabled in the setting in oder to enable the gesture feature.
	 * <p>
	 * Once the settings button is clicked Preferences:IS_PM_ALLOWED will save to allowed which will
	 * not allow the app to show this alert again.
	 *
	 * @param context - current context
	 * @param intent  - settings intent to open
	 */
	public void powerSettings(final Context context, final Intent intent) {
		MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
				.customView(R.layout.layout_power_settings, false).cancelable(true);
		final MaterialDialog dialog = builder.build();
		View customView = dialog.getCustomView();
		if (customView != null) {
			RelativeLayout relativeLayout = customView.findViewById(R.id.layout_dialog);
			FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) relativeLayout.getLayoutParams();
			layoutParams.height = getHeight(0);
			relativeLayout.setLayoutParams(layoutParams);

			View btnOk = customView.findViewById(R.id.btn_ok);
			View btnCancel = customView.findViewById(R.id.btn_cancel);

			btnCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			btnOk.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(intent);
					UserSessionManager.getInstance().setPowerManagerAllowed();
					dialog.dismiss();
				}
			});
		}
		dialog.show();
	}

	/**
	 * Get a fixed height for the material dialog alert.
	 * Calculate height using density and resolution of the screen
	 *
	 * @param reduce - reduce this amount from the calculated height
	 * @return - final height
	 */
	private int getHeight(double reduce) {
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		final int calcHeight;
		float density = getResources().getDisplayMetrics().density;
		if (density > 2.0) { //xxhdpi
			calcHeight = (int) ((size.y) * (0.49 - reduce));
		} else if (density > 1.5) { //xhdpi
			calcHeight = (int) (size.y * (0.57 - reduce));
		} else if (density > 1.0) { //hdpi
			calcHeight = (int) (size.y * (0.59 - reduce));
		} else if (density > 0.75) { //mdpi
			calcHeight = (int) (size.y * (0.55 - reduce));
		} else { //ldpi
			calcHeight = (int) (size.y * (0.54 - reduce));
		}
		return calcHeight;
	}

	@Override
	protected void onStop() {
		super.onStop();
		bubbleHandler.removeCallbacks(runnableBubble);
		hidePopBubble();
	}

	@OnClick(R.id.fab_location)
	void myLocationClick() {
		if (mSearchMarker != null) mSearchMarker.remove();
		if (mLatLng != null) {
			animateCamera(mLatLng, 550);
		}
	}

	@OnClick(R.id.btn_toggle)
	void toggle() {
		drawer.openDrawer(Gravity.START);
	}

	@OnClick(R.id.txt_pop_bubble)
	void popupClick() {
		if (txtBubble.getText().toString().toLowerCase().contains("updat")) return;
		if (txtBubble.getText().toString().toLowerCase().contains("no data")) return;
		if (promoAdapter == null || promoAdapter.getItemCount() == 0) return;

		hidePopBubble();
		bottomSheet.setVisibility(View.VISIBLE);
		fabLocatoin.setVisibility(View.INVISIBLE);
		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
	}

	@OnClick(R.id.search_layout)
	void searchClicked() {
		if (mMap == null) return;
		PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
		try {
			startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
		} catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
			e.printStackTrace();
		}
	}

	private boolean checkLocationPermission() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

			// Asking user if explanation is needed
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.ACCESS_FINE_LOCATION)) {
				showRequestPermissionAlert();
			} else {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						MY_PERMISSIONS_REQUEST_LOCATION);
			}
			return false;
		} else {
			buildGoogleApiClient();
			return true;
		}
	}

	private void enableLocation() {
		final LocationManager manager = (LocationManager) DashboardActivity.this.getSystemService(Context.LOCATION_SERVICE);

		if (!UtilityManager.hasGPSDevice(DashboardActivity.this)) {
			Toast.makeText(this, R.string.gps_error, Toast.LENGTH_SHORT).show();
			return;
		}

		if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && UtilityManager.hasGPSDevice(DashboardActivity.this)) {
			enableLocationServices();
		}
	}

	private void enableLocationServices() {
		if (mGoogleApiClient == null)
			buildGoogleApiClient();

		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(30 * 1000);
		locationRequest.setFastestInterval(5 * 1000);
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(locationRequest);

		builder.setAlwaysShow(true);
		Task<LocationSettingsResponse> result =
				LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
		result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
			@Override
			public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
				try {
					task.getResult(ApiException.class);
				} catch (ApiException exception) {
					switch (exception.getStatusCode()) {
						case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
							try {
								ResolvableApiException resolvable = (ResolvableApiException) exception;
								resolvable.startResolutionForResult(DashboardActivity.this, LOCATION_ENABLE);
							} catch (IntentSender.SendIntentException | ClassCastException e) {
								Log.d(TAG, e.getMessage());
							}
							break;
					}
				}
			}
		});
	}

	/**
	 * Builds a GoogleApiClient.
	 * Uses the addApi() method to request the Google Places API and the Fused Location Provider.
	 */
	private synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, this)
				.addConnectionCallbacks(this)
				.addApi(LocationServices.API)
				.addApi(Places.GEO_DATA_API)
				.addApi(Places.PLACE_DETECTION_API)
				.build();
		mGoogleApiClient.connect();
		enableLocation();
	}

	/**
	 * Handles the result of the request for location permissions.
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String permissions[],
	                                       @NonNull int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_LOCATION:
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					if (ContextCompat.checkSelfPermission(this,
							Manifest.permission.ACCESS_FINE_LOCATION)
							== PackageManager.PERMISSION_GRANTED) {

						if (mGoogleApiClient == null) {
							buildGoogleApiClient();
						}
//						if (mMap != null)
//							mMap.setMyLocationEnabled(true);
					}
				} else {
					showRequestPermissionAlert();
				}
				break;
//			case PERMISSIONS_REQUEST_ACCESS_CAMERA:
//				// If request is cancelled, the result arrays are empty.
//				if (grantResults.length > 0
//						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//					showArActivity();
//				}
//				break;

			default:
				break;
		}
	}

	private void showRequestPermissionAlert() {
		UtilityManager.showSnackBarWithAction(getString(R.string.on_permission_deny),
				Snackbar.LENGTH_INDEFINITE, "SETTINGS",
				coordinatorLayout, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent = new Intent();
						intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						Uri uri = Uri.fromParts("package", getPackageName(), null);
						intent.setData(uri);
						startActivity(intent);
					}
				});
	}

	@Override
	void setListeners() {
		super.setListeners();
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
					imgArrow.setImageDrawable(
							ContextCompat.getDrawable(DashboardActivity.this, R.drawable.ic_up));
				} else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
					imgArrow.setImageDrawable(
							ContextCompat.getDrawable(DashboardActivity.this, R.drawable.ic_down));
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {

			}
		});
		locationAPIListener = new APIHelper.PostManResponseListener() {
			@Override
			public void onResponse(Ancestor ancestor) {
				if (ancestor instanceof LocationListResponse) {
					List<MapLocation> data = ((LocationListResponse) ancestor).getData();
					if (data != null) {
						addMarkers(data);
						if (nearReq) {
							promoAdapter.setList(data);
						}
					}
				}
			}

			@Override
			public void onError(Error error) {
				DealabApplication.getInstance().showError(error);
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
		switch (requestCode) {
			case LOCATION_ENABLE:
				switch (resultCode) {
					case Activity.RESULT_OK:
						break;
					case Activity.RESULT_CANCELED:
						break;
					default:
						break;
				}
				break;
			case PLACE_PICKER_REQUEST:
				if (resultCode == RESULT_OK) {
					Place place = PlacePicker.getPlace(this, data);
					animateCamera(place.getLatLng(), 1000);

					if (distanceBetween(place.getLatLng().latitude, place.getLatLng().longitude) > 400) {
						if (mSearchMarker != null) {
							mSearchMarker.remove();
							mSearchMarker = null;
						}
						Drawable circleDrawable = UtilityManager.resizeImage(this, R.drawable.pin,
								(int) getResources().getDimension(R.dimen._20sdp),
								(int) getResources().getDimension(R.dimen._29sdp));
						BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);
						mSearchMarker = mMap.addMarker(new MarkerOptions()
								.position(place.getLatLng())
								.anchor(0.5f, 0.5f)
								.icon(markerIcon));
					} else if (mSearchMarker != null) {
						mSearchMarker.remove();
						mSearchMarker = null;
					}
				}

				break;
		}
	}

	private void addMarkers(List<MapLocation> data) {
		if (mMap == null) return;
		// Clears the previously touched position
		// mMap.clear();
		markerLocations.clear();

		int dealCount = 0;
		for (MapLocation location : data) {
			MarkerItem markerItem = new MarkerItem(new MarkerOptions()
					.position(new LatLng(location.getLat(), location.getLng())), location);
			markerLocations.add(markerItem);
			dealCount += location.getDealCount();
		}
		if (dealCount == 0) {
			txtBubble.setText(R.string.no_data);
			txtSize.setText("0");
		} else {
			txtBubble.setText(String.valueOf(dealCount).concat(" ").concat(getString(R.string.promos)));
			txtSize.setText(String.valueOf(dealCount));
		}

		animatePopBubble();
		if (mClusterManager != null) {
			if (!markerLocations.isEmpty())
				mClusterManager.addItems(markerLocations);
			mClusterManager.cluster();
		}
	}

	private void setUpCluster() {
		mClusterManager = new ClusterManager<>(this, mMap);
		mClusterManager.setRenderer(new ClusterRenderer(this, mMap, mClusterManager));
		mMap.setOnCameraIdleListener(mClusterManager);
		mMap.setOnMarkerClickListener(mClusterManager);
		mMap.setOnInfoWindowClickListener(mClusterManager);
		mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MarkerItem>() {
			@Override
			public boolean onClusterClick(final Cluster<MarkerItem> cluster) {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						animating = true;
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
								cluster.getPosition(), (float) Math.floor(mMap
										.getCameraPosition().zoom + 3)), 1200,
								new GoogleMap.CancelableCallback() {
									@Override
									public void onFinish() {
										animating = false;
									}

									@Override
									public void onCancel() {
										animating = false;
									}
								});
					}
				});

				return true;
			}
		});

		mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerItem>() {
			@Override
			public boolean onClusterItemClick(final MarkerItem markerItem) {
				Intent intent = new Intent(DashboardActivity.this, PromoActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("bid", (int) markerItem.getMapLocation().getBranchId());
				bundle.putString("branch", markerItem.getMapLocation().getBranch());
				intent.putExtras(bundle);
				startActivity(intent);
				return false;
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		} else {
			super.onBackPressed();
		}
	}

	@OnClick(R.id.layout_header)
	void changeDraggerState() {
		if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_DRAGGING) return;
		if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		} else {
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
		}
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
		//mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		mMap.getUiSettings().setCompassEnabled(false);
		mMap.getUiSettings().setRotateGesturesEnabled(true);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			mMap.setMyLocationEnabled(false);
		}
		setUpCluster();
		mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				mClusterManager.onMarkerClick(marker);
				return true;
			}
		});
	}

	private BitmapDescriptor getMarkerIconFromDrawable(String key, Bitmap bitmap) {
		try {
			if (cache == null) cache = new HashMap<>();

			if (cache.containsKey(key)) {
				Log.i(TAG, "load from cache " + key);
				return cache.get(key);
			} else {
				Log.i(TAG, "added to cache " + key);
				BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
				cache.put(key, bitmapDescriptor);
				return bitmapDescriptor;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public void onLocationChanged(Location location) {
		if (mMap == null) return;
		if (mLastLocation == null) {
			animateCamera(new LatLng(location.getLatitude(), location.getLongitude()), 1200);
		}
		mLastLocation = location;
		mLatLng = null;
		mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		if (mPositionMarker == null) {
			addCarMarker(); // add marker for the first time (call only 1st time)
		} else {
			animateMarker(mPositionMarker, mLatLng);// move marker to my location
		}
	}

	private void addCarMarker() {
		Drawable circleDrawable = getResources().getDrawable(R.drawable.my_circle);
		BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);
		mPositionMarker = mMap.addMarker(new MarkerOptions()
				.position(mLatLng)
				.anchor(0.5f, 0.5f)
				.icon(markerIcon));

	}

	private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
		Canvas canvas = new Canvas();
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return BitmapDescriptorFactory.fromBitmap(bitmap);
	}

	private void animateCamera(LatLng latLng, int delay) {
		if (mMap == null) return;
		CameraPosition camPos = CameraPosition
				.builder(mMap.getCameraPosition())// current Camera
				.bearing(0)
				.zoom(17.0f)
				.target(latLng)
				.build();
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos), delay, null);
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {

	}

	@Override
	public void onProviderEnabled(String s) {

	}

	@Override
	public void onProviderDisabled(String s) {

	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		LocationServices.getFusedLocationProviderClient(this);
		LocationRequest currentLocationRequest = new LocationRequest();
		currentLocationRequest.setInterval(5000)
				.setFastestInterval(5000)
				.setMaxWaitTime(5000)
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			if (mGoogleApiClient.isConnected())
				LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(currentLocationRequest, new LocationCallback() {
					@Override
					public void onLocationResult(LocationResult locationResult) {
						super.onLocationResult(locationResult);
						onLocationChanged(locationResult.getLastLocation());
					}
				}, Looper.myLooper());
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		if (mGoogleApiClient != null)
			mGoogleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	private void animatePopBubble() {
		bottomSheet.setVisibility(View.INVISIBLE);
		fabLocatoin.setVisibility(View.VISIBLE);
		bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

		bubbleHandler.removeCallbacks(runnableBubble);
		bubbleHandler.postDelayed(runnableBubble, 6000);
		if (txtBubble.getVisibility() == View.VISIBLE) return;
		ViewAnimator.animate(txtBubble)
				.translationX(-txtBubble.getWidth(), 0)
				.duration(500)
				.start();
		txtBubble.setVisibility(View.VISIBLE);
	}

	private void hidePopBubble() {
		if (txtBubble.getVisibility() == View.INVISIBLE || animating) return;
		animating = true;
		ViewAnimator.animate(txtBubble)
				.translationX(0, -txtBubble.getWidth())
				.duration(500)
				.onStop(new AnimationListener.Stop() {
					@Override
					public void onStop() {
						animating = false;
						txtBubble.setVisibility(View.INVISIBLE);
					}
				})
				.start();
	}

	private double distanceBetween(double lat, double lng) {
		if (mLastLocation == null)
			return 0;

		Location loc2 = new Location("");
		loc2.setLatitude(lat);
		loc2.setLongitude(lng);

		return mLastLocation.distanceTo(loc2);
	}

	@Override
	public void onComponentClick(View itemView, int position) {
		if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_DRAGGING)
			return;
		if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
			bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		}
		if (promoAdapter != null && promoAdapter.getItemCount() > position) {
			MapLocation mapLocation = promoAdapter.getList().get(position);
			Intent intent = new Intent(DashboardActivity.this, PromoActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt("bid", (int) mapLocation.getBranchId());
			bundle.putString("branch", mapLocation.getBranch());
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	@SuppressLint("StaticFieldLeak")
	private class GetAllLocation extends AsyncTask<String, Integer, Long> {
		protected Long doInBackground(String... urls) {
			nearReq = false;
			new LocationRequestHelperImpl().locationsAll(locationAPIListener);
			return 1L;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mClusterManager != null)
				mClusterManager.clearItems();
			txtBubble.setText(R.string.updating);
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Long result) {
		}
	}

	@SuppressLint("StaticFieldLeak")
	private class GetNearbyLocation extends AsyncTask<String, Integer, Long> {
		LatLng target;

		GetNearbyLocation(LatLng target) {
			this.target = target;
			nearReq = true;
		}

		protected Long doInBackground(String... urls) {
			if (target != null) {
				new LocationRequestHelperImpl().locationsBoundary(target.latitude, target.longitude,
						locationAPIListener);
			}
			return 1L;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mClusterManager != null)
				mClusterManager.clearItems();
			txtBubble.setText(R.string.updating);
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Long result) {
			animatePopBubble();
		}
	}

	public class ClusterRenderer extends DefaultClusterRenderer<MarkerItem> implements
			GoogleMap.OnCameraIdleListener {
		private TextView title;
		private View multiProfile;
		private Context mContext;

		private ClusterRenderer(Context context, GoogleMap map,
		                        ClusterManager<MarkerItem> clusterManager) {
			super(context, map, clusterManager);
			this.mContext = context;
		}


		@Override
		protected void onBeforeClusterItemRendered(final MarkerItem item,
		                                           final MarkerOptions markerOptions) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					multiProfile = LayoutInflater.from(mContext).inflate(R.layout.marker_layout, null);
					title = multiProfile.findViewById(R.id.title);
					title.setText(item.getMapLocation().getCompany()
							.concat("\n")
							.concat(String.valueOf(item.getMapLocation().getDealCount())
									.concat(" Available")));
					title.setVisibility(
							mMap.getCameraPosition().zoom >= 17.3 ?
									View.VISIBLE : View.GONE);
					String key = "key" + (mMap.getCameraPosition().zoom >= 17.3) + item.getMapLocation().getBranchId();

					multiProfile.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
							View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
					multiProfile.layout(0, 0, multiProfile.getMeasuredWidth(), multiProfile.getMeasuredHeight());

					BitmapDescriptor markerIconFromDrawable2 = getMarkerIconFromDrawable(key, loadBitmapFromView(multiProfile));
					markerOptions
							.position(item.getPosition())
							.icon(markerIconFromDrawable2)
							.draggable(false);

				}
			});
		}

		private Bitmap loadBitmapFromView(View view) {
			//Get the dimensions of the view so we can re-layout the view at its current size
			// and create a bitmap of the same size
			int width = view.getWidth();
			int height = view.getHeight();

			int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
			int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

			//Cause the view to re-layout
			view.measure(measuredWidth, measuredHeight);
			view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

			//Create a bitmap backed Canvas to draw the view into
			Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);

			//Now that the view is laid out and we have a canvas, ask the view to draw itself into the canvas
			view.draw(c);

			return b;
		}


		@Override
		protected void onClusterItemRendered(MarkerItem clusterItem, Marker marker) {
			super.onClusterItemRendered(clusterItem, marker);
		}

		@Override
		protected void onBeforeClusterRendered(Cluster<MarkerItem> cluster, MarkerOptions markerOptions) {
			super.onBeforeClusterRendered(cluster, markerOptions);
		}

		@Override
		public void onCameraIdle() {
			if (mLastLocation == null) return;
			if (mMap.getCameraPosition().zoom >= 17.3) {
				new GetNearbyLocation(mMap.getCameraPosition().target).execute();
			} else if (mMap.getCameraPosition().zoom >= 13 && mMap.getCameraPosition().zoom <= 17.2) {
				new GetNearbyLocation(mMap.getCameraPosition().target).execute();
			} else if (cameraPos == mMap.getCameraPosition().zoom && mMap.getCameraPosition().zoom >= 13
					&& mMap.getCameraPosition().zoom <= 17.2) {
				Location mapLocation = new Location("");
				mapLocation.setLongitude(mMap.getCameraPosition().target.longitude);
				mapLocation.setLatitude(mMap.getCameraPosition().target.latitude);
				new GetNearbyLocation(mMap.getCameraPosition().target).execute();
			} else if (mMap.getCameraPosition().zoom <= 12) {
				new GetAllLocation().execute();
			}
			cameraPos = mMap.getCameraPosition().zoom;

			if (mMap.getCameraPosition().zoom < 14) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
//						hideInfoWindow = true;
//						if (mCurrentMarkerInfo != null) {
//							mCurrentMarkerInfo.remove();
//						}
					}
				});
			}
		}
	}
}
