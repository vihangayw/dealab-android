package com.zinios.dealab.util.par;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zinios.dealab.R;

import java.text.DecimalFormat;

public class PARPoiLabel extends PARPoi {
	protected static final DecimalFormat FORMATTER_DISTANCE_LARGEST = new DecimalFormat("#### km");
	protected static final DecimalFormat FORMATTER_DISTANCE_LARGE = new DecimalFormat("###,## km");
	protected static final DecimalFormat FORMATTER_DISTANCE_SMALL = new DecimalFormat("### m");
	protected static Point defaultSize = new Point(256, 128);
	protected Point size = null;
	protected boolean hasCreatedView;
	protected String _distance;
	protected POI poi;
	protected TextView txtDistance;
	//	protected TextView txtOpenTime;
	protected TextView txtTitle;
	protected TextView txtDeal;
	protected float _lastUpdateAtDistance;
	protected Point offset = new Point();
	private String TAG = "PARPoiLabel";
	private View.OnClickListener onClickListener;

	public PARPoiLabel() {
	}

	public PARPoiLabel(Location location, POI poi) {
		super(location);
		this.poi = poi;
		this.offset.set(0, 0);
		setRadarRes();
	}

	public PARPoiLabel(Location atLocation) {
		super(atLocation);
	}

	public static Point getDefaultSize() {
		return defaultSize;
	}

	public static void setDefaultSize(Point defaultSize) {
		defaultSize = defaultSize;
	}

	private void setRadarRes() {
		switch (this.poi.getPoiType()) {
			case red:
				this.radarResourceId = R.drawable.ic_circle_ev;
				break;
			case blue:
				this.radarResourceId = R.drawable.ic_circle_ev;
				break;
			case green:
				this.radarResourceId = R.drawable.ic_circle_ev;
				break;
			case orange:
				this.radarResourceId = R.drawable.ic_circle_ev;
				break;
			default:
				this.radarResourceId = R.drawable.ic_circle_ev;
				break;
		}
	}

	public Point getOffset() {
		return this.offset;
	}

	public void setOffset(Point leftTop) {
		this.offset = leftTop;
	}

	public void createView() {
		if (this.ctx == null) {
			Log.e(this.TAG, "context is NULL");
		} else {
			LayoutInflater inflater = (LayoutInflater) this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (inflater == null) {
				Log.e(this.TAG, "Layout inflater is null");
			} else {
				this._labelView = (RelativeLayout) inflater.inflate(R.layout.item_poi, null);
				_labelView.setId(Integer.parseInt(poi.getId()));
				if (this.onClickListener != null) {
					this._labelView.setOnClickListener(this.onClickListener);
				}
//
				if (this.size == null) {
					this.size = new Point(defaultSize.x, defaultSize.y);
				}
//
				Resources r = this._labelView.getResources();
				int width = (int) TypedValue.applyDimension(1, (float) this.size.x, r.getDisplayMetrics());
				int height = (int) TypedValue.applyDimension(1, (float) this.size.y, r.getDisplayMetrics());
				this._labelView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
				//                this.halfSizeOfView = new PointF((float) (width / 2), (float) (height / 2));
				if (this._backgroundImageResource > -1) {
					this._labelView.setBackgroundResource(this._backgroundImageResource);
				}
//
				this.txtTitle = this._labelView.findViewById(R.id.txt_company);
				this.txtDeal = this._labelView.findViewById(R.id.txt_deal);
				this.txtDistance = this._labelView.findViewById(R.id.txt_distance);
//				this.txtOpenTime = this._labelView.findViewById(R.id.open_time);
//
				this.txtTitle.setText(this.poi.getPlaceTitle());
//				this.txtOpenTime.setText(this.poi.getOpenTime());
				this.txtDeal.setText(this.poi.getDeal());
//
				switch (this.poi.getPoiType()) {
					case red:
//						this.txtTitle.setBackgroundResource(R.drawable.bg_poi_blue);
//						this.txtDeal.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
						break;
					case blue:
//						this.txtTitle.setBackgroundResource(R.drawable.bg_poi_blue);
//						this.txtDeal.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
						break;
					case green:
//						this.txtTitle.setBackgroundResource(R.drawable.bg_poi_blue);
//						this.txtDeal.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
						break;
					case orange:
//						this.txtTitle.setBackgroundResource(R.drawable.bg_poi_blue);
//						this.txtDeal.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
						break;
					default:
//						this.txtTitle.setBackgroundResource(R.drawable.bg_poi_blue);
//						this.txtDeal.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimary));
						break;
				}

				this.hasCreatedView = true;
				this.updateContent();
			}
		}
	}

	public void updateContent() {
		if (this.hasCreatedView) {
			double distance = this.distanceToUser;
			if (distance >= 10000.0D) {
				if (Math.abs(distance - (double) this._lastUpdateAtDistance) < 1000.0D) {
					return;
				}

				distance = Math.floor(distance / 1000.0D);
				this._distance = FORMATTER_DISTANCE_LARGEST.format(distance);
			} else if (distance > 1000.0D) {
				if (Math.abs(distance - (double) this._lastUpdateAtDistance) < 100.0D) {
					return;
				}

				distance = Math.floor(distance / 1000.0D);
				this._distance = FORMATTER_DISTANCE_LARGE.format(distance);
			} else {
				if (Math.abs(distance - (double) this._lastUpdateAtDistance) < 10.0D) {
					return;
				}

				distance = Math.floor(distance / 5.0D) * 5.0D;
				this._distance = FORMATTER_DISTANCE_SMALL.format(distance);
			}

			if (this.txtDistance != null) {
				this.txtDistance.setText(this._distance);
			}

			this._lastUpdateAtDistance = (float) this.distanceToUser;
		}
	}

	public Point getSize() {
		return this.size;
	}

	public void setSize(Point size) {
		this.size = size;
	}

	public void setSize(int w, int h) {
		this.size = new Point(w, h);
	}

	public View.OnClickListener getOnClickListener() {
		return this.onClickListener;
	}

	public void setOnClickListener(View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
		if (this._labelView != null) {
			this._labelView.setOnClickListener(onClickListener);
		}

	}
}