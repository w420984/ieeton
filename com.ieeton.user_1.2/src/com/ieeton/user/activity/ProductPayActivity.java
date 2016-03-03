package com.ieeton.user.activity;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.models.Product;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.RechargeHelper;
import com.ieeton.user.utils.Utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ProductPayActivity extends TemplateActivity {
	public static String EXTRA_NUMBER = "extra_number";
	public static String EXTRA_ORDERID = "extra_orderId";
	public static String PAY_RESULT = "pay_result";
	private final int MODE_GETINFO = 1;
	private final int MODE_ONLINE_PAY = 2;
	private final int MODE_BALANCE_PAY = 3;
	
	private final int BALANCE_PAY = 1;
	private final int ONLINE_PAY = 2;
	
	private TextView mTvTotalPrice;
	private TextView mTvBalance;
	private TextView mTvBalancePay;
	private TextView mTvPay;	
	private ViewGroup mVgBalancePay;
	private ViewGroup mVgOnlinePay;
	private ImageView mIvBalancePay;
	private ImageView mIvOnlinePay;
	
	private int mNumber;
	private int mTotalPrice;
	private int mBalance;
	private String mOrderId;
	private Product mProduct;
	private int mPayType;
	private boolean isTaskFree = true;
	private FechDataTask mTask;
	private IeetonUser mUser;
	private String mOwnerId;
	private BroadcastReceiver mReceiver;
    
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.product_pay);
		setTitleBar(getString(R.string.back), getString(R.string.confirm_pay), null);
		initDataFromIntent();
		initView();
		if (mBalance < 0){
			mTask = new FechDataTask();
			try {
				mTask.execute(MODE_GETINFO);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.WEIXIN_PAY_SUCCESS_ACTION);
		mReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Constants.WEIXIN_PAY_SUCCESS_ACTION.equals(intent.getAction())){
					paySuccess();
				}
			}			
		};
		registerReceiver(mReceiver, filter);
	}

	private void initView(){
		mTvTotalPrice = (TextView) findViewById(R.id.tv_money);
		mTvBalancePay = (TextView) findViewById(R.id.tv_balance_pay);
		mTvBalance = (TextView) findViewById(R.id.tv_balance);
		mTvPay = (TextView) findViewById(R.id.tv_pay);
		mVgBalancePay = (ViewGroup) findViewById(R.id.rl_balance_pay);
		mVgOnlinePay = (ViewGroup) findViewById(R.id.rl_online_pay);
		mIvBalancePay = (ImageView) findViewById(R.id.iv_balance_pay);
		mIvOnlinePay = (ImageView) findViewById(R.id.iv_online_pay);
		
		mTvPay.setOnClickListener(this);
		mVgBalancePay.setOnClickListener(this);
		mVgOnlinePay.setOnClickListener(this);
		
		mTvTotalPrice.setText("¥" + mTotalPrice);
		showBalance();
	}
	
	private void showBalance(){
		mTvBalance.setText("¥" + mBalance);
		TextView balance_pay = (TextView) findViewById(R.id.tv_balance_pay);
		if (mBalance < mTotalPrice){
			balance_pay.setText(R.string.balance_not_enough);
			balance_pay.setTextColor(getResources().getColor(R.color.color_gray));
			setPayType(ONLINE_PAY);			
		}else{
			balance_pay.setText(R.string.account_balance);
			balance_pay.setTextColor(getResources().getColor(R.color.color_black));
			setPayType(BALANCE_PAY);			
		}
	}
	
	private void initDataFromIntent(){
		Intent intent = getIntent();
		if (intent == null){
			finish();
			return;
		}
		mProduct = (Product) intent.getSerializableExtra(Constants.EXTRA_PRODUCT);
		mTotalPrice = intent.getIntExtra(Constants.EXTRA_AMOUNT, 0);
		mOrderId = intent.getStringExtra(Constants.EXTRA_ORDERID);
		mBalance = intent.getIntExtra(Constants.EXTRA_BALANCE, -1);
		mNumber = intent.getIntExtra(Constants.EXTRA_NUMBER, 1);
		mOwnerId = intent.getStringExtra(Constants.EXTRA_UID);
		if (mProduct == null || TextUtils.isEmpty(mOrderId)){
			finish();
			return;
		}
		
	}
	
	@Override
	protected void onDestroy() {
		if (mReceiver != null){
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		super.onDestroy();
	}
	
	private void setPayType(int type){
		if (type == BALANCE_PAY){
			mIvBalancePay.setVisibility(View.VISIBLE);
			mIvOnlinePay.setVisibility(View.INVISIBLE);
			mPayType = BALANCE_PAY;
		}else{
			mIvBalancePay.setVisibility(View.INVISIBLE);
			mIvOnlinePay.setVisibility(View.VISIBLE);
			mPayType = ONLINE_PAY;
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == mTvPay){
//			if (mProduct.getStatus() < 0){
//				if (mProduct.getType().equals(Product.ACTIVITY)){
//					Utils.showToast(this, getString(R.string.activity_outline), Toast.LENGTH_SHORT);
//				}else{
//					Utils.showToast(this, getString(R.string.product_outline), Toast.LENGTH_SHORT);
//				}
//				return;
//			}
			if (!isTaskFree){
				return;
			}
			if (mPayType == ONLINE_PAY){
				onlinePay();
			}else{
				balancePay();
			}
		}else if (v == mVgBalancePay){
			if (mBalance >= mTotalPrice){
				setPayType(BALANCE_PAY);
			}
		}else if (v == mVgOnlinePay){
			setPayType(ONLINE_PAY);
		}
		super.onClick(v);
	}

	private void onlinePay(){
		RechargeHelper.getInstance(this).WXPay(mProduct.getName(), 
				mTotalPrice*100, RechargeHelper.TYPE_BUY, mOrderId);
//		mTask = new FechDataTask();
//		try {
//			mTask.execute(MODE_ONLINE_PAY);
//		} catch (RejectedExecutionException e) {
//			e.printStackTrace();
//		}
	}
		
    private void balancePay(){
    	mTask = new FechDataTask();
    	try {
			mTask.execute(MODE_BALANCE_PAY);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
    }
    
    private void paySuccess(){
		Intent i = new Intent();
		i.setAction(Constants.PAY_SUCCESS_ACTION);
		i.putExtra(Constants.EXTRA_MODE, SuccessActivity.MODE_PAY_SUCCESS);
		i.putExtra(Constants.EXTRA_AMOUNT, mTotalPrice);
		i.putExtra(Constants.EXTRA_ORDERID, mOrderId);
		setResult(Activity.RESULT_OK, i);
		sendBroadcast(i);
		Utils.showToast(ProductPayActivity.this, R.string.pay_success, Toast.LENGTH_SHORT);
		finish();
    }
    
	private class FechDataTask extends AsyncTask<Integer, Void, String>{
	    private Throwable mThr;
	    private int mMode;

		@Override
		protected String doInBackground(Integer... params) {
			mMode = params[0];
			String result = "";
			switch(mMode){
			case MODE_GETINFO:{
				try {
					result = NetEngine.getInstance(ProductPayActivity.this).
								getUserInfo(Utils.getMyUid(ProductPayActivity.this));
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
				break;
			}
			case MODE_ONLINE_PAY:{
				
//				try {
//					result = NetEngine.getInstance(ProductPayActivity.this)
//							.prepareRecharge(Utils.getPassport(ProductPayActivity.this), RechargeActivity.BUSI_PARTNER, 
//									mProduct.getName(), PayOrder.SIGN_TYPE_MD5, 
//									mProduct.getPrice()*mNumber+"", constructRiskItem());
//				} catch (PediatricsIOException e) {
//					mThr=e;
//					e.printStackTrace();
//				} catch (PediatricsParseException e) {
//					mThr=e;
//					e.printStackTrace();
//				} catch (PediatricsApiException e) {
//					mThr=e;
//					e.printStackTrace();
//				}
//				break;
			}
			case MODE_BALANCE_PAY:{
				try {
					result = NetEngine.getInstance(ProductPayActivity.this).
								payProduct(mOrderId, mProduct.getId()+"", mOwnerId, mNumber, 
										mTotalPrice, 0);
				} catch (PediatricsIOException e) {
					mThr=e;
					e.printStackTrace();
				} catch (PediatricsParseException e) {
					mThr=e;
					e.printStackTrace();
				} catch (PediatricsApiException e) {
					mThr=e;
					e.printStackTrace();
				}
				break;
			}
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			isTaskFree = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			isTaskFree = true;
			if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, ProductPayActivity.this);
				}else{
					Utils.showToast(ProductPayActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			switch(mMode){
			case MODE_GETINFO:{
				try {
					JSONObject obj = new JSONObject(result);
					mUser = new IeetonUser(ProductPayActivity.this, obj);
					mBalance = mUser.getBalance();
					showBalance();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
			case MODE_ONLINE_PAY:{
				break;
			}
			case MODE_BALANCE_PAY:{
				JSONObject object;
				try {
					object = new JSONObject(result);					
					if ("true".equals(object.optString("Success"))){
						paySuccess();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				break;
			}
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			isTaskFree = false;
			super.onPreExecute();
		}
		
	}
}
