package com.ieeton.user.view;


import com.ieeton.user.R;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DoctorListItemView extends LinearLayout{
	private Context mContext;
	private RoundedImageView mHeader;
	private TextView mName;
	
	public DoctorListItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	
	public DoctorListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public DoctorListItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	public void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.doctor_list_item_view, this);
		mHeader = (RoundedImageView) findViewById(R.id.header);
		mName = (TextView) findViewById(R.id.name);
	}
	
	public void update(IeetonUser user){
		if (user == null){
			return;
		}
		mName.setText(user.getName());
		AsyncBitmapLoader.getInstance().loadBitmap(mContext, mHeader, 
				NetEngine.getImageUrl(user.getAvatar()));
	}
}
