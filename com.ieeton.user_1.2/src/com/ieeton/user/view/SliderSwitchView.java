package com.ieeton.user.view;

import com.ieeton.user.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;


public class SliderSwitchView extends View implements OnTouchListener{
	public static final int SWITCH_ON_OFF = 0;
	public static final int SWITCH_MALE_FEMALE = 1;
	private Bitmap mSwitchOn;
	private Bitmap mSwitchOff;
	private Bitmap mSlipperButton;
	/**
	 * 按下时的x和当前的x
	 */
	private float mDownX;
	private float mCurX;
	
	/**
	 * 记录用户是否在滑动
	 */
	private boolean mIsOnSlip = false;
	
	/**
	 * 当前的状态
	 */
	private boolean mCurStatus = false;
	
	/**
	 * 监听接口
	 */
	private OnChangedListener mSwitchChangeListener;
	
	
	public SliderSwitchView(Context context) {
		super(context);
		init();
	}

	public SliderSwitchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public void init(){
		//载入图片资源
		mSwitchOn = BitmapFactory.decodeResource(getResources(), R.drawable.switch_male);
		mSwitchOff = BitmapFactory.decodeResource(getResources(), R.drawable.switch_female);
		mSlipperButton = BitmapFactory.decodeResource(getResources(), R.drawable.switch_button);
		
		setOnTouchListener(this);
	}
	
	public void setSwitchType(int type){
		Resources res = getResources();
		if(type == SWITCH_ON_OFF){
			mSwitchOn = BitmapFactory.decodeResource(getResources(), R.drawable.switch_on);
			mSwitchOff = BitmapFactory.decodeResource(getResources(), R.drawable.switch_off);
			mSlipperButton = BitmapFactory.decodeResource(getResources(), R.drawable.switch_button);			
		}else if(type == SWITCH_MALE_FEMALE){
			mSwitchOn = BitmapFactory.decodeResource(getResources(), R.drawable.switch_male);
			mSwitchOff = BitmapFactory.decodeResource(getResources(), R.drawable.switch_female);
			mSlipperButton = BitmapFactory.decodeResource(getResources(), R.drawable.switch_button);			
		}
		
		//refresh
		invalidate();
	}
	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		float x = 0;
		
		//根据nowX设置背景，开或者关状态
		if (mCurX < (mSwitchOn.getWidth()/2)){
			canvas.drawBitmap(mSwitchOff, matrix, paint);//画出关闭时的背景
		}else{
			canvas.drawBitmap(mSwitchOn, matrix, paint);//画出打开时的背景 
		}
		
		if (mIsOnSlip) {//是否是在滑动状态,  
			if(mCurX >= mSwitchOn.getWidth())//是否划出指定范围,不能让滑块跑到外头,必须做这个判断
				x = mSwitchOn.getWidth() - mSlipperButton.getWidth()/2;//减去滑块1/2的长度
			else
				x = mCurX - mSlipperButton.getWidth()/2;
		}else {
			if(mCurStatus){//根据当前的状态设置滑块的x值
				x = mSwitchOn.getWidth() - mSlipperButton.getWidth();
			}else{
				x = 0;
			}
		}
		
		//对滑块滑动进行异常处理，不能让滑块出界
		if (x < 0 ){
			x = 0;
		}
		else if(x > mSwitchOn.getWidth() - mSlipperButton.getWidth()){
			x = mSwitchOn.getWidth() - mSlipperButton.getWidth();
		}
		
		//画出滑块
		canvas.drawBitmap(mSlipperButton, x , 0, paint); 
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:{
			if (event.getX() > mSwitchOff.getWidth() || event.getY() > mSwitchOff.getHeight()){
				return false;
			}else{
				mIsOnSlip = true;
				mDownX = event.getX();
				mCurX = mDownX;
			}
			break;
		}
		case MotionEvent.ACTION_MOVE:{
			mCurX = event.getX();
			break;
		}
		case MotionEvent.ACTION_UP:{
			mIsOnSlip = false;
			if(event.getX() >= (mSwitchOn.getWidth()/2)){
				mCurStatus = true;
				mCurX = mSwitchOn.getWidth() - mSlipperButton.getWidth();
			}else{
				mCurStatus = false;
				mCurX = 0;
			}
			
			if(mSwitchChangeListener != null){
				mSwitchChangeListener.OnChanged(SliderSwitchView.this, mCurStatus);
			}
			break;
		}
		}
		//刷新界面
		invalidate();
		return true;
	}
	
	
	
	@Override
	public void setLayoutParams(LayoutParams params) {
		// TODO Auto-generated method stub
		super.setLayoutParams(params);
		params.width = mSwitchOn.getWidth();
		params.height = mSwitchOn.getHeight();
	}

	/**
	 * 为SliderSwitchView设置一个监听，供外部调用的方法
	 * @param mSwitchChangeListener
	 */
	public void setOnChangedListener(OnChangedListener mSwitchChangeListener){
		this.mSwitchChangeListener = mSwitchChangeListener;
	}
	
	
	/**
	 * 设置滑动开关的初始状态，供外部调用
	 * @param checked
	 */
	public void setChecked(boolean checked){
		if(checked){
			mCurX = mSwitchOff.getWidth();
		}else{
			mCurX = 0;
		}
		mCurStatus = checked;
	}

	
    /**
     * 回调接口
     *
     */
	public interface OnChangedListener {
		public void OnChanged(SliderSwitchView sliderSwitch, boolean checkState);
	}


}
