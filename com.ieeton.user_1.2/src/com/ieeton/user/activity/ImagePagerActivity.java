package com.ieeton.user.activity;
import java.util.ArrayList;
import java.util.List;

import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.Type;
import com.easemob.chat.ImageMessageBody;
import com.ieeton.user.R;
import com.ieeton.user.view.HackyViewPager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.TextView;

public class ImagePagerActivity extends FragmentActivity {
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String EXTRA_IMAGE_INDEX = "image_index";
	public static final String EXTRA_IMAGE_URLS = "image_urls";

	private HackyViewPager mPager;
	private int pagerPosition;
	private TextView indicator;
	private List<EMMessage> mMsgList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail_pager);

		EMMessage curMsg = (EMMessage)getIntent().getParcelableExtra(EXTRA_IMAGE_INDEX);
		List<EMMessage> list = (List<EMMessage>)getIntent().getSerializableExtra(EXTRA_IMAGE_URLS);
		if(list == null){
			finish();
			return;
		}
		mMsgList = new ArrayList<EMMessage>();
		
		List<String> urlslist = new ArrayList<String>();
		for(EMMessage msg:list){
			if(Type.IMAGE == msg.getType()){
				mMsgList.add(msg);
				String filePath = ((ImageMessageBody) msg.getBody()).getLocalUrl();
				if(filePath != null && !"".equals(filePath)){
					urlslist.add(filePath);
				}else{
					filePath = ((ImageMessageBody) msg.getBody()).getRemoteUrl();
					if(filePath != null && !"".equals(filePath)){
						urlslist.add(filePath);
					}
				}
				Log.v("sereinli","MessageAdapter, filePath:"+filePath);
			}
		}
		
		pagerPosition = 0;
		String msgid = curMsg.getMsgId();
		for(int i = 0; i < mMsgList.size(); i++){
			if(msgid.equals(mMsgList.get(i).getMsgId())){
				pagerPosition = i;
				break;
			}
		}
		
		Log.v("sereinli","MessageAdapter, urlslist.size:"+urlslist.size());
		String[] urls = urlslist.toArray(new String[urlslist.size()]); 
		
		mPager = (HackyViewPager) findViewById(R.id.pager);
		ImagePagerAdapter mAdapter = new ImagePagerAdapter(
				getSupportFragmentManager(), urls);
		mPager.setAdapter(mAdapter);
		indicator = (TextView) findViewById(R.id.indicator);

		CharSequence text = getString(R.string.viewpager_indicator, 1, mPager
				.getAdapter().getCount());
		indicator.setText(text);
		// 更新下标
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				CharSequence text = getString(R.string.viewpager_indicator,
						arg0 + 1, mPager.getAdapter().getCount());
				indicator.setText(text);
			}

		});
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		mPager.setCurrentItem(pagerPosition);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, mPager.getCurrentItem());
	}

	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public String[] fileList;

		public ImagePagerAdapter(FragmentManager fm, String[] fileList) {
			super(fm);
			this.fileList = fileList;
		}

		@Override
		public int getCount() {
//			return fileList == null ? 0 : fileList.length;
			if(mMsgList != null){
				return mMsgList.size();
			}
			return 0;
		}

		@Override
		public Fragment getItem(int position) {
			String url = fileList[position];
			return ImageDetailFragment.newInstance(ImagePagerActivity.this, mMsgList.get(position));
		}

	}
}