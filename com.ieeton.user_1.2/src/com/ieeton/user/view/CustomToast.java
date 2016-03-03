package com.ieeton.user.view;

import java.lang.reflect.Field;

import com.ieeton.user.utils.Utils;

import android.content.Context;
import android.os.Handler;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;


public class CustomToast {
	private Handler mHandler = null;
	private Context mContext;
	private Toast mToast;
	private boolean isCancelled;

	public void cancel() {
		isCancelled = true;
		mToast.cancel();
	}

	public boolean isShown() {
		return !isCancelled;
	}

	public void setText(int res, Context context) {
		Context a = context.getApplicationContext();
		LinearLayout pgLayout = Utils.createLoadingLayout(res, a);
		mToast.setView(pgLayout);
	}

	public void show() {
		isCancelled = false;
		doShow();
	}

	private void doShow() {
		// make sure the toast is shutdown when app close
		if (!isCancelled) {
			mToast.show();
			mHandler.postDelayed(new Runnable() {
				public void run() {
					doShow();
				}
			}, 1000);
		}
	}

	public CustomToast(Context context, int res , boolean needProgressBar) {
		mHandler = new Handler();
		mContext = context.getApplicationContext();
		if(needProgressBar){
			mToast = Utils.createProgressToast(res, mContext);
		}else{
			mToast = Utils.createToast(res, context);
		}
//		clearAnimation();
		isCancelled = true;
	}

    public boolean clearAnimation() {
        try {
            Field fieldTN = mToast.getClass().getDeclaredField("mTN");
            fieldTN.setAccessible(true);
            Object objTN = fieldTN.get(mToast);
            Field fieldParams = objTN.getClass().getDeclaredField("mParams");
            fieldParams.setAccessible(true);
            WindowManager.LayoutParams params = (LayoutParams) fieldParams
                    .get(objTN);
            params.windowAnimations = 0;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
