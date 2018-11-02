package com.zinios.dealab.util.par;

import android.location.Location;


public class PARPoiLabelAdvanced extends PARPoiLabel {

	public PARPoiLabelAdvanced(Location location, POI poi) {
		super(location, poi);
	}

	public float getAltitude() {
		return (float) this.getLocation().getAltitude();
	}

	public void setAltitude(float altitude) {
		this.getLocation().setAltitude((double) altitude);
	}

	public void updateContent() {
		if (this.hasCreatedView) {
			super.updateContent();
//            this.txtOpenTime.setText(FORMATTER_DISTANCE_SMALL.format((double) this.getAltitude()));
		}
	}
}