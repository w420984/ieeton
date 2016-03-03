package com.ieeton.user.view;

import java.util.ArrayList;
import java.util.List;

import com.ieeton.user.R;
import com.ieeton.user.activity.BrowserActivity;
import com.ieeton.user.activity.DiscoveryFragment;
import com.ieeton.user.models.Article;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MyViewPager extends FrameLayout {
	private Context mContext;
	private DiscoveryFragment mFragment;
	private ViewPagerAdapter mPagerAdapter;	
	private List<Article> mRecommandList;
	
	private View mView;
	private ViewGroup mVgPager;
	private ViewPager mPager;
	private TextView mTvIndicator;
	private List<View> mViewList;
	private TextView mTvArticleTitle;
	
	public MyViewPager(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public MyViewPager(Context context, DiscoveryFragment fragment){
		super(context);
		mContext = context;
		mFragment = fragment;
		initView();
	}
	
	public MyViewPager(Context context, AttributeSet attrs){
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public MyViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	private void initView(){
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	mView = inflater.inflate(R.layout.view_pager, this);

		mTvArticleTitle = (TextView) mView.findViewById(R.id.tv_article_title);
    	mVgPager = (ViewGroup) mView.findViewById(R.id.fl_pager);
		mPager = (ViewPager) mView.findViewById(R.id.pic_viewpager);
		mTvIndicator = (TextView) mView.findViewById(R.id.tv_index);
	}
	
	public void showPager(List<Article> list){
		mRecommandList = list;
    	mViewList = new ArrayList<View>();
    	for (int i=0; i<mRecommandList.size(); i++){
    		View item = new ViewPagerItemView(mContext);
    		mViewList.add(item);
    	}
    	initIndicator();
		if (mRecommandList.size()>0){
			mTvArticleTitle.setText(mRecommandList.get(0).getTitle());
		}
    	mPagerAdapter = new ViewPagerAdapter();
    	mPager.setAdapter(mPagerAdapter);
    	mPager.setOnPageChangeListener(new OnPageChangeListener() {			
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
    	    		if (mRecommandList.size()>0){
    	    			mTvArticleTitle.setText(mRecommandList.get(0).getTitle());
    	    		}
    			}else{
    				mTvIndicator.setVisibility(View.VISIBLE);
    				mTvIndicator.setText((position+1)+"/"+mViewList.size());
    	    		mTvArticleTitle.setText(mRecommandList.get(position).getTitle());
    			}
    		}
		});
    	
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
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, view.getPic(), 
					NetEngine.getImageUrl(mRecommandList.get(position).getSummaryPicUrl()));
			container.removeView(mViewList.get(position));
			container.addView(mViewList.get(position));
 
			view.getPic().setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Article article = mRecommandList.get(position);
					Intent intent = new Intent(mContext, BrowserActivity.class);
					if(TextUtils.isEmpty(article.getExternalUrl())){
						//内部文章
						intent.putExtra(Constants.WEB_BROWSER_URL, Constants.SERVER_HOST_SHARE_ARTICLE_SERVER);
						intent.putExtra(Constants.WEB_BROWSER_INTERNAL, "true");
						intent.putExtra(Constants.WEB_BROWSER_TITLE, getResources().getString(R.string.artical_detail));
					}else{
						//外部文章，跳转到内置浏览器
						intent.putExtra(Constants.WEB_BROWSER_URL, article.getExternalUrl());
						intent.putExtra(Constants.WEB_BROWSER_INTERNAL, "false");
					}
					intent.putExtra(BrowserActivity.EXTRA_SHOW_BOTTOM, "true");
					intent.putExtra("artical", article);
					mFragment.startActivity(intent);
				}
			});
			
            return mViewList.get(position);
		}
		
	}
}
