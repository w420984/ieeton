package com.ieeton.user.view;

import com.ieeton.user.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ViewPagerItemView extends RelativeLayout {
	private ImageView mPic;

	public ViewPagerItemView(Context context) {
		super(context);
		initView();
	}

	public ViewPagerItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public ViewPagerItemView(Context context, AttributeSet attrs, int arg) {
		super(context, attrs);
		initView();
	}
		
	private void initView(){
    	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.viewpager_item, this);
    	
    	mPic = (ImageView) findViewById(R.id.pic);
	}
	
	public void setPic(Bitmap b){
		mPic.setImageBitmap(b);
	}
	
	public ImageView getPic(){
		return mPic;
	}
}
