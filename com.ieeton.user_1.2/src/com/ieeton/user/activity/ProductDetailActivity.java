package com.ieeton.user.activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.adapter.CommentListAdapter;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Comment;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.models.Product;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.FileUtils;
import com.ieeton.user.utils.SocialShareUtils;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.HorizontalListItemView;
import com.ieeton.user.view.HorizontalListView;
import com.ieeton.user.view.ListViewForScrollView;
import com.ieeton.user.view.MyScrollView;
import com.ieeton.user.view.ViewPagerItemView;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.sso.UMSsoHandler;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class ProductDetailActivity extends TemplateActivity implements OnPageChangeListener{
	public static int REQUEST_BUY = 1000;
	private final int OPERATE_ATTEND = 1;
	private final int OPERATE_UNATTEND = 2;
	private final int OPERATE_COLLECT = 3;
	private final int OPERATE_UNCOLLECT = 4;
	
	private TextView mTvProductName;
	private TextView mTvOwnerName;
	private TextView mTvGoodrate;
	private TextView mTvPrice;
	private TextView mTvIntroduce;
	private ViewGroup mVgDate;
	private TextView mTvDate;
	private TextView mTvDateTitle;
	private ViewGroup mVgNotice;
	private TextView mTvNotice;
	private TextView mTvBuy;
	private VideoView mVideoView;
	private ImageView mIvPlay;
	private ViewGroup mVgOwner;
	private ViewGroup mVgVideo;
	private ImageView mIvVideo;
	
	private ImageView mIvCall;
	private TextView mTvCall;
	private ViewGroup mVgCall;
	private ImageView mIvMessage;
	private TextView mTvMessage;
	private ViewGroup mVgMessage;
	private ImageView mIvCollection;
	private TextView mTvCollection;
	private ViewGroup mVgCollection;
	private ImageView mIvAttend;
	private TextView mTvAttend;
	private ViewGroup mVgAttend;
	
	private ViewGroup mVgPager;
	private ViewPager mPager;
	private TextView mTvIndicator;
	private List<View> mViewList;
	private ViewPagerAdapter mPagerAdapter;
	
	private ViewGroup mVgComment;
	private ViewGroup mVgMore;
	private List<Comment> mCommentList;
	private CommentListAdapter mCommentAdapter;
	private ListViewForScrollView mCommentListView;
	
	private ViewGroup mVgDoctorList;
	private HorizontalListView mDoctorListView;
	private List<IeetonUser> mDoctorList;
	private HorizontalListAdapter mDoctorListAdapter;
	
	private Product mProduct;
	private String mProductId;
	private String mOwnerId;
	private int mCategoryId;
	private boolean mIsIntegral;
	private FechDataTask mTask;
	private GetCommentTask mCommentTask;
	private OperationTask mOperationTask;
	private boolean mIsTaskFree = true;
	private Dialog mCallDialog;
	
	
	private WebView mWebView;
	private RelativeLayout mWebViewContainer;
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			String title = mProduct.getName();
			String content = Html.fromHtml(mProduct.getIntroduce()).toString();
			String weibocontent = "#大大健康# "+mProduct.getName();
			String path = Utils.getBigPicPath(mProduct.getProductionUrl());
			if (TextUtils.isEmpty(path) || !FileUtils.isFileExist(path)){
				path = null;
			}
			String url = Constants.SERVER_HOST_SHARE_PRODUCT_URL+"?productid="
					+mProductId + "&owneruid="+mOwnerId;
			SocialShareUtils.shareToWX(this, title, weibocontent, content, path, url);
			break;
		case LEFT_BUTTON:
			if (mCustomView != null){
	        	invokeVoidMethod(mChromeClient, "onHideCustomView");
			}else{
	        	backToMain(MainActivity.INPUT_HOME);
				finish();
			}
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if (mCustomView != null){
	        	invokeVoidMethod(mChromeClient, "onHideCustomView");
			}else{
	        	backToMain(MainActivity.INPUT_HOME);
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_product_detail);
		
		Intent intent = getIntent();
		if (intent == null){
			return;
		}
		mProduct = (Product) intent.getExtras().getSerializable(Constants.EXTRA_PRODUCT);
		mProductId = intent.getStringExtra(Constants.EXTRA_PRODUCTID);
		mOwnerId = intent.getStringExtra(Constants.EXTRA_UID);
		mCategoryId = intent.getIntExtra(Constants.EXTRA_CATEGORYID, -1);
		mIsIntegral = intent.getBooleanExtra(Constants.EXTRA_IS_INTEGRAL, false);
		initView();
		String title = "";
		switch(mCategoryId){
		case 1:
			title = getString(R.string.category1_detail);
			break;
		case 2:
			title = getString(R.string.category2_detail);
			break;
		case 3:
			title = getString(R.string.category3_detail);
			break;
		default:
			title = getString(R.string.product_detail);
		}
		setTitleBar(getString(R.string.back), title, getString(R.string.share));
		if (mProduct != null){
			update();
		}
		try {
			mTask = new FechDataTask();
			mTask.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private void initView(){
		mTvProductName = (TextView) findViewById(R.id.tv_product_name);
		mTvOwnerName = (TextView) findViewById(R.id.tv_owner_name);
		mTvGoodrate = (TextView) findViewById(R.id.tv_goodrate);
		mTvPrice = (TextView) findViewById(R.id.tv_price);
		mTvBuy = (TextView) findViewById(R.id.tv_buy);
		mTvBuy.setOnClickListener(this);
		mTvIntroduce = (TextView) findViewById(R.id.tv_introduce);
		mTvNotice = (TextView) findViewById(R.id.tv_note);
		mVgNotice = (ViewGroup) findViewById(R.id.ll_notice);
		mTvDate = (TextView) findViewById(R.id.tv_date);
		mVgDate = (ViewGroup) findViewById(R.id.ll_date);
		mTvDateTitle = (TextView) findViewById(R.id.tv_date_title);
		
		mIvCall = (ImageView) findViewById(R.id.iv_call);
		mTvCall = (TextView) findViewById(R.id.tv_call);
		mVgCall = (ViewGroup) findViewById(R.id.rl_call);
		mVgCall.setOnClickListener(this);
		
		mIvMessage = (ImageView) findViewById(R.id.iv_message);
		mTvMessage = (TextView) findViewById(R.id.tv_message);
		mVgMessage = (ViewGroup) findViewById(R.id.rl_message);
		mVgMessage.setOnClickListener(this);

		mIvCollection = (ImageView) findViewById(R.id.iv_collect);
		mTvCollection = (TextView) findViewById(R.id.tv_collect);
		mVgCollection = (ViewGroup) findViewById(R.id.rl_collect);
		mVgCollection.setOnClickListener(this);

		mIvAttend = (ImageView) findViewById(R.id.iv_attend);
		mTvAttend = (TextView) findViewById(R.id.tv_attend);
		mVgAttend = (ViewGroup) findViewById(R.id.rl_attend);
		mVgAttend.setOnClickListener(this);
		
		mVideoView = (VideoView) findViewById(R.id.video);
		mIvPlay = (ImageView) findViewById(R.id.iv_play);
		mIvPlay.setOnClickListener(this);
		mVgOwner = (ViewGroup) findViewById(R.id.rl_owner);
		mVgOwner.setOnClickListener(this);
		mVgVideo = (ViewGroup) findViewById(R.id.fl_video);
		mIvVideo = (ImageView) findViewById(R.id.iv_video);
		
		mVgComment = (ViewGroup) findViewById(R.id.ll_comment);
		mCommentListView = (ListViewForScrollView) findViewById(R.id.comment_list);
		mVgMore = (ViewGroup) findViewById(R.id.rl_more);
		mVgMore.setOnClickListener(this);
		
		mVgDoctorList = (ViewGroup) findViewById(R.id.rl_doctor_list);
		mDoctorListView = (HorizontalListView) findViewById(R.id.doctor_list);
		mDoctorListAdapter = new HorizontalListAdapter();
		mDoctorListView.setAdapter(mDoctorListAdapter);
		mDoctorListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long arg3) {
				Intent intent = new Intent(ProductDetailActivity.this, DoctorProfileActivity.class);
				intent.putExtra(Constants.EXTRA_UID, mDoctorList.get(position).getUid());
				intent.putExtra(Constants.EXTRA_USER, mDoctorList.get(position));
				startActivity(intent);
			}
		});
		
		mCustomViewContainer = (FrameLayout) findViewById(R.id.fullScrrenView);
		mWebViewContainer = (RelativeLayout) findViewById(R.id.webview_container);
		if (mWebView != null){
			if(mWebView != null){
				mWebViewContainer.removeView(mWebView);
				mWebView.destroy();
				mWebView = null;
			}
		}
		mWebView = new WebView(this);
		mWebViewContainer.addView(mWebView);
		initWebViewParams();
		initWebViewSettings(mWebView);
		String url = "http://m.dadahealth.com:81/pages/title/product.html?";
		url += "productid="+mProductId+"&owneruid="+mOwnerId;
		//Log.d("wwt", "url:"+url);
		mWebView.loadUrl(url);
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
	
	private View mCustomView;
    private FrameLayout     mCustomViewContainer;
    private Object mCustomViewCallback;
    private WebChromeClient mChromeClient;
	private void initWebViewParams() {
		mChromeClient = new WebChromeClient(){

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
			}
			
			public void onShowCustomView(View view, CustomViewCallback callback) {
		        // if a view already exists then immediately terminate the new one
		        if (mCustomView != null) {
		        	invokeVoidMethod(callback, "onCustomViewHidden");
		            return;
		        }
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		        
		        // Add the custom view to its container.
		        FrameLayout.LayoutParams customViewParams = new FrameLayout.LayoutParams(
			        		ViewGroup.LayoutParams.FILL_PARENT,
			        		ViewGroup.LayoutParams.FILL_PARENT,
			        		Gravity.CENTER);
		        mCustomViewContainer.addView(view, customViewParams);
		        mCustomView = view;
		        mCustomViewCallback = callback;
		        // Hide the content view.
		        findViewById(R.id.scrollview).setVisibility(View.GONE);
		        //titleBar.setVisibility(View.GONE);
		        // Finally show the custom view container.
		        mCustomViewContainer.setVisibility(View.VISIBLE);
		        mCustomViewContainer.bringToFront();
		    }
		
			public void onHideCustomView() {
		        if (mCustomView == null)
		            return;
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
				// Hide the custom view.
		        mCustomView.setVisibility(View.GONE);
		        // Remove the custom view from its container.
		        mCustomViewContainer.removeView(mCustomView);
		        mCustomView = null;
		        mCustomViewContainer.setVisibility(View.GONE);
	        	invokeVoidMethod(mCustomViewCallback, "onCustomViewHidden");
		        // Show the content view.
	        	findViewById(R.id.scrollview).setVisibility(View.VISIBLE);
		        //titleBar.setVisibility(View.VISIBLE);
		    }
			  
		};
		mWebView.setWebChromeClient(mChromeClient);
		WebViewClient client = new WebViewClient(){

			@Override
			public void onPageFinished(WebView view, String url) {
				dismissProgress();
				super.onPageFinished(view, url);
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				showProgress();
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
			}
			  
		};
		mWebView.setWebViewClient(client);
		
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

	private void update(){
		if (mProduct == null){
			return;
		}
		IeetonUser owner = mProduct.getOwner();
		
		mTvProductName.setText(mProduct.getName());
		if (owner != null){
			mTvOwnerName.setText(owner.getName());
			if (owner.getSwitchMobile() == 1){
				mIvCall.setImageResource(R.drawable.product_phone_s);
				mTvCall.setTextColor(getResources().getColor(R.color.color_black));
			}else{
				mIvCall.setImageResource(R.drawable.product_phone_n);
				mTvCall.setTextColor(getResources().getColor(R.color.color_gray));
			}
			
			if (owner.getSwichMessage() == 1){
				mIvMessage.setImageResource(R.drawable.product_message_s);
				mTvMessage.setTextColor(getResources().getColor(R.color.color_black));
			}else{
				mIvMessage.setImageResource(R.drawable.product_message_n);
				mTvMessage.setTextColor(getResources().getColor(R.color.color_gray));
			}			
			showAttendGroup();
		}
		
		showCollectionGroup();		
		mTvGoodrate.setText(getString(R.string.goodrate)+mProduct.getGoodrate()+"%");
		
		if (TextUtils.isEmpty(mProduct.getDate())){
			mVgDate.setVisibility(View.GONE);
		}else{
			//mVgDate.setVisibility(View.VISIBLE);
			mTvDate.setText(mProduct.getDate());
		}
		int categoryid = mCategoryId > 0 ? mCategoryId : mProduct.getCategoryId();
		if (categoryid == 2 && mProduct.getStatus() == 1){
			mTvDateTitle.setText(getString(R.string.lesson_date));
			mTvBuy.setText(getString(R.string.join_now));
		}else if (categoryid == 3 && mProduct.getStatus() == 1){
			mTvDateTitle.setText(getString(R.string.activity_date));
			mTvBuy.setText(getString(R.string.join_now));
		}else {
			if (mIsIntegral){
				mTvDateTitle.setText(getString(R.string.product_date));
				mTvBuy.setText(getString(R.string.exchange_now));
			}else{
				mTvDateTitle.setText(getString(R.string.product_date));
				mTvBuy.setText(getString(R.string.buy_now));
			}
			if (mProduct.getStatus() == 2){
				mTvBuy.setText(getString(R.string.subscribe_full));
			}else if (mProduct.getStatus() == 3){
				mTvBuy.setText(getString(R.string.is_over));
			}else if (mProduct.getStatus() == 4){
				mTvBuy.setText(getString(R.string.call_mishu));
			}else if (mProduct.getStatus() == 5){
				mTvBuy.setText(getString(R.string.call_institution));
			}else if (mProduct.getStatus() == 0){
				mTvBuy.setText(getString(R.string.is_delete));
			}
		}
		
		if (TextUtils.isEmpty(mProduct.getBuynote())){
			mVgNotice.setVisibility(View.GONE);
		}else{
			//mVgNotice.setVisibility(View.VISIBLE);
		}
		mTvNotice.setText(Html.fromHtml(mProduct.getBuynote()));
		if (mProduct.getIntegral()>0){
			mTvPrice.setText(mProduct.getIntegral()+getString(R.string.integral));			
		}else{
			String price = mProduct.getPrice() > 0 ? "¥" + mProduct.getPrice() : getString(R.string.price_free);
			mTvPrice.setText(price);
		}
		mTvIntroduce.setText(Html.fromHtml(mProduct.getIntroduce()));
		
		showDoctorList();
		showVideo();
	}
	
	private void showAttendGroup(){
		if (mProduct == null || mProduct.getOwner() == null){
			return;
		}
		IeetonUser owner = mProduct.getOwner();
		if (owner.getIsfollow() == 1){
			mIvAttend.setImageResource(R.drawable.product_attention_ed);
			mTvAttend.setText(getString(R.string.unattend));
		}else{
			mIvAttend.setImageResource(R.drawable.product_attention);
			mTvAttend.setText(getString(R.string.attend_user));
		}
	}

	private void showCollectionGroup(){
		if (mProduct == null){
			return;
		}
		if (mProduct.isCollection()){
			mIvCollection.setImageResource(R.drawable.product_collected);
			mTvCollection.setText(getString(R.string.unCollect));
		}else{
			mIvCollection.setImageResource(R.drawable.product_collect);
			mTvCollection.setText(getString(R.string.collect_product));
		}
		
	}
	
	MediaController controller;
	private void showVideo(){
		if (TextUtils.isEmpty(mProduct.getVideoUrl())){
			mVgVideo.setVisibility(View.GONE);
		}else{
			mVgVideo.setVisibility(View.VISIBLE);
//			controller = new MediaController(this);
//			controller.setMediaPlayer(mVideoView);			
//			controller.setAnchorView(mVideoView);
//			mVideoView.setMediaController(controller);
//			mVideoView.setVideoPath(mProduct.getVideoUrl());
//			mVideoView.seekTo(5000);
			Bitmap bitmap = null;
			// 获取视频的缩略图
			bitmap = Utils.createVideoThumbnail(mProduct.getVideoUrl(), getResources().getDimensionPixelSize(R.dimen.video_height), 
					getResources().getDimensionPixelSize(R.dimen.video_height));
			//System.out.println("bitmap"+bitmap);
			if (bitmap != null){
				mIvVideo.setImageBitmap(bitmap);
			}
		}
	}
	
	private void showDoctorList(){
		
		mDoctorList = mProduct.getDoctorList();
		mDoctorListAdapter.notifyDataSetChanged();
		if (mDoctorList != null && !mDoctorList.isEmpty()){
			mVgDoctorList.setVisibility(View.VISIBLE);
		}else{
			mVgDoctorList.setVisibility(View.GONE);
		}
		((MyScrollView)findViewById(R.id.scrollview)).smoothScrollTo(0, 0);
	}	
	
	private void playVideo(){
		if (mProduct == null){
			return;
		}
		mVideoView.seekTo(0);
		mVideoView.start();
		mIvPlay.setVisibility(View.GONE);
	}
	
	@Override
	protected void onPause() {
		if (mCustomView != null) {
        	invokeVoidMethod(mChromeClient, "onHideCustomView");
        }
		
		if (mWebView != null) {
			mWebView.pauseTimers();
			invokeVoidMethod(mWebView, "onPause");
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (mWebView != null) {
			invokeVoidMethod(mWebView, "onResume");
			mWebView.resumeTimers();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		dismissProgress();
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		if (mCommentTask != null && mCommentTask.getStatus() == AsyncTask.Status.RUNNING){
			mCommentTask.cancel(true);
		}
		if (mOperationTask != null && mOperationTask.getStatus() == AsyncTask.Status.RUNNING){
			mOperationTask.cancel(true);
		}
		if (mCallDialog != null && mCallDialog.isShowing()){
			mCallDialog.dismiss();
		}
		if (mWebView != null){
			mWebViewContainer.removeView(mWebView);
			mWebView.setDownloadListener(null);
			mWebView.destroy();
			mWebView = null;
		}

		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View v) {
		if (v == mVgCall || v == mVgMessage || v == mVgAttend
				|| v == mVgCollection || v == mTvBuy){
			//游客先登录
			if (Utils.getMyType(this) == 5){
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				return;
			}
			if (mProduct == null || mProduct.getOwner() == null){
				return;
			}			
		}
		if (v == mTvBuy){
			new UploadUserActionTask().execute();
			if (mProduct.getStatus() == 2 || mProduct.getStatus() == 3
					|| mProduct.getStatus() == 0){
				return;
			}else if (mProduct.getStatus() == 4){
				Utils.showCallDialog(this, NetEngine.getIvrNumber());
				return;
			}else if (mProduct.getStatus() == 5){
				String mobile = TextUtils.isEmpty(mProduct.getOwner().getIvrMobile()) ? 
						mProduct.getOwner().getMobile() : 
						mProduct.getOwner().getIvrMobile();
				mCallDialog = Utils.showCallDialog(this, mobile);
				return;
			}
			if (!TextUtils.isEmpty(mProduct.getExternalUrl())){
				Intent intent = new Intent(this, BrowserActivity.class);
				intent.putExtra(Constants.WEB_BROWSER_URL, mProduct.getExternalUrl());
				intent.putExtra(Constants.WEB_BROWSER_INTERNAL, "false");
				intent.putExtra(Constants.WEB_BROWSER_TITLE, getString(R.string.product_detail));
				intent.putExtra(BrowserActivity.EXTRA_SHOW_BOTTOM, "false");
				startActivity(intent);
			}else{
				Intent intent = new Intent(this, GenerateOrderActivity.class);
				intent.putExtra(Constants.EXTRA_UID, mOwnerId);
				intent.putExtra(Constants.EXTRA_PRODUCT, mProduct);
				startActivityForResult(intent, REQUEST_BUY);
			}
		}else if (v == mIvPlay){
			if (mProduct == null){
				return;
			}
			Intent intent = new Intent(this, VideoPlayerActivity.class);
			intent.putExtra(Constants.EXTRA_URL, mProduct.getVideoUrl());
			startActivity(intent);
//			playVideo();
		}else if (v == mVgOwner){
			Intent intent = new Intent(this, InstitutionActivity.class);
			intent.putExtra(Constants.EXTRA_UID, mOwnerId);
			if (mProduct != null && mProduct.getOwner() != null){
				intent.putExtra(Constants.EXTRA_USER, mProduct.getOwner());
			}
			startActivity(intent);
		}else if (v == mVgMore){
			Intent intent = new Intent(ProductDetailActivity.this, CommentListActivity.class);
			intent.putExtra(Constants.EXTRA_PRODUCTID, mProductId);
			intent.putExtra(Constants.EXTRA_UID, mOwnerId);
			startActivity(intent);
		}else if (v == mVgCall){
			if (mProduct.getOwner().getSwitchMobile() == 0
					|| TextUtils.isEmpty(mProduct.getOwner().getIvrMobile())
						&& TextUtils.isEmpty(mProduct.getOwner().getMobile())){
				Utils.showToast(this, R.string.call_service_is_unavlable, Toast.LENGTH_SHORT);
				return;
			}
			String mobile = TextUtils.isEmpty(mProduct.getOwner().getIvrMobile()) ? 
						mProduct.getOwner().getMobile() : 
						mProduct.getOwner().getIvrMobile();
			mCallDialog = Utils.showCallDialog(this, mobile);
		}else if (v == mVgMessage){
			if (mProduct.getOwner().getSwichMessage() == 0){
				Utils.showToast(this, R.string.chat_service_is_unavlable, Toast.LENGTH_SHORT);
				return;
			}
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra(Constants.EXTRA_UID, mOwnerId);
			intent.putExtra(Constants.EXTRA_USER, mProduct.getOwner());
			startActivity(intent);
		}else if (v == mVgAttend){
			if (!mIsTaskFree){
				return;
			}
			int action = mProduct.getOwner().getIsfollow() == 1 ? OPERATE_UNATTEND : OPERATE_ATTEND;
			mOperationTask = new OperationTask();
			try {
				mOperationTask.execute(action);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}else if (v == mVgCollection){
			if (!mIsTaskFree){
				return;
			}
			int action = mProduct.isCollection() ? OPERATE_UNCOLLECT : OPERATE_COLLECT;
			mOperationTask = new OperationTask();
			try {
				mOperationTask.execute(action);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}
		super.onClick(v);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_BUY){
			if (resultCode == RESULT_OK){
				if (data != null){
					data.setClass(ProductDetailActivity.this, SuccessActivity.class);
					startActivity(data);
					finish();
				}
			}
		}else{
			/**使用SSO授权必须添加如下代码 */
			UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.share");
		    UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(requestCode) ;
		    if(ssoHandler != null){
		       ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		    }
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	class FechDataTask extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;
	    
		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(ProductDetailActivity.this).
							getProductDetail(mProductId, mOwnerId);
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
					//Utils.showToast(ProductDetailActivity.this, R.string.get_account_details_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject obj;
			try {
				obj = new JSONObject(result);
				mProduct = new Product(obj);
				update();
				showPager();
				mCommentTask = new GetCommentTask();
				mCommentTask.execute();
			}catch (JSONException e){
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}
		
	}

	class GetCommentTask extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;
	    
		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(ProductDetailActivity.this).
							getProductComment(mProductId, mOwnerId, 1);
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
					//Utils.showToast(ProductDetailActivity.this, R.string.get_account_details_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject obj;
			try {
				obj = new JSONObject(result);
				JSONArray array = obj.optJSONArray("ProductComment");
				if (array != null && array.length()>0){
					if (mCommentList == null){
						mCommentList = new ArrayList<Comment>();
					}
					for(int i=0; i<array.length(); i++){
						Comment item = new Comment(array.optJSONObject(i));
						mCommentList.add(item);
					}
					if (mCommentList.size()>0){
						List<Comment> list;
						if (mCommentList.size()>2){
							list = mCommentList.subList(0, 2);
							mVgMore.setVisibility(View.VISIBLE);
						}else{
							list = mCommentList;
							mVgMore.setVisibility(View.GONE);
						}
						mCommentAdapter = new CommentListAdapter(ProductDetailActivity.this, list);
						mCommentListView.setAdapter(mCommentAdapter);
						mVgComment.setVisibility(View.VISIBLE);
					}
				}
			}catch (JSONException e){
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
	    private Throwable mThr;
	    private int action;

		@Override
		protected String doInBackground(Integer... params) {
			action = params[0];
			String result = "";
			try {
				if (action == OPERATE_ATTEND){
					result = NetEngine.getInstance(ProductDetailActivity.this).
								followUser(mOwnerId);
				}else if (action == OPERATE_UNATTEND){
					result = NetEngine.getInstance(ProductDetailActivity.this).
							unFollowUser(mOwnerId);
				}else if (action == OPERATE_COLLECT){
					result = NetEngine.getInstance(ProductDetailActivity.this).
							favorateProduct(mProductId, mOwnerId);
				}else if (action == OPERATE_UNCOLLECT){
					result = NetEngine.getInstance(ProductDetailActivity.this).
							unFavorateProduct(mProductId, mOwnerId);
				}
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
					Utils.handleErrorEvent(mThr, ProductDetailActivity.this);
				}else{
					Utils.showToast(ProductDetailActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			if (action == OPERATE_ATTEND){
				Utils.showToast(ProductDetailActivity.this, R.string.attend_succes, Toast.LENGTH_SHORT);
				mProduct.getOwner().setIsfollow(1);
				mProduct.getOwner().addFollowCount();
				Intent intent = new Intent(Constants.ACTION_FOLLOW);
				sendBroadcast(intent);
				showAttendGroup();
			}else if (action == OPERATE_UNATTEND){
				Utils.showToast(ProductDetailActivity.this, R.string.unattend_succes, Toast.LENGTH_SHORT);
				mProduct.getOwner().setIsfollow(0);
				mProduct.getOwner().subtractFollowCount();
				showAttendGroup();
				Intent intent = new Intent(Constants.ACTION_UNFOLLOW);
				sendBroadcast(intent);
			}else if (action == OPERATE_COLLECT){
				Utils.showToast(ProductDetailActivity.this, R.string.artical_favorite_success, Toast.LENGTH_SHORT);
				mProduct.setIsCollection(true);
				showCollectionGroup();
				Intent intent = new Intent(Constants.ACTION_FAVORITE_PRODUCT);
				sendBroadcast(intent);
			}else if (action == OPERATE_UNCOLLECT){
				Utils.showToast(ProductDetailActivity.this, R.string.article_unfavorite_success, Toast.LENGTH_SHORT);
				mProduct.setIsCollection(false);
				showCollectionGroup();
				Intent intent = new Intent(Constants.ACTION_UNFAVORITE_PRODUCT);
				sendBroadcast(intent);
			}
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			showProgress();
			super.onPreExecute();
		}
		
	}	
	
	private void showPager(){
		mVgPager = (ViewGroup) findViewById(R.id.fl_pager);
		mPager = (ViewPager) findViewById(R.id.pic_viewpager);
		mTvIndicator = (TextView) findViewById(R.id.tv_index);
		
		if (mProduct == null){
			mVgPager.setVisibility(View.GONE);
			return;
		}
		if (mProduct.getPics() == null || mProduct.getPics().isEmpty()){
			mVgPager.setVisibility(View.GONE);
			return;
		}
		mVgPager.setVisibility(View.VISIBLE);
    	mViewList = new ArrayList<View>();
    	for (int i=0; i<mProduct.getPics().size(); i++){
    		View item = new ViewPagerItemView(this);
    		mViewList.add(item);
    	}
    	initIndicator();
    	mPagerAdapter = new ViewPagerAdapter();
    	mPager.setAdapter(mPagerAdapter);
    	mPager.setOnPageChangeListener(this);
	}
	
	private void initIndicator(){
    	if (!mViewList.isEmpty()){
    		if (mViewList.size()<2){
        		mTvIndicator.setVisibility(View.GONE);
    		}else{
    			mTvIndicator.setVisibility(View.VISIBLE);
    			mTvIndicator.setText("1/"+mViewList.size());
    		}
    	}else{
    		mTvIndicator.setVisibility(View.GONE);
    	}
	}
	
	class ViewPagerAdapter extends PagerAdapter{
		@Override
		public int getCount() {
			if (mViewList !=null && !mViewList.isEmpty()){
				return mViewList.size();
			}
			return 0;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0==arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(final ViewGroup container, final int position) {
			ViewPagerItemView view = (ViewPagerItemView) mViewList.get(position);
			AsyncBitmapLoader.getInstance().loadBitmap(ProductDetailActivity.this, view.getPic(), 
					NetEngine.getImageUrl(mProduct.getPics().get(position)));
			container.removeView(mViewList.get(position));
			container.addView(mViewList.get(position)); 
            return mViewList.get(position);            
		}
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		
	}

	@Override
	public void onPageSelected(int position) {
		if (mViewList.size()<2){
    		mTvIndicator.setVisibility(View.GONE);
		}else{
			mTvIndicator.setVisibility(View.VISIBLE);
			mTvIndicator.setText((position+1)+"/"+mViewList.size());
		}
	}
	
	private class HorizontalListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mDoctorList != null){
				return mDoctorList.size();
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
			HorizontalListItemView view;
			if (convertView == null){
				view = new HorizontalListItemView(ProductDetailActivity.this);
			}else{
				view = (HorizontalListItemView) convertView;
			}
			if (mDoctorList != null && !mDoctorList.isEmpty() && position<mDoctorList.size()){
				view.update(mDoctorList.get(position));
			}
			return view;
		}
		
	}
	
	private class UploadUserActionTask extends AsyncTask<Void, Void, String>{

		@Override
		protected String doInBackground(Void... params) {
			try {
				NetEngine.getInstance(ProductDetailActivity.this).
					uploadUserAction(mProduct.getId()+"", mProduct.getOwner().getUid());
			} catch (PediatricsIOException e) {
				e.printStackTrace();
			} catch (PediatricsParseException e) {
				e.printStackTrace();
			} catch (PediatricsApiException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
}
