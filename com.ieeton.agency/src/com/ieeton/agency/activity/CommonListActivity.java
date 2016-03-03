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
import com.ieeton.agency.models.CommonItem;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CommonItemView;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.R;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class CommonListActivity extends TemplateActivity implements OnItemClickListener{
	private class CommonListAdapter extends BaseAdapter{

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
			CommonItemView view;
			if (convertView == null){
				view = new CommonItemView(CommonListActivity.this);
			}else{
				view = (CommonItemView) convertView;
			}
			if (mList != null && mList.size() > 0 && position < mList.size()){
				view.update((CommonItem) mList.get(position));
			}
			return view;
		}
		
	}
	
	private class FechDataTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			switch (mMode){
			case MODE_PROVINCE:
			case MODE_CITY:
				try {
					result = NetEngine.getInstance(CommonListActivity.this).getRegionList(mParentId);
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
				break;
			case MODE_HOSPITAL:
				try {
					result = NetEngine.getInstance(CommonListActivity.this).
									getHospitalList(mRegionId, 20, 1);
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
				break;
			case MODE_DEPARTMENT:
				try {
					result = NetEngine.getInstance(CommonListActivity.this).
									getDepartmentList();
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
				break;
			case MODE_TITLE:
				try {
					result = NetEngine.getInstance(CommonListActivity.this).
									getTitleList();
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
				break;
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
					Utils.showToast(CommonListActivity.this, R.string.OthersException, Toast.LENGTH_SHORT);
				}
				return;
			}
			processResult(result);
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private int REQUEST_CITY = 1;
	private int REQUEST_HOSPITAL = 2;
	public static String EXTRA_PARENTID = "parent_id";
	public static String EXTRA_REGIONID = "region_id";
 	public static String EXTRA_MODE = "mode";
	public static final int MODE_PROVINCE = 1;
	public static final int MODE_CITY = 2;
	public static final int MODE_HOSPITAL = 3;
	public static final int MODE_DEPARTMENT = 4;
	public static final int MODE_TITLE = 5;
	
	private ListView mListView;
	private int mMode;
	private List<CommonItem> mList;
	private CommonListAdapter mAdapter;
	private CustomToast mProgressDialog;
	private int mParentId;
	private int mRegionId;

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
		setView(R.layout.common_list);
		Intent intent = getIntent();
		if (intent == null){
			return;
		}
		mMode = intent.getIntExtra(EXTRA_MODE, -1);
		if (mMode == -1){
			return;
		}
		mParentId = intent.getIntExtra(EXTRA_PARENTID, 1);
		mRegionId = intent.getIntExtra(EXTRA_REGIONID, 1);
		
		String title = "";
		switch (mMode){
		case MODE_PROVINCE:
		case MODE_CITY:
			title = getString(R.string.title_select_city);
			break;
		case MODE_HOSPITAL:
			title = getString(R.string.title_select_hospital);
			break;
		case MODE_DEPARTMENT:
			title = getString(R.string.title_select_department);
			break;
		case MODE_TITLE:
			title = getString(R.string.title_select_title);
			break;
		}
		setTitleBar(getString(R.string.back), title, null);
		
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new CommonListAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		
		FechDataTask task = new FechDataTask();
		try {
			task.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long aid) {
		CommonItem item = (CommonItem) mList.get(position);
		if (item == null){
			return;
		}
		switch (mMode){
		case MODE_PROVINCE:
			if (item.hasNextLevel()){
				Intent intent = new Intent(this, CommonListActivity.class);
				intent.putExtra(EXTRA_MODE, MODE_CITY);
				intent.putExtra(EXTRA_PARENTID, item.getId());
				startActivityForResult(intent, REQUEST_CITY);
			}else{
				Intent intent = new Intent(this, CommonListActivity.class);
				intent.putExtra(EXTRA_MODE, MODE_HOSPITAL);
				intent.putExtra(EXTRA_REGIONID, item.getId());
				startActivityForResult(intent, REQUEST_HOSPITAL);
			}
			break;
		case MODE_CITY:
			Intent intent = new Intent(this, CommonListActivity.class);
			intent.putExtra(EXTRA_MODE, MODE_HOSPITAL);
			intent.putExtra(EXTRA_REGIONID, item.getId());
			startActivityForResult(intent, REQUEST_HOSPITAL);
			break;
		case MODE_HOSPITAL:
		case MODE_DEPARTMENT:
		case MODE_TITLE:
			Intent data = new Intent();
			data.putExtra(UpdateInfoActivity.RETURN_DATA, item);
			setResult(RESULT_OK, data);
			finish();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK){
			return;
		}
		if (mMode == MODE_CITY || mMode == MODE_PROVINCE){
			setResult(RESULT_OK, data);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
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
	
	private void processResult(String result){
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			JSONObject data = obj.optJSONObject("messages").optJSONObject("data");
			JSONArray array = null;
			int type = 0;
			switch (mMode){
			case MODE_PROVINCE:
			case MODE_CITY:
				type = CommonItem.TYPE_REGION;
				array = data.optJSONArray("provinces");
				break;
			case MODE_HOSPITAL:
				type = CommonItem.TYPE_HOSPITAL;
				array = data.optJSONArray("hospitals");
				break;
			case MODE_DEPARTMENT:
				type = CommonItem.TYPE_DEPARTMENT;
				array = data.optJSONArray("departments");
				break;
			case MODE_TITLE:
				type = CommonItem.TYPE_TITLE;
				array = data.optJSONArray("titles");
				break;
			}
			if (array == null || array.length() == 0){
				return;
			}
			mList = new ArrayList<CommonItem>();
			for (int i=0; i<array.length(); i++){
				CommonItem item = new CommonItem(array.getJSONObject(i), type);
				mList.add(item);
			}
			mAdapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
