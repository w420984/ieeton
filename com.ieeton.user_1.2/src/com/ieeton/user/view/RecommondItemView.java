package com.ieeton.user.view;

import com.ieeton.user.R;
import com.ieeton.user.models.Product;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class RecommondItemView extends FrameLayout {
	private TextView mTvName;
	private TextView mTvDistance;
	private ImageView mIvPic;
	private Context mContext;

	public RecommondItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public RecommondItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public RecommondItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}
	
	private void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.recommond_item_view, this);
		mTvName = (TextView) findViewById(R.id.tv_name);
		mTvDistance = (TextView) findViewById(R.id.tv_distance);
		mIvPic = (ImageView) findViewById(R.id.iv_pic);
	}
	
	public void update(Product product){
		if (product == null){
			return;
		}
		mTvName.setText(product.getName());
		mTvDistance.setText(product.getDistance()+getResources().getString(R.string.km));
		AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvPic, NetEngine.getImageUrl(product.getProductUrl()));
	}
}
