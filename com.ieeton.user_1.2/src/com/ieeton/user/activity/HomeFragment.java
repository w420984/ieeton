package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.IeetonApplication;
import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.location.BaiduLocationHelper;
import com.ieeton.user.location.IeetonLocation;
import com.ieeton.user.location.IeetonLocationListener;
import com.ieeton.user.models.City;
import com.ieeton.user.models.Product;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.CustomToast;
import com.ieeton.user.view.HomeHeaderView;
import com.ieeton.user.view.PullDownView;
import com.ieeton.user.view.PullDownView.UpdateHandle;
import com.ieeton.user.view.RecommondItemView;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HomeFragment extends Fragment implements OnClickListener,UpdateHandle{
	private final int FETCH_CITY = 1;
	private final int FETCH_PRODUCT = 2;
	private final int LOAD_REFRESH = 0;
	private final int LOAD_MORE = 1;
	
	private TextView mTvCityName;
	private ViewGroup mVgCity;
	private ViewGroup mVgSearch;
	private City mCity;
	
	private View mView;
	private HomeHeaderView mHomeHeaderView;
	private PullDownView mPullDownView;
	private ListView mListView;
	private RecommondListAdapter mAdapter;
	private int mPageNum = 1;
	private int mLoadMode = LOAD_REFRESH;
	
	private List<Product> mProductList;
	private CustomToast mProgressDialog;
	private boolean mIsTaskFree = true;
	private FetchDataTask mTask;
	private boolean isLocationEnd = false;
	
	private IeetonLocationListener locationListener = new IeetonLocationListener() {
		@Override
		public void onLocationStart() {
			isLocationEnd = false;
			showProgress();
		}
		
		@Override
		public void onLocationFinish(IeetonLocation location) {
			isLocationEnd = true;
			if (location == null){
				
			}else{				
				IeetonApplication.mIeetonLocation = location;
				String city = location.getCity();
				if(!TextUtils.isEmpty(city) && location.getLatitude()>0){
					Utils.setMyLocation(getActivity(), city, location.getLatitude(), 
							location.getLongtitude());
				}
				BaiduLocationHelper.StopBaiduLocation();
			}			
			setCity();
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_home, container, false);
		initView();
		return mView;
	}

	private void initView(){
		mVgSearch = (ViewGroup) mView.findViewById(R.id.rl_search);
		mVgSearch.setOnClickListener(this);
		
		mVgCity = (ViewGroup) mView.findViewById(R.id.rl_city);
		mVgCity.setOnClickListener(this);
		mTvCityName = (TextView) mView.findViewById(R.id.tv_city);
		
		mHomeHeaderView = new HomeHeaderView(getActivity(), this);
		
		mPullDownView = (PullDownView) mView.findViewById(R.id.pulldown_view);
		mPullDownView.setUpdateHandle(this);
		mListView = (ListView) mView.findViewById(R.id.list);
		mAdapter = new RecommondListAdapter();
		mListView.addHeaderView(mHomeHeaderView);
		mListView.setAdapter(mAdapter);
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
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long arg3) {
				if (mProductList != null && !mProductList.isEmpty() && position <= mProductList.size()){
					Product product = (Product) mProductList.get(position-1);
					Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
					intent.putExtra(Constants.EXTRA_PRODUCT, product);
					intent.putExtra(Constants.EXTRA_PRODUCTID, product.getId()+"");
					intent.putExtra(Constants.EXTRA_UID, product.getOwnerUid());
					intent.putExtra(Constants.EXTRA_CATEGORYID, product.getCategoryId());
					startActivity(intent);
				}
			}
		});
		
		getData();
	}
		
	private void refreshData(int mode){
		mLoadMode = mode;
		if (!mIsTaskFree){
			return;
		}
		//showEmpty(false);
		if (mode == LOAD_REFRESH){
			mPageNum = 1;
			mPullDownView.update();
		}else{
			mPageNum += 1;
			mPullDownView.updateWithoutOffset();
		}
		mTask = new FetchDataTask();
		try {
			mTask.execute(FETCH_PRODUCT, mPageNum);
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
			JSONArray array = obj.optJSONArray("productinfo");

			if (array == null || array.length() == 0){
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
					Utils.showToast(getActivity(), R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					mAdapter.notifyDataSetChanged();
					//showEmpty(true);
				}
				return;
			}
			if (mProductList == null){
				mProductList = new ArrayList<Product>();
			}
			List<Product> list = new ArrayList<Product>();
			for(int i=0; i<array.length(); i++){
				Product item = new Product(array.optJSONObject(i));
				list.add(item);
			}
			if (mLoadMode == LOAD_REFRESH){
				mProductList.clear();
			}
			mProductList.addAll(list);
			mAdapter.notifyDataSetChanged();
			if (mProductList.size()>0){
				//showEmpty(false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void getData(){
		BaiduLocationHelper.startRequestLocation(getActivity(), locationListener);
		if (IeetonApplication.mCityList == null || IeetonApplication.mCityList.isEmpty()){
			mTask = new FetchDataTask();
			try {
				mTask.execute(FETCH_CITY, 1);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		super.onDestroy();
	}

	@Override
	public void onPause() {
		MobclickAgent.onPageEnd("HomeFragment"); 
		super.onPause();
	}

	@Override
	public void onResume() {
		MobclickAgent.onPageStart("HomeFragment"); 
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (v == mVgCity){
			if (IeetonApplication.mCityList == null || IeetonApplication.mCityList.isEmpty()){
				return;
			}
			startActivityForResult(new Intent(getActivity(), SelectCityActivity.class), 100);
		}else if (v == mVgSearch){
			Intent intent = new Intent(getActivity(), SearchProductActivity.class);
			intent.putExtra(Constants.EXTRA_CITY, mCity);
			startActivity(intent);
		}	
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK){
			if (data == null){
				return;
			}
			City city = (City) data.getSerializableExtra(SelectCityActivity.RETURN_DATA);
			String cityName = Utils.getMyLocationCityName(getActivity());
			double lon = Utils.getMyLocationLon(getActivity());
			double la = Utils.getMyLocationLa(getActivity());
			//用户手动选择城市后，如果本地缓存城市没有，则直接使用用户选择的城市
			//如果有本地缓存，判断本地缓存城市和选择的城市是否一样，如果一样，使用本地缓存的经纬度
			if (TextUtils.isEmpty(cityName)){
				mCity = city;
			}else{
				if (cityName.contains(city.getCityName())){
					city.setLatitude(la);
					city.setLongitude(lon);
				}
				mCity = city;
			}
			mTvCityName.setText(mCity.getCityName());
			refreshData(LOAD_REFRESH);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void setCity(){
		//isLocationEnd表示定位结束
		//isTaskFree表示获取城市联网已经结束
		if (isLocationEnd && mIsTaskFree
						&& IeetonApplication.mCityList != null 
						&& !IeetonApplication.mCityList.isEmpty()){
			String cityName = Utils.getMyLocationCityName(getActivity());
			double lon = Utils.getMyLocationLon(getActivity());
			double la = Utils.getMyLocationLa(getActivity());
			Utils.logd(cityName);
			//如果定位成功，则从城市列表里面找和定位结果匹配的城市，
			//否则定位失败先从本地缓存获取上一次成功定位的结果
			//如果获取不到本地缓存的定位结果，则使用全国
			if (TextUtils.isEmpty(cityName)){
				mCity = IeetonApplication.mCityList.get(0);
			}else{
				boolean isFound = false;
				for(City city : IeetonApplication.mCityList){
					if (cityName.contains(city.getCityName())){
						city.setLatitude(la);
						city.setLongitude(lon);
						isFound = true;
						mCity = city;
						break;
					}
				}
				//如果从城市列表里面没有找到匹配的，也使用全国
				if (!isFound){
					mCity = IeetonApplication.mCityList.get(0);
				}
			}
			mTvCityName.setText(mCity.getCityName());
			refreshData(LOAD_REFRESH);
		}
	}
	
	public City getCity(){
		return mCity;
	}
	
	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, getActivity());
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}

	private class FetchDataTask extends AsyncTask<Integer, Void, String>{
	    private Throwable mThr;
	    private int mMode;
	    private int page;

		@Override
		protected String doInBackground(Integer... params) {
			mMode = params[0];
			page = params[1];
			String result = "";
			switch(mMode){
			case FETCH_CITY:{
				try {
					result = NetEngine.getInstance(getActivity()).GetAvailableCityList();
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
			case FETCH_PRODUCT:{
				try {
					result = NetEngine.getInstance(getActivity()).
								getRecommondProduct(0, 0, mCity.getLongitude(), 
										mCity.getLatitude(), 
										mCity.getCityID(), page);
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
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			mIsTaskFree = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			mPullDownView.endUpdate(null);
			mIsTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getActivity());
				}else{
					Utils.showToast(getActivity(), R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				if (mMode == FETCH_PRODUCT && mLoadMode == LOAD_MORE){
					mPageNum--;
				}
				return;
			}
			switch(mMode){
			case FETCH_CITY:{
				try {
					//JSONObject obj = new JSONObject(result);
					JSONArray array = new JSONArray(result);
					if (IeetonApplication.mCityList == null){
						IeetonApplication.mCityList = new ArrayList<City>();
					}
					for (int i=0; i<array.length(); i++){
						City item = new City(array.optJSONObject(i));
						IeetonApplication.mCityList.add(item);
					}
					setCity();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
			case FETCH_PRODUCT:{
				processResult(result);
				break;
			}
			}
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			super.onPreExecute();
		}
		
	}
	
	private class RecommondListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mProductList != null){
				return mProductList.size();
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
			RecommondItemView view;
			if (convertView == null){
				view = new RecommondItemView(getActivity());
			}else{
				view = (RecommondItemView) convertView;
			}
			if (mProductList != null && !mProductList.isEmpty() && position<mProductList.size()){
				view.update(mProductList.get(position));
			}
			return view;
		}
		
	}

	@Override
	public void onUpdate() {
		refreshData(LOAD_REFRESH);
	}
}
