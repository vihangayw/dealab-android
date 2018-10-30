package com.zinios.dealab.api.request.helper;


import com.zinios.dealab.api.APIHelper;

/**
 * Created by vihanga on 28/10/2018.
 */

public interface LocationRequestHelper {

	void locationsAll(APIHelper.PostManResponseListener listener);

	void locationsBoundary(double lat, double lng, APIHelper.PostManResponseListener listener);

}
