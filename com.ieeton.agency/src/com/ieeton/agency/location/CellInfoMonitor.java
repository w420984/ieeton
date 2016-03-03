package com.ieeton.agency.location;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

/**
 * 基站信息监听器,负责监听手机状态并向使用者提供定位所需要的基站，wifi信息
 * @author zhengtao
 * 
 */
public class CellInfoMonitor {

	public interface CellInfoMonitorListener{
		public void onMonitorFinished(RequestDataWrapper requestDataWrapper);
	}
	
	private Context mContext = null;
	private CellInfoMonitorListener mMonitorListener = null;
	private boolean mIsTerminated = true;
	
	private Looper mMonitorLooper = null;
	private Handler mMonitorHandler = null; 
	
	private WifiManager mWifiManager = null;
	private TelephonyManager mTelephonyManager = null;
	
	private PhoneStateListener mPhoneStateListener = null;
	private int mPhoneType = LocationConstants.RADIO_TYPE_UNKNOW;
	private int mCellSignal = LocationConstants.DEFAULT_CGISIGNAL;
	
	private ArrayList<WifiTowerBean> mListWifiTowerBean = new ArrayList<WifiTowerBean>();
	private ArrayList<GSMCellBean> mListGsmCellBean = new ArrayList<GSMCellBean>();
	private ArrayList<CDMACellBean> mListCdmaCellBean = new ArrayList<CDMACellBean>();
	
	
	public CellInfoMonitor(Context context,CellInfoMonitorListener cellInfoMonitorListener){
		mContext = context.getApplicationContext();
		mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		
		mMonitorListener = cellInfoMonitorListener;
	}
	
	//开始基站监控
	public synchronized void startMonitor(){
		if(!mIsTerminated){
			return;
		}
		mIsTerminated = false;
		createCellMonitorThread();
	}
	
	//停止基站监控
	public synchronized void stopMonitor(){
		if(mMonitorHandler != null){
			mMonitorHandler.sendEmptyMessage(LocationConstants.LOCATINO_MSG_CANCEL);
		}
	}
	
	//启动监听线程
	private void createCellMonitorThread(){
		new Thread(){
			@Override
			public void run() {
				Looper.prepare();
				mMonitorHandler = new Handler(){
					@Override
					public void handleMessage(Message msg) {
						switch(msg.what){
						case LocationConstants.LOCATION_MSG_START: 
							initMonitorListeners();
							break;
						case LocationConstants.LOCATION_MSG_FINISH:
							notifyListener();
							break;
						case LocationConstants.LOCATINO_MSG_CANCEL:
							terminate();
							break;
						}
						super.handleMessage(msg);
					}
				};
				mMonitorLooper = Looper.myLooper();
				mMonitorHandler.sendEmptyMessage(LocationConstants.LOCATION_MSG_START);
				Looper.loop();
			}
		}.start();
	}
	
	private synchronized void notifyListener(){
		if(mMonitorListener != null){
			mMonitorListener.onMonitorFinished(getNetLocationRequestData());
		}
	}
	
	
	//初始化定位需要的监听器
	private synchronized void initMonitorListeners(){
		initWifiInfo();
		initPhoneStateListener();
		
		mMonitorHandler.sendEmptyMessageDelayed(LocationConstants.LOCATION_MSG_FINISH, 10);
	}
	
	
	/**
	 * 初始化WIFI
	 * 
	 * @param 无
	 * @return 无
	 */
	private void initWifiInfo() {
		if(mWifiManager.isWifiEnabled()){
			WifiInfo mMainWifi = mWifiManager.getConnectionInfo();
			if(mMainWifi != null && mMainWifi.getBSSID() != null){
				mListWifiTowerBean.add(getWifiTowerBeanFromWifiInfo(mMainWifi));
			}
			List<ScanResult> listScanResult = mWifiManager.getScanResults();
			if(listScanResult != null && listScanResult.size() > 0){
				for (int i = 0; i < listScanResult.size(); i++) {
					ScanResult scanResult = listScanResult.get(i);
					mListWifiTowerBean.add(getWifiTowerBeanFromScanResult(scanResult));
					if (i > (LocationConstants.MAX_WIFI_NUM - 1)){
						break;
					}
				}
			}
		}
	}
	
