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
import com.ieeton.agency.utils.AsyncBitmapLoader;
import com.ieeton.agency.utils.BitmapHelper;
import com.ieeton.agency.utils.BitmapUtils;
import com.ieeton.agency.utils.CommonUtils;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.FileUtils;
import com.ieeton.agency.utils.LoadPictureTask;
import com.ieeton.agency.utils.UpdateProfileTask;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.utils.AsyncBitmapLoader.ImageCallBack;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateInfoActivity extends TemplateActivity {
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
					result = NetEngine.getInstance(UpdateInfoActivity.this)
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
					result = NetEngine.getInstance(UpdateInfoActivity.this).postImageToServer(UpdateInfoActivity.this, upload_url, oriPicPath);
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
					result = NetEngine.getInstance(UpdateInfoActivity.this).getImageUploadResult(token);
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
					Utils.handleErrorEvent(mThr, UpdateInfoActivity.this);
				}else{
					Utils.showToast(UpdateInfoActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
				}
				return;
			}
        	if (mType == TYPE_HEADER){
        		mNewDoctor.setPortraitUrl(result);
        		if (FileUtils.isFileExist(mCertificationPath)){
        			uploadCertification();
        		}else{
        			updateInfo();
        		}
        	}else if (mType == TYPE_CERTIFICATION){
        		mNewDoctor.setCertification(result);
        		updateInfo();
        	}
        }
    }
	
	public static String EXTRA_USERINFO = "extra_userinfo";
	public static String RETURN_DATA = "return_data";
	public static final int REQUEST_NAME = 1;
	public static final int REQUEST_HOSPITAL = 2;
	public static final int REQUEST_DEPARTMENT = 3;
	public static final int REQUEST_TITLE = 4;
	public static final int REQUEST_SKILLED = 5;
	public static final int REQUEST_CAMERA_HEADER = 6;
	public static final int REQUEST_CAMERA_CERTIFICATION = 7;
	public static final int REQUEST_ALBUM_HEADER = 8;
	public static final int REQUEST_ALBUM_CERTIFICATION = 9;
	public static final int REQUEST_PHOTO_CUT = 10;
	
	private static final int   RECOVER_FROM_OOM_MAX_TRIAL = 3;

	private ViewGroup mVgHeader;
	private ViewGroup mVgCertification;
	private ViewGroup mVgPassword;
	private ViewGroup mVgName;
	private ViewGroup mVgHospital;
	private ViewGroup mVgDepartment;
	private ViewGroup mVgTitle;
	private ViewGroup mVgSkilled;
	private ImageView mIvHeader;
	private ImageView mIvCertification;
	private TextView mTvName;
	private TextView mTvHospital;
	private TextView mTvDepartment;
	private TextView mTvTitle;
	private TextView mTvSkilled;
	
	private Doctor mDoctor;
	private Doctor mNewDoctor;
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
			save();
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setView(R.layout.update_info);
		setTitleBar(getString(R.string.back), getString(R.string.reset_my_info), 
				getString(R.string.save));
		Intent intent = getIntent();
		if (intent == null){
			return;
		}
		mDoctor = (Doctor) intent.getExtras().getSerializable(EXTRA_USERINFO);
		if (mDoctor == null){
			return;
		}
		mNewDoctor = new Doctor();
		initView();
	}

	private void initView(){
		mVgHeader = (ViewGroup) findViewById(R.id.rl_header);
		mVgHeader.setOnClickListener(this);
		
		mVgCertification = (ViewGroup) findViewById(R.id.rl_certification);
		mVgCertification.setOnClickListener(this);
		
		mVgPassword = (ViewGroup) findViewById(R.id.rl_reset_password);
		mVgPassword.setOnClickListener(this);
		
		mVgName = (ViewGroup) findViewById(R.id.rl_reset_name);
		mVgName.setOnClickListener(this);
		
		mVgHospital = (ViewGroup) findViewById(R.id.rl_reset_hospital);
		mVgHospital.setOnClickListener(this);
		
		mVgDepartment = (ViewGroup) findViewById(R.id.rl_reset_department);
		mVgDepartment.setOnClickListener(this);
		
		mVgTitle = (ViewGroup) findViewById(R.id.rl_reset_title);
		mVgTitle.setOnClickListener(this);
		
		mVgSkilled = (ViewGroup) findViewById(R.id.rl_reset_skilled);
		mVgSkilled.setOnClickListener(this);
		
		mIvHeader = (ImageView) findViewById(R.id.iv_portrait);
		Bitmap b = AsyncBitmapLoader.getInstance().loadBitmap(this, 
				mDoctor.getID(), NetEngine.getImageUrl(mDoctor.getPortraitUrl()), 
				"doctor", new ImageCallBack() {
					@Override
					public void imageLoad(Bitmap bitmap, Object user) {
						if (bitmap !=null && !bitmap.isRecycled()){
							mIvHeader.setImageBitmap(bitmap);
						}else {
							mIvHeader.setImageResource(R.drawable.docphoto);
						}
					}
				});
		if (b !=null && !b.isRecycled()){
			mIvHeader.setImageBitmap(b);
		}else {
			mIvHeader.setImageResource(R.drawable.docphoto);
		}
		mIvCertification = (ImageView) findViewById(R.id.iv_certification);
		if (!TextUtils.isEmpty(mDoctor.getCertification())){
			LoadPictureTask task =  new LoadPictureTask();
			task.execute(this, 
					NetEngine.getImageUrl(mDoctor.getCertification()),
						mIvCertification);
		}
		mTvName = (TextView) findViewById(R.id.tv_name);
		mTvName.setText(mDoctor.getDoctorName());
		mTvHospital = (TextView) findViewById(R.id.tv_hospital);
		mTvHospital.setText(mDoctor.getHospitalName());
		mTvDepartment = (TextView) findViewById(R.id.tv_department);
		mTvDepartment.setText(mDoctor.getDepartment());
		mTvTitle = (TextView) findViewById(R.id.tv_title);
		mTvTitle.setText(mDoctor.getTitleName());
		mTvSkilled = (TextView) findViewById(R.id.tv_skilled);
		mTvSkilled.setText(mDoctor.getSkillDescription());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mVgHeader){
			SelectPhotoDialog dialog = new SelectPhotoDialog(this, 
					R.style.NoTitleDialog, mHeaderListener);
			dialog.show();
		}else if (v == mVgCertification){
			SelectPhotoDialog dialog = new SelectPhotoDialog(this, 
					R.style.NoTitleDialog, mCertificationListener);
			dialog.show();
		}else if (v == mVgPassword){
			startActivity(new Intent(this, ResetPasswordActivity.class));
		}else if (v == mVgName){
			Intent intent = new Intent(this, EditActivity.class);
			intent.putExtra(EditActivity.EXTRA_MODE, EditActivity.MODE_RESET_NAME);
			intent.putExtra(EditActivity.EXTRA_CONTENT, mDoctor.getDoctorName());
			startActivityForResult(intent, REQUEST_NAME);
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
		}else if (v == mVgSkilled){
			Intent intent = new Intent(this, EditActivity.class);
			intent.putExtra(EditActivity.EXTRA_MODE, EditActivity.MODE_RESET_SKILLED);
			intent.putExtra(EditActivity.EXTRA_CONTENT, mDoctor.getSkillDescription());
			startActivityForResult(intent, REQUEST_SKILLED);
		}
		super.onClick(v);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK){
			return;
		}
