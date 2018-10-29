package com.zinios.dealab.api;

public class APIURLHelper {

	private static final String BASE_URL = "http://192.168.8.101:9000/v1/deal";

	private static final String ALL_LOCATIONS = "/map-all";

	public static String getAllLocationsURL() {
		return BASE_URL.concat(ALL_LOCATIONS);
	}
}
