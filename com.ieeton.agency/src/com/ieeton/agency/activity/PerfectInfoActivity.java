package com.ieeton.agency.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.easemob.util.PathUtil;
import com.ieeton.agency.DemoApplication;
import com.ieeton.agency.activity.SelectPhotoDialog.ButtonClickListener;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.CommonItem;
import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.BitmapHelper;
import com.ieeton.agency.utils.BitmapUtils;
import com.ieeton.agency.utils.CommonUtils;
import com.ieeton.agency.utils.FileUtils;
import com.ieeton.agency.utils.UpdateProfileTask;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.R;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PerfectInfoActivity extends TemplateActivity {
	private class UploadPhotoTask extends AsyncTask<Void, Void, String>{
		public static final int TYPE_HEADER = 1;
		public static final int TYPE_CERTIFICATION = 2;
		
		public UploadPhotoTask(int type){
			mType = type;
		}
    	private Throwable mThr;
    	private int mType;
    	private String oriPicPath;
    	
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
			showProgress();
			mTaskFree = false;
        }

        @Override
        protected String doInBackground( Void... params ) {
            if ( !isCancelled() ) {
            	if (mType == TYPE_HEADER){
            		oriPicPath = mPortraitPath;
            	}else if (mType == TYPE_CERTIFICATION){
            		oriPicPath = mCertificationPath;
            	}
                //上传图片
                //①获取图片属性信息
    			String result = "";
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
    			   
    			int MAX = 160;
    			//②获取上传url地址
				try {
					result = NetEngine.getInstance(PerfectInfoActivity.this)
									.getImageUploadUrl(MAX, MAX, oriPicPath, s);
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
				Log.v("sereinli","upload url:"+result);
				int start = result.indexOf('(');
				int end = result.indexOf(')');
				if(end > start){
					result = result.substring(start, end);
				}
				Log.v("sereinli","after upload url:"+result);
				JSONObject object = null;
				String upload_url = "";
				String token = "";
				try {
					object = new JSONObject(result);
					if(!object.optBoolean("error")){
						JSONObject json_data = object.optJSONObject("messages").optJSONObject("data");
						token = json_data.optString("token");
						upload_url = json_data.optString("uploadUrl");
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
                //③上传图片
				try {
					result = NetEngine.getInstance(PerfectInfoActivity.this).postImageToServer(PerfectInfoActivity.this, upload_url, oriPicPath);
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
				Log.v("sereinli","upload post img result:"+result);
				//③查询进度
//				try {
//					result = NetEngine.getInstance(EditProfileActivity.this).getImageUploadProgress(upload_url);
//				} catch (PediatricsIOException e) {
//					e.printStackTrace();
//					mThr = e;
//				} catch (PediatricsParseException e) {
//					e.printStackTrace();
//					mThr = e;
//				} catch (PediatricsApiException e) {
//					e.printStackTrace();
//					mThr = e;
//				}
//				Log.v("sereinli","upload progress result:"+result);

				//④查询结果
				try {
					result = NetEngine.getInstance(PerfectInfoActivity.this).getImageUploadResult(token);
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
				Log.v("sereinli","upload result:"+result);
				
				//⑤解析图片上传地址
				try {
					object = new JSONObject(result);
					if(!object.optBoolean("error")){
						JSONObject json_data = object.optJSONObject("messages").optJSONObject("data");
						JSONArray array = json_data.optJSONArray("files");
						boolean isWidthMax = false;
						for(int i=0; i<array.length(); i++){
							JSONArray json_images = ((JSONObject)array.get(i)).optJSONArray("images");
							for(int j=0; j<json_images.length(); j++){
								JSONObject item = (JSONObject)json_images.get(j);
								int width = item.optInt("width");
								int height = item.optInt("height");
								if (j == 0){
									isWidthMax = width > height ? true : false;
								}
								
								if (isWidthMax){
									if (height == MAX){
										result = item.optString("url");
										Utils.logd("height result:"+result);
										break;
									}
								}else {
									if (width == MAX){
										result = item.optString("url");
										Utils.logd("width result:"+result);
										break;
									}
								}
								result = ((JSONObject)json_images.get(0)).optString("url");
								Log.v("sereinli", "new portrait url:"+ result);
							}
						}
					}

				} catch (JSONException e) {
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
            mTaskFree = true; 
        }

        @Override
        protected void onPostExecute( String result ) {
        	mTaskFree = true;
        	dismissProgress();
        	if (result == null || result.equals("")){
				if(mThr != null){
					Utils.handleErrorEvent(mThr, PerfectInfoActivity.this);
				}else{
					Utils.showToast(PerfectInfoActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
        	if (mType == TYPE_HEADER){
        		mDoctor.setPortraitUrl(result);
        		if (FileUtils.isFileExist(mCertificationPath)){
        			uploadCertification();
        		}else{
        			updateInfo();
        		}
        	}else if (mType == TYPE_CERTIFICATION){
        		mDoctor.setCertification(result);
        		updateInfo();
        	}
        }
    }
	
	public static final int REQUEST_HOSPITAL = 1;
	public static final int REQUEST_DEPARTMENT = 2;
	public static final int REQUEST_TITLE = 3;
	public static final int REQUEST_CAMERA_HEADER = 4;
	public static final int REQUEST_CAMERA_CERTIFICATION = 5;
	public static final int REQUEST_ALBUM_HEADER = 6;
	public static final int REQUEST_ALBUM_CERTIFICATION = 7;
	public static final int REQUEST_PHOTO_CUT = 8;

	private static final int   RECOVER_FROM_OOM_MAX_TRIAL = 3;

	private ImageView mIvHeader;
	private ImageView mIvCertification;
	private EditText mEtName;
	private EditText mEtSkilled;
	private Button mBtnSubmit;
	private ViewGroup mVgHospital;
	private ViewGroup mVgDepartment;
	private ViewGroup mVgTitle;
	private TextView mTvHospital;
	private TextView mTvDepartment;
	private TextView mTvTitle;
	private CheckBox mCheckBox;
	private TextView mTvAgreement;
	
	private Doctor mDoctor;
	private File mPortraitFile;
	private File mCertificationFile;
	private String mPortraitPath;
	private String mCertificationPath;
	private CustomToast mProgressDialog;
	private UploadPhotoTask mTask;
	private UpdateProfileTask mUpdataTask;
	private boolean mTaskFree = true;
	
	private ButtonClickListener mHeaderListener = new ButtonClickListener() {
		@Override
		public void onButtonClick(int tag) {
			if (tag == SelectPhotoDialog.BUTTON_CAMERA){
				selectPicFromCamera(REQUEST_CAMERA_HEADER);
			}else if (tag == SelectPhotoDialog.BUTTON_ALBUM){
				selectPicFromLocal(REQUEST_ALBUM_HEADER);
			}
		}
	};
	private ButtonClickListener mCertificationListener = new ButtonClickListener() {
		@Override
		public void onButtonClick(int tag) {
			if (tag == SelectPhotoDialog.BUTTON_CAMERA){
				selectPicFromCamera(REQUEST_CAMERA_CERTIFICATION);
			}else if (tag == SelectPhotoDialog.BUTTON_ALBUM){
				selectPicFromLocal(REQUEST_ALBUM_CERTIFICATION);
			}
		}
	};
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			
			break;
		case LEFT_BUTTON:
			Utils.exitApp(this);
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.activity_perfect_info);
		setTitleBar(getString(R.string.back), getString(R.string.perfect_info), null);
	
		initView();
	}

	private void initView(){
		mIvHeader = (ImageView) findViewById(R.id.iv_header);
		mIvHeader.setOnClickListener(this);
		
		mIvCertification = (ImageView) findViewById(R.id.iv_certificate);
		mIvCertification.setOnClickListener(this);
		
		mEtName = (EditText) findViewById(R.id.et_true_name);
		mEtSkilled = (EditText) findViewById(R.id.et_skilled);
		
		mTvHospital = (TextView) findViewById(R.id.tv_hospital);
		mVgHospital = (ViewGroup) findViewById(R.id.rl_select_hospital);
		mVgHospital.setOnClickListener(this);
		
		mTvDepartment = (TextView) findViewById(R.id.tv_department);
		mVgDepartment = (ViewGroup) findViewById(R.id.rl_select_department);
		mVgDepartment.setOnClickListener(this);
		
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mVgTitle = (ViewGroup) findViewById(R.id.rl_select_title);
		mVgTitle.setOnClickListener(this);
		
		mBtnSubmit = (Button) findViewById(R.id.btn_submit);
		mBtnSubmit.setOnClickListener(this);
		
		mCheckBox = (CheckBox) findViewById(R.id.checkbox);
		mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mBtnSubmit.setClickable(isChecked);
				if (isChecked){
					mBtnSubmit.setBackgroundResource(R.drawable.button);
				}else {
					mBtnSubmit.setBackgroundResource(R.drawable.button_s);
				}
			}
		});
		
		mTvAgreement = (TextView) findViewById(R.id.tv_agreement);
		mTvAgreement.setOnClickListener(this);
		
		mDoctor = new Doctor();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			Utils.exitApp(this);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		if (v == mIvHeader){
			SelectPhotoDialog dialog = new SelectPhotoDialog(this, 
					R.style.NoTitleDialog, mHeaderListener);
			dialog.show();
		}else if (v == mIvCertification){
			SelectPhotoDialog dialog = new SelectPhotoDialog(this, 
					R.style.NoTitleDialog, mCertificationListener);
			dialog.show();
		}else if (v == mVgHospital){
			Intent intent = new Intent(this, CommonListActivity.class);
			intent.putExtra(CommonListActivity.EXTRA_MODE, CommonListActivity.MODE_PROVINCE);
			intent.putExtra(CommonListActivity.EXTRA_PARENTID, 1);
			startActivityForResult(intent, REQUEST_HOSPITAL);
		}else if (v == mVgDepartment){
			Intent intent = new Intent(this, CommonListActivity.class);
			intent.putExtra(CommonListActivity.EXTRA_MODE, CommonListActivity.MODE_DEPARTMENT);
			startActivityForResult(intent, REQUEST_DEPARTMENT);
		}else if (v == mVgTitle){
			Intent intent = new Intent(this, CommonListActivity.class);
			intent.putExtra(CommonListActivity.EXTRA_MODE, CommonListActivity.MODE_TITLE);
			startActivityForResult(intent, REQUEST_TITLE);
		}else if (v == mBtnSubmit){
			submit();
		}else if (v == mTvAgreement){
			startActivity(new Intent(this, AgreementActivity.class));
		}
		super.onClick(v);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK){
			return;
		}
		switch (requestCode){
		case REQUEST_HOSPITAL:
			CommonItem hospital = (CommonItem) data.getSerializableExtra(UpdateInfoActivity.RETURN_DATA);
			mDoctor.setHostitalId(hospital.getId());
			mTvHospital.setText(hospital.getName());
			break;
		case REQUEST_DEPARTMENT:
			CommonItem department = (CommonItem) data.getSerializableExtra(UpdateInfoActivity.RETURN_DATA);
			mTvDepartment.setText(department.getName());
			mDoctor.setDepartmentId(department.getId());
			break;
		case REQUEST_TITLE:
			CommonItem title = (CommonItem) data.getSerializableExtra(UpdateInfoActivity.RETURN_DATA);
			mTvTitle.setText(title.getName());
			mDoctor.setTitleId(title.getId());
			break;
		case REQUEST_CAMERA_HEADER:
			if (mPortraitFile != null && mPortraitFile.exists()){
				//mPortraitPath = mPortraitFile.getAbsolutePath();
				//asyncLoadPreviewBitmap(mIvHeader, mPortraitPath);
				crop(Uri.fromFile(mPortraitFile));
			}
			break;
		case REQUEST_CAMERA_CERTIFICATION:
			if (mCertificationFile != null && mCertificationFile.exists()){
				mCertificationPath = mCertificationFile.getAbsolutePath();
				asyncLoadPreviewBitmap(mIvCertification, mCertificationPath);
			}
			break;
		case REQUEST_ALBUM_HEADER:
			if (data != null) {
                // 得到图片的全路径
                Uri uri = data.getData();
                crop(uri);
            }
			break;
		case REQUEST_ALBUM_CERTIFICATION:
			processAlbumData(data, requestCode);
			break;
		case REQUEST_PHOTO_CUT:
			// 从剪切图片返回的数据
            if (data != null) {
                Bitmap bitmap = data.getParcelableExtra("data");
                mIvHeader.setImageBitmap(bitmap);
                BitmapUtils.compressToFile(mPortraitFile, bitmap, Bitmap.CompressFormat.JPEG);
                mPortraitPath = mPortraitFile.getAbsolutePath();
            }
            break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/*
     * 剪切图片
     */
    private void crop(Uri uri) {
        // 裁剪图片意图
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // 裁剪框的比例，1：1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // 裁剪后输出图片的尺寸大小
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
 
        intent.putExtra("outputFormat", "JPEG");// 图片格式
        intent.putExtra("noFaceDetection", true);// 取消人脸识别
        intent.putExtra("return-data", true);
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CUT
        startActivityForResult(intent, REQUEST_PHOTO_CUT);
    }

	private void processAlbumData(Intent data, int requestCode){
		Uri selectedImage = data.getData();
		if (selectedImage != null) {
			Cursor cursor = getContentResolver().query(selectedImage, null, null, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex("_data");
				String picturePath = cursor.getString(columnIndex);
				cursor.close();
				cursor = null;

				if (picturePath == null || picturePath.equals("null")) {
					Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return;
				}
				if (requestCode == REQUEST_ALBUM_HEADER){
					mPortraitPath = picturePath;
					asyncLoadPreviewBitmap(mIvHeader, mPortraitPath);
				}else if (requestCode == REQUEST_ALBUM_CERTIFICATION){
					mCertificationPath = picturePath;
					asyncLoadPreviewBitmap(mIvCertification, mCertificationPath);
				}
			} else {
				File file = new File(selectedImage.getPath());
				if (!file.exists()) {
					Toast toast = Toast.makeText(this, "找不到图片", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					return;

				}
				if (requestCode == REQUEST_ALBUM_HEADER){
					mPortraitPath = file.getAbsolutePath();
					asyncLoadPreviewBitmap(mIvHeader, mPortraitPath);
				}else if (requestCode == REQUEST_ALBUM_CERTIFICATION){
					mCertificationPath = file.getAbsolutePath();
					asyncLoadPreviewBitmap(mIvCertification, mCertificationPath);
				}
			}
		}
	}
	
	/**
	 * 照相获取图片
	 */
	public void selectPicFromCamera(int requestCode) {
		if (!CommonUtils.isExitsSdcard()) {
			Utils.showToast(this, "SD卡不存在，不能拍照", Toast.LENGTH_SHORT);
			return;
		}
		File file = new File(PathUtil.getInstance().getImagePath(), DemoApplication.getInstance().getUserName()
				+ System.currentTimeMillis() + ".jpg");
		if (file != null && file.getParent() != null){
			file.getParentFile().mkdirs();
		}
		if (requestCode == REQUEST_CAMERA_HEADER){
			mPortraitFile = file;
		}else if (requestCode == REQUEST_CAMERA_CERTIFICATION){
			mCertificationFile = file;
		}
		startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file)),
				requestCode);
	}

	/**
	 * 从图库获取图片
	 */
	public void selectPicFromLocal(int requestCode) {
		Intent intent;
		if (Build.VERSION.SDK_INT < 19) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");

		} else {
			intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		
		if (requestCode == REQUEST_ALBUM_HEADER){
			File file = new File(PathUtil.getInstance().getImagePath(), DemoApplication.getInstance().getUserName()
					+ System.currentTimeMillis() + ".jpg");
			if (file != null && file.getParent() != null){
				file.getParentFile().mkdirs();
			}
			mPortraitFile = file;
		}
		startActivityForResult(intent, requestCode);
	}
	
	private void asyncLoadPreviewBitmap(final ImageView view, final String path) {
        try {
            new AsyncTask<Void, Void, Bitmap>() {
            	
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Bitmap doInBackground( Void... params ) {
                	Bitmap bitmap = null;
                    if ( !isCancelled() ) {
                        final String oriPicPath = path;        				
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
                                //recycleBitmap( mOriginPreviewBitmap );
                            }
                        }

                    }
                    return bitmap;
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                }

                @Override
                protected void onPostExecute( Bitmap bitmap ) {
                    if(bitmap != null && !bitmap.isRecycled()){
                    	view.setImageBitmap(bitmap);
                    }
                }
            }.execute();
        }
        catch( RejectedExecutionException e ) {
            Utils.loge( e );
        }
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
	
	private void showProgress(){
		if (mProgressDialog == null){
			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, this);
		}
		mProgressDialog.show();
	}
	
	private void dismissProgress(){
		if (mProgressDialog != null){
			mProgressDialog.cancel();
		}
	}
	
	private void submit(){
		if (!FileUtils.isFileExist(mPortraitPath)
				|| !FileUtils.isFileExist(mCertificationPath)
				|| TextUtils.isEmpty(mEtName.getText().toString())
//				|| TextUtils.isEmpty(mEtSkilled.getText().toString())
//				|| mDoctor.getHospitalId() == -1
//				|| mDoctor.getDepartmentId() == -1
//				|| mDoctor.getTitleId() == -1
				){
			Utils.showToast(this, R.string.info_not_complete, Toast.LENGTH_SHORT);
			return;
		}
		
		if (Utils.checkSpecialCharacters(mEtName.getText().toString())){
			Utils.showToast(this, R.string.nick_containt_special_character, Toast.LENGTH_SHORT);
			return;
		}
		
		if (!mCheckBox.isChecked()){
			return;
		}
		
		if (!mTaskFree){
			return;
		}
		mDoctor.setDoctorName(mEtName.getText().toString());
//		mDoctor.setSkillDescription(mEtSkilled.getText().toString());
		
		if (FileUtils.isFileExist(mPortraitPath)){
			uploadHeader();
		}else if (FileUtils.isFileExist(mCertificationPath)){
			uploadCertification();
		}else if (!TextUtils.isEmpty(mDoctor.getDoctorName()) 
//			|| !TextUtils.isEmpty(mDoctor.getSkillDescription()) 
//			|| mDoctor.getHospitalId() != -1 
//			|| mDoctor.getDepartmentId() != -1 
//			|| mDoctor.getTitleId() != -1
			){
				updateInfo();
		}else{
		}
	}
	
	private void uploadHeader(){
		mTask = new UploadPhotoTask(UploadPhotoTask.TYPE_HEADER);
		try {
			mTask.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private void uploadCertification(){
		mTask = new UploadPhotoTask(UploadPhotoTask.TYPE_CERTIFICATION);
		try {
			mTask.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private void updateInfo(){
		mUpdataTask = new UpdateProfileTask(this, mDoctor, UpdateProfileTask.MODE_PERFECT) {
			
			@Override
			protected void updateEnd(boolean success) {
				mTaskFree = true;
				dismissProgress();
				if (success){
					Utils.showToast(PerfectInfoActivity.this, R.string.update_info_success, 
							Toast.LENGTH_SHORT);
					Utils.setNeedPerfectInfo(PerfectInfoActivity.this, false);					
					Intent intent = new Intent(PerfectInfoActivity.this, LoginActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
				}
			}
			
			@Override
			protected void updateCancel() {
				mTaskFree = true;
				dismissProgress();
			}
			
			@Override
			protected void updateBegin() {
				mTaskFree = false;
				showProgress();
			}
		};
		try {
			mUpdataTask.execute();
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
}
