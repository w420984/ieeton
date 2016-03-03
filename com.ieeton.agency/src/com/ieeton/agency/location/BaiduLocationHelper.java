package com.ieeton.agency.location;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.ieeton.agency.activity.SplashActivity;

public class BaiduLocationHelper {
	private static LocationClient mLocationClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private IeetonLocationListener mIeetonLocationListener;
	private Context mContext;
	private static BaiduLocationHelper mBaiduLocationHelper = null;
	
	public static void startRequestLocation(Context context, IeetonLocationListener listener){
		BaiduLocationHelper mBaiduLocationHelper = BaiduLocationHelper.getInstance(context);
		mBaiduLocationHelper.startRequestLocation(listener);
	}

	
	public static synchronized BaiduLocationHelper getInstance(Context context){
		if(mBaiduLocationHelper == null){
			mBaiduLocationHelper = new BaiduLocationHelper(context);
		}
		return mBaiduLocationHelper;
	}

	private BaiduLocationHelper(Context context){
		mContext = context;
		mLocationClient = new LocationClient(mContext);
		setLocationOption();
		mLocationClient.registerLocationListener(myListener);
	}

	public synchronized void startRequestLocation(IeetonLocationListener listener){
		if(listener == null){
			return;
		}
		Log.v("sereinli", "startRequestLocation");
		if(mLocationClient.isStarted()){
			mLocationClient.stop();
		}
		mIeetonLocationListener = listener;
		listener.onLocationStart();

		mLocationClient.start();
		
		mLocationClient.requestLocation();
	}
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); 	// 设置是否打开gps，使用gps前提是用户硬件打开gps。默认是不打开gps的
		option.setCoorType("bd09ll"); 	// 设置返回值的坐标类型
		option.setServiceName("com.ieeton.agency");
		option.setAddrType("all");	// 设置是否要返回地址信息，默认为无地址信息
		option.setScanSpan(60000);	// 设置定时定位的时间间隔。单位ms
		option.setPriority(LocationClientOption.NetWorkFirst); 	// 设置网络优先
//		option.setPoiNumber(10); 	// 设置最多可返回的POI个数，默认值为3
//		option.setPoiExtraInfo(false); 	// 设置是否需要POI的电话地址等详细信息：
//		option.setPoiDistance(500);		//设置查询范围，默认值为500，即以当前定位位置为中心的半径大小
//		option.disableCache(true); 		// 设置是否启用缓存定位.true表示禁用缓存定位，false表示启用缓存定位。
		
		mLocationClient.setLocOption(option);
	}
	
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.v("sereinli", "onReceiveLocation,"+location);
			if (location == null)
			{
				Log.v("sereinli", "onReceiveLocation,null");
				mIeetonLocationListener.onLocationFinish(null);
				return ;
			}
//			StringBuffer sb = new StringBuffer(256);
//			sb.append("time : ");
//			sb.append(location.getTime());
//			sb.append("\nerror code : ");
//			sb.append(location.getLocType());
//			sb.append("\nlatitude : ");
//			sb.append(location.getLatitude());
//			sb.append("\nlontitude : ");
//			sb.append(location.getLongitude());
//			sb.append("\nradius : ");
//			sb.append(location.getRadius());
//			if (location.getLocType() == BDLocation.TypeGpsLocation){
//				sb.append("\nspeed : ");
//				sb.append(location.getSpeed());
//				sb.append("\nsatellite : ");
//				sb.append(location.getSatelliteNumber());
//			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
//				sb.append("\n省：");
//				sb.append(location.getProvince());
//				sb.append("\n市：");
//				sb.append(location.getCity());
//				sb.append("\n区/县：");
//				sb.append(location.getDistrict());
//				sb.append("\naddr : ");
//				sb.append(location.getAddrStr());
//			}
//			sb.append("\nsdk version : ");
//			sb.append(mLocationClient.getVersion());
//			sb.append("\nisCellChangeFlag : ");
//			sb.append(location.isCellChangeFlag());
//			
//			Log.v("sereinli", "sb:"+sb.toString());
			
			IeetonLocation ieeton_location = new IeetonLocation();
			ieeton_location.setLatitude(location.getLatitude());
			ieeton_location.setLongtitude(location.getLongitude());
			ieeton_location.setRadius(location.getRadius());
			ieeton_location.setProvince(location.getProvince());
			ieeton_location.setCity(location.getCity());
			ieeton_location.setDistrict(location.getDistrict());
			mIeetonLocationListener.onLocationFinish(ieeton_location);
		}
		
		@Override
		public void onReceivePoi(BDLocation poiLocation) {
			Log.v("sereinli", "onReceivePoi,"+poiLocation);
			if (poiLocation == null){
				return ; 
			}

			StringBuffer sb = new StringBuffer(256);
			sb.append("Poi time : ");
			sb.append(poiLocation.getTime());
			sb.append("\nerror code : "); 
			sb.append(poiLocation.getLocType());
			sb.append("\nlatitude : ");
			sb.append(poiLocation.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(poiLocation.getLongitude());
			sb.append("\nradius : ");
			sb.append(poiLocation.getRadius());
			if (poiLocation.getLocType() == BDLocation.TypeNetWorkLocation){
				sb.append("\naddr : ");
				sb.append(poiLocation.getAddrStr());
			} 
			if(poiLocation.hasPoi()){
				sb.append("\nPoi:");
				sb.append(poiLocation.getPoi());
			}else{				
				sb.append("noPoi information");
			}
			
			Log.v("sereinli", "sb:"+sb.toString());
		}
	}
	
	public static void StopBaiduLocationService(){
		if(mBaiduLocationHelper != null){
			mBaiduLocationHelper = null;
		}
		if(mLocationClient != null && mLocationClient.isStarted()){
			mLocationClient.stop();
			mLocationClient = null;
		}

	}
}
