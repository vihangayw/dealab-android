package com.zinios.dealab.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zinios.dealab.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class UtilityManager {

	public static void showSnackBarWithAction(String msg, int duration, String actionMsg,
	                                          CoordinatorLayout coordinatorLayout, View.OnClickListener listener) {
		Snackbar snackbar = Snackbar
				.make(coordinatorLayout, msg, duration)
				.setAction(actionMsg, listener);
		snackbar.show();
	}

	public static Drawable resizeImage(Context ctx, int resId, int iconWidth, int iconHeight) {

		// load the origial Bitmap
		Bitmap BitmapOrg = BitmapFactory.decodeResource(ctx.getResources(),
				resId);

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = iconWidth;
		int newHeight = iconHeight;

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);

		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);

		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		return new BitmapDrawable(ctx.getResources(), resizedBitmap);

	}
	public static boolean hasGPSDevice(Context context) {
		final LocationManager mgr = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		if (mgr == null)
			return false;
		final List<String> providers = mgr.getAllProviders();
		return providers != null && providers.contains(LocationManager.GPS_PROVIDER);
	}
	public static AlertDialog showAlert(final Context context, String title, String msg, String btnPositive,
	                                    String btnNegative, boolean cancelable,
	                                    DialogInterface.OnClickListener positiveClick,
	                                    DialogInterface.OnClickListener negativeClick) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogStyle);
		builder.setMessage(msg);
		if (!TextUtils.isEmpty(title))
			builder.setTitle(title);
		builder.setCancelable(cancelable);
		builder.setPositiveButton(btnPositive, positiveClick);
		builder.setNegativeButton(btnNegative, negativeClick);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
		//call after showing dialog
		if (positiveClick != null)
			alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
		if (negativeClick != null)
			alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorBlack));

		return alertDialog;
	}

	public static void showSnack(String msg, int duration, CoordinatorLayout coordinatorLayout) {
		Snackbar.make(coordinatorLayout, msg, duration).show();
	}

	public static ProgressDialog showProgressAlert(final Context context, String msg) {
		ProgressDialog builder = new ProgressDialog(context, R.style.DialogStyle);
		builder.setMessage(msg);
		builder.setCancelable(false);
		builder.show();
		return builder;
	}

	public static ObjectMapper getDefaultObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(getDefaultSimpleDateFormat());
		return objectMapper;
	}

	private static SimpleDateFormat getDefaultSimpleDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", java.util.Locale.getDefault());
	}

}
