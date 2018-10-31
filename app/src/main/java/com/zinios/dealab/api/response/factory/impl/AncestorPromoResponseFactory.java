package com.zinios.dealab.api.response.factory.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinios.dealab.api.response.Ancestor;
import com.zinios.dealab.api.response.PromoResponse;
import com.zinios.dealab.api.response.factory.AncestorsFactory;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by vihanga on 28/10/2018.
 */

public class AncestorPromoResponseFactory implements AncestorsFactory {
	@Override
	public Ancestor parse(JSONObject response) throws IOException {
		return new ObjectMapper()
				.readValue(response.toString(), PromoResponse.class);
	}

}
