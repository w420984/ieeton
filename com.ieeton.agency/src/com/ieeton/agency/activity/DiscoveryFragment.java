package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ieeton.agency.activity.ContactlistFragment.OperationReceiver;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.Article;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.DiscoverListItemView;
import com.ieeton.agency.view.PullDownView;
import com.ieeton.agency.view.PullDownView.UpdateHandle;
import com.ieeton.agency.R;
import com.umeng.analytics.MobclickAgent;

/**
 * 联系人列表页
 * 
 */
@SuppressLint("ResourceAsColor")
public class DiscoveryFragment extends Fragment implements OnClickListener, UpdateHandle{
	private boolean hidden;
	private List<Article> mRecommandArticalList;
	private List<Article> mAttendArticalList;
	private int mCurrentPageIndex           = 0;
	private ArticalListAdapter mRecommandArticalListAdapter;
	private ArticalListAdapter mAttendArticalListAdapter;
	private View mPageView;
	private RelativeLayout mRecommandTab;
	private RelativeLayout mAttendTab;
	private RelativeLayout mNoLogin;
	private Button mRegisterBtn;
	private Button mLoginBtn;
	private ImageView mSearchButton;

	private LoadRecommandArticalListTask mLoadRecommandArticalListTask;
	private LoadFollowedArticalListTask mLoadFollowedArticalListTask;
	private boolean mIsRecommandTaskFree = true;
	private boolean mIsFollowedTaskFree = true;

	private ListView mArticalListView;

	private int mRecommandPageCount = 1;
	private int mFollowedPageCount = 1;
	private boolean mRecommandHasMore = false;
	private boolean mFollowedHasMore = false;
	
	private final int Artical_LOADMORE = 0;
	private final int Artical_RELOAD = 1;
	private int mFetchDataType;
	private PullDownView mPullDownView;
	private Date mUpdateTime;
	public static final String LAST_UPDATE_TIME = "updateTime";
	protected int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
	
