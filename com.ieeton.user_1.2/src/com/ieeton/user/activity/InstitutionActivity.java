package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;

import com.ieeton.user.R;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.utils.Constants;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class InstitutionActivity extends FragmentActivity implements OnClickListener{
	public static final String strKey = "3AB1810EBAAE0175EB41A744CF3B2D6497407B87";
	private final int INDEX_INTRODUCE = 0;
	private final int INDEX_PRODUCTS = 1;
	private final int INDEX_DOCTORS = 2;
	
	private ImageView mIvBack;
	private TextView mTvTitle;
	private Button mTabIntroduce;
	private Button mTabProducts;
	private Button mTabDoctors;
	private List<View> mLineList;
	
	private int mCurIndex;
	private int mClickIndex = INDEX_INTRODUCE;
	private IntroduceFragment mIntroduceFragment;
	private ProductsFragment mProductsFragment;
	private DoctorsFragment mDoctorsFragment;
	private Fragment[] mFragments;
	
	public IeetonUser mUser;
	public String mUserId;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		Intent intent = getIntent();
		mUser = (IeetonUser) intent.getSerializableExtra(Constants.EXTRA_USER);
		mUserId = intent.getStringExtra(Constants.EXTRA_UID);
		
		setContentView(R.layout.activity_institution);		
		initView();
		showDefaltTab(mClickIndex);
	}
		
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView(){
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mIvBack.setOnClickListener(this);
		
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mLineList = new ArrayList<View>();
		View lineIntroduce = (View) findViewById(R.id.line_introduce);
		mLineList.add(lineIntroduce);
		View lineProducts = (View) findViewById(R.id.line_products);
		mLineList.add(lineProducts);
		View lineDoctors = (View) findViewById(R.id.line_doctors);
		mLineList.add(lineDoctors);
		
		mTabIntroduce = (Button) findViewById(R.id.btn_introduce);
		mTabIntroduce.setOnClickListener(this);
		mTabProducts = (Button) findViewById(R.id.btn_products);
		mTabProducts.setOnClickListener(this);
		mTabDoctors = (Button) findViewById(R.id.btn_doctors);
		mTabDoctors.setOnClickListener(this);
		
		mIntroduceFragment = new IntroduceFragment();
		mProductsFragment = new ProductsFragment();
		mDoctorsFragment = new DoctorsFragment();
		mFragments = new Fragment[]{
				mIntroduceFragment, mProductsFragment, mDoctorsFragment
		};
	}

	private void showDefaltTab(int index){
		FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
		if (!mFragments[index].isAdded()){
			trx.add(R.id.fragment_container, mFragments[index]).commit();
		}else{
			trx.replace(R.id.fragment_container, mFragments[index]).commit();
		}
		mCurIndex = index;
		showLine();
	}
	
	private void showLine(){
		for(View view : mLineList){
			view.setVisibility(View.INVISIBLE);
		}
		mLineList.get(mCurIndex).setVisibility(View.VISIBLE);
		String title = "";
		if (mCurIndex == INDEX_INTRODUCE){
			title = getString(R.string.institution_introduce);
		}else if (mCurIndex == INDEX_PRODUCTS){
			title = getString(R.string.other_products);
		}else if (mCurIndex == INDEX_DOCTORS){
			title = getString(R.string.all_doctor);
		}
		mTvTitle.setText(title);
	}
	
	private void switchTab(int index){
		if (mCurIndex != index) {
			FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
			trx.hide(mFragments[mCurIndex]);
			if (!mFragments[index].isAdded()) {
				trx.add(R.id.fragment_container, mFragments[index]);
			}
			trx.show(mFragments[index]).commit();
		}
		mCurIndex = index;
		showLine();
	}
	
	@Override
	public void onClick(View v) {
		if (v == mIvBack){
			finish();
		}else if (v == mTabIntroduce){
			mClickIndex = INDEX_INTRODUCE;
			switchTab(mClickIndex);
		}else if (v == mTabProducts){
			mClickIndex = INDEX_PRODUCTS;
			switchTab(mClickIndex);
		}else if (v == mTabDoctors){
			mClickIndex = INDEX_DOCTORS;
			switchTab(mClickIndex);
		}
	}
}
