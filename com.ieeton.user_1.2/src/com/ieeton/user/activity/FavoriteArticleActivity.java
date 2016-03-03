package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Article;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.FavoriteArticleItemView;
import com.ieeton.user.view.PullDownView;
import com.ieeton.user.view.PullDownView.UpdateHandle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Toast;

public class FavoriteArticleActivity extends TemplateActivity implements UpdateHandle{
	private class ArticleListAdapter extends BaseAdapter{

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
			FavoriteArticleItemView view ;
			if (convertView == null){
				view = new FavoriteArticleItemView(FavoriteArticleActivity.this);
			}else{
				view = (FavoriteArticleItemView) convertView;
			}
			if (mList != null && !mList.isEmpty() && position < mList.size()){
				view.update(mList.get(position));
			}
			return view;
		}
		
	}
	
	private class FechDataTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			int page = params[0];
			try {
				result = NetEngine.getInstance(FavoriteArticleActivity.this)
							.getFavoriteArticle(page);
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
			//dismissProgress();
			if (mLoadMode == LOAD_MORE){
				mPageNum--;
			}
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			//dismissProgress();
			mPullDownView.endUpdate(null);
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(FavoriteArticleActivity.this, R.string.no_data, Toast.LENGTH_SHORT);
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
	
	private final int LOAD_REFRESH = 0;
	private final int LOAD_MORE = 1;
	
	public static final int MODE_DOCTOR = 1;		//专家
	public static final int MODE_INSTITUTION = 2;	//机构

	private List<Article> mList;
	private PullDownView mPullDownView;
	private SwipeListView mListView;
	private ArticleListAdapter mAdapter;
	
	private BroadcastReceiver mReceiver;
	private FechDataTask mTask;
	private int mPageNum = 1;
	private boolean mIsTaskFree = true;
	private int mLoadMode = LOAD_REFRESH;

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
		setView(R.layout.activity_favorite_article_list);
		setTitleBar(getString(R.string.back), getString(R.string.favorite_title_article), null);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.FAVORITE_ARTICLE_ACTION);
		filter.addAction(Constants.UNFAVORITE_ARTICLE_ACTION);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Constants.UNFAVORITE_ARTICLE_ACTION.equals(intent.getAction())){
					String articleId = intent.getExtras().getString(Constants.EXTRA_ARTICLEID);
					deleteItem(articleId);
				}
			}
		};
		registerReceiver(mReceiver, filter);
		
		mPullDownView = (PullDownView) findViewById(R.id.pulldown_view);
		mPullDownView.setUpdateHandle(this);
		
		mListView = (SwipeListView) findViewById(R.id.list);
		mAdapter = new ArticleListAdapter();
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
		mListView.setSwipeListViewListener(new BaseSwipeListViewListener(){

			@Override
			public void onClickFrontView(int position) {
				if (mList != null && position < mList.size()){
					Article article = mList.get(position);
					if (article == null){
						return;
					}
					Intent intent = new Intent(FavoriteArticleActivity.this, BrowserActivity.class);
					if(TextUtils.isEmpty(article.getExternalUrl())){
						//内部文章
						intent.putExtra(Constants.WEB_BROWSER_URL, Constants.SERVER_HOST_SHARE_ARTICLE_SERVER);
						intent.putExtra(Constants.WEB_BROWSER_INTERNAL, "true");
						intent.putExtra(Constants.WEB_BROWSER_TITLE, getResources().getString(R.string.artical_detail));
					}else{
						//外部文章，跳转到内置浏览器
						intent.putExtra(Constants.WEB_BROWSER_URL, article.getExternalUrl());
						intent.putExtra(Constants.WEB_BROWSER_INTERNAL, "false");
					}
					intent.putExtra(BrowserActivity.EXTRA_SHOW_BOTTOM, "true");
					intent.putExtra("artical", article);
					startActivity(intent);
				}
			}
			
		});
		
		if (mList == null || mList.isEmpty()){
			refreshData(LOAD_REFRESH);
		}
	}
	
	private void deleteItem(String articleId){
		if (mList == null || mList.isEmpty()){
			return;
		}
		for(Article article : mList){
			if (article.getID().equals(articleId)){
				mList.remove(article);
				mAdapter.notifyDataSetChanged();
				mListView.closeOpenedItems();
				if (mList.isEmpty()){
					showEmpty(true);
				}
				break;
			}
		}
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
		mTask = new FechDataTask();
		try {
			mTask.execute(mPageNum);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		if (mReceiver != null){
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		super.onDestroy();
	}

	@Override
	public void onUpdate() {
		refreshData(LOAD_REFRESH);
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
			JSONArray array = obj.optJSONArray("articles");
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
}
