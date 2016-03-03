package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.calendar.CalendarCard.OnCellClickListener;
import com.ieeton.user.calendar.CustomDate;
import com.ieeton.user.calendar.MyCalendarView;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Product;
import com.ieeton.user.models.SubscribeInfo;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.RechargeHelper;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.ListViewForScrollView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GenerateOrderActivity extends TemplateActivity {
	private final int BUY_TYPE_MONEY = 1;
	private final int BUY_TYPE_INTEGRAL = 2;
	private final int BALANCE_PAY = 1;
	private final int ONLINE_PAY = 2;
	
	private ImageView mIvProductPic;
	private TextView mTvProductName;
	private TextView mTvUnitPrice;
	private TextView mTvProductIntroduce;
	private TextView mTvNumber;
	private TextView mTvBalance;
	private ImageView mIvAdd;
	private ImageView mIvSubtract;
	private TextView mTvTotalPrice;
	private TextView mTvGenerate;
	private ViewGroup mVgBalancePay;
	private ViewGroup mVgOnlinePay;
	private ImageView mIvBalancePay;
	private ImageView mIvOnlinePay;
	private ViewGroup mVgPayType;
	
	private ViewGroup mVgDate;
	private TextView mTvDate;
	private ViewGroup mVgCalendar;
	private MyCalendarView mCalendarView;
	private ViewGroup mVgTime;
	private TextView mTvTime;
	private ViewGroup mVgDoctor;
	private EditText mEtMobile;
	private ListViewForScrollView mTimeListView;
	private ViewGroup mVgTimeChoice;
	private TextView mTvAddress;
	
	private int mNumber;
	private Product mProduct;
	private String mUid;
	private GenerateOrderTask mGenerateOrderTask;
	private PayTask mPayTask;
	private GetSubscribeInfoTask mGetSubscribeInfoTask;
	private UpdateSubscribInfoTask mUpdateSubscribInfoTask;
	private String mOrderId;
	private int mBuyType;
	private int mPayType;
	private int mTotalPrice;
	private int mBalance = -1;
	private int mIntegral = -1;
	private SubscribeInfo mSubscribeInfo;
	private List<CustomDate> mDateList; 
	private String mSelectDate;
	private String mSelectTime;
	private String mSelectMobile;
	private String mSubmitDate;
	private List<String> mTimeList;
	private CustomDate mChoiceDate;
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
		setView(R.layout.activity_generate_order);
		initView();
		initDataFromIntent();
		mGetSubscribeInfoTask = new GetSubscribeInfoTask();
		try {
			mGetSubscribeInfoTask.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
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

	@Override
	protected void onDestroy() {
		dismissProgress();
		if (mGenerateOrderTask != null && mGenerateOrderTask.getStatus() == AsyncTask.Status.RUNNING){
			mGenerateOrderTask.cancel(true);
		}
		if (mPayTask != null && mPayTask.getStatus() == AsyncTask.Status.RUNNING){
			mPayTask.cancel(true);
		}
		if (mGetSubscribeInfoTask != null && mGetSubscribeInfoTask.getStatus() == AsyncTask.Status.RUNNING){
			mGetSubscribeInfoTask.cancel(true);
		}
		if (mUpdateSubscribInfoTask != null && mUpdateSubscribInfoTask.getStatus() == AsyncTask.Status.RUNNING){
			mUpdateSubscribInfoTask.cancel(true);
		}
		if (mReceiver != null){
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		super.onDestroy();
	}
	
	private void initView(){
		mIvProductPic = (ImageView) findViewById(R.id.iv_product);
		mTvProductName = (TextView) findViewById(R.id.tv_product_name);
		mTvProductIntroduce = (TextView) findViewById(R.id.tv_introduce);
		mTvUnitPrice = (TextView) findViewById(R.id.tv_unit_price);
		mTvNumber = (TextView) findViewById(R.id.tv_number);
		mTvBalance = (TextView) findViewById(R.id.tv_balance);
		mIvAdd = (ImageView) findViewById(R.id.tv_add);
		mIvSubtract = (ImageView) findViewById(R.id.tv_subtract);
		mTvTotalPrice = (TextView) findViewById(R.id.tv_total_price);
		mTvGenerate = (TextView) findViewById(R.id.tv_generate);
		mEtMobile = (EditText) findViewById(R.id.et_mobile);
		mTimeListView = (ListViewForScrollView) findViewById(R.id.timeList);
		mVgTimeChoice = (ViewGroup) findViewById(R.id.ll_time_choice);
		
		mVgPayType = (ViewGroup) findViewById(R.id.ll_payType);
		mVgBalancePay = (ViewGroup) findViewById(R.id.rl_balance_pay);
		mVgOnlinePay = (ViewGroup) findViewById(R.id.rl_online_pay);
		mIvBalancePay = (ImageView) findViewById(R.id.iv_balance_pay);
		mIvOnlinePay = (ImageView) findViewById(R.id.iv_online_pay);
		mVgBalancePay.setOnClickListener(this);
		mVgOnlinePay.setOnClickListener(this);
		mTvAddress = (TextView) findViewById(R.id.tv_address);
		
		mVgDate = (ViewGroup) findViewById(R.id.rl_day);
		mVgDate.setOnClickListener(this);
		mTvDate = (TextView) findViewById(R.id.tv_day);		
		mVgCalendar = (ViewGroup) findViewById(R.id.rl_calendar);
		mCalendarView = (MyCalendarView) findViewById(R.id.calendarview);
		mVgTime = (ViewGroup) findViewById(R.id.rl_time);
		mVgTime.setOnClickListener(this);
		mTvTime = (TextView) findViewById(R.id.tv_time);
		mCalendarView.setCellClickListener(new OnCellClickListener() {
			
			@Override
			public void clickDate(CustomDate date) {
				mChoiceDate = date;
				mSelectDate = date.year+"年"+date.month+"月"+date.day+"日";
				mSubmitDate = date.year + "-" + date.month + "-" + date.day;
				for(CustomDate item: mDateList){
					if (item.year == mChoiceDate.year 
							&& item.month == mChoiceDate.month 
							&& item.day == mChoiceDate.day){
						List<String> hourlist = item.getHour();
						if (hourlist == null || hourlist.size()>1){
							mSelectTime = "";
						}
						break;
					}
				}
				mVgCalendar.setVisibility(View.GONE);
				mVgTime.setVisibility(View.VISIBLE);
				updateSubscribInfo();
			}
			
			@Override
			public void changeDate(CustomDate date) {
				mCalendarView.getMonthText().setText(date.year + "年" + date.month + "月");
			}
		});
		
		mIvAdd.setOnClickListener(this);
		mIvSubtract.setOnClickListener(this);
		mTvGenerate.setOnClickListener(this);
	}
	
	private void initDataFromIntent(){
		Intent intent = getIntent();
		if (intent == null){
			finish();
			return;
		}
		mProduct = (Product) intent.getSerializableExtra(Constants.EXTRA_PRODUCT);
		mUid = intent.getStringExtra(Constants.EXTRA_UID);
		mOrderId = intent.getStringExtra(Constants.EXTRA_ORDERID);
		if (mProduct == null){
			finish();
			return;
		}
		
		if (mProduct.getIntegral() > 0){
			mBuyType = BUY_TYPE_INTEGRAL;
		}else{
			mBuyType = BUY_TYPE_MONEY;
		}
//		if (mBuyType == BUY_TYPE_INTEGRAL){
//			setTitleBar(getString(R.string.back), getString(R.string.exchange), null);
//			mTvGenerate.setText(getString(R.string.exchange_now));			
//		}else{
//			setTitleBar(getString(R.string.back), getString(R.string.generate_order), null);
//			mTvGenerate.setText(getString(R.string.generate_order));
//		}
		setTitleBar(getString(R.string.back), getString(R.string.confirm_order), null);
		if(mBuyType == BUY_TYPE_INTEGRAL){
			mTvGenerate.setText(getString(R.string.exchange_now));			
		}else{
			mTvGenerate.setText(getString(R.string.pay));			
		}
		mNumber = 1;
		mTvProductName.setText(mProduct.getName());
		mTvProductIntroduce.setText(Html.fromHtml(mProduct.getIntroduce()));
		update();
	}

	private void update(){
		AsyncBitmapLoader.getInstance().loadBitmap(this, mIvProductPic, 
				NetEngine.getImageUrl(mProduct.getProductionUrl()));
		if (mBuyType == BUY_TYPE_INTEGRAL){
			mTvUnitPrice.setText(mProduct.getIntegral()+getString(R.string.integral));			
			mTvTotalPrice.setText(String.format(getString(R.string.total_integral), mProduct.getIntegral()*mNumber));
			mTotalPrice = mProduct.getIntegral()*mNumber;
		}else{
			String price = mProduct.getPrice() > 0 ? "¥" + mProduct.getPrice() : getString(R.string.price_free);
			mTvUnitPrice.setText(price);
			mTvTotalPrice.setText(String.format(getString(R.string.total_money), mProduct.getPrice()*mNumber));			
			mTotalPrice = mProduct.getPrice()*mNumber;
		}
		mTvNumber.setText(mNumber+"");
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
	
	private void updateSubscribInfo(){
//		if (!TextUtils.isEmpty(mSelectDate)){
			mTvDate.setText(mSelectDate);
//		}
//		if (!TextUtils.isEmpty(mSelectTime)){
			mTvTime.setText(mSelectTime);
//		}
//		if (!TextUtils.isEmpty(mSelectMobile)){
			mEtMobile.setText(mSelectMobile);
//		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == mIvAdd){
			mNumber++;
			update();
		}else if (v == mIvSubtract){
			if (mNumber>1){
				mNumber--;
				update();
			}
		}else if (v == mTvGenerate){
			if (Utils.getMyType(this) == 5){
				startActivity(new Intent(this, LoginActivity.class));
				return;
			}
			if (mBalance < 0){
				return;
			}
			if (mBuyType == BUY_TYPE_INTEGRAL){
				if (mIntegral < mTotalPrice){
					Utils.showToast(GenerateOrderActivity.this, "积分不足", Toast.LENGTH_SHORT);
					return;
				}
			}else{
				if (mBalance < mTotalPrice && mPayType == BALANCE_PAY){
					Utils.showToast(GenerateOrderActivity.this, "余额不足", Toast.LENGTH_SHORT);
					return;
				}
			}
			mSelectMobile = mEtMobile.getText().toString();
			if (TextUtils.isEmpty(mSelectDate)){
				Utils.showToast(GenerateOrderActivity.this, "请选择预约日期", Toast.LENGTH_SHORT);
				return;
			}
			if (TextUtils.isEmpty(mSelectTime)){
				Utils.showToast(GenerateOrderActivity.this, "请选择预约时间", Toast.LENGTH_SHORT);
				return;
			}
			if (TextUtils.isEmpty(mSelectMobile) || !Utils.isMobileNumber(mSelectMobile)){
				Utils.showToast(GenerateOrderActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT);
				return;
			}
			mSubmitDate = mSubmitDate + " " + mSelectTime;
			if (TextUtils.isEmpty(mOrderId)){
				mGenerateOrderTask = new GenerateOrderTask();
				try {
					mGenerateOrderTask.execute();
				} catch (RejectedExecutionException e) {
					e.printStackTrace();
				}
			}else{
				if (mSubscribeInfo != null 
						&& mSelectMobile.equals(mSubscribeInfo.getMobile())
						&& mSubmitDate.equals(mSubscribeInfo.getDate())){
					productPay();
				}else{
					mUpdateSubscribInfoTask = new UpdateSubscribInfoTask();
					mUpdateSubscribInfoTask.execute();
				}
			}
		}else if (v == mVgDate){
			if (mDateList == null || mDateList.isEmpty()){
				return;
			}
			if (mVgCalendar.getVisibility() == View.GONE){
				mVgCalendar.setVisibility(View.VISIBLE);
			}else{
				mVgCalendar.setVisibility(View.GONE);
			}
		}else if (v == mVgTime){
			if (mVgTimeChoice.getVisibility() == View.VISIBLE){
				mVgTimeChoice.setVisibility(View.GONE);
			}else{
				for(CustomDate date: mDateList){
					if (date.year == mChoiceDate.year 
							&& date.month == mChoiceDate.month 
							&& date.day == mChoiceDate.day){
						mTimeList = date.getHour();
						break;
					}
				}
				Utils.logd("wwt"+mTimeList);
				if (mTimeList == null || mTimeList.isEmpty()){
					return;
				}
				showTimeChoice(mTimeList);
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
	
	private void showTimeChoice(final List<String> timeList){
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.time_choice_item, R.id.tv, timeList);
		mTimeListView.setAdapter(adapter);
		mVgTimeChoice.setVisibility(View.VISIBLE);
		mTimeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				mSelectTime = timeList.get(position);
				updateSubscribInfo();
				mVgTimeChoice.setVisibility(View.GONE);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ProductDetailActivity.REQUEST_BUY){
			if (resultCode == RESULT_OK){
				if (data != null){
					setResult(RESULT_OK, data);
					finish();
					return;
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void productPay(){
		if (mBuyType == BUY_TYPE_INTEGRAL){
			if (mIntegral >= mTotalPrice){
				pay();
			}else{
				Utils.showToast(GenerateOrderActivity.this, "积分不足", Toast.LENGTH_SHORT);
			}
		}else{
			if (mPayType == BALANCE_PAY){
				pay();						
			}else{
				RechargeHelper.getInstance(GenerateOrderActivity.this).
					WXPay(mProduct.getName(), mTotalPrice*100, RechargeHelper.TYPE_BUY, mOrderId);
			}
		}
	}
	
	private void pay(){
		mPayTask = new PayTask();
		mPayTask.execute();
	}
	
	private void initPayType(){
		if (mBuyType == BUY_TYPE_INTEGRAL){
			mVgPayType.setVisibility(View.GONE);
		}else{
			mVgPayType.setVisibility(View.VISIBLE);
			if (mBalance<mTotalPrice){
				setPayType(ONLINE_PAY);
			}else{
				setPayType(BALANCE_PAY);
			}
		}
	}
	
	private class GenerateOrderTask extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;
		int price; 		//单价
		int amount;		//总价 金额
		int integral;	//总价 积分

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				if (mBuyType == BUY_TYPE_INTEGRAL){
					price = mProduct.getIntegral();
					amount = 0;
					integral = price * mNumber;
				}else{
					price = mProduct.getPrice();
					integral = 0;
					amount = price * mNumber;
				}

				result = NetEngine.getInstance(GenerateOrderActivity.this).
							generateOrder(mProduct.getId()+"", mUid, mNumber, 
									price, amount, integral, mSubmitDate, mSelectMobile);
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
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(GenerateOrderActivity.this, R.string.generate_order_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject obj = null;
			try {
				obj = new JSONObject(result);
				mOrderId = obj.optString("internalId");
				int balance = obj.optJSONObject("user").optInt("balance");
				int myIntegral = obj.optJSONObject("user").optInt("Integral");
				productPay();
			}catch (JSONException e){
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private class PayTask extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;
		int amount;		//总价 金额
		int integral;	//总价 积分

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				if (mBuyType == BUY_TYPE_INTEGRAL){
					amount = 0;
					integral = mProduct.getIntegral() * mNumber;
				}else{
					integral = 0;
					amount = mProduct.getPrice() * mNumber;
				}

					
				result = NetEngine.getInstance(GenerateOrderActivity.this).
							payProduct(mOrderId, mProduct.getId()+"", mUid, mNumber, 
									amount, integral);
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
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(GenerateOrderActivity.this, R.string.generate_order_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			paySuccess();
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private class GetSubscribeInfoTask extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				String ownerid = mProduct.getOwnerUid();
				if (TextUtils.isEmpty(ownerid)){
					ownerid = mProduct.getOwner().getUid();
				}
				result = NetEngine.getInstance(GenerateOrderActivity.this).
							getSubscribeInfo(mOrderId, mProduct.getId()+"", ownerid);
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
					Utils.handleErrorEvent(mThr, GenerateOrderActivity.this);
				}else{
					Utils.showToast(GenerateOrderActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			try {
				JSONObject obj = new JSONObject(result);
				mSubscribeInfo = new SubscribeInfo(obj.optJSONObject("yu"));
				if (mSubscribeInfo != null){
					mTvAddress.setText(mSubscribeInfo.getAddress());
				}
				mBalance = obj.optJSONObject("user").optInt("balance");
				mTvBalance.setText(mBalance+"");
				mIntegral = obj.optJSONObject("user").optInt("Integral");
				initPayType();
				JSONArray dateArray = obj.optJSONArray("time");
				mDateList = new ArrayList<CustomDate>();
				Calendar calendar = Calendar.getInstance();
				CustomDate curDate = new CustomDate(calendar.get(Calendar.YEAR), 
						calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
				for(int i=0; dateArray!=null && dateArray.length()>0 && i<dateArray.length(); i++){
					String date = dateArray.optJSONObject(i).optString("dateTime");
					CustomDate item = convertDate(date);
					if (item.year>=curDate.year && item.month>=curDate.month
							&& item.day>curDate.day){
						mDateList.add(item);
					}
				}
				mCalendarView.setValidDateList(mDateList);
				if (mSubscribeInfo != null && !TextUtils.isEmpty(mSubscribeInfo.getDate())){
					String [] array1 = mSubscribeInfo.getDate().split(" ");
					String date = array1[0];	//得到"2015-06-01"
					String hours = array1[1];	//得到"09-12,14-17"
					String [] array2 = date.split("-");
					mChoiceDate = new CustomDate(Integer.parseInt(array2[0]), 
							Integer.parseInt(array2[1]), 
							Integer.parseInt(array2[2]));
					mSelectDate = mChoiceDate.year+"年"+mChoiceDate.month+"月"+mChoiceDate.day+"日";
					mSelectTime = hours;
					mSelectMobile = mSubscribeInfo.getMobile();
					mVgCalendar.setVisibility(View.GONE);
					mVgTime.setVisibility(View.VISIBLE);
					updateSubscribInfo();
				}else{
					if (mDateList.size() == 1){
						CustomDate date = mDateList.get(0);
						mChoiceDate = date;
						mSelectDate = date.year+"年"+date.month+"月"+date.day+"日";
						mSubmitDate = date.year + "-" + date.month + "-" + date.day;
						mVgCalendar.setVisibility(View.GONE);
						
						if (date.hours.size() == 1){
							mSelectTime = date.hours.get(0);
						}else{
							mSelectTime = "";
						}
						mVgTime.setVisibility(View.VISIBLE);
						updateSubscribInfo();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private class UpdateSubscribInfoTask extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(GenerateOrderActivity.this).
						updateSubscribInfo(mOrderId, mSubmitDate, mSelectMobile);
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
					Utils.handleErrorEvent(mThr, GenerateOrderActivity.this);
				}else{
					Utils.showToast(GenerateOrderActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			productPay();			
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	//将"2015-06-01 09-12,14-17"格式日期转化为日历控件使用的日期
	private CustomDate convertDate(String dateStr){
		if (TextUtils.isEmpty(dateStr)){
			return null;
		}
		String [] array1 = dateStr.split(" ");
		String date = array1[0];	//得到"2015-06-01"
		String hours = array1[1];	//得到"09-12,14-17"
		String [] array2 = date.split("-");
		CustomDate rlt = new CustomDate(Integer.parseInt(array2[0]), 
				Integer.parseInt(array2[1]), Integer.parseInt(array2[2]));
		String [] hour_array = hours.split(",");	//得到"09-12"数组
		List<String> hourList = new ArrayList<String>();
		for(String hour: hour_array){
			hourList.add(hour);
		}
//		if (hour_array.length > 1){
//			for(int i=0; i<hour_array.length; i++){
//				String [] h = hour_array[i].split("-");
//				String str = h[0]+":00";
//				hourList.add(str);
//			}
//		}else{
//			String [] minMax = hour_array[0].split("-");
//			int min = Integer.parseInt(minMax[0]);
//			int max = Integer.parseInt(minMax[1]);
//			for(int i=min; i<=max; i++){
//				String str = i<10 ? "0"+i+":00" : i+":00";
//				hourList.add(str);
//			}
//		}
		rlt.setHour(hourList);
		return rlt;
	}
	
    private void paySuccess(){
		Intent i = new Intent();
		i.setAction(Constants.PAY_SUCCESS_ACTION);
		if (mBuyType == BUY_TYPE_INTEGRAL){
			i.putExtra(Constants.EXTRA_MODE, SuccessActivity.MODE_EXCHANGE_SUCCESS);
		}else{
			i.putExtra(Constants.EXTRA_MODE, SuccessActivity.MODE_PAY_SUCCESS);
			i.putExtra(Constants.EXTRA_AMOUNT, mTotalPrice);
		}
		i.putExtra(Constants.EXTRA_ORDERID, mOrderId);
		setResult(Activity.RESULT_OK, i);
		sendBroadcast(i);
		finish();
    }
}
