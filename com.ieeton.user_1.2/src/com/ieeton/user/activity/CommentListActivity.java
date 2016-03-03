package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import com.ieeton.user.R;
import com.ieeton.user.adapter.CommentListAdapter;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Comment;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.PullDownView;
import com.ieeton.user.view.PullDownView.UpdateHandle;
import com.umeng.analytics.MobclickAgent;

public class CommentListActivity extends TemplateActivity implements UpdateHandle{
	private final int LOAD_REFRESH = 0;
	private final int LOAD_MORE = 1;

	private PullDownView mPullDownView;
	private ListView mListView;
	private CommentListAdapter mAdapter;
	private int mPageNum = 1;
	private int mLoadMode = LOAD_REFRESH;
	private List<Comment> mCommentList;
	private boolean mIsTaskFree = true;
	private FetchDataTask mTask;
	
	private String mProductId;
	private String mOwnerId;

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
		setView(R.layout.activity_comment_list);
		setTitleBar(getString(R.string.back), getString(R.string.comment), null);

		Intent intent = getIntent();
		mProductId = intent.getStringExtra(Constants.EXTRA_PRODUCTID);
		mOwnerId = intent.getStringExtra(Constants.EXTRA_UID);
		
		initView();
	}

	private void initView(){
		mPullDownView = (PullDownView) findViewById(R.id.pulldown_view);
		mPullDownView.setUpdateHandle(this);
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new CommentListAdapter(this, mCommentList);
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
		
		refreshData(LOAD_REFRESH);
	}
	
	@Override
	public void onPause() {
		MobclickAgent.onPageEnd("ProductsFragment"); 
		super.onPause();
	}

	@Override
	public void onResume() {
		MobclickAgent.onPageStart("ProductsFragment"); 
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		super.onDestroy();
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
	
	private void processResult(String result){
		if (TextUtils.isEmpty(result)){
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			JSONArray array = obj.optJSONArray("ProductComment");

			if (array == null || array.length() == 0){
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
					Utils.showToast(CommentListActivity.this, R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					mAdapter.refresh(mCommentList);
					//showEmpty(true);
				}
				return;
			}
			if (mCommentList == null){
				mCommentList = new ArrayList<Comment>();
			}
			List<Comment> list = new ArrayList<Comment>();
			for(int i=0; i<array.length(); i++){
				Comment item = new Comment(array.optJSONObject(i));
				list.add(item);
			}
			if (mLoadMode == LOAD_REFRESH){
				mCommentList.clear();
			}
			mCommentList.addAll(list);
			mAdapter.refresh(mCommentList);
			if (mCommentList.size()>0){
				//showProductList();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private class FetchDataTask extends AsyncTask<Integer, Void, String>{
	    private Throwable mThr;
	    private int page;

		@Override
		protected String doInBackground(Integer... params) {
			page = params[0];
			String result = "";
				try {
					result = NetEngine.getInstance(CommentListActivity.this).
								getProductComment(mProductId, mOwnerId, page);
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
			//dismissProgress();
			mIsTaskFree = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			//dismissProgress();
			mPullDownView.endUpdate(null);
			mIsTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, CommentListActivity.this);
				}else{
					Utils.showToast(CommentListActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
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
