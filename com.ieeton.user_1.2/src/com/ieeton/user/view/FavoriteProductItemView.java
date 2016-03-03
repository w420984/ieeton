package com.ieeton.user.view;

import java.util.concurrent.RejectedExecutionException;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Product;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FavoriteProductItemView extends RelativeLayout implements View.OnClickListener{
	private final int FAVORITE = 1;
	private final int UNFAVORITE = 2;
	
	private Context mContext;
	private Button mBackViewFollow;
	private ImageView mAvatar;
	private TextView mTvName;
	private TextView mTvDescription;
	private TextView mTvPrice;
	private Product mProduct;
	private OperationTask mTask;
	private boolean mIsTaskFree = true;
	private int mProductId;
	
	public FavoriteProductItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public FavoriteProductItemView(Context context, AttributeSet attrs){
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public FavoriteProductItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}
	
	private void initView(){
		if (isInEditMode()) { return; }
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.favorite_product_item_view, this);
		
    	mBackViewFollow = (Button) findViewById(R.id.id_unfavorite);
    	mAvatar = (ImageView) findViewById(R.id.iv_avatar);
    	mTvName = (TextView) findViewById(R.id.tv_name);
    	mTvDescription = (TextView) findViewById(R.id.tv_description);    
    	mTvPrice = (TextView) findViewById(R.id.tv_price);
    	mBackViewFollow.setOnClickListener(this);
	}
	
	public void update(Product product){
		mProduct = product;
		if (product == null){
			return;
		}
		mProductId = product.getId();
		mTvName.setText(mProduct.getName());
		mTvDescription.setText(Html.fromHtml(mProduct.getIntroduce()));
		AsyncBitmapLoader.getInstance().loadBitmap(mContext, mAvatar, 
				NetEngine.getImageUrl(product.getProductionUrl()));
		if (mProduct.getIntegral()>0){
			mTvPrice.setText(mProduct.getIntegral()+mContext.getString(R.string.integral));			
		}else{
			String price = mProduct.getPrice() > 0 ? "¥" + mProduct.getPrice() : mContext.getString(R.string.price_free);
			mTvPrice.setText(price);
		}
		mBackViewFollow.setText(getResources().getString(R.string.unCollect));
	}

	@Override
	public void onClick(View v) {
		if (v == mBackViewFollow){
			if (mProduct == null){
				return;
			}
			if (!mIsTaskFree){
				return;
			}
			mTask = new OperationTask();
			try {
				mTask.execute(UNFAVORITE);
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}			
		}
	}
	
	
	class OperationTask extends AsyncTask<Integer, Void, String>{
		private int oid;
		private Throwable mThr;

		@Override
		protected String doInBackground(Integer... params) {
			oid = params[0];
			String result = "";

			if (oid == FAVORITE){
				try {
					result = NetEngine.getInstance(mContext).favorateProduct(mProductId+"", mProduct.getOwnerUid());
				} catch (PediatricsIOException e) {
					mThr = e;
					e.printStackTrace();
				} catch (PediatricsParseException e) {
					mThr = e;
					e.printStackTrace();
				} catch (PediatricsApiException e) {
					mThr = e;
					e.printStackTrace();
				}
			}else if (oid == UNFAVORITE){
				try {
					result = NetEngine.getInstance(mContext).unFavorateProduct(mProductId+"", mProduct.getOwnerUid());
				} catch (PediatricsIOException e) {
					mThr = e;
					e.printStackTrace();
				} catch (PediatricsParseException e) {
					mThr = e;
					e.printStackTrace();
				} catch (PediatricsApiException e) {
					mThr = e;
					e.printStackTrace();
				}
			}
			return result;
		}

		@Override
		protected void onCancelled() {
			mIsTaskFree = true;
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
			if (TextUtils.isEmpty(result)){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, mContext);
				}else{
					Utils.showToast(mContext, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
			Intent intent = null;
			if (oid == FAVORITE){
				mBackViewFollow.setText(getResources().getString(R.string.unattend));
				intent = new Intent(Constants.ACTION_FAVORITE_PRODUCT);
				Utils.showToast(mContext, getResources().getString(R.string.artical_favorite_success), Toast.LENGTH_SHORT);
			}else if (oid == UNFAVORITE){
				mBackViewFollow.setText(getResources().getString(R.string.addattend));
				intent = new Intent(Constants.ACTION_UNFAVORITE_PRODUCT);
				Utils.showToast(mContext, getResources().getString(R.string.article_unfavorite_success), Toast.LENGTH_SHORT);
			}
			intent.putExtra(Constants.EXTRA_PRODUCTID, mProductId);
			mContext.sendBroadcast(intent);
		}

		@Override
		protected void onPreExecute() {
			mIsTaskFree = false;
			super.onPreExecute();
		}
		
	}
}