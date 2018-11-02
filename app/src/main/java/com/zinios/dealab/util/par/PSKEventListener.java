package com.zinios.dealab.util.par;

import android.location.Location;

public interface PSKEventListener {
	void onLocationChangedEv(Location var1);

	void onDeviceOrientationChanged(PSKDeviceOrientation var1);
}
