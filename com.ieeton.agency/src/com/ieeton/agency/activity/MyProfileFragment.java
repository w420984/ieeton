package com.ieeton.agency.activity;

import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.AsyncBitmapLoader;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.UpdateProfileTask;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.utils.AsyncBitmapLoader.ImageCallBack;
import com.ieeton.agency.view.SliderSwitchView;
import com.ieeton.agency.view.SliderSwitchView.OnChangedListener;
import com.ieeton.agency.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengDialogButtonListener;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateConfig;
import com.umeng.update.UpdateStatus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
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

public class MyProfileFragment extends Fragment implements OnClickListener, OnChangedListener{
	public static final int REQUEST_SET_PRICE = 1;
	public static String EXTRA_PRICE = "extra_price";
	
	private TextView mTvName;
	private TextView mTvFansNum;
	private TextView mTvLikedNum;
	private TextView mTvPatientNum;
	private ImageView mIVPortait;
	private TextView mTvPrice;
	private TextView mTvBalance;
	private TextView mTvRefresh;
	private TextView mTvVersion;
	
	private ViewGroup mVgHeader;
	private ViewGroup mVgCallPrice;
	private ViewGroup mVgMoney;
	private ViewGroup mVgQrcode;
	private ViewGroup mVgMobile;
	private ViewGroup mVgFavorite;
	private ViewGroup mVgAbout;
	private ViewGroup mVgFeedback;
	private ViewGroup mVgVersion;
	private ViewGroup mVgSetting;
	private SliderSwitchView mSwitchCall;
	private SliderSwitchView mSwitchChat;
	
