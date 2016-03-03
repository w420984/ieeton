package com.ieeton.agency.location;


public class CDMACellBean {

	private String mMnc = "";
	private String mMcc = "";
	private int mLat = Integer.MAX_VALUE;
	private int mLon = Integer.MAX_VALUE;
	private int mSid = 0;
	private int mNid = 0;
	private int mBid = 0;
	private int mSignal = LocationConstants.DEFAULT_CGISIGNAL; 
	private String mCellType = "";
	
	
	public String getMcc() {
		return mMcc;
	}

	public void setMcc(String mcc) {
		mMcc = mcc;
	}

	public String getMnc() {
		return mMnc;
	}

	public void setMnc(String mnc) {
		mMnc = mnc;
	}

	public int getLat() {
		return mLat;
	}

	public void setLat(int lat) {
		if (lat < Integer.MAX_VALUE) {
			mLat = lat;
		}
	}
	
	public boolean isLatValid(){
		return mLat != Integer.MAX_VALUE && mLat != 0;
	}

	public int getLon() {
		return mLon;
	}

	public void setLon(int lon) {
		if (lon < Integer.MAX_VALUE) {
			mLon = lon;
		}
	}
	
	public boolean isLonValid(){
		return mLon != Integer.MAX_VALUE && mLon != 0;
	}

	public int getSid() {
		return mSid;
	}

	public void setSid(int sid) {
		mSid = sid;
	}

	public int getNid() {
		return mNid;
	}

	public void setNid(int nid) {
		mNid = nid;
	}

	public int getBid() {
		return mBid;
	}

	public void setBid(int bid) {
		mBid = bid;
	}

	public int getSignal() {
		return mSignal;
	}

	public void setSignal(int signal) {
		mSignal = signal;
	}
	
	public String getCellType(){
		return mCellType;
	}
	
	public void setCellType(String cellType){
		mCellType = cellType;
	}
}
