package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.City;
import com.ieeton.user.models.Product;
import com.ieeton.user.models.SignInfo;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.IntegralProductItemView;
import com.ieeton.user.view.PullDownView;
import com.ieeton.user.view.PullDownView.UpdateHandle;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SignActivity extends TemplateActivity {
	private class IntegralProductAdapter extends BaseAdapter{

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
		public View getView(final int position, View convertView, ViewGroup parent) {
			IntegralProductItemView view ;
			if (convertView == null){
				view = new IntegralProductItemView(SignActivity.this);
			}else{
				view = (IntegralProductItemView) convertView;
			}
			if (mList != null && !mList.isEmpty() && position < mList.size()){
				view.update(mList.get(position));
				view.getBuyBtn().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Product product = mList.get(position);
						if (product == null || mSignInfo == null){
							return;
						}
						if (mSignInfo.getIntegral() < product.getIntegral()){
							Utils.showToast(SignActivity.this, R.string.integral_not_enough, Toast.LENGTH_SHORT);
							return;
						}
						Intent intent = new Intent(SignActivity.this, GenerateOrderActivity.class);
						intent.putExtra(Constants.EXTRA_UID, product.getOwnerUid());
						intent.putExtra(Constants.EXTRA_PRODUCT, product);
						startActivityForResult(intent, ProductDetailActivity.REQUEST_BUY);				
					}
				});
			}
			return view;
		}
		
	}
	
	private class FechDataTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			int page = params[0];
			try {
				int cityid = mCity == null ? -1 : mCity.getCityID();
				result = NetEngine.getInstance(SignActivity.this)
							.getIntegralProduct(cityid, Utils.getMyLocationLon(SignActivity.this),
							Utils.getMyLocationLa(SignActivity.this),page);
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
			mIsTaskFree = true;
			//dismissProgress();
			if (mLoadMode == LOAD_MORE){
				mPageNum--;
			}
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			//dismissProgress();
			mPullDownView.endUpdate(null);
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(SignActivity.this, R.string.no_data, Toast.LENGTH_SHORT);
				}
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
				}
				return;
			}
			processResult(result);
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			//showProgress();
			super.onPreExecute();
		}
		
	}
	
	private class SignTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;
		private int action;
		public static final int ACTION_GET_SIGNINFO = 1;
		public static final int ACTION_SIGN = 2;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			action = params[0];
			try {
				if (action == ACTION_GET_SIGNINFO){
					result = NetEngine.getInstance(SignActivity.this).getSignInfo();
				}else if (action == ACTION_SIGN){
					result = NetEngine.getInstance(SignActivity.this).sign(mSignInfo.getNextIntegral());
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
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(SignActivity.this, R.string.no_data, Toast.LENGTH_SHORT);
				}
				return;
			}
			if (action == ACTION_GET_SIGNINFO){
				try {
					JSONObject obj = new JSONObject(result);
					mSignInfo = new SignInfo(obj);
					updateSignInfo();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else if (action == ACTION_SIGN){
				Utils.showToast(SignActivity.this, R.string.sign_success, Toast.LENGTH_SHORT);
				mSignTask = new SignTask();
				mSignTask.execute(ACTION_GET_SIGNINFO);
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private final int LOAD_REFRESH = 0;
	private final int LOAD_MORE = 1;

	private TextView mTvName;
	private TextView mTvSign;
	private TextView mTvIntegral;
	private TextView mBtnSign;
	private TextView mTvRule;
	
	private SignInfo mSignInfo;
	private List<Product> mList;
	private PullDownView mPullDownView;
	private ListView mListView;
	private IntegralProductAdapter mAdapter;
	private int mPageNum = 1;
	private boolean mIsTaskFree = true;
	private int mLoadMode = LOAD_REFRESH;
	private FechDataTask mTask;
	private SignTask mSignTask;
	private City mCity;
	
	
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
		setView(R.layout.activity_sign);
		setTitleBar(getString(R.string.back), getString(R.string.category5), null);
		mCity = (City) getIntent().getSerializableExtra(Constants.EXTRA_CITY);

		mPullDownView = (PullDownView) findViewById(R.id.pulldown_view);
		mPullDownView.setUpdateHandle(new UpdateHandle() {			
			@Override
			public void onUpdate() {
				refreshData(LOAD_REFRESH);
			}
		});
		
		mTvName = (TextView) findViewById(R.id.tv_name);
		mTvSign = (TextView) findViewById(R.id.tv_sign);
		mTvIntegral = (TextView) findViewById(R.id.tv_integral);
		mBtnSign = (TextView) findViewById(R.id.btn_sign);
		mBtnSign.setOnClickListener(this);
		mTvRule = (TextView) findViewById(R.id.tv_rule);
		mTvRule.setOnClickListener(this);
		
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new IntegralProductAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long arg3) {
				Product product = mList.get(position);
				Intent intent = new Intent(SignActivity.this, ProductDetailActivity.class);
				intent.putExtra(Constants.EXTRA_PRODUCT, product);
				intent.putExtra(Constants.EXTRA_PRODUCTID, product.getId()+"");
				intent.putExtra(Constants.EXTRA_UID, product.getOwnerUid());
				intent.putExtra(Constants.EXTRA_CATEGORYID, product.getCategoryId());
				intent.putExtra(Constants.EXTRA_IS_INTEGRAL, true);
				startActivity(intent);
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {
			private boolean retrievable;
			private boolean isEnd;
			protected int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
					isEnd = true;
				} else {
					isEnd = false;
				}
				if (firstVisibleItem == 0) {
					mScrollState = SCROLL_STATE_IDLE;
				}


				if (firstVisibleItem
								+ visibleItemCount >= totalItemCount - 1) 
				{
					retrievable = true;
				} else {
					retrievable = false;
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (isEnd) {
					mScrollState = SCROLL_STATE_IDLE;
				} else {
					mScrollState = scrollState;
				}
				if (scrollState == SCROLL_STATE_IDLE && retrievable) {
					retrievable = false;
					refreshData(LOAD_MORE);
				}
				
			}
		});
		
		mSignTask = new SignTask();
		mSignTask.execute(SignTask.ACTION_GET_SIGNINFO);
		if (mList == null || mList.isEmpty()){
			refreshData(LOAD_REFRESH);
		}
	}
	
	private void updateSignInfo(){
		if (mSignInfo == null){
			return;
		}
		mTvName.setText(mSignInfo.getName());
		mTvIntegral.setText(mSignInfo.getIntegral()+"");
		mTvSign.setText(String.format(getString(R.string.serial_sign), mSignInfo.getSerialDay()));
		if (mSignInfo.isSigned()){
			mBtnSign.setTextColor(getResources().getColor(R.color.color_gray));
			mBtnSign.setText(getString(R.string.had_signed));
		}else{
			mBtnSign.setTextColor(getResources().getColor(R.color.ieeton_color_blue));
			mBtnSign.setText(getString(R.string.category5) + "+" + mSignInfo.getNextIntegral());
		}
	}
	
	private void refreshData(int mode){
		mLoadMode = mode;
		if (!mIsTaskFree){
			return;
		}
		if (mode == LOAD_REFRESH){
			mPageNum = 1;
			mPullDownView.update();
		}else{
			mPageNum += 1;
			mPullDownView.updateWithoutOffset();
		}
		mTask = new FechDataTask();
		try {
			mTask.execute(mPageNum);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnSign){
			if (mSignInfo == null || mSignInfo.isSigned()){
				return;
			}
			mSignTask = new SignTask();
			mSignTask.execute(SignTask.ACTION_SIGN);
		}else if (v == mTvRule){
			Intent intent = new Intent(this, BrowserActivity.class);
			intent.putExtra(Constants.WEB_BROWSER_URL, 
					Constants.SERVER_HOST_INTEGRAL_URL);
			intent.putExtra(Constants.WEB_BROWSER_INTERNAL, "false");
			intent.putExtra(Constants.WEB_BROWSER_TITLE, getString(R.string.integral_rule));
			intent.putExtra(BrowserActivity.EXTRA_SHOW_BOTTOM, "false");
			startActivity(intent);
		}
		super.onClick(v);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK){
			if (requestCode == ProductDetailActivity.REQUEST_BUY){
				mSignTask = new SignTask();
				mSignTask.execute(SignTask.ACTION_GET_SIGNINFO);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		if (mSignTask != null && mSignTask.getStatus() == AsyncTask.Status.RUNNING){
			mSignTask.cancel(true);
		}
		super.onDestroy();
	}

	private void processResult(String result){
		if (TextUtils.isEmpty(result)){
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			JSONArray array = obj.optJSONArray("productinfo");
			if (array == null || array.length() == 0){
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
					Utils.showToast(this, R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					//showEmpty(true);
				}
				return;
			}
			if (mList == null){
				mList = new ArrayList<Product>();
			}
			List<Product> list = new ArrayList<Product>();
			for(int i=0; i<array.length(); i++){
				Product item = new Product(array.optJSONObject(i));
				list.add(item);
			}
			if (mLoadMode == LOAD_REFRESH){
				mList.clear();
			}
			mList.addAll(list);
			mAdapter.notifyDataSetChanged();
			if (mList.size()>0){
				//showEmpty(false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}
