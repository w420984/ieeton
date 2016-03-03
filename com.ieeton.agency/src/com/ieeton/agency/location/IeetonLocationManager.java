package com.ieeton.agency.location;

import java.util.ArrayList;
import android.content.Context;

public class IeetonLocationManager {

	private static IeetonLocationManager LOCATION_MANAGER = null;
	private Context mContext = null;
	
	private CellInfoPositionFixed mCellPositionFixed;
	private GPSPositionFixed mGPSPositionFixed;
	private ArrayList<IeetonLocationListener> mLocationListeners = new ArrayList<IeetonLocationListener>();
	
	public static synchronized IeetonLocationManager getInstance(Context context){
		if(LOCATION_MANAGER == null){
			LOCATION_MANAGER = new IeetonLocationManager(context);
		}
		return LOCATION_MANAGER;
	}
	
	private IeetonLocationManager(Context context){
		mContext = context.getApplicationContext();
		mCellPositionFixed = new CellInfoPositionFixed(mContext);
		mGPSPositionFixed = new GPSPositionFixed(mContext);
	}
	

	public synchronized void startRequestLocation(IeetonLocationListener listener){
		if(listener == null){
			return;
		}
		if(!mLocationListeners.contains(listener)){
			mLocationListeners.add(listener);
			listener.onLocationStart();
		}
		mCellPositionFixed.startPositionFixed();
		mGPSPositionFixed.startPositionFixed();
	}
	
	public synchronized void locationFinished(final int from, final IeetonLocation weiboLocation){
		if(from == LocationConstants.LOCATION_FINISH_CELL){
			if(mGPSPositionFixed.isTerminated()){
				notifyListeners(weiboLocation);
			}else{
				if(isLocationValid(weiboLocation)){
					notifyListeners(weiboLocation);
				}
				mGpsCollectDataWrapper = mCellPositionFixed.getNetLocationRequestData();
			}
			mCellPositionFixed.cancelPositionFixed();
		}else if(from == LocationConstants.LOCATION_FINISH_GPS){
			if(mCellPositionFixed.isTerminated()){
				notifyListeners(weiboLocation);
			}else{
				if(isLocationValid(weiboLocation)){
					mGpsCollectDataWrapper = mCellPositionFixed.getNetLocationRequestData();
					notifyListeners(weiboLocation);
				}
			}
			if(isLocationValid(weiboLocation) && mGpsCollectDataWrapper != null){
				mGpsCollectDataWrapper.setGPSLocation(mGPSPositionFixed.getLastGPSLocation());
				uploadGPSDataCollect(mGpsCollectDataWrapper);
			}
			mGPSPositionFixed.cancelPositionFixed();
			mGpsCollectDataWrapper = null;
		}
	}
	
	public synchronized void cancelRequestLocation(IeetonLocationListener listener){
		mLocationListeners.remove(listener);
		if(mLocationListeners.size() == 0){
			mCellPositionFixed.cancelPositionFixed();
			mGPSPositionFixed.cancelPositionFixed();
		}
	}
	
	private void notifyListeners(final IeetonLocation weiboLocation){
		for(final IeetonLocationListener listener : mLocationListeners){
			listener.getPostHandler().post(new Runnable() {
				@Override
				public void run() {
					listener.onLocationFinish(weiboLocation);
				}
			});
		}
		mLocationListeners.clear();
	}
	
	private boolean isLocationValid(IeetonLocation weiboLocation){
		return weiboLocation != null && weiboLocation.isUseful();
	}
	
	private RequestDataWrapper mGpsCollectDataWrapper;
	//gps数据采集
	private void uploadGPSDataCollect(RequestDataWrapper gpsCollectDataWrapper){
		final RequestDataWrapper collectDataWrapper = gpsCollectDataWrapper;
		new Thread(){
			@Override
			public void run() {
				ILocationRequest locationRequest = new LocationRequestFromSina(mContext);
				locationRequest.requestWeiboLocation(collectDataWrapper);
			}
		}.start();
	}
}
