package com.zinios.dealab.model;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

public class MarkerItem implements ClusterItem {
	private String title;
	private String snippet;
	private LatLng latLng;
	private BitmapDescriptor icon;
	private MapLocation mapLocation;

	public MarkerItem(MarkerOptions markerOptions, MapLocation mapLocation) {
		this.mapLocation = mapLocation;
		this.latLng = markerOptions.getPosition();
		this.title = markerOptions.getTitle();
		this.snippet = markerOptions.getSnippet();
		this.icon = markerOptions.getIcon();
	}

	@Override
	public LatLng getPosition() {
		return latLng;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public BitmapDescriptor getIcon() {
		return icon;
	}

	public void setIcon(BitmapDescriptor icon) {
		this.icon = icon;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}

	public MapLocation getMapLocation() {
		return mapLocation;
	}

	public void setMapLocation(MapLocation mapLocation) {
		this.mapLocation = mapLocation;
	}
}