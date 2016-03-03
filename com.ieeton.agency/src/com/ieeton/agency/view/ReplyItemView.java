package com.ieeton.agency.view;

import com.ieeton.agency.models.ReplyTemplate;
import com.ieeton.agency.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ReplyItemView extends RelativeLayout {
	private Context mContext;
	private ReplyTemplate mReply;
	
	TextView mTvNum;
	TextView mTvContent;
	
	public ReplyItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	
	public ReplyItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public ReplyItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}
	
	private void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.reply_item, this);
		
		mTvNum = (TextView) findViewById(R.id.tv_num);
		mTvContent = (TextView) findViewById(R.id.tv_content);
	}
	
	public void update(ReplyTemplate reply, int num){
		mReply = reply;
		if (mReply == null){
			return;
		}
		
		mTvNum.setText((num+1)+"");
		mTvContent.setText(mReply.getContent());
	}
}
