package com.zinios.dealab.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by vihanga on 28/10/2018.
 */

public class StringResponse extends Ancestor<String> {
	StringResponse(@JsonProperty("message") String message,
	               @JsonProperty("data") String data,
	               @JsonProperty("statusCode") int code) {
		super(message, code, data);
	}
}
