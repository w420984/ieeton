package com.ieeton.user.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.Product;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.BitmapHelper;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.FileUtils;
import com.ieeton.user.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AddCommentActivity extends TemplateActivity {
	public static final int REQUEST_ALBUM = 1;
	public static final int REQUEST_CAMERA = 2;

	private TextView mTvName;
	private TextView mTvPrice;
	private ImageView mIvProductIcon;
	private List<ImageView> mStarList;
	private EditText mEtContent;
	private FrameLayout mFlCamera;
	private List<ImageView> mPicList;
	private TextView mTvSend;
	
	private Product mProduct;
	private Uri mCameraPicUri;
	private List<String> mPics;
	private CommentTask mTask;
	private boolean mIsTaskFree = true;
	private int mLevel = 5;
	private String mOrderId;
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mFlCamera){
			showPop();
		}else if (v == mTvSend){
			if (TextUtils.isEmpty(mEtContent.getText().toString())){
				Utils.showToast(this, "您还没输入任何内容哦", Toast.LENGTH_SHORT);
				return;
			}
			if (mIsTaskFree){
				mTask = new CommentTask();
				mTask.execute();
			}
		}else{
			for(int i=0; i<mStarList.size(); i++){
				if (v == mStarList.get(i)){
					setStars(i);
					break;
				}
			}
		}
		super.onClick(v);
	}

	@Override
	protected void onResume() {
		setPicGroup();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING){
			mTask.cancel(true);
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mPics == null){
			mPics = new ArrayList<String>();
		}
		switch (requestCode) {
		case REQUEST_ALBUM:
			if (resultCode == RESULT_OK && data != null){
//				if (mPics != null){
//					mPics.addAll((ArrayList<String>)(data.getSerializableExtra(PhotoAlbumActivity.RETURN_DATA)));
//				}
				copyPic2IeetonPath((ArrayList<String>)(data.getSerializableExtra(PhotoAlbumActivity.RETURN_DATA)));
			}			
			break;
		case REQUEST_CAMERA:
            String orgPicPath = Utils.getAbsolutePath( this, mCameraPicUri );
			if (resultCode == Activity.RESULT_CANCELED){
                FileUtils.deleteDependon( orgPicPath );
				return;
			}
			if (FileUtils.doesExisted(orgPicPath)){
				mPics.add(orgPicPath);
			}else if( data != null &&  data.getData() != null) {
				mCameraPicUri = data.getData();
				orgPicPath = Utils.getAbsolutePath( this, mCameraPicUri );
				mPics.add(orgPicPath);
            }
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_comment);
		setTitleBar(getString(R.string.back), getString(R.string.comment), null);
		
		mProduct = (Product) getIntent().getSerializableExtra(Constants.EXTRA_PRODUCT);
		mOrderId = getIntent().getStringExtra(Constants.EXTRA_ORDERID);
		if (mProduct == null){
			finish();
			return;
		}
		
		initView();
	}

	private void initView(){
		mTvName = (TextView) findViewById(R.id.tv_name);
		mTvName.setText(mProduct.getName());
		mTvPrice = (TextView) findViewById(R.id.tv_price);
		if (mProduct.getIntegral()>0){
			mTvPrice.setText(mProduct.getIntegral()+getString(R.string.integral));			
		}else{
			String price = mProduct.getPrice() > 0 ? "¥" + mProduct.getPrice() : getString(R.string.price_free);
			mTvPrice.setText(price);
		}
		mIvProductIcon = (ImageView) findViewById(R.id.iv_product_icon);
		AsyncBitmapLoader.getInstance().loadBitmap(this, mIvProductIcon, NetEngine.getImageUrl(mProduct.getProductionUrl()));

		mStarList = new ArrayList<ImageView>();
		mStarList.add((ImageView) findViewById(R.id.iv_star1));
		mStarList.add((ImageView) findViewById(R.id.iv_star2));
		mStarList.add((ImageView) findViewById(R.id.iv_star3));
		mStarList.add((ImageView) findViewById(R.id.iv_star4));
		mStarList.add((ImageView) findViewById(R.id.iv_star5));
		for(ImageView view : mStarList){
			view.setOnClickListener(this);
		}
		mEtContent = (EditText) findViewById(R.id.et_content);
		mFlCamera = (FrameLayout) findViewById(R.id.fl_camera);
		mFlCamera.setOnClickListener(this);
		mPicList = new ArrayList<ImageView>();
		mPicList.add((ImageView) findViewById(R.id.iv_pic1));
		mPicList.add((ImageView) findViewById(R.id.iv_pic2));
		mPicList.add((ImageView) findViewById(R.id.iv_pic3));
		mTvSend = (TextView) findViewById(R.id.tv_comment);
		mTvSend.setOnClickListener(this);
		setStars(4);
	}
	
	private void setStars(int index){
		mLevel = index+1;
		for(ImageView view : mStarList){
			view.setImageResource(R.drawable.comment_star_n);
		}
		for(int i=0; i<=index; i++){
			mStarList.get(i).setImageResource(R.drawable.comment_star);
		}
	}
	
	private void setPicGroup(){
		if (mPics == null || mPics.isEmpty()){
			return;
		}
		for(int i=0; i<mPics.size(); i++){
			mPicList.get(i).setVisibility(View.VISIBLE);
			setImageView(mPics.get(i), mPicList.get(i));
		}
	}
	
	private void setImageView(String path, ImageView view){
		if (view == null || TextUtils.isEmpty(path)){
			return;
		}
		
		int width = getResources().getDimensionPixelSize(R.dimen.add_comment_pic_height);
		Bitmap bitmap = BitmapHelper.getBitmapFromFile(path, width, 
				width, true, true);
		if (bitmap != null && !bitmap.isRecycled()){
			view.setImageBitmap(bitmap);
		}else{
			view.setVisibility(View.GONE);
		}
	}
	
	private void showPop(){
		Button camera;
		Button album;
		Button cancel;
		
		final Dialog dialog = new Dialog(this, R.style.NoTitleDialog);
		LayoutInflater li=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View v=li.inflate(R.layout.add_pic_pop_layout, null);
        camera = (Button)v.findViewById(R.id.camera);
        album = (Button)v.findViewById(R.id.album);
        cancel = (Button)v.findViewById(R.id.cancel);
        dialog.setContentView(v);
		Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);

        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.4); 
        p.width = (int) d.getWidth(); 
        dialogWindow.setAttributes(p);
 
        camera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (FileUtils.hasSDCardMounted()){
		            String picUri = Constants.FOLDER_PORTRAIT + System.currentTimeMillis() + ".jpg";
		            mCameraPicUri = Uri.fromFile( new File( picUri ) );
		            FileUtils.makesureParentExist( picUri );
		            
		            try{
			            Intent i = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
			            i.putExtra( MediaStore.EXTRA_OUTPUT, mCameraPicUri );
			            startActivityForResult( i, REQUEST_CAMERA );
		            }catch(ActivityNotFoundException e){
						Utils.showToast(AddCommentActivity.this, R.string.camera_not_allowed, Toast.LENGTH_SHORT);
						e.printStackTrace();
		            }

				}else{
					Utils.showToast(AddCommentActivity.this, R.string.pls_insert_sdcard, Toast.LENGTH_SHORT);
				}
				dialog.dismiss();
			}
		});
        album.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AddCommentActivity.this, PhotoAlbumActivity.class);
				int num=3;
				if (mPics != null){
					num = 3-mPics.size();
				}
				intent.putExtra(PhotoAlbumActivity.PARAM_DATA_SELECT_NUMBER, num);
				startActivityForResult(intent, REQUEST_ALBUM);
				dialog.dismiss();
			}
		});
        cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
        
        dialog.show();
	}
	
	private void copyPic2IeetonPath(List<String> list){
		if (list == null || list.isEmpty()){
			return;
		}
		for (int i=0; i<list.size(); i++){
			if (list.get(i).startsWith(Constants.FOLDER_PORTRAIT)){
				mPics.add(list.get(i));
				continue;
			}
			String path = Constants.FOLDER_PORTRAIT + Utils.getMyUid(this)
					 + "_" + System.currentTimeMillis() + ".jpg";
			try {
				FileUtils.copy(list.get(i), path);
				mPics.add(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private List<String> mUploadPicList;
	private class CommentTask extends AsyncTask<Void, Void, String>{
    	private Throwable mThr;

		@Override
		protected String doInBackground(Void... params) {
			String result = "";
			for(int i=0; mPics!=null&&i<mPics.size(); i++){
				try {
					result = NetEngine.getInstance(AddCommentActivity.this).
							postImageToServer(AddCommentActivity.this, 1505, mPics.get(i));
				} catch (PediatricsIOException e) {
					e.printStackTrace();
					mThr = e;
				} catch (PediatricsParseException e) {
					e.printStackTrace();
					mThr = e;
				} catch (PediatricsApiException e) {
					e.printStackTrace();
					mThr = e;
				}
				JSONObject object;
				
				if (TextUtils.isEmpty(result)){
					return null;
				}
				try {
					if (mUploadPicList == null){
						mUploadPicList = new ArrayList<String>();
					}
					object = new JSONObject(result);
					String pic = object.optString("url");
					if (TextUtils.isEmpty(pic)){
						return null;
					}
					mUploadPicList.add(pic);
				} catch (JSONException e1) {
					e1.printStackTrace();
					return null;
				}
			}
			try {
				result = NetEngine.getInstance(AddCommentActivity.this).addComment(mOrderId, mProduct.getOwnerUid(), 
						mProduct.getId()+"", mEtContent.getText().toString(), mLevel, mUploadPicList); 
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

            return result;
		}

		@Override
		protected void onCancelled() {
			mIsTaskFree = true;
			dismissProgress();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			mIsTaskFree = true;
        	dismissProgress();
        	if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, AddCommentActivity.this);
				}else{
					Utils.showToast(AddCommentActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}

        	Intent intent = new Intent(AddCommentActivity.this, SuccessActivity.class);
			intent.putExtra(Constants.EXTRA_MODE, SuccessActivity.MODE_COMMENT_SUCCESS);
        	startActivity(intent);
        	finish();

        }

		@Override
		protected void onPreExecute() {
			showProgress();
			mIsTaskFree = false;
			super.onPreExecute();
		}
		
	}
}
