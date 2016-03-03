package com.ieeton.user.view;

import com.ieeton.user.R;
import com.ieeton.user.models.Product;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.AsyncBitmapLoader.ImageCallBack;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProductItemView extends LinearLayout {
	private Context mContext;
	private TextView mTvName;
	private TextView mTvIndroduce;
	private TextView mTvPrice;
	private ImageView mIvIcon;
	private TextView mTvDistance;
	private TextView mTvGoodrate;
	private ImageView mIvDistance;

	public ProductItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public ProductItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public ProductItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}
	
	private void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.product_item_view, this);
		
		mTvName = (TextView) findViewById(R.id.name);
		mTvIndroduce = (TextView) findViewById(R.id.introduce);
		mIvDistance = (ImageView) findViewById(R.id.iv_distance);
		mTvPrice = (TextView) findViewById(R.id.price);
		mIvIcon = (ImageView) findViewById(R.id.iv_icon);
		mTvDistance = (TextView) findViewById(R.id.tv_distance);
		mTvGoodrate = (TextView) findViewById(R.id.tv_goodrate);
	}
	
	public void update(Product product){
		if (product == null){
			return;
		}
		mTvName.setText(product.getName());
		mTvIndroduce.setText(Html.fromHtml(product.getIntroduce()));
		String price = product.getPrice()>0 ? "¥" + product.getPrice() : getResources().getString(R.string.price_free);
		mTvPrice.setText(price);
		if (TextUtils.isEmpty(product.getDistance())){
			mTvDistance.setVisibility(View.GONE);
			mIvDistance.setVisibility(View.GONE);
		}else{
			mTvDistance.setVisibility(View.VISIBLE);
			mIvDistance.setVisibility(View.VISIBLE);
			mTvDistance.setText(product.getDistance()+getResources().getString(R.string.km));
		}
		mTvGoodrate.setText(product.getGoodrate()+"%");
		if (!TextUtils.isEmpty(product.getProductionUrl())){
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvIcon,
							NetEngine.getImageUrl(product.getProductionUrl()));
		}
	}
}
