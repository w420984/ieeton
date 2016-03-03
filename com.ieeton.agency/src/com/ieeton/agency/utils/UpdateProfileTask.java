package com.ieeton.agency.utils;

import com.ieeton.agency.activity.PatientProfileActivity;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.R;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

public abstract class UpdateProfileTask extends AsyncTask<Void, Void, String> {
	public final static int MODE_UPDATE = 0;
	public final static int MODE_PERFECT = 1;
	private Doctor mDoctor;
	
	private Context mContext;
	private Throwable mThr;
	private int mMode;
	
	
	public UpdateProfileTask(Context context, Doctor doctor){
		mContext = context;
		mDoctor = doctor;
		mMode = MODE_UPDATE;
	}
	
	public UpdateProfileTask(Context context, Doctor doctor, int type){
		mContext = context;
		mDoctor = doctor;
		mMode = type;
	}

	@Override
	protected String doInBackground(Void... params) {
		String result = "";
		try {
			if (mMode == MODE_UPDATE){
				result = NetEngine.getInstance(mContext)
						.updateDoctorInfo(Utils.getPassport(mContext), mDoctor);
			}else{
				result = NetEngine.getInstance(mContext)
						.perfectDoctorInfo(Utils.getPassport(mContext), mDoctor);
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
		updateCancel();
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		if (TextUtils.isEmpty(result)){
			if(mThr != null){
				Utils.handleErrorEvent(mThr, mContext);
			}else{
				Utils.showToast(mContext, R.string.update_profile_failed, Toast.LENGTH_SHORT);
			}
			updateEnd(false);
			return;
		}
		updateEnd(true);
	}

	@Override
	protected void onPreExecute() {
		updateBegin();
		super.onPreExecute();
	}

	abstract protected void updateBegin();
	abstract protected void updateEnd(boolean success);
	abstract protected void updateCancel();
}