	/**
	 * 初始化PHONESTATE监听器
	 * 
	 * @param 无
	 * @return 无
	 */
	private synchronized void initPhoneStateListener() {
		mPhoneStateListener = new PhoneStateListener() {
			@Override
			public void onCellLocationChanged(CellLocation cellLocation) {
				if(!mIsTerminated){
					handleCellLocationChanged(cellLocation, mTelephonyManager);
					super.onCellLocationChanged(cellLocation);
				}
			}

			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				if(!mIsTerminated){
					handleCellSignalStrengthsChanged(signalStrength);
					super.onSignalStrengthsChanged(signalStrength);
				}
			}
		};
		mTelephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		mTelephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_CELL_LOCATION);
		mPhoneStateListener.onCellLocationChanged(LocationUtils.getCellLocation(mTelephonyManager));
		CellLocation.requestLocationUpdate();
	}
	
	/**
	 * 处理手机基站切换的回调
	 * 
	 * @param CellLocation
	 *            主基站
	 * @param TelephonyManager
	 * @return 无
	 */
	private synchronized void handleCellLocationChanged(
			CellLocation cellLocation, TelephonyManager telephonyManager) {
		if(cellLocation == null){
			return;
		}
		int intCellLocationType = LocationUtils.getCellLocationType(cellLocation);
		switch (intCellLocationType) {
		case LocationConstants.RADIO_TYPE_GSM:
			handleGsmCellLocationChange(cellLocation, telephonyManager);
			break;
		case LocationConstants.RADIO_TYPE_CDMA:
			handleCdmaCellLocationChange(cellLocation, telephonyManager);
			break;
		default:
			break;
		}
	}
	
	/**
	 * 设置基站的信号强度
	 * 
	 * @param SignalStrength
	 * @return 无
	 */
	private synchronized void handleCellSignalStrengthsChanged(
			SignalStrength signalStrength) {
		if(signalStrength == null){
			return;
		}
		switch (mPhoneType) {
		case LocationConstants.RADIO_TYPE_GSM:
			int intAsu = signalStrength.getGsmSignalStrength();
			mCellSignal = LocationUtils.transferAsu2Dbm(intAsu);
			if(mListGsmCellBean.size() > 0){
				mListGsmCellBean.get(0).setSignal(mCellSignal);
			}
			break;
		case LocationConstants.RADIO_TYPE_CDMA:
			mCellSignal = signalStrength.getCdmaDbm();
			if(mListCdmaCellBean.size() > 0){
				mListCdmaCellBean.get(0).setSignal(mCellSignal);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 处理GSM基站切换
	 * 
	 * @param CellLocation
	 *            主基站
	 * @param TelephonyManager
	 * @return 无
	 */
	private void handleGsmCellLocationChange(CellLocation cellLocation,
			TelephonyManager telephonyManager) {
		GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
		//由于部分双卡双待手机获取的CellLocation的参数无效，改if分支针对这种情况，做专门处理
		if(!isGsmCellValid(gsmCellLocation)){
			gsmCellLocation = getGsmCellViaReflect();
			if(gsmCellLocation == null){
				return;
			}
		}
		mPhoneType = LocationConstants.RADIO_TYPE_GSM;
		mListGsmCellBean.clear();
		mListGsmCellBean.add(getGsmCellBeanFromCellLocation(gsmCellLocation,telephonyManager));
		List<NeighboringCellInfo> listNeighboringCellInfo = telephonyManager.getNeighboringCellInfo();
		if(listNeighboringCellInfo != null){
			for (int i = 0; i < listNeighboringCellInfo.size(); i++) {
				if (listNeighboringCellInfo.get(i).getCid() < 65535) {
					mListGsmCellBean.add(getGsmCellBeanFromNeighboringCellInfo(
							listNeighboringCellInfo.get(i), telephonyManager));
				}
			}
		}
	}
	
	/**
	 * 处理CDMA基站切换
	 * 
	 * @param CellLocation
	 *            主基站
	 * @param TelephonyManager
	 * @return 无
	 */
	private void handleCdmaCellLocationChange(CellLocation cellLocation,
			TelephonyManager telephonyManager) {
		CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cellLocation;
		//由于部分双卡双待手机获取的CellLocation的参数无效，该if分支针对这种情况，做专门处理
		if(!isCdmaCellValid(cdmaCellLocation)){
			cdmaCellLocation = getCdmaCellViaReflect();
			if(cdmaCellLocation == null){
				return;
			}
		}
		mPhoneType = LocationConstants.RADIO_TYPE_CDMA;
		mListCdmaCellBean.clear();
		String[] stringArrayMccMnc = LocationUtils.getMccMnc(telephonyManager);
		CDMACellBean cdmaCellBean = new CDMACellBean();
		cdmaCellBean.setMcc(stringArrayMccMnc[0]);
		cdmaCellBean.setMnc(stringArrayMccMnc[1]);
		cdmaCellBean.setSid(cdmaCellLocation.getSystemId());
		cdmaCellBean.setNid(cdmaCellLocation.getNetworkId());
		cdmaCellBean.setBid((cdmaCellLocation.getBaseStationId()));
		cdmaCellBean.setSignal(mCellSignal);
		cdmaCellBean.setLat(cdmaCellLocation.getBaseStationLatitude());
		cdmaCellBean.setLon(cdmaCellLocation.getBaseStationLongitude());
		cdmaCellBean.setCellType(LocationConstants.TYPE_MAIN);
		mListCdmaCellBean.add(cdmaCellBean);
	}
	
	/**
	 * 从CellLocation中获取GsmCellBean
	 * 
	 * @param CellLocation
	 *            主基站
	 * @param TelephonyManager
	 * @return GsmCellBean
	 */
	private GSMCellBean getGsmCellBeanFromCellLocation(
			GsmCellLocation gsmCellLocation, TelephonyManager telephonyManager) {
		String[] stringArrayMccMnc = LocationUtils.getMccMnc(telephonyManager);
		GSMCellBean gsmCellBean = new GSMCellBean();
		gsmCellBean.setMcc(stringArrayMccMnc[0]);
		gsmCellBean.setMnc(stringArrayMccMnc[1]);
		gsmCellBean.setLac(gsmCellLocation.getLac());
		gsmCellBean.setCellid(gsmCellLocation.getCid());
		gsmCellBean.setSignal(mCellSignal);
		gsmCellBean.setCellType(LocationConstants.TYPE_MAIN);
		return gsmCellBean;
	}
	
	/**
	 * 从NeighboringCellInfo中获取GsmCellBean
	 * 
	 * @param NeighboringCellInfo
	 *            周边基站
	 * @return GsmCellBean
	 */
	private GSMCellBean getGsmCellBeanFromNeighboringCellInfo(
			NeighboringCellInfo neighboringCellInfo,
			TelephonyManager telephonyManager) {
		String[] stringArrayMccMnc = LocationUtils.getMccMnc(telephonyManager);
		GSMCellBean gsmCellBean = new GSMCellBean();
		gsmCellBean.setMcc(stringArrayMccMnc[0]);
		gsmCellBean.setMnc(stringArrayMccMnc[1]);
		gsmCellBean.setLac(neighboringCellInfo.getLac());
		gsmCellBean.setCellid(neighboringCellInfo.getCid());
		int intRssi = neighboringCellInfo.getRssi();
		gsmCellBean.setSignal(LocationUtils.transferAsu2Dbm(intRssi));
		gsmCellBean.setCellType(LocationConstants.TYPE_NEARBY);
		return gsmCellBean;
	}
	
	
	
	private boolean isGsmCellValid(GsmCellLocation gsmCell){
		return gsmCell.getCid() != 0;
	}
	
	private boolean isCdmaCellValid(CdmaCellLocation cdmaCell){
		return cdmaCell.getBaseStationId() != 0;
	}
	
	private GsmCellLocation getGsmCellViaReflect(){
		Bundle bundle = null;
		GsmCellLocation backupCell = null;
        try {
            bundle = getITelephonyBundle(mTelephonyManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bundle != null) {
            backupCell = new GsmCellLocation(bundle);
        }
        return backupCell;
	}
	
	private CdmaCellLocation getCdmaCellViaReflect(){
		Bundle bundle = null;
		CdmaCellLocation backupCell = null;
        try {
            bundle = getITelephonyBundle(mTelephonyManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bundle != null) {
        	backupCell = new CdmaCellLocation(bundle);
        }
        return backupCell;
	}
	
	
	private Bundle getITelephonyBundle(TelephonyManager telMgr)throws Exception {
		Method getITelephony = telMgr.getClass().getDeclaredMethod("getITelephony");
		getITelephony.setAccessible(true);
		Object iTelephony = getITelephony.invoke(telMgr);
		Method getCellLocation = iTelephony.getClass().getDeclaredMethod("getCellLocation");
		Object result = getCellLocation.invoke(iTelephony);
		return (Bundle) result;
	}
	
	
	private WifiTowerBean getWifiTowerBeanFromWifiInfo(WifiInfo wifiInfo){
		WifiTowerBean wifiTower = new WifiTowerBean();
		wifiTower.setMacAddress(wifiInfo.getBSSID());
		wifiTower.setSSID(wifiInfo.getSSID());
		wifiTower.setSignal(wifiInfo.getRssi());
		wifiTower.setWifiType(LocationConstants.TYPE_MAIN);
		return wifiTower;
	}
	
	private WifiTowerBean getWifiTowerBeanFromScanResult(ScanResult scanResult){
		WifiTowerBean wifiTower = new WifiTowerBean();
		wifiTower.setMacAddress(scanResult.BSSID);
		wifiTower.setSSID(scanResult.SSID);
		wifiTower.setSignal(scanResult.level);
		wifiTower.setWifiType(LocationConstants.TYPE_NEARBY);
		return wifiTower;
	}
	
	//中断监听
	private synchronized void terminate(){
		mIsTerminated = true;
		if(mMonitorLooper != null){
			mMonitorLooper.quit();
			mMonitorLooper = null;
		}
		mMonitorHandler = null;
		try {
			mTelephonyManager.listen(mPhoneStateListener,PhoneStateListener.LISTEN_NONE);
		} catch (Exception exception) {
		}
		mPhoneType = LocationConstants.RADIO_TYPE_UNKNOW;
		mCellSignal = LocationConstants.DEFAULT_CGISIGNAL;
		mPhoneStateListener = null;
		mListGsmCellBean.clear();
		mListCdmaCellBean.clear();
		mListWifiTowerBean.clear();
	}
	
	
	/**
	 * 获取发起请求的KEY
	 * 
	 * @param 无
	 * @return String
	 */
	public synchronized String getLocationRequestKey(){
		String stringKey = "";
		StringBuffer sb = new StringBuffer();
		switch (mPhoneType) {
		case LocationConstants.RADIO_TYPE_GSM:
			if(mListGsmCellBean.size() > 0){
				GSMCellBean gsmCellBean = mListGsmCellBean.get(0);
				sb = new StringBuffer();
				sb.append(gsmCellBean.getMcc());
				sb.append("#");
				sb.append(gsmCellBean.getMnc());
				sb.append("#");
				sb.append(gsmCellBean.getLac());
				sb.append("#");
				sb.append(gsmCellBean.getCellid());
				stringKey = sb.toString();
				gsmCellBean = null;
			}
			break;
		case LocationConstants.RADIO_TYPE_CDMA:
			if(mListCdmaCellBean.size() > 0){
				CDMACellBean cdmaCellBean = mListCdmaCellBean.get(0);
				sb = new StringBuffer();
				sb.append(cdmaCellBean.getMcc());
				sb.append("#");
				sb.append(cdmaCellBean.getMnc());
				sb.append("#");
				sb.append(cdmaCellBean.getSid());
				sb.append("#");
				sb.append(cdmaCellBean.getNid());
				sb.append("#");
				sb.append(cdmaCellBean.getBid());
				stringKey = sb.toString();
				cdmaCellBean = null;
			}
			break;
		default:
			break;
		}
		sb.setLength(0);
		return stringKey;
	}
	
	//获取监听到的基站，wifi信息
	public synchronized RequestDataWrapper getNetLocationRequestData(){
		RequestDataWrapper dataWrapper = new RequestDataWrapper();
		dataWrapper.setPlatform("android");
		dataWrapper.setImei(mTelephonyManager.getDeviceId());
		dataWrapper.setImsi(mTelephonyManager.getSubscriberId());
		String[] stringArrayMccMnc = LocationUtils.getMccMnc(mTelephonyManager);
		dataWrapper.setMcc(stringArrayMccMnc[0]);
		dataWrapper.setMnc(stringArrayMccMnc[1]);
		dataWrapper.setPhoneType(mPhoneType);
		dataWrapper.setInfType(LocationUtils.getInfType(mContext));
		dataWrapper.setNetworkType(LocationUtils.getNetworkType(mTelephonyManager));
		dataWrapper.setGSMCellBeanList(mListGsmCellBean);
		dataWrapper.setCDMACellBeanList(mListCdmaCellBean);
		dataWrapper.setWifiTowerList(mListWifiTowerBean);
		return dataWrapper;
	}
	
}
