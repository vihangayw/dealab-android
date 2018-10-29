package com.zinios.dealab.ui.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.zinios.dealab.DealabApplication;
import com.zinios.dealab.R;

public class BaseActivity extends AppCompatActivity {

	private ProgressDialog progressDialog;
	private KProgressHUD kProgressHUD;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	void initializeViews() {
		kProgressHUD = KProgressHUD.create(BaseActivity.this)
				.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
				.setLabel(getString(R.string.please_wait))
				.setCancellable(false)
				.setAnimationSpeed(2)
				.setDimAmount(0.2f);
	}

	void setListeners() {

	}

	void showProgress() {
		kProgressHUD.show();
	}

	void hideProgress() {
		if (kProgressHUD.isShowing()) {
			kProgressHUD.dismiss();
		}
	}

	public boolean checkNetwork() {
		if (isNetworkConnected()) {
			return true;
		}
		Toast.makeText(this, getString(R.string.no_network), Toast.LENGTH_SHORT).show();
		return false;
	}

	public boolean isNetworkConnected() {
		ConnectivityManager cm =
				(ConnectivityManager) DealabApplication.getInstance().getApplicationContext()
						.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			return activeNetwork != null &&
					activeNetwork.isConnectedOrConnecting();
		}
		return false;
	}

	void hideKeyBoard() {
		View view = this.getCurrentFocus();
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	@SuppressLint("RestrictedApi")
	public void progressDialogSystem(final String msg) {
		if (progressDialog == null)
			progressDialog = new ProgressDialog(new ContextThemeWrapper(BaseActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog));
		progressDialog.setCancelable(false);
		progressDialog.setMessage(msg);
		progressDialog.show();
	}

	public void hideSystemProgress() {
		if (progressDialog != null) progressDialog.dismiss();
		progressDialog = null;

	}
}
