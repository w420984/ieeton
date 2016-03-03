package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.IeetonApplication;
import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Lable;
import com.ieeton.user.models.ListItem;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.ListItemView;

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

public class AgeLableListActivity extends TemplateActivity {
	private class MyListAdapter extends BaseAdapter{

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
				view = new ListItemView(AgeLableListActivity.this);
			}else{
				view = (ListItemView)convertView;
			}
			
			view.update(mList.get(position));

			return view;
		}
		
	}
	
	private class FetchDataTask extends AsyncTask<Integer, Void, String>{
    	private Throwable mThr;
    	private int action;
    	
		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			action = params[0];
			try {
				if (action == ACTION_GET){
					result = NetEngine.getInstance(AgeLableListActivity.this).getAgeLable();
				}else if (action == ACTION_SET){
					if (mMode == MODE_AGE){
						result = NetEngine.getInstance(AgeLableListActivity.this).updateAgeLable(mMinAge, mMaxAge, null);
					}else {
						result = NetEngine.getInstance(AgeLableListActivity.this).updateAgeLable(-1, -1, mLable);
					}
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
			mIsTaskFree = true;
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, AgeLableListActivity.this);
				}else{
					Utils.showToast(AgeLableListActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			if (action == ACTION_GET){
				try {
					JSONObject obj = new JSONObject(result);
					int minAge = obj.optInt("minage");
					int maxAge = obj.optInt("maxage");
					String [] selectedLables = null;
					String lables = obj.optString("Label");
					if (!TextUtils.isEmpty(lables)){
						selectedLables =  lables.split(",");
					}				 
					if (mMode == MODE_AGE){
						
					}else{
						for(int i=0; selectedLables != null && i<selectedLables.length; i++){
							if (!TextUtils.isEmpty(selectedLables[i])){
								mList.get(Integer.parseInt(selectedLables[i])-1).setChecked(true);
							}
						}
						mAdapter.notifyDataSetChanged();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else{
				Utils.showToast(AgeLableListActivity.this, R.string.operation_succes, Toast.LENGTH_SHORT);
				finish();
			}
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			dismissProgress();
			super.onPreExecute();
		}
		
	}
	
	public static final int MODE_AGE = 1;
	public static final int MODE_LABLE =2;
	
	private static final int ACTION_GET = 1;
	private static final int ACTION_SET = 2;

	private ListView mListView;
	private MyListAdapter mAdapter;
	private int mMode;
	private List<ListItem> mList;
	private FetchDataTask mTask;
	private boolean mIsTaskFree = true;
	private int mMinAge = -1;
	private int mMaxAge = -1;
	private String mLable = "";
	private String [] agelist = {"怀孕", "0-1岁", "1-3岁", "3-6岁", "6岁以上"};
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			save();
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_age_lable_list);
		mMode = getIntent().getIntExtra(Constants.EXTRA_MODE, 0);
		String title = "";
		mList = new ArrayList<ListItem>();
		if (mMode == MODE_AGE){
			title = getString(R.string.select_age);
			for(int i=0; i<agelist.length; i++){
				ListItem item = new ListItem(agelist[i], false);
				mList.add(item);
			}
		}else{
			title = getString(R.string.select_lable);
			List<Lable> lableList = IeetonApplication.mServerHostData.getLableList();
			if (lableList !=null && !lableList.isEmpty()){
				for(int i=0; i<lableList.size(); i++){
					ListItem item = new ListItem(lableList.get(i).getName(), false);
					mList.add(item);
				}
			}
		}		
		setTitleBar(getString(R.string.back), title, getString(R.string.save));
		
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new MyListAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long aid) {
				mList.get(position).setChecked(!mList.get(position).isChecked());
				mAdapter.notifyDataSetChanged();
			}
		});
		
		executeTask(ACTION_GET);
	}

	private void executeTask(int action){
		if (mIsTaskFree){
			mTask = new FetchDataTask();
			mTask.execute(action);
		}
	}
	
	private void save(){
		if (mMode == MODE_AGE){
			List<Integer> list = new ArrayList<Integer>();
			for(int i=0; i<mList.size(); i++){
				if (mList.get(i).isChecked()){
					if (i==0){
						list.add(Integer.valueOf(0));
						list.add(Integer.valueOf(0));
					}else if (i==1){
						list.add(Integer.valueOf(0));
						list.add(Integer.valueOf(1));
					}else if (i==2){
						list.add(Integer.valueOf(1));
						list.add(Integer.valueOf(3));
					}else if (i==3){
						list.add(Integer.valueOf(3));
						list.add(Integer.valueOf(6));
					}else if (i==4){
						list.add(Integer.valueOf(6));
						list.add(Integer.valueOf(12));
					}
				}
			}
			if (list != null && list.size()>0){
				Object[] array = list.toArray();
				Arrays.sort(array);			
				mMinAge = ((Integer)array[0]).intValue();
				mMaxAge = ((Integer)array[array.length-1]).intValue();
			}
			Utils.logd(mMinAge+"");
			Utils.logd(mMaxAge+"");
		}else{
			for(int i=0; i<mList.size(); i++){
				if (mList.get(i).isChecked()){
					if (TextUtils.isEmpty(mLable)){
						mLable += ((i+1) + "");
					}else{
						mLable += (","+(i+1));
					}
				}
			}
		}
		executeTask(ACTION_SET);
	}

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		super.onDestroy();
	}
}
