package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.DemoApplication;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.City;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.SelectCityListItemView;
import com.ieeton.agency.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SelectCityActivity extends Activity implements android.view.View.OnClickListener{
	private List<City> mCityList;
	private CityListAdapter mCityListAdapter;
	private ListView mListView;
	private TextView mMyCity;
	private TextView mHint;
	private ProgressBar mProgressBar;
	private LocationFinishReceiver mLocationFinishReceiver;
	
	private boolean mIsTaskFree = true;
	private LoadAvailableCityTask mLoadAvailableCityTask;
	
	private int mMode = 0;
	
	public static String MODE = "mode";
	public static int MODE_SEARCH = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.v("sereinli","SelectCityActivity,onCreate");

		super.onCreate(savedInstanceState);
		
		IntentFilter intentfilter = new IntentFilter();
		intentfilter.addAction(Constants.LOCATION_FINISH_ACTION);
		mLocationFinishReceiver = new LocationFinishReceiver();
		registerReceiver(mLocationFinishReceiver, intentfilter);
		
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.select_city);
        
        RelativeLayout rl_auto_location = (RelativeLayout)findViewById(R.id.rl_auto_location);
        rl_auto_location.setOnClickListener(this);
        
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null){
        	mMode = getIntent().getExtras().getInt(MODE);
        }
        
        mProgressBar = (ProgressBar)findViewById(R.id.progress_loading);
        mProgressBar.setVisibility(View.VISIBLE);
        
        mHint = (TextView)findViewById(R.id.tv_location_des);
        mHint.setVisibility(View.GONE);
        
        mListView = (ListView)findViewById(R.id.lv_city_list);
        mCityListAdapter = new CityListAdapter();
        mListView.setAdapter(mCityListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long aid) {
				if (mMode == MODE_SEARCH){
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putSerializable("city", mCityList.get(position));
					intent.putExtras(bundle);
					setResult(RESULT_OK, intent);
				}else{
					Intent intent = new Intent(SelectCityActivity.this, MainActivity.class);
					intent.putExtra(MainActivity.INPUT_INDEX, MainActivity.INPUT_CONTACTLIST);
					startActivity(intent);
				}
				Utils.setMyCity(SelectCityActivity.this, mCityList.get(position));
				finish();
			}
		});

        
        //获取可选城市列表
        if(mIsTaskFree){
        	try{
        		mLoadAvailableCityTask = new LoadAvailableCityTask();
        		mLoadAvailableCityTask.execute();
        	}catch(RejectedExecutionException e){
        		e.printStackTrace();
        	}
        }
        
        mMyCity = (TextView)findViewById(R.id.tv_location);
		if(DemoApplication.mIeetonLocation != null){
			
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					mMyCity.setText(DemoApplication.mIeetonLocation.getCity());
					mProgressBar.setVisibility(View.GONE);
					mHint.setVisibility(View.VISIBLE);
				}
			}, 3000);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(mLocationFinishReceiver != null){
			unregisterReceiver(mLocationFinishReceiver);
		}
		super.onDestroy();
	}

	class CityListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mCityList != null && !mCityList.isEmpty()){
				return mCityList.size();
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
			SelectCityListItemView view = null;
			if (convertView == null){
				view = new SelectCityListItemView(SelectCityActivity.this);
			}else{
				view = (SelectCityListItemView)convertView;
			}
			
			view.update(mCityList.get(position));

			return view;
		}
		
	}

	class LoadAvailableCityTask extends AsyncTask<Void, Void, List<City>>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsTaskFree = false;
		}

		@Override
		protected List<City> doInBackground(Void... arg0) {
			String result = "";

				try {
					result = NetEngine.getInstance(SelectCityActivity.this).GetAvailableCityList();
				} catch (PediatricsIOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mThr = e;
				} catch (PediatricsParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mThr = e;
				} catch (PediatricsApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mThr = e;
				}

				JSONObject object = null;
				try {
					object = new JSONObject(result);
					if(!object.getBoolean("error")){
						JSONObject json_data = object.optJSONObject("messages").optJSONObject("data");
						JSONArray array = json_data.getJSONArray("provinces");
		
						List<City> list = new ArrayList<City>();
						for(int i=0; i<array.length(); i++){
							City item = new City((JSONObject)array.get(i));
							list.add(item);
						}
						return list;
					}else{
						return null;
					}
				} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}	

		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(List<City> result) {
			mIsTaskFree = true;
						
			if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, SelectCityActivity.this);
				}else{
					Utils.showToast(SelectCityActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}

			if(mCityList != null){
				mCityList.clear();
			}
			mCityList = result;
			mCityListAdapter.notifyDataSetChanged();

		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.rl_auto_location){
			City city = null;
			boolean find = false;
			for(City c : mCityList){
				if(DemoApplication.mIeetonLocation.getCity().startsWith(c.getCityName())){
					city = c;
					find = true;
					break;
				}
			}
			if(!find){
				city = new City(1, DemoApplication.mIeetonLocation.getCity());
			}
			Utils.setMyCity(SelectCityActivity.this, city);

			if (mMode == MODE_SEARCH){
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("city", city);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
			}else{
				Intent intent = new Intent(SelectCityActivity.this, MainActivity.class);
				intent.putExtra(MainActivity.INPUT_INDEX, MainActivity.INPUT_CONTACTLIST);
				startActivity(intent);
			}
			finish();
		}
	}
	
	public class LocationFinishReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(Constants.LOCATION_FINISH_ACTION.equals(action)){
				if(DemoApplication.mIeetonLocation != null){
					mMyCity.setText(DemoApplication.mIeetonLocation.getCity());
					mProgressBar.setVisibility(View.GONE);
					mHint.setVisibility(View.VISIBLE);
				}
			}
			Log.v("sereinli", "SelectActivity, LocationFinishReceiver,"+action);
		}
		
	}
}
