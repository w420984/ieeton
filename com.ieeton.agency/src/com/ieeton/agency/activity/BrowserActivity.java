package com.ieeton.agency.activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.AccountDetailItem;
import com.ieeton.agency.models.Article;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.FileUtils;
import com.ieeton.agency.utils.SocialShareUtils;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.LoadingBar;
import com.ieeton.agency.R;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.sso.UMSsoHandler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class BrowserActivity extends Activity implements OnClickListener{
	public static String EXTRA_SHOW_BOTTOM = "extra_bottom";
	
	private LoadingBar mLoadingBar;
	private RelativeLayout mWebViewContainer;
	private LinearLayout mBottomBar;
	private WebView wvBrowser;
	private String mUrl;
	private Article mArtical;
	private boolean mIsInternal = true;
	private ImageView mBtnShare;
	private LinearLayout mBackBtn;
	private TextView mTVTitle;
	private String mTitle;
	private String mHtmlTitle;
	private boolean mIsTaskFree = true;
	private RelativeLayout mAwardLayout;
	private RelativeLayout mFavoriteLayout;
	private ImageView mIvFavorite;
	private RelativeLayout mLikeLayout;
	private ImageView mIvLiked;
	private CustomToast mProgressDialog;
	private boolean mIsShowBottomBar;

	private static final int ACTION_AWARD = 0;
	private static final int ACTION_FAVORITE = 1;
	private static final int ACTION_LIKE = 2;
	private static final int ACTION_UNFAVORITE = 3;
	private static final int ACTION_UNLIKE = 4;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);
		initViews();
	}

	public void initViews(){
		Intent intent = getIntent();
		if(intent == null){
			finish();
			return;
		}
		intent.getStringExtra(Constants.WEB_BROWSER_URL);
		String url = intent.getStringExtra(Constants.WEB_BROWSER_URL);
		mIsInternal = intent.getBooleanExtra(Constants.WEB_BROWSER_INTERNAL, true);
		mTitle = intent.getStringExtra(Constants.WEB_BROWSER_TITLE);
		mArtical = (Article)intent.getSerializableExtra("artical");
		mIsShowBottomBar = intent.getBooleanExtra(EXTRA_SHOW_BOTTOM, false);
		if(!TextUtils.isEmpty(url)){
			//内部文章
			if (mIsInternal && mArtical != null){
				mUrl = url + "?articleid=" + mArtical.getID();
				Utils.logd("url:"+mUrl);
			}else{//外部文章或者外部产品链接活动链接
				mUrl = url;
			}
		}else{
			finish();
			return;
		}
  
		mAwardLayout = (RelativeLayout)findViewById(R.id.rl_award);
		mAwardLayout.setOnClickListener(this);
		mFavoriteLayout = (RelativeLayout)findViewById(R.id.rl_favorite);
		mIvFavorite = (ImageView) findViewById(R.id.iv_favorite);
		mFavoriteLayout.setOnClickListener(this);
		mLikeLayout = (RelativeLayout)findViewById(R.id.rl_like);
		mIvLiked = (ImageView) findViewById(R.id.iv_liked);
		mLikeLayout.setOnClickListener(this);
		
		mBtnShare = (ImageView) findViewById(R.id.iv_share);
		mBtnShare.setOnClickListener(this);
		mBackBtn = (LinearLayout)findViewById(R.id.ll_back);
		mBackBtn.setOnClickListener(this);
		mTVTitle = (TextView)findViewById(R.id.title);
		mLoadingBar = (LoadingBar) findViewById(R.id.loading_bar);
		mWebViewContainer = (RelativeLayout) findViewById(R.id.webview_container);
		mBottomBar = (LinearLayout)findViewById(R.id.ly_toolbar);

		if(!TextUtils.isEmpty(mTitle)){
			mTVTitle.setText(mTitle);
		}else if(!TextUtils.isEmpty(mHtmlTitle)){
			mTVTitle.setText(mHtmlTitle);
		}else{
			mTVTitle.setText(R.string.artical_detail);
		}
		
		if(mIsShowBottomBar){
			findViewById(R.id.bottom_line).setVisibility(View.VISIBLE);
			mBottomBar.setVisibility(View.VISIBLE);
			mBtnShare.setVisibility(View.VISIBLE);
			mIvLiked.setImageResource(mArtical.getIsLiked() == 1 ? R.drawable.discover_liked
					: R.drawable.discover_like);
			mIvFavorite.setImageResource(mArtical.getIsFavorited() == 1 ? R.drawable.discover_favorited
					: R.drawable.discover_favorite);
		}else{
			findViewById(R.id.bottom_line).setVisibility(View.GONE);
			mBottomBar.setVisibility(View.GONE);
			mBtnShare.setVisibility(View.GONE);
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
	
	public void back(){
		if (wvBrowser.canGoBack()) {
		    wvBrowser.goBack();
        } else {
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
	
	private void initWebViewParams() {
		WebChromeClient chromeclient = new WebChromeClient(){

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				mLoadingBar.drawProgress(newProgress);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if(mTitle != null && "".equals(mTitle)){
					mTVTitle.setText(mTitle);
				}else if(mHtmlTitle != null && "".equals(mHtmlTitle)){
					mTVTitle.setText(mHtmlTitle);
				}
			}
			  
		};
		wvBrowser.setWebChromeClient(chromeclient);
		WebViewClient client = new WebViewClient(){

			@Override
			public void onPageFinished(WebView view, String url) {
				mLoadingBar.setVisibility(View.INVISIBLE);
				if (mArtical != null){
					try {
						new ViewArticleTask().execute();
					} catch (RejectedExecutionException e) {
						e.printStackTrace();
					}
				}
				super.onPageFinished(view, url);
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mLoadingBar.setVisibility(View.VISIBLE);
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
		if (id == R.id.iv_share){
			if (mArtical != null){
				String content = "好文分享  " + mArtical.getTitle();
				String path = Utils.getBigPicPath(mArtical.getSummaryPicUrl());
				if (TextUtils.isEmpty(path) || !FileUtils.isFileExist(path)){
					path = null;
				}
				SocialShareUtils.shareToWX(BrowserActivity.this, getString(R.string.share_title), content, path, R.drawable.share_icon, mUrl);
			}
		} else if(view == mBackBtn){
			back();
		} else if(view == mAwardLayout){
			Intent intent = new Intent(this, RewardActivity.class);
			intent.putExtra(Constants.EXTRA_UID, mArtical.getPublisherInfo().getId());
			startActivity(intent);
		} else if(view == mFavoriteLayout){
			if (mArtical.getIsFavorited() == 1){
				doAction(ACTION_UNFAVORITE);
			}else{
				doAction(ACTION_FAVORITE);
			}
		} else if(view == mLikeLayout){
			if (mArtical.getIsLiked() == 1){
				doAction(ACTION_UNLIKE);
			}else{
				doAction(ACTION_LIKE);
			}
		}
	}
	
	public void doAction(int action){
		if(Utils.getPassport(BrowserActivity.this).equals("")){
			Toast.makeText(BrowserActivity.this, getString(R.string.no_login_wranning), Toast.LENGTH_SHORT).show();
			return;
		}
		
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
						result = NetEngine.getInstance(BrowserActivity.this).
									likeArtical(Utils.getPassport(BrowserActivity.this), mArtical.getID());
					}else if(mAction == ACTION_FAVORITE){
						result = NetEngine.getInstance(BrowserActivity.this).
									favoriteArtical(Utils.getPassport(BrowserActivity.this), mArtical.getID());
					}else if (mAction == ACTION_UNLIKE){
						result = NetEngine.getInstance(BrowserActivity.this).
									unLikeArtical(Utils.getPassport(BrowserActivity.this), mArtical.getID());
					}else if (mAction == ACTION_UNFAVORITE){
						result = NetEngine.getInstance(BrowserActivity.this).
									unFavoriteArtical(Utils.getPassport(BrowserActivity.this), mArtical.getID());
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
			}else if(mAction == ACTION_LIKE){
				popstring = getString(R.string.artical_like_success);
				mIvLiked.setImageResource(R.drawable.discover_liked);
				action = Constants.ACTION_LIKE_ARTICLE;
				mArtical.setIsLiked(1);
			}else if(mAction == ACTION_UNFAVORITE){
				popstring = getString(R.string.article_unfavorite_success);
				mIvFavorite.setImageResource(R.drawable.discover_favorite);
				action = Constants.UNFAVORITE_ARTICLE_ACTION;
				mArtical.setIsFavorited(0);
			}else if (mAction == ACTION_UNLIKE){
				popstring = getString(R.string.article_unlike_success);
				mIvLiked.setImageResource(R.drawable.discover_like);
				action = Constants.ACTION_UNLIKE_ARTICLE;
				mArtical.setIsLiked(0);
			}
			if (!TextUtils.isEmpty(action)){
				Intent intent = new Intent(action);
				intent.putExtra(MyFavoriteActivity.EXTRA_ARTICLE, mArtical);
				intent.putExtra(Constants.EXTRA_ARTICLEID, mArtical.getID());
				sendBroadcast(intent);
			}
			Toast.makeText(BrowserActivity.this, popstring, Toast.LENGTH_SHORT).show();
		}
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
	
	class ViewArticleTask extends AsyncTask<Void, Void, String>{

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(BrowserActivity.this).viewArticle(mArtical.getID());
			} catch (PediatricsIOException e) {
				e.printStackTrace();
			} catch (PediatricsParseException e) {
				e.printStackTrace();
			} catch (PediatricsApiException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (TextUtils.isEmpty(result)){
				return;
			}
			Intent intent = new Intent(Constants.VIEW_ARTICLE_ACTION);
			intent.putExtra(Constants.EXTRA_ARTICLEID, mArtical.getID());
			sendBroadcast(intent);
		}
		
	}
}
