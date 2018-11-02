package com.zinios.dealab.util.par;


import java.io.Serializable;

/**
 * Created by vihanga on 12/30/17.
 */

public class POI implements Serializable {

	private String id;
	private String placeTitle;
	private String openClose;
	private String openTime;
	private double latitude;
	private double longitude;
	private POIType poiType;
	private double distance;
	private double altitude;

	private int width;
	private int height;

	public POI() {
	}

	public POI(String id, String placeTitle, String openClose, String openTime, double latitude, double longitude, POIType poiType) {
		this.id = id;
		this.placeTitle = placeTitle;
		this.openClose = openClose;
		this.openTime = openTime;
		this.latitude = latitude;
		this.longitude = longitude;
		this.poiType = poiType;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public POIType getPoiType() {
		return poiType;
	}

	public void setPoiType(POIType poiType) {
		this.poiType = poiType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlaceTitle() {
		return placeTitle;
	}

	public void setPlaceTitle(String placeTitle) {
		this.placeTitle = placeTitle;
	}

	public String getOpenClose() {
		return openClose;
	}

	public void setOpenClose(String openClose) {
		this.openClose = openClose;
	}

	public String getOpenTime() {
		return openTime;
	}

	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