//		Utils.logd("data:"+data);
//		if (data == null){
//			return;
//		}
		switch (requestCode){
		case REQUEST_NAME:
			String name = data.getExtras().getString(EditActivity.INPUT_CONTENT);
			mNewDoctor.setDoctorName(name);
			mTvName.setText(name);
			break;
		case REQUEST_SKILLED:
			String skilled = data.getExtras().getString(EditActivity.INPUT_CONTENT);
			mNewDoctor.setSkillDescription(skilled);
			mTvSkilled.setText(skilled);
			break;
		case REQUEST_HOSPITAL:
			CommonItem hospital = (CommonItem) data.getSerializableExtra(RETURN_DATA);
			mTvHospital.setText(hospital.getName());
			mNewDoctor.setHostitalId(hospital.getId());
			break;
		case REQUEST_DEPARTMENT:
			CommonItem department = (CommonItem) data.getSerializableExtra(RETURN_DATA);
			mTvDepartment.setText(department.getName());
			mNewDoctor.setDepartmentId(department.getId());
			break;
		case REQUEST_TITLE:
			CommonItem title = (CommonItem) data.getSerializableExtra(RETURN_DATA);
			mTvTitle.setText(title.getName());
			mNewDoctor.setTitleId(title.getId());
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
	
	private void save(){
		if (!mTaskFree){
			return;
		}
		
		if (FileUtils.isFileExist(mPortraitPath)){
			uploadHeader();
		}else if (FileUtils.isFileExist(mCertificationPath)){
			uploadCertification();
		}else if (!TextUtils.isEmpty(mNewDoctor.getDoctorName()) || 
			!TextUtils.isEmpty(mNewDoctor.getSkillDescription()) ||
			mNewDoctor.getHospitalId() != -1 ||
			mNewDoctor.getDepartmentId() != -1 ||
			mNewDoctor.getTitleId() != -1){
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
		mUpdataTask = new UpdateProfileTask(this, mNewDoctor) {
			
			@Override
			protected void updateEnd(boolean success) {
				mTaskFree = true;
				dismissProgress();
				if (success){
					Utils.showToast(UpdateInfoActivity.this, R.string.update_info_success, 
							Toast.LENGTH_SHORT);
					Intent intent = new Intent(Constants.ACTION_UPDATE_INFO);
					sendBroadcast(intent);
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
