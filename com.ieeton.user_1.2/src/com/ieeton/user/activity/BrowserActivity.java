package com.ieeton.user.activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Article;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.FileUtils;
import com.ieeton.user.utils.SocialShareUtils;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.LoadingBar;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.sso.UMSsoHandler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class BrowserActivity extends TemplateActivity implements OnClickListener{
	public static String EXTRA_SHOW_BOTTOM = "showBottom";
	
	private LoadingBar mLoadingBar;
	private RelativeLayout mWebViewContainer;
	private LinearLayout mBottomBar;
	private WebView wvBrowser;
	private String mUrl;
	private Article mArtical;
	private String mArticleId;
	private String mIsInternal;
	private String mTitle;
	private String mHtmlTitle;
	private boolean mIsTaskFree = true;
	private RelativeLayout mAwardLayout;
	private RelativeLayout mFavoriteLayout;
	private ImageView mIvFavorite;
	private RelativeLayout mShareLayout;
	private ImageView mIvShare;
	private String mIsShowBottomBar;
	private String mOrderId;

	private static final int ACTION_AWARD = 0;
	private static final int ACTION_FAVORITE = 1;
	private static final int ACTION_LIKE = 2;
	private static final int ACTION_UNFAVORITE = 3;
	private static final int ACTION_UNLIKE = 4;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.browser);
		initViews();
	}

	public void initViews(){
		Intent intent = getIntent();
		if(intent == null){
			finish();
			return;
		}
		String url = intent.getStringExtra(Constants.WEB_BROWSER_URL);
		mIsInternal = intent.getStringExtra(Constants.WEB_BROWSER_INTERNAL);
		mTitle = intent.getStringExtra(Constants.WEB_BROWSER_TITLE);
		mArtical = (Article)intent.getSerializableExtra("artical");
		mArticleId = intent.getStringExtra("articleId");
		mOrderId = intent.getStringExtra(Constants.EXTRA_ORDERID);
		if (mArtical != null){
			mArticleId = mArtical.getID();
		}
		mIsShowBottomBar = intent.getStringExtra(EXTRA_SHOW_BOTTOM);
		if(!TextUtils.isEmpty(url)){
			//内部文章
			if ("true".equals(mIsInternal)){
				mUrl = url + "?articleId=" + mArticleId + 
						"&uid="+Utils.getMyUid(this) + "&channel=app";
			}else{//外部文章或者外部产品链接活动链接
				mUrl = url;
			}
		}else{
			finish();
			return;
		}
		Utils.logd("url:"+mUrl);
		mAwardLayout = (RelativeLayout)findViewById(R.id.rl_award);
		mAwardLayout.setOnClickListener(this);
		mFavoriteLayout = (RelativeLayout)findViewById(R.id.rl_favorite);
		mIvFavorite = (ImageView) findViewById(R.id.iv_favorite);
		mFavoriteLayout.setOnClickListener(this);
		mShareLayout = (RelativeLayout)findViewById(R.id.rl_share);
		mIvShare = (ImageView) findViewById(R.id.iv_share);
		mShareLayout.setOnClickListener(this);
		
		mLoadingBar = (LoadingBar) findViewById(R.id.loading_bar);
		mWebViewContainer = (RelativeLayout) findViewById(R.id.webview_container);
		mBottomBar = (LinearLayout)findViewById(R.id.ly_toolbar);

		String title = "";
		if(!TextUtils.isEmpty(mTitle)){
			title = mTitle;
		}else if(!TextUtils.isEmpty(mHtmlTitle)){
			title = mHtmlTitle;
		}else{
			title = getString(R.string.artical_detail);
		}
		
		if("true".equals(mIsShowBottomBar)){
			setTitleBar(getString(R.string.back), title, null);
			if (mArtical != null){
				updateBottomBtn();
			}else{
				try {
					new GetArticleTask().execute();
				} catch (RejectedExecutionException e) {
					e.printStackTrace();
				}
			}
		}else{
			if (TextUtils.isEmpty(mOrderId)){
				setTitleBar(getString(R.string.back), title, null);
			}else{
				setTitleBar(getString(R.string.back), title, getString(R.string.ok));
			}
			findViewById(R.id.bottom_line).setVisibility(View.GONE);
			mBottomBar.setVisibility(View.GONE);
		}

		if(wvBrowser != null){
			mWebViewContainer.removeView(wvBrowser);
			wvBrowser.destroy();
			wvBrowser = null;
		}
		wvBrowser = new WebView(this);
		mWebViewContainer.addView(wvBrowser);
		  
		initWebViewParams();
		initWebViewSettings(wvBrowser);
	  
		wvBrowser.loadUrl(mUrl);
	}
	
	private void updateBottomBtn(){
		findViewById(R.id.bottom_line).setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.VISIBLE);
		mIvFavorite.setImageResource(mArtical.getIsFavorited() == 1 ? R.drawable.discover_favorited
				: R.drawable.discover_favorite);
	}
	
	public void back(){
		if (wvBrowser.canGoBack()) {
		    wvBrowser.goBack();
        } else {
        	backToMain(MainActivity.INPUT_DISCOVERY);
			finish();
		}
	}
	
	private void initWebViewSettings(WebView webview){
		webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webview.requestFocusFromTouch();
		webview.getSettings().setJavaScriptEnabled(true);
		invokeVoidMethod(webview.getSettings(), "setPluginsEnabled", true);
		webview.getSettings().setSupportZoom(true);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.getSettings().setAllowFileAccess(true);
		webview.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		webview.getSettings().setUseWideViewPort(true);
		invokeVoidMethod(webview.getSettings(), "setLoadWithOverviewMode", true);
		invokeVoidMethod(webview.getSettings(), "setDisplayZoomControls", false);
	}
	
	private WebChromeClient chromeclient;
	private void initWebViewParams() {
		chromeclient = new WebChromeClient(){

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				mLoadingBar.drawProgress(newProgress);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
//				if(mTitle != null && "".equals(mTitle)){
//					mTVTitle.setText(mTitle);
//				}else if(mHtmlTitle != null && "".equals(mHtmlTitle)){
//					mTVTitle.setText(mHtmlTitle);
//				}
			}
			  
		};
		wvBrowser.setWebChromeClient(chromeclient);
		WebViewClient client = new WebViewClient(){

			@Override
			public void onPageFinished(WebView view, String url) {
				mLoadingBar.setVisibility(View.INVISIBLE);
				super.onPageFinished(view, url);
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mLoadingBar.setVisibility(View.VISIBLE);
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Utils.logd("shouldOverrideUrlLoading url:"+url);
				return super.shouldOverrideUrlLoading(view, url);
			}
			  
		};
		wvBrowser.setWebViewClient(client);
	}
	
	private void invokeVoidMethod(Object owner, String methodName, boolean property) {
		try {
			Method method = owner.getClass().getMethod(methodName, boolean.class);
			method.invoke(owner, property);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}  
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (view == mFavoriteLayout && Utils.getMyType(this) == 5){
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			return;
		}
		
		if (id == R.id.rl_share){
			if (mArtical != null){
				String title = mArtical.getTitle();
				String content = mArtical.getSummary();
				String path = Utils.getBigPicPath(mArtical.getSummaryPicUrl());
				if (TextUtils.isEmpty(path) || !FileUtils.isFileExist(path)){
					path = null;
				}
				String url = "";
				if ("true".equals(mIsInternal)){
					url = getIntent().getStringExtra(Constants.WEB_BROWSER_URL) + "?articleId=" + mArticleId;
				}else{
					url = mUrl;
				}
				SocialShareUtils.shareToWX(BrowserActivity.this, title, null, content, path, url);
			}
		} else if(view == mAwardLayout){
			if (mArtical != null){
//				Intent intent = new Intent(this, RewardActivity.class);
//				intent.putExtra(Constants.EXTRA_UID, mArtical.getPublisherInfo().getId());
//				startActivity(intent);
			}
		} else if(view == mFavoriteLayout){
			if (mArtical != null){
				if (mArtical.getIsFavorited() == 1){
					doAction(ACTION_UNFAVORITE);
				}else{
					doAction(ACTION_FAVORITE);
				}
			}
		}
		super.onClick(view);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void doAction(int action){
		
		Log.v("sereinli","doAction");
		if(!mIsTaskFree){
			return;
		}
		Log.v("sereinli","doAction1");
		try{
			DoActionTask task = new DoActionTask(action);
			task.execute();
		}catch(RejectedExecutionException e){
			e.printStackTrace();
		}
	}
	
	class DoActionTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		private final int mAction;

		public DoActionTask(int action){
			mAction = action;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgress();
			mIsTaskFree = false;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = "";

//			04-18 23:39:02.119: V/sereinli(1481): like,fav,result:{"error":false,"verify":false,"redirect":false,"ok":true,"code":200,"messages":{"serverTime":"2015-04-19T11:39:01+0800","data":{"inserted":1,"userId":"passport_122","articleId":"1"}}}

				try {
					if(mAction == ACTION_LIKE){
//						result = NetEngine.getInstance(BrowserActivity.this).
//									likeArtical(Utils.getPassport(BrowserActivity.this), mArticleId);
					}else if(mAction == ACTION_FAVORITE){
						result = NetEngine.getInstance(BrowserActivity.this).
									favoriteArtical(mArticleId);
					}else if (mAction == ACTION_UNLIKE){
//						result = NetEngine.getInstance(BrowserActivity.this).
//									unLikeArtical(Utils.getPassport(BrowserActivity.this), mArticleId);
					}else if (mAction == ACTION_UNFAVORITE){
						result = NetEngine.getInstance(BrowserActivity.this).
									unFavoriteArtical(mArticleId);
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
				Log.v("sereinli","like,fav,result:"+result);

				JSONObject object = null;
				try {
					object = new JSONObject(result);
					if(!object.getBoolean("error")){
						return result;
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
			super.onCancelled();
			dismissProgress();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			mIsTaskFree = true;
			
			if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, BrowserActivity.this);
				}else{
					Utils.showToast(BrowserActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			String popstring = "";
			String action = "";
			if(mAction == ACTION_FAVORITE){
				popstring = getString(R.string.artical_favorite_success);
				mIvFavorite.setImageResource(R.drawable.discover_favorited);
				action = Constants.FAVORITE_ARTICLE_ACTION;
				mArtical.setIsFavorited(1);
			}else if(mAction == ACTION_UNFAVORITE){
				popstring = getString(R.string.article_unfavorite_success);
				mIvFavorite.setImageResource(R.drawable.discover_favorite);
				action = Constants.UNFAVORITE_ARTICLE_ACTION;
				mArtical.setIsFavorited(0);
			}
			if (!TextUtils.isEmpty(action)){
				Intent intent = new Intent(action);
				intent.putExtra(Constants.EXTRA_ARTICLE, mArtical);
				intent.putExtra(Constants.EXTRA_ARTICLEID, mArticleId);
				sendBroadcast(intent);
			}
			Toast.makeText(BrowserActivity.this, popstring, Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onPause() {
//		if (mCustomView != null) {
//        	invokeVoidMethod(chromeclient, "onHideCustomView");
//        }
		
		if (wvBrowser != null) {
			wvBrowser.pauseTimers();
			invokeVoidMethod(wvBrowser, "onPause");
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (wvBrowser != null) {
			invokeVoidMethod(wvBrowser, "onResume");
			wvBrowser.resumeTimers();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (wvBrowser != null){
			mWebViewContainer.removeView(wvBrowser);
			wvBrowser.setDownloadListener(null);
			wvBrowser.destroy();
			wvBrowser = null;
		}
		super.onDestroy();
	}
	
	private void invokeVoidMethod(Object owner, String methodName) {
		try {
			Method method = owner.getClass().getMethod(methodName, new Class[0]);
			method.invoke(owner, new Object[0]);
		} catch (SecurityException e) {
			Utils.loge(e);
		} catch (NoSuchMethodException e) {
			Utils.loge(e);
		} catch (IllegalArgumentException e) {
			Utils.loge(e);
		} catch (IllegalAccessException e) {
			Utils.loge(e);
		} catch (InvocationTargetException e) {
			Utils.loge(e);
		}  
	}
	
	private class SubmitSubcribeTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(BrowserActivity.this).updateSubcribe(mOrderId);
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
		protected void onPostExecute(String result) {
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, BrowserActivity.this);
				}else{
					Utils.showToast(BrowserActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			Intent intent = new Intent(BrowserActivity.this, SuccessActivity.class);
			intent.putExtra(Constants.EXTRA_MODE, SuccessActivity.MODE_SUBSCRIBE_SUBMIT);
			intent.putExtra(Constants.EXTRA_ORDERID, mOrderId);
			startActivity(intent);
		}
		
	}
	
	class GetArticleTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(BrowserActivity.this).getArticle(mArticleId);
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
		protected void onPostExecute(String result) {
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getApplication());
				}else{
					//Utils.showToast(BrowserActivity.this, R.string.no_data, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject obj;
			try {
				obj = new JSONObject(result);
				mArtical = new Article(obj);
				updateBottomBtn();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/**使用SSO授权必须添加如下代码 */
		UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
	    UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode) ;
	    if(ssoHandler != null){
	       ssoHandler.authorizeCallBack(requestCode, resultCode, data);
	    }
	}

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			if (!TextUtils.isEmpty(mOrderId)){
				new SubmitSubcribeTask().execute();
			}
			break;
		case LEFT_BUTTON:
			back();
			break;
		}
	}
}
