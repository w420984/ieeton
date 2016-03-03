package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.ieeton.user.R;
import com.ieeton.user.utils.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class SelectAgeActivity extends TemplateActivity {
	private ImageView mIvAge1;
	private ImageView mIvAge2;
	private ImageView mIvAge3;
	private ImageView mIvAge4;
	private ImageView mIvPregnant;
	private Button mBtnNext1;
	private Button mBtnNext2;
	
	private int mMinAge = 0;
	private int mMaxAge = 12;

	@Override
	protected void handleTitleBarEvent(int eventId) {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_age);
		setTitleBar(null, null, null);
		
		mIvAge1 = (ImageView) findViewById(R.id.iv_age1);
		mIvAge2 = (ImageView) findViewById(R.id.iv_age2);
		mIvAge3 = (ImageView) findViewById(R.id.iv_age3);
		mIvAge4 = (ImageView) findViewById(R.id.iv_age4);
		mIvPregnant = (ImageView) findViewById(R.id.iv_pregnant);
		mBtnNext1 = (Button) findViewById(R.id.btn_next1);
		mBtnNext2 = (Button) findViewById(R.id.btn_next2);
		
		mIvAge1.setOnClickListener(this);
		mIvAge2.setOnClickListener(this);
		mIvAge3.setOnClickListener(this);
		mIvAge4.setOnClickListener(this);
		mIvPregnant.setOnClickListener(this);
		mBtnNext1.setOnClickListener(this);
		mBtnNext2.setOnClickListener(this);
	}

	private void setMyAge(){
		List<Integer> list = new ArrayList<Integer>();
		if (mIvAge1.isSelected()){
			list.add(Integer.valueOf(0));
			list.add(Integer.valueOf(1));
		}
		if (mIvAge2.isSelected()){
			list.add(Integer.valueOf(1));
			list.add(Integer.valueOf(3));
		}
		if (mIvAge3.isSelected()){
			list.add(Integer.valueOf(3));
			list.add(Integer.valueOf(6));
		}
		if (mIvAge4.isSelected()){
			list.add(Integer.valueOf(6));
			list.add(Integer.valueOf(12));
		}
		if (mIvPregnant.isSelected()){
			list.add(Integer.valueOf(0));
			list.add(Integer.valueOf(0));
		}
		if (list != null && list.size()>0){
			Object[] array = list.toArray();
			Arrays.sort(array);			
			mMinAge = ((Integer)array[0]).intValue();
			mMaxAge = ((Integer)array[array.length-1]).intValue();
		}
		Utils.logd(mMinAge+"");
		Utils.logd(mMaxAge+"");
	}
	
	@Override
	public void onClick(View v) {
		if (v == mIvAge1){
			mIvAge1.setSelected(!mIvAge1.isSelected());
		}else if (v == mIvAge2){
			mIvAge2.setSelected(!mIvAge2.isSelected());
		}else if (v == mIvAge3){
			mIvAge3.setSelected(!mIvAge3.isSelected());
		}else if (v == mIvAge4){
			mIvAge4.setSelected(!mIvAge4.isSelected());
		}else if (v == mIvPregnant){
			mIvPregnant.setSelected(!mIvPregnant.isSelected());
		}else if (v == mBtnNext1 || v == mBtnNext2){
			setMyAge();
			Intent intent = new Intent(this, SelectLableActivity.class);
			intent.putExtra(SelectLableActivity.EXTRA_MIN_AGE, mMinAge);
			intent.putExtra(SelectLableActivity.EXTRA_MAX_AGE, mMaxAge);
			startActivity(intent);
			finish();
		}
		super.onClick(v);
	}

}
