package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.Article;
import com.ieeton.agency.models.ChatUser;
import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.models.Patient;
import com.ieeton.agency.models.UserInfo;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.AsyncBitmapLoader;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.utils.AsyncBitmapLoader.ImageCallBack;
import com.ieeton.agency.view.DiscoverListItemView;
import com.ieeton.agency.view.PullDownView;
import com.ieeton.agency.view.PullDownView.UpdateHandle;
import com.ieeton.agency.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class UserProfileActivity extends TemplateActivity{
	public static String EXTRA_USERINFO = "extra_userinfo";
	
	private ImageView mActionBtn;
	private TextView mTitle;
	private PullDownView mPullDownView;
	private ListView mListView;
	private View mUserHeaderView;
	private Date mUpdateTime;
	public static final String LAST_UPDATE_TIME = "updateTime";
	protected int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
	
	private LoadArticalListTask mLoadArticalListTask;
	private boolean mIsRecommandTaskFree = true;
	private List<Article> mArticalList;
	private ArticalListAdapter mArticalListAdapter;
	
	private int mPageCount = 1;
	private boolean mHasMore = true;
	
	private boolean mIsTaskFree = true;
	
	private final int Artical_LOADMORE = 0;
	private final int Artical_RELOAD = 1;
	private int mFetchDataType;
	
	private ChatUser mChatUser;
	//private Article mArtical;
	private UserInfo mUser;
	
	private boolean mIsFollowTaskFree = true;
	private final int ACTION_FOLLOW = 0;
	private final int ACTION_UNFOLLOW = 1;
	
	private BroadcastReceiver mReceiver;

	@Override
	protected void handleTitleBarEvent(int eventId) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.user_profile_activity);
		setTitleBar(null, null, null);
		
		init();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.VIEW_ARTICLE_ACTION);
		filter.addAction(Constants.ACTION_LIKE_ARTICLE);
		filter.addAction(Constants.ACTION_UNLIKE_ARTICLE);
		filter.addAction(Constants.FAVORITE_ARTICLE_ACTION);
		filter.addAction(Constants.UNFAVORITE_ARTICLE_ACTION);
		mReceiver = new BroadcastReceiver(){
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
		registerReceiver(mReceiver, filter);
	}
	
	private void setArticle(String id, String action){
		if (mArticalList != null && !mArticalList.isEmpty()){
			for (Article article: mArticalList){
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
					mArticalListAdapter.notifyDataSetChanged();
					break;
				}
			}
		}
	}

	public void init(){
		initDataIntent();
		initView();
		
		if(mIsTaskFree){
			try{
				GetUserInfoTask task = new GetUserInfoTask();
				task.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}
		}
		mPageCount = 0;
		loadMore(Artical_RELOAD);
	}
	
	public void initDataIntent(){
		Intent intent = getIntent();
		if(intent == null){
			finish();
			return;
		}
		
		mChatUser = (ChatUser)intent.getSerializableExtra(EXTRA_USERINFO);
	}
	
	public void initView(){
		initHeaderView();
		
		View backBtn = findViewById(R.id.ll_back);
		backBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		mPullDownView = (PullDownView)findViewById(R.id.list_pulldown_view);
		mPullDownView.setUpdateHandle(new UpdateHandle() {
			@Override
			public void onUpdate() {
				if(mIsTaskFree){
					try{
						GetUserInfoTask task = new GetUserInfoTask();
						task.execute();
					}catch(RejectedExecutionException e){
						e.printStackTrace();
					}
				}
				mPageCount = 0;
				loadMore(Artical_RELOAD);
			}
		});
		mListView = (ListView)findViewById(R.id.lv_artical_list);
		mArticalListAdapter = new ArticalListAdapter();
		
		mListView.setOnScrollListener(new OnScrollListener() {
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
					loadMore(Artical_LOADMORE);
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
		
		mListView.addHeaderView(mUserHeaderView);
		mListView.setAdapter(mArticalListAdapter);
		
		mActionBtn = (ImageView)findViewById(R.id.btn_follow_unfollow);
		setFollowIcon(false);
		mActionBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(Utils.getPassport(UserProfileActivity.this).equals("")){
					Toast.makeText(UserProfileActivity.this, getString(R.string.no_login_wranning), Toast.LENGTH_SHORT).show();
					return;
				}
				if (mUser == null){
					return;
				}
				//follow/unfollow
				if(mIsFollowTaskFree){
					try{
						FollowUnfollowUserTask task = null;
						if (isFollowed() == -1){
							return;
						}
						if(isFollowed() == 0){
							task = new FollowUnfollowUserTask(ACTION_FOLLOW);
						}else{
							task = new FollowUnfollowUserTask(ACTION_UNFOLLOW);
						}
						task.execute();
					}catch(RejectedExecutionException e){
						e.printStackTrace();
					}
				}
				
			}
		});
		
		mTitle = (TextView)findViewById(R.id.tv_title);
		mTitle.setText(mChatUser.getName());
	}
	
	@Override
	protected void onDestroy() {
		if (mReceiver != null){
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		super.onDestroy();
	}

	private void setFollowIcon(boolean isShow){
		if (!isShow){
			mActionBtn.setVisibility(View.GONE);
		}else {
			mActionBtn.setVisibility(View.VISIBLE);
			if(isFollowed() == 0){
				mActionBtn.setImageResource(R.drawable.icon_follow_m);
			}else{
				mActionBtn.setImageResource(R.drawable.icon_followed_m);
			}
		}
	}
	
	private int isFollowed(){
		int status = -1;
		if (mUser == null){
			return status;
		}
		if (mUser.getUserType() == UserInfo.ACCOUNT_DOCTOR){
			return mUser.getDoctor().getIsFollowedStatus();
		}else if (mUser.getUserType() == UserInfo.ACCOUNT_PATIENT){
			return mUser.getPatient().getIsFollowedStatus();
		}
		return status;
	}
	
	public void initHeaderView(){
		mUserHeaderView = LayoutInflater.from(UserProfileActivity.this).inflate(R.layout.user_info_header_view, null);
		mUserHeaderView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				if(mChatUser.getType() == ChatUser.USER_DOCTOR){
					Intent intent = new Intent(UserProfileActivity.this, UserProfileActivity.class);
					intent.putExtra(Constants.EXTRA_DOCTORID, mChatUser.getId());
					startActivity(intent);
					return;
				}
			}
		});
		
		RelativeLayout rl_consult = (RelativeLayout)mUserHeaderView.findViewById(R.id.rl_consult);
		rl_consult.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
				intent.putExtra(ChatActivity.EXTRA_USERID, mChatUser.getId());
				intent.putExtra(ChatActivity.EXTRA_USERINFO, mChatUser);
				
				startActivity(intent);
			}
		});
	}
	
	public void updateHeaderView(){
		final ImageView portrait = (ImageView)mUserHeaderView.findViewById(R.id.iv_portrait);
		TextView nick = (TextView)mUserHeaderView.findViewById(R.id.tv_nick);
		TextView intro = (TextView)mUserHeaderView.findViewById(R.id.tv_intro);
		TextView fansNums = (TextView)mUserHeaderView.findViewById(R.id.tv_fans_num);
		TextView articalNums = (TextView)mUserHeaderView.findViewById(R.id.tv_articals_num);
		
		if (NetEngine.getFeedbackId().equals(mChatUser.getId())){
			fansNums.setVisibility(View.GONE);
			articalNums.setVisibility(View.GONE);
		}
		
		if(mUser != null){
			if(mUser.getUserType() == UserInfo.ACCOUNT_DOCTOR){
				nick.setText(mUser.getDoctor().getDoctorName());
				intro.setText(mUser.getDoctor().getSkillDescription());
				fansNums.setText(getString(R.string.fans) + " " + mUser.getDoctor().getFansNum());
				articalNums.setText(getString(R.string.article) + " " + mUser.getDoctor().getArticalNum());
				Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(this, mUser.getDoctor().getID(), 
						NetEngine.getImageUrl(mUser.getDoctor().getPortraitUrl()), 
						ChatUser.USER_DOCTOR, new ImageCallBack() {
							@Override
							public void imageLoad(Bitmap bitmap, Object user) {
								if (bitmap !=null && !bitmap.isRecycled()){
									portrait.setImageBitmap(bitmap);
								}else{
									portrait.setImageResource(Utils.getDefaultPortraitId(ChatUser.USER_DOCTOR, mUser.getDoctor()));
								}
							}
						});
				
				if (b !=null && !b.isRecycled()){
					portrait.setImageBitmap(b);
				}else{
					portrait.setImageResource(Utils.getDefaultPortraitId(ChatUser.USER_DOCTOR, mUser.getDoctor()));
				}
			}else if(mUser.getUserType() == UserInfo.ACCOUNT_PATIENT){
				nick.setText(mUser.getPatient().getNick());
				fansNums.setText(getString(R.string.fans) + " " + mUser.getPatient().getFollowCount());
				intro.setText(mUser.getPatient().getDescription());
				articalNums.setText(getString(R.string.article) + " " + mUser.getPatient().getArticleCount());
				Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(this, mUser.getPatient().getID(), 
						NetEngine.getImageUrl(mUser.getPatient().getPortraitUrl()), 
						ChatUser.USER_PATIENT, new ImageCallBack() {
					@Override
					public void imageLoad(Bitmap bitmap, Object user) {
						if (bitmap !=null && !bitmap.isRecycled()){
							portrait.setImageBitmap(bitmap);
						}else{
							portrait.setImageResource(Utils.getDefaultPortraitId(ChatUser.USER_PATIENT, mUser.getPatient()));
						}
					}
				});
				if (b !=null && !b.isRecycled()){
					portrait.setImageBitmap(b);
				}else{
					portrait.setImageResource(Utils.getDefaultPortraitId(ChatUser.USER_PATIENT, mUser.getPatient()));
				}
			}
		}
	}
	
	public void loadMore(int mode){
//		if(!mHasMore){
//			Toast.makeText(UserProfileActivity.this, getString(R.string.no_more_data), Toast.LENGTH_SHORT).show();
//			return;
//		}
		if(!mIsRecommandTaskFree){
			return;
		}
		mPageCount += 1;
		loadTaskBegin(mode);
		try{
			mLoadArticalListTask = new LoadArticalListTask();
			mLoadArticalListTask.execute();
		}catch(RejectedExecutionException e){
			e.printStackTrace();
		}		
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
			SharedPreferences preferences = getSharedPreferences(
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
					SharedPreferences preferences = getSharedPreferences(
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
	}
	
	class LoadArticalListTask extends AsyncTask<Void, Void, List<Article>>{
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
					result = NetEngine.getInstance(UserProfileActivity.this)
											.getArticalListByUserID(Utils.getPassport(UserProfileActivity.this),
											mChatUser.getId(), Constants.MAX_PAGE_SIZE, mPageCount);
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
							mHasMore = true;
						}else{
							mHasMore = false;
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
					Utils.handleErrorEvent(mThr, UserProfileActivity.this);
				}else{
					Utils.showToast(UserProfileActivity.this, R.string.no_more_articals, Toast.LENGTH_SHORT);
				}
				loadTaskEnd(false);
				return;
			}
			loadTaskEnd(true);
			if(result.size() == 0){
				if(mHasMore){
					mHasMore = false;
					Utils.showToast(UserProfileActivity.this, R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					Utils.showToast(UserProfileActivity.this, R.string.no_more_articals, Toast.LENGTH_SHORT);
				}
				return;
			}
			if(mFetchDataType == Artical_RELOAD){
				if(mArticalList != null){
					mArticalList.clear();
				}
				mArticalList = result;
			}else{
				if(mArticalList == null){
					mArticalList = result;
				}else{
					mArticalList.addAll(result);
				}
			}
			if(mArticalListAdapter != null){
				mArticalListAdapter.notifyDataSetChanged();
			}

		}
	}
	
   class ArticalListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mArticalList != null && !mArticalList.isEmpty()){
				return mArticalList.size();
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
				view = new DiscoverListItemView(UserProfileActivity.this);
			}else{
				view = (DiscoverListItemView)convertView;
			}
		
			if(mArticalList != null && mArticalList.size() > 0 && position < mArticalList.size()){
				view.update(mArticalList.get(position), false, null);
				view.findViewById(R.id.rl_userinfo).setVisibility(View.GONE);
			}
		
			return view;
		}
	}
   
	class FollowUnfollowUserTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		private final int mAction;

		public FollowUnfollowUserTask(int action){
			mAction = action;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsFollowTaskFree = false;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = "";

				try {
					if(mAction == ACTION_FOLLOW){
						result = NetEngine.getInstance(UserProfileActivity.this).followUser(mChatUser.getId());
					}else{
						result = NetEngine.getInstance(UserProfileActivity.this).unFollowUser(mChatUser.getId());
					}
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
						
					}else{
						return "";
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				return result;
		}

		@Override
		protected void onCancelled() {
			mIsFollowTaskFree = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsFollowTaskFree = true;
			
			if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, UserProfileActivity.this);
				}
				return;
			}
			
			if(mAction == ACTION_FOLLOW){
				mActionBtn.setImageResource(R.drawable.icon_followed_m);
			}else{
				mActionBtn.setImageResource(R.drawable.icon_follow_m);
			}
			if(mIsTaskFree){
				try{
					GetUserInfoTask task = new GetUserInfoTask();
					task.execute();
				}catch(RejectedExecutionException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	private class GetUserInfoTask extends AsyncTask<Void, Void, String>{
        private Throwable mThr;
        
		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				if(ChatUser.USER_DOCTOR.equals(mChatUser.getType())){
					result = NetEngine.getInstance(UserProfileActivity.this)
									.getDoctorInfo(Utils.getPassport(UserProfileActivity.this), mChatUser.getId());
				}else if(ChatUser.USER_PATIENT.equals(mChatUser.getType())){
					result = NetEngine.getInstance(UserProfileActivity.this)
									.GetPatientInfo(Utils.getPassport(UserProfileActivity.this), mChatUser.getId());
				}
								
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
					if(ChatUser.USER_DOCTOR.equals(mChatUser.getType())){
						JSONObject doctor = json_data.getJSONObject("doctor");
						Doctor doc = new Doctor(UserProfileActivity.this, (JSONObject)doctor);
						mUser = new UserInfo(doc);
					}else if(ChatUser.USER_PATIENT.equals(mChatUser.getType())){
						JSONObject patient = json_data.getJSONObject("patient");
						Patient pat = new Patient(UserProfileActivity.this, patient);
						mUser = new UserInfo(pat);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
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
			mIsTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, UserProfileActivity.this);
				}
				return;
			}
			if (mUser != null && !NetEngine.getFeedbackId().equals(mChatUser.getId())){
				setFollowIcon(true);
			}
			updateHeaderView();
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			super.onPreExecute();
		}
		
	}
}