	private Doctor mDoctor;
	private boolean mTaskFree = true;
	private UdpateInfoReceiver mReceiver;
	private int curVersionCode;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
		initView(view);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_UPDATE_INFO);
		mReceiver = new UdpateInfoReceiver();
		getActivity().registerReceiver(mReceiver, filter);
		return view;
	}
	
	private class UdpateInfoReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.ACTION_UPDATE_INFO.equals(intent.getAction())){
				fechInfo();
			}
		}
		
	}
	
	@Override
	public void onPause() {
		MobclickAgent.onPageEnd("MyProfileFrament"); 
		super.onPause();
	}

	@Override
	public void onResume() {
		MobclickAgent.onPageStart("MyProfileFrament");
		super.onResume();
	}

	private void initView(View view){
		
		mTvName = (TextView) view.findViewById(R.id.tv_name);
		mTvPrice = (TextView) view.findViewById(R.id.tv_price);
		mTvBalance = (TextView) view.findViewById(R.id.tv_money);
		mTvFansNum = (TextView) view.findViewById(R.id.tv_fans);
		mTvLikedNum = (TextView) view.findViewById(R.id.tv_liked);
		mTvPatientNum = (TextView) view.findViewById(R.id.tv_patient);
		mIVPortait = (ImageView)view.findViewById(R.id.iv_header);
		mTvVersion = (TextView) view.findViewById(R.id.tv_version);
		mTvRefresh = (TextView) view.findViewById(R.id.titleRefresh);
		mTvRefresh.setOnClickListener(this);
		
		mVgHeader = (ViewGroup) view.findViewById(R.id.rl_header);
		mVgHeader.setOnClickListener(this);
		mVgCallPrice = (ViewGroup) view.findViewById(R.id.rl_price);
		mVgCallPrice.setOnClickListener(this);
		mVgMoney = (ViewGroup) view.findViewById(R.id.rl_money);
		mVgMoney.setOnClickListener(this);
		mVgQrcode = (ViewGroup) view.findViewById(R.id.rl_qrcode);
		mVgQrcode.setOnClickListener(this);
		mVgMobile = (ViewGroup) view.findViewById(R.id.rl_change_mobile);
		mVgMobile.setOnClickListener(this);
		mVgFavorite = (ViewGroup) view.findViewById(R.id.rl_favorite);
		mVgFavorite.setOnClickListener(this);
		mVgAbout = (ViewGroup) view.findViewById(R.id.rl_about);
		mVgAbout.setOnClickListener(this);
		mVgFeedback = (ViewGroup) view.findViewById(R.id.rl_feedback);
		mVgFeedback.setOnClickListener(this);
		mVgVersion = (ViewGroup) view.findViewById(R.id.rl_version_check);
		mVgVersion.setOnClickListener(this);
		mVgSetting = (ViewGroup) view.findViewById(R.id.rl_setting);
		mVgSetting.setOnClickListener(this);
		mSwitchCall = (SliderSwitchView) view.findViewById(R.id.switch_call);
		mSwitchCall.setOnChangedListener(this);
		mSwitchChat = (SliderSwitchView) view.findViewById(R.id.switch_chat);
		mSwitchChat.setOnChangedListener(this);
		
		PackageInfo info;
		String nowVersion = "";
        try {
            info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            nowVersion = info.versionName;
            curVersionCode = info.versionCode;
        } catch (NameNotFoundException e) {
        }
        mTvVersion.setText(getString(R.string.cur_version)+"V"+nowVersion);

        fechInfo();
	}

	private void fechInfo(){
		try{
			FetchInfoTask task = new FetchInfoTask();
			task.execute();
		}catch(RejectedExecutionException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		if (mReceiver != null){
			getActivity().unregisterReceiver(mReceiver);
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mVgHeader){
			if (!mTaskFree){
				return;
			}
			Intent intent = new Intent(getActivity(), UpdateInfoActivity.class);
			intent.putExtra(UpdateInfoActivity.EXTRA_USERINFO, mDoctor);
			startActivity(intent);
		}else if (v == mVgCallPrice){
			if (mDoctor.getPriceEditable() == 1){
				startActivityForResult(new Intent(getActivity(), SetPriceActivity.class), 
							REQUEST_SET_PRICE);
			}else{
				Utils.showToast(getActivity(), R.string.can_not_change_price, Toast.LENGTH_SHORT);
			}
		}else if (v == mVgMoney){
			Intent intent = new Intent(getActivity(), AccountDetailActivity.class); 
			intent.putExtra("balance", mDoctor.getBalance());
			startActivity(intent);
		}else if (v == mVgQrcode){
			startActivity(new Intent(getActivity(), MyQrcodeActivity.class));
		}else if (v == mVgMobile){
			Intent intent = new Intent(getActivity(), BindMobileActivity.class);
			intent.putExtra(BindMobileActivity.EXTRA_ISCHANGE_MOBILE, true);
			startActivity(intent);
		}else if (v == mVgFavorite){
			startActivity(new Intent(getActivity(), MyFavoriteActivity.class));
		}else if (v == mVgAbout){
			startActivity(new Intent(getActivity(), AboutActivity.class));
		}else if (v == mVgFeedback){
			Intent intent = new Intent(getActivity(), ChatActivity.class);
			intent.putExtra("chatType", ChatActivity.CHATTYPE_SINGLE);
			intent.putExtra(ChatActivity.EXTRA_USERID, NetEngine.getFeedbackId());
			startActivity(intent);
		}else if (v == mVgVersion){
			checkVersion();
		}else if (v == mVgSetting){
			startActivity(new Intent(getActivity(), SettingMainActivity.class));
		}else if (v == mTvRefresh){
			fechInfo();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK){
			return;
		}
		if (requestCode == REQUEST_SET_PRICE){
//			if (data != null){
//				String price = data.getStringExtra(EXTRA_PRICE);
//				if (!TextUtils.isEmpty(price)){
//					mTvPrice.setText(String.format(getString(R.string.price), price));
//				}
//			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void updateUI(){
		if (mDoctor == null){
			return;
		}
		mTvName.setText(mDoctor.getDoctorName());
		mTvFansNum.setText(getString(R.string.fans)+mDoctor.getFansNum());
		mTvLikedNum.setText(getString(R.string.like)+mDoctor.getPositiveNum());
		mTvPatientNum.setText(getString(R.string.diagnosis)+mDoctor.getPatientNum());
		String price = String.format(getString(R.string.price), mDoctor.getPrice()+"");
		mTvPrice.setText(price);
		mTvBalance.setText(mDoctor.getBalance()+getString(R.string.money_unit));
		mSwitchCall.setChecked(mDoctor.getMobileOnlineStatus() == 1);
		mSwitchChat.setChecked(mDoctor.getMessageOnlineStatus() == 1);
		Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(getActivity(), 
				mDoctor.getID(), NetEngine.getImageUrl(mDoctor.getPortraitUrl()), 
				"doctor", new ImageCallBack() {
					@Override
					public void imageLoad(Bitmap bitmap, Object user) {
						if (bitmap !=null && !bitmap.isRecycled()){
							mIVPortait.setImageBitmap(bitmap);
						}else {
							mIVPortait.setImageResource(R.drawable.docphoto);
						}
					}
				});
		if (b !=null && !b.isRecycled()){
			mIVPortait.setImageBitmap(b);
		}else {
			mIVPortait.setImageResource(R.drawable.docphoto);
		}
	}
	
	class FetchInfoTask extends AsyncTask<Void, Void, String>{
		private Throwable mThr;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mTaskFree = false;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String result = "";

			try {
				result = NetEngine.getInstance(getActivity())
								.getDoctorInfo(Utils.getPassport(getActivity()), Utils.getMyUid(getActivity()));
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

			JSONObject object = null;
			try {
				object = new JSONObject(result);
				if(!object.getBoolean("error")){
					JSONObject json_data = object.getJSONObject("messages").
													getJSONObject("data").
													getJSONObject("doctor");
					mDoctor = new Doctor(getActivity(), json_data);
					Utils.checkUserPortrait(getActivity(), mDoctor.getID(), mDoctor.getPortraitUrl());
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mTaskFree = true;
		}

		@Override
		protected void onPostExecute(String result) {
			mTaskFree = true;
			if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, getActivity());
				}else{
					Utils.showToast(getActivity(), R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}

				return;
			}

			updateUI();
		}
		
	}

	@Override
	public void OnChanged(SliderSwitchView sliderSwitch, boolean checkState) {
		int mobileOn = -1;
		int messageOn = -1;
		if (sliderSwitch == mSwitchCall){
			mobileOn = checkState ? 1 : 0;
		}else if (sliderSwitch == mSwitchChat){
			messageOn = checkState ? 1 : 0;
		}
		if (mobileOn != -1 || messageOn != -1){
			Doctor doctor = new Doctor();
			doctor.setMessageOnlineStatus(messageOn);
			doctor.setMobileOnlineStatus(mobileOn);
			UpdateProfileTask task = new UpdateProfileTask(getActivity(), doctor) {
				
				@Override
				protected void updateEnd(boolean success) {
					
				}
				
				@Override
				protected void updateCancel() {
					
				}
				
				@Override
				protected void updateBegin() {
					
				}
			};
			try {
				task.execute();
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void checkVersion(){
    	//友盟版本更新
    	MobclickAgent.updateOnlineConfig(getActivity().getApplicationContext());
    	UpdateConfig.setDebug(true);	//调试信息开关
    	UmengUpdateAgent.setUpdateOnlyWifi(false);	//设置是否仅wifi模式才更新
    	UmengUpdateAgent.setUpdateCheckConfig(true);		//集成监测开关
    	UmengUpdateAgent.update(getActivity().getApplicationContext());
    	
    	String updateConfig = MobclickAgent.getConfigParams(getActivity().getApplicationContext(), "upgrade_mode");
    	Utils.logd("updateConfig="+updateConfig);
    	if (TextUtils.isEmpty(updateConfig)){
    		return;
    	}
    	try {
			JSONObject obj = new JSONObject(updateConfig);
			String versionCode = obj.getString("versionCode");
			String modeString = obj.getString("mode");
			if (TextUtils.isEmpty(versionCode) || TextUtils.isEmpty(modeString)){
				return;
			}
			int code = curVersionCode;
			try {
				code = Integer.valueOf(versionCode);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if (curVersionCode >= code){
				Utils.showToast(getActivity(), getString(R.string.no_new_version), Toast.LENGTH_SHORT);
				return;
			}
			Utils.logd("modeString="+modeString);
			if ("F".equals(modeString)){
				UmengUpdateAgent.setDialogListener(new UmengDialogButtonListener() {

				    @Override
				    public void onClick(int status) {
				        switch (status) {
				        case UpdateStatus.Update:
				            break;
				        default:
				        	if (getActivity().isFinishing()){
				        		Utils.showToast(getActivity().getApplicationContext(), R.string.update_notice, Toast.LENGTH_SHORT);
								Utils.exitApp(getActivity());
								return;
				        	}
				        	android.app.AlertDialog.Builder builder =  new android.app.AlertDialog.Builder(getActivity());
				        	builder.setMessage(getString(R.string.update_notice));
				        	builder.setCancelable(false);		
				        	builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Utils.exitApp(getActivity());
								}
							});
				        	builder.show();
				        }
				    }
				});
			}else {
				
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
