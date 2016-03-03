package com.ieeton.user.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.ieeton.user.R;

public class UserGuideImageView extends RelativeLayout{
	private Context mContext;
	public UserGuideImageView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	
	public UserGuideImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public UserGuideImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	public void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.guide_image_pager_view, this);

	}
}
