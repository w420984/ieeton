package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;
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
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Toast;
import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Article;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.DiscoverListItemView;
import com.ieeton.user.view.MyListView;
import com.ieeton.user.view.MyViewPager;
import com.ieeton.user.view.PullDownView;
import com.ieeton.user.view.PullDownView.UpdateHandle;
import com.umeng.analytics.MobclickAgent;

public class DiscoveryFragment extends Fragment implements OnClickListener, UpdateHandle{
	private List<Article> mRecommandList;
	private List<Article> mAllArticleList;
	private ArticalListAdapter mRecommandListAdapter;
	private ArticalListAdapter mAllArticleListAdapter;
	private View mView;
	private MyViewPager mPager;
	
	private FetchDataTask mTask;
	private boolean mIsTaskFree = true;
	private MyListView mArticalListView;
	private int mPageNum = 1;

	private BroadcastReceiver mReceiver;
	private final int LOAD_REFRESH = 0;
	private final int LOAD_MORE = 1;
	private int mLoadMode;
	private PullDownView mPullDownView;
	protected int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		mView = inflater.inflate(R.layout.fragment_discover_list, container, false);
		
		initView();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.FAVORITE_ARTICLE_ACTION);
		filter.addAction(Constants.UNFAVORITE_ARTICLE_ACTION);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				String id = intent.getStringExtra(Constants.EXTRA_ARTICLEID);
				if (TextUtils.isEmpty(id)){
					return;
				}
				if (Constants.FAVORITE_ARTICLE_ACTION.equals(action)
						|| Constants.UNFAVORITE_ARTICLE_ACTION.equals(action)){
					setArticle(id, action);
				}
			}
		};
		getActivity().registerReceiver(mReceiver, filter);
		refreshData(LOAD_REFRESH);
		return mView;
	}
	
	private void setArticle(String id, String action){
		if (mAllArticleList != null && !mAllArticleList.isEmpty()){
			for (Article article: mAllArticleList){
				if (article.getID().equals(id)){
					if (Constants.FAVORITE_ARTICLE_ACTION.equals(action)){
						article.setIsFavorited(1);
					}else if (Constants.UNFAVORITE_ARTICLE_ACTION.equals(action)){
						article.setIsFavorited(0);
					}
					mAllArticleListAdapter.notifyDataSetChanged();
					break;
				}
			}
		}
		if (mRecommandList != null && !mRecommandList.isEmpty()){
			for (Article article: mRecommandList){
				if (article.getID().equals(id)){
					if (article.getID().equals(id)){
						if (Constants.FAVORITE_ARTICLE_ACTION.equals(action)){
							article.setIsFavorited(1);
						}else if (Constants.UNFAVORITE_ARTICLE_ACTION.equals(action)){
							article.setIsFavorited(0);
						}
						mRecommandListAdapter.notifyDataSetChanged();
						break;
					}
				}
			}
		}
	}

	void initView(){
		
		mPullDownView = (PullDownView)mView.findViewById(R.id.list_pulldown_view);
		mPullDownView.setUpdateHandle(this);
		mPullDownView.setVisibility(View.VISIBLE);
				
		mRecommandListAdapter = new ArticalListAdapter();
		mAllArticleListAdapter = new ArticalListAdapter();

		mArticalListView = (MyListView) mView.findViewById(R.id.lv_discover_list);
		mPager = new MyViewPager(getActivity(), this);
		mArticalListView.addHeaderView(mPager);
		mArticalListView.setAdapter(mAllArticleListAdapter);
		
		mArticalListView.setOnScrollListener(new OnScrollListener() {
			private boolean retrievable;
			private boolean isEnd;
			
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
		});
		mArticalListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long arg3) {
				Article article = mAllArticleList.get(position-1);
				Intent intent = new Intent(getActivity(), BrowserActivity.class);
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
		});
	}

	public void update(){
		if(mPullDownView != null){
			mPullDownView.setVisibility(View.VISIBLE);
		}
	}
	
   private class ArticalListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mAllArticleList != null && !mAllArticleList.isEmpty()){
				return mAllArticleList.size();
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
			DiscoverListItemView view = null;
			if (convertView == null){
				view = new DiscoverListItemView(getActivity());
			}else{
				view = (DiscoverListItemView)convertView;
			}
			
			if(mAllArticleList != null && mAllArticleList.size() > 0 && position < mAllArticleList.size()){
				view.update(mAllArticleList.get(position));
			}
			return view;
		}
		
	}

	@Override
	public void onDestroy() {
		if (mTask != null 
				&& mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		if (mReceiver != null){
			getActivity().unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("DiscoveryFragment"); 
	}

	@Override
	public void onPause() {
		MobclickAgent.onPageEnd("DiscoveryFragment"); 
		super.onPause();
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
	
	private class FetchDataTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;
	    private int page;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsTaskFree = false;
		}

		@Override
		protected String doInBackground(Integer... arg0) {
			String result = "";
			page = arg0[0];
			try {
				result = NetEngine.getInstance(getActivity()).getAllArticalList(page);
			} catch (PediatricsIOException e) {
				e.printStackTrace();
				mThr = e;
			} catch (PediatricsParseException e) {
				e.printStackTrace();
				mThr = e;
			} catch (PediatricsApiException e) {
				e.printStackTrace();
				mThr = e;
			}

			return result;
		}

		@Override
		protected void onCancelled() {
			mIsTaskFree = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mPullDownView.endUpdate(null);
			mIsTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getActivity());
				}else{
					Utils.showToast(getActivity(), R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
				}
				return;
			}
			processResult(result);
		}
	}
	
	private void processResult(String result){
		if (TextUtils.isEmpty(result)){
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			if (mRecommandList == null){
				mRecommandList = new ArrayList<Article>();
			}
			JSONArray array = obj.optJSONObject("recommend").optJSONArray("articles");
			if (array != null && array.length() > 0){
				mRecommandList.clear();
				for(int i=0; i<array.length(); i++){
					Article item = new Article(array.optJSONObject(i));
					mRecommandList.add(item);
				}
			}
			mPager.showPager(mRecommandList);
			
			array = obj.optJSONObject("articles").optJSONArray("articles");
			if (array == null || array.length() == 0){
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
					Utils.showToast(getActivity(), R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					mAllArticleListAdapter.notifyDataSetChanged();
					//showEmpty(true);
				}
				return;
			}
			if (mAllArticleList == null){
				mAllArticleList = new ArrayList<Article>();
			}
			List<Article> list = new ArrayList<Article>();
			for(int i=0; i<array.length(); i++){
				Article item = new Article(array.optJSONObject(i));
				list.add(item);
			}
			if (mLoadMode == LOAD_REFRESH){
				mAllArticleList.clear();
			}
			mAllArticleList.addAll(list);
			mAllArticleListAdapter.notifyDataSetChanged();
			if (mAllArticleList.size()>0){
				//showEmpty(false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		
	}

	@Override
	public void onUpdate() {
		refreshData(LOAD_REFRESH);
	}
		
}
