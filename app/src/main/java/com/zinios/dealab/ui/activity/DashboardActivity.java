package com.zinios.dealab.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.zinios.dealab.R;
import com.zinios.dealab.api.APIHelper;
import com.zinios.dealab.api.request.helper.impl.LocationRequestHelperImpl;
import com.zinios.dealab.api.response.Ancestor;
import com.zinios.dealab.api.response.Error;
import com.zinios.dealab.api.response.LocationListResponse;
import com.zinios.dealab.model.MapLocation;
import com.zinios.dealab.model.MarkerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DashboardActivity extends BaseActivity implements OnMapReadyCallback {

	private static final String TAG = DashboardActivity.class.getSimpleName();

	@BindView(R.id.drawer_layout)
	DrawerLayout drawer;
	@BindView(R.id.fab_location)
	FloatingActionButton btnLocation;

	private GoogleMap mMap;

	private ClusterManager<MarkerItem> mClusterManager;

	private List<MarkerItem> markerLocations = new ArrayList<>();
	private Map<String, BitmapDescriptor> cache = new HashMap<>();
	private boolean animating;
	private float cameraPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		initializeViews();
		setListeners();
		fetchAllLocations();

	}

	@Override
	void initializeViews() {
		super.initializeViews();
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		if (mapFragment != null)
			mapFragment.getMapAsync(this);
	}

	@Override
	void setListeners() {
		super.setListeners();
	}

	private void fetchAllLocations() {
		new LocationRequestHelperImpl().locationsAll(new APIHelper.PostManResponseListener() {
			@Override
			public void onResponse(Ancestor ancestor) {
				if (ancestor instanceof LocationListResponse) {
					List<MapLocation> data = ((LocationListResponse) ancestor).getData();
					if (data != null) {
						addMarkers(data);
					}
				}
			}

			@Override
			public void onError(Error error) {

				Toast.makeText(DashboardActivity.this, "error" + error.getMessage() + " " + error.getData(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void addMarkers(List<MapLocation> data) {
		if (mMap == null) return;
		// Clears the previously touched position
		mMap.clear();
		markerLocations.clear();
		if (mClusterManager != null)
			mClusterManager.clearItems();

		for (MapLocation location : data) {

			MarkerItem markerItem = new MarkerItem(new MarkerOptions()
					.position(new LatLng(location.getLat(), location.getLng())), location);
			markerLocations.add(markerItem);
		}

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
				Toast.makeText(DashboardActivity.this, "clcik", Toast.LENGTH_SHORT).show();
				return false;
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
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
		mMap.getUiSettings().setCompassEnabled(false);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			mMap.setMyLocationEnabled(false);
		}
		mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
					String key = "key" + (mMap.getCameraPosition().zoom >= 17.3);

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
			fetchAllLocations();
			if (mMap.getCameraPosition().zoom >= 17.3) {
				//near by
			} else if (mMap.getCameraPosition().zoom >= 13 && mMap.getCameraPosition().zoom <= 17.2) {
				//near by
			} else if (cameraPos == mMap.getCameraPosition().zoom && mMap.getCameraPosition().zoom >= 13
					&& mMap.getCameraPosition().zoom <= 17.2) {
				Location mapLocation = new Location("");
				mapLocation.setLongitude(mMap.getCameraPosition().target.longitude);
				mapLocation.setLatitude(mMap.getCameraPosition().target.latitude);
				//near by
			} else if (mMap.getCameraPosition().zoom <= 12) {
				fetchAllLocations();
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
