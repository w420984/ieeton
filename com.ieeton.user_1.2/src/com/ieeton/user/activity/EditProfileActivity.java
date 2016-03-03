package com.ieeton.user.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.easemob.util.PathUtil;
import com.ieeton.user.IeetonApplication;
import com.ieeton.user.R;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import com.ieeton.user.utils.BitmapHelper;
import com.ieeton.user.utils.BitmapUtils;
import com.ieeton.user.utils.CommonUtils;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.utils.ImageSizeUtils.UploadImageUtils;
import com.ieeton.user.view.RoundedImageView;
import com.ieeton.user.view.SelectPhotoDialog;
import com.ieeton.user.view.SliderSwitchView;
import com.ieeton.user.view.SliderSwitchView.OnChangedListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EditProfileActivity extends TemplateActivity implements OnClickListener, OnChangedListener{
	public static final int REQUEST_CODE_SELECT_FILE = 0;
	public static final int REQUEST_CODE_CAMERA = 1;
	public static final int REQUEST_CODE_LOCAL = 2;
	public static final int REQUEST_CODE_CUT_PICTURE = 3;
	
	public static final int PROFILE_PORTRAIT_WIDTH = 160;
	public static final int PROFILE_PORTRAIT_HEIGHT = 160;
	
	private RelativeLayout mEditPortraitLayout;
	private RelativeLayout mSelectCityLayout;
	private RelativeLayout mChangePswLayout;
	private RelativeLayout mSetAgeLayout;
	private RelativeLayout mSetLableLayout;
//	private RelativeLayout mEditNickNameLayout;
	private File mPortraitFile;
	private String mPortraitPath;
	private RoundedImageView mPortrait;
	private TextView mCityTextView;
	private EditText mNickName;
	private SliderSwitchView mSwitchDistinction;
	private Bitmap             mOriginPreviewBitmap;
	private GenPortraitImageTask mCurrentTask               = null;
	private static final int   RECOVER_FROM_OOM_MAX_TRIAL = 3;
	private IeetonUser mUser;
	private boolean mSaveTaskFree = true;
	private static final int EDIT_PROFILE_CITY = 100;
	private int mGender;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_edit_profile);
		setTitleBar(getString(R.string.back), getString(R.string.my_info), 
				getString(R.string.save));
		
		mPortraitPath = null;
		
		Intent intent = getIntent();
		if (intent == null){
			finish();
			return;
		}
		mUser = (IeetonUser)intent.getSerializableExtra(Constants.EXTRA_USER);
		if (mUser == null){
			finish();
			return;
		}
		mUser.getUid();
		mGender = mUser.getGender();
		
		mEditPortraitLayout = (RelativeLayout)findViewById(R.id.rl_edit_portrait);
		mEditPortraitLayout.setOnClickListener(this);
		
		mChangePswLayout = (RelativeLayout)findViewById(R.id.rl_edit_password);
		mChangePswLayout.setOnClickListener(this);

		mSetAgeLayout = (RelativeLayout)findViewById(R.id.rl_edit_age);
		mSetAgeLayout.setOnClickListener(this);

		mSetLableLayout = (RelativeLayout)findViewById(R.id.rl_edit_lable);
		mSetLableLayout.setOnClickListener(this);
		
//		mEditNickNameLayout = (RelativeLayout)findViewById(R.id.rl_edit_nickname);
//		mEditNickNameLayout.setOnClickListener(this);
				
		mPortrait = (RoundedImageView)findViewById(R.id.portrait);
		
		mSwitchDistinction = (SliderSwitchView)findViewById(R.id.switch_distinction);
		mSwitchDistinction.setOnChangedListener(this);
		
		mSelectCityLayout = (RelativeLayout)findViewById(R.id.rl_edit_city);
		mSelectCityLayout.setOnClickListener(this);
		mCityTextView = (TextView)findViewById(R.id.tv_city);
		mCityTextView.setText(Utils.getMyCity(EditProfileActivity.this).getCityName());

		mNickName = (EditText)findViewById(R.id.tv_nick_name);

