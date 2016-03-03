package com.ieeton.user.view;

import java.util.ArrayList;
import java.util.List;

import com.ieeton.user.IeetonApplication;
import com.ieeton.user.R;
import com.ieeton.user.activity.HomeFragment;
import com.ieeton.user.activity.LoginActivity;
import com.ieeton.user.activity.ProductListActivity;
import com.ieeton.user.activity.SignActivity;
import com.ieeton.user.models.Lable;
import com.ieeton.user.models.ProductCategory;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class HomeHeaderViewOld extends LinearLayout implements OnClickListener{
	private List<ImageView> mCategoryIvList;
	private List<ViewGroup> mCategoryVgList;
	private List<TextView> mCategoryTvList;
	
	private TextView mTvLable1;
	private TextView mTvLable2;
	private TextView mTvLable3;
	private TextView mTvLable4;
	private TextView mTvLable5;
	private TextView mTvLableDes1;
	private TextView mTvLableDes2;
	private TextView mTvLableDes3;
	private TextView mTvLableDes4;
	private TextView mTvLableDes5;
	private RoundedImageView mIvLable1;
	private ImageView mIvLable3;
	private ImageView mIvLable4;
	private ImageView mIvLable5;
	private ImageView mIvLable6;
	private ImageView mIvLable7;
	private ImageView mIvLable8;
	private ImageView mIvLable9;
	private List<ViewGroup> mVgLableList;

	private Context mContext;
	private HomeFragment mFragment;
	private View mView;
	
	public HomeHeaderViewOld(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public HomeHeaderViewOld(Context context, HomeFragment fragment){
		super(context);
		mContext = context;
		mFragment = fragment;
		initView();
	}
	
	public HomeHeaderViewOld(Context context, AttributeSet attrs){
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public HomeHeaderViewOld(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	private void initView(){
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	mView = inflater.inflate(R.layout.home_header_view, this);
		
		mCategoryIvList = new ArrayList<ImageView>();
		mCategoryIvList.add((ImageView) findViewById(R.id.iv_category1));
		mCategoryIvList.add((ImageView) findViewById(R.id.iv_category2));
		mCategoryIvList.add((ImageView) findViewById(R.id.iv_category3));
		mCategoryIvList.add((ImageView) findViewById(R.id.iv_category4));
		mCategoryIvList.add((ImageView) findViewById(R.id.iv_category5));
		
		mCategoryVgList = new ArrayList<ViewGroup>();
		mCategoryVgList.add((ViewGroup) findViewById(R.id.ll_category1));
		mCategoryVgList.add((ViewGroup) findViewById(R.id.ll_category2));
		mCategoryVgList.add((ViewGroup) findViewById(R.id.ll_category3));
		mCategoryVgList.add((ViewGroup) findViewById(R.id.ll_category4));
		mCategoryVgList.add((ViewGroup) findViewById(R.id.ll_category5));
		
		mCategoryTvList = new ArrayList<TextView>();
		mCategoryTvList.add((TextView) findViewById(R.id.tv_category1));
		mCategoryTvList.add((TextView) findViewById(R.id.tv_category2));
		mCategoryTvList.add((TextView) findViewById(R.id.tv_category3));
		mCategoryTvList.add((TextView) findViewById(R.id.tv_category4));
		mCategoryTvList.add((TextView) findViewById(R.id.tv_category5));

		initLable();
		setCategory();
	}
	
	private void initLable(){
		mTvLable1 = (TextView) mView.findViewById(R.id.tv_lable1);
		mTvLable2 = (TextView) mView.findViewById(R.id.tv_lable2);
		mTvLable3 = (TextView) mView.findViewById(R.id.tv_lable3);
		mTvLable4 = (TextView) mView.findViewById(R.id.tv_lable4);
		mTvLable5 = (TextView) mView.findViewById(R.id.tv_lable5);

		mTvLableDes1 = (TextView) mView.findViewById(R.id.tv_lable1_description);
		mTvLableDes2 = (TextView) mView.findViewById(R.id.tv_lable2_description);
		mTvLableDes3 = (TextView) mView.findViewById(R.id.tv_lable3_description);
		mTvLableDes4 = (TextView) mView.findViewById(R.id.tv_lable4_description);
		mTvLableDes5 = (TextView) mView.findViewById(R.id.tv_lable5_description);
		
		mIvLable1 = (RoundedImageView) mView.findViewById(R.id.iv_lable1);
		mIvLable3 = (ImageView) mView.findViewById(R.id.iv_lable3);
		mIvLable4 = (ImageView) mView.findViewById(R.id.iv_lable4);
		mIvLable5 = (ImageView) mView.findViewById(R.id.iv_lable5);
		mIvLable6 = (ImageView) mView.findViewById(R.id.iv_lable6);
		mIvLable7 = (ImageView) mView.findViewById(R.id.iv_lable7);
		mIvLable8 = (ImageView) mView.findViewById(R.id.iv_lable8);
		mIvLable9 = (ImageView) mView.findViewById(R.id.iv_lable9);

		mVgLableList = new ArrayList<ViewGroup>();
		mVgLableList.add((ViewGroup) mView.findViewById(R.id.vg_lable1));
		mVgLableList.add((ViewGroup) mView.findViewById(R.id.vg_lable2));
		mVgLableList.add((ViewGroup) mView.findViewById(R.id.vg_lable3));
		mVgLableList.add((ViewGroup) mView.findViewById(R.id.vg_lable4));
		mVgLableList.add((ViewGroup) mView.findViewById(R.id.vg_lable5));
		mVgLableList.add((ViewGroup) mView.findViewById(R.id.vg_lable6));
		mVgLableList.add((ViewGroup) mView.findViewById(R.id.vg_lable7));
		mVgLableList.add((ViewGroup) mView.findViewById(R.id.vg_lable8));
		mVgLableList.add((ViewGroup) mView.findViewById(R.id.vg_lable9));
		
		List<Lable> lableList = null;
		if (IeetonApplication.mServerHostData != null){
			lableList = IeetonApplication.mServerHostData.getLableList();
		}
		if (lableList != null && !lableList.isEmpty()){
			for(ViewGroup vg : mVgLableList){
				vg.setOnClickListener(this);
			}
			mTvLable1.setText(lableList.get(0).getName());
			mTvLable2.setText(lableList.get(1).getName());
			mTvLable3.setText(lableList.get(2).getName());
			mTvLable4.setText(lableList.get(3).getName());
			mTvLable5.setText(lableList.get(4).getName());
			
			mTvLableDes1.setText(lableList.get(0).getDescription());
			mTvLableDes2.setText(lableList.get(1).getDescription());
			mTvLableDes3.setText(lableList.get(2).getDescription());
			mTvLableDes4.setText(lableList.get(3).getDescription());
			mTvLableDes5.setText(lableList.get(4).getDescription());
			
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvLable1, 
					NetEngine.getImageUrl(lableList.get(0).getIconUrl()));
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvLable3, 
					NetEngine.getImageUrl(lableList.get(2).getIconUrl()));
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvLable4, 
					NetEngine.getImageUrl(lableList.get(3).getIconUrl()));
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvLable5, 
					NetEngine.getImageUrl(lableList.get(4).getIconUrl()));
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvLable6, 
					NetEngine.getImageUrl(lableList.get(5).getIconUrl()));
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvLable7, 
					NetEngine.getImageUrl(lableList.get(6).getIconUrl()));
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvLable8, 
					NetEngine.getImageUrl(lableList.get(7).getIconUrl()));
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvLable9, 
					NetEngine.getImageUrl(lableList.get(8).getIconUrl()));
		}
	}
	
	private void setCategory(){
		List<ProductCategory> list = NetEngine.getProductCategoryList();
		for(int i=0; i<mCategoryIvList.size(); i++){
			if (list != null && !list.isEmpty()){
				AsyncBitmapLoader.getInstance().loadBitmap(mContext, 
						mCategoryIvList.get(i), NetEngine.getImageUrl(list.get(i).getCategoryIconUrl()));
				mCategoryTvList.get(i).setText(list.get(i).getCagegoryName());
			}
		}
		for(ViewGroup vg : mCategoryVgList){
			vg.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		for(int i=0; i<mCategoryVgList.size(); i++){
			if (v == mCategoryVgList.get(i)){
				if (i == 4){
					if (Utils.getMyType(mContext) == 5){
						mContext.startActivity(new Intent(mContext, LoginActivity.class));
						return;
					}
					Intent intent = new Intent(mContext, SignActivity.class);
					intent.putExtra(Constants.EXTRA_CITY, mFragment.getCity());
					mFragment.startActivity(intent);
				}else{
					Intent intent = new Intent(mContext, ProductListActivity.class);
					intent.putExtra(Constants.EXTRA_CITY, mFragment.getCity());
					List<ProductCategory> list = NetEngine.getProductCategoryList();
					intent.putExtra(Constants.EXTRA_CATEGORYID, list.get(i).getCategoryId());
					intent.putExtra(Constants.EXTRA_CATEGORY, list.get(i));
					mFragment.startActivity(intent);
				}
				return;
			}
		}
		for(int i=0; i<mVgLableList.size(); i++){
			if (v == mVgLableList.get(i)){
				List<Lable> lableList = IeetonApplication.mServerHostData.getLableList();
				if (lableList != null && !lableList.isEmpty()){
					Intent intent = new Intent(mContext, ProductListActivity.class);
					intent.putExtra(Constants.EXTRA_CITY, mFragment.getCity());
					intent.putExtra(Constants.EXTRA_LABLE, lableList.get(i));
					mFragment.startActivity(intent);
					break;
				}
			}
		}
	}	
}
