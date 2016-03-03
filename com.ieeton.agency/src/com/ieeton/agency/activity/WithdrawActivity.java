package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.ListItem;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.ListItemView;
import com.ieeton.agency.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WithdrawActivity extends TemplateActivity{
	private class PrepareRechargeTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(WithdrawActivity.this)
						.prepareWithdraw(Utils.getPassport(WithdrawActivity.this), mMoney);
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
            
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}
				return;
			}
			Utils.showToast(WithdrawActivity.this, R.string.withdraw_request_success, Toast.LENGTH_LONG);
		}

		@Override
		protected void onPreExecute() {
			isTaskFree = false;
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private TextView mBtnOk;
	private ListView mListView;
	private RechargeListAdapter mRechargeListAdapter;
	private List<ListItem>	mList;
	private CustomToast mProgressDialog;
	private String mMoney;
	private boolean isTaskFree = true;
	private int[] amount = {500, 1000, 1500, 2000, 2500, 3000, 5000};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.withdraw_activity);
		setTitleBar(getString(R.string.back), getString(R.string.withdraw), null);

		mList = new ArrayList<ListItem>();
		int balance = getIntent().getIntExtra("balance", 0);
		if (balance < 50){
			return;
		}
		for(int i = 0; i<amount.length; i++){
			int item = amount[i];
			if (item <= balance){
				ListItem listItem = new ListItem(item + getString(R.string.money_unit), false);
				mList.add(listItem);
			}else{
				break;
			}
			
		}
		mBtnOk = (TextView) findViewById(R.id.tv_ok);
		mBtnOk.setOnClickListener(this);
		mListView = (ListView)findViewById(R.id.listview);
		mRechargeListAdapter = new RechargeListAdapter();
		mListView.setAdapter(mRechargeListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long aid) {

				mMoney = amount[position]+"";
				for(ListItem item : mList){
					if (item.isChecked()){
						item.setChecked(false);
						break;
					}
				}
				mList.get(position).setChecked(true);
				mRechargeListAdapter.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		if (v == mBtnOk){
			withdraw();
		}
		super.onClick(v);
	}

	private void withdraw(){
		if (!isTaskFree || TextUtils.isEmpty(mMoney)){
			return;
		}
		
		try {
			new PrepareRechargeTask().execute();
		} catch (RejectedExecutionException e) {
			
		}
	}
	
    @Override
	protected void onDestroy() {
		dismissProgress();
		super.onDestroy();
	}
	
	class RechargeListAdapter extends BaseAdapter{

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
				view = new ListItemView(WithdrawActivity.this);
			}else{
				view = (ListItemView)convertView;
			}
			
			view.update(mList.get(position));

			return view;
		}
		
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
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}
}
