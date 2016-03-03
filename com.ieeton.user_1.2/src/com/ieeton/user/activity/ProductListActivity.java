package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.adapter.ProductListAdapter;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.City;
import com.ieeton.user.models.Lable;
import com.ieeton.user.models.Product;
import com.ieeton.user.models.ProductCategory;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.PullDownView;
import com.ieeton.user.view.PullDownView.UpdateHandle;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class ProductListActivity extends TemplateActivity implements UpdateHandle{
	private final int LOAD_REFRESH = 0;
	private final int LOAD_MORE = 1;

	private ViewGroup mVgSearch;
	private PullDownView mPullDownView;
	private ListView mListView;
	private ProductListAdapter mAdapter;
	private int mPageNum = 1;
	private int mLoadMode = LOAD_REFRESH;
	private List<Product> mProductList;
	
	
	private City mCity;
	private ProductCategory mCategory;
	private Lable mLable;
	private boolean mIsTaskFree = true;
	private FetchDataTask mTask;
	

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
		setView(R.layout.activity_product_list);
		
		mCity = (City) getIntent().getSerializableExtra(Constants.EXTRA_CITY);
		mCategory = (ProductCategory) getIntent().getSerializableExtra(Constants.EXTRA_CATEGORY);
		mLable = (Lable) getIntent().getSerializableExtra(Constants.EXTRA_LABLE);
		
		if (mCategory != null){
			setTitleBar(getString(R.string.back), mCategory.getCagegoryName(), null);
		}else{
			setTitleBar(getString(R.string.back), mLable.getName(), null);
		}
		
		mVgSearch = (ViewGroup) findViewById(R.id.rl_search);
		mVgSearch.setOnClickListener(this);
		
		mPullDownView = (PullDownView) findViewById(R.id.pulldown_view);
		mPullDownView.setUpdateHandle(this);
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new ProductListAdapter(this, mProductList);
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
				if (mProductList != null && !mProductList.isEmpty() && position < mProductList.size()){
					Product product = (Product) mProductList.get(position);
					Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
					intent.putExtra(Constants.EXTRA_PRODUCT, product);
					intent.putExtra(Constants.EXTRA_PRODUCTID, product.getId()+"");
					intent.putExtra(Constants.EXTRA_UID, product.getOwnerUid());
					intent.putExtra(Constants.EXTRA_CATEGORYID, product.getCategoryId());
					startActivity(intent);
				}
			}
		});
		
		refreshData(LOAD_REFRESH);
	}
	
	@Override
	public void onUpdate() {
		refreshData(LOAD_REFRESH);
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
			mTask.execute(mPageNum);
		} catch (RejectedExecutionException e) {
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
					Utils.showToast(ProductListActivity.this, R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					mAdapter.refresh(mProductList);
					showEmpty(true);
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
			mAdapter.refresh(mProductList);
			if (mProductList.size()>0){
				showEmpty(false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mVgSearch){
			Intent intent = new Intent(this, SearchProductActivity.class);
			intent.putExtra(Constants.EXTRA_CITY, mCity);
			intent.putExtra(Constants.EXTRA_CATEGORY, mCategory);
			intent.putExtra(Constants.EXTRA_LABLE, mLable);
			startActivity(intent);
			
		}
		super.onClick(v);
	}

	private class FetchDataTask extends AsyncTask<Integer, Void, String>{
	    private Throwable mThr;
	    private int page;

		@Override
		protected String doInBackground(Integer... params) {
			page = params[0];
			String result = "";
				try {
					int categoryid = mCategory == null ? 0 : mCategory.getCategoryId();
					int lableid = mLable == null ? 0 : mLable.getId();
					result = NetEngine.getInstance(ProductListActivity.this).
								getRecommondProduct(lableid, categoryid, mCity.getLongitude(), 
										mCity.getLatitude(), mCity.getCityID(), page);
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
					Utils.handleErrorEvent(mThr, ProductListActivity.this);
				}else{
					Utils.showToast(ProductListActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
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
