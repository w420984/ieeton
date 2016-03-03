package com.ieeton.user.activity;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.RoundedImageView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class DoctorProfileActivity extends TemplateActivity {
	private final int OPERATE_ATTEND = 1;
	private final int OPERATE_UNATTEND = 2;
	
	private RoundedImageView mIvHeader;
	private TextView mTvName;
	private ViewGroup mVgCall;
	private ImageView mIvCall;
	private TextView mTvCall;
	private ViewGroup mVgMessage;
	private ImageView mIvMessage;
	private TextView mTvMessage;
	private ViewGroup mVgAttend;
	private ImageView mIvAttend;
	private TextView mTvAttend;
	private TextView mTvTime;
	private ViewGroup mVgTime;
	private TextView mTvIntroduce;
	
	private VideoView mVideoView;
	private ImageView mIvPlay;
	private ViewGroup mVgVideo;	
	private ImageView mIvVideo;
	
	private String mUserId;
	private IeetonUser mUser;
	private boolean mIsTaskFree = true;
	private FetchDataTask mTask;
	private OperationTask mOperationTask;
	private Dialog mCallDialog;

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

		Intent intent = getIntent();
		mUser = (IeetonUser) intent.getSerializableExtra(Constants.EXTRA_USER);
		mUserId = intent.getStringExtra(Constants.EXTRA_UID);
		
		initView();
		updateUI();
//		if (mUser == null){
			mTask = new FetchDataTask();
			mTask.execute();
//		}
	}

	private void initView(){
		mIvHeader = (RoundedImageView) findViewById(R.id.iv_header);
		mTvName = (TextView) findViewById(R.id.tv_name);
		mTvTime = (TextView) findViewById(R.id.tv_time);
		mTvIntroduce = (TextView) findViewById(R.id.tv_introduce);
		mVgTime = (ViewGroup) findViewById(R.id.ll_time);

		mIvCall = (ImageView) findViewById(R.id.iv_call);
		mTvCall = (TextView) findViewById(R.id.tv_call);
		mVgCall = (ViewGroup) findViewById(R.id.rl_call);
		mVgCall.setOnClickListener(this);
		
		mIvMessage = (ImageView) findViewById(R.id.iv_message);
		mTvMessage = (TextView) findViewById(R.id.tv_message);
		mVgMessage = (ViewGroup) findViewById(R.id.rl_message);
		mVgMessage.setOnClickListener(this);
		
		mIvAttend = (ImageView) findViewById(R.id.iv_attend);
		mTvAttend = (TextView) findViewById(R.id.tv_attend);
		mVgAttend = (ViewGroup) findViewById(R.id.rl_attend);
		mVgAttend.setOnClickListener(this);
		
		mVideoView = (VideoView) findViewById(R.id.video);
		mIvPlay = (ImageView) findViewById(R.id.iv_play);
		mIvPlay.setOnClickListener(this);
		mVgVideo = (ViewGroup) findViewById(R.id.fl_video);
		mIvVideo = (ImageView) findViewById(R.id.iv_video);
	}
	
	private void updateUI(){
		if (mUser == null){
			return;
		}
		AsyncBitmapLoader.getInstance().loadBitmap(this, mIvHeader, 
				NetEngine.getImageUrl(mUser.getAvatar()));
		mTvName.setText(mUser.getName());
		mTvIntroduce.setText(Html.fromHtml(mUser.getDescription()));
		if (TextUtils.isEmpty(mUser.getClinicaltile())){
			mVgTime.setVisibility(View.GONE);
		}else{
			mVgTime.setVisibility(View.VISIBLE);
		}
		mTvTime.setText(mUser.getClinicaltile());
		
//		if (mUser.getSwitchMobile() == 1){
			mIvCall.setImageResource(R.drawable.doctor_phone);
			mTvCall.setTextColor(getResources().getColor(R.color.color_white));
//		}else{
//			mIvCall.setImageResource(R.drawable.product_phone_n);
//			mTvCall.setTextColor(getResources().getColor(R.color.color_gray));
//		}
		
//		if (mUser.getSwichMessage() == 1){
			mIvMessage.setImageResource(R.drawable.doctor_message_s);
			mTvMessage.setTextColor(getResources().getColor(R.color.color_white));
//		}else{
//			mIvMessage.setImageResource(R.drawable.product_message_n);
//			mTvMessage.setTextColor(getResources().getColor(R.color.color_gray));
//		}
		
		showAttendGroup();		
		showVideo();
	}
	
	private void showAttendGroup(){
		if (mUser.getIsfollow() == 1){
			mIvAttend.setImageResource(R.drawable.doctor_attention_ed);
			mTvAttend.setText(getString(R.string.unattend));
		}else{
			mIvAttend.setImageResource(R.drawable.doctor_attention);
			mTvAttend.setText(getString(R.string.attend));
		}
	}
	
	private void showVideo(){
		if (TextUtils.isEmpty(mUser.getVideoUrl())){
			mVgVideo.setVisibility(View.GONE);
		}else{
			mVgVideo.setVisibility(View.VISIBLE);
//			MediaController controller = new MediaController(this);
//			controller.setMediaPlayer(mVideoView);
//			mVideoView.setMediaController(controller);
//			mVideoView.setVideoPath(mUser.getVideoUrl());
//			mVideoView.seekTo(5000);
			Bitmap bitmap = null;
			// 获取视频的缩略图
			bitmap = Utils.createVideoThumbnail(mUser.getVideoUrl(), getResources().getDimensionPixelSize(R.dimen.video_height), 
					getResources().getDimensionPixelSize(R.dimen.video_height));
			//System.out.println("bitmap"+bitmap);
			if (bitmap != null){
				mIvVideo.setImageBitmap(bitmap);
			}
		}
	}
	
	private void playVideo(){
		if (mUser == null){
			return;
		}
		mVideoView.seekTo(0);
		mVideoView.start();
		mIvPlay.setVisibility(View.GONE);
	}
	
	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		if (mOperationTask != null && mOperationTask.getStatus() == AsyncTask.Status.RUNNING){
			mOperationTask.cancel(true);
		}
		if (mCallDialog != null && mCallDialog.isShowing()){
			mCallDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mVgCall || v == mVgMessage || v == mVgAttend){
			//游客先登录
			if (Utils.getMyType(this) == 5){
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				return;
			}
			if (mUser == null){
				return;
			}			
		}
		if (v == mVgCall){
			if (mUser.getSwitchMobile() == 0 || 
					TextUtils.isEmpty(mUser.getIvrMobile()) && TextUtils.isEmpty(mUser.getMobile())){
				Utils.showToast(this, R.string.call_service_is_unavlable, Toast.LENGTH_SHORT);
				return;
			}
			String mobile = TextUtils.isEmpty(mUser.getIvrMobile()) ? mUser.getMobile() : mUser.getIvrMobile();
			mCallDialog = Utils.showCallDialog(this, mobile);
		}else if (v == mVgMessage){
			if (mUser == null){
				return;
			}
			if (mUser.getSwichMessage() == 0){
				Utils.showToast(this, R.string.chat_service_is_unavlable, Toast.LENGTH_SHORT);
				return;
			}
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra(Constants.EXTRA_UID, mUserId);
			intent.putExtra(Constants.EXTRA_USER, mUser);
			startActivity(intent);
		}else if (v == mVgAttend){
			if (!mIsTaskFree){
				return;
			}
			int action = mUser.getIsfollow() == 1 ? OPERATE_UNATTEND : OPERATE_ATTEND;
			mOperationTask = new OperationTask();
			try {
				mOperationTask.execute(action);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}else if (v == mIvPlay){
			if (mUser == null){
				return;
			}
			Intent intent = new Intent(this, VideoPlayerActivity.class);
			intent.putExtra(Constants.EXTRA_URL, mUser.getVideoUrl());
			startActivity(intent);
			//playVideo();
		}
		super.onClick(v);
	}
	
	private class FetchDataTask extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
				try {
					result = NetEngine.getInstance(DoctorProfileActivity.this).
								getUserInfo(mUserId);
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
			return result;
		}

		@Override
		protected void onCancelled() {
			dismissProgress();
			mIsTaskFree = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			dismissProgress();
			mIsTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, DoctorProfileActivity.this);
				}else{
					Utils.showToast(DoctorProfileActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			try {
				JSONObject obj = new JSONObject(result);
				mUser = new IeetonUser(DoctorProfileActivity.this, obj);
				updateUI();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			mIsTaskFree = false;
			super.onPreExecute();
		}		
	}
	
	private class OperationTask extends AsyncTask<Integer, Void, String>{
	    private Throwable mThr;
	    private int action;

		@Override
		protected String doInBackground(Integer... params) {
			action = params[0];
			String result = "";
			try {
				if (action == OPERATE_ATTEND){
					result = NetEngine.getInstance(DoctorProfileActivity.this).
								followUser(mUserId);
				}else if (action == OPERATE_UNATTEND){
					result = NetEngine.getInstance(DoctorProfileActivity.this).
							unFollowUser(mUserId);
				}
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
					Utils.showToast(DoctorProfileActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			if (action == OPERATE_ATTEND){
				Utils.showToast(DoctorProfileActivity.this, R.string.attend_succes, Toast.LENGTH_SHORT);
				mUser.setIsfollow(1);
				Intent intent = new Intent(Constants.ACTION_FOLLOW);
				sendBroadcast(intent);
			}else if (action == OPERATE_UNATTEND){
				Utils.showToast(DoctorProfileActivity.this, R.string.unattend_succes, Toast.LENGTH_SHORT);
				mUser.setIsfollow(0);
				Intent intent = new Intent(Constants.ACTION_UNFOLLOW);
				sendBroadcast(intent);
			}
			showAttendGroup();
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			showProgress();
			super.onPreExecute();
		}
		
	}
}
