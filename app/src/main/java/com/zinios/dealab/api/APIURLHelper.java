package com.zinios.dealab.api;

public class APIURLHelper {

	private static final String BASE_URL = "http://192.168.8.101:9000/v1/deal";

	private static final String ALL_LOCATIONS = "/map-all";
	private static final String NEAR_LOCATIONS = "/map-boundary-all";
	private static final String LONGITUDE = "lng=";
	private static final String LATITUDE = "lat=";

	public static String getAllLocationsURL() {
		return BASE_URL.concat(ALL_LOCATIONS);
	}

	public static String getNearLocationsURL(double lat, double lng) {
		return BASE_URL.concat(NEAR_LOCATIONS).concat("?")
				.concat(LONGITUDE).concat(String.valueOf(lng))
				.concat("&")
				.concat(LATITUDE).concat(String.valueOf(lat));
	}
}
