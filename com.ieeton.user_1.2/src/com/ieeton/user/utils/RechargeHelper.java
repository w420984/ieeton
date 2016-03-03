package com.ieeton.user.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.view.CustomToast;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.test.MoreAsserts;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class RechargeHelper {
	public static final int TYPE_RECHARGE = 0;
	public static final int TYPE_BUY = 1;
	
	private static RechargeHelper mInstance;
	private static Context mContext;
	private String mBody;
	private int mFee;
	public static int mType;
	private String orderNum;
	private static IWXAPI api;
	private PayReq req;
	private Task mTask;
	private boolean isTaskFree = true;
	private CustomToast mProgressDialog;
	
	public interface RechargeCallback{
		public void callBack(String id);
	}
	
	public static RechargeHelper getInstance(Context context){
		mContext = context;
		if (mInstance == null){
			mInstance = new RechargeHelper();
		}
		if (api == null){
			api = WXAPIFactory.createWXAPI(mContext, null);
		}
		return mInstance;
	}
	
	/*
	 * 微信充值接口
	 * 如果type为充值，则充值到账户余额
	 * 如果type为购买产品，需传产品订单号，微信支付成功后，后台直接购买产品，不需要
	 * 再调用余额支付接口
	 */
	public void WXPay(String body, int fee, int type, String orderNum){
		if (!isTaskFree){
			return;
		}
		mBody = body;
		mFee = fee;
		mType = type;
		this.orderNum = orderNum;
		
		mTask = new Task();
		mTask.execute();
	}
		
	private class Task extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;
	    
		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			
			try {
				result = NetEngine.getInstance(mContext).prepareRecharge(mBody, mFee, getLocalIpAddress(), 
							mType, orderNum);
			} catch (PediatricsIOException e) {
				mThr = e;
				e.printStackTrace();
			} catch (PediatricsParseException e) {
				mThr = e;
				e.printStackTrace();
			} catch (PediatricsApiException e) {
				mThr = e;
				e.printStackTrace();
			}
			return result;			
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			isTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, mContext);
				}else{
					Utils.showToast(mContext, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			genPayReq(result);
			sendPayReq();
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			isTaskFree = true;
			super.onCancelled();
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			isTaskFree = false;
			super.onPreExecute();
		}
		
	}
	
	private void genPayReq(String str) {
		if (TextUtils.isEmpty(str)){
			Utils.logd("xml error");
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(str);
			if (req == null){
				req = new PayReq();
			}
			req.appId = obj.optString("appid");
			req.partnerId = obj.optString("partnerId");
			req.prepayId = obj.optString("prepayId");
			req.packageValue = obj.optString("packageValue");
			req.nonceStr = obj.optString("nonceStr");
			req.timeStamp = obj.optString("timeStamp");
			req.sign = obj.optString("sign");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void sendPayReq() {
		if (req == null){
			Utils.logd("req error");
			return;
		}
		api.registerApp(req.appId);
		api.sendReq(req);
	}
	
	public static String getLocalIpAddress(){  
        try {  
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){  
               NetworkInterface intf = en.nextElement();  
               for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();){  
                   InetAddress inetAddress = enumIpAddr.nextElement();  
                   if (!inetAddress.isLoopbackAddress() && 
                		   InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())){  
                       return inetAddress.getHostAddress().toString();  
                   }  
               }  
           }  
        }  
        catch (SocketException ex){  
            Log.e("WifiPreference IpAddress", ex.toString());  
        }  
        return null;  
    }  
	
	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, mContext);
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}
}
