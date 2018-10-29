package com.zinios.dealab.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by vihanga on 28/10/2018.
 */

public class BooleanResponse extends Ancestor<Boolean> {
	BooleanResponse(@JsonProperty("message") String message,
	                @JsonProperty("data") Boolean data,
	                @JsonProperty("statusCode") int code) {
		super(message, code, data);
	}
}
