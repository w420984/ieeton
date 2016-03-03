package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.DoctorListItemView;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

public class DoctorsFragment extends Fragment {
	private View mView;
	private GridView mGridView;
	private PicAdapter mAdapter;
	
	private InstitutionActivity mActivity;
	private String mUserId;
	private List<IeetonUser> mDoctorList;
	private boolean mIsTaskFree = true;
	private FetchDataTask mTask;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_doctors, container, false);
		mActivity = (InstitutionActivity) getActivity();
		mUserId = mActivity.mUserId;
		initView();
		return mView;
	}

	private void initView(){
		mGridView = (GridView) mView.findViewById(R.id.gridView);
		mAdapter = new PicAdapter();
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long arg3) {
				Intent intent = new Intent(mActivity, DoctorProfileActivity.class);
				intent.putExtra(Constants.EXTRA_UID, mDoctorList.get(position).getUid());
				intent.putExtra(Constants.EXTRA_USER, mDoctorList.get(position));
				startActivity(intent);
			}
		});
		
		mTask = new FetchDataTask();
		mTask.execute();
	}
	
	@Override
	public void onPause() {
		MobclickAgent.onPageEnd("DoctorsFragment"); 
		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		MobclickAgent.onPageStart("DoctorsFragment"); 
		super.onResume();
	}

	private class PicAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mDoctorList != null){
				return mDoctorList.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			DoctorListItemView view;
			if (convertView == null){
				view = new DoctorListItemView(mActivity);
			}else{
				view = (DoctorListItemView) convertView;
			}
			if (mDoctorList != null && position<mDoctorList.size()){
				view.update(mDoctorList.get(position));
			}
			return view;
		}
		
	}
	
	private class FetchDataTask extends AsyncTask<Void, Void, String>{
	    private Throwable mThr;
	    //private int page;

		@Override
		protected String doInBackground(Void... params) {
			//page = params[0];
			String result = "";
				try {
					result = NetEngine.getInstance(mActivity).
							getInstitutionDoctor(mUserId);
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
			//dismissProgress();
			mIsTaskFree = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			//dismissProgress();
			mIsTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, mActivity);
				}else{
					Utils.showToast(mActivity, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			try {
				JSONObject obj = new JSONObject(result);
				JSONArray array = obj.optJSONArray("user");
				if (array != null && array.length()>0){
					if (mDoctorList == null){
						mDoctorList = new ArrayList<IeetonUser>();
					}
					for(int i=0; i<array.length(); i++){
						IeetonUser item = new IeetonUser(getActivity(), array.optJSONObject(i));
						mDoctorList.add(item);
					}
					mAdapter.notifyDataSetChanged();
					if (mDoctorList.isEmpty()){
						showEmpty(true);
					}else{
						showEmpty(false);
					}
				}else{
					showEmpty(true);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			super.onPreExecute();
		}
		
	}
	
	private void showEmpty(boolean isEmpty){
		if (isEmpty){
			mGridView.setVisibility(View.GONE);
			mView.findViewById(R.id.iv_empty).setVisibility(View.VISIBLE);
		}else{
			mGridView.setVisibility(View.VISIBLE);
			mView.findViewById(R.id.iv_empty).setVisibility(View.GONE);
		}
	}
}
