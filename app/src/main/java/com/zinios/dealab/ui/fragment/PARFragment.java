package com.zinios.dealab.ui.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Location;
import android.opengl.Matrix;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.zinios.dealab.R;
import com.zinios.dealab.util.par.PARCameraView;
import com.zinios.dealab.util.par.PARController;
import com.zinios.dealab.util.par.PARPoi;
import com.zinios.dealab.util.par.PARProgressBar;
import com.zinios.dealab.util.par.PARRadarView;
import com.zinios.dealab.util.par.PARView;
import com.zinios.dealab.util.par.POI;
import com.zinios.dealab.util.par.PSKDeviceAttitude;
import com.zinios.dealab.util.par.PSKDeviceOrientation;
import com.zinios.dealab.util.par.PSKDeviceProperties;
import com.zinios.dealab.util.par.PSKEventListener;
import com.zinios.dealab.util.par.PSKMath;
import com.zinios.dealab.util.par.PSKSensorManager;

import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.ListIterator;


public class PARFragment extends Fragment implements PSKEventListener {
	protected static PARFragment activeFragment;
	protected PARCameraView _cameraView;
	protected PARView _arView;
	protected int viewLayoutId;
	protected boolean isInAirplaneMode = false;
	protected boolean hasAirplaneModeDialog = false;
	protected AlertDialog airplaneModeDialog = null;
	protected boolean isGPSEnabled = true;
	protected boolean hasGPSDialog = false;
	protected AlertDialog gpsDialog = null;
	private RelativeLayout _mainView;
	private PARRadarView _arRadarView;
	private String TAG = "PARFragment";
	private PSKDeviceAttitude _deviceAttitude;
	private PSKSensorManager _sensorManager;
	private Runnable renderRunnable;
	private Handler renderLoopHandler = null;
	private float[] _perspectiveMatrix;
	private float[] _perspectiveCameraMatrix = new float[16];
	private boolean isDataTrackingExecuted = false;
	private boolean _hasProjectionMatrix = false;
	private Point _screenMargin = null;
	private int _screenOrientation;
	private int _screenOrientationPrevious;
	private float _screenOrientationOffsetAngle;
	private Point _cameraSize = new Point(0, 0);
	private Point _screenSize = new Point(0, 0);
	private PARProgressBar progressBar;
	private boolean arViewShouldBeVisible;
	private boolean orientationHidesARView;
	private boolean hadLocationUpdate;
	private int airplaneModeCounter = 0;
	private List<POI> spotList;
	private ARFragment.LocationChange locationChange;

	public PARFragment() {
	}

	public static boolean isAirplaneModeOn(Context context) {
		return VERSION.SDK_INT < 17 ? System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0 : Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
	}