//		if(mUser.getGender() == 1){
//        	mPortrait.setImageResource(R.drawable.userphoto_male);
//        }else{
//        	mPortrait.setImageResource(R.drawable.userphoto_female);
//      }

		AsyncBitmapLoader.getInstance().loadBitmap(this, Utils.getMyUid(this), 
				NetEngine.getImageUrl(mUser.getAvatar()), mPortrait, null);
		
		if(mUser.getGender() == 0){
			mSwitchDistinction.setChecked(false);
		}else{
			mSwitchDistinction.setChecked(true);
		}
		
		mNickName.setText(mUser.getName());

	}

	@Override
	public void onClick(View v) {
		if(v ==  mEditPortraitLayout){
			showPop();
		}else if(v == mSelectCityLayout){
			Intent intent = new Intent(EditProfileActivity.this, SelectCityActivity.class);
			intent.putExtra(SelectCityActivity.MODE, SelectCityActivity.MODE_EDIT_PROFILE);
			startActivityForResult(intent, EDIT_PROFILE_CITY);
		}else if(v == mChangePswLayout){
			startActivity(new Intent(EditProfileActivity.this, ResetPasswordActivity.class));
		}else if (v == mSetAgeLayout){
			Intent intent = new Intent(this, AgeLableListActivity.class);
			intent.putExtra(Constants.EXTRA_MODE, AgeLableListActivity.MODE_AGE);
			startActivity(intent);
		}else if (v == mSetLableLayout){
			Intent intent = new Intent(this, AgeLableListActivity.class);
			intent.putExtra(Constants.EXTRA_MODE, AgeLableListActivity.MODE_LABLE);
			startActivity(intent);
		}
//		else if(v == mEditNickNameLayout){
//			Intent intent = new Intent(EditProfileActivity.this, EditNickNameActivity.class);
//			intent.putExtra("nick", mAccount.getNickName());
//			startActivityForResult(intent, EDIT_PROFILE_NICK);
//		}
		super.onClick(v);
	}
	
	void saveProfile(){
		if(!mSaveTaskFree){
			return;
		}
		try{
			UploadAndSaveProfileTask task = new UploadAndSaveProfileTask();
			task.execute();
		}catch(RejectedExecutionException e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onDestroy() {
		if( mCurrentTask != null ) {
            mCurrentTask.cancel( false );
            mCurrentTask = null;
        }
		super.onDestroy();
	}

	private void showPop(){
		SelectPhotoDialog dialog = new SelectPhotoDialog(this, R.style.NoTitleDialog,
				new SelectPhotoDialog.ButtonClickListener() {
			
			@Override
			public void onButtonClick(int tag) {
				if (tag == SelectPhotoDialog.BUTTON_CAMERA){
					selectPicFromCamera();
				}else if (tag == SelectPhotoDialog.BUTTON_ALBUM){
					selectPicFromLocal();
				}
			}
		});
		dialog.show();
	}
	
	/**
	 * 照相获取图片
	 */
	public void selectPicFromCamera() {
		if (!CommonUtils.isExitsSdcard()) {
			Toast.makeText(getApplicationContext(), "SD卡不存在，不能拍照", Toast.LENGTH_SHORT).show();
			return;
		}

		mPortraitFile = new File(PathUtil.getInstance().getImagePath(), IeetonApplication.getInstance().getUserName()
				+ System.currentTimeMillis() + ".jpg");
		mPortraitFile.getParentFile().mkdirs();
		startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPortraitFile)),
				REQUEST_CODE_CAMERA);
	}
	
	/**
	 * 从图库获取图片
	 */
	public void selectPicFromLocal() {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");

		} else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, REQUEST_CODE_LOCAL);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CODE_CAMERA) {
				if (mPortraitFile != null && mPortraitFile.exists()){
					mPortraitPath = mPortraitFile.getAbsolutePath();
					startPhotoZoom(Uri.fromFile(mPortraitFile));
				}
			}else if (requestCode == REQUEST_CODE_LOCAL) { // 发送本地图片
				
				if (data != null) {
					startPhotoZoom(data.getData());
				}
			}else if(requestCode == EDIT_PROFILE_CITY){
//				if(data != null){
//					mCity = (City) data.getExtras().getSerializable("city");
//					String name = mCity.getCityName();
//					if(name != null && !name.equals("")){
//						mCityTextView.setText(mCity.getCityName());
//						mAccount.setRegionId(mCity.getCityID());
//					}
//				}
			}else if(requestCode == REQUEST_CODE_CUT_PICTURE){
				if(data != null){
					setPicToView(data);
				}
			}
		}
	}

	class GenPortraitImageTask extends AsyncTask<Void, Void, Bitmap>{           	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
			showProgress();
        }

        @Override
        protected Bitmap doInBackground( Void... params ) {
        	Bitmap bitmap = null;
            if ( !isCancelled() ) {
                final String oriPicPath = Constants.FOLDER_PORTRAIT + "portrait.jpg";
                File destfile = new File(oriPicPath);
                if(destfile.exists()){
                	destfile.delete();
                }
                try {
					destfile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
                UploadImageUtils.revitionPostImageSize(EditProfileActivity.this, mPortraitPath, oriPicPath);
                // to recover from OOM error, try 3 times
                for( int i = 0; i < RECOVER_FROM_OOM_MAX_TRIAL; ++i ) {
                    int ratio = (int) Math.pow( 2, i );
                    try {
                        bitmap = loadPreviewBitmap( getApplication(), 
                                oriPicPath, ratio );
                        
                        //mResultPreviewBitmap = generateResultBitmap( mOriginPreviewBitmap, 0, 0 );
                        break;
                    }
                    catch( OutOfMemoryError e ) {
                        e.printStackTrace();
                        recycleBitmap( mOriginPreviewBitmap );
                    }
                }

            }
            return bitmap;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dismissProgress();
            mCurrentTask = null;
        }

        @Override
        protected void onPostExecute( Bitmap bitmap ) {
        	dismissProgress();
            if(bitmap != null && !bitmap.isRecycled()){
            	mPortrait.setImageBitmap(bitmap);
            }
            mCurrentTask = null;
        }
    }
	
    private void asyncLoadPreviewBitmap() {
    	if(mCurrentTask != null){
    		return;
    	}
        try {
            mCurrentTask = new GenPortraitImageTask();
            mCurrentTask.execute();
        }
        catch( RejectedExecutionException e ) {
            Utils.loge( e );
        }
    }
    
    private static void recycleBitmap( Bitmap bmp ) {
        BitmapUtils.recycleBitmap( bmp );
    }

    private static Bitmap loadPreviewBitmap( Context context, String imagePath, int rateRatio ) {
        if ( imagePath == null || imagePath.length() == 0  ) {
            return null;
        }

        // screen size
        Rect screenSize = new Rect();
        Utils.getScreenRect( context, screenSize );
        File bmpFile = new File( imagePath );
        // bitmap size
        Rect size = new Rect();
        BitmapUtils.getZoomOutBitmapBound( bmpFile, 1, size );
        int rate = BitmapHelper.getSampleSizeAutoFitToScreen( screenSize.width(),
                screenSize.height(), size.width(), size.height() );

        // not use BitmapUtils.createZoomOutBitmap for some device has something wrong when working
        // with BufferedInputStream, e.g. LG610s
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = rate * rateRatio;
        return BitmapFactory.decodeFile( imagePath, opts );
    }

	@Override
	public void OnChanged(SliderSwitchView sliderSwitch, boolean checkState) {
		if(sliderSwitch == mSwitchDistinction){
			if(checkState){
				mGender = 1;
			}else{
				mGender = 0;
			}
		}
		
	}
			
	private class UploadAndSaveProfileTask extends AsyncTask<Void, Void, String>{
    	private Throwable mThr;
		String newAvatar = "";
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSaveTaskFree = false;
			showProgress();
        }

        @Override
        protected String doInBackground( Void... params ) {
            if ( !isCancelled() ) {
                final String oriPicPath = mPortraitPath;
    			String result = "";

    			//修改过图片后才需要上传，否则直接更新用户信息
                if(!TextUtils.isEmpty(oriPicPath)){
                    //上传图片
                    //①获取图片属性信息
        			File src = new File(oriPicPath);
        			FileInputStream fis = null;
        			long s = 0;
        			   try {
        				fis = new FileInputStream(src);
        				s = fis.available();
        				if(fis != null){
        					fis.close();
        				}
        			} catch (FileNotFoundException e1) {
        				e1.printStackTrace();
        				return null;
        			} catch (IOException e) {
        				e.printStackTrace();
        				return null;
        			}
        			   
        			BitmapFactory.Options options = new BitmapFactory.Options();  
        			options.inJustDecodeBounds = true;

    				try {
    					result = NetEngine.getInstance(EditProfileActivity.this).postImageToServer(EditProfileActivity.this, 1501, oriPicPath);
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
						object = new JSONObject(result);
						newAvatar = object.optString("url");
						if (TextUtils.isEmpty(newAvatar)){
							return null;
						}
					} catch (JSONException e1) {
						e1.printStackTrace();
						return null;
					}
                }
				try {
					result = NetEngine.getInstance(EditProfileActivity.this).updateInfo(newAvatar, mNickName.getText().toString(), mGender);
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
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dismissProgress();
        }

        @Override
        protected void onPostExecute( String result ) {
        	mSaveTaskFree = true;
        	dismissProgress();
        	if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, EditProfileActivity.this);
				}else{
					Utils.showToast(EditProfileActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}

        	Utils.showToast(EditProfileActivity.this, getString(R.string.edit_info_success), Toast.LENGTH_SHORT);
			Intent intent = new Intent();
			if (!TextUtils.isEmpty(newAvatar)){
				mUser.setAvatar(newAvatar);
				Utils.checkUserPortrait(EditProfileActivity.this, mUser.getUid(), newAvatar);
			}
			mUser.setName(mNickName.getText().toString());
			mUser.setGender(mGender);
			intent.putExtra(Constants.EXTRA_USER, mUser);
			setResult(RESULT_OK, intent);
        	finish();

        }		
	}
	
	/**
	 * 裁剪图片方法实现
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", PROFILE_PORTRAIT_WIDTH);
		intent.putExtra("outputY", PROFILE_PORTRAIT_HEIGHT);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, REQUEST_CODE_CUT_PICTURE);
	}
	
	/**
	 * 保存裁剪之后的图片数据
	 * 
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap bitmap = extras.getParcelable("data");
			
			mPortraitFile = new File(PathUtil.getInstance().getImagePath(), IeetonApplication.getInstance().getUserName()
					+ System.currentTimeMillis() + "_cut.jpg");
			mPortraitFile.getParentFile().mkdirs();
			if(mPortraitFile.exists()){
				mPortraitFile.delete();
			}
			
			try{
				FileOutputStream out = new FileOutputStream(mPortraitFile);
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.flush();
				out.close();
				mPortraitPath = mPortraitFile.getAbsolutePath();
				asyncLoadPreviewBitmap();
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			if (TextUtils.isEmpty(mPortraitPath) 
					&& mUser.getName().equals(mNickName.getText().toString())
					&& mGender == mUser.getGender()){
				Toast.makeText(EditProfileActivity.this, getString(R.string.profile_not_modify), Toast.LENGTH_SHORT).show();
				return;
			}
			if(Utils.checkSpecialCharacters(mNickName.getText().toString())){
				Utils.showToast(this, R.string.nick_containt_special_character, Toast.LENGTH_SHORT);
				return;
			}
			if(Utils.calculateLength(mNickName.getText().toString()) < Constants.MIN_NICKNAME_LENGTH 
					|| Utils.calculateLength(mNickName.getText().toString()) > Constants.MAX_NICKNAME_LENGTH){
				Toast.makeText(this, R.string.nick_hint, Toast.LENGTH_SHORT).show();
				return;
			}
			saveProfile();
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}
}
