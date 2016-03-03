package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.ListItem;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.ListItemView;
import com.ieeton.agency.R;
import com.yintong.pay.utils.BaseHelper;
import com.yintong.pay.utils.Constants;
import com.yintong.pay.utils.EnvConstants;
import com.yintong.pay.utils.Md5Algorithm;
import com.yintong.pay.utils.MobileSecurePayer;
import com.yintong.pay.utils.PayOrder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RewardActivity extends TemplateActivity {
	private int MODE_GENERATE_ORDER = 1;
	private int MODE_REWARD = 2;
	private int MODE_RECHARGE = 3;
	
	private TextView mBtnOk;
	private List<ListItem> mList;
	private RewardListAdapter mAdapter;
	private double mMoney;
	private String mUserId;
	private String mOrderId;
	private CustomToast mProgressDialog;
	private RewardTask mTask;
    private Handler mHandler = createHandler();
	public static String BUSI_PARTNER = "101001";

    private Handler createHandler() {
        return new Handler() {
            public void handleMessage(Message msg) {
                String strRet = (String) msg.obj;
                switch (msg.what) {
                    case Constants.RQF_PAY: {
                        JSONObject objContent = BaseHelper.string2JSON(strRet);
                        String retCode = objContent.optString("ret_code");
                        String retMsg = objContent.optString("ret_msg");

                        // 先判断状态码，状态码为 成功或处理中 的需要 验签
                        if (Constants.RET_CODE_SUCCESS.equals(retCode)) {
                            String resulPay = objContent
                                    .optString("result_pay");
                            if (Constants.RESULT_PAY_SUCCESS
                                    .equalsIgnoreCase(resulPay)) {
//                                BaseHelper.showDialog(ProductPayActivity.this, "提示",
//                                        "支付成功，交易状态码：" + retCode,
//                                        android.R.drawable.ic_dialog_alert);
                            	reward(MODE_REWARD);
                            } else {
                                BaseHelper.showDialog(RewardActivity.this, "提示",
                                        retMsg + "，交易状态码:" + retCode,
                                        android.R.drawable.ic_dialog_alert);
                            }
                        } else if (Constants.RET_CODE_PROCESS.equals(retCode)) {
                            String resulPay = objContent.optString("result_pay");
                            if (Constants.RESULT_PAY_PROCESSING
                                    .equalsIgnoreCase(resulPay)) {
                                BaseHelper.showDialog(RewardActivity.this, "提示",
                                        objContent.optString("ret_msg") + "交易状态码："
                                                + retCode,
                                        android.R.drawable.ic_dialog_alert);
                            }

                        } else {
                            BaseHelper.showDialog(RewardActivity.this, "提示", retMsg
                                    + "，交易状态码:" + retCode,
                                    android.R.drawable.ic_dialog_alert);
                        }
                    }
                        break;
                }
                super.handleMessage(msg);
            }
        };

    }

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
		setView(R.layout.reward_activity);
		setTitleBar(getString(R.string.back), getString(R.string.reward_title), null);

		mUserId = getIntent().getStringExtra(com.ieeton.agency.utils.Constants.EXTRA_UID);
		
		mBtnOk = (TextView) findViewById(R.id.tv_ok);
		mBtnOk.setOnClickListener(this);
		ListView listView = (ListView)findViewById(R.id.listview);
		mAdapter = new RewardListAdapter();
		listView.setAdapter(mAdapter);
		mList = new ArrayList<ListItem>();
		String[] array = getResources().getStringArray(R.array.reward_list_item);
		for(int i = 0; i < array.length; i++){
			ListItem item = new ListItem(array[i], false);
			mList.add(item);
		}
		mAdapter.notifyDataSetChanged();
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long aid) {
				String[] moneys = {"1", "2", "5", "10", "20", "30", "40", "50"};

				mMoney = Double.valueOf(moneys[position]);
				for(ListItem item : mList){
					if (item.isChecked()){
						item.setChecked(false);
						break;
					}
				}
				mList.get(position).setChecked(true);
				mAdapter.notifyDataSetChanged();
			}
		});
	}
	
	private void reward(int mode){
		if (mMoney <= 0){
			return;
		}
		try {
			mTask = new RewardTask();
			mTask.execute(mode);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
    private String constructRiskItem() {
        JSONObject mRiskItem = new JSONObject();
        try {
            mRiskItem.put("user_info_bind_phone", Utils.getMyLoginName(this));
            mRiskItem.put("user_info_dt_register", "201407251110120");
            mRiskItem.put("frms_ware_category", "1002");
            mRiskItem.put("user_info_mercht_userno", Utils.getMyUid(this));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mRiskItem.toString();
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, this);
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnOk){
			reward(MODE_GENERATE_ORDER);
		}
		super.onClick(v);
	}

	class RewardTask extends AsyncTask<Integer, Void, String>{
	    private Throwable mThr;
	    private int mMode;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			mMode = params[0];
			try {
				if (mMode == MODE_GENERATE_ORDER){
					Utils.logd("mUserId:"+mUserId);
					Utils.logd("mMoney:"+mMoney);
					result = NetEngine.getInstance(RewardActivity.this).
								prepareRewardUser(mUserId, mMoney);
				}else if (mMode == MODE_RECHARGE){
					result = NetEngine.getInstance(RewardActivity.this)
							.prepareRecharge(Utils.getPassport(RewardActivity.this), BUSI_PARTNER, 
									getString(R.string.reward_user), PayOrder.SIGN_TYPE_MD5, 
									mMoney+"", constructRiskItem());
				}else if (mMode == MODE_REWARD){
					String content = String.format("amount=%s&orderId=%s&userId=%s&key=ieeton.pay", 
							mMoney+"", mOrderId, mUserId);
					String sign = Md5Algorithm.sign(content);
					Utils.logd("content:"+content);
					Utils.logd("sign:"+sign);
					result = NetEngine.getInstance(RewardActivity.this).
							payReward(mUserId, mOrderId, mMoney, sign);
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
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, RewardActivity.this);
				}else{
					Utils.showToast(RewardActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			if (mMode == MODE_GENERATE_ORDER){
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					JSONObject data = obj.optJSONObject("messages").optJSONObject("data");
					mOrderId = data.optString("rewardOrderId");
					double balance = data.optJSONObject("user").optDouble("balance");
					if (TextUtils.isEmpty(mOrderId)){
						return;
					}
					int mode = balance >= mMoney ? MODE_REWARD : MODE_RECHARGE;
                	reward(mode);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else if (mMode == MODE_RECHARGE){
				PayOrder mOrder = new PayOrder();
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					JSONObject data = obj.optJSONObject("messages")
							.optJSONObject("data");
					
			        // busi_partner 是指商户的业务类型，"101001"为虚拟商品销售，详情请参考接口说明书
					mOrder.setBusi_partner(data.optString("busi_partner"));
			        // 商户订单
					mOrder.setNo_order(data.optString("no_order"));
					//订单时间
					mOrder.setDt_order(data.optString("dt_order"));
					//产品名称
			        mOrder.setName_goods(getString(R.string.reward_user));
			        //充值成功后的通知链接
			        mOrder.setNotify_url(data.optString("notify_url"));
			        // MD5 签名方式
			        mOrder.setSign_type(data.optString("sign_type"));
			        // RSA 签名方式
			        // order.setSign_type(PayOrder.SIGN_TYPE_RSA);
			        //有效期， 默认7天 ，单位：分钟
			        //mOrder.setValid_order("100");
			        
			        mOrder.setUser_id(Utils.getMyUid(RewardActivity.this));
			        //充值金额
			        mOrder.setMoney_order(mMoney+"");
			        //是否可修改  默认可修改   0表示可修改 1表示不可修改
			        mOrder.setFlag_modify("0");		        
			        // 风险控制参数。
			        mOrder.setRisk_item(data.optString("risk_item"));
			        //商户号
			        mOrder.setOid_partner(EnvConstants.PARTNER);
			        //签名串
			        mOrder.setSign(data.optString("sign"));
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}
				
	            String content4Pay = BaseHelper.toJSONString(mOrder);
	            // 关键 content4Pay 用于提交到支付SDK的订单支付串，如遇到签名错误的情况，请将该信息帖给我们的技术支持
	            Utils.logd("content4Pay:"+content4Pay);
	            MobileSecurePayer msp = new MobileSecurePayer();
	            boolean bRet = msp.pay(content4Pay, mHandler,
	                    Constants.RQF_PAY, RewardActivity.this, false);

	            Utils.logd("bRet:"+String.valueOf(bRet));
			}else if (mMode == MODE_REWARD){
				JSONObject object;
				try {
					object = new JSONObject(result);
					JSONObject json_data = object.optJSONObject("messages").optJSONObject("data");
					if ("success".equals(json_data.opt("result"))){
						Utils.showToast(RewardActivity.this, getString(R.string.reward_success), Toast.LENGTH_SHORT);
						finish();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	class RewardListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mList != null && !mList.isEmpty()){
				return mList.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListItemView view = null;
			if (convertView == null){
				view = new ListItemView(RewardActivity.this);
			}else{
				view = (ListItemView)convertView;
			}
			
			view.update(mList.get(position));

			return view;
		}
	}
}
