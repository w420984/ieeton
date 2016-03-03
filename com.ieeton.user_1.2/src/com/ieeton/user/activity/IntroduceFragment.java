package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.ViewPagerItemView;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class IntroduceFragment extends Fragment implements 
		OnPageChangeListener, OnClickListener{
	
	private View mView;
	private TextView mTvName;
	private TextView mTvFans;
	private TextView mTvIntroduce;
	private TextView mTvAddress;
	private ViewGroup mVgAddress;
	
	private ViewGroup mVgPager;
	private ViewPager mPager;
	private TextView mTvIndicator;
	private List<View> mViewList;
	private ViewPagerAdapter mPagerAdapter;

	private VideoView mVideoView;
	private ImageView mIvPlay;
	private ViewGroup mVgVideo;	
	private ImageView mIvVideo;
	
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private GeoCoder mSearch;
		
	private InstitutionActivity mActivity;
	private IeetonUser mUser;
	private String mUserId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_introduce, container, false);
		initView();
		mActivity = (InstitutionActivity) getActivity();
		mUser = mActivity.mUser;
		mUserId = mActivity.mUserId;
		return mView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		updateUI();
		if (mUser == null){
			new FetchDataTask().execute();
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mIvPlay){
			if (mUser == null){
				return;
			}
			Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
			intent.putExtra(Constants.EXTRA_URL, mUser.getVideoUrl());
			startActivity(intent);
//			playVideo();
		}else if (v == mVgAddress){
			Intent intent = new Intent(getActivity(), BaiduMapActivity.class);
			intent.putExtra("latitude", mUser.getLatitude());
			intent.putExtra("longitude", mUser.getLongitude());
			intent.putExtra("address", mUser.getAddress());
			startActivity(intent);
		}
	}
	
	private void initView(){
		mTvName = (TextView) mView.findViewById(R.id.tv_name);
		mTvFans = (TextView) mView.findViewById(R.id.tv_fans);
		mTvIntroduce = (TextView) mView.findViewById(R.id.tv_introduce);
		mTvAddress = (TextView) mView.findViewById(R.id.tv_address);
		mVgAddress = (ViewGroup) mView.findViewById(R.id.rl_address);
		mVgAddress.setOnClickListener(this);

		mVideoView = (VideoView) mView.findViewById(R.id.video);
		mIvPlay = (ImageView) mView.findViewById(R.id.iv_play);
		mIvPlay.setOnClickListener(this);
		mVgVideo = (ViewGroup) mView.findViewById(R.id.fl_video);
		mIvVideo = (ImageView) mView.findViewById(R.id.iv_video);
		
		mMapView = (MapView) mView.findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
	}

	private void updateUI(){
		if (mUser == null){
			return;
		}
		mTvName.setText(mUser.getName());
		mTvFans.setText(getString(R.string.fans)+"  "+mUser.getFollowCount());
		mTvIntroduce.setText(Html.fromHtml(mUser.getDescription()));
		mTvAddress.setText(mUser.getAddress());
		
		showPager();
		showVideo();
		showMap();
	}
	
	private void showMap() {
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
			
			@Override
			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
				if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//					Toast.makeText(BaiduMapActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
//							.show();
					return;
				}
				mBaiduMap.clear();
				mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.icon_marka)));
				mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
						.getLocation()));
				mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(17));
//				Toast.makeText(BaiduMapActivity.this, result.getAddress(),
//						Toast.LENGTH_LONG).show();

			}
			
			@Override
			public void onGetGeoCodeResult(GeoCodeResult result) {
				if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
//					Toast.makeText(BaiduMapActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
//							.show();
					return;
				}
				mBaiduMap.clear();
				mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.icon_marka)));
				mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
						.getLocation()));
				mBaiduMap.animateMapStatus(MapStatusUpdateFactory.zoomTo(17));
