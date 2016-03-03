package com.ieeton.user.view;

import com.ieeton.user.R;
import com.ieeton.user.activity.GenerateOrderActivity;
import com.ieeton.user.activity.SignActivity;
import com.ieeton.user.models.Product;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.utils.AsyncBitmapLoader.ImageCallBack;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class IntegralProductItemView extends LinearLayout {
	private Context mContext;
	private TextView mTvName;
	private TextView mTvPrice;
	private ImageView mIvIcon;
	private TextView mTvBuy;
	private Product mProduct;

	public IntegralProductItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public IntegralProductItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public IntegralProductItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}
	
	private void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.integral_product_item_view, this);
		
		mTvName = (TextView) findViewById(R.id.name);
		mTvPrice = (TextView) findViewById(R.id.price);
		mTvBuy = (TextView) findViewById(R.id.tv_buy);
		mIvIcon = (ImageView) findViewById(R.id.iv_icon);
//		mTvBuy.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (mProduct == null){
//					return;
//				}
//				Intent intent = new Intent(mContext, GenerateOrderActivity.class);
//				intent.putExtra(Constants.EXTRA_UID, mProduct.getOwnerUid());
//				intent.putExtra(Constants.EXTRA_PRODUCT, mProduct);
//				mContext.startActivity(intent);				
//			}
//		});
	}
	
	public TextView getBuyBtn(){
		return mTvBuy;
	}
	
	public void update(Product product){
		if (product == null){
			return;
		}
		mProduct = product;
		mTvName.setText(product.getName());
		String price = product.getIntegral() + getResources().getString(R.string.integral);
		mTvPrice.setText(price);
		if (!TextUtils.isEmpty(product.getProductionUrl())){
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvIcon,
							NetEngine.getImageUrl(product.getProductionUrl()));
		}
	}
}
