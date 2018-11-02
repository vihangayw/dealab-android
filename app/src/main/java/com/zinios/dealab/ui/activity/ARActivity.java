package com.zinios.dealab.ui.activity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.zinios.dealab.R;
import com.zinios.dealab.ui.fragment.ARFragment;
import com.zinios.dealab.util.Constants;


public class ARActivity extends BaseActivity {

	private double mLat;
	private double mLng;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_ar);
		mLat = getIntent().getDoubleExtra(Constants.NEAR_BY_SPOTS_LAT, 0.0);
		mLng = getIntent().getDoubleExtra(Constants.NEAR_BY_SPOTS_LNG, 0.0);
		initializeComponents();
//        getDeviceLocation();
	}

	@SuppressWarnings("deprecation")
	public void initializeComponents() {
		ARFragment arFragment = new ARFragment();
		arFragment.setmLat(mLat);
		arFragment.setmLng(mLng);
//        arFragment.setSpotList(spotList);
		getFragmentManager().beginTransaction().add(R.id.container, arFragment).commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}


}
