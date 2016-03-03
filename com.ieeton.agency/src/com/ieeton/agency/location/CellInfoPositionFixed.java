package com.ieeton.agency.location;

import android.content.Context;

import com.ieeton.agency.location.CellInfoMonitor.CellInfoMonitorListener;

public class CellInfoPositionFixed {
	
	private Context mContext;
	private int mLocationState = LocationConstants.LOCATION_STATE_TERMINATE;
	
	private CellInfoMonitor mCellInfoMonitor;
	private RequestDataWrapper mRequestDataWrapper;
	
	public CellInfoPositionFixed(Context context){
		mContext = context.getApplicationContext();
	}
	
	public synchronized void startPositionFixed(){
		if(mLocationState == LocationConstants.LOCATION_STATE_RUNNING){
			return;
		}
		mLocationState = LocationConstants.LOCATION_STATE_RUNNING;
		if(mCellInfoMonitor == null){
			mCellInfoMonitor = new CellInfoMonitor(mContext, mCellInfoMonitorListener);
		}
		mCellInfoMonitor.startMonitor();
	}
	
	//取消定位(state: --->terminated)
	public synchronized void cancelPositionFixed(){
		if(mLocationState == LocationConstants.LOCATION_STATE_TERMINATE){
			return;
		}
		mLocationState = LocationConstants.LOCATION_STATE_TERMINATE;
		mCellInfoMonitor.stopMonitor();
		mRequestDataWrapper = null;
	}
	
	public synchronized boolean isTerminated(){
		return mLocationState == LocationConstants.LOCATION_STATE_TERMINATE;
	}
	
	public RequestDataWrapper getNetLocationRequestData(){
		return mRequestDataWrapper;
	}
	
	private CellInfoMonitorListener mCellInfoMonitorListener = new CellInfoMonitorListener(){
		@Override
		public void onMonitorFinished(RequestDataWrapper requestDataWrapper) {
			mRequestDataWrapper = requestDataWrapper;
			startRequestLoation();
		}
	};
	
	//通过基站信息请求位置信息，先取缓存，再请求网络
	private void startRequestLoation(){
		new Thread(){
			@Override
			public void run() {
				// 查询位置缓存，如果存在缓存，则无需发起网络请求
				final String stringKey = mCellInfoMonitor.getLocationRequestKey();
				IeetonLocation weiboLocation = LocationCache.getInstance().getCache(stringKey);
				if (weiboLocation == null) {
					ILocationRequest locationRequest = new LocationRequestFromSina(mContext);
					weiboLocation = locationRequest.requestWeiboLocation(mRequestDataWrapper);
					LocationCache.getInstance().addCache(stringKey, weiboLocation);
				}
				IeetonLocationManager.getInstance(mContext).locationFinished(LocationConstants.LOCATION_FINISH_CELL,weiboLocation);
			}
		}.start();
	}
}
