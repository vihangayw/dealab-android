package com.zinios.dealab.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zinios.dealab.DealabApplication;
import com.zinios.dealab.R;
import com.zinios.dealab.api.APIHelper;
import com.zinios.dealab.api.request.helper.impl.LocationRequestHelperImpl;
import com.zinios.dealab.api.response.Ancestor;
import com.zinios.dealab.api.response.Error;
import com.zinios.dealab.api.response.LocationListResponse;
import com.zinios.dealab.model.MapLocation;
import com.zinios.dealab.ui.activity.PromoActivity;
import com.zinios.dealab.util.par.PARController;
import com.zinios.dealab.util.par.PARPoiLabel;
import com.zinios.dealab.util.par.PARPoiLabelAdvanced;
import com.zinios.dealab.util.par.POI;
import com.zinios.dealab.util.par.POIType;
import com.zinios.dealab.util.par.PSKDeviceOrientation;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment providing AR functionality
 * configure and add content here
 */
public class ARFragment extends PARFragment implements PARFragment.LocationChange, View.OnClickListener {
	private List<POI> spotList;
	private TextView txtView;
	private View ovalay, progress;
	private double maxDis;
	private double mLat;
	private double mLng;
	private Location location;
	private View backButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// add content using helper methods defined below
		// example to add costume drawable
		getDeviceLocation();

	}

	public void setmLat(double mLat) {
		this.mLat = mLat;
	}

	public void setmLng(double mLng) {
		this.mLng = mLng;
	}

	public void setSpotList(List<POI> spotList) {
		this.spotList = spotList;
		if (spotList != null && !spotList.isEmpty()) {
			for (POI poi : spotList) {
				PARPoiLabel label = createPoi(poi, poi.getAltitude());
				//  label.setSize(poi.getWidth(), poi.getHeight());
				PARController.getInstance().addPoi(label);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// FIRST: setup default resource IDs
		// IMPORTANT: call before super.onCreate()
		this.viewLayoutId = R.layout.panicar_view;
		View view = super.onCreateView(inflater, container, savedInstanceState, spotList, this);
		progress = view.findViewById(R.id.progress);
		txtView = view.findViewById(R.id.txt_center);
		ovalay = view.findViewById(R.id.ovalay);
		backButton = view.findViewById(R.id.back_icon);
		backButton.setOnClickListener(this);
		getRadarView().setRadarRange(30);
		return view;
	}

	/**
	 * Request Nearby Locations
	 */
	private void requestNearByLocations() {
		new LocationRequestHelperImpl().locationsBoundary(mLat, mLng,
				new APIHelper.PostManResponseListener() {
					@Override
					public void onResponse(Ancestor ancestor) {
						if (ancestor instanceof LocationListResponse) {
							List<MapLocation> data = ((LocationListResponse) ancestor).getData();
							if (data != null) {
								displayNearByLocations(data);
							}
						}
					}

					@Override
					public void onError(Error error) {
						DealabApplication.getInstance().showError(error);
					}
				});
	}

	private void getDeviceLocation() {
		if (getActivity() == null) return;
		if (ContextCompat.checkSelfPermission(getActivity(),
				Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
			requestNearByLocations();
		} else {
			ActivityCompat.requestPermissions(getActivity(),
					new String[]{Manifest.permission.CAMERA},
					39);
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case 39:
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					requestNearByLocations();
				} else {
					progress.setVisibility(View.GONE);
					txtView.setText("Permission Denied");
				}
				break;

			default:
				break;
		}
	}

	private void displayNearByLocations(List<MapLocation> data) {
		List<POI> spotList = new ArrayList<>();
		try {
			for (int i = 0; i < data.size(); i++) {
				MapLocation mapLocation = data.get(i);

				POI poi = new POI();
				poi.setId(String.valueOf(mapLocation.getBranchId()));
				poi.setLatitude(mapLocation.getLat());
				poi.setLongitude(mapLocation.getLng());
				poi.setOpenClose("Open Now");
				poi.setOpenTime("08.30 AM");
				poi.setDeal("Promotions - " + mapLocation.getDealCount());
				poi.setPlaceTitle(mapLocation.getCompany());
				POIType poiType = POIType.green;
				if (i % 3 == 0) {
					poiType = POIType.blue;
				} else if (i % 3 == 1) {
					poiType = POIType.red;
				} else if (i % 3 == 2) {
					poiType = POIType.orange;
				}
				poi.setPoiType(poiType);
				poi.setDistance(distanceBetween(mLat, mLng, mapLocation.getLat(), mapLocation.getLng()));
				if (poi.getDistance() > maxDis) maxDis = poi.getDistance();
				spotList.add(poi);
				if (i > 8) break;
			}

//			spotList.add(new POI("1", "Keels Super", "Closed Now", "8AM - 10.30PM", 6.0785734, 80.2428903, POIType.red));
//			spotList.add(new POI("2", "AIA Sri Lanka", "Closed Now", "8AM - 10.30PM", 6.0792314, 80.2438568, POIType.red));
			//            spotList.add(new POI("3", "Amari Galle Sri Lanka", "Open", "24 Hours", 6.0789691, 80.2436997, POIType.green));
			//            spotList.add(new POI("4", "HSBC", "Closed today", "", 6.0786076, 80.2442514, POIType.orange));
			//            spotList.add(new POI("5", "Lion Lodge", "Closed today", "", 6.0789043, 80.2438196, POIType.orange));

			if (!spotList.isEmpty()) {
				calcAltitude(spotList);
				setSpotList(spotList);
				location = new Location("");
				location.setLatitude(mLat);
				location.setLongitude(mLng);
				progress.setVisibility(View.GONE);
				txtView.setVisibility(View.GONE);
				ovalay.setVisibility(View.GONE);
			} else {
				txtView.setText(R.string.no_spots_available);
				progress.setVisibility(View.GONE);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	private void calcAltitude(List<POI> spotList) {
		final double ALTITUDE = maxDis / 10;
		//        int minWidth = 210;
		//        int minHeight = 100;
		//        int maxWidth = 300;
		//        int maxHeight = 170;
		double increment = ALTITUDE / spotList.size();
		for (int i = 0; i < spotList.size(); i++) {
			POI poi = spotList.get(i);

			//            if (maxHeight < minHeight) maxHeight = minHeight;
			//            if (maxWidth < minWidth) maxWidth = minWidth;
			//            poi.setHeight(maxHeight);
			//            poi.setWidth(maxWidth);
			//            maxHeight -= 8;
			//            maxWidth -= 10;

			if (i == 0) {
				poi.setAltitude(0.0);
				continue;
			}
			poi.setAltitude(increment);
			increment += ALTITUDE / spotList.size();
		}
	}
	//    @Override
	//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	//        inflater.inflate(R.menu.fragment_menu, menu);
	//        super.onCreateOptionsMenu(menu, inflater);
	//    }

	//    @Override
	//    public boolean onOptionsItemSelected(MenuItem item) {
	//        switch (item.getItemId()){
	//            case R.id.action_add_random_poi:
	//                int random = (new Random().nextInt(Math.abs(labelRepo.size()-1)));
	//                PARController.getInstance().addPoi(labelRepo.get(random));
	//                Toast.makeText(this.getActivity(),"Added: " + labelRepo.get(random).getTitle(), Toast.LENGTH_SHORT).show();
	//                return super.onOptionsItemSelected(item);
	//            case R.id.action_add_cardinal_pois:
	//
	//                return super.onOptionsItemSelected(item);
	//            case R.id.action_delete_last_poi:
	//                if (PARController.getInstance().numberOfObjects() > 0){
	//                    int lastObject = PARController.getInstance().numberOfObjects()-1;
	//                    Toast.makeText(this.getActivity(),"Removing: " + ((PARPoiLabel)PARController.getInstance().getObject(lastObject)).getTitle(), Toast.LENGTH_SHORT).show();
	//                    PARController.getInstance().removeObject(lastObject);
	//                }
	//                return super.onOptionsItemSelected(item);
	//            case R.id.action_delete_all_pois:
	//                PARController.getInstance().clearObjects();
	//                return super.onOptionsItemSelected(item);
	//        }
	//        return super.onOptionsItemSelected(item);
	//    }

	@Override
	public void onDeviceOrientationChanged(PSKDeviceOrientation newOrientation) {
		super.onDeviceOrientationChanged(newOrientation);
	}


	/**
	 * Create a poi with title, description and position
	 *
	 * @param poi point of interest
	 * @return PARPoiLabel which is a subclass of PARPoi (extended for title, description and so on)
	 */
	public PARPoiLabel createPoi(final POI poi) {
		Location poiLocation = new Location(poi.getPlaceTitle());
		poiLocation.setLatitude(poi.getLatitude());
		poiLocation.setLongitude(poi.getLongitude());

		final PARPoiLabel parPoiLabel = new PARPoiLabel(poiLocation, poi);

		parPoiLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openSpotDetails(Integer.parseInt(poi.getId()), poi.getPlaceTitle());
			}
		});

		return parPoiLabel;
	}

	/**
	 * Create a poi with title, description and position
	 *
	 * @param poi point of interest
	 * @return PARPoiLabelAdvanced which is a subclass of PARPoiLabel (extended for altitude support)
	 */
	public PARPoiLabelAdvanced createPoi(final POI poi, double alt) {
		Location poiLocation = new Location(poi.getPlaceTitle());
		poiLocation.setLatitude(poi.getLatitude());
		poiLocation.setLongitude(poi.getLongitude());
		//        poiLocation.setAltitude(alt);

		final PARPoiLabelAdvanced parPoiLabel = new PARPoiLabelAdvanced(poiLocation, poi);
		parPoiLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openSpotDetails(Integer.parseInt(poi.getId()), poi.getPlaceTitle());
			}
		});

		return parPoiLabel;
	}

	public void openSpotDetails(int id, String branch) {
		if (getActivity() == null) return;
		Intent intentDetail = new Intent(getActivity(), PromoActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("bid", id);
		bundle.putString("branch", branch);
		intentDetail.putExtras(bundle);
		startActivity(intentDetail);
	}

	@Override
	public void onLocationChange(Location location) {
		if (this.location != null && distanceBetween(this.location.getLatitude(), this.location.getLongitude(),
				location.getLatitude(), location.getLongitude()) > 300) { //refresh every 300 m
			PARController.getInstance().clearObjects();
			progress.setVisibility(View.VISIBLE);
			txtView.setVisibility(View.VISIBLE);
			ovalay.setVisibility(View.VISIBLE);
			getDeviceLocation();
		}
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

		Location loc2 = new Location("");
		loc2.setLatitude(lat2);
		loc2.setLongitude(lng2);

		return loc1.distanceTo(loc2);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		PARController.getInstance().clearObjects();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.back_icon:
				PARController.getInstance().clearObjects();
				if (getActivity() != null) getActivity().finish();
				break;
		}
	}
}