	public static PARFragment getActiveFragment() {
		return activeFragment;
	}

	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		this._deviceAttitude = PSKDeviceAttitude.sharedDeviceAttitude();
		this._sensorManager = PSKSensorManager.getSharedSensorManager();
		PARController.getInstance().init(getActivity(), getActivity().getResources().getString(R.string.google_maps_key));
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, List<POI> spotList, LocationChange locationChange) {
		View view = inflater.inflate(this.viewLayoutId, container, false);
		this._mainView = view.findViewWithTag("arMainLayout");
		this._cameraView = this._mainView.findViewWithTag("arCameraView");
		this.locationChange = locationChange;
		this._cameraView.onCreateView();
		this._arView = this._mainView.findViewWithTag("arContentView");
		this._arView.setVisibility(View.GONE);
		this.setARViewShouldBeVisible(true);
		this.spotList = spotList;
		this.orientationHidesARView = false;

		this.progressBar = this.createProgressBar();
		this._mainView.addView(this.getProgressBar().getMainLayout());
		if (PARController.DEBUG) {
			this._arView.setBackgroundColor(1073807104);
		} else {
			this._arView.setBackgroundColor(0);
		}

		this._arRadarView = this._mainView.findViewWithTag("arRadarView");
		return view;
	}

	public void onResume() {
		super.onResume();
		if (this._arRadarView != null) {
			this._arRadarView.showRadarInMode(1, this);
		}

		activeFragment = this;
		if (!PSKDeviceProperties.sharedDeviceProperties().isARSupported()) {
			this.onARNotSupportedRaised();
		} else {
			this._arView.setVisibility(View.VISIBLE);
			this._sensorManager.startListening();
			this.progressBar.showWithText(getString(R.string.waiting_for_location));
			this._cameraView.onResume();
			this.startRendering();
		}
	}

	public void onPause() {
		super.onPause();
		this.stopRendering();
		this.sendARDataAndSystemSpecs();
		this._cameraView.onPause();
		if (this.getRadarView() != null) {
			this.getRadarView().stop();
		}

		this._sensorManager.stopListening();
		this.hadLocationUpdate = false;
		if (activeFragment == this) {
			activeFragment = null;
		}

	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				PARFragment.this._cameraView.determineDisplayOrientation();
				PARFragment.this._arRadarView.requestLayout();
				PARFragment.this._cameraView.requestLayout();
				PARFragment.this._mainView.requestLayout();
			}
		}, 200L);
	}

	private void onARNotSupportedRaised() {
		Builder builder = new Builder(this.getActivity());
		//   PSKDeviceProperties props = PSKDeviceProperties.sharedDeviceProperties();
		String errorMsg = getString(R.string.ar_not_supported) + "\n";
//        boolean more = false;
//        if (props.hasAccelerometer()) {
//            more = true;
//            errorMsg = errorMsg + "Accelerometer";
//        }
//
//        if (props.hasCompass()) {
//            if (more) errorMsg = errorMsg + ", ";
//            more = true;
//            errorMsg = errorMsg + "Compass";
//        }
//
//        if (props.hasGyroscope()) {
//            if (more) errorMsg = errorMsg + ", ";
//            more = true;
//            errorMsg = errorMsg + "Gyroscope";
//        }
//
//        if (props.hasGravitySensor()) {
//            if (more) errorMsg = errorMsg + ", ";
//            more = true;
//            errorMsg = errorMsg + "GravitySensor";
//        }

		/* errorMsg = errorMsg + " " + getString(R.string.not_available_on_device)*/

		builder.setTitle(R.string.ar_not_support_title);
		builder.setMessage(errorMsg);
		builder.setNeutralButton("Ok", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				PARFragment.this.progressBar.hide();
				PARFragment.this._arRadarView.hideRadar();
				if (getActivity() != null)
					getActivity().finish();
			}
		});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				PARFragment.this.progressBar.hide();
				PARFragment.this._arRadarView.hideRadar();
				if (getActivity() != null)
					getActivity().finish();
			}
		});
		builder.show();
	}

	private void startRendering() {
		if (this.renderLoopHandler == null) {
			this.renderLoopHandler = new Handler();
			this.renderRunnable = new Runnable() {
				public void run() {
					PARFragment.this.updateView();
//                    Log.e("XXX", PARFragment.this.RENDER_INTERVAL + "");
					PARFragment.this.renderLoopHandler.postDelayed(this, 5);
				}
			};
			this.renderLoopHandler.post(this.renderRunnable);
		}

	}

	private void stopRendering() {
		if (this.renderLoopHandler != null) {
			this.renderLoopHandler.removeCallbacks(this.renderRunnable);
			this.renderRunnable = null;
			this.renderLoopHandler = null;
		}

	}

	public PARProgressBar createProgressBar() {
		return new PARProgressBar(this.getActivity(), (AttributeSet) null, 16842871);
	}

	private void sendARDataAndSystemSpecs() {
		if (!this.isDataTrackingExecuted) {
			int degreesLandscape = this._deviceAttitude.getCurrentSurfaceRotation();
			float headingAccuracyMin = PSKSensorManager.getSharedSensorManager().getHeadingAccuracyMin();
			float headingAccuracyMax = PSKSensorManager.getSharedSensorManager().getHeadingAccuracyMax();
			try {
				PARController.dataCollector.addEntry(new BasicNameValuePair(URLEncoder.encode("entry.1937168601", "UTF-8"), Integer.toString(degreesLandscape)));
				PARController.dataCollector.addEntry(new BasicNameValuePair(URLEncoder.encode("entry.2001845173", "UTF-8"), Float.toString(headingAccuracyMax)));
				PARController.dataCollector.addEntry(new BasicNameValuePair(URLEncoder.encode("entry.1022928538", "UTF-8"), Float.toString(headingAccuracyMin)));
				PARController.dataCollector.execute(new Void[0]);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			this.isDataTrackingExecuted = true;
		}

	}

	private void updateView() {
		++this.airplaneModeCounter;
		if (this.airplaneModeCounter > 30) {
			this.airplaneModeCounter = 0;
			this.checkForAirplaneMode();
			this.checkForGPSDisabled();
		}

		if (!this.isInAirplaneMode) {
			if (this.getCurrentARViewVisbility() != 0) {
				if (this._arView.getVisibility() != View.GONE) {
					this._arView.setVisibility(View.GONE);
				}

			} else {
				if (this._arView.getVisibility() != View.VISIBLE) {
					this._arView.setVisibility(View.VISIBLE);
					this._arView.requestLayout();
				}

				this._cameraSize = this._cameraView.getViewSize();
				if (!this._hasProjectionMatrix) {
					Point viewPort = new Point();
					if (this._cameraSize.x > this._cameraSize.y) {
						viewPort.set(this._cameraSize.x, this._cameraSize.y);
					} else {
						viewPort.set(this._cameraSize.y, this._cameraSize.x);
					}

					double fov = PSKDeviceProperties.sharedDeviceProperties().getBackFacingCameraFieldOfView()[0];
					fov *= 0.017453292519943295D;
					this._perspectiveMatrix = PSKMath.PSKMatrixCreateProjection(fov, (float) viewPort.x / (float) viewPort.y, 0.25F, 10000.0F);
					this._hasProjectionMatrix = true;
				}

				this._screenOrientation = this._deviceAttitude.getCurrentSurfaceRotation();
				if (this._screenMargin == null || this._screenOrientation != this._screenOrientationPrevious) {
					this._screenOrientationOffsetAngle = -PSKMath.deltaAngle(90.0F * (float) this._screenOrientation, 0.0F);
					this._cameraView.determineDisplayOrientation();
					this._cameraSize = this._cameraView.getViewSize();
					int maxScreenSize = Math.max(this._cameraSize.x, this._cameraSize.y);
					int margin = maxScreenSize - Math.min(this._cameraSize.x, this._cameraSize.y);
					if (this._cameraSize.x <= this._cameraSize.y) {
						this._screenMargin = new Point(0, margin);
					} else {
						this._screenMargin = new Point(margin, 0);
					}

					LayoutParams arViewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
					arViewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
					arViewLayoutParams.leftMargin = -this._screenMargin.x;
					arViewLayoutParams.rightMargin = -this._screenMargin.x;
					arViewLayoutParams.topMargin = -this._screenMargin.y;
					arViewLayoutParams.bottomMargin = -this._screenMargin.y;
					this._arView.setScreenSize(maxScreenSize);
					this._arView.setLayoutParams(arViewLayoutParams);
					this._arView.requestLayout();
					this._screenOrientationPrevious = this._screenOrientation;
					this._hasProjectionMatrix = false;
					Log.i(this.TAG, "update AR View: " + maxScreenSize);
				}

				if (this._cameraSize.x < 1 || this._cameraSize.y < 1) {
					this._screenMargin = null;
				}

				float orientationRoll = this._deviceAttitude.getOrientationRoll();
				float orientationWithOffset = orientationRoll + this._screenOrientationOffsetAngle;
				if (Math.abs(this._arView.getRotation() - orientationWithOffset) > 1.0F) {
					//// TODO: 12/30/17 {VIHANGA} Add this line of u want to rotate the POI along with the display // this._arView.setRotation(-orientationWithOffset);
					PARPoi.setViewRotation(this._screenOrientationOffsetAngle);
				}

				this._screenSize.x = this._arView.getWidth();
				this._screenSize.y = this._arView.getHeight();
				if (this._hasProjectionMatrix) {
					this.drawLabels();
				}


			}
		}
	}

	protected void checkForAirplaneMode() {
		boolean newAirplaneMode = isAirplaneModeOn(this.getActivity());
		if (newAirplaneMode != this.isInAirplaneMode) {
			this.isInAirplaneMode = newAirplaneMode;
			this.onAirplaneModeDetected(newAirplaneMode);
		}

	}

	public void onAirplaneModeDetected(boolean airplaneMode) {
		if (airplaneMode) {
			if (!this.hasAirplaneModeDialog) {
				Builder builder = new Builder(this.getActivity());
				builder.setTitle("Airplane mode on");
				builder.setMessage("Disable airplane mode to use AR");
				builder.setPositiveButton("Ok", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						PARFragment.this.airplaneModeDialog = null;
						PARFragment.this.hasAirplaneModeDialog = false;
						PARFragment.this.getActivity().finish();
					}
				});
				this.airplaneModeDialog = builder.show();
			}

			this.hasAirplaneModeDialog = true;
		} else if (this.airplaneModeDialog != null) {
			this.airplaneModeDialog.hide();
			this.airplaneModeDialog = null;
			this.hasAirplaneModeDialog = false;
		}

	}

	protected void checkForGPSDisabled() {
		boolean newGPSMode = this._sensorManager.isGPSEnabled();
		if (newGPSMode != this.isGPSEnabled) {
			this.isGPSEnabled = newGPSMode;
			this.onGPSDisabled(newGPSMode);
		}

	}

	public void onGPSDisabled(boolean gpsEnabled) {
		if (!gpsEnabled) {
			if (!this.hasGPSDialog) {
				Builder builder = new Builder(this.getActivity());
				builder.setTitle(R.string.gps_disabled_ar);
				builder.setMessage(R.string.enable_gps_ar);
				builder.setPositiveButton("Ok", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						PARFragment.this.gpsDialog = null;
						PARFragment.this.hasGPSDialog = false;
						PARFragment.this.getActivity().finish();
					}
				});
				this.gpsDialog = builder.show();
			}

			this.hasGPSDialog = true;
		} else if (this.gpsDialog != null) {
			this.gpsDialog.hide();
			this.gpsDialog = null;
			this.hasGPSDialog = false;
		}

	}


	protected void drawLabels() {
		Matrix.multiplyMM(this._perspectiveCameraMatrix, 0, this._perspectiveMatrix, 0, this._deviceAttitude.getRotationVectorAttitudeMatrix(), 0);
		ListIterator it = PARController.getPois().listIterator();

		while (it.hasNext()) {
			PARPoi arPoi = (PARPoi) it.next();
			if (!arPoi.isClippedByDistance()) {
				arPoi.renderInView(this);
			}
		}

	}

	public PARCameraView getCameraView() {
		return this._cameraView;
	}

	public float[] getPerspectiveCameraMatrix() {
		return this._perspectiveCameraMatrix;
	}

	public int getScreenMarginX() {
		return this._screenMargin.x;
	}

	public int getScreenMarginY() {
		return this._screenMargin.y;
	}

	public Point getScreenSize() {
		return this._screenSize;
	}

	public PARRadarView getRadarView() {
		return this._arRadarView;
	}

	public PARView getARView() {
		return this._arView;
	}

	@Override
	public void onLocationChangedEv(Location location) {
		this.progressBar.hide();
		this.hadLocationUpdate = true;
		if (locationChange != null)
			locationChange.onLocationChange(location);
	}

	@Override
	public void onDeviceOrientationChanged(PSKDeviceOrientation newOrientation) {
		this.orientationHidesARView = newOrientation == PSKDeviceOrientation.FaceUp || newOrientation == PSKDeviceOrientation.FaceDown;
		this.updateRadarOnOrientationChange(newOrientation);
	}

	public void updateRadarOnOrientationChange(PSKDeviceOrientation newOrientation) {
//        if (this._arRadarView != null) {
//            if (newOrientation == PSKDeviceOrientation.FaceUp) {
//                if (this._arRadarView.radarMode != 2) {
//                    this.getRadarView().setRadarToThumbnail();
//                    this._mainView.requestLayout();
//                }
//            } else if (this._arRadarView.radarMode != 1) {
//            this.getRadarView().setRadarToThumbnail();
//            this._mainView.requestLayout();
//            }
//        }
	}

	public void setARViewShouldBeVisible(boolean arViewShouldBeVisible) {
		this.arViewShouldBeVisible = arViewShouldBeVisible;
	}

	public int getCurrentARViewVisbility() {
		return !this.hadLocationUpdate ? View.GONE : (this.orientationHidesARView ? View.GONE : (!this.arViewShouldBeVisible ? View.GONE : View.VISIBLE));
	}

	public PARProgressBar getProgressBar() {
		return this.progressBar;
	}

	public RelativeLayout get_mainView() {
		return _mainView;
	}

	public interface LocationChange {
		void onLocationChange(Location location);
	}
}
