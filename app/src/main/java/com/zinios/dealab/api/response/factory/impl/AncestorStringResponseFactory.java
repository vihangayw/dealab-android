package com.zinios.dealab.api.response.factory.impl;


import com.zinios.dealab.api.response.Ancestor;
import com.zinios.dealab.api.response.StringResponse;
import com.zinios.dealab.api.response.factory.AncestorsFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by vihanga on 28/10/2018.
 */

public class AncestorStringResponseFactory implements AncestorsFactory {
	@Override
	public Ancestor parse(JSONObject response) throws IOException {
		return new ObjectMapper()
				.readValue(response.toString(), StringResponse.class);
	}

}
