package com.ieeton.agency.activity;

import java.util.concurrent.RejectedExecutionException;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.ChatUser;
import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.LoadPictureTask;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CallCheckDialog;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.RoundedImageView;
import com.ieeton.agency.R;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DoctorProfileActivity extends TemplateActivity {
	private class OperationTask extends AsyncTask<Void, Void, String>{
        private Throwable mThr;
        private final int mMode;

        public OperationTask(int mode){
        	mMode = mode;
        }
        
		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			switch(mMode){
			case DoctorProfileActivity.MODE_LIKE:
				try {
					result = NetEngine.getInstance(DoctorProfileActivity.this)
									.likeDoctor(Utils.getPassport(DoctorProfileActivity.this), 
												mDoctorInfo.getID());
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
				break;
			case DoctorProfileActivity.MODE_UNLIKE:
				try {
					result = NetEngine.getInstance(DoctorProfileActivity.this)
									.unLikeDoctor(Utils.getPassport(DoctorProfileActivity.this), 
												mDoctorInfo.getID());
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
				break;
			case DoctorProfileActivity.MODE_FOLLOW:
				try {
					result = NetEngine.getInstance(DoctorProfileActivity.this)
									.followDoctor(Utils.getPassport(DoctorProfileActivity.this), 
												mDoctorInfo.getID());
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
				break;
			case DoctorProfileActivity.MODE_UNFOLLOW:
				try {
					result = NetEngine.getInstance(DoctorProfileActivity.this)
									.unfollowDoctor(Utils.getPassport(DoctorProfileActivity.this), 
												mDoctorInfo.getID());
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
				break;
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			mIsTaskFree = true;
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			dismissProgress();
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, DoctorProfileActivity.this);
				}else{
					Utils.showToast(DoctorProfileActivity.this, R.string.operation_failed, Toast.LENGTH_SHORT);
				}
				return;
			}
			switch(mMode){
			case MODE_LIKE:
				setLikeImage(true);
				break;
			case MODE_UNLIKE:
				setLikeImage(false);
				break;
			case MODE_FOLLOW:
				setFollowImage(true);
				break;
			case MODE_UNFOLLOW:
				setFollowImage(false);
				break;
			}
			Utils.showToast(DoctorProfileActivity.this, R.string.operation_succes, Toast.LENGTH_SHORT);
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			showProgress();
			super.onPreExecute();
		}
		
	}
	

	public static final String EXTRA_DOCTOR = "doctor";
	public static final int MODE_LIKE = 1;
	public static final int MODE_UNLIKE = 2;
	public static final int MODE_FOLLOW = 3;
	public static final int MODE_UNFOLLOW = 4;
	
	private ImageView mIvLike;
	private ImageView mIvFollow;
	private RoundedImageView mIvHeader;
	private TextView mTvNick;
	private TextView mTvJob;
	private TextView mTvHospital;
	private TextView mTvExpert;
	private TextView mTvFansNum;
	private TextView mTvLikedNum;
	private TextView mTvDiagnosisNum;
	private Button mBtnGraphic;
	private Button mBtnCall;
	
	private Doctor mDoctorInfo;
	private CustomToast mProgressDialog;
	private OperationTask mTask;
	private boolean mIsLiked;
	private boolean mIsFollowed;
	private boolean mIsTaskFree = true;
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_doctor_profile);
		setTitleBar(getString(R.string.back), getString(R.string.doctor_detail), null);
		
		mDoctorInfo = (Doctor) getIntent().getExtras().getSerializable(EXTRA_DOCTOR);
		if (mDoctorInfo == null){
			finish();
		}
		initView();
	}

	private void initView(){
		mIvLike = (ImageView)findViewById(R.id.btn_like);
		setLikeImage(mDoctorInfo.getIsLikedStatus() == 1);
		mIvLike.setOnClickListener(this);

		mIvFollow = (ImageView)findViewById(R.id.btn_follow);
		setFollowImage(mDoctorInfo.getIsFollowedStatus() == 1);
		mIvFollow.setOnClickListener(this);

		mIvHeader = (RoundedImageView)findViewById(R.id.head_photo);
		if (!TextUtils.isEmpty(mDoctorInfo.getPortraitUrl())){
			LoadPictureTask task =  new LoadPictureTask();
			task.execute(this, 
					NetEngine.getImageUrl(mDoctorInfo.getPortraitUrl()),
					mIvHeader);
		}
		
		mTvNick = (TextView)findViewById(R.id.nick);
		mTvNick.setText(mDoctorInfo.getDoctorName());
		
		mTvJob = (TextView)findViewById(R.id.job);
		String job = mDoctorInfo.getDepartment() + " | "
					+ mDoctorInfo.getTitleName();
		mTvJob.setText(job);
		
		mTvHospital = (TextView)findViewById(R.id.hospital);
		mTvHospital.setText(mDoctorInfo.getHospitalName());
		
		
		mTvExpert = (TextView)findViewById(R.id.expert);
		String expert = getString(R.string.expert) + mDoctorInfo.getSkillDescription();
		mTvExpert.setText(expert);
		
		mTvFansNum = (TextView)findViewById(R.id.tv_fans_num);
		mTvFansNum.setText(mDoctorInfo.getFansNum()+"");
		
		mTvLikedNum = (TextView)findViewById(R.id.tv_like_num);
		mTvLikedNum.setText(mDoctorInfo.getPositiveNum()+"");
		
		mTvDiagnosisNum = (TextView)findViewById(R.id.tv_diagnosis_num);
		mTvDiagnosisNum.setText(mDoctorInfo.getPatientNum()+"");

		mBtnGraphic = (Button)findViewById(R.id.btn_graphic);
		mBtnGraphic.setOnClickListener(this);

		mBtnCall = (Button)findViewById(R.id.btn_call);
		mBtnCall.setOnClickListener(this);
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		dismissProgress();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void setLikeImage(boolean isLiked){
		mIsLiked = isLiked;
		if (isLiked){
			mIvLike.setImageResource(R.drawable.docprofile_liked);
		}else{
			mIvLike.setImageResource(R.drawable.docprofile_like);
		}
	}
	
	private void setFollowImage(boolean isFollowed){
		mIsFollowed = isFollowed;
		if (isFollowed){
			mIvFollow.setImageResource(R.drawable.docprofile_followed);
		}else{
			mIvFollow.setImageResource(R.drawable.docprofile_follow);
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == mIvLike || v == mIvFollow || v == mBtnGraphic
				|| v == mBtnCall){
			if (TextUtils.isEmpty(Utils.getMyUid(this))
					|| TextUtils.isEmpty(Utils.getPassport(this))){
				startActivity(new Intent(this, LoginActivity.class));
				finish();
				return;
			}
		}
		
		if (v == mIvLike && mIsTaskFree){
			if (mIsLiked){
				mTask = new OperationTask(MODE_UNLIKE);
			}else{
				mTask = new OperationTask(MODE_LIKE);
			}
			try {
				mTask.execute();
			} catch (RejectedExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (v == mIvFollow && mIsTaskFree){
			if (mIsFollowed){
				mTask = new OperationTask(MODE_UNFOLLOW);
			}else{
				mTask = new OperationTask(MODE_FOLLOW);
			}
			try {
				mTask.execute();
			} catch (RejectedExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (v == mBtnGraphic){
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra(ChatActivity.EXTRA_USERID, mDoctorInfo.getID());
			intent.putExtra(ChatActivity.EXTRA_USERINFO, new ChatUser(mDoctorInfo));
			startActivity(intent);
			
		}else if (v == mBtnCall){
			CallCheckDialog dialog = new CallCheckDialog(this, R.style.NoTitleDialog, mDoctorInfo);
	        dialog.show();
		}
		super.onClick(v);
	}

	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, this);
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}
}
