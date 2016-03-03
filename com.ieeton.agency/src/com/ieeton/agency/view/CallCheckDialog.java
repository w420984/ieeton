package com.ieeton.agency.view;

import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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

public class CallCheckDialog extends Dialog implements android.view.View.OnClickListener{

	private Context mContext;
	private Doctor mDoctor;
	
	private TextView mTvPrice;
	private TextView mTvMoney;
	private Button mBtnCall;
	private Button mBtnCancel;
	private ViewGroup mVgAddMoney;

	public CallCheckDialog(Context context) {
		super(context);
		mContext = context;
	}

	public CallCheckDialog(Context context, int theme, Doctor doctor){
		super(context, theme);
		mContext = context;
		mDoctor = doctor;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.call_dialog);
		initView();
	}
	
	private void initView(){
		mTvPrice = (TextView)findViewById(R.id.call_price);
		String price = String.format(mContext.getString(R.string.call_price), mDoctor.getPrice()+"");
		mTvPrice.setText(price);
		
		mTvMoney = (TextView)findViewById(R.id.money);
		String money = String.format(mContext.getString(R.string.rest_money), 100);
		mTvMoney.setText(money);
		
		mBtnCall = (Button)findViewById(R.id.btn_call);
		mBtnCancel = (Button)findViewById(R.id.btn_cancel);
		mVgAddMoney = (ViewGroup)findViewById(R.id.add_money);
		
		mBtnCall.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		mVgAddMoney.setOnClickListener(this);
		
		setCancelable(false);
		Window dialogWindow = this.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnCall){
			
		}else if (v == mBtnCancel){
			this.dismiss();
		}else if (v == mVgAddMoney){
			
		}
	}
}
