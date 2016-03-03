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
import com.ieeton.user.models.Order;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.OrderItemView;
import com.ieeton.user.view.PullDownView;
import com.ieeton.user.view.PullDownView.UpdateHandle;

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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class OrdersActivity extends TemplateActivity implements 
	UpdateHandle, OnScrollListener{
	private final int LOAD_REFRESH = 0;
	private final int LOAD_MORE = 1;
	
	private PullDownView mPullDownView;
	private ListView mListView;
	private OrdersAdapter mAdapter;
	private int mPageNum = 1;
	private FechDataTask mTask;
	private boolean mIsTaskFree = true;
	private int mLoadMode = LOAD_REFRESH;
	private BroadcastReceiver mReceiver;
	
	private List<Order> mList ;
	
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
		setView(R.layout.activity_my_orders);
		setTitleBar(getString(R.string.back), getString(R.string.my_orders), null);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.PAY_SUCCESS_ACTION);
		filter.addAction(Constants.ACTION_REFRESH_ORDER_LIST);
		mReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Constants.PAY_SUCCESS_ACTION.equals(intent.getAction())
						|| Constants.ACTION_REFRESH_ORDER_LIST.equals(intent.getAction())){
					refreshData(LOAD_REFRESH);
				}
			}			
		};
		registerReceiver(mReceiver, filter);
				
		mPullDownView = (PullDownView) findViewById(R.id.pulldown_view);
		mPullDownView.setUpdateHandle(this);
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new OrdersAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long arg3) {
				if (mList != null && !mList.isEmpty() && position < mList.size()){
					Order order = (Order) mList.get(position);
					Intent intent = new Intent(OrdersActivity.this, ProductDetailActivity.class);
					intent.putExtra(Constants.EXTRA_PRODUCT, order.getProduct());
					intent.putExtra(Constants.EXTRA_PRODUCTID, order.getProduct().getId()+"");
					intent.putExtra(Constants.EXTRA_UID, order.getProduct().getOwnerUid());
					intent.putExtra(Constants.EXTRA_CATEGORYID, order.getProduct().getCategoryId());
					if (order.getIntegral() > 0){
						intent.putExtra(Constants.EXTRA_IS_INTEGRAL, true);
					}
					startActivity(intent);
				}
			}
		});
		
		if (mList == null || mList.isEmpty()){
			refreshData(LOAD_REFRESH);
		}
	}
		
	private void refreshData(int mode){
		mLoadMode = mode;
		if (!mIsTaskFree){
			return;
		}
		showEmpty(false);
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
	
	private void processResult(String result){
		if (TextUtils.isEmpty(result)){
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			JSONArray array = obj.optJSONArray("orders");

			if (array == null || array.length() == 0){
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
					Utils.showToast(this, R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					mAdapter.notifyDataSetChanged();
					showEmpty(true);
				}
				return;
			}
			if (mList == null){
				mList = new ArrayList<Order>();
			}
			List<Order> list = new ArrayList<Order>();
			for(int i=0; i<array.length(); i++){
				Order item = new Order(array.optJSONObject(i));
				list.add(item);
			}
			if (mLoadMode == LOAD_REFRESH){
				mList.clear();
			}
			mList.addAll(list);
			mAdapter.notifyDataSetChanged();
			if (mList.size()>0){
				showEmpty(false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void showEmpty(boolean isEmpty){
		if (isEmpty){
			mPullDownView.setVisibility(View.GONE);
			findViewById(R.id.iv_empty).setVisibility(View.VISIBLE);
		}else{
			mPullDownView.setVisibility(View.VISIBLE);
			findViewById(R.id.iv_empty).setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onDestroy() {
		if (mReceiver != null){
			unregisterReceiver(mReceiver);
		}
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ProductDetailActivity.REQUEST_BUY){
			if (resultCode == Activity.RESULT_OK){
				refreshData(LOAD_REFRESH);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	class OrdersAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			if (mList != null && !mList.isEmpty()){
				return mList.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			OrderItemView view;
			if (convertView == null){
				view = new OrderItemView(OrdersActivity.this);
			}else{
				view = (OrderItemView) convertView;
			}
			if (mList != null && !mList.isEmpty() && position < mList.size()){
				view.update(mList.get(position));
			}
			return view;
		}
	}

	@Override
	public void onUpdate() {
		refreshData(LOAD_REFRESH);
	}

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
	
	class FechDataTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			int page = params[0];
			try {
				result = NetEngine.getInstance(OrdersActivity.this).
						getOrders(10, page);
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
			if (mLoadMode == LOAD_MORE){
				mPageNum--;
			}
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			mPullDownView.endUpdate(null);
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(OrdersActivity.this, R.string.no_data, Toast.LENGTH_SHORT);
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
			super.onPreExecute();
		}
		
	}
}
