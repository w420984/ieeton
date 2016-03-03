package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.models.ThirdPartner;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.CustomToast;
import com.ieeton.user.view.RoundedImageView;

import android.app.Activity;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MyProfileFragment extends Fragment implements OnClickListener{
	private final int MODE_GETINFO = 1;
	private final int MODE_GETCOUNT = 2;
	
	private final int REQUEST_EDIT_INFO = 1001;
	private final int REQUEST_RECHARGE = 1002;
	
	private RoundedImageView mIvHeader;
	private TextView mTvRefresh;
	private TextView mTvName;
	private ImageView mIvSetting;
	private TextView mTvProductsNum;
	private ViewGroup mVgProducts;
	private TextView mTvInstitutionNum;
	private ViewGroup mVgInstitution;
	private TextView mTvArticlesNum;
	private ViewGroup mVgArticles;
	private TextView mTvDoctorsNum;
	private ViewGroup mVgDoctors;
	private TextView mTvBalance;
	private ViewGroup mVgBalance;
	private ViewGroup mVgOrders;
	private ViewGroup mVgIntegral;
	private ViewGroup mVgFeedback;
	private ViewGroup mVgAbout;
	
	private List<ViewGroup> mVgThirdPartnerList;
	private ViewGroup mVgThirdPartner;
	private List<ThirdPartner> mThirdPartners;
	
	private CustomToast mProgressDialog;
	private IeetonUser mUser;
	private FetchInfoTask mTask;
	private int mCountProduct;
	private int mCountInstitution;
	private int mCountArticle;
	private int mCountDoctor;
	private BroadcastReceiver mReceiver;
	private boolean mIsTaskFree = true;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
		initView(view);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.FAVORITE_ARTICLE_ACTION);
		filter.addAction(Constants.UNFAVORITE_ARTICLE_ACTION);
		filter.addAction(Constants.ACTION_FOLLOW);
		filter.addAction(Constants.ACTION_UNFOLLOW);
		filter.addAction(Constants.ACTION_FAVORITE_PRODUCT);
		filter.addAction(Constants.ACTION_UNFAVORITE_PRODUCT);
		filter.addAction(Constants.PAY_SUCCESS_ACTION);
		filter.addAction(Constants.LOGIN_ACTION);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Constants.PAY_SUCCESS_ACTION.equals(intent.getAction())
						||Constants.LOGIN_ACTION.equals(intent.getAction())
						){
					executeTask(MODE_GETINFO);
				}else if (Constants.FAVORITE_ARTICLE_ACTION.equals(intent.getAction())
						|| Constants.UNFAVORITE_ARTICLE_ACTION.equals(intent.getAction())
						|| Constants.ACTION_FOLLOW.equals(intent.getAction())
						|| Constants.ACTION_UNFOLLOW.equals(intent.getAction())
						|| Constants.ACTION_FAVORITE_PRODUCT.equals(intent.getAction())
						|| Constants.ACTION_UNFAVORITE_PRODUCT.equals(intent.getAction())
						){
					executeTask(MODE_GETCOUNT);
				}
			}
		};
		getActivity().registerReceiver(mReceiver, filter);
		return view;
	}
	
	void initView(View view){
		mTvRefresh = (TextView) view.findViewById(R.id.tv_refresh);
		mTvRefresh.setOnClickListener(this);
		mIvHeader = (RoundedImageView) view.findViewById(R.id.iv_header);
		mIvHeader.setOnClickListener(this);
		mTvName = (TextView) view.findViewById(R.id.tv_name);
		mIvSetting = (ImageView) view.findViewById(R.id.iv_setting);
		mIvSetting.setOnClickListener(this);
		
		mTvProductsNum = (TextView) view.findViewById(R.id.tv_collection_num);
		mVgProducts = (ViewGroup) view.findViewById(R.id.ll_collection);
		mVgProducts.setOnClickListener(this);
		
		mTvInstitutionNum = (TextView) view.findViewById(R.id.tv_institution_num);
		mVgInstitution = (ViewGroup) view.findViewById(R.id.ll_institution);
		mVgInstitution.setOnClickListener(this);
		
		mTvArticlesNum = (TextView) view.findViewById(R.id.tv_articles_num);
		mVgArticles = (ViewGroup) view.findViewById(R.id.ll_articles);
		mVgArticles.setOnClickListener(this);
		
		mTvDoctorsNum = (TextView) view.findViewById(R.id.tv_doctors_num);
		mVgDoctors = (ViewGroup) view.findViewById(R.id.ll_doctors);
		mVgDoctors.setOnClickListener(this);
		
		mTvBalance = (TextView) view.findViewById(R.id.tv_balance_num);
		mVgBalance = (ViewGroup) view.findViewById(R.id.rl_balance);
		mVgBalance.setOnClickListener(this);
		
		mVgOrders = (ViewGroup) view.findViewById(R.id.rl_orders);
		mVgOrders.setOnClickListener(this);
		mVgIntegral = (ViewGroup) view.findViewById(R.id.rl_integral);
		mVgIntegral.setOnClickListener(this);
		mVgFeedback = (ViewGroup) view.findViewById(R.id.rl_feedback);
		mVgFeedback.setOnClickListener(this);
		mVgAbout = (ViewGroup) view.findViewById(R.id.rl_about);
		mVgAbout.setOnClickListener(this);
		
		initThirdPartnerView(view);
		
		executeTask(MODE_GETINFO);
	}

	private void initThirdPartnerView(View view){
		mVgThirdPartner = (ViewGroup) view.findViewById(R.id.ll_third_partner);
		mThirdPartners = NetEngine.getThirdPartnerList();
		mVgThirdPartnerList = new ArrayList<ViewGroup>();
		{
			ViewGroup partner = (ViewGroup) view.findViewById(R.id.ll_partner1);
			mVgThirdPartnerList.add(partner);
		}
		{
			ViewGroup partner = (ViewGroup) view.findViewById(R.id.ll_partner2);
			mVgThirdPartnerList.add(partner);
		}
		{
			ViewGroup partner = (ViewGroup) view.findViewById(R.id.ll_partner3);
			mVgThirdPartnerList.add(partner);
		}
		{
			ViewGroup partner = (ViewGroup) view.findViewById(R.id.ll_partner4);
			mVgThirdPartnerList.add(partner);
		}
		{
			ViewGroup partner = (ViewGroup) view.findViewById(R.id.ll_partner5);
			mVgThirdPartnerList.add(partner);
		}
		for(int i=0; i<mVgThirdPartnerList.size(); i++){
			mVgThirdPartnerList.get(i).setOnClickListener(this);
		}
		
		if (mThirdPartners == null || mThirdPartners.isEmpty()){
			return;
		}
		mVgThirdPartner.setVisibility(View.VISIBLE);
		for(int i=0; i<mThirdPartners.size(); i++){
			ViewGroup vg = mVgThirdPartnerList.get(i);
			vg.setVisibility(View.VISIBLE);
			ImageView iv = (ImageView) vg.findViewById(R.id.iv_partner);
			AsyncBitmapLoader.getInstance().loadBitmap(getActivity(), 
					iv, NetEngine.getImageUrl(mThirdPartners.get(i).getPicUrl()));
			TextView title = (TextView) vg.findViewById(R.id.tv_partner);
			title.setText(mThirdPartners.get(i).getTitle());
		}
	}
	
	public void update(){
		if (mUser == null){
			return;
		}
		mTvName.setText(mUser.getName());
		AsyncBitmapLoader.getInstance().loadBitmap(getActivity(), Utils.getMyUid(getActivity()), 
				NetEngine.getImageUrl(mUser.getAvatar()), mIvHeader, null);
		mTvBalance.setText(mUser.getBalance()+"");
		mTvProductsNum.setText(mCountProduct+"");
		mTvInstitutionNum.setText(mCountInstitution+"");
		mTvArticlesNum.setText(mCountArticle+"");
		mTvDoctorsNum.setText(mCountDoctor+"");
	}
	
	@Override
	public void onDestroy() {
		if (mReceiver != null){
			getActivity().unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		super.onDestroy();
	}

	private void executeTask(int action){
		if (!mIsTaskFree){
			return;
		}
		mTask = new FetchInfoTask();
		try {
			mTask.execute(action);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == mTvRefresh){
			executeTask(MODE_GETINFO);
		}else if (v == mIvHeader){
			if (mUser == null){
				return;
			}
			Intent intent = new Intent(getActivity(), EditProfileActivity.class);
			intent.putExtra(Constants.EXTRA_USER, mUser);
			startActivityForResult(intent, REQUEST_EDIT_INFO);
		}else if (v == mIvSetting){
			startActivity(new Intent(getActivity(), SettingsActivity.class));
		}else if (v == mVgProducts){
			startActivity(new Intent(getActivity(), FavoriteProductActivity.class));
		}else if (v == mVgInstitution){
			Intent intent = new Intent(getActivity(), FollowUsersActivity.class);
			intent.putExtra(Constants.EXTRA_MODE, FollowUsersActivity.MODE_INSTITUTION);
			startActivity(intent);
		}else if (v == mVgArticles){
			startActivity(new Intent(getActivity(), FavoriteArticleActivity.class));
		}else if (v == mVgDoctors){
			Intent intent = new Intent(getActivity(), FollowUsersActivity.class);
			intent.putExtra(Constants.EXTRA_MODE, FollowUsersActivity.MODE_DOCTOR);
			startActivity(intent);			
		}else if (v == mVgBalance){
			Intent intent = new Intent(getActivity(), RechargeActivity.class);
			startActivityForResult(intent, REQUEST_RECHARGE);
		}else if (v == mVgOrders){
			Intent intent = new Intent(getActivity(), OrdersActivity.class);
			startActivity(intent);
		}else if (v == mVgIntegral){
			Intent intent = new Intent(getActivity(), SignActivity.class);
			startActivity(intent);
		}else if (v == mVgFeedback){
			Intent intent = new Intent(getActivity(), ChatActivity.class);
			intent.putExtra(Constants.EXTRA_UID, NetEngine.getSecretaryID());
			intent.putExtra(ChatActivity.EXTRA_AUTO_BEGIN_CONVERSATION, true);
			startActivity(intent);
		}else if (v == mVgAbout){
			startActivity(new Intent(getActivity(), AboutActivity.class));
		}else{
			for(int i=0; i< mVgThirdPartnerList.size(); i++){
				if (v == mVgThirdPartnerList.get(i)){
					Intent intent = new Intent(getActivity(), BrowserActivity.class);
					intent.putExtra(Constants.WEB_BROWSER_URL, mThirdPartners.get(i).getUrl());
					intent.putExtra(Constants.WEB_BROWSER_INTERNAL, "false");
					intent.putExtra(Constants.WEB_BROWSER_TITLE, mThirdPartners.get(i).getTitle());
					intent.putExtra(BrowserActivity.EXTRA_SHOW_BOTTOM, "false");
					startActivity(intent);
					break;
				}
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK){
			if (requestCode == REQUEST_EDIT_INFO){
				if (data != null){
					mUser = (IeetonUser) data.getSerializableExtra(Constants.EXTRA_USER);
					update();
				}
			}else if (requestCode == REQUEST_RECHARGE){
				executeTask(MODE_GETINFO);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
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
	
	private class FetchInfoTask extends AsyncTask<Integer, Void, String>{
		private Throwable mThr;
		private int mode;
		
		@Override
		protected String doInBackground(Integer... params) {
			String result = "";
			mode = params[0];
			try {
				if (mode == MODE_GETINFO){
					result = NetEngine.getInstance(getActivity()).
								getUserInfo(Utils.getMyUid(getActivity()));
				}else if (mode == MODE_GETCOUNT){
					result = NetEngine.getInstance(getActivity()).
							getCount();
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
					Utils.handleErrorEvent(mThr, getActivity());
				}else{
					Utils.showToast(getActivity(), R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			try {
				JSONObject obj = new JSONObject(result);
				if (mode == MODE_GETINFO){
					mUser = new IeetonUser(getActivity(), obj);
					executeTask(MODE_GETCOUNT);
				}else if (mode == MODE_GETCOUNT){
					mCountProduct = obj.optInt("productCount");
					mCountInstitution = obj.optInt("institutionCount");
					mCountArticle = obj.optInt("articleCount");
					mCountDoctor = obj.optInt("doctorCount");
				}
				update();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			showProgress();
			super.onPreExecute();
		}
		
	}
}
