package com.zinios.dealab.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zinios.dealab.model.MapLocation;

import java.util.List;

/**
 * Created by vihanga on 28/10/2018.
 */

public class LocationListResponse extends Ancestor<List<MapLocation>> {
	LocationListResponse(@JsonProperty("message") String message,
	                     @JsonProperty("data") List<MapLocation> data,
	                     @JsonProperty("statusCode") int code) {
		super(message, code, data);
	}
}
