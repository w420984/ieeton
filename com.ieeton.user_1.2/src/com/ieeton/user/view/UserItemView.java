package com.ieeton.user.view;

import java.util.concurrent.RejectedExecutionException;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UserItemView extends RelativeLayout implements View.OnClickListener{
	private final int FOLLOW = 1;
	private final int UNFOLLOW = 2;
	
	private Context mContext;
	private Button mBackViewFollow;
	private ImageView mAvatar;
	private TextView mTvName;
	private TextView mTvDescription;
	private IeetonUser mUser;
	private OperationTask mTask;
	private boolean mIsTaskFree = true;
	private String mUid;
	
	public UserItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public UserItemView(Context context, AttributeSet attrs){
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public UserItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}
	
	private void initView(){
		if (isInEditMode()) { return; }
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.user_item_view, this);
		
    	mBackViewFollow = (Button) findViewById(R.id.id_unfollow);
    	mAvatar = (ImageView) findViewById(R.id.iv_avatar);
    	mTvName = (TextView) findViewById(R.id.tv_name);
    	mTvDescription = (TextView) findViewById(R.id.tv_description);    	
    	mBackViewFollow.setOnClickListener(this);
	}
	
	public void update(IeetonUser user){
		mUser = user;
		if (user == null){
			return;
		}
		mUid = user.getUid();
		mTvName.setText(user.getName());
		mTvDescription.setText(Html.fromHtml(user.getDescription()));
		AsyncBitmapLoader.getInstance().loadBitmap(mContext, mUid, 
				NetEngine.getImageUrl(user.getAvatar()), mAvatar, null);
		mBackViewFollow.setText(getResources().getString(R.string.unattend));
	}

	@Override
	public void onClick(View v) {
		if (v == mBackViewFollow){
			if (mUser == null){
				return;
			}
			if (!mIsTaskFree){
				return;
			}
			if ("passport_5200".equals(mUid)){
				Utils.showToast(mContext, getResources().getString(R.string.cant_unfollow_xiaomishu), Toast.LENGTH_SHORT);
				return;
			}else if ("passport_6000".equals(mUid)){
				Utils.showToast(mContext, getResources().getString(R.string.cant_unfollow_dadajiankang), Toast.LENGTH_SHORT);
				return;
			}
			mTask = new OperationTask();
			try {
				mTask.execute(UNFOLLOW);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}			
		}
	}
	
	private void setFollowInfo(int status){
		if (mUser == null){
			return;
		}
//		if (mUser.getUserType() == UserInfo.ACCOUNT_DOCTOR){
//			Doctor doctor = mUser.getDoctor();
//			doctor.setIsFollowedStatus(status);
//		}else{
//			Patient patient = mUser.getPatient();
//			patient.setIsFollowedStatus(status);
//		}
	}
	
	class OperationTask extends AsyncTask<Integer, Void, String>{
		private int oid;
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			oid = params[0];
			String result = "";

			if (oid == FOLLOW){
				try {
					result = NetEngine.getInstance(mContext).followUser(mUid);
				} catch (PediatricsIOException e) {
					mThr = e;
					e.printStackTrace();
				} catch (PediatricsParseException e) {
					mThr = e;
					e.printStackTrace();
				} catch (PediatricsApiException e) {
					mThr = e;
					e.printStackTrace();
				}
			}else if (oid == UNFOLLOW){
				try {
					result = NetEngine.getInstance(mContext).unFollowUser(mUid);
				} catch (PediatricsIOException e) {
					mThr = e;
					e.printStackTrace();
				} catch (PediatricsParseException e) {
					mThr = e;
					e.printStackTrace();
				} catch (PediatricsApiException e) {
					mThr = e;
					e.printStackTrace();
				}
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			mIsTaskFree = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, mContext);
				}else{
					Utils.showToast(mContext, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			Intent intent = null;
			if (oid == FOLLOW){
				mBackViewFollow.setText(getResources().getString(R.string.unattend));
				intent = new Intent(Constants.ACTION_FOLLOW);
				setFollowInfo(1);
				Utils.showToast(mContext, getResources().getString(R.string.attend_succes), Toast.LENGTH_SHORT);
			}else if (oid == UNFOLLOW){
				mBackViewFollow.setText(getResources().getString(R.string.addattend));
				intent = new Intent(Constants.ACTION_UNFOLLOW);
				setFollowInfo(0);
				Utils.showToast(mContext, getResources().getString(R.string.unattend_succes), Toast.LENGTH_SHORT);
			}
			intent.putExtra(Constants.EXTRA_UID, mUid);
			mContext.sendBroadcast(intent);
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			super.onPreExecute();
		}
		
	}
}
