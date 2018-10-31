package com.zinios.dealab.api.request.helper;


import com.zinios.dealab.api.APIHelper;

/**
 * Created by vihanga on 28/10/2018.
 */

public interface PromoRequestHelper {

	void getBranchPromos(int branchID, APIHelper.PostManResponseListener listener);

}
