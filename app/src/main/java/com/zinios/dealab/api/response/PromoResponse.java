package com.zinios.dealab.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zinios.dealab.model.PromoList;

/**
 * Created by vihanga on 28/10/2018.
 */

public class PromoResponse extends Ancestor<PromoList> {
	PromoResponse(@JsonProperty("message") String message,
	              @JsonProperty("data") PromoList data,
	              @JsonProperty("statusCode") int code) {
		super(message, code, data);
	}
}
