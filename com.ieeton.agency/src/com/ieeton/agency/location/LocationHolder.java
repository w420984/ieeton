package com.ieeton.agency.location;

import java.io.Serializable;

import android.text.TextUtils;



final public class LocationHolder implements Serializable {
    private static final long serialVersionUID = -8205421689204807445L;
    public double lat;
    public double lon;
    public String address = "";
    public String poiid = "";
    public String poititle = "";
    public String xid = "";
    public boolean offset = false; // 经纬度未偏移

    
    public LocationHolder(){}
    
    public LocationHolder(IeetonLocation weiboLocation){
    	if(weiboLocation != null){
    		lat = weiboLocation.getLatitude();
    		lon = weiboLocation.getLongtitude();
    		offset = weiboLocation.isOffset();
    	}
    	
    }
    
    public boolean equals(Object o) {
        if (o == null) { return false; }

        if (o == this) { return true; }

        if (o.getClass() == getClass()) {
            final LocationHolder holder = (LocationHolder) o;
            if (address.equals(holder.address)) {
                if ((Math.abs(holder.lat - lat) < .001)
                        && (Math.abs(holder.lon - lon) < .001)) { return true; }
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
   	public int hashCode() {
   		final int prime = 31;
   		int result = 1;
   		result = prime * result + ((address == null) ? 0 : address.hashCode());
   		long temp;
   		temp = Double.doubleToLongBits(lat);
   		result = prime * result + (int) (temp ^ (temp >>> 32));
   		temp = Double.doubleToLongBits(lon);
   		result = prime * result + (int) (temp ^ (temp >>> 32));
   		return result;
   	}

    public String toString() {
        return ">>>>>>>>>> LocationHolder <<<<<<<<<<" + "\tlat:" + lat
                + "\tlon:" + lon + "\tadress:" + address;
    }

    public boolean isUseful() {
        return isIllegle(lat) && isIllegle(lon);
    }

    private boolean isIllegle(double pos) {
        if ((pos > 1.) || (pos < -1.)) { return true; }
        return false;
    }

    public boolean isUsefulWithAddress() {
        return !TextUtils.isEmpty(address) && isUseful();
    }

    public void clearLocationInfo(){
        this.lat = 0.0;
        this.lon = 0.0;
        this.address = "";
        this.poiid = "";
        this.poititle = "";
        this.xid = "";
    }
    
    public void clone(LocationHolder holder){
        if( holder == null ){
            holder = new LocationHolder();
        }
        holder.address = this.address;
        holder.lat = this.lat;
        holder.lon = this.lon;
        holder.poiid = this.poiid;
        holder.poititle = this.poititle;
        holder.xid = this.xid;
        holder.offset = this.offset;
    }
}