package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.AccountDetailItem;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.AccountDetailItemView;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.PullDownView;
import com.ieeton.agency.view.PullDownView.UpdateHandle;
import com.ieeton.agency.R;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AccountDetailActivity extends TemplateActivity
				implements UpdateHandle, OnScrollListener{
	class GetAccountDetailsTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			int page = params[0];
			try {
				result = NetEngine.getInstance(AccountDetailActivity.this).
								getAccountDetails(Utils.getPassport(AccountDetailActivity.this), 
										20, page);
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
			dismissProgress();
			if (mLoadMode == LOAD_MORE){
				mPageNum--;
			}
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			dismissProgress();
			mPullDownView.endUpdate(null);
			
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(AccountDetailActivity.this, R.string.get_account_details_failed, Toast.LENGTH_SHORT);
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
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	class AccountDetailAdapter extends BaseAdapter{

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
			AccountDetailItemView view;
			if (convertView == null){
				view = new AccountDetailItemView(AccountDetailActivity.this);
			}else{
				view = (AccountDetailItemView) convertView;
			}
			if (mList != null && mList.size() > 0 && position < mList.size()){
				view.update((AccountDetailItem) mList.get(position));
			}
			return view;
		}
		
	}
	
	public static final int LOAD_REFRESH = 0;
	public static final int LOAD_MORE = 1;

	private ViewGroup mVgBalance;
	private TextView mTvBalance;
	private CustomToast mProgressDialog;
	private PullDownView mPullDownView;
	private ListView mListView;
	private List<AccountDetailItem> mList;
	private AccountDetailAdapter mAdapter;
	private int mLoadMode = LOAD_REFRESH;
	private boolean mIsTaskFree = true;
	private int mPageNum = 1;
	private GetAccountDetailsTask mTask;
	private ViewGroup mVgDetails;
	private TextView mEmptyText;
	private int mBalance;

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
		setView(R.layout.account_detail);
		setTitleBar(getString(R.string.back), getString(R.string.account_details), null);

		mVgBalance = (ViewGroup) findViewById(R.id.rl_balance);
		mVgBalance.setOnClickListener(this);
		mTvBalance = (TextView) findViewById(R.id.tv_balance);
		mBalance = getIntent().getIntExtra("balance", 0);
		mTvBalance.setText(getString(R.string.balance) + mBalance + getString(R.string.money_unit));
		
		mVgDetails = (ViewGroup) findViewById(R.id.ll_account_details);
		mEmptyText = (TextView) findViewById(R.id.empty_text);
		
		mPullDownView = (PullDownView) findViewById(R.id.pulldown_view);
		mPullDownView.setUpdateHandle(this);		
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new AccountDetailAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this);
		
		if (mList == null || mList.isEmpty()){
			refreshData(LOAD_REFRESH);
		}
	}
	
	@Override
	public void onClick(View v) {
		
		if (v == mVgBalance){
			if (mBalance >= 50){
				Intent intent = new Intent(this, WithdrawActivity.class);
				intent.putExtra("balance", mBalance);
				startActivity(intent);
			}else{
				Utils.showToast(this, getString(R.string.balance_not_enough), Toast.LENGTH_SHORT);
			}
		}
		super.onClick(v);
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
		mTask = new GetAccountDetailsTask();
		try {
			mTask.execute(mPageNum);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		dismissProgress();
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


		if ((firstVisibleItem + visibleItemCount == totalItemCount - 5 || firstVisibleItem
						+ visibleItemCount >= totalItemCount - 1)) 
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

	@Override
	public void onUpdate() {
		refreshData(LOAD_REFRESH);		
	}
	
	private void processResult(String result){
		if (TextUtils.isEmpty(result)){
			if (mLoadMode == LOAD_REFRESH){
				showEmpty(true);
			}
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			JSONObject data = obj.optJSONObject("messages").optJSONObject("data");
			JSONArray array = data.optJSONArray("details");
			if (array == null || array.length() == 0){
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
				}else{
					showEmpty(true);
				}
				return;
			}
			if (mList == null){
				mList = new ArrayList<AccountDetailItem>();
			}
			List<AccountDetailItem> list = new ArrayList<AccountDetailItem>();
			for(int i=0; i<array.length(); i++){
				AccountDetailItem item = new AccountDetailItem(this, array.getJSONObject(i));
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
			mEmptyText.setVisibility(View.VISIBLE);
			mVgDetails.setVisibility(View.GONE);
		}else{
			mEmptyText.setVisibility(View.GONE);
			mVgDetails.setVisibility(View.VISIBLE);
		}
	}
}
