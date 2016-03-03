package com.ieeton.agency.location;

import java.util.ArrayList;

import android.location.Location;

public class RequestDataWrapper {

	private Location mGPSLocation = null;
	private ArrayList<WifiTowerBean> mListWifiTowerBean = new ArrayList<WifiTowerBean>();
	private ArrayList<GSMCellBean> mListGsmCellBean = new ArrayList<GSMCellBean>();
	private ArrayList<CDMACellBean> mListCdmaCellBean = new ArrayList<CDMACellBean>();
	
	//用户机型平台
	private String mPlatform = "";
	private String mImei = "";
	private String mImsi = "";
	private String mMnc = "";
	private String mMcc = "";
	
	//手机联网方式，wifi,mobile
	private int mInfType;
	//蜂窝网络类型
	private String mNetworkType = "";
	//radio类型，gsm,cdma
	private int mPhoneType = LocationConstants.RADIO_TYPE_UNKNOW;
	
	
	
	public void setPlatform(String platform){
		mPlatform = platform;
	}
	
	public String getPlatform(){
		return mPlatform;
	}
	
	public void setImsi(String imsi){
		mImsi = imsi;
	}
	
	public String getImsi(){
		return mImsi;
	}
	
	public void setImei(String imei){
		mImei = imei;
	}
	
	public String getImei(){
		return mImei;
	}
	
	public void setMnc(String mnc){
		mMnc = mnc;
	}
	
	public String getMnc(){
		return mMnc;
	}
	
	public void setMcc(String mcc){
		mMcc = mcc;
	}
	
	public String getMcc(){
		return mMcc;
	}
	
	public void setInfType(int infType){
		mInfType = infType;
	}
	
	public int getInfType(){
		return mInfType;
	}
	
	public void setNetworkType(String networkType){
		mNetworkType = networkType;
	}
	
	public String getNetworkType(){
		return mNetworkType;
	}
	
	
	public void setPhoneType(int phoneType){
		mPhoneType = phoneType;
	}
	
	public int getPhoneType(){
		return mPhoneType;
	}
	
	public void setGPSLocation(Location gpsLocation){
		mGPSLocation = gpsLocation;
	}
	
	public Location getGPSLocation(){
		return mGPSLocation;
	}
	
	public void setWifiTowerList(ArrayList<WifiTowerBean> listWifiTowerBean){
		mListWifiTowerBean.addAll(listWifiTowerBean);
	}
	
	public ArrayList<WifiTowerBean> getWifiTowerList(){
		return mListWifiTowerBean;
	}
	
	public void setGSMCellBeanList(ArrayList<GSMCellBean> listGsmCellBean){
		mListGsmCellBean.addAll(listGsmCellBean);
	}
	
	public ArrayList<GSMCellBean> getGSMCellBeanList(){
		return mListGsmCellBean;
	}
	
	public void setCDMACellBeanList(ArrayList<CDMACellBean> listCdmaCellBean){
		mListCdmaCellBean.addAll(listCdmaCellBean);
	}
	
	public ArrayList<CDMACellBean> getCDMACellBeanList(){
		return mListCdmaCellBean;
	}
	
}
