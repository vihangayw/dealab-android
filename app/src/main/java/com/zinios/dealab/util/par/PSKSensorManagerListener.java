package com.zinios.dealab.util.par;

import android.location.GpsStatus.Listener;
import android.location.LocationListener;

public interface PSKSensorManagerListener extends LocationListener, Listener {
	void onRotationVectorChanged(float[] var1, long var2);

	void onRotationVectorAccuracyChanged(int var1);

	void onGravityChanged(float[] var1, long var2);

	void onGravityAccuracyChanged(int var1);

	void onOrientationChanged(int var1);
}