//				String strInfo = String.format("纬度：%f 经度：%f",
//						result.getLocation().latitude, result.getLocation().longitude);
//				Toast.makeText(BaiduMapActivity.this, strInfo, Toast.LENGTH_LONG).show();
			}
		});
		
		double latitude = mUser.getLatitude();
		double longtitude = mUser.getLongitude();
		String address = mUser.getAddress();
		mMapView.setVisibility(View.VISIBLE);
		
		if (latitude != 0){
			LatLng ptCenter = new LatLng(latitude, longtitude);
			// 反Geo搜索
			mSearch.reverseGeoCode(new ReverseGeoCodeOption()
					.location(ptCenter));
		}else if (!TextUtils.isEmpty(address)){
			mSearch.geocode(new GeoCodeOption().address(address));
		}
	}

	private void showVideo(){
		if (TextUtils.isEmpty(mUser.getVideoUrl())){
			mVgVideo.setVisibility(View.GONE);
		}else{
			mVgVideo.setVisibility(View.VISIBLE);
//			MediaController controller = new MediaController(mActivity);
//			controller.setMediaPlayer(mVideoView);
//			mVideoView.setMediaController(controller);
//			mVideoView.setVideoPath(mUser.getVideoUrl());
//			mVideoView.seekTo(5000);
			Bitmap bitmap = null;
			// 获取视频的缩略图
			bitmap = Utils.createVideoThumbnail(mUser.getVideoUrl(), getResources().getDimensionPixelSize(R.dimen.video_height), 
					getResources().getDimensionPixelSize(R.dimen.video_height));
			//System.out.println("bitmap"+bitmap);
			if (bitmap != null){
				mIvVideo.setImageBitmap(bitmap);
			}
		}
	}
	
	private void playVideo(){
		if (mUser == null){
			return;
		}
		mVideoView.seekTo(0);
		mVideoView.start();
		mIvPlay.setVisibility(View.GONE);
	}
	
	private void showPager(){
		mVgPager = (ViewGroup) mView.findViewById(R.id.fl_pager);
		mPager = (ViewPager) mView.findViewById(R.id.pic_viewpager);
		mTvIndicator = (TextView) mView.findViewById(R.id.tv_index);
		
		if (mUser == null){
			mVgPager.setVisibility(View.GONE);
			return;
		}
		if (mUser.getPicList() == null || mUser.getPicList().isEmpty()){
			mVgPager.setVisibility(View.GONE);
			return;
		}
		mVgPager.setVisibility(View.VISIBLE);
    	mViewList = new ArrayList<View>();
    	for (int i=0; i<mUser.getPicList().size(); i++){
    		View item = new ViewPagerItemView(mActivity);
    		mViewList.add(item);
    	}
    	initIndicator();
    	mPagerAdapter = new ViewPagerAdapter();
    	mPager.setAdapter(mPagerAdapter);
    	mPager.setOnPageChangeListener(this);
	}
		
	@Override
	public void onDestroy() {
		mMapView.onDestroy();
		if (mSearch != null){
			mSearch.destroy();
		}
		super.onDestroy();
	}

	@Override
	public void onPause() {
		MobclickAgent.onPageEnd("IntroduceFragment"); 
		mMapView.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		MobclickAgent.onPageStart("IntroduceFragment"); 
		mMapView.onResume();
		super.onResume();
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
	
	private class ViewPagerAdapter extends PagerAdapter{
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
			AsyncBitmapLoader.getInstance().loadBitmap(mActivity, view.getPic(), 
					NetEngine.getImageUrl(mUser.getPicList().get(position)));
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
	
	private class FetchDataTask extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;
	    
		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			try {
				result = NetEngine.getInstance(mActivity).
						getUserInfo(mUserId);
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
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			//dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, mActivity);
				}else{
					//Utils.showToast(ProductDetailActivity.this, R.string.get_account_details_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			JSONObject obj;
			try {
				obj = new JSONObject(result);
				mUser = new IeetonUser(getActivity(), obj);
				updateUI();
			}catch (JSONException e){
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			//showProgress();
			super.onPreExecute();
		}
		
	}

}
