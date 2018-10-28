package com.zinios.dealab.api.request.helper;


import com.zinios.dealab.api.APIHelper;

/**
 * Created by vihanga on 28/10/2018.
 */

public interface UserRequestHelper {

	void loginUser(String domain, String email, String pw, APIHelper.PostManResponseListener listener);

}
