package com.ieeton.user.location;

import android.os.Handler;

public abstract class IeetonLocationListener {

	private Handler mHandler = new Handler();
	public abstract void onLocationStart();
	public abstract void onLocationFinish(IeetonLocation location);
	
	/**package**/ Handler getPostHandler(){
		return mHandler;
	}
}
