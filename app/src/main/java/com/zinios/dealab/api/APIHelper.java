package com.zinios.dealab.api;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.zinios.dealab.api.response.Ancestor;
import com.zinios.dealab.api.response.Error;
import com.zinios.dealab.api.response.factory.AncestorsFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import static com.zinios.dealab.api.VolleySingleton.PARSE_ERROR;

/**
 * Created by vihanga on 20/8/2018.
 */
public class APIHelper {

	private static APIHelper instance;

	private APIHelper() {
	}

	public static APIHelper getInstance() {
		if (instance == null) instance = new APIHelper();
		return instance;
	}

	public void sendJSONRequestsWithParams(final PostManResponseListener context,
	                                       final AncestorsFactory factory,
	                                       int httpMethod, String apiUrl,
	                                       final JSONObject jsonObject) {

		JsonObjectRequest request = new JsonObjectRequest(httpMethod, apiUrl, jsonObject,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						if (context != null) {
							try {
								context.onResponse(factory.parse(response));
							} catch (IOException e) {
								e.printStackTrace();
								context.onError(new Error("IOException", e.getLocalizedMessage(), PARSE_ERROR));
							}
						}
					}

				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (context != null)
					context.onError(VolleySingleton.getInstance().getErrorMessage(error));
			}
		}) {
			@Override
			public Map<String, String> getHeaders() {
				return VolleySingleton.getInstance().getAPIHeaderJson();
			}
		};

		VolleySingleton.getInstance().addToRequestQueue(request);
	}


	public void sendJSONRequestsWithParamsWithAuth(final PostManResponseListener context,
	                                               final AncestorsFactory factory,
	                                               int httpMethod, String apiUrl,
	                                               final JSONObject jsonObject) {

		JsonObjectRequest request = new JsonObjectRequest(httpMethod, apiUrl, jsonObject,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						if (context != null) {
							try {
								context.onResponse(factory.parse(response));
							} catch (IOException e) {
								e.printStackTrace();
								context.onError(new Error("IOException", e.getLocalizedMessage(), PARSE_ERROR));
							}
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						if (context != null)
							context.onError(VolleySingleton.getInstance().getErrorMessage(error));
					}
				}) {
			@Override
			public Map<String, String> getHeaders() {
				return VolleySingleton.getInstance().getAPIHeaderJsonAuth();
			}
		};

		VolleySingleton.getInstance().addToRequestQueue(request);
	}

	public interface PostManResponseListener {
		void onResponse(Ancestor ancestor);

		void onError(Error error);
	}
}