package com.ieeton.user.activity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ieeton.user.R;
import com.ieeton.user.activity.TemplateActivity;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Product;
import com.ieeton.user.models.SubscribeInfo;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;


public class SubscribeInfoActivity extends TemplateActivity {
	private ImageView mIvPic;
	private TextView mTvProductName;
	private TextView mTvIntroduce;
	private TextView mTvPrice;
	private TextView mTvAddress;
	private ImageView mIvAddress;
	private ViewGroup mVgAddress;
	private TextView mTvDate;
	private TextView mTvName;
	private TextView mTvPhone;
	private TextView mTvService;
	private ImageView mIvPhone;

	private FetchDataTask mTask;
	private String mOrderId;
	private Product mProduct;
	private SubscribeInfo mSubscribeInfo;
	private Dialog mCallDialog;
	
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
		setView(R.layout.activity_subscribe_info);
		setTitleBar(getString(R.string.back), getString(R.string.subscribe_info), null);
		
		mOrderId = getIntent().getStringExtra(Constants.EXTRA_ORDERID);
		mProduct = (Product) getIntent().getSerializableExtra(Constants.EXTRA_PRODUCT);
		if (TextUtils.isEmpty(mOrderId)){
			finish();
			return;
		}
		
		mIvPic = (ImageView) findViewById(R.id.iv_pic);
		mTvProductName = (TextView) findViewById(R.id.tv_name);
		mTvIntroduce = (TextView) findViewById(R.id.tv_description);
		mTvPrice = (TextView) findViewById(R.id.tv_price);
		mTvAddress = (TextView) findViewById(R.id.tv_address);
		mIvAddress = (ImageView) findViewById(R.id.iv_address);
		mIvAddress.setOnClickListener(this);
		mVgAddress = (ViewGroup) findViewById(R.id.rl_address);
		mVgAddress.setOnClickListener(this);
		mTvDate = (TextView) findViewById(R.id.tv_date);
		mTvName = (TextView) findViewById(R.id.tv_person);
		mTvPhone = (TextView) findViewById(R.id.tv_phone);
		mTvService = (TextView) findViewById(R.id.tv_service);
		mIvPhone = (ImageView) findViewById(R.id.iv_phone);
		mIvPhone.setOnClickListener(this);
		
		mTask = new FetchDataTask();
		mTask.execute();
	}

	private void update(){
		if (mSubscribeInfo == null){
			return;
		}
		AsyncBitmapLoader.getInstance().loadBitmap(this, mIvPic, 
					NetEngine.getImageUrl(mSubscribeInfo.getProduct().getProductionUrl()));
		
		mTvProductName.setText(mSubscribeInfo.getProduct().getName());
		mTvIntroduce.setText(Html.fromHtml(mSubscribeInfo.getProduct().getIntroduce()));
		if (mSubscribeInfo.getProduct().getIntegral()>0){
			mTvPrice.setText(mSubscribeInfo.getProduct().getIntegral()+getString(R.string.integral));			
		}else{
			String price = mSubscribeInfo.getProduct().getPrice() > 0 ? "¥" + mSubscribeInfo.getProduct().getPrice() : getString(R.string.price_free);
			mTvPrice.setText(price);
		}
		mTvAddress.setText("地址："+mSubscribeInfo.getAddress());
		mTvDate.setText("预约时间："+mSubscribeInfo.getDate());
		mTvName.setText("预约人："+mSubscribeInfo.getName());
		mTvPhone.setText(mSubscribeInfo.getMobile());
		mTvService.setText(getString(R.string.contact_us)+NetEngine.getIvrNumber());
		mIvPhone = (ImageView) findViewById(R.id.iv_phone);
	}
	
	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		if (mCallDialog != null){
			mCallDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mIvPhone){
			mCallDialog = Utils.showCallDialog(this, NetEngine.getIvrNumber());
		}else if (v == mIvAddress || v == mVgAddress){
			Intent intent = new Intent(this, BaiduMapActivity.class);
			intent.putExtra("latitude", mSubscribeInfo.getLatitude());
			intent.putExtra("longitude", mSubscribeInfo.getLongitude());
			intent.putExtra("address", mSubscribeInfo.getAddress());
			startActivity(intent);
		}
		super.onClick(v);
	}
	
	private void showEmpty(boolean isEmpty){
		if (isEmpty){
			findViewById(R.id.ll_info).setVisibility(View.GONE);
			findViewById(R.id.iv_empty).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.ll_info).setVisibility(View.VISIBLE);
			findViewById(R.id.iv_empty).setVisibility(View.GONE);
		}
	}

	private class FetchDataTask extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(SubscribeInfoActivity.this).
							getSubscribeInfo(mOrderId, mProduct.getId()+"", mProduct.getOwnerUid());
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
		protected void onCancelled() {
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, SubscribeInfoActivity.this);
				}else{
					Utils.showToast(SubscribeInfoActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				showEmpty(true);
				return;
			}
			try {
				JSONObject obj = new JSONObject(result);
				mSubscribeInfo = new SubscribeInfo(obj.optJSONObject("yu"));
				update();
				showEmpty(false);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
}
