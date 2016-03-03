package com.ieeton.user.activity;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.test.MoreAsserts;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SuccessActivity extends TemplateActivity {
	public static final int MODE_PAY_SUCCESS = 1;			//支付成功
	public static final int MODE_EXCHANGE_SUCCESS = 2;		//兑换成功
	public static final int MODE_SUBSCRIBE_SUBMIT = 3;			//预约提交
	public static final int MODE_SUBSCRIBE_CANCEL = 4;			//预约取消
	public static final int MODE_COMMENT_SUCCESS = 5;		//评论成功
	
	private TextView mTvStr1;
	private TextView mTvStr2;
	private ImageView mIvIcon;
	private TextView mBtnLeft;
	private TextView mBtnRight;
	
	private int mMode;
	private int amount;
	private String mOrderId;

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
		setView(R.layout.activity_process_success);
		
		mMode = getIntent().getIntExtra(Constants.EXTRA_MODE, 0);
		amount = getIntent().getIntExtra(Constants.EXTRA_AMOUNT, 0);
		mOrderId = getIntent().getStringExtra(Constants.EXTRA_ORDERID);
		
		mTvStr1 = (TextView) findViewById(R.id.tv_str1);
		mTvStr2 = (TextView) findViewById(R.id.tv_str2);
		mIvIcon = (ImageView) findViewById(R.id.iv_icon);
		mBtnLeft = (TextView) findViewById(R.id.btn_left);
		mBtnRight = (TextView) findViewById(R.id.btn_right);
		
		mBtnLeft.setOnClickListener(this);
		mBtnRight.setOnClickListener(this);
		
		String title="";
		String str1="";
		String str2="";
		String left="";
		String right="";
		switch(mMode){
		case MODE_PAY_SUCCESS:{
			title = getString(R.string.pay_success_title);
			str1 = String.format(getString(R.string.pay_success_str1), amount);
			left = getString(R.string.pay_success_left);
			right = getString(R.string.pay_success_right);
			mIvIcon.setImageResource(R.drawable.pay);
			break;
		}
		case MODE_EXCHANGE_SUCCESS:{
			title = getString(R.string.exchange_success_title);
			str1 = getString(R.string.exchange_success_str1);
			str2 = getString(R.string.exchange_success_str2);
			left = getString(R.string.pay_success_left);
			right = getString(R.string.pay_success_right);
			mIvIcon.setImageResource(R.drawable.right);
			break;
		}
		case MODE_SUBSCRIBE_SUBMIT:{
			title = getString(R.string.order_submit_title);
			str1 = getString(R.string.order_submit_str1);
			str2 = getString(R.string.order_submit_str2);
			left = getString(R.string.order_submit_left);
			right = getString(R.string.order_submit_right);
			mIvIcon.setImageResource(R.drawable.working);
			break;
		}
		case MODE_SUBSCRIBE_CANCEL:{
			title = getString(R.string.order_cancel_title);
			str1 = getString(R.string.order_cancel_str1);
			left = getString(R.string.order_cancel_left);
			right = getString(R.string.order_cancel_right);
			mIvIcon.setImageResource(R.drawable.hint);
			break;
		}
		case MODE_COMMENT_SUCCESS:{
			title = getString(R.string.comment_success_title);
			str1 = getString(R.string.comment_success_str1);
			str2 = getString(R.string.comment_success_str2);
			left = getString(R.string.comment_success_left);
			right = getString(R.string.comment_success_right);
			mIvIcon.setImageResource(R.drawable.comment);
			break;
		}
		}
		setTitleBar(getString(R.string.back), title, null);
		mTvStr1.setText(str1);
		if (TextUtils.isEmpty(str2)){
			mTvStr2.setVisibility(View.GONE);
		}else{
			mTvStr2.setVisibility(View.GONE);
			mTvStr2.setText(str2);
		}
		mBtnLeft.setText(left);
		mBtnRight.setText(right);
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnLeft){
			switch(mMode){
			case MODE_PAY_SUCCESS:
			case MODE_SUBSCRIBE_CANCEL:
			case MODE_EXCHANGE_SUCCESS:
			case MODE_SUBSCRIBE_SUBMIT:
			case MODE_COMMENT_SUCCESS:{
				//我的订单
				startActivity(new Intent(this, OrdersActivity.class));
				finish();
				break;
			}
			}
		}else if (v == mBtnRight){
			switch(mMode){
			case MODE_PAY_SUCCESS:
			case MODE_SUBSCRIBE_CANCEL:
			case MODE_COMMENT_SUCCESS:{
				//继续逛逛
				Intent intent = new Intent(this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(MainActivity.INPUT_INDEX, MainActivity.INPUT_HOME);
				startActivity(intent);
				finish();
				break;
			}
			case MODE_EXCHANGE_SUCCESS:{
				//立即预约
				Utils.subscribe(this);
				break;
			}
			case MODE_SUBSCRIBE_SUBMIT:{
				new CancelSubscribeTask().execute();
				//取消预约
				break;
			}
			}
		}
		super.onClick(v);
	}

	private class CancelSubscribeTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(SuccessActivity.this).
							cancelSubscribe(mOrderId);
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
					Utils.handleErrorEvent(mThr, SuccessActivity.this);
				}else{
					Utils.showToast(SuccessActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			Intent i = new Intent(SuccessActivity.this, SuccessActivity.class);
			i.putExtra(Constants.EXTRA_MODE, SuccessActivity.MODE_SUBSCRIBE_CANCEL);
			i.putExtra(Constants.EXTRA_ORDERID, mOrderId);
			startActivity(i);
			finish();
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
}
