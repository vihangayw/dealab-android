package com.zinios.dealab.util.par;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class PARCameraView extends SurfaceView implements Callback {
	private static final String TAG = "PARCameraView";
	private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
	private static final int PREVIEW_SIZE_MAX_WIDTH = 640;
	private int cameraId = 0;
	private Camera camera;
	private SurfaceHolder surfaceHolder;
	private Size bestPreviewSize;
	private float displaySizeW = 16.0F;
	private float displaySizeH = 9.0F;
	private float resolvedAspectRatioW;
	private float resolvedAspectRatioH;
	private float parentWidth;
	private float parentHeight;
	private DisplayMetrics displaymetrics;
	private boolean camReleased;

	public PARCameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.resolvedAspectRatioW = this.displaySizeW;
		this.resolvedAspectRatioH = this.displaySizeH;
		this.displaymetrics = new DisplayMetrics();
	}

	public PARCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.resolvedAspectRatioW = this.displaySizeW;
		this.resolvedAspectRatioH = this.displaySizeH;
		this.displaymetrics = new DisplayMetrics();
	}

	public PARCameraView(Context context) {
		super(context);
		this.resolvedAspectRatioW = this.displaySizeW;
		this.resolvedAspectRatioH = this.displaySizeH;
		this.displaymetrics = new DisplayMetrics();
	}

	public void onCreateView() {
//        int numberOfCameras = Camera.getNumberOfCameras();
//        for (int i = 0; i < numberOfCameras; i++) {
//            CameraInfo info = new CameraInfo();
//            Camera.getCameraInfo(i, info);
//            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
//                cameraId = i;
//                // break;
//            }
//            Log.d("XXX", info.facing + "");
//
//        }
		this.getHolder().addCallback(this);
		Display display = ((WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		try {
			if (VERSION.SDK_INT >= 17) {
				display.getRealMetrics(this.displaymetrics);
			} else {
				display.getMetrics(this.displaymetrics);
			}

			this.parentWidth = (float) this.displaymetrics.widthPixels;
			this.parentHeight = (float) this.displaymetrics.heightPixels;
		} catch (Exception var3) {
			var3.printStackTrace();
			this.parentWidth = 1280.0F;
			this.parentHeight = 1280.0F;
		}

	}

	public Point getViewSize() {
		return new Point((int) this.parentWidth, (int) this.parentHeight);
	}

	public void onResume() {
		try {
			this.camera = Camera.open(this.cameraId);
			this.startCameraPreview();
			camReleased = true;
		} catch (Exception var2) {
			Log.e("PARCameraView", "Can't open camera with id " + this.cameraId, var2);
		}
	}

	public void onPause() {
		try {
			if (this.camera != null) {
				this.stopCameraPreview();
				this.camera.release();
				camReleased = true;
			}
		} catch (Exception var2) {
			var2.printStackTrace();
		}

	}

	private synchronized void startCameraPreview() {
		this.determineDisplayOrientation();
		this.setupCamera();

		try {
			this.camera.setPreviewDisplay(this.surfaceHolder);
			this.camera.startPreview();
//            Log.e("XXXX", this.cameraId + " ");
		} catch (IOException var2) {
			Log.e("PARCameraView", "Can't start camera preview due to IOException", var2);
		} catch (NullPointerException var3) {
			var3.printStackTrace();
		}

	}

	private synchronized void stopCameraPreview() {
		try {
			this.camera.stopPreview();
		} catch (Exception var2) {
			Log.i("PARCameraView", "Exception during stopping camera preview");
		}

	}

	public void determineDisplayOrientation() {
		CameraInfo cameraInfo = new CameraInfo();
		Camera.getCameraInfo(this.cameraId, cameraInfo);
		int rotation = ((WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
			case 0:
				this.resolvedAspectRatioW = this.displaySizeW;
				this.resolvedAspectRatioH = this.displaySizeH;
				this.parentWidth = (float) this.displaymetrics.widthPixels;
				this.parentHeight = (float) this.displaymetrics.heightPixels;
				degrees = 0;
				break;
			case 1:
				this.resolvedAspectRatioW = this.displaySizeH;
				this.resolvedAspectRatioH = this.displaySizeW;
				this.parentWidth = (float) this.displaymetrics.heightPixels;
				this.parentHeight = (float) this.displaymetrics.widthPixels;
				degrees = 90;
				break;
			case 2:
				this.resolvedAspectRatioW = this.displaySizeW;
				this.resolvedAspectRatioH = this.displaySizeH;
				this.parentWidth = (float) this.displaymetrics.widthPixels;
				this.parentHeight = (float) this.displaymetrics.heightPixels;
				degrees = 180;
				break;
			case 3:
				degrees = 270;
		}

		int displayOrientation;
		if (cameraInfo.facing == 1) {
			displayOrientation = (cameraInfo.orientation + degrees) % 360;
			displayOrientation = (360 - displayOrientation) % 360;
		} else {
			displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
		}

		if (this.camera != null && !camReleased) {
			this.camera.setDisplayOrientation(displayOrientation);
		}

	}

	public void setupCamera() {
		if (this.camera != null) {
			Parameters parameters = this.camera.getParameters();
			this.bestPreviewSize = this.determineBestPreviewSize(parameters);
			Size bestPictureSize = this.determineBestPictureSize(parameters);
			parameters.setPreviewSize(this.bestPreviewSize.width, this.bestPreviewSize.height);
			parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);
			this.camera.setParameters(parameters);
		}
	}

	private Size determineBestPreviewSize(Parameters parameters) {
		List<Size> sizes = parameters.getSupportedPreviewSizes();
		return this.determineBestSize(sizes, 640);
	}

	private Size determineBestPictureSize(Parameters parameters) {
		List<Size> sizes = parameters.getSupportedPictureSizes();
		return this.determineBestSize(sizes, 1280);
	}

	protected Size determineBestSize(List<Size> sizes, int widthThreshold) {
		Size bestSize = null;
		Iterator i$ = sizes.iterator();

		while (i$.hasNext()) {
			Size currentSize = (Size) i$.next();
			boolean isDesiredRatio = (float) currentSize.width / this.resolvedAspectRatioW == (float) currentSize.height / this.resolvedAspectRatioH;
			boolean isBetterSize = bestSize == null || currentSize.width > bestSize.width;
			boolean isInBounds = currentSize.width <= 1280;
			if (isDesiredRatio && isInBounds && isBetterSize) {
				bestSize = currentSize;
			}
		}

		if (bestSize == null) {
			return sizes.get(0);
		} else {
			return bestSize;
		}
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        float originalWidth = (float)MeasureSpec.getSize((int)this.parentWidth);
//        float originalHeight = (float)MeasureSpec.getSize((int)this.parentHeight);
//        float width = originalWidth;
//        float height = originalHeight;
//        float parentWidth = (float)((ViewGroup)this.getParent()).getMeasuredWidth();
//        float parentHeight = (float)((ViewGroup)this.getParent()).getMeasuredHeight();
//        if(originalWidth > originalHeight * this.getResolvedAspectRatio()) {
//            width = originalHeight / this.getResolvedAspectRatio() + 0.5F;
//        } else {
//            height = originalWidth * this.getResolvedAspectRatio() + 0.5F;
//        }
//
//        this.setX((parentWidth - width) * 0.5F);
//        this.setY((parentHeight - height) * 0.5F);
//        this.setMeasuredDimension((int)width, (int)height);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		this.surfaceHolder = holder;

		try {
			this.startCameraPreview();
		} catch (Exception var3) {
			var3.printStackTrace();
		}

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	private float getResolvedAspectRatio() {
		return this.resolvedAspectRatioW / this.resolvedAspectRatioH;
	}
}