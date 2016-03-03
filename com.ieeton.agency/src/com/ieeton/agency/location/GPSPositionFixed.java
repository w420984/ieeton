package com.ieeton.agency.location;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class GPSPositionFixed {

	private Context mContext;
	private LocationManager mLocationManager = null;
	private Object mLock = new Object();
	private boolean mLocationIsTerminated = true;
	private Handler mGpsLocationHandler;
	private Looper mGpsLocationLooper;
	
	private LocationListener mLocationListenerGps = null;
	private GpsStatus.Listener mGpsStatusListener = null;
	private Location mLocationLastGps = new Location("gps");
	private float mFloatGpsSignal = 0f;
	
	public GPSPositionFixed(Context context){
		mContext = context;
		mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public boolean isTerminated(){
		return mLocationIsTerminated;
	}
	
	
	public void startPositionFixed(){
		synchronized (mLock) {
			if(!mLocationIsTerminated){
				return;
			}
			mLocationIsTerminated = false;
		}
		new Thread(){
			@Override
			public void run() {
				Looper.prepare();
				mGpsLocationHandler = new Handler(){
					@Override
					public void handleMessage(Message msg) {
						switch(msg.what){
						case LocationConstants.LOCATION_MSG_START: 
							handleWeiboLocationStart();
							break;
						case LocationConstants.LOCATINO_MSG_TIMEOUT:
							handleGpsLocationFinish(mLocationLastGps);
							break;
						case LocationConstants.LOCATINO_MSG_CANCEL:
							terminate();
							break;
						}
						super.handleMessage(msg);
					}
				};
				mGpsLocationLooper = Looper.myLooper();
				mGpsLocationHandler.sendEmptyMessage(LocationConstants.LOCATION_MSG_START);
				Looper.loop();
			}
		}.start();
	}
	
	//开始定位
	private void handleWeiboLocationStart(){
		initLocationListeners();
	}
	
	public void cancelPositionFixed(){
		synchronized (mLock) {
			if(mLocationIsTerminated){
				return;
			}
			mLocationIsTerminated = true;
		}
		if(mGpsLocationHandler != null){
			mGpsLocationHandler.sendEmptyMessage(LocationConstants.LOCATINO_MSG_CANCEL);
		}
	}
	
	//初始化定位需要的监听器
	private void initLocationListeners(){
		List<String> providers = mLocationManager.getProviders(true);
		if((providers != null && providers.size() != 0)
				&& providers.contains(LocationManager.GPS_PROVIDER)
				&& mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			initGpsLocationListener();
			initGpsStatusListener();
			//1分钟内定位不到就取消定位
			mGpsLocationHandler.sendEmptyMessageDelayed(
					LocationConstants.LOCATINO_MSG_TIMEOUT, LocationConstants.TIMEOUT_OF_GPS_LOCATION);
		}else{
			mGpsLocationHandler.sendEmptyMessage(LocationConstants.LOCATINO_MSG_CANCEL);
		}
	}
	
	/**
	 * 初始化GPS LOCATION监听器
	 * 
	 * @param 无
	 * @return 无
	 */
	private void initGpsLocationListener() {
		removeGpsLocationListener();
		mLocationListenerGps = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				mLocationLastGps = new Location(location);
				handleGpsLocationFinish(mLocationLastGps);
			}

			@Override
			public void onProviderDisabled(String stringProvider) {
				mLocationLastGps.reset();
				handleGpsLocationFinish(mLocationLastGps);
			}

			@Override
			public void onProviderEnabled(String stringProvider) {
			}

			@Override
			public void onStatusChanged(String stringProvider, int intStatus,
					Bundle bundleExtras) {
				switch (intStatus) {
				case LocationProvider.OUT_OF_SERVICE:
					mLocationLastGps.reset();
					break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					mLocationLastGps.reset();
					break;
				default:
					break;
				}
			}
		};
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				LocationConstants.minGpsLocationUpdateInterval * 1000, 0,
				mLocationListenerGps, mGpsLocationLooper);
	}
	
	
	/**
	 * 初始化GPSSTATUS监听器
	 * 
	 * @param 无
	 * @return 无
	 */
	private void initGpsStatusListener() {
		removeGpsStatusListener();
		mGpsStatusListener = new GpsStatus.Listener() {
			@Override
			public void onGpsStatusChanged(int intEvent) {
				handleGpsStatusChanged(intEvent);
			}
		};
		mLocationManager.addGpsStatusListener(mGpsStatusListener);
	}
	
	/**
	 * 处理GPS状态改变
	 * 
	 * @param int
	 * @return 无
	 */
	private void handleGpsStatusChanged(int intEvent) {
		switch (intEvent) {
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
			mFloatGpsSignal = 0l;
			Iterator<GpsSatellite> iterator = mLocationManager.getGpsStatus(null).getSatellites().iterator();
			while (iterator.hasNext()) {
				GpsSatellite gpsSatellite = (GpsSatellite) iterator.next();
				mFloatGpsSignal += gpsSatellite.getSnr();
			}
			break;
		case GpsStatus.GPS_EVENT_STOPPED:
			mFloatGpsSignal = 0l;
			mLocationLastGps.reset();
			handleGpsLocationFinish(mLocationLastGps);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 移除GPS LOCATION监听
	 * 
	 * @param 无
	 * @return 无
	 */
	private void removeGpsLocationListener() {
		try {
			if (mLocationListenerGps != null) {
				mLocationManager.removeUpdates(mLocationListenerGps);
			}
		} catch (Exception exception) {
			
		}
		mLocationListenerGps = null;
	}

	/**
	 * 移除GPS STATUS监听
	 * 
	 * @param 无
	 * @return 无
	 */
	private void removeGpsStatusListener() {
		try {
			if (mGpsStatusListener != null) {
				mLocationManager.removeGpsStatusListener(mGpsStatusListener);
			}
		} catch (Exception exception) {
		}
		mGpsStatusListener = null;
	}
	
	private void handleGpsLocationFinish(Location gpsLocation){
		IeetonLocation weiboLocation = new IeetonLocation();
		weiboLocation.setLongtitude(mLocationLastGps.getLongitude());
		weiboLocation.setLatitude(mLocationLastGps.getLatitude());
		weiboLocation.setOffset(false);
		IeetonLocationManager.getInstance(mContext)
					.locationFinished(LocationConstants.LOCATION_FINISH_GPS,weiboLocation);
	}
	
	public Location getLastGPSLocation(){
		return mLocationLastGps;
	}
	
	//中断定位
	private void terminate(){
		mLocationIsTerminated = true;
		mGpsLocationHandler.removeMessages(LocationConstants.LOCATINO_MSG_TIMEOUT);
		removeGpsLocationListener();
		removeGpsStatusListener();
		if(mGpsLocationLooper != null){
			mGpsLocationLooper.quit();
			mGpsLocationLooper = null;
		}
		mGpsLocationHandler = null;
	}
}
