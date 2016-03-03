package com.ieeton.agency.view;

import java.util.Date;
import java.util.HashMap;

import com.ieeton.agency.activity.BrowserActivity;
import com.ieeton.agency.activity.UserProfileActivity;
import com.ieeton.agency.models.Article;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.AsyncBitmapLoader;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.utils.AsyncBitmapLoader.ImageCallBack;
import com.ieeton.agency.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DiscoverListItemView extends RelativeLayout implements android.view.View.OnClickListener{
	private Context mContext;
	private View mView;
	private TextView mTitle;
	private TextView mSummary;
	private ImageView mSummaryPic;
	private Article mArtical;
	
	private TextView mLikedNums;
	private TextView mReadedNums;
	private TextView mTimeLine;
	
	private CheckBox mCheckBox;
	
	private ImageView mPortraint;
	private TextView mName;
	private RelativeLayout mUserInfoLayout;
	private LinearLayout mItemLayout;
	private boolean isShowUserInfo;
	

	public DiscoverListItemView(Context context) {
		super(context);
		mContext = context;
		isShowUserInfo = true;
		init();
	}
	
	public DiscoverListItemView(Context context, boolean isShowUser) {
		super(context);
		mContext = context;
		isShowUserInfo = isShowUser;
		init();
	}

	public DiscoverListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		isShowUserInfo = true;
		init();
	}
	
	public DiscoverListItemView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		isShowUserInfo = true;
		init();
	}

	public void init(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.discover_list_item, this);
		
		initViews();

	}
	
	public void initViews(){
		mItemLayout = (LinearLayout)findViewById(R.id.ll_item);
		mItemLayout.setOnClickListener(this);
		
		mUserInfoLayout = (RelativeLayout) findViewById(R.id.rl_userinfo);
		mUserInfoLayout.setOnClickListener(this);
		if (!isShowUserInfo){
			mUserInfoLayout.setVisibility(View.GONE);
		}
		
		mPortraint = (ImageView)findViewById(R.id.iv_portrait);
		mName = (TextView)findViewById(R.id.tv_username);
		
		mTitle = (TextView)findViewById(R.id.tv_content_title);
		mSummary = (TextView)findViewById(R.id.tv_content_description);
		mSummaryPic = (ImageView)findViewById(R.id.iv_image_thumbtail);
		
		mLikedNums = (TextView)findViewById(R.id.tv_liked_num);
		mReadedNums = (TextView)findViewById(R.id.tv_readed_num);
		mTimeLine = (TextView)findViewById(R.id.tv_timeline);
		
		mCheckBox = (CheckBox) findViewById(R.id.check_box);
	}
	
	public CheckBox getCheckBox(){
		return mCheckBox;
	}
	
	public void update(Article artical, boolean isShowCheckBox, HashMap<String, Boolean> map){		
		mArtical = artical;
		mTitle.setText(mArtical.getTitle());
		mSummary.setText(mArtical.getSummary());
		Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(mContext,
						NetEngine.getImageUrl(mArtical.getSummaryPicUrl()), 
						new ImageCallBack() {
			@Override
			public void imageLoad(Bitmap bitmap, Object user) {
				if (bitmap !=null && !bitmap.isRecycled()){
					mSummaryPic.setImageBitmap(bitmap);
				}
			}
		});
		if (b !=null && !b.isRecycled()){
			mSummaryPic.setImageBitmap(b);
		}
		
		mLikedNums.setText(String.valueOf(mArtical.getLikedNums()));
		mReadedNums.setText(String.valueOf(mArtical.getReadNums()));
		
		String timeStr = mArtical.getPublishTime().split("\\+")[0];
		timeStr = timeStr.replace("T", " ");
		Date date = Utils.strToDateLong(timeStr);
		mTimeLine.setText(Utils.formatDate(mContext, date));
		if (isShowUserInfo){
			Bitmap bitmap = AsyncBitmapLoader.getInstance().loadBitmap(mContext, mArtical.getPublisherInfo().getId(), 
					NetEngine.getImageUrl(mArtical.getPublisherInfo().getAvatar()), 
					mArtical.getPublisherInfo().getType(), new ImageCallBack() {
						@Override
						public void imageLoad(Bitmap bitmap, Object user) {
							if (bitmap !=null && !bitmap.isRecycled()){
								mPortraint.setImageBitmap(bitmap);
							}else{
								mPortraint.setImageResource(Utils.getDefaultPortraitId(mArtical.getPublisherInfo().getType(), null));
							}
						}
					});
			if (bitmap !=null && !bitmap.isRecycled()){
				mPortraint.setImageBitmap(bitmap);
			}else{
				mPortraint.setImageResource(Utils.getDefaultPortraitId(mArtical.getPublisherInfo().getType(), null));
			}
		}
		
		mName.setText(mArtical.getPublisherInfo().getName());
		if (isShowCheckBox){
			mCheckBox.setVisibility(View.VISIBLE);
		}else {
			mCheckBox.setVisibility(View.GONE);
		}		
		//Utils.logd("update map:"+map);
		if (map != null){
			boolean isChecked = false;
			if (map.containsKey(mArtical.getID())){
				isChecked = map.get(mArtical.getID());
			}
			mCheckBox.setChecked(isChecked);
		}
	}

	@Override
	public void onClick(View view) {
		if(view == mUserInfoLayout){
			Intent intent = new Intent(mContext, UserProfileActivity.class);
			intent.putExtra(UserProfileActivity.EXTRA_USERINFO, mArtical.getPublisherInfo());
			mContext.startActivity(intent);
		}else if(view == mItemLayout){
			Intent intent = new Intent(mContext, BrowserActivity.class);
			if(mArtical.getExternalUrl() == null || "".equals(mArtical.getExternalUrl())){
				//内部文章
				intent.putExtra(Constants.WEB_BROWSER_URL, Constants.SERVER_HOST_SHARE_ARTICLE_SERVER);
				intent.putExtra(Constants.WEB_BROWSER_INTERNAL, true);
				intent.putExtra(Constants.WEB_BROWSER_TITLE, getResources().getString(R.string.artical_detail));
			}else{
				//外部文章，跳转到内置浏览器
				intent.putExtra(Constants.WEB_BROWSER_URL, mArtical.getExternalUrl());
				intent.putExtra(Constants.WEB_BROWSER_INTERNAL, false);
			}
			intent.putExtra(BrowserActivity.EXTRA_SHOW_BOTTOM, true);
			intent.putExtra("artical", mArtical);
			mContext.startActivity(intent);
		}
	}
}
