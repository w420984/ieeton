package com.ieeton.agency.location;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.ieeton.agency.net.NetUtils;
import com.ieeton.agency.net.NetUtils.NetworkState;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;

public class LocationUtils {

	
	/**
	 * 将手机信号ASU转化为DBM
	 * 
	 * @param int 信号ASU信息
	 * @return int
	 */
	public static int transferAsu2Dbm(int intAsu) {
		return (-113 + 2 * intAsu);
	}
	
	
	
	public static int getCellLocationType(CellLocation cellLocation) {
		int intType = LocationConstants.RADIO_TYPE_UNKNOW;
		if (cellLocation instanceof GsmCellLocation) {
			intType = LocationConstants.RADIO_TYPE_GSM;
		} else {
			try {
				Class.forName("android.telephony.cdma.CdmaCellLocation");
				intType = LocationConstants.RADIO_TYPE_CDMA;
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return intType;
	}
	
	
	
	/**
	 * 获取手机MCC和MNC
	 * 
	 * @param TelephonyManager
	 * @return String
	 */
	public static String[] getMccMnc(TelephonyManager telephonyManager) {
		String stringNetworkOperator = telephonyManager.getNetworkOperator();
		if(TextUtils.isEmpty(stringNetworkOperator) || !isNumeric(stringNetworkOperator)){
			stringNetworkOperator = telephonyManager.getSubscriberId();
		}
		String[] stringArrayMccMnc = { "", "" };
		if(!TextUtils.isEmpty(stringNetworkOperator) && stringNetworkOperator.length() >= 5){
			stringArrayMccMnc[0] = stringNetworkOperator.substring(0, 3);
			stringArrayMccMnc[1] = stringNetworkOperator.substring(3, 5);
		}
		return stringArrayMccMnc;
	}
	
	//获取基站信息，兼容双卡手机
	public static CellLocation getCellLocation(TelephonyManager telephonyManager){
        CellLocation cellLocation = telephonyManager.getCellLocation();
        if (cellLocation == null){
            Class<?> cls;
            try {
               cls = Class.forName("com.mediatek.telephony.TelephonyManagerEx");
               Method method1 = cls.getDeclaredMethod("getCellLocation", int.class);
               Method method2 = cls.getDeclaredMethod("getDefault");
               Object obj = (Object)method2.invoke(null);
               cellLocation = (CellLocation)method1.invoke(obj, 1);  //0表示卡1，1表示卡2
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return cellLocation;
    }
	
	/**
	 * 获取蜂窝网络类型
	 * 
	 * @param TelephonyManager
	 * @return String
	 */
	public static String getNetworkType(TelephonyManager telephonyManager){
		int intNetworkType = telephonyManager.getNetworkType();
		String strNetworkType = LocationConstants.NETWORK_TYPE_ARRAY.get(intNetworkType, "UNKNOWN");
		return strNetworkType;
	}
	
	/**
	 * 获取手机联网方式
	 * @param context
	 * @return int
	 */
	public static int getInfType(Context context){
		int inftype = LocationConstants.INFTYPE_INVALID;
		NetworkState networkState = NetUtils.getNetworkState(context);
		if(networkState == NetworkState.WIFI){
			inftype = LocationConstants.INFTYPE_WIFI;
		}else if(networkState == NetworkState.MOBILE){
			inftype = LocationConstants.INFTYPE_MOBILE;
		}
		return inftype;
	}
	
	private static boolean isNumeric(CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

}
