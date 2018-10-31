package com.zinios.dealab.api.request.helper.impl;

import com.zinios.dealab.api.APIHelper;
import com.zinios.dealab.api.APIURLHelper;
import com.zinios.dealab.api.request.helper.PromoRequestHelper;
import com.zinios.dealab.api.response.factory.impl.AncestorPromoResponseFactory;

import org.json.JSONObject;

import static com.android.volley.Request.Method.GET;

public class PromoRequestHelperImpl implements PromoRequestHelper {
	@Override
	public void getBranchPromos(int branchID, APIHelper.PostManResponseListener listener) {
		APIHelper.getInstance().sendJSONRequestsWithParams(listener, new AncestorPromoResponseFactory(),
				GET, APIURLHelper.getAllLocationsURL(branchID), new JSONObject());
	}
}
