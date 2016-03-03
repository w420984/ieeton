package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.Article;
import com.ieeton.agency.models.ChatUser;
import com.ieeton.agency.models.Patient;
import com.ieeton.agency.models.Remark;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.AsyncBitmapLoader;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.utils.AsyncBitmapLoader.ImageCallBack;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.DiscoverListItemView;
import com.ieeton.agency.view.PullDownView;
import com.ieeton.agency.view.RemarkItemView;
import com.ieeton.agency.view.PullDownView.UpdateHandle;
import com.ieeton.agency.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PatientProfileActivity extends TemplateActivity implements UpdateHandle,
		OnScrollListener{
	class FetchInfoTask extends AsyncTask<Void, Void, Patient>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}

		@Override
		protected Patient doInBackground(Void... arg0) {
			String result = "";

				try {
					result = NetEngine.getInstance(PatientProfileActivity.this)
									.GetPatientInfo(Utils.getPassport(PatientProfileActivity.this), mPatientId);
									
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
						JSONObject doctor = json_data.getJSONObject("patient");
		
						
						Patient item = new Patient(PatientProfileActivity.this, (JSONObject)doctor);

						return item;
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
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Patient result) {			
			dismissProgress();
			if (result == null){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, PatientProfileActivity.this);
				}else{
					Utils.showToast(PatientProfileActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			mPatient = result;
			updateView();
			initData();
		}
	}
	
	private class GetRemarkTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			int page = params[0];
			try {
				result = NetEngine.getInstance(PatientProfileActivity.this).
								getRemark(Utils.getPassport(PatientProfileActivity.this), 
										mPatient.getID(), 20, page);
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
			if (mLoadMode == Constants.LOAD_MORE){
				mPageNum--;
			}
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			mPullDownViewRemark.endUpdate(null);
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(PatientProfileActivity.this, R.string.get_remark_failed, Toast.LENGTH_SHORT);
				}
				if (mLoadMode == Constants.LOAD_MORE){
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
	
	class GetArticleTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			int page = params[0];
			try {
				result = NetEngine.getInstance(PatientProfileActivity.this)
							.getUserArticle(Utils.getPassport(PatientProfileActivity.this), 
									mPatientId, 20, page);
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
			if (mLoadMode == Constants.LOAD_MORE){
				mPageNum--;
			}
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			mPullDownViewArticle.endUpdate(null);
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(PatientProfileActivity.this, R.string.no_data, Toast.LENGTH_SHORT);
				}
				if (mLoadMode == Constants.LOAD_MORE){
					mPageNum--;
				}
				return;
			}
			processResultArticle(result);
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			//showProgress();
			super.onPreExecute();
		}
		
	}
	
	private class DeleteRemarkTask extends AsyncTask<String, Void, String>{
		private Throwable mThr;
		String remarkId;
		@Override
		protected String doInBackground(String... params) {
			String result = "";
			remarkId = params[0];
			try {
				result = NetEngine.getInstance(PatientProfileActivity.this)
									.deleteRemark(Utils.getPassport(PatientProfileActivity.this), 
											remarkId);
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
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(PatientProfileActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			Utils.showToast(PatientProfileActivity.this, R.string.operation_succes, Toast.LENGTH_SHORT);
			deleteItem(remarkId);
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private class AddRemarkTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(PatientProfileActivity.this)
									.addRemark(Utils.getPassport(PatientProfileActivity.this), 
											mPatient.getID(), mContent);
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
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(PatientProfileActivity.this, R.string.add_remark_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject obj;
			try {
				obj = new JSONObject(result);
				JSONObject data = obj.optJSONObject("messages").optJSONObject("data");
				String time = obj.optJSONObject("messages").optString("serverTime");
				time = Utils.getTime(time);
				String id = data.optString("id");
				Remark remark = new Remark(id, time, mContent);
				if (mListRemark == null){
					mListRemark = new ArrayList<Remark>();
				}
				mListRemark.add(0, remark);
				mAdapterRemark.notifyDataSetChanged();
				showEmpty(false);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}
	
	private class OperationTask extends AsyncTask<Integer, Void, String>{
		private int oid;
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			oid = params[0];
			String result = "";

			if (oid == FOLLOW){
				try {
					result = NetEngine.getInstance(PatientProfileActivity.this).followUser(mPatientId);
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
			}else if (oid == UNFOLLOW){
				try {
					result = NetEngine.getInstance(PatientProfileActivity.this).unFollowUser(mPatientId);
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
			mIsTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, PatientProfileActivity.this);
				}else{
					Utils.showToast(PatientProfileActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject obj;
			try {
				obj = new JSONObject(result);
				JSONObject data = obj.optJSONObject("messages")
						.optJSONObject("data");
				String uid = data.optString("userId");
				Intent intent = null;
				if (oid == FOLLOW){
					mIvFollow.setImageResource(R.drawable.icon_followed_m);
					intent = new Intent(Constants.ACTION_FOLLOW);
					mPatient.setIsFollowedStatus(1);
					Utils.showToast(PatientProfileActivity.this, getResources().getString(R.string.attend_succes), Toast.LENGTH_SHORT);
				}else if (oid == UNFOLLOW){
					mIvFollow.setImageResource(R.drawable.icon_follow_m);
					intent = new Intent(Constants.ACTION_UNFOLLOW);
					mPatient.setIsFollowedStatus(0);
					Utils.showToast(PatientProfileActivity.this, getResources().getString(R.string.unattend_succes), Toast.LENGTH_SHORT);
				}
				intent.putExtra("uid", mPatientId);
				sendBroadcast(intent);
			}catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			mIsTaskFree = false;
			super.onPreExecute();
		}
		
	}
	
	private class RemarkListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mListRemark != null && !mListRemark.isEmpty()){
				return mListRemark.size();
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
			RemarkItemView view;
			if (convertView == null){
				view = new RemarkItemView(PatientProfileActivity.this);
			}else{
				view = (RemarkItemView) convertView;
			}
			if (mListRemark != null && mListRemark.size() > 0 && position < mListRemark.size()){
				view.update(mListRemark.get(position));
			}
			Button btn = (Button) view.findViewById(R.id.id_unfollow);
			btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mDeleteRemarkTask = new DeleteRemarkTask();
					mDeleteRemarkTask.execute(mListRemark.get(position).getId());
				}
			});
			return view;
		}
		
	}

	private class ArticleListAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			if (mListArticle != null && !mListArticle.isEmpty()){
				return mListArticle.size();
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
				view = new DiscoverListItemView(PatientProfileActivity.this, false);
			}else{
				view = (DiscoverListItemView) convertView;
			}
			if (mListArticle != null && !mListArticle.isEmpty() && position < mListArticle.size()){
				view.update(mListArticle.get(position), false, null);
			}
			return view;
		}		
	}
	
	public static final String EXTRA_USERINFO = "user_info";
	public static final String EXTRA_USERID = "user_id";
	private int REQUEST_EDIT_REMARK = 1;
	private int FOLLOW = 1;
	private int UNFOLLOW = 2;

	private ViewGroup mVgHeader;
	private ViewGroup mVgTab;
	private ViewGroup mBtnChat;
	private ViewGroup mTabArticle;
	private ViewGroup mTabRemark;
	private ImageView mIvHeader;
	private ViewGroup mIvBack;
	private ImageView mIvAdd;
	private ImageView mIvFollow;
	private TextView mTvName;
	private TextView mTvTitle;
	private TextView mTvRegionName;
	private TextView mTvDescription;
	private TextView mTvFansNum;
	private TextView mTvArticlesNum;
	private TextView mTvTabArticle;
	private TextView mTvTabRemark;
	private ImageView mIvTabLeft;
	private ImageView mIvTabRighe;
	private ViewGroup mVgEmpty;
	private SwipeListView mRemarkListView;
	private ListView mArticleListView;
	private PullDownView mPullDownViewRemark;
	private PullDownView mPullDownViewArticle;
	private ViewGroup mVgRemarks;
	
	private Patient mPatient;
	private String mPatientId;
	private List<Remark> mListRemark;
	private List<Article> mListArticle;
	private RemarkListAdapter mAdapterRemark;
	private ArticleListAdapter mAdapterArticle;
	private CustomToast mProgressDialog;
	private GetRemarkTask mTask;
	private GetArticleTask mGetArticleTask;
	private FetchInfoTask mFetchInfoTask;
	private DeleteRemarkTask mDeleteRemarkTask;
	private AddRemarkTask mAddRemarkTask;
	private OperationTask mOperationTask;
	private int mPageNum = 1;
	private String mContent;
	private boolean mIsTaskFree = true;
	private int mLoadMode = Constants.LOAD_REFRESH;
	
	private final int SHOW_REMARKS = 1;
	private final int SHOW_ARTICLES = 0;
	private int mShowMode;
	private BroadcastReceiver mReceiver;

	@Override
	protected void handleTitleBarEvent(int eventId) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.patient_profile);
		setTitleBar(null, null, null);
	
		initView();
		mPatientId = getIntent().getStringExtra(EXTRA_USERID);
		if (TextUtils.isEmpty(mPatientId)){
			finish();
			return;
		}
		mFetchInfoTask = new FetchInfoTask();
		mFetchInfoTask.execute();
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
		registerReceiver(mReceiver, filter);
	}
	
	private void setArticle(String id, String action){
		if (mListArticle != null && !mListArticle.isEmpty()){
			for (Article article: mListArticle){
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
					mAdapterArticle.notifyDataSetChanged();
					break;
				}
			}
		}
	}
	
	private void initView(){
		mVgHeader = (ViewGroup) findViewById(R.id.rl_header);
		mIvHeader = (ImageView) findViewById(R.id.iv_header);
		mTvName = (TextView) findViewById(R.id.tv_name);
		mTvTitle = (TextView) findViewById(R.id.tvTitle);
		mTvRegionName = (TextView) findViewById(R.id.tv_region);
		mVgTab = (ViewGroup) findViewById(R.id.ll_tab);       
		mBtnChat = (ViewGroup) findViewById(R.id.ll_chat);    
		mBtnChat.setOnClickListener(this);
		mTabArticle = (ViewGroup) findViewById(R.id.fl_artilce);  
		mTabArticle.setOnClickListener(this);
		mTabRemark = (ViewGroup) findViewById(R.id.fl_remarks);   
		mTabRemark.setOnClickListener(this);
		mIvBack = (ViewGroup) findViewById(R.id.ll_back);      
		mIvBack.setOnClickListener(this);
		mIvAdd = (ImageView) findViewById(R.id.iv_add);       
		mIvAdd.setOnClickListener(this);
		mIvFollow = (ImageView) findViewById(R.id.iv_follow);    
		mIvFollow.setOnClickListener(this);
		mTvDescription = (TextView) findViewById(R.id.tv_description);
		mTvFansNum = (TextView) findViewById(R.id.tv_left_string);    
		mTvArticlesNum = (TextView) findViewById(R.id.tv_middle_string);
		mTvTabArticle = (TextView) findViewById(R.id.tv_article); 
		mTvTabRemark = (TextView) findViewById(R.id.tv_remarks);  
		mIvTabLeft = (ImageView) findViewById(R.id.iv_line_left);   
		mIvTabRighe = (ImageView) findViewById(R.id.iv_line_right); 
		mVgRemarks = (ViewGroup) findViewById(R.id.ll_remark);
		
		
		mVgEmpty = (ViewGroup) findViewById(R.id.rl_empty);
		mRemarkListView = (SwipeListView) findViewById(R.id.remark_list);		
		mAdapterRemark = new RemarkListAdapter();
		mRemarkListView.setAdapter(mAdapterRemark);
		mRemarkListView.setOnScrollListener(this);		
		mPullDownViewRemark = (PullDownView) findViewById(R.id.pulldown_view_remark);
		mPullDownViewRemark.setUpdateHandle(this);
		
		mArticleListView = (ListView) findViewById(R.id.article_list);		
		mAdapterArticle = new ArticleListAdapter();
		mArticleListView.setAdapter(mAdapterArticle);
		mArticleListView.setOnScrollListener(this);
		
		mPullDownViewArticle = (PullDownView) findViewById(R.id.pulldown_view_article);
		mPullDownViewArticle.setUpdateHandle(this);
		
		if (!TextUtils.isEmpty(Utils.getNickCache(this, mPatientId))){
			mTvTitle.setText(Utils.getNickCache(this, mPatientId));
		}
	}

	private void updateView(){
		mVgHeader.setVisibility(View.VISIBLE);
		if (mPatient.getArticleCount() > 0){
			mVgTab.setVisibility(View.VISIBLE);
			showMode(SHOW_ARTICLES);
			mTvDescription.setVisibility(View.VISIBLE);
			mTvRegionName.setVisibility(View.GONE);
			mTvFansNum.setVisibility(View.VISIBLE);
			mTvArticlesNum.setVisibility(View.VISIBLE);
		}else{
			mVgTab.setVisibility(View.GONE);
			showMode(SHOW_REMARKS);
			mTvDescription.setVisibility(View.GONE);
			mTvRegionName.setVisibility(View.VISIBLE);
			mTvFansNum.setVisibility(View.GONE);
			mTvArticlesNum.setVisibility(View.GONE);
		}
		
		mTvName.setText(mPatient.getNick());
		mTvTitle.setText(mPatient.getNick());
		mTvRegionName.setText(mPatient.getRegionName());
		mTvDescription.setText(mPatient.getDescription());
		mTvFansNum.setText(getString(R.string.fans)+mPatient.getFollowCount());
		mTvArticlesNum.setText(getString(R.string.article)+mPatient.getArticleCount());
		
		if (mPatient.getIsFollowedStatus() == 1){
			mIvFollow.setImageResource(R.drawable.icon_followed_m);
		}else{
			mIvFollow.setImageResource(R.drawable.icon_follow_m);
		}
		
		Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(this, 
				mPatientId, NetEngine.getImageUrl(mPatient.getPortraitUrl()), 
				ChatUser.USER_PATIENT, new ImageCallBack() {
			@Override
			public void imageLoad(Bitmap bitmap, Object user) {
				if (bitmap !=null && !bitmap.isRecycled()){
					mIvHeader.setImageBitmap(bitmap);
				}else {
					mIvHeader.setImageResource(Utils.getDefaultPortraitId(ChatUser.USER_PATIENT, user));
				}
			}
		});
		if (b !=null && !b.isRecycled()){
			mIvHeader.setImageBitmap(b);
		}else {
			mIvHeader.setImageResource(Utils.getDefaultPortraitId(ChatUser.USER_PATIENT, null));
		}
		boolean show = false;
		if (mListRemark == null || mListRemark.isEmpty()){
			show = true;
		}
		showEmpty(show);
	}
	
	private void initData(){
		if (mShowMode == SHOW_REMARKS){
			if (mListRemark == null || mListRemark.isEmpty()){
				mLoadMode = Constants.LOAD_REFRESH;
				refreshData(mLoadMode);
			}
		}else{
			if (mListArticle == null || mListArticle.isEmpty()){
				mLoadMode = Constants.LOAD_REFRESH;
				refreshData(mLoadMode);
			}
		}
	}
	
	private void refreshData(int mode){
		if (!mIsTaskFree){
			return;
		}
		if (mode == Constants.LOAD_REFRESH){
			mPageNum = 1;
			if (mShowMode == SHOW_REMARKS){
				mPullDownViewRemark.update();
			}else{
				mPullDownViewArticle.update();
			}
		}else{
			mPageNum += 1;
			if (mShowMode == SHOW_REMARKS){
				mPullDownViewRemark.updateWithoutOffset();
			}else{
				mPullDownViewArticle.updateWithoutOffset();
			}
		}
		if (mShowMode == SHOW_REMARKS){
			mTask = new GetRemarkTask();
			try {
				mTask.execute(mPageNum);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}else{
			mGetArticleTask = new GetArticleTask();
			try {
				mGetArticleTask.execute(mPageNum);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void showEmpty(boolean show){
		if (show){
			mVgEmpty.setVisibility(View.VISIBLE);
			mRemarkListView.setVisibility(View.GONE);
		}else{
			mVgEmpty.setVisibility(View.GONE);
			mRemarkListView.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onDestroy() {
		if (mReceiver != null){
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		if (mFetchInfoTask != null && mFetchInfoTask.getStatus() == AsyncTask.Status.RUNNING){
			mFetchInfoTask.cancel(true);
		}
		if (mGetArticleTask != null && mGetArticleTask.getStatus() == AsyncTask.Status.RUNNING){
			mGetArticleTask.cancel(true);
		}
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		if (mDeleteRemarkTask != null && mDeleteRemarkTask.getStatus() == AsyncTask.Status.RUNNING){
			mDeleteRemarkTask.cancel(true);
		}
		if (mAddRemarkTask != null && mAddRemarkTask.getStatus() == AsyncTask.Status.RUNNING){
			mAddRemarkTask.cancel(true);
		}
		if (mOperationTask != null && mOperationTask.getStatus() == AsyncTask.Status.RUNNING){
			mOperationTask.cancel(true);
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (mPatient == null){
			return;
		}
		if (v == mIvBack){
			finish();
		}else if (v == mBtnChat){
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra(ChatActivity.EXTRA_USERID, mPatient.getID());
			intent.putExtra(ChatActivity.EXTRA_USERINFO, new ChatUser(mPatient));
			startActivity(intent);
		}else if (v == mIvAdd){
			Intent intent = new Intent(this, EditActivity.class);
			intent.putExtra(EditActivity.EXTRA_MODE, EditActivity.MODE_REMARK);
			startActivityForResult(intent, REQUEST_EDIT_REMARK);
		}else if (v == mIvFollow){
			int oid = 0;
			if (mPatient.getIsFollowedStatus() == 1){
				oid = UNFOLLOW;
			}else {
				oid = FOLLOW;
			}
			mOperationTask = new OperationTask();
			mOperationTask.execute(oid);
		}else if (v == mTabRemark){
			if (mShowMode != SHOW_REMARKS){
				showMode(SHOW_REMARKS);
				if (mListRemark == null || mListRemark.isEmpty()){
					refreshData(Constants.LOAD_REFRESH);
				}
			}
		}else if (v == mTabArticle){
			if (mShowMode != SHOW_ARTICLES){
				showMode(SHOW_ARTICLES);
				if (mListArticle == null || mListArticle.isEmpty()){
					refreshData(Constants.LOAD_REFRESH);
				}
			}
		}
		super.onClick(v);
	}

	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK){
			return;
		}
		if (requestCode == REQUEST_EDIT_REMARK){
			if (data == null){
				return;
			}
			mContent = data.getStringExtra(EditActivity.INPUT_CONTENT);
			addRemark();
		}
		super.onActivityResult(requestCode, resultCode, data);
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
	
	private void processResult(String result){
		if (TextUtils.isEmpty(result)){
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			JSONObject data = obj.optJSONObject("messages").optJSONObject("data");
			JSONArray array = data.optJSONArray("remarks");
			if (array == null || array.length() == 0){
				if (mLoadMode == Constants.LOAD_MORE){
					mPageNum--;
				}
				return;
			}
			List <Remark> list = new ArrayList<Remark>();
			if (mListRemark == null){
				mListRemark = new ArrayList<Remark>();
			}
			for(int i=0; i<array.length(); i++){
				Remark item = new Remark(array.getJSONObject(i));
				list.add(item);
			}
			if (mLoadMode == Constants.LOAD_REFRESH){
				mListRemark.clear();
			}
			mListRemark.addAll(list);
			mAdapterRemark.notifyDataSetChanged();
			if (mListRemark.size()>0){
				showEmpty(false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void addRemark(){
		mAddRemarkTask = new AddRemarkTask();
		mAddRemarkTask.execute();
	}
		
	private void deleteItem(String id){
		if (mListRemark == null || mListRemark.isEmpty() || TextUtils.isEmpty(id)){
			return;
		}
		for(Remark item : mListRemark){
			if (id.equals(item.getId())){
				mListRemark.remove(item);
				mAdapterRemark.notifyDataSetChanged();
				mRemarkListView.closeOpenedItems();
				break;
			}
		}
	}
	
	@Override
	public void onUpdate() {
		mLoadMode = Constants.LOAD_REFRESH;
		refreshData(mLoadMode);
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


		if ((firstVisibleItem + visibleItemCount == totalItemCount - 5 || firstVisibleItem
						+ visibleItemCount >= totalItemCount - 1)) 
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
			mLoadMode = Constants.LOAD_MORE;
			refreshData(mLoadMode);
		}
		
	}
	
	private void showMode(int mode){
		mShowMode = mode;
		if (mode == SHOW_REMARKS){
			mVgRemarks.setVisibility(View.VISIBLE);
			mPullDownViewArticle.setVisibility(View.GONE);
			mIvTabLeft.setVisibility(View.GONE);
			mIvTabRighe.setVisibility(View.VISIBLE);
			mTvTabRemark.setTextColor(getResources().getColor(R.color.ieeton_color_blue));
			mTvTabArticle.setTextColor(getResources().getColor(R.color.color_gray));
		}else{
			mVgRemarks.setVisibility(View.GONE);
			mPullDownViewArticle.setVisibility(View.VISIBLE);
			mIvTabLeft.setVisibility(View.VISIBLE);
			mIvTabRighe.setVisibility(View.GONE);
			mTvTabRemark.setTextColor(getResources().getColor(R.color.color_gray));
			mTvTabArticle.setTextColor(getResources().getColor(R.color.ieeton_color_blue));
		}
	}
	
	private void processResultArticle(String result){
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
				if (mLoadMode == Constants.LOAD_MORE){
					mPageNum--;
					Utils.showToast(this, R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					//showEmpty(true);
					//mTvEmpty.setVisibility(View.VISIBLE);
				}
				return;
			}
			if (mListArticle == null){
				mListArticle = new ArrayList<Article>();
			}
			List<Article> list = new ArrayList<Article>();
			for(int i=0; i<array.length(); i++){
				Article item = new Article(array.optJSONObject(i));
				list.add(item);
			}
			if (mLoadMode == Constants.LOAD_REFRESH){
				mListArticle.clear();
			}
			mListArticle.addAll(list);
			mAdapterArticle.notifyDataSetChanged();
			if (mListArticle.size()>0){
				//showEmpty(false);
				//mTvEmpty.setVisibility(View.GONE);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
