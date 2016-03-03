package com.ieeton.agency.activity;

import com.ieeton.agency.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class SelectPhotoDialog extends Dialog implements android.view.View.OnClickListener{

	public static interface ButtonClickListener{
		public void onButtonClick(int tag);
	}
	
	public static final int BUTTON_CAMERA = 1;
	public static final int BUTTON_ALBUM = 2;
	
	private Context mContext;
	ButtonClickListener mListener;
	
	private Button mBtnCamera;
	private Button mBtnAlbum;
	private Button mBtnCancel;

	public SelectPhotoDialog(Context context) {
		super(context);
		mContext = context;
	}
	
	public SelectPhotoDialog(Context context, int theme, ButtonClickListener listener) {
		super(context, theme);
		mContext = context;
		mListener = listener;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_photo_dialog);
		initView();
	}
	
	private void initView(){
		mBtnCamera = (Button)findViewById(R.id.btn_camera);
		mBtnAlbum = (Button)findViewById(R.id.btn_album);
		mBtnCancel = (Button)findViewById(R.id.btn_cancel);
		
		mBtnCamera.setOnClickListener(this);
		mBtnAlbum.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		
		setCancelable(true);
		Window dialogWindow = this.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnCamera){
			if (mListener != null){
				mListener.onButtonClick(BUTTON_CAMERA);
			}
			dismiss();
		} else if (v == mBtnAlbum){
			if (mListener != null){
				mListener.onButtonClick(BUTTON_ALBUM);
			}
			dismiss();
		} else if (v == mBtnCancel){
			this.dismiss();
		} 
	}
}
