package com.ieeton.agency.activity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.easemob.chat.EMChatConfig;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.cloud.CloudOperationCallback;
import com.easemob.cloud.HttpFileManager;
import com.easemob.util.ImageUtils;
import com.easemob.util.PathUtil;
import com.ieeton.agency.task.LoadLocalBigImgTask;
import com.ieeton.agency.utils.ImageCache;
import com.ieeton.agency.R;

public class ImageDetailFragment extends Fragment {
	private ImageView mImageView;
	private ProgressBar progressBar;
	private PhotoViewAttacher mAttacher;
	
	private Activity mActivity;
	private Bitmap bitmap;
	private ProgressDialog pd;
	private int default_res = R.drawable.docphoto;
	private boolean showAvator;
	private String localFilePath;
	private boolean isDownloaded;
	
	private EMMessage mMsg;
	public static final String IMAGE_DIR = "chat/image/";

	public static ImageDetailFragment newInstance(Activity activity, EMMessage msg) {
		final ImageDetailFragment f = new ImageDetailFragment(activity);
				
//		mMsg = msg;
		final Bundle args = new Bundle();
//		args.putSerializable("msg", (Serializable)msg);
		args.putParcelable("msg", msg);
		f.setArguments(args);

		return f;
	}

	public ImageDetailFragment(Activity activity){
		super();
		mActivity = activity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMsg = getArguments() != null ? (EMMessage)getArguments().getParcelable("msg") : null;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
		mImageView = (ImageView) v.findViewById(R.id.image);
		mAttacher = new PhotoViewAttacher(mImageView);
		
		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {
			
			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2) {
				getActivity().finish();
			}
		});
		
		progressBar = (ProgressBar) v.findViewById(R.id.loading);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		String remotepath = "";
		ImageMessageBody imgBody = (ImageMessageBody) mMsg.getBody();
		String filePath = imgBody.getLocalUrl();
		if (mMsg.direct == EMMessage.Direct.RECEIVE) {
			if (imgBody.getLocalUrl() != null) {
				remotepath = imgBody.getRemoteUrl();
			}
		}else{
			if (filePath != null && new File(filePath).exists()) {
				remotepath = null;
			}else{
				remotepath = IMAGE_DIR;
			}
		}
		
		File file = new File(filePath);

		//本地存在，直接显示本地的图片
		if (file.exists()) {
			System.err.println("showbigimage file exists. directly show it");
			DisplayMetrics metrics = new DisplayMetrics();
			mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			// int screenWidth = metrics.widthPixels;
			// int screenHeight =metrics.heightPixels;
			bitmap = ImageCache.getInstance().get(filePath);
			if (bitmap == null) {
				LoadLocalBigImgTask task = new LoadLocalBigImgTask(mActivity, filePath, mImageView, progressBar, ImageUtils.SCALE_IMAGE_WIDTH,
						ImageUtils.SCALE_IMAGE_HEIGHT);
				if (android.os.Build.VERSION.SDK_INT > 10) {
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					task.execute();
				}
			} else {
				mImageView.setImageBitmap(bitmap);
			}
		} else if(remotepath != null){ //去服务器下载图片
			System.err.println("download remote image");
			Map<String, String> maps = new HashMap<String, String>();
			String accessToken = EMChatManager.getInstance().getAccessToken();
			maps.put("Authorization", "Bearer " + accessToken);
			ImageMessageBody body = (ImageMessageBody)mMsg.getBody();
			String secret = body.getSecret();
			if (!TextUtils.isEmpty(secret)) {
				maps.put("share-secret", secret);
			}
			maps.put("Accept", "application/octet-stream");
			downloadImage(remotepath, maps);
		}else {
			mImageView.setImageResource(default_res);
		}			
	}

	/**
	 * 下载图片
	 * 
	 * @param remoteFilePath
	 */
	private void downloadImage(final String remoteFilePath, final Map<String, String> headers) {
		pd = new ProgressDialog(mActivity);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage("下载图片: 0%");
		pd.show();
		if (!showAvator) {
			if (remoteFilePath.contains("/"))
				localFilePath = PathUtil.getInstance().getImagePath().getAbsolutePath() + "/"
						+ remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
			else
				localFilePath = PathUtil.getInstance().getImagePath().getAbsolutePath() + "/" + remoteFilePath;
		} else {
			if (remoteFilePath.contains("/"))
				localFilePath = PathUtil.getInstance().getImagePath().getAbsolutePath() + "/"
						+ remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
			else
				localFilePath = PathUtil.getInstance().getImagePath().getAbsolutePath() + "/" + remoteFilePath;

		}
		final HttpFileManager httpFileMgr = new HttpFileManager(mActivity, EMChatConfig.getInstance().getStorageUrl());
		final CloudOperationCallback callback = new CloudOperationCallback() {
			public void onSuccess(String resultMsg) {

				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DisplayMetrics metrics = new DisplayMetrics();
						mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
						int screenWidth = metrics.widthPixels;
						int screenHeight = metrics.heightPixels;

						bitmap = ImageUtils.decodeScaleImage(localFilePath, screenWidth, screenHeight);
						if (bitmap == null) {
							mImageView.setImageResource(default_res);
						} else {
							mImageView.setImageBitmap(bitmap);
							ImageCache.getInstance().put(localFilePath, bitmap);
							isDownloaded = true;

						}
						if (pd != null) {
							pd.dismiss();
						}
					}
				});
			}

			public void onError(String msg) {
				Log.e("###", "offline file transfer error:" + msg);
				File file = new File(localFilePath);
				if (file.exists()) {
					file.delete();
				}
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pd.dismiss();
						mImageView.setImageResource(default_res);
					}
				});
			}

			public void onProgress(final int progress) {
				Log.d("ease", "Progress: " + progress);
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pd.setMessage("下载图片: " + progress + "%");
					}
				});
			}
		};

		new Thread(new Runnable() {
			@Override
			public void run() {
				httpFileMgr.downloadFile(remoteFilePath, localFilePath, EMChatConfig.getInstance().APPKEY, headers, callback);
			}
		}).start();

	}
}
