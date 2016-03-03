package com.ieeton.user.view;

import com.ieeton.user.R;
import com.ieeton.user.models.AccountDetailItem;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import android.content.Context;
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
		if (AccountDetailItem.DETAIL_TYPE_RECHARGE == item.getType()){		
			mIvIcon.setImageResource(R.drawable.myprofile_icon_recharge);
		}else if (AccountDetailItem.DETAIL_TYPE_PRODUCT_BUY == item.getType()
				|| AccountDetailItem.DETAIL_TYPE_PRODUCT_SALE == item.getType()){
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvIcon,
					NetEngine.getImageUrl(item.getProduct().getProductionUrl()));
		}
	}
}
