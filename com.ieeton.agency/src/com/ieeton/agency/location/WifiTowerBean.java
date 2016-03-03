package com.ieeton.agency.location;


public class WifiTowerBean {

	private String mMacAddress = "";
	private String mSSID = "";
	private String mWifiType = "";
	private int mSignal = LocationConstants.DEFAULT_CGISIGNAL;
	
	public String getMacAddress(){
		return mMacAddress;
	}
	
	public void setMacAddress(String macAddress){
		mMacAddress = macAddress;
	}
	
	public String getSSID(){
		return mSSID;
	}
	
	public void setSSID(String ssid){
		mSSID = ssid;
	}
	
	public String getWifiType(){
		return mWifiType;
	}
	
	public void setWifiType(String wifiType){
		mWifiType = wifiType;
	}
	
	
	public int getSignal(){
		return mSignal;
	}
	
	public void setSignal(int signal){
		mSignal = signal;
	}
	
	
}
