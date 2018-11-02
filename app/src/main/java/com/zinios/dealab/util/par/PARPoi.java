package com.zinios.dealab.util.par;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.Location;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zinios.dealab.ui.fragment.PARFragment;


public class PARPoi {
	private static float[] deviceGravity;
	private static float viewSin = 0.0F;
	private static float viewCos = 0.0F;
	protected int _backgroundImageResource = -1;
	protected Context ctx;
	protected boolean observed;
	protected int radarResourceId;
	protected Location location;
	protected Point offset = new Point(0, 0);
	protected double distanceToUser = 0.0D;
	protected RelativeLayout _labelView;
	protected boolean isHidden = false;
	protected boolean isClippedByDistance = false;
	protected boolean isClippedByViewport = false;
	protected View radarView;
	//    protected PointF halfSizeOfView = new PointF();
	protected boolean isAddedToController;
	protected boolean addedToView;
	protected boolean addedToRadar;
	private double lastDistanceToUser;
	private float angleToUser;
	private float[] toUserRotationMatrix = new float[16];
	private float[] worldPositionVector4 = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
	private float[] worldToScreenSpaceVector4 = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
	private PointF relativeScreenPosition = new PointF();
	private RectF relativeViewportBounds = new RectF(-0.25F, -0.25F, 1.5F, 1.5F);
	private boolean hadLocationUpdate;
	private PSKVector3 ecefCoordinatesDevice;
	private PSKVector3 ecefCoordinatesPOI;
	private PSKVector3 worldToRadarSpace;
	private float[] radarSpace;

	public PARPoi() {
		ctx = null;
		this.ctx = PARController.getContext();
	}

	public PARPoi(Location atLocation) {
		this.setLocation(atLocation);
		ctx = null;
		this.ctx = PARController.getContext();
	}

	public static void setDeviceGravity(float[] gravity) {
		deviceGravity = gravity;
	}

	public static void setViewRotation(float angle) {
		viewSin = PSKMath.linsin(angle);
		viewCos = PSKMath.lincos(angle);
	}

	public View getRadarView() {
		return this.radarView;
	}

	public boolean isHidden() {
		return this.isHidden;
	}

	public boolean isClippedByDistance() {
		return this.isClippedByDistance;
	}

	public View getView() {
		return this._labelView;
	}

	public void renderInRadar(PARRadarView radar) {
		if (this.hadLocationUpdate) {
			if (this.isAddedToController) {
				if (this.radarView == null) {
					this.radarView = new ImageView(this.ctx);
					this.radarView.setPadding(8, 8, 8, 8);
					this.radarView.setBackgroundResource(this.radarResourceId);
					this.radarView.setVisibility(View.VISIBLE);
				}

				float range = radar.getRadarRange();
				float radius = radar.getRadarRadiusForRendering();
				float distanceOnRadar = Math.min((float) this.distanceToUser / range, 1.0F) * radius;
				this.worldToRadarSpace = PSKVector3.Zero();
				PSKVector3 v = new PSKVector3(0.0F, 0.0F, distanceOnRadar);
				float[] finalRotation = PSKMath.PSKMatrixFastMultiplyWithMatrix(radar.getRadarMatrix().getArray(), this.toUserRotationMatrix);
				this.worldToRadarSpace = PSKMath.PSKMatrix3x3MultiplyWithVector3(finalRotation, v);
				float[] tempWorldToRadarSpace = new float[]{this.worldToRadarSpace.x, this.worldToRadarSpace.y, this.worldToRadarSpace.z};
				this.radarSpace = PSKMath.PSKRadarCoordinatesFromVectorWithGravity(tempWorldToRadarSpace, PSKDeviceAttitude.sharedDeviceAttitude().getNormalizedGravity());
				float var10001 = this.radarSpace[1];
				float x = radar.getCenter().x + PSKMath.clampf(var10001, -radius, radius);
				var10001 = this.radarSpace[0];
				float y = radar.getCenter().y - PSKMath.clampf(var10001, -radius, radius);
				this.radarView.setX(x - (float) this.radarView.getMeasuredWidth() * 0.5F);
				this.radarView.setY(y - (float) this.radarView.getMeasuredHeight() * 0.5F);
				if (!this.addedToRadar) {
					this.addToRadar(radar);
				}
			}
		}
	}

