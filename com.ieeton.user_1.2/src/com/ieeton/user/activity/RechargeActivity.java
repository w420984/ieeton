package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ieeton.user.R;
import com.ieeton.user.models.ListItem;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.RechargeHelper;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.ListItemView;

public class RechargeActivity extends TemplateActivity{
	
	private TextView mOKBtn;
	private ListView mListView;
	private RechargeListAdapter mRechargeListAdapter;
	private List<ListItem>	mList;
	private String mMoney;
	private BroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_recharge);
		setTitleBar(getString(R.string.back), getString(R.string.select_recharge_account), getString(R.string.bill));

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.WEIXIN_PAY_SUCCESS_ACTION);
		mReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Constants.WEIXIN_PAY_SUCCESS_ACTION.equals(intent.getAction())){
					RechargeActivity.this.setResult(Activity.RESULT_OK);
					RechargeActivity.this.finish();
				}
			}			
		};
		registerReceiver(mReceiver, filter);

		mOKBtn = (TextView) findViewById(R.id.tv_ok);
		mOKBtn.setOnClickListener(this);
		mList = new ArrayList<ListItem>();
		String[] array = getResources().getStringArray(R.array.recharge_list_item);
		for(int i = 0; i < array.length; i++){
			ListItem item = new ListItem(array[i], false);
			mList.add(item);
		}
		
		mListView = (ListView)findViewById(R.id.listview);
		mRechargeListAdapter = new RechargeListAdapter();
		mListView.setAdapter(mRechargeListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long aid) {
				String[] moneys = {"50", "100", "200",
							"300", "400", "500"};
//				String[] moneys = {"0.01", "0.02", "0.03",
//						"4", "5", "6"};

				mMoney = moneys[position];
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
	
	private void reCharge(){
		RechargeHelper.getInstance(this).WXPay("大大健康充值", Integer.valueOf(mMoney)*100, 
				RechargeHelper.TYPE_RECHARGE, null);
	}
	
    @Override
	protected void onDestroy() {
		dismissProgress();
		if (mReceiver != null){
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
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
				view = new ListItemView(RechargeActivity.this);
			}else{
				view = (ListItemView)convertView;
			}
			
			view.update(mList.get(position));

			return view;
		}
		
	}
	
	@Override
	public void onClick(View v) {
		if (v == mOKBtn){
			reCharge();
		}
		super.onClick(v);
	}

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			startActivity(new Intent(this, AccountDetailActivity.class));
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Utils.logd("RechargeActivity onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
	}
}
