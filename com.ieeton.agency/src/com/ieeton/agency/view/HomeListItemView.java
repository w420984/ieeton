package com.ieeton.agency.view;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.models.Patient;
import com.ieeton.agency.models.UserInfo;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.AsyncBitmapLoader;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.utils.AsyncBitmapLoader.ImageCallBack;
import com.ieeton.agency.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeListItemView extends RelativeLayout implements View.OnClickListener{
	private final int FOLLOW = 1;
	private final int UNFOLLOW = 2;
	
	private Context mContext;
	private Button mBackViewFollow;
	private ImageView mAvatar;
	private ImageView mFrontViewFollow;
	private TextView mTvName;
	private TextView mTvHospital;
	private TextView mTvSkilled;
	private TextView mTvDescription;
	private TextView mTvLeft;
	private TextView mTvMiddle;
	private TextView mTvRight;
	private ImageView mIvUserType;
	
	private boolean mIsFollowList;
	private UserInfo mUser;
	private OperationTask mTask;
	private boolean mIsTaskFree = true;
	private String mUid;
	
	public HomeListItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public HomeListItemView(Context context, AttributeSet attrs){
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public HomeListItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}
	
	private void initView(){
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.home_list_item_view, this);
		
    	mBackViewFollow = (Button) findViewById(R.id.id_unfollow);
    	mAvatar = (ImageView) findViewById(R.id.iv_avatar);
    	mFrontViewFollow = (ImageView) findViewById(R.id.followBtn);
    	mTvName = (TextView) findViewById(R.id.tv_name);
    	mTvHospital = (TextView) findViewById(R.id.tv_hospital);
    	mTvSkilled = (TextView) findViewById(R.id.tv_skilled);
    	mTvDescription = (TextView) findViewById(R.id.tv_description);
    	mTvLeft = (TextView) findViewById(R.id.tv_left_string);
    	mTvMiddle = (TextView) findViewById(R.id.tv_middle_string);
    	mTvRight = (TextView) findViewById(R.id.tv_right_string);
    	mIvUserType = (ImageView) findViewById(R.id.userTypeIcon);
    	
    	mBackViewFollow.setOnClickListener(this);
    	mFrontViewFollow.setOnClickListener(this);
	}
	
	public void update(UserInfo user, boolean isFollowList){
		mUser = user;
		mIsFollowList = isFollowList;
		if (user == null){
			return;
		}
		
		if (isFollowList){
			mIvUserType.setVisibility(View.VISIBLE);
			mFrontViewFollow.setVisibility(View.GONE);
			mBackViewFollow.setText(getResources().getString(R.string.unattend));
		}else{
			mIvUserType.setVisibility(View.GONE);
			mFrontViewFollow.setVisibility(View.VISIBLE);
		}
		
		if (user.getUserType() == UserInfo.ACCOUNT_DOCTOR){
			Doctor doctor = user.getDoctor();
			if (doctor == null){
				return;
			}
			mUid = doctor.getID();
			if (!isFollowList){
				//关注按钮
				if (doctor.getIsFollowedStatus() == 1){
					mFrontViewFollow.setImageResource(R.drawable.icon_followed_m);
					mBackViewFollow.setText(getResources().getString(R.string.unattend));
				}else{
					mFrontViewFollow.setImageResource(R.drawable.icon_follow_m);
					mBackViewFollow.setText(getResources().getString(R.string.addattend));
				}
			}
			
			//头像
			Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(mContext, 
					doctor.getID(), NetEngine.getImageUrl(doctor.getPortraitUrl()), 
					"doctor", new ImageCallBack() {
				@Override
				public void imageLoad(Bitmap bitmap, Object user) {
					if (bitmap !=null && !bitmap.isRecycled()){
						mAvatar.setImageBitmap(bitmap);
					}else {
						mAvatar.setImageResource(Utils.getDefaultPortraitId("doctor", user));
					}
				}
			});
			if (b !=null && !b.isRecycled()){
				mAvatar.setImageBitmap(b);
			}else {
				mAvatar.setImageResource(Utils.getDefaultPortraitId("doctor", null));
			}
			
			if (NetEngine.getFeedbackId().equals(mUid)){
				mTvLeft.setVisibility(View.GONE);
				mTvMiddle.setVisibility(View.GONE);
				mTvRight.setVisibility(View.GONE);
			}else{
				mTvLeft.setVisibility(View.VISIBLE);
				mTvMiddle.setVisibility(View.VISIBLE);
				mTvRight.setVisibility(View.VISIBLE);
				mTvLeft.setText(getResources().getString(R.string.fans) + doctor.getFansNum());
				mTvMiddle.setText(getResources().getString(R.string.like) + doctor.getPositiveNum());
			}

			if (Doctor.DOCTOR_PEOPLE.equals(doctor.getDoctorType())){	//医生
				mTvName.setText(doctor.getDoctorName() + "  " + doctor.getTitleName());
				
				mTvHospital.setVisibility(View.VISIBLE);
				mTvSkilled.setVisibility(View.VISIBLE);
				mTvDescription.setVisibility(View.GONE);
				mTvHospital.setText(doctor.getHospitalName() + "  " + doctor.getDepartment());
				mTvSkilled.setText(getResources().getString(R.string.expert)+doctor.getSkillDescription());
				
				mTvRight.setText(getResources().getString(R.string.diagnosis) + doctor.getPatientNum());
				
				mIvUserType.setImageResource(R.drawable.mylist_doc);
				
			}else{	//机构
				mTvName.setText(doctor.getDoctorName());
				
				mTvHospital.setVisibility(View.GONE);
				mTvSkilled.setVisibility(View.GONE);
				mTvDescription.setVisibility(View.VISIBLE);
				mTvDescription.setText(doctor.getSkillDescription());
				
				mTvRight.setText(getResources().getString(R.string.consultation) + doctor.getPatientNum());
				
				mIvUserType.setImageResource(R.drawable.mylist_institution);
			}
		}else{
			Patient patient = user.getPatient();
			if (patient == null){
				return;
			}
			mUid = patient.getID();
			if (!isFollowList){
				//关注按钮
				if (patient.getIsFollowedStatus() == 1){
					mFrontViewFollow.setImageResource(R.drawable.icon_followed_m);
					mBackViewFollow.setText(getResources().getString(R.string.unattend));
				}else{
					mFrontViewFollow.setImageResource(R.drawable.icon_follow_m);
					mBackViewFollow.setText(getResources().getString(R.string.addattend));
				}
			}
						
			//头像
			Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(mContext, 
					patient.getID(), NetEngine.getImageUrl(patient.getPortraitUrl()), 
					"patient", new ImageCallBack() {
				@Override
				public void imageLoad(Bitmap bitmap, Object user) {
					if (bitmap !=null && !bitmap.isRecycled()){
						mAvatar.setImageBitmap(bitmap);
					}else {
						mAvatar.setImageResource(Utils.getDefaultPortraitId("patient", user));
					}
				}
			});
			if (b !=null && !b.isRecycled()){
				mAvatar.setImageBitmap(b);
			}else {
				mAvatar.setImageResource(Utils.getDefaultPortraitId("patient", null));
			}
			
			mTvName.setText(patient.getNick());
			mTvHospital.setVisibility(View.GONE);
			mTvSkilled.setVisibility(View.GONE);
			mTvDescription.setVisibility(View.VISIBLE);
			mTvDescription.setText(patient.getDescription());
			mTvRight.setVisibility(View.GONE);
			if (patient.getArticleCount() > 0){
				mIvUserType.setImageResource(R.drawable.mylist_famous);
				mTvLeft.setVisibility(View.VISIBLE);
				mTvMiddle.setVisibility(View.VISIBLE);
				mTvLeft.setText(getResources().getString(R.string.fans) + patient.getFollowCount());
				mTvMiddle.setText(getResources().getString(R.string.article) + patient.getArticleCount());
			}else{
				mIvUserType.setImageResource(R.drawable.mylist_male);
				mTvLeft.setVisibility(View.GONE);
				mTvMiddle.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mBackViewFollow || v == mFrontViewFollow){
			if (TextUtils.isEmpty(Utils.getPassport(mContext))){
				Intent successIntent = new Intent(Constants.NEED_RELOGIN_ACTION);
				mContext.sendBroadcast(successIntent);
				return;
			}
			if (mUser == null){
				return;
			}
			if (!mIsTaskFree){
				return;
			}
			if (NetEngine.getFeedbackId().equals(mUid)){
				Utils.showToast(mContext, getResources().getString(R.string.cant_unfollow_xiaomishu), Toast.LENGTH_SHORT);
				return;
			}
			int oid;
			if (mIsFollowList){
				oid = UNFOLLOW;
			}else{
				if (mUser.getUserType() == UserInfo.ACCOUNT_DOCTOR){
					Doctor doctor = mUser.getDoctor();
					if (doctor == null){
						return;
					}
					if (doctor.getIsFollowedStatus() == 1){
						oid = UNFOLLOW;
					}else{
						oid = FOLLOW;
					}
				}else{
					Patient patient = mUser.getPatient();
					if (patient == null){
						return;
					}
					if (patient.getIsFollowedStatus() == 1){
						oid = UNFOLLOW;
					}else{
						oid = FOLLOW;
					}
				}
			}
			mTask = new OperationTask();
			try {
				mTask.execute(oid);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setFollowInfo(int status){
		if (mUser == null){
			return;
		}
		if (mUser.getUserType() == UserInfo.ACCOUNT_DOCTOR){
			Doctor doctor = mUser.getDoctor();
			doctor.setIsFollowedStatus(status);
		}else{
			Patient patient = mUser.getPatient();
			patient.setIsFollowedStatus(status);
		}
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
			JSONObject obj;
			try {
				obj = new JSONObject(result);
				JSONObject data = obj.optJSONObject("messages")
						.optJSONObject("data");
				String uid = data.optString("userId");
				Intent intent = null;
				if (oid == FOLLOW){
					mFrontViewFollow.setImageResource(R.drawable.icon_followed_m);
					mBackViewFollow.setText(getResources().getString(R.string.unattend));
					intent = new Intent(Constants.ACTION_FOLLOW);
					setFollowInfo(1);
					Utils.showToast(mContext, getResources().getString(R.string.attend_succes), Toast.LENGTH_SHORT);
				}else if (oid == UNFOLLOW){
					mFrontViewFollow.setImageResource(R.drawable.icon_follow_m);
					mBackViewFollow.setText(getResources().getString(R.string.addattend));
					intent = new Intent(Constants.ACTION_UNFOLLOW);
					setFollowInfo(0);
					Utils.showToast(mContext, getResources().getString(R.string.unattend_succes), Toast.LENGTH_SHORT);
				}
				intent.putExtra(Constants.EXTRA_UID, uid);
				mContext.sendBroadcast(intent);
			}catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			super.onPreExecute();
		}
		
	}
}