	public PointF getRelativeScreenPosition() {
		return this.relativeScreenPosition != null ? this.relativeScreenPosition : new PointF(0.0F, 0.0F);
	}

	public void updateLocation() {
		PSKDeviceAttitude deviceAttitude = PSKDeviceAttitude.sharedDeviceAttitude();
		if (deviceAttitude != null) {
			Location userLocation = deviceAttitude.getLocation();
			if (userLocation != null) {
				this.lastDistanceToUser = this.distanceToUser;
				this.ecefCoordinatesDevice = deviceAttitude.getEcefCoordinates();
				PSKVector3Double enuCoordinates = PSKMath.PSKEcefToEnu(this.getLocation().getLatitude(), this.getLocation().getLongitude(), this.ecefCoordinatesDevice, this.ecefCoordinatesPOI);
				if (this instanceof PARPoiLabelAdvanced) {
					this.worldPositionVector4 = new float[]{(float) enuCoordinates.x, (float) enuCoordinates.y, (float) enuCoordinates.z, 1.0F};
				} else {
					this.worldPositionVector4 = new float[]{(float) enuCoordinates.x, (float) enuCoordinates.y, 0.0F, 1.0F};
				}

				this.distanceToUser = (double) this.getLocation().distanceTo(userLocation);
				float[] distanceResults = new float[3];
				Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), this.getLocation().getLatitude(), this.getLocation().getLongitude(), distanceResults);
				this.angleToUser = distanceResults[2];
				PSKMath.PSKMatrixSetYRotationUsingDegrees(this.toUserRotationMatrix, this.angleToUser);
				if (this.distanceToUser < (double) PARController.CLIP_POIS_NEARER_THAN) {
					this.isClippedByDistance = true;
				} else if (this.distanceToUser > (double) PARController.CLIP_POIS_FARER_THAN) {
					this.isClippedByDistance = true;
				} else {
					this.isClippedByDistance = false;
				}

				if (this.isClippedByDistance) {
					if (this.addedToView) {
						this.removeFromView();
					}

					if (this.addedToRadar) {
						this.removeFromRadar();
					}
				}

				this.hadLocationUpdate = true;
				if (this.distanceToUser != this.lastDistanceToUser) {
					this.updateContent();
				}

			}
		}
	}

	public void updateContent() {
	}

	public boolean isInView(float[] perspectiveMatrix) {
		int x = 0;
		int y = 1;
		int z = 2;
		int w = 3;
		this.worldToScreenSpaceVector4 = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
		Matrix.multiplyMV(this.worldToScreenSpaceVector4, 0, perspectiveMatrix, 0, this.worldPositionVector4, 0);
		if (this.worldToScreenSpaceVector4[z] >= 0.0F) {
			return false;
		} else {
			PointF p = PSKMath.RotatedPointAboutOrigin(this.worldToScreenSpaceVector4[x] / this.worldToScreenSpaceVector4[w], this.worldToScreenSpaceVector4[y] / this.worldToScreenSpaceVector4[w], viewSin, viewCos);
			this.relativeScreenPosition.x = (p.x + 1.0F) * 0.5F;
			this.relativeScreenPosition.y = (p.y + 1.0F) * 0.5F;
			return this.relativeViewportBounds.contains(this.relativeScreenPosition.x, this.relativeScreenPosition.y);
		}
	}

	protected Point getOffset() {
		return this.offset;
	}

	/**
	 * add poi items to view
	 *
	 * @param parent root view
	 */
	public void renderInView(PARFragment parent) {
		if (this.hadLocationUpdate) {
			if (this.isAddedToController) {
				this.isClippedByViewport = !this.isInView(parent.getPerspectiveCameraMatrix());
				if (this.isClippedByViewport) {
					if (this.addedToView) {
						this.removeFromView();
					}

				} else {
					if (this._labelView == null) {
						this.createView();
					}

					Point screenSize = parent.getScreenSize();
					int x = (int) ((float) screenSize.x * this.relativeScreenPosition.x);
					int y = (int) ((float) screenSize.y * (1.0F - this.relativeScreenPosition.y));
					float finalX = (float) x - (float) this._labelView.getMeasuredWidth() * 0.5F + (float) this.offset.x;
					float finalY = (float) y - (float) this._labelView.getMeasuredHeight() * 0.5F - (float) this.offset.y;
					this._labelView.setX(finalX);
					this._labelView.setY(finalY);
					if (this.isObserved()) {
						Log.d(PARPoi.class.getSimpleName(), "relativeScreenPosition: " + this.relativeScreenPosition.toString() + " x/y: " + x + ", " + y + " final x/y: " + finalX + ", " + finalY + " size: " + this._labelView.getMeasuredWidth() + "x" + this._labelView.getMeasuredHeight() + " screenMargin: " + (float) parent.getScreenMarginX() * 0.5F + " " + (float) parent.getScreenMarginY() * 0.5F);
					}

					if (!this.addedToView) {
						this.addToView(parent);
					}
				}
			}
		}
	}

	public void createView() {
	}

	public void onAddedToARController() {
		this.isAddedToController = true;
	}

	public void onRemovedFromARController() {
		this.isAddedToController = false;

		try {
			if (this._labelView != null && this._labelView.getParent() != null) {
				((ViewGroup) this._labelView.getParent()).removeView(this._labelView);
			}
		} catch (NullPointerException var2) {
			var2.printStackTrace();
		} catch (Exception var3) {
			var3.printStackTrace();
		}

		if (this.addedToRadar) {
			this.removeFromRadar();
		}

	}

	public int getBackgroundImageResource() {
		return this._backgroundImageResource;
	}

	public void setBackgroundImageResource(int backgroundImageResource) {
		this._backgroundImageResource = backgroundImageResource;
	}

	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location atLocation) {
		this.location = atLocation;
		this.ecefCoordinatesPOI = PSKMath.PSKConvertLatLonToEcef(atLocation.getLatitude(), atLocation.getLongitude(), atLocation.getAltitude());
	}

	public boolean isClippedByViewport() {
		return this.isClippedByViewport;
	}

	void addToView(PARFragment theView) {
		this._labelView.setVisibility(View.VISIBLE);
		theView.getARView().addView(_labelView);
		this.addedToView = true;

	}

	private boolean isViewOverlapping(View firstView, View secondView) {
		int[] firstPosition = new int[2];
		int[] secondPosition = new int[2];

		firstView.getLocationOnScreen(firstPosition);
		secondView.getLocationOnScreen(secondPosition);

		// Rect constructor parameters: left, top, right, bottom
		Rect rectFirstView = new Rect(firstPosition[0], firstPosition[1],
				firstPosition[0] + firstView.getMeasuredWidth(), firstPosition[1] + firstView.getMeasuredHeight());
		Rect rectSecondView = new Rect(secondPosition[0], secondPosition[1],
				secondPosition[0] + secondView.getMeasuredWidth(), secondPosition[1] + secondView.getMeasuredHeight());
		return rectFirstView.intersect(rectSecondView);
	}

	void removeFromView() {
		try {
			if (this._labelView != null && this._labelView.getParent() != null) {
				((ViewGroup) this._labelView.getParent()).removeView(this._labelView);
				this._labelView.setVisibility(View.GONE);
				this.addedToView = false;
			}
		} catch (NullPointerException var2) {
			var2.printStackTrace();
		} catch (Exception var3) {
			var3.printStackTrace();
		}

	}

	void addToRadar(PARRadarView theRadar) {
		theRadar.addView(this.radarView);
		this.radarView.setVisibility(View.VISIBLE);
		this.addedToRadar = true;
	}

	void removeFromRadar() {
		try {
			if (this.radarView != null && this.radarView.getParent() != null) {
				((ViewGroup) this.radarView.getParent()).removeView(this.radarView);
			}
		} catch (NullPointerException var2) {
			var2.printStackTrace();
		} catch (Exception var3) {
			var3.printStackTrace();
		}

		this.radarView.setVisibility(View.GONE);
		this.addedToRadar = false;
	}

	public boolean isObserved() {
		return this.observed;
	}

	public void setObserved(boolean observed) {
		this.observed = observed;
	}

	public boolean isHadLocationUpdate() {
		return this.hadLocationUpdate;
	}
}

