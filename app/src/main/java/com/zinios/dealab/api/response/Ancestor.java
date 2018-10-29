package com.zinios.dealab.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by vihanga on 28/10/2018.
 */

public class Ancestor<T> {

	private String message;
	private int status;
	private T data;

	@JsonCreator
	Ancestor(@JsonProperty("message") String message,
	         @JsonProperty("statusCode") int status,
	         @JsonProperty("data") T data) {
		this.message = message;
		this.data = data;
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public T getData() {
		return data;
	}

	public int getStatus() {
		return status;
	}
}