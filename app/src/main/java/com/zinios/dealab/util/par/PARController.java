package com.zinios.dealab.util.par;

import android.content.Context;
import android.location.Location;
import android.os.Build.VERSION;
import android.util.Log;

import com.zinios.dealab.BuildConfig;
import com.zinios.dealab.ui.fragment.PARFragment;

import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class PARController implements PSKEventListener {
	public static boolean DEBUG = false;
	public static float CLIP_POIS_NEARER_THAN = 5.0F;
	public static float CLIP_POIS_FARER_THAN = 1.0E7F;
	public static PARDataCollector dataCollector;
	private static PARController _sharedPARController = new PARController();
	private static Context _context;
	private static String _apiKey;
	private static boolean _hasValidApiKey = false;
	private static List<PARPoi> _pois;
	private String TAG = "PARController";

	private PARController() {
		_pois = new ArrayList<>();
	}

	public static Context getContext() {
		return _context;
	}

	public static PARController getInstance() {
		return _sharedPARController;
	}

	public static List<PARPoi> getPois() {
		return _pois;
	}

	public void init(Context ctx, String apiKey) {
		_context = ctx;
		this._apiKey = apiKey;
		Log.i(this.TAG, "init()");
		this.setApiKey();
		PARDataCollector dataCollector = new PARDataCollector();
		String osVersion = VERSION.RELEASE;
		String deviceId = PARInstallation.id(ctx);
		try {
			dataCollector.addEntry(new BasicNameValuePair(URLEncoder.encode("entry.355335633", "UTF-8"), URLEncoder.encode(deviceId, "UTF-8")));
			dataCollector.addEntry(new BasicNameValuePair(URLEncoder.encode("entry.990059982", "UTF-8"), URLEncoder.encode(osVersion, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.dataCollector = dataCollector;
		PSKSensorManager.getSharedSensorManager().setEventListener(this);
	}

	private void setApiKey() {
		Log.wtf(this.TAG, "API-Key set to " + _apiKey);
		_hasValidApiKey = this.validateApiKey(_apiKey);
	}

	public boolean hasValidApiKey() {
		return _hasValidApiKey;
	}

	public void addPoi(PARPoi poi) {
		if (_pois.contains(poi)) {
			Log.e(this.TAG, "PARPoi not added (same PARPoi already added to PARController).");
		} else {
			_pois.add(poi);
			poi.updateLocation();
			poi.onAddedToARController();
		}
	}

//    public void addPois(ArrayList<PARPoi> anArray) {
//        Iterator i$ = anArray.iterator();
//
//        while(i$.hasNext()) {
//            PARPoi arPoi = (PARPoi)i$.next();
//            this.addPoi(arPoi);
//        }
//
//    }

	private void removeObject(PARPoi poi) {
		if (!_pois.contains(poi)) {
			Log.e(this.TAG, "PARPoi not removed (not added to PARController).");
		} else {
			poi.onRemovedFromARController();
			_pois.remove(poi);
		}
	}

	private void removeObject(int index) {
		try {
			if (index >= 0 && index < _pois.size()) {
				this.removeObject(_pois.get(index));
			}
		} catch (NullPointerException var3) {
			var3.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException var4) {
			var4.printStackTrace();
		}

	}

	public void clearObjects() {
		try {
			ListIterator iterator = _pois.listIterator();

			while (iterator.hasNext()) {
				PARPoi poi = (PARPoi) iterator.next();
				poi.onRemovedFromARController();
				iterator.remove();
			}
		} catch (NoSuchElementException var4) {
			var4.printStackTrace();
		} catch (UnsupportedOperationException var5) {
			var5.printStackTrace();
		} catch (Exception var6) {
			try {
				while (_pois.size() > 0) {
					this.removeObject(0);
				}
			} catch (Exception var3) {
				var3.printStackTrace();
			}
		}

	}

	public int numberOfObjects() {
		Log.wtf(this.TAG, "_poi.size = " + _pois.size());
		return _pois.size();
	}

	public PARPoi getObject(int index) {
		return index >= 0 && index < _pois.size() ? _pois.get(index) : null;
	}

	@Override
	public void onLocationChangedEv(Location location) {
		location.setAltitude(0.0);
		Iterator i$ = _pois.iterator();

		while (i$.hasNext()) {
			PARPoi poi = (PARPoi) i$.next();
			poi.updateLocation();
		}

		this.sortMarkersByDistance();
		if (PARFragment.getActiveFragment() != null) {
			PARFragment.getActiveFragment().onLocationChangedEv(location);
		}

	}

	@Override
	public void onDeviceOrientationChanged(PSKDeviceOrientation newOrientation) {
		if (PARFragment.getActiveFragment() != null) {
			PARFragment.getActiveFragment().onDeviceOrientationChanged(newOrientation);
		}

	}

	private void sortMarkersByDistance() {
		try {
			Collections.sort(_pois, new Comparator<PARPoi>() {
				public int compare(PARPoi parPoi1, PARPoi parPoi2) {
					return parPoi1.distanceToUser >= parPoi2.distanceToUser ? 1 : (parPoi1.distanceToUser < parPoi2.distanceToUser ? -1 : 0);
				}
			});
		} catch (NullPointerException var2) {
			Log.wtf(this.TAG, "Sort objects failed.");
			var2.printStackTrace();
		} catch (Exception var3) {
			var3.printStackTrace();
		}

	}

	private boolean validateApiKey(String apiKey) {
		if (apiKey.equals("")) {
			Log.wtf(this.TAG, "no API-Key!\nUsing Demo-Version of Framework (Limitations enabled)!\nNot intended for Release!");
			return false;
		} else if (apiKey.equals("Override the setApiKey method in your PARApplication class!")) {
			return false;
		} else {
			try {
				String bundleIdentifier = BuildConfig.APPLICATION_ID;
				String hash = bundleIdentifier.toLowerCase() + "636Ux372^Q?6}CZ7^#/2Vk.;p.6j}s7%a3E3?$m4+{[(6HuW9k#6:q[q494z";
				MessageDigest md = MessageDigest.getInstance("MD5");
				md.reset();
				md.update(hash.getBytes());
				byte[] byteData = md.digest();
				StringBuffer hexString = new StringBuffer();

				for (byte aByteData : byteData) {
					hexString.append(Integer.toString((aByteData & 255) + 256, 16).substring(1));
				}

				if (hexString.toString().trim().equals(apiKey.trim())) {
					Log.wtf(this.TAG, "API-Key valid!");
					return true;
				}
			} catch (NoSuchAlgorithmException var9) {
				var9.printStackTrace();
			} catch (NullPointerException var10) {
				Log.e(this.TAG, "Could not read package name from AndroidManifest.xml\nUsing Demo-Version of Framework (Limitations enabled)!\nNot intended for Release!");
				return false;
			}

			Log.e(this.TAG, "API-Key not valid for BundleID of this App!\nUsing Demo-Version of Framework (Limitations enabled)!\nNot intended for Release!");
			return false;
		}
	}

}
