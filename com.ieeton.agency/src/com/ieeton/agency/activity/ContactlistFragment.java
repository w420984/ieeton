/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ieeton.agency.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.ieeton.agency.activity.UserProfileActivity;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.ChatUser;
import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.models.Patient;
import com.ieeton.agency.models.UserInfo;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.HomeListItemView;
import com.ieeton.agency.view.PullDownView;
import com.ieeton.agency.view.PullDownView.UpdateHandle;
import com.ieeton.agency.R;
import com.umeng.analytics.MobclickAgent;

/**
 * 联系人列表页
 * 
 */
@SuppressLint("ResourceAsColor")
public class ContactlistFragment extends Fragment implements UpdateHandle, OnScrollListener{
	class HomeListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mList != null && !mList.isEmpty()){
				return mList.size();
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
			HomeListItemView view ;
			if (convertView == null){
				view = new HomeListItemView(getActivity());
			}else{
				view = (HomeListItemView) convertView;
			}
			if (mList != null && !mList.isEmpty() && position < mList.size()){
				view.update(mList.get(position), true);
			}
			return view;
		}
		
	}
	
	class FechDataTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			int page = params[0];
			try {
				result = NetEngine.getInstance(getActivity())
							.getFollowedUser(Utils.getPassport(getActivity()), 
									20, page);
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
			//dismissProgress();
			if (mLoadMode == LOAD_MORE){
				mPageNum--;
			}
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			//dismissProgress();
			mPullDownView.endUpdate(null);
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getActivity());
				}else{
					Utils.showToast(getActivity(), R.string.no_data, Toast.LENGTH_SHORT);
				}
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
				}
				return;
			}
			processResult(result);
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			//showProgress();
			super.onPreExecute();
		}
		
	}
	
	private final int LOAD_REFRESH = 0;
	private final int LOAD_MORE = 1;
	
	public static String EXTRA_MODE = "extra_mode";
	public static final int MODE_PARENTAL = 1;	//育儿保健
	public static final int MODE_MAMI = 2;		//美丽妈妈
	public static final int MODE_SECURITY = 3;	//关怀保障
	public static final int MODE_FOLLOWED = 4;	//我的关注

	private List<UserInfo> mList;
	private PullDownView mPullDownView;
	private SwipeListView mListView;
	private HomeListAdapter mAdapter;
	
	private OperationReceiver mReceiver;
	private CustomToast mProgressDialog;
	private FechDataTask mTask;
	private int mPageNum = 1;
	private boolean mIsTaskFree = true;
	private int mLoadMode = LOAD_REFRESH;
	private View mView;
		
	class OperationReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Constants.ACTION_FOLLOW.equals(action)){
				mListView.closeOpenedItems();				
			}else if (Constants.ACTION_UNFOLLOW.equals(action)){
				String uid = intent.getExtras().getString("uid");
				if (TextUtils.isEmpty(uid)){
					return;
				}
				deleteItem(uid);
				mListView.closeOpenedItems();
			}
		}
		
	}

	private void deleteItem(String uid){
		if (mList == null || mList.isEmpty()){
			return;
		}
		for(UserInfo info : mList){
			if (info.getUserType() == UserInfo.ACCOUNT_DOCTOR){
				Doctor doctor = info.getDoctor();
				if (doctor.getID().equals(uid)){
					mList.remove(info);
					mAdapter.notifyDataSetChanged();
					mListView.closeOpenedItems();
					break;
				}
			}else{
				Patient patient = info.getPatient();
				if (patient.getID().equals(uid)){
					mList.remove(info);
					mAdapter.notifyDataSetChanged();
					mListView.closeOpenedItems();
					break;
				}
			}
		}
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_contact_list, container, false);

		initView();
		return mView;
	}

	@Override
	public void onPause() {
		MobclickAgent.onPageEnd("ContactlistFragment"); 
		super.onPause();
	}

	@Override
	public void onResume() {
		MobclickAgent.onPageStart("ContactlistFragment"); 
		super.onResume();
	}


	private void initView() {		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_FOLLOW);
		filter.addAction(Constants.ACTION_UNFOLLOW);
		mReceiver = new OperationReceiver();
		getActivity().registerReceiver(mReceiver, filter);
		
		mPullDownView = (PullDownView) mView.findViewById(R.id.pulldown_view);
		mPullDownView.setUpdateHandle(this);
		
		mListView = (SwipeListView) mView.findViewById(R.id.list);
		mAdapter = new HomeListAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this);
		mListView.setSwipeListViewListener(new BaseSwipeListViewListener(){

			@Override
			public void onClickFrontView(int position) {
				if (mList != null && position < mList.size()){
					UserInfo user = mList.get(position);
					if (user == null){
						return;
					}
					if (user.getUserType() == UserInfo.ACCOUNT_DOCTOR){
						Doctor doctor = user.getDoctor();
						if (doctor == null){
							return;
						}
						Intent intent = new Intent(getActivity(), UserProfileActivity.class);
						intent.putExtra(UserProfileActivity.EXTRA_USERINFO, new ChatUser(doctor));
						startActivity(intent);
					}else{
						Patient patient = user.getPatient();
						if (patient == null){
							return;
						}
						Intent intent = new Intent(getActivity(), PatientProfileActivity.class);
						intent.putExtra(PatientProfileActivity.EXTRA_USERID, patient.getID());
						startActivity(intent);
					}
				}
			}
			
		});
		
		if (mList == null || mList.isEmpty()){
			refreshData(LOAD_REFRESH);
		}
	}

	private void refreshData(int mode){
		mLoadMode = mode;
		if (!mIsTaskFree){
			return;
		}
		if (mode == LOAD_REFRESH){
			mPageNum = 1;
			mPullDownView.update();
		}else{
			mPageNum += 1;
			mPullDownView.updateWithoutOffset();
		}
		mTask = new FechDataTask();
		try {
			mTask.execute(mPageNum);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		//dismissProgress();
		if (mReceiver != null){
			getActivity().unregisterReceiver(mReceiver);
		}
		super.onDestroy();
	}
	
	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, getActivity());
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}

	@Override
	public void onUpdate() {
		refreshData(LOAD_REFRESH);
	}

	private boolean retrievable;
	private boolean isEnd;
	protected int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
			isEnd = true;
		} else {
			isEnd = false;
		}
		if (firstVisibleItem == 0) {
			mScrollState = SCROLL_STATE_IDLE;
		}


		if (firstVisibleItem
						+ visibleItemCount >= totalItemCount - 1) 
		{
			retrievable = true;
		} else {
			retrievable = false;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (isEnd) {
			mScrollState = SCROLL_STATE_IDLE;
		} else {
			mScrollState = scrollState;
		}
		if (scrollState == SCROLL_STATE_IDLE && retrievable) {
			retrievable = false;
			refreshData(LOAD_MORE);
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
			JSONArray array = data.optJSONArray("users");
			if (array == null || array.length() == 0){
				if (mLoadMode == LOAD_MORE){
					mPageNum--;
					Utils.showToast(getActivity(), R.string.no_more_data, Toast.LENGTH_SHORT);
				}else{
					//showEmpty(true);
				}
				return;
			}
			if (mList == null){
				mList = new ArrayList<UserInfo>();
			}
			List<UserInfo> list = new ArrayList<UserInfo>();
			for(int i=0; i<array.length(); i++){
				UserInfo item;
				if ("doctor".equals(array.optJSONObject(i).optString("type"))){
					Doctor doctor = new Doctor(getActivity(), array.optJSONObject(i));
					item = new UserInfo(doctor);
				}else{
					Patient patient = new Patient(getActivity(), array.optJSONObject(i));
					item = new UserInfo(patient);
				}
				list.add(item);
			}
			if (mLoadMode == LOAD_REFRESH){
				mList.clear();
			}
			mList.addAll(list);
			mAdapter.notifyDataSetChanged();
			if (mList.size()>0){
				//showEmpty(false);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