	private BroadcastReceiver mReceiver;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		mPageView = inflater.inflate(R.layout.fragment_discover_list, container, false);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.VIEW_ARTICLE_ACTION);
		filter.addAction(Constants.ACTION_LIKE_ARTICLE);
		filter.addAction(Constants.ACTION_UNLIKE_ARTICLE);
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
				if (Constants.VIEW_ARTICLE_ACTION.equals(action)
						|| Constants.ACTION_LIKE_ARTICLE.equals(action)
						|| Constants.ACTION_UNLIKE_ARTICLE.equals(action)
						|| Constants.FAVORITE_ARTICLE_ACTION.equals(action)
						|| Constants.UNFAVORITE_ARTICLE_ACTION.equals(action)){
					setArticle(id, action);
				}
			}
		};
		getActivity().registerReceiver(mReceiver, filter);
		
		mRecommandArticalList = new ArrayList<Article>();
		mAttendArticalList = new ArrayList<Article>();
		initView();
		refreshList();
		return mPageView;
	}

	private void setArticle(String id, String action){
		if (mRecommandArticalList != null && !mRecommandArticalList.isEmpty()){
			for (Article article: mRecommandArticalList){
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
					}else if (Constants.FAVORITE_ARTICLE_ACTION.equals(action)){
						article.setIsFavorited(1);
					}else if (Constants.UNFAVORITE_ARTICLE_ACTION.equals(action)){
						article.setIsFavorited(0);
					}
					mRecommandArticalListAdapter.notifyDataSetChanged();
					break;
				}
			}
		}
		if (mAttendArticalList != null && !mAttendArticalList.isEmpty()){
			for (Article article: mAttendArticalList){
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
					}else if (Constants.FAVORITE_ARTICLE_ACTION.equals(action)){
						article.setIsFavorited(1);
					}else if (Constants.UNFAVORITE_ARTICLE_ACTION.equals(action)){
						article.setIsFavorited(0);
					}
					mAttendArticalListAdapter.notifyDataSetChanged();
					break;
				}
			}
		}
	}
	
	@SuppressLint("ResourceAsColor")
	void initView(){
		mPullDownView = (PullDownView)mPageView.findViewById(R.id.list_pulldown_view);
		mPullDownView.setUpdateHandle(this);
		mPullDownView.setVisibility(View.VISIBLE);
		
		mSearchButton = (ImageView)mPageView.findViewById(R.id.iv_search);
		mSearchButton.setOnClickListener(this);
		
		mNoLogin = (RelativeLayout)mPageView.findViewById(R.id.rl_no_login);
		mNoLogin.setVisibility(View.GONE);

		mRegisterBtn = (Button)mPageView.findViewById(R.id.btn_register);
		mLoginBtn = (Button)mPageView.findViewById(R.id.btn_login);
		mRegisterBtn.setOnClickListener(this);
		mLoginBtn.setOnClickListener(this);
		
		mCurrentPageIndex = 0;
		
		mRecommandArticalListAdapter = new ArticalListAdapter();
		mAttendArticalListAdapter = new ArticalListAdapter();

		mArticalListView = (ListView) mPageView.findViewById(R.id.lv_discover_list);
		mArticalListView.setVisibility(View.VISIBLE);
//		mArticalListView.setDivider(getResources().getDrawable(R.drawable.list_divider));

		//默认进入附近的医生列表
		mArticalListView.setAdapter(mRecommandArticalListAdapter);
		
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
					loadMore();
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
		
        mRecommandTab = (RelativeLayout)mPageView.findViewById(R.id.rl_recommand);
        mRecommandTab.setOnClickListener(this);
        mAttendTab = (RelativeLayout)mPageView.findViewById(R.id.rl_attend);
        mAttendTab.setOnClickListener(this);
	}

	public void update(){
		if(mNoLogin != null){
			mNoLogin.setVisibility(View.GONE);
		}
		if(mPullDownView != null){
			mPullDownView.setVisibility(View.VISIBLE);
		}
	}
	
   class ArticalListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if(mCurrentPageIndex == 0){
				if (mRecommandArticalList != null && !mRecommandArticalList.isEmpty()){
					return mRecommandArticalList.size();
				}
				return 0;
			}else{
				if (mAttendArticalList != null && !mAttendArticalList.isEmpty()){
					return mAttendArticalList.size();
				}
				return 0;
			}
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
			
			if(mCurrentPageIndex == 0){
				if(mRecommandArticalList != null && mRecommandArticalList.size() > 0 && position < mRecommandArticalList.size()){
					view.update(mRecommandArticalList.get(position), false, null);
				}
			}else{
				if(mAttendArticalList != null && mAttendArticalList.size() > 0 && position < mAttendArticalList.size()){
					view.update(mAttendArticalList.get(position), false, null);
				}
			}
			return view;
		}
		
	}

	@Override
	public void onDestroy() {
		if (mLoadFollowedArticalListTask != null 
				&& mLoadFollowedArticalListTask.getStatus() == AsyncTask.Status.RUNNING){
			mLoadFollowedArticalListTask.cancel(true);
		}
		if (mLoadRecommandArticalListTask != null 
				&& mLoadRecommandArticalListTask.getStatus() == AsyncTask.Status.RUNNING){
			mLoadRecommandArticalListTask.cancel(true);
		}
		if (mReceiver != null){
			getActivity().unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		super.onDestroy();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if (!hidden) {
			refresh();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("DiscoveryFragment"); 
		if (!hidden) {
			refresh();
		}
		if(mRecommandArticalListAdapter != null){
			mRecommandArticalListAdapter.notifyDataSetChanged();
		}
		if(mAttendArticalListAdapter != null){
			mAttendArticalListAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onPause() {
		MobclickAgent.onPageEnd("DiscoveryFragment"); 
		super.onPause();
	}
	
	private void loadTaskBegin(int method) {
		mFetchDataType = method;
		if (method == Artical_RELOAD) {
			mPullDownView.update();
		} else if (method == Artical_LOADMORE) {
			mPullDownView.updateWithoutOffset();
		}
	}
	
	private Date getUpdateTime() {
		if (mUpdateTime == null) {
			SharedPreferences preferences = getActivity().getSharedPreferences(
					LAST_UPDATE_TIME, Activity.MODE_PRIVATE);
			long time = preferences.getLong(LAST_UPDATE_TIME, 0);
			if (time == 0) {
				mUpdateTime = new Date();
			} else {
				mUpdateTime = new Date(time);
			}
		} else {
			mUpdateTime = new Date();
			new Thread(new Runnable() {

				@Override
				public void run() {
					SharedPreferences preferences = getActivity().getSharedPreferences(
							LAST_UPDATE_TIME, Activity.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putLong(LAST_UPDATE_TIME,
							mUpdateTime.getTime());
					editor.commit();
				}
			}).start();
		}
		return mUpdateTime;
	}
	private void loadTaskEnd(boolean success) {
		if (mPullDownView != null) {
			if (success) {
				mPullDownView.endUpdate(getUpdateTime());
			} else {
				mPullDownView.endUpdate(null);
			}
		}

//		mArticalListView.setSelection(1);
	}
	// 刷新ui
	public void refresh() {
		//ieeton暂时不需要好友功能，医生列表与环信无关
/*		try {
			// 可能会在子线程中调到这方法
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					getContactList();
					adapter.notifyDataSetChanged();

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		if(mCurrentPageIndex == 0){
			if(mRecommandArticalList != null && mRecommandArticalList.size() == 0){
				refreshList();
			}
		}else{
			if(mAttendArticalList != null && mAttendArticalList.size() == 0){
				refreshList();
			}
		}
	}

	public void refreshList(){
//		mArticalListView.setSelection(0);
		if(mCurrentPageIndex == 0){
			if(!mIsRecommandTaskFree){
				return;
			}
//			if(DemoApplication.mIeetonLocation == null){
//				Toast.makeText(getActivity(), getString(R.string.location_failed), Toast.LENGTH_LONG).show();
//				return;
//			}
			mRecommandPageCount = 1;
			loadTaskBegin(Artical_RELOAD);
			try{
				mLoadRecommandArticalListTask = new LoadRecommandArticalListTask();
				mLoadRecommandArticalListTask.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
				loadTaskEnd(false);
			}
		}else{
			if(!mIsFollowedTaskFree){
				return;
			}
			mFollowedPageCount = 1;
			loadTaskBegin(Artical_RELOAD);
			try{
				mLoadFollowedArticalListTask = new LoadFollowedArticalListTask();
				mLoadFollowedArticalListTask.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
				loadTaskEnd(false);
			}			
		}
	}

	public void loadMore(){
		if(mCurrentPageIndex == 0){
			if(!mRecommandHasMore){
				Toast.makeText(getActivity(), getString(R.string.no_more_data), Toast.LENGTH_SHORT).show();
				return;
			}
			if(!mIsRecommandTaskFree){
				return;
			}
			mRecommandPageCount += 1;
			loadTaskBegin(Artical_LOADMORE);
			try{
				mLoadRecommandArticalListTask = new LoadRecommandArticalListTask();
				mLoadRecommandArticalListTask.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}
		}else{
			if(!mFollowedHasMore){
				Toast.makeText(getActivity(), getString(R.string.no_more_data), Toast.LENGTH_SHORT).show();
				return;
			}
			if(!mIsFollowedTaskFree){
				return;
			}
			mFollowedPageCount += 1;
			loadTaskBegin(Artical_LOADMORE);
			try{
				mLoadFollowedArticalListTask = new LoadFollowedArticalListTask();
				mLoadFollowedArticalListTask.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}			
		}
	}
	
	class LoadRecommandArticalListTask extends AsyncTask<Void, Void, List<Article>>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsRecommandTaskFree = false;
		}

		@Override
		protected List<Article> doInBackground(Void... arg0) {
			String result = "";

				try {
					result = NetEngine.getInstance(getActivity())
									.getAllArticalList(Utils.getPassport(getActivity()), Constants.MAX_PAGE_SIZE, mRecommandPageCount);
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

				JSONObject object = null;
				try {
					object = new JSONObject(result);
					if(!object.getBoolean("error")){
						JSONObject json_data = object.getJSONObject("messages").getJSONObject("data");
						JSONArray array = json_data.getJSONArray("articles");
						int cur_count = json_data.getInt("count");
						int page_size = json_data.getInt("pageSize");
						if(cur_count > 0 && cur_count >= page_size){
							mRecommandHasMore = true;
						}else{
							mRecommandHasMore = false;
						}
						
						List<Article> list = new ArrayList<Article>();
						for(int i=0; i<array.length(); i++){
							Article item = new Article((JSONObject)array.get(i));
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
		protected void onPostExecute(List<Article> result) {
			mIsRecommandTaskFree = true;
			
			if (result == null){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getActivity());
				}else{
					Utils.showToast(getActivity(), R.string.no_more_articals, Toast.LENGTH_SHORT);
				}
				loadTaskEnd(false);
				return;
			}
			loadTaskEnd(true);
			if(result.size() == 0){
				if(mRecommandHasMore){
					mRecommandHasMore = false;
					Utils.showToast(getActivity(), R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					Utils.showToast(getActivity(), R.string.no_more_articals, Toast.LENGTH_SHORT);
				}
				return;
			}
			if(mFetchDataType == Artical_RELOAD){
				if(mRecommandArticalList != null){
					mRecommandArticalList.clear();
				}
				mRecommandArticalList = result;
			}else{
				mRecommandArticalList.addAll(result);
			}
			if(mRecommandArticalListAdapter != null){
				mRecommandArticalListAdapter.notifyDataSetChanged();
			}

		}
		
	}

	class LoadFollowedArticalListTask extends AsyncTask<Void, Void, List<Article>>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsFollowedTaskFree = false;
		}

		@Override
		protected List<Article> doInBackground(Void... arg0) {
			String result = "";

				try {
					result = NetEngine.getInstance(getActivity())
									.getFollowedArticalList(Utils.getPassport(getActivity()), Constants.MAX_PAGE_SIZE, mFollowedPageCount);
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

				JSONObject object = null;
				try {
					object = new JSONObject(result);
					if(!object.getBoolean("error")){
						JSONObject json_data = object.getJSONObject("messages").getJSONObject("data");
						JSONArray array = json_data.getJSONArray("articles");
						int cur_count = json_data.getInt("count");
						int page_size = json_data.getInt("pageSize");
						if(cur_count > 0 && cur_count >= page_size){
							mFollowedHasMore = true;
						}else{
							mFollowedHasMore = false;
						}
						
						List<Article> list = new ArrayList<Article>();
						for(int i=0; i<array.length(); i++){
							Article item = new Article((JSONObject)array.get(i));
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
		protected void onPostExecute(List<Article> result) {
			mIsFollowedTaskFree = true;
			//关注列表没有数据需清空之前的记录
			if(mFetchDataType == Artical_RELOAD){
				if(mAttendArticalList != null){
					mAttendArticalList.clear();
				}
				if (result != null){
					mAttendArticalList = result;
				}
			}else{
				if (result != null){
					mAttendArticalList.addAll(result);
				}
			}

			if (result == null){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getActivity());
				}else{
					Utils.showToast(getActivity(), R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				loadTaskEnd(false);
				return;
			}

			loadTaskEnd(true);
			if(result.size() == 0){
				if(mFollowedHasMore){
					mFollowedHasMore = false;
					Utils.showToast(getActivity(), R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					Utils.showToast(getActivity(), R.string.no_followed_data, Toast.LENGTH_SHORT);
				}
				return;
			}
			if(mAttendArticalListAdapter != null){
				mAttendArticalListAdapter.notifyDataSetChanged();
			}

		}
		
	}

	@Override
	public void onClick(View v) {
		if(v == mLoginBtn){
			startActivity(new Intent(getActivity(), LoginActivity.class));
		}else if(v == mRegisterBtn){
			startActivity(new Intent(getActivity(), RegisterActivity.class));
		}else if(v == mSearchButton){
			Intent intent = new Intent(getActivity(), SearchArticleActivity.class);
			startActivity(intent);
		}else if(v == mRecommandTab){
			mCurrentPageIndex = 0;
			mRecommandTab.setBackgroundResource(R.color.ieeton_color_blue);
			mAttendTab.setBackgroundResource(R.color.ieeton_color_gray);
			mArticalListView.setAdapter(mRecommandArticalListAdapter);
			mPullDownView.setVisibility(View.VISIBLE);
			mNoLogin.setVisibility(View.GONE);
			refreshList();
		}else if(v == mAttendTab){
			mCurrentPageIndex = 1;
			mRecommandTab.setBackgroundResource(R.color.ieeton_color_gray);
			mAttendTab.setBackgroundResource(R.color.ieeton_color_blue);
			mArticalListView.setAdapter(mAttendArticalListAdapter);
			
			if(Utils.getPassport(getActivity()).equals("")){
				mNoLogin.setVisibility(View.VISIBLE);
				mPullDownView.setVisibility(View.GONE);
			}else{
				mNoLogin.setVisibility(View.GONE);
				mPullDownView.setVisibility(View.VISIBLE);
				refreshList();
			}
		
		}
	}

	@Override
	public void onUpdate() {
		refreshList();
	}
}
