package com.ieeton.user.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.net.NetUtils;
import com.ieeton.user.view.RoundedImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

public class AsyncBitmapLoader{ 
	private static AsyncBitmapLoader mInstance;
	private IeetonUser user = null;
	private Context mContext;
	private String mUid;
	private String mUrl;
	private ImageCallBack mCallBack;
	private Task mTask;
	private View mView;

	public AsyncBitmapLoader(){ 
	} 
	
	public static AsyncBitmapLoader getInstance(){
//		if (mInstance == null){
			mInstance = new AsyncBitmapLoader();
//		}
		return mInstance;
	}
	
	public Bitmap loadBitmap(Context context, View view, final String url){
		mContext = context;
		mView = view;
		mUrl = url;
		mView.setTag(mUrl);
//		Utils.logd("mView:"+mView + "---  imageUrl:"+url);
		
		Bitmap bitmap = null;
		bitmap = ImageCache.getInstance().get(url);
		if(bitmap != null){
			//Utils.logd("ImageCache OK");
			if (mView instanceof ImageView){
				((ImageView) mView).setImageBitmap(bitmap);
			}else if (mView instanceof RoundedImageView){
				((RoundedImageView) mView).setImageBitmap(bitmap);
			}
			return bitmap;
		}else{
			try{
				LoadPicByUrlTask task = new LoadPicByUrlTask();
				task.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public Bitmap loadBitmap(Context context, final String url, final ImageCallBack imagecallback){
		mContext = context;
		mUrl = url;
		mCallBack = imagecallback;
		
		Bitmap bitmap = null;
		bitmap = ImageCache.getInstance().get(url);
		if(bitmap != null){
			
			return bitmap;
		}else{
			try{
				LoadPicByUrlTask task = new LoadPicByUrlTask();
				task.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public Bitmap loadBitmap(Context context, final String uid, 
			final String imageURL, final View view,
			final ImageCallBack imageCallBack){
		//Utils.logd("loadBitmap");
		mContext = context;
		mUid = uid;
		mUrl = imageURL;
		mView = view;
		mCallBack = imageCallBack;
		mView.setTag(mUid);
		
		if (ImageCache.getInstance().get(uid) != null){
			if (mView.getTag().equals(mUid)){
				if (mView instanceof ImageView){
					((ImageView) mView).setImageBitmap(ImageCache.getInstance().get(uid));
				}else if (mView instanceof RoundedImageView){
					((RoundedImageView) mView).setImageBitmap(ImageCache.getInstance().get(uid));
				}				
			}
			return ImageCache.getInstance().get(uid);
		}
		
		try{
			mTask = new Task();
			mTask.execute();
		}catch(RejectedExecutionException e){
			e.printStackTrace();
		}
		return null; 
	} 
	
	public interface ImageCallBack{ 
		public void imageLoad(Bitmap bitmap, IeetonUser user); 
	} 
	
	public static class HttpUtils{ 
		public static InputStream getStreamFromURL(String imageURL){ 
			InputStream in=null; 
			try{ 
				URL url=new URL(imageURL); 
				HttpURLConnection connection=(HttpURLConnection)url.openConnection(); 
				in=connection.getInputStream(); 
			}catch(Exception e){ 
				e.printStackTrace(); 
			} 
			return in; 
		} 
	} 

	class Task extends AsyncTask<Void, Void, Bitmap>{

		@Override
		protected Bitmap doInBackground(Void... params) { 
			/** 
			*加上一个对本地缓存的查找 
			*/ 
			Bitmap portrait_bitmap = null;
			File cacheDir=new File(Constants.FOLDER_PORTRAIT); 
			File[]cacheFiles=cacheDir.listFiles(); 
			int i=0; 
			if(null != cacheFiles){ 
				for(;i<cacheFiles.length;i++){
					String name = cacheFiles[i].getName();
					if (name.startsWith(mUid)){
						Bitmap bitmap = BitmapFactory.decodeFile(Constants.FOLDER_PORTRAIT + 
								name);
						if (bitmap != null){
							ImageCache.getInstance().put(mUid,bitmap); 
							//Utils.logd("loadBitmap filecache ok");
							return bitmap;
						}
					}
				} 
			} 
			/*
			 * 如果内存缓存和本地文件都没有，则从网络获取
			 */
			String url = "";
			//如果传进来的url为空，则根据uid获取昵称和头像地址
			String rlt = "";
			try {
				rlt = NetEngine.getInstance(mContext).getUserInfo(mUid); 
				JSONObject obj = new JSONObject(rlt);
				user = new IeetonUser(mContext, obj);
			} catch (PediatricsIOException e) {
				e.printStackTrace();
			} catch (PediatricsParseException e) {
				e.printStackTrace();
			} catch (PediatricsApiException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (user == null || TextUtils.isEmpty(user.getAvatar())){
				Utils.logd("loadBitmap url null");
				return null;
			}
			url = NetEngine.getImageUrl(user.getAvatar());
			if (TextUtils.isEmpty(url)){
				Utils.logd("loadBitmap url null");
				return null;
			}
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream(NetUtils.getImageStream(url));
			} catch (Exception e) {
				Utils.logd("loadBitmap network failed");
				e.printStackTrace();
				return null;
			}
			if (bitmap == null){
				Utils.logd("loadBitmap network failed");
				return null;
			}
			ImageCache.getInstance().put(mUid,bitmap); 
			//Utils.logd("loadBitmap network ok");
			
			
			File dir=new File(Constants.FOLDER_PORTRAIT); 
			if(!dir.exists()){ 
				dir.mkdirs(); 
			} 
			//Utils.logd("file:"+Utils.getPortraitPath(mUid, url));
			File bitmapFile=new File(Utils.getPortraitPath(mUid, url));
			if(!bitmapFile.exists()){ 
				try{ 
					bitmapFile.createNewFile(); 
				}catch(IOException e){ 
					e.printStackTrace(); 
				} 
			} 
			FileOutputStream fos; 
			try{ 
				fos=new FileOutputStream(bitmapFile); 
				bitmap.compress(Bitmap.CompressFormat.PNG, 100,fos); 
				fos.close(); 
			}catch(FileNotFoundException e){ 
				e.printStackTrace(); 
			}catch(IOException e){ 
				e.printStackTrace(); 
			} 
			return bitmap;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}


		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null && mView != null){
				if (mView.getTag().equals(mUid)){
					if (mView instanceof ImageView){
						((ImageView) mView).setImageBitmap(result);
					}else if (mView instanceof RoundedImageView){
						((RoundedImageView) mView).setImageBitmap(result);
					}
				}
			}
			if (mCallBack != null){
				mCallBack.imageLoad(result, user);
			}
		}
		
	}
	
	class LoadPicByUrlTask extends AsyncTask<Void, Void, Bitmap>{

		@Override
		protected Bitmap doInBackground(Void... params) { 
			/** 
			*加上一个对本地缓存的查找 
			*/ 
			File cacheDir=new File(Constants.FOLDER_PORTRAIT); 
			File[]cacheFiles=cacheDir.listFiles(); 
			int i=0; 
			if(null != cacheFiles){ 
//				int start = mUrl.lastIndexOf("/") == -1 ? 0 : mUrl.lastIndexOf("/")+1;
//				int end = mUrl.lastIndexOf(".") == -1 ? mUrl.length() : mUrl.lastIndexOf(".");
//				String file_name = mUrl.substring(start, end);
				String subs = mUrl.indexOf("/upload/") == -1 ? mUrl : mUrl.substring(mUrl.indexOf("/upload/")+8);
				String file_name = subs.replace("/", "_");
				for(;i<cacheFiles.length;i++){
					String name = cacheFiles[i].getName();
					if (name.startsWith(file_name)){
						Bitmap bitmap = BitmapFactory.decodeFile(Constants.FOLDER_PORTRAIT + 
								name);
						//Utils.logd("FileCache OK");
						ImageCache.getInstance().put(mUrl,bitmap);
						return bitmap;
					}
				} 
			} 

			/*
			 * 如果内存缓存和本地文件都没有，则从网络获取
			 */
			String url = mUrl;
			if (TextUtils.isEmpty(url)){
				return null;
			}
			Bitmap bitmap = null;
			try {
				bitmap = BitmapFactory.decodeStream(NetUtils.getImageStream(url));
			} catch (Exception e) {
				Utils.logd("loadBitmap network failed");
				e.printStackTrace();
				return null;
			}
			if (bitmap == null){
				Utils.logd("loadBitmap network failed");
				return null;
			}
			ImageCache.getInstance().put(mUrl,bitmap);
			
			
			File dir=new File(Constants.FOLDER_PORTRAIT); 
			if(!dir.exists()){ 
				dir.mkdirs(); 
			} 
//			int start = url.lastIndexOf("/") == -1 ? 0 : url.lastIndexOf("/")+1;
//			int end = url.lastIndexOf(".") == -1 ? url.length() : url.lastIndexOf(".")-1;
//			String path = Constants.FOLDER_PORTRAIT + url.substring(start, end);
			String path = Utils.getBigPicPath(url);
			File bitmapFile=new File(path);
			if(!bitmapFile.exists()){ 
				try{ 
					bitmapFile.createNewFile(); 
				}catch(IOException e){ 
					e.printStackTrace(); 
				} 
			} 
			FileOutputStream fos; 
			try{ 
				fos=new FileOutputStream(bitmapFile); 
				bitmap.compress(Bitmap.CompressFormat.PNG, 100,fos); 
				fos.close(); 
			}catch(FileNotFoundException e){ 
				e.printStackTrace(); 
			}catch(IOException e){ 
				e.printStackTrace(); 
			} 
			//Utils.logd("NetImage OK");
			return bitmap;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}


		@Override
		protected void onPostExecute(Bitmap result) {
			//Utils.logd("mView:"+mView + "---  mView.getTag():"+mView.getTag());
			if (result != null && mView != null){
				if (mUrl.equals(mView.getTag())){
					if (mView instanceof ImageView){
						((ImageView) mView).setImageBitmap(result);
					}else if (mView instanceof RoundedImageView){
						((RoundedImageView) mView).setImageBitmap(result);
					}
				}
			}
			if (mCallBack != null){
				mCallBack.imageLoad(result, null);
			}
		}
		
	}
} 


