package com.zinios.dealab.api.response.factory;



import com.zinios.dealab.api.response.Ancestor;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by vihanga on 28/10/2018.
 */

public interface AncestorsFactory {
	Ancestor parse(JSONObject response) throws IOException;
}
