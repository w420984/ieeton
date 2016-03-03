package com.ieeton.user.location;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

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
		setLocationOption(0);//60000
		mLocationClient.registerLocationListener(myListener);
	}

	public synchronized void startRequestLocation(IeetonLocationListener listener){
		if(listener == null){
			return;
		}
//		Log.v("sereinli", "startRequestLocation");
		if(mLocationClient.isStarted()){
			mLocationClient.stop();
		}
		mIeetonLocationListener = listener;
		listener.onLocationStart();

		mLocationClient.start();
		
		mLocationClient.requestLocation();
	}
	
	public static void setLocationOption(int timesec) {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，
        option.setScanSpan(timesec);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
//        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
//        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
//        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        mLocationClient.setLocOption(option);
	}
	
	public static void resetLocationOption(int timesec) {
		if(mLocationClient.isStarted()){
			mLocationClient.stop();
		}
		setLocationOption(timesec);
		mLocationClient.start();
		
		mLocationClient.requestLocation();
	}
	
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
//			Log.v("sereinli", "onReceiveLocation,"+location);
			if (location == null)
			{
//				Log.v("sereinli", "onReceiveLocation,null");
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
		
	}
	
	public static void ExitBaiduLocationService(){
		if(mBaiduLocationHelper != null){
			mBaiduLocationHelper = null;
		}
		if(mLocationClient != null && mLocationClient.isStarted()){
			mLocationClient.stop();
			mLocationClient = null;
		}

	}
	
	public static void StopBaiduLocation(){
		if(mLocationClient != null && mLocationClient.isStarted()){
			mLocationClient.stop();
		}
	}
}
