package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.IeetonApplication;
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
import com.ieeton.user.view.PopMenu;
import com.ieeton.user.view.PopMenu.OnItemClick;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class SearchProductActivity extends TemplateActivity implements UpdateHandle,
				OnItemClick{
	private final int LOAD_REFRESH = 0;
	private final int LOAD_MORE = 1;
	
	private final int FETCH_HOTKEYWORD = 1;
	private final int FETCH_SEARCH = 2;
	
	private final int POP_CITY = 1;
	private final int POP_CATEGORY = 2;
	
	private ImageView mIvBack;
	private EditText mEtKeyword;
	private ImageView mIvSearch;
	private ViewGroup mVgCity;
	private ViewGroup mVgCategory;
	private TextView mTvCity;
	private ImageView mIvCityArrow;
	private ImageView mIvCitySharp;
	private TextView mTvCategory;
	private ImageView mIvCategoryArrow;
	private ImageView mIvCategorySharp;
	private List<TextView> mTvHotKeywordList;
	private ViewGroup mVgHotKeyword;

	private List<String> mHotKeywordList;
	
	private PullDownView mPullDownView;
	private ListView mListView;
	private ProductListAdapter mAdapter;
	private int mPageNum = 1;
	private int mLoadMode = LOAD_REFRESH;
	private List<Product> mProductList;
	private String mKeyword;
	
	private Lable mLable;
	private City mCity;
	private ProductCategory mCategory;
	private boolean mIsTaskFree = true;
	private FetchDataTask mTask;
	
	private PopMenu mPopMenu;
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_search_product);
		setTitleBar(null, null, null);
		
		mCity = (City) getIntent().getSerializableExtra(Constants.EXTRA_CITY);
		mCategory = (ProductCategory) getIntent().getSerializableExtra(Constants.EXTRA_CATEGORY);
		mLable = (Lable) getIntent().getSerializableExtra(Constants.EXTRA_LABLE);
