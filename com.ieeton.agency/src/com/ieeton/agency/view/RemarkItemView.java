package com.ieeton.agency.view;

import com.ieeton.agency.models.Remark;
import com.ieeton.agency.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RemarkItemView extends LinearLayout {
	private Context mContext;
	private Remark mRemark;
	
	TextView mTvTime;
	TextView mTvContent;
	
	public RemarkItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	
	public RemarkItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public RemarkItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	public RemarkItemView(Context context, Remark remark) {
		super(context);
		mContext = context;
		mRemark = remark;
		initView();
		update(remark);
	}
	
	private void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.remark_item, this);
		
		mTvTime = (TextView) findViewById(R.id.tv_time);
		mTvContent = (TextView) findViewById(R.id.tv_content);
	}
	
	public void update(Remark remark){
		mRemark = remark;
		if (mRemark == null){
			return;
		}
		
		mTvTime.setText(mRemark.getTime());
		mTvContent.setText(mRemark.getContent());
	}
}
