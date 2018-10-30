package com.zinios.dealab.api.request.helper.impl;

import com.zinios.dealab.api.APIHelper;
import com.zinios.dealab.api.APIURLHelper;
import com.zinios.dealab.api.request.helper.LocationRequestHelper;
import com.zinios.dealab.api.response.factory.impl.AncestorLocationListtResponseFactory;

import org.json.JSONObject;

import static com.android.volley.Request.Method.GET;

public class LocationRequestHelperImpl implements LocationRequestHelper {
	@Override
	public void locationsAll(APIHelper.PostManResponseListener listener) {
		APIHelper.getInstance().sendJSONRequestsWithParams(listener, new AncestorLocationListtResponseFactory(),
				GET, APIURLHelper.getAllLocationsURL(), new JSONObject());
	}

	@Override
	public void locationsBoundary(double lat, double lng, APIHelper.PostManResponseListener listener) {
		APIHelper.getInstance().sendJSONRequestsWithParams(listener, new AncestorLocationListtResponseFactory(),
				GET, APIURLHelper.getNearLocationsURL(lat, lng), new JSONObject());
	}
}