//		if (mCategory == null){
//			mCategory = IeetonApplication.mServerHostData.getProductCategoryList().get(0);
//		}
//		if (mCity == null){
//			mCity = IeetonApplication.mCityList.get(0);
//		}
		init();
		
		mTask = new FetchDataTask();
		mTask.execute(FETCH_HOTKEYWORD, 1);
	}
	
	private void init(){
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mIvBack.setOnClickListener(this);
		
		mEtKeyword = (EditText) findViewById(R.id.search_input_box);
		mIvSearch = (ImageView) findViewById(R.id.search_btn);
		mIvSearch.setOnClickListener(this);
		
		mVgCity = (ViewGroup) findViewById(R.id.fl_city);
		mVgCity.setOnClickListener(this);
		mIvCityArrow = (ImageView) findViewById(R.id.iv_city_arrow);
		mTvCity = (TextView) findViewById(R.id.tv_city);
		mIvCitySharp = (ImageView) findViewById(R.id.iv_city_sharp);
		
		mVgCategory = (ViewGroup) findViewById(R.id.fl_category);
		mVgCategory.setOnClickListener(this);
		mIvCategoryArrow = (ImageView) findViewById(R.id.iv_category_arrow);
		mTvCategory = (TextView) findViewById(R.id.tv_category);
		mIvCategorySharp = (ImageView) findViewById(R.id.iv_category_sharp);
		
		mVgHotKeyword = (ViewGroup) findViewById(R.id.ll_hot_keyword);
		
		mTvHotKeywordList = new ArrayList<TextView>();
		mTvHotKeywordList.add((TextView) findViewById(R.id.tv_hot_keyword1));
		mTvHotKeywordList.add((TextView) findViewById(R.id.tv_hot_keyword2));
		mTvHotKeywordList.add((TextView) findViewById(R.id.tv_hot_keyword3));
		mTvHotKeywordList.add((TextView) findViewById(R.id.tv_hot_keyword4));
		mTvHotKeywordList.add((TextView) findViewById(R.id.tv_hot_keyword5));
		mTvHotKeywordList.add((TextView) findViewById(R.id.tv_hot_keyword6));
		mTvHotKeywordList.add((TextView) findViewById(R.id.tv_hot_keyword7));
		mTvHotKeywordList.add((TextView) findViewById(R.id.tv_hot_keyword8));
		mTvHotKeywordList.add((TextView) findViewById(R.id.tv_hot_keyword9));
		for(TextView v : mTvHotKeywordList){
			v.setOnClickListener(this);
		}
		
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
					Intent intent = new Intent(SearchProductActivity.this, ProductDetailActivity.class);
					intent.putExtra(Constants.EXTRA_PRODUCT, product);
					intent.putExtra(Constants.EXTRA_PRODUCTID, product.getId()+"");
					intent.putExtra(Constants.EXTRA_UID, product.getOwnerUid());
					intent.putExtra(Constants.EXTRA_CATEGORYID, product.getCategoryId());
					startActivity(intent);
				}
			}
		});
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
			mTask.execute(FETCH_SEARCH, mPageNum);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mIvSearch){
			mKeyword = mEtKeyword.getText().toString();
			refreshData(LOAD_REFRESH);
		}else if (v == mVgCity){
			showPopMenu(v, POP_CITY);
		}else if (v == mVgCategory){
			showPopMenu(v, POP_CATEGORY);
		}else if (v == mIvBack){
			finish();
		}else{
			for(int i=0; i<mTvHotKeywordList.size(); i++){
				if (v == mTvHotKeywordList.get(i)){
					if (mHotKeywordList == null || mHotKeywordList.isEmpty()){
						return;
					}
					mKeyword = mHotKeywordList.get(i);
					mEtKeyword.setText(mKeyword);
					refreshData(LOAD_REFRESH);
					break;
				}
			}
		}
		super.onClick(v);
	}
	
	@Override
	public void onItemClick(int index, int type) {
		if (type == POP_CITY){
			mCity = IeetonApplication.mCityList.get(index);
			mTvCity.setText(mCity.getCityName());
		}else if (type == POP_CATEGORY){
			mCategory = NetEngine.getProductCategoryList().get(index);
			mTvCategory.setText(mCategory.getCagegoryName());
		}
	}

	private void showPopMenu(View v, int type){
		if (mPopMenu == null){
			mPopMenu = new PopMenu(this);
		}
		if (mPopMenu.isShowing()){
			mPopMenu.dismiss();
		}
		mPopMenu.setPopType(type);
		mPopMenu.setOnItemClick(this);
		mPopMenu.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				mIvCitySharp.setVisibility(View.GONE);
				mIvCategorySharp.setVisibility(View.GONE);
				mIvCityArrow.setImageResource(R.drawable.home_arrow);
				mIvCategoryArrow.setImageResource(R.drawable.home_arrow);
			}
		});
		mPopMenu.clearList();
		if (type == POP_CITY){
			mIvCategorySharp.setVisibility(View.GONE);
			mIvCitySharp.setVisibility(View.VISIBLE);
			mIvCityArrow.setImageResource(R.drawable.search_arrow_up);
			for (City city : IeetonApplication.mCityList){
				mPopMenu.addItem(city.getCityName());
			}
		}else if (type == POP_CATEGORY){
			mIvCategorySharp.setVisibility(View.VISIBLE);
			mIvCategoryArrow.setImageResource(R.drawable.search_arrow_up);
			mIvCitySharp.setVisibility(View.GONE);
			List<ProductCategory> list = NetEngine.getProductCategoryList();
			for (ProductCategory category : list){
				mPopMenu.addItem(category.getCagegoryName());
			}
		}
		mPopMenu.showAsDropDown(v);
	}
	
	@Override
	public void onUpdate() {
		refreshData(LOAD_REFRESH);
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
					Utils.showToast(SearchProductActivity.this, R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					mAdapter.refresh(mProductList);
					//showEmpty(true);
					Utils.showToast(SearchProductActivity.this, R.string.no_search_data, Toast.LENGTH_SHORT);
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
				showProductList();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void showProductList(){
		mVgHotKeyword.setVisibility(View.GONE);
		mPullDownView.setVisibility(View.VISIBLE);
	}
	
	private void setHotKeyword(){
		if (mHotKeywordList == null || mHotKeywordList.isEmpty()){
			return;
		}
		for(int i=0; i<mTvHotKeywordList.size(); i++){
			mTvHotKeywordList.get(i).setText(mHotKeywordList.get(i));
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
			case FETCH_HOTKEYWORD:{
				try {
					result = NetEngine.getInstance(SearchProductActivity.this).getHotKeyword();
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
			case FETCH_SEARCH:{
				try {
					int categoryid = mCategory == null ? -1 : mCategory.getCategoryId();
					int lableid = mLable == null ? -1 : mLable.getId();
					result = NetEngine.getInstance(SearchProductActivity.this).
								searchProduct(lableid, categoryid, mCity.getLongitude(), 
										mCity.getLatitude(), mKeyword, 
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
					Utils.handleErrorEvent(mThr, SearchProductActivity.this);
				}else{
					Utils.showToast(SearchProductActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				if (mMode == FETCH_SEARCH && mLoadMode == LOAD_MORE){
					mPageNum--;
				}
				return;
			}
			switch(mMode){
			case FETCH_HOTKEYWORD:{
				try {
					JSONArray array = new JSONArray(result);
					if (mHotKeywordList == null){
						mHotKeywordList = new ArrayList<String>();
					}
					for (int i=0; i<array.length(); i++){
						String item = array.optJSONObject(i).optString("hotword");
						mHotKeywordList.add(item);
					}
					setHotKeyword();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
			case FETCH_SEARCH:{
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
}
