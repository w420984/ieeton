package com.ieeton.user.location;

import java.io.Serializable;

public class IeetonLocation implements Serializable{

	/**
     * 
     */
    private static final long serialVersionUID = -3578207678672364539L;
    private double mLatitude = 0;
	private double mLongtitude = 0;
	private double mRadius;
	private String mProvince;
	private String mCity;
	private String mDistrict;
	private boolean mOffset = false;
	
	public void setProvince(String province){
		mProvince = province;
	}
	
	public void setCity(String city){
		mCity = city;
	}
	
	public void setDistrict(String district){
		mDistrict = district;
	}
	
	public void setRadius(double radius){
		mRadius = radius;
	}
	
	public String getProvince(){
		return mProvince;
	}
	
	public String getCity(){
		return mCity;
	}
	
	public String getDistrict(){
		return mDistrict;
	}
	
	public double getRadius(){
		return mRadius;
	}
	
	public void setOffset(boolean offset){
		mOffset = offset;
	}
	
	public boolean isOffset(){
		return mOffset;
	}
	
	public void setLatitude(double latitude){
		mLatitude = latitude;
	}
	
	public void setLongtitude(double longtitude){
		mLongtitude = longtitude;
	}
	
	public double getLatitude(){
		return mLatitude;
	}
	
	public double getLongtitude(){
		return mLongtitude;
	}
	
	public boolean isUseful() {
        return isIllegle(mLatitude) && isIllegle(mLongtitude);
    }

    private boolean isIllegle(double pos) {
        if ((pos > 1.) || (pos < -1.)) { return true; }
        return false;
    }

    @Override
    public boolean equals( Object o ) {
        if (o == null) {
            return false;
        }

        if( this == o ) {
            return true;
        }

        if( o.getClass() == getClass() ) {
            IeetonLocation location = (IeetonLocation) o;
            return mLatitude == location.getLatitude()
                    && mLongtitude == location.getLongtitude()
                    && mOffset == location.isOffset();
        }
        return false;
    }
	
}
