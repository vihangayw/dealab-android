package com.zinios.dealab.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.github.florent37.viewanimator.ViewAnimator;
import com.zinios.dealab.R;

public class SplashActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_activty);

		ViewAnimator.animate(findViewById(R.id.img))
				.wobble().startDelay(50).start();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
				finish();
			}
		}, 1500);
	}
}
