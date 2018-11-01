package com.zinios.dealab.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.zinios.dealab.R;

public class NotificationActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// Show the Up button in the action bar.
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle("Notifications");
		}

	}
}
