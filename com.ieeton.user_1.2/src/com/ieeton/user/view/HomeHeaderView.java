package com.ieeton.user.view;

import java.io.Serializable;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class HomeHeaderView extends LinearLayout implements OnClickListener{
	private List<ImageView> mCategoryIvList;
	private List<ViewGroup> mCategoryVgList;
	private List<TextView> mCategoryTvList;
	
	private Context mContext;
	private HomeFragment mFragment;
	private View mView;
	
	public HomeHeaderView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public HomeHeaderView(Context context, HomeFragment fragment){
		super(context);
		mContext = context;
		mFragment = fragment;
		initView();
	}
	
	public HomeHeaderView(Context context, AttributeSet attrs){
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public HomeHeaderView(Context context, AttributeSet attrs, int defStyle) {
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
	
	private List<Lable> lableList = null;
	
	private void initLable(){
		LinearLayout lableListView = (LinearLayout) findViewById(R.id.lable_list);
		if (IeetonApplication.mServerHostData != null){
			lableList = IeetonApplication.mServerHostData.getLableList();
		}
		if (lableList != null && !lableList.isEmpty()){
			for (int i=0; i<lableList.size(); i++){
				final ImageView img = new ImageView(mContext);
				LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, getResources().getDimensionPixelSize(R.dimen.recommond_pic_height));
				lp.topMargin = 10;
				img.setLayoutParams(lp);
				img.setScaleType(ScaleType.CENTER_CROP);
				AsyncBitmapLoader.getInstance().loadBitmap(mContext, img, 
						NetEngine.getImageUrl(lableList.get(i).getIconUrl()));
				img.setTag(NetEngine.getImageUrl(lableList.get(i).getIconUrl()));
				img.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if (mFragment.getCity() == null){
							return;
						}
						Intent intent = new Intent(mContext, ProductListActivity.class);
						intent.putExtra(Constants.EXTRA_CITY, mFragment.getCity());
						Lable lable = null;
						for(Lable item : lableList){
							if (img.getTag().equals(NetEngine.getImageUrl(item.getIconUrl()))){
								lable = item;
								break;
							}
						}
						intent.putExtra(Constants.EXTRA_LABLE, lable);
						mFragment.startActivity(intent);
					}
				});
				lableListView.addView(img);
			}
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
					if (mFragment.getCity() == null){
						return;
					}
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
//		for(int i=0; i<mVgLableList.size(); i++){
//			if (v == mVgLableList.get(i)){
//				List<Lable> lableList = IeetonApplication.mServerHostData.getLableList();
//				if (lableList != null && !lableList.isEmpty()){
//					Intent intent = new Intent(mContext, ProductListActivity.class);
//					intent.putExtra(Constants.EXTRA_CITY, mFragment.getCity());
//					intent.putExtra(Constants.EXTRA_LABLE, lableList.get(i));
//					mFragment.startActivity(intent);
//					break;
//				}
//			}
//		}
	}	
}
