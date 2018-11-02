package com.zinios.dealab.util.par;

import android.content.Context;
import android.graphics.PointF;
import android.renderscript.Matrix4f;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.zinios.dealab.ui.fragment.PARFragment;

import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

public class PARRadarView extends RelativeLayout {
	public static final int RADAR_MODE_THUMBNAIL = 1;
	public static final int RADAR_MODE_FULLSCREEN = 2;
	public static final float RADAR_RANGE_DEFAULT = 1000.0F;
	public static final int RADAR_SIZE_DEFAULT = 256;
	public static final long RENDER_RADAR_TIME_INTERVAL = 66L;
	public static final int RENDER_RADAR_TIME_DELAY = 10;
	protected static PARRadarView activeView;
	private final String TAG = "PARRadarView";
	public int radarMode;
	protected float radarRange;
	protected float radarInset = 32.0F;
	protected int radarRadius = -1;
	protected Matrix4f radarMatrix;
	protected PARFragment arViewController;
	protected Timer renderTimer;
	protected PointF fullscreenMargin;
	protected float fullscreenSizeOffset;
	private PointF center;

	public PARRadarView(Context context) {
		super(context);
		this.init();
	}

	public PARRadarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	public PARRadarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.init();
	}

	public static PARRadarView getActiveView() {
		return activeView;
	}

	private static void setActiveView(PARRadarView activeView) {
		activeView = activeView;
	}

	private void init() {
		this.radarRange = 1000.0F;
		this.radarMode = -1;
		this.radarRadius = -1;
		this.radarMatrix = new Matrix4f();
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = ((ViewGroup)this.getParent()).getMeasuredWidth();
//        int height = ((ViewGroup)this.getParent()).getMeasuredHeight();
//        MarginLayoutParams params = (MarginLayoutParams)this.getLayoutParams();
//        int size;
//        if(this.radarMode == 1) {
//            size = (int)TypedValue.applyDimension(1, 256.0F, this.getResources().getDisplayMetrics());
//            int x = width - size - params.rightMargin;
//            int y = height - size - params.bottomMargin;
//            this.setX((float)x);
//            this.setY((float)y);
////            params.widtt = size;
////            this.setLayoutParams(params);
//        } else if(this.radarMode == 2) {
//            width = (int)((float)width + this.fullscreenMargin.x);
//            height = (int)((float)height + this.fullscreenMargin.y);
//            size = Math.min(width, height) - (int)this.fullscreenSizeOffset;
//            float x = (float)((width - size) / 2);
//            float y = (float)((height - size) / 2);
//            this.setX(x);
//            this.setY(y);
////            params.width = size;
////            params.height = size;
////            this.setLayoutParams(params);
//        }
//
//        this.setMeasuredDimension(params.width, params.height);
	}

	private void start(PARFragment arViewController) {
		this.arViewController = arViewController;
		setActiveView(this);
		this.renderTimer = new Timer("PARRadarViewTimer");
		this.renderTimer.schedule(new TimerTask() {
			public void run() {
				PARRadarView.this.drawRadar();
			}
		}, 10L, 66L);
	}

	public void stop() {
		if (renderTimer != null) {
			this.renderTimer.cancel();
			this.renderTimer.purge();
			this.renderTimer = null;
		}
		setActiveView(null);
	}

	public void showRadarInMode(int radarMode, PARFragment controller) {
		this.radarMode = radarMode;
		switch (this.radarMode) {
			case 1:
				this.setRadarToThumbnail();
				break;
			case 2:
				this.setRadarToFullscreen();
				break;
			default:
				this.hideRadar();
		}

		this.start(controller);
		this.setVisibility(VISIBLE);
	}

	public void hideRadar() {
		this.setVisibility(GONE);
		this.stop();
	}

	public void drawRadar() {
		float[] g = PSKDeviceAttitude.sharedDeviceAttitude().getNormalizedGravity();
		PARPoi.setDeviceGravity(g);
		this.radarMatrix.load(PSKDeviceAttitude.sharedDeviceAttitude().getHeadingMatrix());
		this.radarRadius = this.getHeight() < this.getWidth() ? this.getHeight() / 2 : this.getWidth() / 2;
		this.center = new PointF((float) this.radarRadius, (float) this.radarRadius);
		ListIterator<PARPoi> it = PARController.getPois().listIterator();
		final PARRadarView r = this;

		while (it.hasNext()) {
			try {
				final PARPoi poi = it.next();
				if (!poi.isHidden && !poi.isClippedByDistance) {
					this.arViewController.getActivity().runOnUiThread(new Runnable() {
						public void run() {
							poi.renderInRadar(r);
						}
					});
				}
			} catch (ConcurrentModificationException e) {

			}
		}

		this.refreshDrawableState();
	}

	public Matrix4f getRadarMatrix() {
		return this.radarMatrix;
	}

	public boolean isRadarVisible() {
		return this.getVisibility() == VISIBLE;
	}

	public int getRadarMode() {
		return this.radarMode;
	}

	public float getRadarRange() {
		return this.radarRange;
	}

	public void setRadarRange(float range) {
		this.radarRange = range;
	}

	public float getRadarInset() {
		return this.radarInset;
	}

	public void setRadarInset(float inset) {
		this.radarInset = inset;
	}

	public PointF getCenter() {
		return this.center;
	}

	public float getRadarRadiusForRendering() {
		return (float) this.radarRadius - this.radarInset;
	}

	public int getRadarRadius() {
		return this.radarRadius;
	}

	public void setRadarRadius(int radius) {
		this.radarRadius = radius;
	}

	public void setRadarToFullscreen() {
		this.setRadarToFullscreen(new PointF(0.0F, 0.0F), 0.0F);
	}

	public void setRadarToFullscreen(PointF offset, float sizeOffset) {
		this.fullscreenMargin = offset;
		this.fullscreenSizeOffset = sizeOffset;
		this.radarMode = 2;
		this.requestLayout();
	}

	public void setRadarToThumbnail() {
		this.radarMode = 1;
		this.requestLayout();
	}
}
