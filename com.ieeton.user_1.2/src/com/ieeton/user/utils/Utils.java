package com.ieeton.user.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;

import com.ieeton.user.R;
import com.ieeton.user.activity.TaskTopActivity;
import com.ieeton.user.exception.PediatricsApiException;
import com.ieeton.user.exception.PediatricsIOException;
import com.ieeton.user.exception.PediatricsParseException;
import com.ieeton.user.models.City;
import com.ieeton.user.models.Settings;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.net.NetUtils;
import com.ieeton.user.net.Reflection;
import com.ieeton.user.view.CustomToast;
import com.umeng.message.PushAgent;
import com.umeng.message.proguard.k.e;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class Utils {

	public static void logd(CharSequence msg) {
		if (!TextUtils.isEmpty(msg) && Constants.DEBUG) {
			Log.d(Constants.TAG, msg.toString());
		}
	}

	public static void loge(CharSequence msg) {
		if (!TextUtils.isEmpty(msg) && Constants.DEBUG) {
			Log.e(Constants.TAG, msg.toString());
		}
	}

	public static void loge(Throwable e) {
		if (e != null && Constants.DEBUG) {
			Log.e(Constants.TAG, "", e);
		}
	}
	
	public static void log_d(String tag, String msg){
		if (Constants.DEBUG){
			Log.d(tag, msg);
		}
	}
	
	public static void exitApp(Context context){
		Intent intent = new Intent();
		intent.setClass(context, TaskTopActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //注意
		context.startActivity(intent);//启动A Activity
		((Activity)context).finish();
	}
	
	/*
	 * 检查该用户当前的头像链接和之前缓存的头像是否一样
	 */
	public static void checkUserPortrait(final Context context, final String id, final String url){
		if (TextUtils.isEmpty(id)
				|| TextUtils.isEmpty(url)){
			return;
		}
		new Thread(){
			@Override
			public void run() {
				File cacheDir=new File(Constants.FOLDER_PORTRAIT); 
				File[]cacheFiles=cacheDir.listFiles(); 
				int i=0; 
				if(null != cacheFiles){ 
					for(;i<cacheFiles.length;i++){
						String name = cacheFiles[i].getName();
						if (name.startsWith(id)){
							String oldPath = Constants.FOLDER_PORTRAIT+name;
							String newPath = getPortraitPath(id, 
									NetEngine.getImageUrl(url));
//							Utils.logd("old path:"+oldPath);
//							Utils.logd("newPath:"+newPath);
							if (!oldPath.equals(newPath)){
								ImageCache.getInstance().remove(id);
								FileUtils.deleteDependon(oldPath);
							}
						}
					} 
				} 
			}
			
		}.start();
	}
	
	public static void saveNickCache(Context context, String uid, String nick){
		//Utils.logd("saveNickCache uid:"+uid);
		//Utils.logd("saveNickCache nick:"+nick);
		if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(nick)){
			return;
		}
		SharedPreferences mySharedPreferences= context.getSharedPreferences(Constants.PREFERENCE_NICK,
				Activity.MODE_PRIVATE); 
		SharedPreferences.Editor editor = mySharedPreferences.edit(); 
		editor.putString(uid, nick);
		editor.commit();
	}

	public static String getNickCache(Context context, String uid){
		if (TextUtils.isEmpty(uid)){
			return "";
		}
		SharedPreferences mySharedPreferences= context.getSharedPreferences(Constants.PREFERENCE_NICK,
				Activity.MODE_PRIVATE); 
		return mySharedPreferences.getString(uid, "");
	}
		
	/*
	 * 根据uid和头像url拼出缓存图片的路径
	 */
	public static String getPortraitPath(String uid, String url){
		if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(url)){
			return null;
		}
		//Utils.logd("url:"+url);
		int start = url.lastIndexOf("/") == -1 ? 0 : url.lastIndexOf("/")+1;
		int end = url.lastIndexOf(".") == -1 ? url.length() : url.lastIndexOf(".");
		String path = Constants.FOLDER_PORTRAIT + uid + "_" + 
				url.substring(start, end);
		Utils.logd("path:"+path);
		return path;
	}
	
	public static String getBigPicPath(String url){
		if (TextUtils.isEmpty(url)){
			return null;
		}
//		Utils.logd("url:"+url);
		String subs = url.indexOf("/upload/") == -1 ? url : url.substring(url.indexOf("/upload/")+8);
		subs = subs.replace("/", "_");
//		int start = url.lastIndexOf("/") == -1 ? 0 : url.lastIndexOf("/")+1;
//		int end = url.lastIndexOf(".") == -1 ? url.length() : url.lastIndexOf(".");
//		String path = Constants.FOLDER_PORTRAIT + url.substring(start, end);
		String path = Constants.FOLDER_PORTRAIT + subs;
//		Utils.logd("path:"+path);
		return path;
	}
	
	/*
	 * 通过评论图片的缩略图获取原图url
	 */
	public static String getCommentOriUrl(String url){
		if (TextUtils.isEmpty(url)){
			return url;
		}
		String oriUrl = url.replace("200200", "");
		return oriUrl;
	}
	
	public static void setWindowHardWareAccelerated(Activity activity) {// 开启Window级别硬件加速
		int flagHardWareAccelerated = getFlagHardWareAccelerated();
		if (flagHardWareAccelerated != -1) {
			activity.getWindow().setFlags(flagHardWareAccelerated,
					flagHardWareAccelerated);
		}
	}

	public static int getFlagHardWareAccelerated() {// 通过反射获得WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
		try {
			Field field = WindowManager.LayoutParams.class
					.getField("FLAG_HARDWARE_ACCELERATED");
			int result = field.getInt(WindowManager.LayoutParams.class); // 0x01000000
			return result;
		} catch (IllegalArgumentException e) {
			Utils.loge(e.getMessage());
		} catch (IllegalAccessException e) {
			Utils.loge(e.getMessage());
		} catch (SecurityException e) {
			Utils.loge(e.getMessage());
		} catch (NoSuchFieldException e) {
			Utils.loge(e.getMessage());
		}
		return -1;
	}

	public static File getUriFile(String filePath, Context context) {
		Uri uri = Uri.parse(filePath);
		File file = null;
		if (uri != null && uri.getScheme() != null
				&& uri.getScheme().equals("content")) {

			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor actualimagecursor = context.getContentResolver().query(uri,
					proj, null, null, null);

			if (actualimagecursor == null) {
				return null;
			}

			int actual_image_column_index = actualimagecursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			if (actualimagecursor.moveToFirst()) {
				String img_path = actualimagecursor
						.getString(actual_image_column_index);
				if (TextUtils.isEmpty(img_path)) {
					return null;
				}
				file = new File(img_path);

				// os4.2编辑直接share时第三方的provider MBAD-2764
				if (file == null || !file.exists()) {
					file = new File(Uri.parse(img_path).getLastPathSegment());
				}
			}
		} else if (uri != null && uri.getScheme() != null
				&& uri.getScheme().equals("file")) {
			file = new File(uri.getPath());
		} else {
			file = new File(filePath);
		}
		return file;
	}

	public static String getUriFilePath(String filePath, Context context) {

		File file = getUriFile(filePath, context);

		if (file != null) {
			return file.getAbsolutePath();
		}

		return null;
	}
	
	/**
	 * 确保指定文件或者文件夹存在
	 * 
	 * @param file_
	 * @return
	 */
	public static boolean makesureFileExist(File file_) {
		if (file_ == null){
			return false;
		}

		if (makesureParentDirExist(file_)) {
			if (file_.isFile()) {
				try {
					return file_.createNewFile();
				} catch (IOException e) {
				}
			} else if (file_.isDirectory()) {
				return file_.mkdir();
			}
		}

		return false;
	}

	/**
	 * 确保指定文件或者文件夹存在
	 * 
	 * @param filePath_
	 * @return
	 */
	public static boolean makesureFileExist(String filePath_) {
		if (TextUtils.isEmpty(filePath_)){
			return false;
		}
		return makesureFileExist(new File(filePath_));
	}

	/**
	 * 确保某文件或文件夹的父文件夹存在
	 * 
	 * @param file_
	 */
	public static boolean makesureParentDirExist(File file_) {
		if (file_ == null){
			return false;
		}
		final File parent = file_.getParentFile();
		if (parent == null || parent.exists())
			return true;
		return mkdirs(parent);
	}
	
	public static boolean mkdirs(File dir) {
		if (dir == null)
			return false;
		return dir.mkdirs();
	}
	

    public static String getAbsolutePath( Context context, Uri uri ) {
        try {
            return Utils.getUriFile( uri.toString(), context ).getAbsolutePath();
        }
        catch( NullPointerException e ) {
        }
        return null;
    }

	public static String getSDPath(){
		File sdDir = null;
	       boolean sdCardExist = Environment.getExternalStorageState()  
	                           .equals(android.os.Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
	       if(sdCardExist)  
	       {               
	         sdDir = Environment.getExternalStorageDirectory();//获取跟目录
	      }  
	       if(sdDir!=null)
	    	   return sdDir.toString();
	       else
	    	   return null;
	}
	
	// 删除文件或者文件夹
    public synchronized static void clearDirectory( File file ) {
        if (file == null)
            return;
        if (!file.exists())
            return;
        
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.exists() && f.canWrite()) {
                        if (f.isDirectory()) {
                            clearDirectory(f);
                        } else if (f.isFile()) {
                                f.delete();
                        }
                    }
                }
            }
        } else if (file.isFile()) {
            if (file.exists() && file.canWrite()) {
                file.delete();
            }
        }
    }
    
    /**
     * 计算文件夹大小
     * @param dir
     * @return
     */
    public static long getCacheSize( File dir ) {
        if (dir == null) {
            return 0;
        }
        if (!dir.isDirectory()) {
            return 0;
        }
        if (!dir.exists()) {
            return 0;
        }
        long dirSize = 0;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                dirSize += file.length();
            } else if (file.isDirectory()) {
                dirSize += getCacheSize(file); // 如果遇到目录则通过递归调用继续统计
            }
        }
        return dirSize;
    }  
	/**
	 * 获取图片指定
	 * @param reflection
	 * @param tag
	 * @param exif
	 * @param defalut
	 * @return
	 */
	
	public static int getTagInt(Reflection reflection,String tag,Object exif,int defalut){
		try{
			String value =(String) reflection.invokeMethod(exif, "getAttribute",
					new Object[] { tag });
			if(value == null){
				return defalut;
			}
			return Integer.valueOf(value);
			
		}catch(Exception e){
			Utils.loge(e);
		}
		return defalut;
	}
	
	public static Object getPicExif(Reflection reflection, String path) {
		Object exifInterface = null;
		try {
			exifInterface = reflection.newInstance(
					"android.media.ExifInterface", new Object[] { path });
		} catch (IOException e) {
			Utils.loge(e);
			exifInterface = null;
		} catch (Exception e) {
			Utils.loge(e);
			exifInterface = null;
		}
		return exifInterface;
	}
			
	public static String getFileNameFromPath(String path){
		if (TextUtils.isEmpty(path)){
			return null;
		}
        int start=path.lastIndexOf("/");  
        int end=path.lastIndexOf(".");  
        if(start!=-1 && end!=-1){  
            return path.substring(start+1,end);    
        }else{  
            return null;  
        }  	          
	}
	
	public static String translationThrowable(Context ctx, Throwable tr) {
		tr = getRootCause(tr);
		if(tr instanceof PediatricsIOException){
			return ctx.getString(R.string.PediatricsIOException);
		}else if (tr instanceof PediatricsApiException) {
            String msg = tr.getMessage();
            /*
             * if (msg.contains(":")) { msg =
             * msg.substring(msg.lastIndexOf(":")+1); }
             */
            String flag = "Reason:";
            if (msg.contains(flag)) {
                msg = msg.substring(msg.indexOf(flag) + flag.length());
            }
            return msg;
        }else if (tr instanceof PediatricsParseException) {
            return ctx.getString(R.string.PediatricsParseException);
        }else {
		    if (tr == null || tr.getMessage() == null) {
		        return ctx.getString(R.string.OthersException);
		    } else {
		    	if (tr.getMessage().contains("failed:")){
					return ctx.getString(R.string.PediatricsIOException);
				}else{
					return tr.getMessage();
				}
			}
		}
	}
	
	/**
	 * 取得异常或者错误的根本原因
	 * 
	 * @param tr
	 * @return
	 */
	public static Throwable getRootCause(Throwable tr) {
		if (tr == null)
			return null;
		Throwable error = null;
		Throwable lastCause, currentCause;
		lastCause = currentCause = tr.getCause();
		while (currentCause != null) {
			lastCause = currentCause;
			currentCause = currentCause.getCause();
		}

		if (lastCause == null) {
			error = tr;
		}
		else {
			error = lastCause;
		}
		return error;
	}
	
    /**
     * 
     * @param error
     * @return 如果error被处理则返回true 否则fasle
     */
    public static boolean handleErrorEvent(Throwable error, final Context ctx){
    	
        final String errorMsg = Utils.translationThrowable( ctx,
                Utils.getRootCause( error ) );
        if(errorMsg.equals("passport is invalid")){
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setTitle(R.string.warning_tile)
					.setMessage(R.string.login_timeout)
					.setCancelable(false)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent successIntent = new Intent(Constants.NEED_RELOGIN_ACTION);
							ctx.sendBroadcast(successIntent);
						}
					})
					.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			AlertDialog dialog = builder.create();
			dialog.show();
        }else if (error instanceof PediatricsApiException){
    		String[] code = ctx.getResources().getStringArray(R.array.error_code);
    		String[] messages = ctx.getResources().getStringArray(R.array.error_message);
    		PediatricsApiException exception = (PediatricsApiException) error;
    		String errorStr = "";
    		if (exception.getErrMessage() != null && isContainChinese(exception.getErrMessage().errmsg)){
    			errorStr = exception.getErrMessage().errmsg;
    		}else {
	    		for(int i=0; i<code.length; i++){
	    			if (exception.getErrMessage() != null && code[i].equals(exception.getErrMessage().errorcode)){
	    				errorStr = messages[i];
	    				break;
	    			}
	    		}
    		}
    		if (!TextUtils.isEmpty(errorStr)){
    			Utils.showToast(ctx, errorStr, Toast.LENGTH_SHORT);
    			return true;
    		}else{
//				if(exception.getErrMessage() != null && "operation_denied".equals(exception.getErrMessage().errorcode) && "doctor mobile is offline".equals(exception.getErrMessage().errmsg)){
//					Utils.showToast(ctx, R.string.doctor_offline, Toast.LENGTH_SHORT);
//				}
				if(exception.getErrMessage() != null && "operation_denied".equals(exception.getErrMessage().errorcode)){
					if(exception.getErrMessage().errmsg != null && !"".equals(exception.getErrMessage().errmsg)){
						Utils.showToast(ctx, exception.getErrMessage().errmsg, Toast.LENGTH_SHORT);
					}
				}
				return true;
    		}
    	}else if(error instanceof NetUtils.NoSignalException){
    		Utils.showToast(ctx, R.string.PediatricsIOException, Toast.LENGTH_LONG);
    	}else{
        	Utils.showToast(ctx, errorMsg, Toast.LENGTH_LONG);
        }
        return true;
    }

    public static Bitmap getBitmapCache(String path, Map<String, Bitmap> map){
        if (TextUtils.isEmpty(path)) {
            return null;
        }
    	
        if (map != null && map.containsKey(path)) {
            Bitmap bmp = (Bitmap) map.get(path);
            if (bmp == null || bmp.isRecycled()) {
            	map.remove(path);
            }
            return bmp;
        }
        return null;
    }
    
    public static void saveBitmapCache(String path, Bitmap bitmap, Map<String, Bitmap> map){
        if (map == null || bitmap == null || bitmap.isRecycled() || TextUtils.isEmpty(path)) {
            return;
        }

        map.put(path, bitmap);
    }
    
    public static void recycleBitmapCache(Map<String, Bitmap> map){
        int size = map.size();
        for (int i = 0; i < size; i++) {
            Bitmap bmp = map.get(i);
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
            }
            bmp = null;
        }
        map.clear();
        map = null;
        System.gc();
	
    }
    public static boolean isEmptyOrBlank( CharSequence str ) {
        return (TextUtils.isEmpty(str)) || (str.toString().trim().length() == 0);
    }
    
	public static void getScreenRect(Context ctx_, Rect outrect_) {
		if (ctx_ == null || outrect_ == null){
			return;
		}
		Display screenSize = ((WindowManager) ctx_
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		outrect_.set(0, 0, screenSize.getWidth(), screenSize.getHeight());
	}
	
	public static int calculateLength(CharSequence c) {  
		int len = 0;  
        for (int i = 0; i < c.length(); i++) {  
            int tmp = (int) c.charAt(i);  
            if (tmp > 0 && tmp < 127) {  
                len += 1;  
            } else {  
                len += 2;  
            }  
        }  
        return len;  
    } 

    private static Object getMetaValue(Context context, String key)
    {
    	Object value = null;

      String packageName = context.getPackageName();
      try
      {
        ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        if (appInfo.metaData != null)
        {
          value = appInfo.metaData.get(key);
        }
      }
      catch (PackageManager.NameNotFoundException e)
      {
        e.printStackTrace();
      }

      return value;
    }
    
    public static String getIeetonFrom(Context context){
		String from = "";
		from = context.getSharedPreferences(Constants.PREFERENCE_FROM, 0).getString(Constants.IEETON_FROM, "");
		if (TextUtils.isEmpty(from)){
			from = (String)getMetaValue(context, Constants.IEETON_FROM);
		}
        return from;
    }
    
	public static void saveIeetonFrom(Context context){
		SharedPreferences preferences = context.getSharedPreferences(Constants.PREFERENCE_FROM, 0);
		if (TextUtils.isEmpty(preferences.getString(Constants.IEETON_FROM, ""))){
			Editor editor = preferences.edit();
			editor.putString(Constants.IEETON_FROM, getIeetonFrom(context));
			editor.commit();
		}
	}
    
    public static String getUmengAppkey(Context context){
        return (String)getMetaValue(context, Constants.UMENG_APPKEY);
    }
    
    public static String getWeixinAppkey(Context context){
        return (String)getMetaValue(context, Constants.WEIXIN_APPKEY);
    }
    
    public static String getWeixinAppSecret(Context context){
        return (String)getMetaValue(context, Constants.WEIXIN_APPSECRET);
    }
    
    public static String getWeiboAppkey(Context context){
        String appid =  (String)getMetaValue(context, Constants.WEIBO_APPKEY);
        appid = appid.substring(5);
    	return String.valueOf(appid);
    }
    
    public static String getQQAppkey(Context context){
        return (String)getMetaValue(context, Constants.QQ_APPKEY);
    }
    
    public static String getQQAppID(Context context){
        String appid =  (String)getMetaValue(context, Constants.QQ_APPID);
        appid = appid.substring(5);
    	return String.valueOf(appid);
    }
    
    public static String getVersion( Context ctx ) throws PackageManager.NameNotFoundException {
        PackageManager pm = ctx.getPackageManager();
        PackageInfo pi = null;
        pi = pm.getPackageInfo(ctx.getPackageName(), 64);
        return pi == null ? null : pi.versionName;
    }
    
    private static String currentVersionCode;
    public static String getVersionCode( Context context ) {
        if (!TextUtils.isEmpty(currentVersionCode)) {
            return currentVersionCode;
        }
        try {
            currentVersionCode = context.getPackageManager().getPackageInfo(Constants.PACKAGE_NAME,
                    PackageManager.GET_CONFIGURATIONS).versionName;
        } catch (NameNotFoundException e) {
            currentVersionCode = "";
        }
        return currentVersionCode;
    }

    /** 
     * 验证手机号格式 
     */ 
    public static boolean isMobileNumber(String mobiles){  
        /* 
		        移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188 
		        联通：130、131、132、152、155、156、185、186 
		        电信：133、153、180、189、（1349卫通） 
		        总结起来就是第一位必定为1，第二位必定为3或5或8(新增7和9，虚拟运营商号码)，其他位置的可以为0-9 
        */  
        String telRegex = "[1][345789]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。  
        if (TextUtils.isEmpty(mobiles)){
        	return false;  
        }else{
        	return mobiles.matches(telRegex);  
        }
    }
    
    /*
     * 验证密码是否符合规则(6-18位字母或数字)
     * 符合返回true，否则返回false
     */
    public static boolean checkPassword(String password){
    	if (TextUtils.isEmpty(password)){
    		return false;
    	}
    	String regex = "[0-9|a-z|A-Z]{6,18}";
    	return password.matches(regex);
    }
    
    public static boolean checkCode(String code){
    	if (TextUtils.isEmpty(code)){
    		return false;
    	}
//    	Utils.logd("length="+code.length());
//    	if (code.length() != 6){
//    		return false;
//    	}
    	String regex = "[0-9]{6}";
    	return code.matches(regex);
    }
    
    public static boolean checkSpecialCharacters(String source){
    	String regEx="[~!@#$%^&*<> ?/+=,\\！。？“”，、]";
    	Pattern p = Pattern.compile(regEx);
    	Matcher m = p.matcher(source);
    	if(m.find()){
    		return true;
    	} 
    	return false;
    }
    
    public static void showToast(final Context context, int resId, int duration){
        Context appContext = context.getApplicationContext();
        Toast.makeText( appContext, resId, duration).show();
    }
    
    public static void showToast(final Context context, CharSequence text, int duration){
        Context appContext = context.getApplicationContext();
        if(TextUtils.isEmpty(text)){
        	return;
        }
        Toast.makeText( appContext, text, duration).show();
    }

	public static LinearLayout createLoadingLayout(int res, Context a) {
		LinearLayout pgLayout = new LinearLayout(a);
		ProgressBar mProgressBar = null;

		pgLayout.setGravity(Gravity.CENTER);
		mProgressBar = new ProgressBar(a);
		mProgressBar.setIndeterminate(false);
		mProgressBar.setIndeterminateDrawable(a.getResources().getDrawable(
				R.drawable.progressbar));
		pgLayout.addView(
				mProgressBar,
				new LinearLayout.LayoutParams(
						a.getResources().getDimensionPixelSize(
								R.dimen.baselayout_title_height), a
								.getResources().getDimensionPixelSize(
										R.dimen.baselayout_title_height)));
		TextView tv = new TextView(a);
		tv.setText(res);
		tv.setTextSize(13);
		tv.setTextColor(a.getResources().getColor(R.color.card_title_text_color));
		pgLayout.addView(tv);

		return pgLayout;
	}
	
	public static CustomToast createProgressCustomToast(int res, Context a) {
		CustomToast ct = new CustomToast(a.getApplicationContext(), res, true);
		return ct;
	}
	
	public static Toast createProgressToast(int res, Context a) {
		Toast toast = new Toast(a);
		LinearLayout pgLayout = createLoadingLayout(res, a);
		toast.setView(pgLayout);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		return toast;
	}

	public static Toast createToast(int res, Context a) {
		Toast toast = null;
		toast = new Toast(a);
		LinearLayout pgLayout = new LinearLayout(a);
		TextView v = new TextView(a);
		((TextView) v).setText(res);
		((TextView) v).setGravity(Gravity.CENTER);
		((TextView) v).setTextSize(13);
		((TextView) v).setPadding(15, 0, 15, 0);
		v.setTextColor(a.getResources().getColor(R.color.toast_text));
		pgLayout.addView(v);

		toast.setView(pgLayout);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		return toast;
	}
	
	public static int getMyType(Context context){
		return context.getSharedPreferences(Constants.MYINFO, 0).getInt(Constants.MY_USERTYPE, 5);
	}
	
	//1医生 2机构 3金牌妈妈 4普通用户 5游客
	public static void setMyType(Context context, int type){
		Editor editor = context.getSharedPreferences(Constants.MYINFO, 0).edit();
		editor.putInt(Constants.MY_USERTYPE, type);
		editor.commit();
	}
	
	public static String getMyUid(Context context){
		String uid = "";
		uid = context.getSharedPreferences(Constants.MYINFO, 0).getString(Constants.MYUID, "");
		return uid;
	}
	
	public static void setMyUid(Context context, String uid){
		if (uid == null){
			uid = "";
		}
		Editor editor = context.getSharedPreferences(Constants.MYINFO, 0).edit();
		editor.putString(Constants.MYUID, uid);
		editor.commit();
	}
			
	public static String getLoginType(Context context){
		String type = "";
		type = context.getSharedPreferences(Constants.LOGIN_TYPE, 0).getString(Constants.LOGIN_TYPE, "");
		return type;
	}
	
	public static void setLoginType(Context context, String type){
		Editor editor = context.getSharedPreferences(Constants.LOGIN_TYPE, 0).edit();
		editor.putString(Constants.LOGIN_TYPE, type);
		editor.commit();
	}
	
	public static int getUserGuideStatus(Context context){
		return context.getSharedPreferences(Constants.USER_GUIDE_SHOW, 0).getInt(Constants.USER_GUIDE_SHOW, 0);
	}
	
	public static void setUserGuideStatus(Context context){
		Editor editor = context.getSharedPreferences(Constants.USER_GUIDE_SHOW, 0).edit();
		editor.putInt(Constants.USER_GUIDE_SHOW, 1);
		editor.commit();
	}
	
	public static City getMyCity(Context context){
		SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MYCITY, 0);
		String name = sharedPreferences.getString(Constants.MYCITY_NAME, "");
		int id = sharedPreferences.getInt(Constants.MYCITY_ID, 0);
		City city = new City(id, name);
		return city;
	}
	
	public static void setMyCity(Context context, City city){
		boolean clear = false;
		if (city == null){
			clear = true;
		}
		if("".equals(city.getCityName())){
			return;
		}
		Editor editor = context.getSharedPreferences(Constants.MYCITY, 0).edit();
		if(!clear){
			editor.putString(Constants.MYCITY_NAME, city.getCityName());
			editor.putInt(Constants.MYCITY_ID, city.getCityID());
		}else{
			editor.clear();
		}
		editor.commit();
	}
	
	public static void setMyLocation(Context context, String name, double la, double lon){
		if (context == null){
			return;
		}
		SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.MY_LOCATION, 0);
		Editor editor = sharedPreferences.edit();
		editor.putString(Constants.LOCATION_CITYNAME, name);
		editor.putFloat(Constants.LOCATION_LA, (float) la);
		editor.putFloat(Constants.LOCATION_LON, (float) lon);
		editor.commit();
	}
	
	public static String getMyLocationCityName(Context context){
		if (context == null){
			return "";
		}
		String name = context.getSharedPreferences(Constants.MY_LOCATION, 0).getString(Constants.LOCATION_CITYNAME, "");
		return name;
	}
	
	public static double getMyLocationLa(Context context){
		double la = context.getSharedPreferences(Constants.MY_LOCATION, 0).getFloat(Constants.LOCATION_LA, 0);
		return la;
	}
	
	public static double getMyLocationLon(Context context){
		double lon = context.getSharedPreferences(Constants.MY_LOCATION, 0).getFloat(Constants.LOCATION_LON, 0);
		return lon;
	}

	public static boolean needBindMobile(Context context){
		return context.getSharedPreferences(Constants.NEED_BIND_MOBILE, 0).
				getBoolean(Constants.NEED_BIND_MOBILE, false);
	}
	
	public static void setNeedBindMobile(Context context, boolean need){
		Editor editor = context.getSharedPreferences(Constants.NEED_BIND_MOBILE, 0).edit();
		editor.putBoolean(Constants.NEED_BIND_MOBILE, need);
		editor.commit();
	}
	
	public static boolean[] getMessageNotifySetting(Context context){
		boolean [] onoff = new boolean[2];
		onoff[0] = context.getSharedPreferences(Constants.SETTINGS_MESSAGE_NOTIFY, 0).getBoolean(Constants.SETTINGS_MESSAGE_NOTIFY_SOUND, true);
		onoff[1] = context.getSharedPreferences(Constants.SETTINGS_MESSAGE_NOTIFY, 0).getBoolean(Constants.SETTINGS_MESSAGE_NOTIFY_VIBRATE, true);
		return onoff;
	}
	
	public static void setMessageNotifySetting(Context context, boolean[] onoff){
		Editor editor = context.getSharedPreferences(Constants.SETTINGS_MESSAGE_NOTIFY, 0).edit();
		editor.putBoolean(Constants.SETTINGS_MESSAGE_NOTIFY_SOUND, onoff[0]);
		editor.putBoolean(Constants.SETTINGS_MESSAGE_NOTIFY_VIBRATE, onoff[1]);
		editor.commit();
	}
	
	public static Settings getSettings(Context context){
		int notify = context.getSharedPreferences(Constants.SETTINGS, 0).getInt(Constants.SETTINGS_MESSAGE_NOTIFY, 0);
		boolean via = context.getSharedPreferences(Constants.SETTINGS, 0).getBoolean(Constants.SETTINGS_VIA_RECEIVER, true);
		boolean cache = context.getSharedPreferences(Constants.SETTINGS, 0).getBoolean(Constants.SETTINGS_AUTO_CLEAR_CACHE, false);//默认关闭自动清理缓存
		Settings settings = new Settings(notify, via, cache);
		return settings;
	}
	
	public static void saveSettings(Context context, Settings settings){
		boolean clear = false;
		if (settings == null){
			clear = true;
		}
		Editor editor = context.getSharedPreferences(Constants.SETTINGS, 0).edit();
		if(!clear){
			Log.v("sereinli","SETTINGS_VIA_RECEIVER:"+settings.getViaLoundSpeaker());
			Log.v("sereinli","SETTINGS_AUTO_CLEAR_CACHE:"+settings.getAutoCleatCache());
			editor.putInt(Constants.SETTINGS_MESSAGE_NOTIFY, settings.getNewMessageNotify());
			editor.putBoolean(Constants.SETTINGS_VIA_RECEIVER, settings.getViaLoundSpeaker());
			editor.putBoolean(Constants.SETTINGS_AUTO_CLEAR_CACHE, settings.getAutoCleatCache());
		}else{
			Log.v("sereinli","SETTINGS_clear");
			editor.clear();
		}
		editor.commit();
	}

	public static Set<String> loadKeyWordList(String cachedir) {
		return (Set<String>) load(cachedir);
	}
	
	public static Object load(String path) {
		makesureFileExist(path);
		Object obj = null;
		File file = new File(path);
		try {
			/*
			 * if(file != null){ file.mkdirs(); }
			 */
			if (file.exists()) {
				FileInputStream fis = new FileInputStream(file);
				ObjectInputStream ois = new ObjectInputStream(fis);
				try {
					obj = ois.readObject();
				} catch (ClassNotFoundException e) {
					loge(e);
				}
				ois.close();
			}
		} catch (IOException e) {
			loge(e);
		}
		return obj;
	}
	
	public static void saveKeyWordList(String cachedir, Set<String> usrnames) {
		cleanKeyWordList(cachedir);
		save(usrnames, cachedir);
	}
	
	private static boolean cleanKeyWordList(String cachedir) {
		return new File(cachedir).delete();
	}
	
	private static void save(Object obj, String path) {
		makesureFileExist(path);
		try {
			File f = new File(path);
			/*
			 * if(f != null){ f.mkdirs(); f.createNewFile(); }
			 */
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			fos.close();
		} catch (IOException e) {
			loge(e);
		}
	}
	
	public static Display getDisplayWidth( Context activity ) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager w = ((Activity) activity).getWindowManager();
        Display display = w.getDefaultDisplay();
        display.getMetrics( metrics );
        return display;
	}
			
	/*
	 * 判断字符串是否包含中文字符
	 */
	public static boolean isContainChinese(String str){
		if (TextUtils.isEmpty(str)){
			return false;
		}
		Pattern pattern=Pattern.compile("[\\u4e00-\\u9fa5]");  
		Matcher matcher=pattern.matcher(str);  
		return matcher.find();		
	}
	
	public static void addUMengAlias(final Context context, String uid){
		if (context == null || TextUtils.isEmpty(uid)){
			return;
		}
		final PushAgent agent = PushAgent.getInstance(context);
		String device_token = agent.getRegistrationId();
        Utils.logd("device_token:"+device_token);
		if (TextUtils.isEmpty(device_token)){
			return;
		}
    	new Thread(){
			@Override
			public void run() {
	        	try {
	        		agent.addAlias(Utils.getMyUid(context), "ieeton_uid");
				} catch (e e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				super.run();
			}    		
    	}.start();
	}
	
	public static Dialog showCallDialog(final Context context, final String number){
		Dialog dialog = new AlertDialog.Builder(context)
				.setTitle("提示")
				.setMessage("确定要拨打电话："+number+"吗?")
				.setPositiveButton("确定", new OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number));
						context.startActivity(intent);
					}
				})
				.setNegativeButton("取消", new OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.create();
		
		dialog.show();
		return dialog;
	}
	
	//微信支付MD5加密算法
	public final static String getMessageDigest(byte[] buffer) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(buffer);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static void subscribe(final Context context){
		new AlertDialog.Builder(context)
			.setMessage(String.format(context.getString(R.string.subscribe_notice), NetEngine.getIvrNumber()))
			.setPositiveButton(context.getString(R.string.call_now), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent=new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+NetEngine.getIvrNumber()));
						context.startActivity(intent);
					}
				})  
			.setNegativeButton(context.getString(R.string.cancel), new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();  
	}
	
	
	public static Bitmap createVideoThumbnail(String url, int width, int height) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		int kind = MediaStore.Video.Thumbnails.MINI_KIND;
		try {
			if (Build.VERSION.SDK_INT >= 14) {
				retriever.setDataSource(url, new HashMap<String, String>());
			} else {
				retriever.setDataSource(url);
			}
			bitmap = retriever.getFrameAtTime();
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
			}
		}
		if (kind == Images.Thumbnails.MICRO_KIND && bitmap != null) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		return bitmap;
	}
}
