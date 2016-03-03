package com.ieeton.user.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {
    private boolean canScroll;
    private boolean isMoveY;
    
    private GestureDetector mGestureDetector;
    View.OnTouchListener mGestureListener;
 
    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(new YScrollDetector());
        canScroll = true;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_UP){
            canScroll = true;
        }else if (ev.getAction() == MotionEvent.ACTION_MOVE && isMoveY){//不响应子view里面Y方向的move时间
        	return true;
        }
        return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }
 
	int startX = 0;
	int startY = 0;
	int endX;
	int endY;
    @Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN){
			startX = (int) ev.getX();
			startY = (int) ev.getY();
			//isMoveY = false;
		}else if (ev.getAction() == MotionEvent.ACTION_MOVE){
			endX = (int) ev.getX();
			endY = (int) ev.getY();
			if (Math.abs(endY-startY) > Math.abs(endX-startX)){
				isMoveY = true;
			}
		}else if (ev.getAction() == MotionEvent.ACTION_UP){
			isMoveY = false;
		}
		return super.dispatchTouchEvent(ev);
	}

	class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(canScroll){
                if (Math.abs(distanceY) >= Math.abs(distanceX)){
                    canScroll = true;
                }else{
                    canScroll = false;
                }
            }
            return canScroll;
        }
    }
}
