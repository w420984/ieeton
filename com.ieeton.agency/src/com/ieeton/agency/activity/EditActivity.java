package com.ieeton.agency.activity;

import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

public class EditActivity extends TemplateActivity {
	public static String INPUT_CONTENT = "content";
	public static String EXTRA_MODE = "extra_mode";
	public static String EXTRA_CONTENT = "extra_content";
	
	public static final int MODE_REMARK = 1;
	public static final int MODE_RESET_NAME = 2;
	public static final int MODE_RESET_SKILLED = 3;
	public static final int MODE_REPLY = 4;
	
	private EditText mEtContent;
	private int mMode;
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			save();
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_edit);
		
		mMode = getIntent().getIntExtra(EXTRA_MODE, -1);
		String title = "";
		if (mMode == MODE_REMARK){
			title = getString(R.string.title_remark);
		}else if (mMode == MODE_RESET_NAME){
			title = getString(R.string.reset_name_title);
		}else if (mMode == MODE_RESET_SKILLED){
			title = getString(R.string.reset_skill_title);
		}else if (mMode == MODE_REPLY){
			title = getString(R.string.quick_reply);
		}
		setTitleBar(getString(R.string.back), title, 
					getString(R.string.ok));
		
		mEtContent = (EditText) findViewById(R.id.content);
		String content = getIntent().getStringExtra(EXTRA_CONTENT);
		if (!TextUtils.isEmpty(content)){
			mEtContent.setText(content);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	
	private void save(){
		String content = mEtContent.getText().toString();
		if (TextUtils.isEmpty(content)){
			Utils.showToast(this, R.string.input_null_error, Toast.LENGTH_SHORT);
			return;
		}
		if (mMode == MODE_RESET_NAME){
			if (Utils.checkSpecialCharacters(content)){
				Utils.showToast(this, R.string.nick_containt_special_character, Toast.LENGTH_SHORT);
				return;
			}
		}
		Intent data = new Intent();
		data.putExtra(INPUT_CONTENT, content);
		setResult(RESULT_OK, data);
		finish();
	}
}
