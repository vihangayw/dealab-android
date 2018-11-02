package com.zinios.dealab.util.par;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;


public class PARDataCollector extends AsyncTask<Void, Void, Void> {
	private String formUrl = "https://docs.google.com/a/dopanic.com/forms/d/1jNYcNDvtrz4DY4xjGOXblzknFkIJ-SrppRLKcSSS6fQ/formResponse";
	private ArrayList<BasicNameValuePair> data = new ArrayList();

	public PARDataCollector() {
	}

	protected Void doInBackground(Void... voids) {
		this.post();
		return null;
	}

	public void addEntry(BasicNameValuePair data) {
		this.data.add(data);
	}

	public void post() {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(this.formUrl);

		try {
			post.setEntity(new UrlEncodedFormEntity(this.data));
		} catch (Exception var6) {
			Log.e("DATA_COLLECTOR", "Encoding not supported", var6);
		}

		try {
			client.execute(post);
		} catch (Exception var4) {
			Log.e("DATA_COLLECTOR", "client protocol exception", var4);
		}

	}
}