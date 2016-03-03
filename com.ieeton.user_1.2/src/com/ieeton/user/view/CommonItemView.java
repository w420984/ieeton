package com.ieeton.user.view;

import com.ieeton.user.R;
import com.ieeton.user.models.CommonItem;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommonItemView extends RelativeLayout {
	private TextView mTvName;
	private ImageView mIvRight;
	
	private Context mContext;
	
	public CommonItemView(Context context) {
		super(context);
		initView();
		mContext = context;
	}

	public CommonItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public CommonItemView(Context context, AttributeSet attrs, int arg) {
		super(context, attrs);
		mContext = context;
		initView();
	}
		
	private void initView(){
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.common_item_view, this);
    	mTvName = (TextView) findViewById(R.id.tv_name);
    	mIvRight = (ImageView) findViewById(R.id.iv_icon_right);
	}
	
	public void update(CommonItem item){
		if (item == null){
			return;
		}
		mTvName.setText(item.getName());
		if (item.hasNextLevel()){
			mIvRight.setVisibility(View.VISIBLE);
		}else{
			mIvRight.setVisibility(View.GONE);
		}
	}
}
