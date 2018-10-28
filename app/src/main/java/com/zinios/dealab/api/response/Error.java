package com.zinios.dealab.api.response;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by vihanga on 28/10/2018.
 */

public class Error extends Ancestor<String> {


	public Error(@JsonProperty("message") String message,
	             @JsonProperty("data") String data,
	             @JsonProperty("status") int status) {
		super(message, status, data);
	}

	public Error(VolleyError ignored) {
		super(String.valueOf(ignored.networkResponse.statusCode),
				-99,
				new String(ignored.networkResponse.data));
	}

	public Error(Throwable ignored) {
		super(ignored.getLocalizedMessage(),
				-999,
				ignored.toString());
	}

}
