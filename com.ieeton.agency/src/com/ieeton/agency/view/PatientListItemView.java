package com.ieeton.agency.view;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.activity.PatientProfileActivity;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.Patient;
import com.ieeton.agency.models.Remark;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.AsyncBitmapLoader;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.utils.AsyncBitmapLoader.ImageCallBack;
import com.ieeton.agency.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PatientListItemView extends RelativeLayout implements View.OnClickListener{
	private class GetRemarkTask extends AsyncTask<Integer, Void, String>{
		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			int page = params[0];
			try {
				result = NetEngine.getInstance(mContext).
								getRemark(Utils.getPassport(mContext), 
										mPatient.getID(), 20, page);
			} catch (PediatricsIOException e) {
				e.printStackTrace();
			} catch (PediatricsParseException e) {
				e.printStackTrace();
			} catch (PediatricsApiException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			if (TextUtils.isEmpty(result)){
//				if(mThr != null){
//					Utils.handleErrorEvent(mThr, mContext);
//				}else{
//					Utils.showToast(mContext, R.string.get_remark_failed, Toast.LENGTH_SHORT);
//				}
				return;
			}
			processResult(result);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
	}
	
	private View mView;
	private Context mContext;
	private FollowDoctorTask mFollowDoctorTask;
	private UnFollowDoctorTask mUnFollowDoctorTask;
	private Patient mPatient;
	private boolean mIsTaskFree = true;
	private boolean mIsUnFollowTaskFree = true;
	private ImageView mAction;
	private	Button mUnFollowButton;
	private RelativeLayout mBackView;
	private ImageView mPortrait;
	private TextView mRemark;
	private List<Remark> mList;
	
	public PatientListItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	
	public PatientListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public PatientListItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	public void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.patient_list_item_view, this);
		
		mBackView = (RelativeLayout)findViewById(R.id.ieeton_back);

		mAction = (ImageView)mView.findViewById(R.id.iv_action_icon);
		mAction.setOnClickListener(this);

		mView.findViewById(R.id.ll_doctor_main).setOnClickListener(this);
		mUnFollowButton = (Button)findViewById(R.id.id_unfollow);
		mUnFollowButton.setOnClickListener(this);
		
		mPortrait = (ImageView)findViewById(R.id.iv_doctor_icon);
		mPortrait.setOnClickListener(this);
		
	}
	
	public void update(Patient patient){
		mPatient = patient;
		
		TextView name = (TextView)mView.findViewById(R.id.tv_doctor_name);
		name.setText(patient.getNick());
		
		mRemark = (TextView)mView.findViewById(R.id.tv_doctor_description);
				
		if(mPatient.getIsFollowedStatus() == 0){
			mAction.setImageResource(R.drawable.icon_follow);
			mUnFollowButton.setText(R.string.attend);
		}else{
			mAction.setImageResource(R.drawable.icon_followed);
			mUnFollowButton.setText(R.string.unattend);
		}
		Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(mContext, 
				mPatient.getID(), NetEngine.getImageUrl(mPatient.getPortraitUrl()), 
				"patient", new ImageCallBack() {
			@Override
			public void imageLoad(Bitmap bitmap, Object user) {
				if (bitmap !=null && !bitmap.isRecycled()){
					mPortrait.setImageBitmap(bitmap);
				}else {
					mPortrait.setImageResource(Utils.getDefaultPortraitId("patient", user));
				}
			}
		});
		if (b !=null && !b.isRecycled()){
			mPortrait.setImageBitmap(b);
		}else {
			mPortrait.setImageResource(Utils.getDefaultPortraitId("patient", null));
		}
//		if (mList == null){
			new GetRemarkTask().execute(1);
//		}
	}

	@Override
	public void onClick(View v) {
		if(v == mAction){			
			if(mPatient.getIsFollowedStatus() == 0){
				if(mIsTaskFree){
					try{
						mFollowDoctorTask = new FollowDoctorTask();
						mFollowDoctorTask.execute();
					}catch(RejectedExecutionException e){
						e.printStackTrace();
					}
				}
			}else{
				//表示当前已经关注该医生
				//这里不让用户可以方便取消关注，需要通过横向滑动才能显示“取消关注”菜单
			}
		}else if(v == mUnFollowButton){
			Log.v("sereinli", "unfollow,"+mIsUnFollowTaskFree);
			if(mPatient.getIsFollowedStatus() == 0){
				if(mIsTaskFree){
					try{
						mFollowDoctorTask = new FollowDoctorTask();
						mFollowDoctorTask.execute();
					}catch(RejectedExecutionException e){
						e.printStackTrace();
					}
				}
			}else{
				if(mIsUnFollowTaskFree){
					try{
						mUnFollowDoctorTask = new UnFollowDoctorTask();
						mUnFollowDoctorTask.execute();
					}catch(RejectedExecutionException e){
						e.printStackTrace();
					}
				}
			}

		}else if (v.getId() == R.id.ll_doctor_main || 
				v.getId() == R.id.iv_doctor_icon){
			Intent intent = new Intent(mContext, PatientProfileActivity.class);
			intent.putExtra(PatientProfileActivity.EXTRA_USERINFO, mPatient);
			mContext.startActivity(intent);
		}
	}
	

	class FollowDoctorTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsTaskFree = false;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = "";

				try {
					result = NetEngine.getInstance(mContext).followPatient(Utils.getPassport(mContext), mPatient.getID());
				} catch (PediatricsIOException e) {
					e.printStackTrace();
					mThr = e;
				} catch (PediatricsParseException e) {
					e.printStackTrace();
					mThr = e;
				} catch (PediatricsApiException e) {
					e.printStackTrace();
					mThr = e;
				}
				return result;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			
			if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, mContext);
				}else{
					Utils.showToast(mContext, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			mPatient.setIsFollowedStatus(1);
			mAction.setImageResource(R.drawable.icon_followed);
			mUnFollowButton.setText(R.string.unattend);
		}
		
	}

	class UnFollowDoctorTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mIsUnFollowTaskFree = false;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = "";

				try {
					result = NetEngine.getInstance(mContext).unFollowPatient(Utils.getPassport(mContext), mPatient.getID());
				} catch (PediatricsIOException e) {
					e.printStackTrace();
					mThr = e;
				} catch (PediatricsParseException e) {
					e.printStackTrace();
					mThr = e;
				} catch (PediatricsApiException e) {
					e.printStackTrace();
					mThr = e;
				}
				return result;

		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsUnFollowTaskFree = true;
			
			if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, mContext);
				}else{
					Utils.showToast(mContext, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			mPatient.setIsFollowedStatus(0);
			mAction.setImageResource(R.drawable.icon_follow);
			mUnFollowButton.setText(R.string.attend);
			
			Intent successIntent = new Intent("com.ieeton.agency.swipelist.close");
			mContext.sendBroadcast(successIntent);
			Intent unfollow = new Intent(Constants.ACTION_UNFOLLOW);
			unfollow.putExtra(Constants.OPERATION_UID, mPatient.getID());
			mContext.sendBroadcast(unfollow);
		}
	}
	
	private void processResult(String result){
		if (TextUtils.isEmpty(result)){
			return;
		}
		JSONObject obj;
		try {
			obj = new JSONObject(result);
			JSONObject data = obj.optJSONObject("messages").optJSONObject("data");
			JSONArray array = data.optJSONArray("remarks");
			if (array == null || array.length() == 0){
				return;
			}
			mList = new ArrayList<Remark>();
			for(int i=0; i<array.length(); i++){
				Remark item = new Remark(array.getJSONObject(i));
				mList.add(item);
			}
			Utils.logd("list.size:"+mList.size());
			if (mList.size()>0){
				mRemark.setText(((Remark) mList.get(0)).getContent());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
