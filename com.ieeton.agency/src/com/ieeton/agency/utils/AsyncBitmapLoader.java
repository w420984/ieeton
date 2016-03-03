package com.ieeton.agency.utils;

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

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.ChatUser;
import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.models.Patient;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.net.NetUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;

public class AsyncBitmapLoader{ 
	private static AsyncBitmapLoader mInstance;
	private Object user = null;
	private Context mContext;
	private String mUid;
	private String mUrl;
	private String mType;
	private ImageCallBack mCallBack;
	private Task mTask;

	/** 
	*内存图片软引用缓冲 
	*/ 
//	private HashMap<String,SoftReference<Bitmap>> imageCache=null; 
	public AsyncBitmapLoader() 
	{ 
//		imageCache=new HashMap<String,SoftReference<Bitmap>>(); 
	} 
	
	public static AsyncBitmapLoader getInstance(){
//		if (mInstance == null){
			mInstance = new AsyncBitmapLoader();
//		}
		return mInstance;
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
			final String imageURL, final String type,
			final ImageCallBack imageCallBack){
		Utils.logd("loadBitmap");
		mContext = context;
		mUid = uid;
		mUrl = imageURL;
		mType = type;
		mCallBack = imageCallBack;
		
		//在内存缓存中，则返回Bitmap对象 
		if (ImageCache.getInstance().get(uid) != null){
			Utils.logd("loadBitmap SoftReference ok");
			return ImageCache.getInstance().get(uid); 
		}else{
			try{
				mTask = new Task();
				mTask.execute();
			}catch(RejectedExecutionException e){
				e.printStackTrace();
			}
		}
		return null; 
	} 
	
	public interface ImageCallBack{ 
		public void imageLoad(Bitmap bitmap, Object user); 
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
			boolean got_nick = false;
			boolean got_portrait = false;
			Bitmap portrait_bitmap = null;
			String nick = Utils.getNickCache(mContext, mUid);
			if(nick != null && !"".equals(nick)){
				got_nick = true;
			}
			File cacheDir=new File(Constants.FOLDER_PORTRAIT); 
			File[]cacheFiles=cacheDir.listFiles(); 
			int i=0; 
			if(null != cacheFiles){ 
				for(;i<cacheFiles.length;i++){
					String name = cacheFiles[i].getName();
					if (name.startsWith(mUid)){
						Bitmap bitmap = BitmapFactory.decodeFile(Constants.FOLDER_PORTRAIT + 
								name);
						ImageCache.getInstance().put(mUid,bitmap); 
						Utils.logd("loadBitmap filecache ok");
						got_portrait = true;
						if(got_nick && got_portrait){
							return bitmap;
						}else{
							portrait_bitmap = bitmap;
							break;
						}
					}
				} 
			} 

			/*
			 * 如果内存缓存和本地文件都没有，则从网络获取
			 */
			String url = "";
			if (TextUtils.isEmpty(mUrl)){
				//如果传进来的url为空，则根据uid或者昵称和头像地址
				String rlt = "";
				try {
					if (mType.equals(ChatUser.USER_DOCTOR)){
						rlt = NetEngine.getInstance(mContext).getDoctorInfo(Utils.getPassport(mContext), 
								mUid);
					}else if (mType.equals(ChatUser.USER_PATIENT)){
						rlt = NetEngine.getInstance(mContext).GetPatientInfo(Utils.getPassport(mContext),
								mUid);
					}else if (mType.equals(ChatUser.USER_PUBLIC) || mType.equals(ChatUser.USER_HUANXIN)){
						rlt = NetEngine.getInstance(mContext).getNickPortrait(ChatUser.USER_HUANXIN, 
								mUid);
					}
					JSONObject obj = new JSONObject(rlt);
					if (!obj.optBoolean("error")){
						JSONObject data = obj.optJSONObject("messages").optJSONObject("data");
						if (mType.equals(ChatUser.USER_DOCTOR)){
							Doctor doctor = new Doctor(mContext, data.optJSONObject("doctor"));
							url = NetEngine.getImageUrl(doctor.getPortraitUrl());
							if (!TextUtils.isEmpty(doctor.getDoctorName())){
								Utils.saveNickCache(mContext, mUid, doctor.getDoctorName());
							}
							user = doctor;
						}else if (mType.equals(ChatUser.USER_PATIENT)){
							Patient patient = new Patient(mContext, data.optJSONObject("patient"));
							url = NetEngine.getImageUrl(patient.getPortraitUrl());
							if (!TextUtils.isEmpty(patient.getNick())){
								Utils.saveNickCache(mContext, mUid, patient.getNick());
							}
							user = patient;
						}else if (mType.equals(ChatUser.USER_PUBLIC) || mType.equals(ChatUser.USER_HUANXIN)){
							JSONObject usr = data.optJSONObject("user");
							ChatUser item = new ChatUser(mContext, usr);
							Utils.saveNickCache(mContext, mUid, item.getName());
							url = NetEngine.getImageUrl(item.getAvatar());
							user = item;
						}
						got_nick = true;
					}
				} catch (PediatricsIOException e) {
					e.printStackTrace();
				} catch (PediatricsParseException e) {
					e.printStackTrace();
				} catch (PediatricsApiException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else{
				url = mUrl;
			}
			if(got_nick && got_portrait && portrait_bitmap != null && !portrait_bitmap.isRecycled()){
				return portrait_bitmap;
			}
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
			Utils.logd("loadBitmap network ok");
			
			
			File dir=new File(Constants.FOLDER_PORTRAIT); 
			if(!dir.exists()){ 
				dir.mkdirs(); 
			} 
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
			mCallBack.imageLoad(result, user);
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
				int start = mUrl.lastIndexOf("/") == -1 ? 0 : mUrl.lastIndexOf("/")+1;
				int end = mUrl.lastIndexOf(".") == -1 ? mUrl.length() : mUrl.lastIndexOf(".")-1;
				String file_name = mUrl.substring(start, end);
				for(;i<cacheFiles.length;i++){
					String name = cacheFiles[i].getName();
					if (name.startsWith(file_name)){
						Bitmap bitmap = BitmapFactory.decodeFile(Constants.FOLDER_PORTRAIT + 
								name);
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
			return bitmap;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}


		@Override
		protected void onPostExecute(Bitmap result) {
			mCallBack.imageLoad(result, null);
		}
		
	}
} 


