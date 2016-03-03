package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.Article;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.DiscoverListItemView;
import com.ieeton.agency.R;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class SearchArticleActivity extends TemplateActivity {
	private ImageView mIvBack;
	private ImageView mIvSearch;
	private EditText mEtKeyword;
	private ListView mListView;
	private List<Article> mList;
	private ArticleListAdapter mAdapter;
	private CustomToast mProgressDialog;
	private boolean mIsTaskFree = true;
	private FechDataTask mTask;
	private String mKeyword;
	

	@Override
	protected void handleTitleBarEvent(int eventId) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.search_article);
		setTitleBar(null, null, null);
		
		mIvBack = (ImageView) findViewById(R.id.cancel_btn);
		mIvBack.setOnClickListener(this);
		mIvSearch = (ImageView) findViewById(R.id.search_btn);
		mIvSearch.setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.list);
		mAdapter = new ArticleListAdapter();
		mListView.setAdapter(mAdapter);
		mEtKeyword = (EditText) findViewById(R.id.search_input_box);
		mEtKeyword.setImeOptions( EditorInfo.IME_ACTION_SEARCH );
		mEtKeyword.setOnEditorActionListener( new OnEditorActionListener() {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event ) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_UNSPECIFIED:
                    case EditorInfo.IME_ACTION_SEARCH:
                    	hideSoftKeyborad();
                		mKeyword = mEtKeyword.getEditableText().toString(); 
                        search();
                        return true;
                    default:
                        return false;
                }
            }
        } );
	}
	
	private void search(){
		mEtKeyword.setText("");
		if(mIsTaskFree){
			try{
				mTask = new FechDataTask();
				mTask.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}
		}
	}
	
	private void hideSoftKeyborad(){
        InputMethodManager imm = (InputMethodManager) SearchArticleActivity.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive(mEtKeyword)) {
            imm.hideSoftInputFromWindow(mEtKeyword.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
	}

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
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

	@Override
	public void onClick(View v) {
		if (v == mIvBack){
			finish();
		}else if (v == mIvSearch){
			hideSoftKeyborad();
			mKeyword = mEtKeyword.getText().toString();
			search();
		}
		super.onClick(v);
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
//				if (mLoadMode == LOAD_MORE){
//					mPageNum--;
//					Utils.showToast(this, R.string.no_more_data, Toast.LENGTH_SHORT);
//				}else{
//					showEmpty(true);
//				}
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
//			if (mLoadMode == LOAD_REFRESH){
//				mList.clear();
//			}
			mList.addAll(list);
			mAdapter.notifyDataSetChanged();
//			if (mList.size()>0){
//				showEmpty(false);
//			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	class FechDataTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			//int page = params[0];
			try {
				result = NetEngine.getInstance(SearchArticleActivity.this)
							.searchArticle(mKeyword, 20, 1);
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
//			if (mLoadMode == LOAD_MORE){
//				mPageNum--;
//			}
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			dismissProgress();
//			mPullDownView.endUpdate(null);
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					Utils.showToast(SearchArticleActivity.this, R.string.no_data, Toast.LENGTH_SHORT);
				}
//				if (mLoadMode == LOAD_MORE){
//					mPageNum--;
//				}
				return;
			}
			processResult(result);
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			showProgress();
			super.onPreExecute();
		}
	}
	
	class ArticleListAdapter extends BaseAdapter{
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
				view = new DiscoverListItemView(SearchArticleActivity.this);
			}else{
				view = (DiscoverListItemView) convertView;
			}
			if (mList != null && !mList.isEmpty() && position < mList.size()){
				view.update(mList.get(position), isShowCheckBox, null);
			}
			return view;
		}
	}

}
