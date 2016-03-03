package com.ieeton.agency.location;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.net.NetUtils;

import android.content.Context;
import android.location.Location;


public class LocationRequestFromSina implements ILocationRequest{

	private Context mContext;
	
	public LocationRequestFromSina(Context context){
		mContext = context;
	}
	
	@Override
	public IeetonLocation requestWeiboLocation(RequestDataWrapper requestData){
		if(requestData == null){
		    return null;
		}
		
		String requestJson = createRequestJson(requestData);
		IeetonLocation weiboLocation = null;
		
		try {
			weiboLocation = NetEngine.getInstance(mContext).getLocation();
		} catch (PediatricsIOException e) {
			// ignore
		} catch (PediatricsParseException e) {
			// ignore
		} catch (PediatricsApiException e) {
			// ignore
		}
		
		return weiboLocation;
	}
	
	
	private String createRequestJson(RequestDataWrapper requestData){
		JSONObject jsonRequest = new JSONObject();
		try {
			jsonRequest.put("user_agent", URLEncoder.encode(NetUtils.generateUA(mContext)));
			jsonRequest.put("platform", requestData.getPlatform());
			jsonRequest.put("home_mobile_country_code", requestData.getMcc());
			jsonRequest.put("home_mobile_network_code", requestData.getMnc());
			jsonRequest.put("radio_type", requestData.getNetworkType());
			jsonRequest.put("imei_imsi", requestData.getImei()+"_"+requestData.getImsi());
			int phoneType = requestData.getPhoneType();
			jsonRequest.put("cdma_type", phoneType== LocationConstants.RADIO_TYPE_CDMA ? 1 : 0);
			jsonRequest.put("nettype", requestData.getInfType());
			
			Location gpsLocation = requestData.getGPSLocation();
			if(gpsLocation != null){
				jsonRequest.put("location", locationToJson(gpsLocation));
			}
			
			if(phoneType == LocationConstants.RADIO_TYPE_GSM){
				ArrayList<GSMCellBean> gsmCellList = requestData.getGSMCellBeanList();
				if(gsmCellList != null && gsmCellList.size() > 0){
					JSONArray gsmCellListJson = new JSONArray();
					for(GSMCellBean gsmCellBean : gsmCellList){
						if(gsmCellBean != null){
							gsmCellListJson.put(gsmCellToJson(gsmCellBean));
						}
					}
					jsonRequest.put("cell_towers", gsmCellListJson);
				}
			}else if(phoneType == LocationConstants.RADIO_TYPE_CDMA){
				ArrayList<CDMACellBean> cdmaCellList = requestData.getCDMACellBeanList();
				if(cdmaCellList != null && cdmaCellList.size() > 0){
					JSONArray cdmaCellListJson = new JSONArray();
					for(CDMACellBean cdmaCellBean : cdmaCellList){
						if(cdmaCellBean != null){
							cdmaCellListJson.put(cdmaCellToJson(cdmaCellBean));
						}
					}
					jsonRequest.put("cell_towers", cdmaCellListJson);
				}
			}
			
			ArrayList<WifiTowerBean> wifiTowerList = requestData.getWifiTowerList();
			if(wifiTowerList != null && wifiTowerList.size() > 0){
				JSONArray wifiTowerListJson = new JSONArray();
				for(WifiTowerBean wifiTower : wifiTowerList){
					if(wifiTower != null){
						wifiTowerListJson.put(wifiTowerToJson(wifiTower));
					}
				}
				jsonRequest.put("wifi_towers", wifiTowerListJson);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonRequest.toString();
	}
	
	private JSONObject locationToJson(Location locationGPS){
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("latitude", locationGPS.getLatitude());
			jsonObj.put("longitude", locationGPS.getLongitude());
			jsonObj.put("accuracy", locationGPS.getAccuracy());
			jsonObj.put("timestamp", locationGPS.getTime());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObj;
	}
	
	private JSONObject gsmCellToJson(GSMCellBean gsmCell){
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("cell_id", gsmCell.getCellid());
			jsonObj.put("location_area_code", gsmCell.getLac());
			jsonObj.put("mobile_country_code", gsmCell.getMcc());
			jsonObj.put("mobile_network_code", gsmCell.getMnc());
			jsonObj.put("signal_strength", gsmCell.getSignal());
			jsonObj.put("cell_type", gsmCell.getCellType());
			jsonObj.put("with_lat_lon", 0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonObj;
	}
	
	private JSONObject cdmaCellToJson(CDMACellBean cdmaCell){
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("cdma_base_station_id", cdmaCell.getBid());
			jsonObj.put("cdma_network_id", cdmaCell.getNid());
			jsonObj.put("mobile_country_code", cdmaCell.getMcc());
			jsonObj.put("cdma_system_id", cdmaCell.getSid());
			jsonObj.put("signal_strength", cdmaCell.getSignal());
			jsonObj.put("cell_type", cdmaCell.getCellType());
			if(cdmaCell.isLatValid() && cdmaCell.isLonValid()){
				jsonObj.put("with_lat_lon", 1);
				jsonObj.put("latitude", cdmaCell.getLat());
				jsonObj.put("longitude", cdmaCell.getLon());
			}else{
				jsonObj.put("with_lat_lon", 0);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObj;
	}
	
	
	private JSONObject wifiTowerToJson(WifiTowerBean wifiTower){
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("mac_address", wifiTower.getMacAddress());
			jsonObj.put("ssid", wifiTower.getSSID());
			jsonObj.put("signal_strength", wifiTower.getSignal());
			jsonObj.put("wifi_type", wifiTower.getWifiType());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObj;
	}
}
