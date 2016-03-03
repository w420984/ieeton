package com.ieeton.user.activity;

import java.lang.ref.SoftReference;

import com.ieeton.user.R;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.UserGuideImageView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class UserGuideActivity extends TemplateActivity{

	private ViewPager          mViewPager;
	private ImagePagerAdpter  mImagePagerAdpter;
	private int                mCurrentIndex           = 0;
	private final int[]				mGuideImages = {R.drawable.user_guide_img_1, R.drawable.user_guide_img_2, R.drawable.user_guide_img_3};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setView(R.layout.activity_guide);
		setTitleBar(null, null, null);
		mViewPager = (ViewPager)findViewById(R.id.guide_viewpager);
        mImagePagerAdpter = new ImagePagerAdpter();
        mViewPager.setAdapter( mImagePagerAdpter );
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				mCurrentIndex = position;
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
        
        mViewPager.setCurrentItem( mCurrentIndex );
	}

    private class ImagePagerAdpter extends PagerAdapter {
        private SparseArray<SoftReference<UserGuideImageView>> mSubViewCache = new SparseArray<SoftReference<UserGuideImageView>>();

        @Override
        public int getCount() {
            return mGuideImages.length;
        }

        @Override
        public boolean isViewFromObject( View view, Object obj ) {
            return view == obj;
        }

        @Override
        public void destroyItem( ViewGroup container, int position, Object object ) {
            container.removeView( (View)object );
        }

        @Override
        public Object instantiateItem( ViewGroup container, final int position ) {
        	UserGuideImageView v = null;
        	if( mSubViewCache.get( position ) != null ) {
                v = mSubViewCache.get( position ).get();
            }
        	if(v == null){
        		v = new UserGuideImageView(UserGuideActivity.this);
                mSubViewCache.put( position, new SoftReference<UserGuideImageView>(v) );
        	}
        	ImageView backgroud = (ImageView)v.findViewById(R.id.iv_image);
        	backgroud.setImageResource(mGuideImages[position]);
        	backgroud.setVisibility(View.VISIBLE);
        	ImageView button = (ImageView)v.findViewById(R.id.iv_image_button);
        	//button.setImageResource(R.drawable.start_now);
        	if(position == (getCount() - 1)){
        		button.setVisibility(View.VISIBLE);
        		button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Utils.setUserGuideStatus(UserGuideActivity.this);						
				    	startActivity(new Intent(UserGuideActivity.this, TaskTopActivity.class));  		
				    	finish();
					}
				});
        	}else{
        		button.setVisibility(View.GONE);
        	}
        	
        	if( v.getParent() == null ) {
                container.addView( v );
            }
            else {
                container.requestLayout();
                container.invalidate();
            }
    		return v;
        }
    }

	@Override
	protected void handleTitleBarEvent(int eventId) {
		// TODO Auto-generated method stub
		
	}

}
