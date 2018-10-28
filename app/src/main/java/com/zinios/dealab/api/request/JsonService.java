package com.zinios.dealab.api.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vihanga on 27/2/18.
 */
public class JsonService {


	public JsonService() {
	}

	/**
	 * Converts a String to a JsonNode
	 *
	 * @param string the String to be converted
	 * @return json
	 * @throws java.io.IOException
	 */
	public static JSONObject toJsonNode(String key, String string) throws JSONException {

		JSONObject jsonObject = new JSONObject();
		jsonObject.put(key, string);
		return jsonObject;
	}

	/**
	 * Converts an object to a JsonNode
	 *
	 * @param object the object to be converted
	 * @return
	 */
	public static JSONObject toJsonNode(Object object) throws JSONException {
		ObjectMapper om = new ObjectMapper();
		//        om.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"));
		//        om.setTimeZone(TimeZone.getDefault());
		JsonNode n = om.convertValue(object, JsonNode.class);
		return new JSONObject(n.toString());

	}

}
