package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.Article;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.DiscoverListItemView;
import com.ieeton.agency.view.PullDownView;
import com.ieeton.agency.view.PullDownView.UpdateHandle;
import com.ieeton.agency.R;

public class MyFavoriteActivity extends TemplateActivity implements
		UpdateHandle, OnScrollListener {
	public static String EXTRA_ARTICLE = "extra_article";
	private final int LOAD_REFRESH = 0;
	private final int LOAD_MORE = 1;

	private List<Article> mList;
	private PullDownView mPullDownView;
	private ListView mListView;
	private FavoriteListAdapter mAdapter;
	
	private ViewGroup mVgDelete;
	
	private FechDataTask mTask;
	private UnFavoriteTask mUnFavoriteTask;
	private int mPageNum = 1;
	private boolean mIsTaskFree = true;
	private int mLoadMode = LOAD_REFRESH;
	private CustomToast mProgressDialog;

	private boolean isEditorMode = false;
	private HashMap<String , Boolean> mMap = new HashMap<String, Boolean>();
	private BroadcastReceiver mReceiver;
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			if (!isEditorMode){
				if (mAdapter == null || mList == null || mList.isEmpty()){
					return;
				}
				//禁用下拉刷新功能
				mPullDownView.setEnable(false);
				mMap.clear();
				isEditorMode = true;
				setTitleBar(getString(R.string.back), getString(R.string.my_favorite), getString(R.string.finished));
				mAdapter.showCheckBox(true);
				mAdapter.notifyDataSetChanged();
				mVgDelete.setVisibility(View.VISIBLE);
			}else{
				//恢复下拉刷新功能
				mPullDownView.setEnable(true);
				isEditorMode = false;
				setTitleBar(getString(R.string.back), getString(R.string.my_favorite), getString(R.string.editor));
				mAdapter.showCheckBox(false);
				mAdapter.notifyDataSetChanged();
				mVgDelete.setVisibility(View.GONE);
			}
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mVgDelete){
			if (mAdapter == null || mList == null || mList.isEmpty()){
				return;
			}
			
			Utils.logd("map:"+mMap);
			if (mMap.isEmpty()){
				return;
			}
			String idList = "";
			int i=0;
			Iterator iterator = mMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry entry = (java.util.Map.Entry)iterator.next();
				//Utils.logd("key:"+entry.getKey() + "  value:"+entry.getValue());
				if (i>0){
					idList += ",";
				}
				idList += entry.getKey();
				i++;
			}
			Utils.logd("idList:"+idList);
			if (mIsTaskFree && !TextUtils.isEmpty(idList)){
				try {
					mUnFavoriteTask = new UnFavoriteTask();
					mUnFavoriteTask.execute(idList);
				} catch (RejectedExecutionException e) {
					e.printStackTrace();
				}
			}			
		}
		super.onClick(v);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.favorite_list);
		setTitleBar(getString(R.string.back), getString(R.string.my_favorite), getString(R.string.editor));

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.FAVORITE_ARTICLE_ACTION);
		filter.addAction(Constants.UNFAVORITE_ARTICLE_ACTION);
		filter.addAction(Constants.VIEW_ARTICLE_ACTION);
		filter.addAction(Constants.ACTION_LIKE_ARTICLE);
		filter.addAction(Constants.ACTION_UNLIKE_ARTICLE);
		mReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Constants.FAVORITE_ARTICLE_ACTION.equals(intent.getAction())){
					Article article = (Article) intent.getSerializableExtra(EXTRA_ARTICLE);
					if (article == null){
						return;
					}
					insertArticle(article);
				}else if (Constants.UNFAVORITE_ARTICLE_ACTION.equals(intent.getAction())){
					Article article = (Article) intent.getSerializableExtra(EXTRA_ARTICLE);
					if (article == null){
						return;
					}
					deleteArticles(article.getID());
				}else if (Constants.VIEW_ARTICLE_ACTION.equals(intent.getAction())
						|| Constants.ACTION_LIKE_ARTICLE.equals(intent.getAction())
						|| Constants.ACTION_UNLIKE_ARTICLE.equals(intent.getAction())){
					String id = intent.getStringExtra(Constants.EXTRA_ARTICLEID);
					if (TextUtils.isEmpty(id)){
						return;
					}
					setArticle(id, intent.getAction());
				}
			}
		};
		registerReceiver(mReceiver, filter);
		
		
		mVgDelete = (ViewGroup) findViewById(R.id.ll_bottom);
		mVgDelete.setOnClickListener(this);
		
		mPullDownView = (PullDownView) findViewById(R.id.pulldown_view);
		mPullDownView.setUpdateHandle(this);
		
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new FavoriteListAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this);

		refreshData(LOAD_REFRESH);
	}
	
	private void setArticle(String id, String action){
		if (mList != null && !mList.isEmpty()){
			for (Article article: mList){
				if (article.getID().equals(id)){
					int num = article.getLikedNums();
					if (Constants.ACTION_LIKE_ARTICLE.equals(action)){
						article.setLikedNums(num + 1);
						article.setIsLiked(1);
					}else if (Constants.ACTION_UNLIKE_ARTICLE.equals(action)){
						article.setLikedNums(num-1>0 ? num-1 : 0);
						article.setIsLiked(0);
					}else if (Constants.VIEW_ARTICLE_ACTION.equals(action)){
						article.addReadNums();
					}
					mAdapter.notifyDataSetChanged();
					break;
				}
			}
		}
	}
	
	private void insertArticle(Article article){
		if (mList == null){
			mList = new ArrayList<Article>();
		}
		mList.add(0, article);
		mAdapter.notifyDataSetChanged();
	}
	
	private void deleteArticles(String ids){
		if (TextUtils.isEmpty(ids)){
			return;
		}
		if (mList == null || mList.isEmpty()){
			return;
		}
		String [] list = ids.split(",");
		if (list == null || list.length == 0){
			return;
		}
		Utils.logd("list:"+list);
		for (String id : list){
			Utils.logd("id:"+id);
			for(Article article : mList){
				if (article.getID().equals(id)){
					mList.remove(article);
					break;
				}
			}
		}
		mAdapter.notifyDataSetChanged();
	}
	
	private void refreshData(int mode){
		if (isEditorMode){
			return;
		}
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
		mTask = new FechDataTask();
		try {
			mTask.execute(mPageNum);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private void showEmpty(boolean isEmpty){
		if (isEmpty){
			mPullDownView.setVisibility(View.GONE);
			findViewById(R.id.tv_empty).setVisibility(View.VISIBLE);
		}else{
			mPullDownView.setVisibility(View.VISIBLE);
			findViewById(R.id.tv_empty).setVisibility(View.GONE);
		}
	}
	
	private void processResult(String result){
		if (TextUtils.isEmpty(result)){
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			JSONObject data = obj.optJSONObject("messages").optJSONObject("data");
			JSONArray array;
			array = data.optJSONArray("articles");
			if (array == null || array.length() == 0){
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
					Utils.showToast(this, R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					showEmpty(true);
				}
				return;
			}
			if (mList == null){
				mList = new ArrayList<Article>();
			}
			List<Article> list = new ArrayList<Article>();
			for(int i=0; i<array.length(); i++){
				Article item = new Article(array.optJSONObject(i));
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

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		if (mUnFavoriteTask != null && mUnFavoriteTask.getStatus() == AsyncTask.Status.RUNNING){
			mUnFavoriteTask.cancel(true);
		}
		if (mReceiver != null){
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
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

	class UnFavoriteTask extends AsyncTask<String, Void, String>{
		private Throwable mThr;
		private String ids;

		@Override
		protected String doInBackground(String... params) {
			String result = "";
			ids = params[0];
			try {
				result = NetEngine.getInstance(MyFavoriteActivity.this).
							unFavoriteArtical(Utils.getPassport(MyFavoriteActivity.this), ids);
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
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(MyFavoriteActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			deleteArticles(ids);
			Utils.showToast(MyFavoriteActivity.this, R.string.article_unfavorite_success, Toast.LENGTH_SHORT);
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	class FechDataTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			int page = params[0];
			try {
				result = NetEngine.getInstance(MyFavoriteActivity.this)
							.getFavoriteArticle(Utils.getPassport(MyFavoriteActivity.this), 
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
					Utils.showToast(MyFavoriteActivity.this, R.string.no_data, Toast.LENGTH_SHORT);
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
			//showProgress();
			super.onPreExecute();
		}
		
	}
	
	class FavoriteListAdapter extends BaseAdapter{
		private boolean isShowCheckBox = false;
		
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
			DiscoverListItemView view;
			if (convertView == null){
				view = new DiscoverListItemView(MyFavoriteActivity.this);
			}else{
				view = (DiscoverListItemView) convertView;
			}
			if (mList != null && !mList.isEmpty() && position < mList.size()){
				view.update(mList.get(position), isShowCheckBox, mMap);
				view.getCheckBox().setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						CheckBox view = (CheckBox) v;
						mMap.put(mList.get(position).getID(), view.isChecked());
					}
				});
			}
			return view;
		}
		
		public void showCheckBox(boolean isShow){
			isShowCheckBox = isShow;
		}
		
		public HashMap<String, Boolean> getCheckMap(){
			return mMap;
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

	@Override
	public void onUpdate() {
		refreshData(LOAD_REFRESH);
	}

}
