package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;

import com.ieeton.user.IeetonApplication;
import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Lable;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Utils;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class SelectLableActivity extends TemplateActivity {
	public static String EXTRA_MIN_AGE = "extra_minAge";
	public static String EXTRA_MAX_AGE = "extra_maxAge";
	
	private List<TextView> mLableTvList;
	private List<CheckBox> mCheckBoxList;
	private Button mBtnNext;
	private Button mBtnJump;
	private int mMinAge;
	private int mMaxAge;

	@Override
	protected void handleTitleBarEvent(int eventId) {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_lable);
		setTitleBar(null, null, null);
		mMinAge = getIntent().getIntExtra(EXTRA_MIN_AGE, 0);
		mMaxAge = getIntent().getIntExtra(EXTRA_MAX_AGE, 12);
		
		mLableTvList = new ArrayList<TextView>();
		mLableTvList.add((TextView) findViewById(R.id.tv_lable1));
		mLableTvList.add((TextView) findViewById(R.id.tv_lable2));
		mLableTvList.add((TextView) findViewById(R.id.tv_lable3));
		mLableTvList.add((TextView) findViewById(R.id.tv_lable4));
		mLableTvList.add((TextView) findViewById(R.id.tv_lable5));
		mLableTvList.add((TextView) findViewById(R.id.tv_lable6));
		mLableTvList.add((TextView) findViewById(R.id.tv_lable7));
		mLableTvList.add((TextView) findViewById(R.id.tv_lable8));
		mLableTvList.add((TextView) findViewById(R.id.tv_lable9));
		
		mCheckBoxList = new ArrayList<CheckBox>();
		mCheckBoxList.add((CheckBox) findViewById(R.id.cb1));
		mCheckBoxList.add((CheckBox) findViewById(R.id.cb2));
		mCheckBoxList.add((CheckBox) findViewById(R.id.cb3));
		mCheckBoxList.add((CheckBox) findViewById(R.id.cb4));
		mCheckBoxList.add((CheckBox) findViewById(R.id.cb5));
		mCheckBoxList.add((CheckBox) findViewById(R.id.cb6));
		mCheckBoxList.add((CheckBox) findViewById(R.id.cb7));
		mCheckBoxList.add((CheckBox) findViewById(R.id.cb8));
		mCheckBoxList.add((CheckBox) findViewById(R.id.cb9));
		
		mBtnNext = (Button) findViewById(R.id.btn_next);
		mBtnNext.setOnClickListener(this);
		mBtnJump = (Button) findViewById(R.id.btn_jump);
		mBtnJump.setOnClickListener(this);
		updateLable();
	}

	private void updateLable(){
		List<Lable> list = IeetonApplication.mServerHostData.getLableList();
		if (list == null || list.isEmpty()){
			return;
		}
		for(int i=0; i<mLableTvList.size(); i++){
			mLableTvList.get(i).setText(list.get(i).getName());
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == mBtnNext || v == mBtnJump){
//			if (v == mBtnNext){
				new UpdateAgeTask().execute();
//			}
			Utils.setUserGuideStatus(this);						
	    	startActivity(new Intent(this, TaskTopActivity.class));  
	    	finish();
		}
		super.onClick(v);
	}

	private class UpdateAgeTask extends AsyncTask<Void, Void, String>{

		@Override
		protected String doInBackground(Void... params) {
			String lable = "";
			String result = "";
			try {
				List<Lable> list = IeetonApplication.mServerHostData.getLableList();
				if (list == null || list.isEmpty()){
					return null;
				}
				for(int i=0; i<mLableTvList.size(); i++){
					if (mCheckBoxList.get(i).isChecked()){
						if (TextUtils.isEmpty(lable)){
							lable += (list.get(i).getId() + "");
						}else{
							lable += (","+list.get(i).getId());
						}
					}
				}
				result = NetEngine.getInstance(SelectLableActivity.this).
					updateAgeLable(mMinAge, mMaxAge, lable);
			} catch (PediatricsIOException e) {
				e.printStackTrace();
			} catch (PediatricsParseException e) {
				e.printStackTrace();
			} catch (PediatricsApiException e) {
				e.printStackTrace();
			}
			return result;
		}
		
	}
}
