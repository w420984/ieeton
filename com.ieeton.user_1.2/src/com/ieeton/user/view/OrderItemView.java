package com.ieeton.user.view;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.activity.AddCommentActivity;
import com.ieeton.user.activity.BrowserActivity;
import com.ieeton.user.activity.GenerateOrderActivity;
import com.ieeton.user.activity.ProductDetailActivity;
import com.ieeton.user.activity.ProductPayActivity;
import com.ieeton.user.activity.SignActivity;
import com.ieeton.user.activity.SubscribeInfoActivity;
import com.ieeton.user.activity.SuccessActivity;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.models.Order;
import com.ieeton.user.models.Product;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.RechargeHelper;
import com.ieeton.user.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OrderItemView extends LinearLayout implements android.view.View.OnClickListener{
	private Context mContext;
	private TextView mTvName;
	private TextView mTvDate;
	private TextView mTvNumber;
	private TextView mTvPrice;
	private ImageView mIvIcon;
	private ImageView mIvStatus;
	private TextView mBtn1;
	private TextView mBtn2;
	private TextView mBtn3;
	private TextView mBtn4;
	private Order mOrder;
	private CustomToast mProgressDialog;
	private OperationTask mTask;
	private boolean isTaskFree = true;
	
	public OrderItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public OrderItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public OrderItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	private void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.order_item_view, this);
		
		mTvName = (TextView) findViewById(R.id.tv_name);
		mTvDate = (TextView) findViewById(R.id.date);
		mTvNumber = (TextView) findViewById(R.id.number);
		mTvPrice = (TextView) findViewById(R.id.total_price);
		mIvStatus = (ImageView) findViewById(R.id.iv_status);
		mIvIcon = (ImageView) findViewById(R.id.iv_icon);
		mBtn1 = (TextView) findViewById(R.id.btn_1);
		mBtn1.setOnClickListener(this);
		mBtn2 = (TextView) findViewById(R.id.btn_2);
		mBtn2.setOnClickListener(this);
		mBtn3 = (TextView) findViewById(R.id.btn_3);
		mBtn3.setOnClickListener(this);
		mBtn4 = (TextView) findViewById(R.id.btn_4);
		mBtn4.setOnClickListener(this);
		
	}
	
	public void update(Order order){
		if (order == null){
			return;
		}
		mOrder = order;
		mTvName.setText(order.getProduct().getName());
		//String date = getTime(order.getDate());
		//date = date.substring(0, date.indexOf(" "));
		mTvDate.setText(getResources().getString(R.string.order_date)+order.getDate());
		mTvNumber.setText(getResources().getString(R.string.order_number)+order.getProductCount());
		String price;
		if (order.getIntegral() > 0){
			price = order.getIntegral() + getResources().getString(R.string.integral);
		}else{
			price = order.getAmount() > 0 ? "¥" + order.getAmount() : getResources().getString(R.string.price_free);
		}
		mTvPrice.setText(getResources().getString(R.string.order_total)+price);
						
		if (!TextUtils.isEmpty(order.getProduct().getProductionUrl())){
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvIcon, 
					NetEngine.getImageUrl(order.getProduct().getProductionUrl()));
		}
		
		mIvStatus.setVisibility(View.GONE);
		mBtn2.setVisibility(View.GONE);
		mBtn3.setTextColor(getResources().getColor(R.color.color_black));
		mBtn3.setBackgroundResource(R.drawable.indent_button);
		mBtn4.setVisibility(View.GONE);
		if (order.getStatus() == 0){
			//未支付			取消订单   支付
			mBtn2.setVisibility(View.VISIBLE);
			mBtn2.setText(getResources().getString(R.string.cancel_order));
			mBtn3.setText(getResources().getString(R.string.pay_product));
			mBtn3.setTextColor(getResources().getColor(R.color.ieeton_color_red));
			mBtn3.setBackgroundResource(R.drawable.indent_button_red);
		}else if (order.getStatus() == 1){
			//已支付未预约    预约
			mBtn3.setText(getResources().getString(R.string.subscribe));
		}else if (order.getStatus() == 2){
			//已支付正在预约			取消预约  正在预约
			//mBtn2.setVisibility(View.VISIBLE);
			//mBtn2.setText(getResources().getString(R.string.cancel_subscribe));
			mBtn3.setText(getResources().getString(R.string.subscribe_now));
		}else if (order.getStatus() == 3){
			//已支付已预约			评价  删除订单 查看预约
			mIvStatus.setVisibility(View.VISIBLE);
			mIvStatus.setImageResource(R.drawable.finish);
			mBtn4.setVisibility(View.VISIBLE);
			Utils.logd("order.isComment():"+order.isComment());
			if (order.isComment() == 0){
				mBtn2.setVisibility(View.VISIBLE);
				mBtn2.setText(getResources().getString(R.string.comment_now));
			}
			mBtn3.setText(getResources().getString(R.string.delete_order));
			mBtn4.setText(getResources().getString(R.string.check_subscribe_info));
		}else if (order.getStatus() == 4){
			//取消订单状态		删除订单
			mBtn3.setText(getResources().getString(R.string.delete_order));
		}
	}
	
	private String getTime(String serverTime){
		String time = "";
		if (TextUtils.isEmpty(serverTime)){
			return time;
		}
		serverTime = serverTime.replaceAll("-", ".")
						.replaceAll("T", " ");
		int end = serverTime.lastIndexOf(":");
		if (end > 0){
			time = serverTime.substring(0, end);
		}
		return time;
	}

	@Override
	public void onClick(View v) {
		if (v == mBtn1){
			//联系商家
			String number = NetEngine.getIvrNumber();
			if(number != null && !"".equals(number)){
				Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number));
				mContext.startActivity(intent);
			}
		}else if (v == mBtn2){
			String text = mBtn2.getText().toString();
			click(text);
		}else if (v == mBtn3){
			String text = mBtn3.getText().toString();
			click(text);
		}else if (v == mBtn4){
			String text = mBtn4.getText().toString();
			click(text);
		}
	}
	
	private void excuteTask(int action){
		if (!isTaskFree){
			return;
		}
		mTask = new OperationTask();
		try {
			mTask.execute(action);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private void click(String text){
		if (getResources().getString(R.string.cancel_order).equals(text)){
			//取消订单
			excuteTask(CANCEL_ORDER);
		}else if (getResources().getString(R.string.pay_product).equals(text)){
			//支付
			if (mOrder.getIntegral() > 0){
				excuteTask(PAYPRODUCT);
			}else{
//				Intent intent = new Intent(mContext, ProductPayActivity.class);
//				intent.putExtra(Constants.EXTRA_PRODUCT, mOrder.getProduct());
//				intent.putExtra(Constants.EXTRA_AMOUNT, mOrder.getAmount());
//				intent.putExtra(Constants.EXTRA_ORDERID, mOrder.getOrderId());
//				intent.putExtra(Constants.EXTRA_NUMBER, mOrder.getProductCount());
//				intent.putExtra(Constants.EXTRA_UID, mOrder.getProduct().getOwnerUid());
//				mContext.startActivity(intent);
				
				Intent intent = new Intent(mContext, GenerateOrderActivity.class);
				intent.putExtra(Constants.EXTRA_UID, mOrder.getProduct().getOwnerUid());
				intent.putExtra(Constants.EXTRA_PRODUCT, mOrder.getProduct());
				intent.putExtra(Constants.EXTRA_ORDERID, mOrder.getOrderId());
				mContext.startActivity(intent);
			}
			//excuteTask(GET_BALANCE);
		}else if (getResources().getString(R.string.subscribe).equals(text)){
			//预约
			Utils.subscribe(mContext);
//			Intent intent = new Intent(mContext, BrowserActivity.class);
//			intent.putExtra(Constants.WEB_BROWSER_URL, 
//					Constants.SERVER_HOST_SUBSCRIBE_URL+"?internalId="+mOrder.getOrderId());
//			intent.putExtra(Constants.WEB_BROWSER_INTERNAL, "false");
//			intent.putExtra(Constants.WEB_BROWSER_TITLE, mContext.getString(R.string.subscribe));
//			intent.putExtra(BrowserActivity.EXTRA_SHOW_BOTTOM, "false");
//			intent.putExtra(Constants.EXTRA_ORDERID, mOrder.getOrderId());
//			mContext.startActivity(intent);
		}else if (getResources().getString(R.string.subscribe_now).equals(text)
				|| getResources().getString(R.string.check_subscribe_info).equals(text)){
			//正在预约
			Intent intent = new Intent(mContext, SubscribeInfoActivity.class);
			intent.putExtra(Constants.EXTRA_ORDERID, mOrder.getOrderId());
			intent.putExtra(Constants.EXTRA_PRODUCT, mOrder.getProduct());
			mContext.startActivity(intent);			
		}else if (getResources().getString(R.string.cancel_subscribe).equals(text)){
			//取消预约
			excuteTask(CANCEL_SUBSCRIBE);
		}else if (getResources().getString(R.string.comment_now).equals(text)){
			//评价
			Intent intent = new Intent(mContext, AddCommentActivity.class);
			intent.putExtra(Constants.EXTRA_PRODUCT, mOrder.getProduct());
			intent.putExtra(Constants.EXTRA_ORDERID, mOrder.getOrderId());
			mContext.startActivity(intent);
		}else if (getResources().getString(R.string.delete_order).equals(text)){
			//删除订单
			excuteTask(DELETE_ORDER);
		}
	}
	
	private final int CANCEL_ORDER = 1;			//取消订单
	private final int CANCEL_SUBSCRIBE = 2;		//取消预约
	private final int DELETE_ORDER = 3;			//删除订单
	private final int GET_BALANCE = 4;			//获取账号余额
	private final int PAYPRODUCT = 5;			//积分兑换
	
	private class OperationTask extends AsyncTask<Integer, Void, String>{
	    private Throwable mThr;
	    private int action;

		@Override
		protected String doInBackground(Integer... params) {
			action = params[0];
			String result = "";
			try {
				if (action == CANCEL_ORDER){
					result = NetEngine.getInstance(mContext).cancelOrder(mOrder.getOrderId());
				}else if (action == CANCEL_SUBSCRIBE){
					result = NetEngine.getInstance(mContext).cancelSubscribe(mOrder.getOrderId());
				}else if (action == DELETE_ORDER){
					result = NetEngine.getInstance(mContext).deleteOrder(mOrder.getOrderId());
				}else if (action == GET_BALANCE){
					result = NetEngine.getInstance(mContext).getUserInfo(Utils.getMyUid(mContext));
				}else if (action == PAYPRODUCT){
					result = NetEngine.getInstance(mContext).payProduct(mOrder.getOrderId(), 
							mOrder.getProduct().getId()+"", mOrder.getProduct().getOwnerUid(), 
							mOrder.getProductCount(), mOrder.getAmount(), mOrder.getIntegral());
				}
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
			isTaskFree = true;
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			isTaskFree = true;
			dismissProgress();
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, mContext);
				}else{
					Utils.showToast(mContext, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			if (action == CANCEL_ORDER || action == CANCEL_SUBSCRIBE
						|| action == DELETE_ORDER || action == PAYPRODUCT){
				Intent intent = new Intent(Constants.ACTION_REFRESH_ORDER_LIST);
				mContext.sendBroadcast(intent);				
			}else if (action == GET_BALANCE){
				try {
					JSONObject obj = new JSONObject(result);
					IeetonUser user = new IeetonUser(mContext, obj);
					
					int balance = user.getBalance();
					int myIntegral = user.getIntegral();
					if (mOrder.getIntegral() > 0){
						if (myIntegral >= mOrder.getIntegral()){
							excuteTask(PAYPRODUCT);
						}else{
							Utils.showToast(mContext, "积分不足", Toast.LENGTH_SHORT);
						}
					}else{
						if (balance >= mOrder.getAmount()){
							excuteTask(PAYPRODUCT);
						}else{
							RechargeHelper.getInstance(mContext).
								WXPay(mOrder.getProduct().getName(), mOrder.getAmount()*100, 
										RechargeHelper.TYPE_BUY, mOrder.getOrderId());
						}
					}
				}catch (JSONException e){
					e.printStackTrace();
				}
				
			}
		}

		@Override
		protected void onPreExecute() {
			isTaskFree = false;
			showProgress();
			super.onPreExecute();
		}
		
	}
		
	protected void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, mContext);
		}
		mProgressDialog.show();
	}
	
	protected void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}
}
