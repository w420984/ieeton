package com.ieeton.agency.location;

import android.util.SparseArray;

public class LocationConstants {

	//基站定位，GPS定位状态(正在定位，休眠，中断)
	public static final int LOCATION_STATE_RUNNING = 1;
	public static final int LOCATION_STATE_SLEEPING = 2;
	public static final int LOCATION_STATE_TERMINATE = 3;
	
	//定位模块消息类型
	public static final int LOCATION_MSG_START = 1;
	public static final int LOCATION_MSG_FINISH = 2;
	public static final int LOCATINO_MSG_CANCEL = 3;
	public static final int LOCATINO_MSG_TIMEOUT = 4;
	
	// GPS位置更新的最小时间间隔
	public static final int minGpsLocationUpdateInterval = 5 * 1000;
	// GPS定位超时时间
	public static final int TIMEOUT_OF_GPS_LOCATION = 60 * 1000;
	
	// 两次定位间的最小时间间隔
	public static final int MIN_REQUEST_INTERVAL = 30 * 1000;
	
	// 默认基站信号（GSM/CDMA/WCDMA）强度（DBM）
	public static final int DEFAULT_CGISIGNAL = -100;
	
	// 内存中缓存的位置记录数量上限
	public static final int MAX_LOCATIONCACHE_NUM = 30;
	
	// 内存中缓存的位置记录时间上限（毫秒）
	public static final long MAX_LOCATIONCACHE_TIME = 5 * 60 * 1000;
	
	// 包含的WIFI个数上限，用于减少数据量
	public static final int MAX_WIFI_NUM = 10;
	
	//手机网络类型
	public static final int RADIO_TYPE_GSM = 1;
	public static final int RADIO_TYPE_CDMA = 2;
	public static final int RADIO_TYPE_UNKNOW = 9;
	
	//基站,wifi,主从类型 "main"表示主基站，"nearby"表示相邻基站
	public static final String TYPE_MAIN = "main";
	public static final String TYPE_NEARBY = "nearby";
	
	//手机联网方式
	public static final int INFTYPE_MOBILE = 0;
	public static final int INFTYPE_WIFI = 1;
	public static final int INFTYPE_INVALID = -1;
	
	//标识定位哪个定位模块定位结束
	public static final int LOCATION_FINISH_CELL = 1;
	public static final int LOCATION_FINISH_GPS = 2;
	
	//手机网络制式映射表
	public static final SparseArray<String> NETWORK_TYPE_ARRAY = new SparseArray<String>();
	static{
		NETWORK_TYPE_ARRAY.append(0, "UNKNOWN");
		NETWORK_TYPE_ARRAY.append(1, "GPRS");
		NETWORK_TYPE_ARRAY.append(2, "EDGE");
		NETWORK_TYPE_ARRAY.append(3, "UMTS");
		NETWORK_TYPE_ARRAY.append(4, "CDMA");
		NETWORK_TYPE_ARRAY.append(5, "EVDO_0");
		NETWORK_TYPE_ARRAY.append(6, "EVDO_A");
		NETWORK_TYPE_ARRAY.append(7, "1xRTT");
		NETWORK_TYPE_ARRAY.append(8, "HSDPA");
		NETWORK_TYPE_ARRAY.append(9, "HSUPA");
		NETWORK_TYPE_ARRAY.append(10, "HSPA");
		NETWORK_TYPE_ARRAY.append(11, "IDEN");
		NETWORK_TYPE_ARRAY.append(12, "EVDO_B");
		NETWORK_TYPE_ARRAY.append(13, "LTE");
		NETWORK_TYPE_ARRAY.append(14, "EHRPD");
		NETWORK_TYPE_ARRAY.append(15, "HSPAP");
	}
	
}
