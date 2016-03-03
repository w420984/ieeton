package com.ieeton.agency.view;

import com.ieeton.agency.models.AccountDetailItem;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.AsyncBitmapLoader;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.utils.AsyncBitmapLoader.ImageCallBack;
import com.ieeton.agency.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AccountDetailItemView extends LinearLayout {
	private Context mContext;
	private ImageView mIvIcon;
	private TextView mTvTitle;
	private TextView mTvAmount;
	private TextView mTvTime;
	
	public AccountDetailItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	
	public AccountDetailItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public AccountDetailItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	private void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.account_detail_item, this);
		
		mIvIcon = (ImageView) findViewById(R.id.icon);
		mTvTitle = (TextView) findViewById(R.id.title);
		mTvAmount = (TextView) findViewById(R.id.amount);
		mTvTime = (TextView) findViewById(R.id.time);
	}
	
	public void update(final AccountDetailItem item){
		mTvTitle.setText(item.getTitle());
		mTvAmount.setText(item.getAmount());
		mTvTime.setText(item.getTime());
		if (AccountDetailItem.DETAIL_TYPE_CALL.equals(item.getType())
				|| AccountDetailItem.DETAIL_TYPE_REWARDIN.equals(item.getType())
				|| AccountDetailItem.DETAIL_TYPE_REWARDOUT.equals(item.getType())){
			if (item.getUser() == null){
				mIvIcon.setImageResource(Utils.getDefaultPortraitId(item.getUser().getType(), null));
			}else{
				Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(mContext, 
						item.getUser().getId(), NetEngine.getImageUrl(item.getUser().getAvatar()), 
						item.getUser().getType(), new ImageCallBack() {
							@Override
							public void imageLoad(Bitmap bitmap, Object user) {
								if (AccountDetailItem.DETAIL_TYPE_CALL.equals(item.getType())){
									if (bitmap !=null && !bitmap.isRecycled()){
										mIvIcon.setImageBitmap(bitmap);
									}else {
										mIvIcon.setImageResource(Utils.getDefaultPortraitId(item.getUser().getType(), null));
									}
								}
							}
						});
				if (b !=null && !b.isRecycled()){
					mIvIcon.setImageBitmap(b);
				}else {
					mIvIcon.setImageResource(Utils.getDefaultPortraitId("patient", null));
				}
			}
		}else if (AccountDetailItem.DETAIL_TYPE_RECHARGE.equals(item.getType())){		
			mIvIcon.setImageResource(R.drawable.myprofile_icon_recharge);
		}else if (AccountDetailItem.DETAIL_TYPE_PRODUCT_BUT.equals(item.getType())
				|| AccountDetailItem.DETAIL_TYPE_PRODUCT_SELL.equals(item.getType())){
//			Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(mContext, 
//					NetEngine.getImageServerUrl() + item.getProduct().getPics().get(0), 
//					new ImageCallBack() {
//						@Override
//						public void imageLoad(Bitmap bitmap, Object user) {
//							if (bitmap != null && !bitmap.isRecycled()){
//								mIvIcon.setImageBitmap(bitmap);
//							}
//						}
//					});
//			if (b !=null && !b.isRecycled()){
//				mIvIcon.setImageBitmap(b);
//			}
		}else{		
			mIvIcon.setImageResource(R.drawable.myprofile_icon_cash);
		}
	}
}
