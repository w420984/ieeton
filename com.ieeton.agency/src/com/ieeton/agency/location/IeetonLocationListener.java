package com.ieeton.agency.location;

import android.os.Handler;

public abstract class IeetonLocationListener {

	private Handler mHandler = new Handler();
	public abstract void onLocationStart();
	public abstract void onLocationFinish(IeetonLocation weiboLocation);
	
	/**package**/ Handler getPostHandler(){
		return mHandler;
	}
}
