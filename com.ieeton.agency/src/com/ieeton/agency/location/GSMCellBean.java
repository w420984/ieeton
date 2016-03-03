package com.ieeton.agency.location;


public class GSMCellBean {

	private String mMcc = "";
	private String mMnc = "";
	private int mLac = 0;
	private int mCellid = 0;
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

	public int getLac() {
		return mLac;
	}

	public void setLac(int lac) {
		mLac = lac;
	}

	public int getCellid() {
		return mCellid;
	}

	public void setCellid(int cellid) {
		mCellid = cellid;
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