//
//
//package com.zinios.dealab.util.par;
//
//		import android.content.Context;
//		import android.graphics.Point;
//		import android.graphics.PointF;
//		import android.graphics.Rect;
//		import android.graphics.RectF;
//		import android.location.Location;
//		import android.opengl.Matrix;
//		import android.view.View;
//		import android.view.ViewGroup;
//		import android.widget.ImageView;
//		import android.widget.RelativeLayout;
//
//		import com.ceffectz.parkmeapp.ui.fragment.PARFragment;
//
//		import java.util.ArrayList;
//		import java.util.Collections;
//		import java.util.List;
//
//
//public class PARPoi {
//	private static float[] deviceGravity;
//	private static float viewSin = 0.0F;
//	private static float viewCos = 0.0F;
//	protected int _backgroundImageResource = -1;
//	protected Context ctx;
//	protected boolean observed;
//	protected int radarResourceId;
//	protected Location location;
//	protected Point offset = new Point(0, 0);
//	protected double distanceToUser = 0.0D;
//	protected RelativeLayout _labelView;
//	protected boolean isHidden = false;
//	protected boolean isClippedByDistance = false;
//	protected boolean isClippedByViewport = false;
//	protected View radarView;
//	//    protected PointF halfSizeOfView = new PointF();
//	protected boolean isAddedToController;
//	protected boolean addedToView;
//	protected boolean addedToRadar;
//	private double lastDistanceToUser;
//	private float angleToUser;
//	private float[] toUserRotationMatrix = new float[16];
//	private float[] worldPositionVector4 = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
//	private float[] worldToScreenSpaceVector4 = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
//	private PointF relativeScreenPosition = new PointF();
//	private RectF relativeViewportBounds = new RectF(-0.25F, -0.25F, 1.5F, 1.5F);
//	private boolean hadLocationUpdate;
//	private PSKVector3 ecefCoordinatesDevice;
//	private PSKVector3 ecefCoordinatesPOI;
//	private PSKVector3 worldToRadarSpace;
//	private float[] radarSpace;
//
//	public PARPoi() {
//		ctx = null;
//		this.ctx = PARController.getContext();
//	}
//
//	public PARPoi(Location atLocation) {
//		this.setLocation(atLocation);
//		ctx = null;
//		this.ctx = PARController.getContext();
//	}
//
//	public static void setDeviceGravity(float[] gravity) {
//		deviceGravity = gravity;
//	}
//
//	public static void setViewRotation(float angle) {
//		viewSin = PSKMath.linsin(angle);
//		viewCos = PSKMath.lincos(angle);
//	}
//
//	public View getRadarView() {
//		return this.radarView;
//	}
//
//	public boolean isHidden() {
//		return this.isHidden;
//	}
//
//	public boolean isClippedByDistance() {
//		return this.isClippedByDistance;
//	}
//
//	public View getView() {
//		return this._labelView;
//	}
//
//	public void renderInRadar(PARRadarView radar) {
//		if (this.hadLocationUpdate) {
//			if (this.isAddedToController) {
//				if (this.radarView == null) {
//					this.radarView = new ImageView(this.ctx);
//					this.radarView.setPadding(8, 8, 8, 8);
//					this.radarView.setBackgroundResource(this.radarResourceId);
//					this.radarView.setVisibility(View.VISIBLE);
//				}
//
//				float range = radar.getRadarRange();
//				float radius = radar.getRadarRadiusForRendering();
//				float distanceOnRadar = Math.min((float) this.distanceToUser / range, 1.0F) * radius;
//				this.worldToRadarSpace = PSKVector3.Zero();
//				PSKVector3 v = new PSKVector3(0.0F, 0.0F, distanceOnRadar);
//				float[] finalRotation = PSKMath.PSKMatrixFastMultiplyWithMatrix(radar.getRadarMatrix().getArray(), this.toUserRotationMatrix);
//				this.worldToRadarSpace = PSKMath.PSKMatrix3x3MultiplyWithVector3(finalRotation, v);
//				float[] tempWorldToRadarSpace = new float[]{this.worldToRadarSpace.x, this.worldToRadarSpace.y, this.worldToRadarSpace.z};
//				this.radarSpace = PSKMath.PSKRadarCoordinatesFromVectorWithGravity(tempWorldToRadarSpace, PSKDeviceAttitude.sharedDeviceAttitude().getNormalizedGravity());
//				float var10001 = this.radarSpace[1];
//				float x = radar.getCenter().x + PSKMath.clampf(var10001, -radius, radius);
//				var10001 = this.radarSpace[0];
//				float y = radar.getCenter().y - PSKMath.clampf(var10001, -radius, radius);
//				this.radarView.setX(x - (float) this.radarView.getMeasuredWidth() * 0.5F);
//				this.radarView.setY(y - (float) this.radarView.getMeasuredHeight() * 0.5F);
//				if (!this.addedToRadar) {
//					this.addToRadar(radar);
//				}
//			}
//		}
//	}
//
//	public PointF getRelativeScreenPosition() {
//		return this.relativeScreenPosition != null ? this.relativeScreenPosition : new PointF(0.0F, 0.0F);
//	}
//
//	public void updateLocation() {
//		PSKDeviceAttitude deviceAttitude = PSKDeviceAttitude.sharedDeviceAttitude();
//		if (deviceAttitude != null) {
//			Location userLocation = deviceAttitude.getLocation();
//			if (userLocation != null) {
//				this.lastDistanceToUser = this.distanceToUser;
//				this.ecefCoordinatesDevice = deviceAttitude.getEcefCoordinates();
//				PSKVector3Double enuCoordinates = PSKMath.PSKEcefToEnu(this.getLocation().getLatitude(), this.getLocation().getLongitude(), this.ecefCoordinatesDevice, this.ecefCoordinatesPOI);
//				if (this instanceof PARPoiLabelAdvanced) {
//					this.worldPositionVector4 = new float[]{(float) enuCoordinates.x, (float) enuCoordinates.y, (float) enuCoordinates.z, 1.0F};
//				} else {
//					this.worldPositionVector4 = new float[]{(float) enuCoordinates.x, (float) enuCoordinates.y, 0.0F, 1.0F};
//				}
//
//				this.distanceToUser = (double) this.getLocation().distanceTo(userLocation);
//				float[] distanceResults = new float[3];
//				Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), this.getLocation().getLatitude(), this.getLocation().getLongitude(), distanceResults);
//				this.angleToUser = distanceResults[2];
//				PSKMath.PSKMatrixSetYRotationUsingDegrees(this.toUserRotationMatrix, this.angleToUser);
//				if (this.distanceToUser < (double) PARController.CLIP_POIS_NEARER_THAN) {
//					this.isClippedByDistance = true;
//				} else if (this.distanceToUser > (double) PARController.CLIP_POIS_FARER_THAN) {
//					this.isClippedByDistance = true;
//				} else {
//					this.isClippedByDistance = false;
//				}
//
//				if (this.isClippedByDistance) {
//					if (this.addedToView) {
//						this.removeFromView();
//					}
//
//					if (this.addedToRadar) {
//						this.removeFromRadar();
//					}
//				}
//
//				this.hadLocationUpdate = true;
//				if (this.distanceToUser != this.lastDistanceToUser) {
//					this.updateContent();
//				}
//
//			}
//		}
//	}
//
//	public void updateContent() {
//	}
//
//	public boolean isInView(float[] perspectiveMatrix) {
//		int x = 0;
//		int y = 1;
//		int z = 2;
//		int w = 3;
//		this.worldToScreenSpaceVector4 = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
//		Matrix.multiplyMV(this.worldToScreenSpaceVector4, 0, perspectiveMatrix, 0, this.worldPositionVector4, 0);
//		if (this.worldToScreenSpaceVector4[z] >= 0.0F) {
//			return false;
//		} else {
//			PointF p = PSKMath.RotatedPointAboutOrigin(this.worldToScreenSpaceVector4[x] / this.worldToScreenSpaceVector4[w], this.worldToScreenSpaceVector4[y] / this.worldToScreenSpaceVector4[w], viewSin, viewCos);
//			this.relativeScreenPosition.x = (p.x + 1.0F) * 0.5F;
//			this.relativeScreenPosition.y = (p.y + 1.0F) * 0.5F;
//			return this.relativeViewportBounds.contains(this.relativeScreenPosition.x, this.relativeScreenPosition.y);
//		}
//	}
//
//	protected Point getOffset() {
//		return this.offset;
//	}
//
//	/**
//	 * add poi items to view
//	 *
//	 * @param parent root view
//	 */
//	public void renderInView(PARFragment parent) {
//		if (this.hadLocationUpdate) {
//			if (this.isAddedToController) {
//				this.isClippedByViewport = !this.isInView(parent.getPerspectiveCameraMatrix());
//				if (this.isClippedByViewport) {
//					if (this.addedToView) {
//						this.removeFromView();
//					}
//
//				} else {
//					if (this._labelView == null) {
//						this.createView();
//					}
//
//					Point screenSize = parent.getScreenSize();
//					int x = (int) ((float) screenSize.x * this.relativeScreenPosition.x);
//					int y = (int) ((float) screenSize.y * (1.0F - this.relativeScreenPosition.y));
//					float finalX = (float) x - (float) this._labelView.getMeasuredWidth() * 0.5F + (float) this.offset.x;
//					float finalY = (float) y - (float) this._labelView.getMeasuredHeight() * 0.5F - (float) this.offset.y;
//
//					//set the location according to the no. of items
//					List<PARPoi> spotList = PARController.getPois();
//					int index = 0;
//					List<PARPoi> visible = new ArrayList<>();
//					if (spotList != null)
//						for (int i = 0; i < spotList.size(); i++) {
//							PARPoi poi = spotList.get(i);
//							View viewById = poi.getView();
//							if (viewById != null && viewById.isShown()) {
//								visible.add(poi); // add currently visible items
//							}
//						}
//					for (int i = 0; i < visible.size(); i++) {
//						PARPoi poi = visible.get(i);
//						View viewById = poi.getView();
//						if (viewById != null && viewById.isShown() && _labelView.getId() == viewById.getId()) {//_labelView.getId() == viewById.getId()
//							index = i; // index of current item
//							break;
//						}
//					}
//					this._labelView.setX(finalX);
//					float calY = finalY + ((this._labelView.getMeasuredHeight() / 1.1F) * (index - 1));
//					this._labelView.setY(calY); // set Y position according to the index of current item
//
//					if (!this.addedToView) {
//						if (spotList != null)
//							for (int i = 0; i < spotList.size(); i++) {
//								PARPoi poi = spotList.get(i);
//								View viewById = poi.getView();
//								if (viewById != null && _labelView.getId() == viewById.getId()) {
//									Collections.swap(spotList, i, spotList.size() - 1);
//									break;
//								}
//							}
//						this.addToView(parent, _labelView);
//					}
//				}
//			}
//		}
//	}
//
//	public void createView() {
//	}
//
//	public void onAddedToARController() {
//		this.isAddedToController = true;
//	}
//
//	public void onRemovedFromARController() {
//		this.isAddedToController = false;
//
//		try {
//			if (this._labelView != null && this._labelView.getParent() != null) {
//				((ViewGroup) this._labelView.getParent()).removeView(this._labelView);
//			}
//		} catch (NullPointerException var2) {
//			var2.printStackTrace();
//		} catch (Exception var3) {
//			var3.printStackTrace();
//		}
//
//		if (this.addedToRadar) {
//			this.removeFromRadar();
//		}
//
//	}
//
//	public int getBackgroundImageResource() {
//		return this._backgroundImageResource;
//	}
//
//	public void setBackgroundImageResource(int backgroundImageResource) {
//		this._backgroundImageResource = backgroundImageResource;
//	}
//
//	public Location getLocation() {
//		return this.location;
//	}
//
//	public void setLocation(Location atLocation) {
//		this.location = atLocation;
//		this.ecefCoordinatesPOI = PSKMath.PSKConvertLatLonToEcef(atLocation.getLatitude(), atLocation.getLongitude(), atLocation.getAltitude());
//	}
//
//	public boolean isClippedByViewport() {
//		return this.isClippedByViewport;
//	}
//
//	void addToView(PARFragment theView, View view) {
//		this._labelView.setVisibility(View.VISIBLE);
//		theView.getARView().addView(_labelView);
//		this.addedToView = true;
//
//	}
//
//	private boolean isViewOverlapping(View firstView, View secondView) {
//		int[] firstPosition = new int[2];
//		int[] secondPosition = new int[2];
//
//		firstView.getLocationOnScreen(firstPosition);
//		secondView.getLocationOnScreen(secondPosition);
//
//		// Rect constructor parameters: left, top, right, bottom
//		Rect rectFirstView = new Rect(firstPosition[0], firstPosition[1],
//				firstPosition[0] + firstView.getMeasuredWidth(), firstPosition[1] + firstView.getMeasuredHeight());
//		Rect rectSecondView = new Rect(secondPosition[0], secondPosition[1],
//				secondPosition[0] + secondView.getMeasuredWidth(), secondPosition[1] + secondView.getMeasuredHeight());
//		return rectFirstView.intersect(rectSecondView);
//	}
//
//	void removeFromView() {
//		try {
//			if (this._labelView != null && this._labelView.getParent() != null) {
//				((ViewGroup) this._labelView.getParent()).removeView(this._labelView);
//				this._labelView.setVisibility(View.GONE);
//				this.addedToView = false;
//			}
//		} catch (NullPointerException var2) {
//			var2.printStackTrace();
//		} catch (Exception var3) {
//			var3.printStackTrace();
//		}
//
//	}
//
//	void addToRadar(PARRadarView theRadar) {
//		theRadar.addView(this.radarView);
//		this.radarView.setVisibility(View.VISIBLE);
//		this.addedToRadar = true;
//	}
//
//	void removeFromRadar() {
//		try {
//			if (this.radarView != null && this.radarView.getParent() != null) {
//				((ViewGroup) this.radarView.getParent()).removeView(this.radarView);
//			}
//		} catch (NullPointerException var2) {
//			var2.printStackTrace();
//		} catch (Exception var3) {
//			var3.printStackTrace();
//		}
//
//		this.radarView.setVisibility(View.GONE);
//		this.addedToRadar = false;
//	}
//
//	public boolean isObserved() {
//		return this.observed;
//	}
//
//	public void setObserved(boolean observed) {
//		this.observed = observed;
//	}
//
//	public boolean isHadLocationUpdate() {
//		return this.hadLocationUpdate;
//	}
//}

/**/