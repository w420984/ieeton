package com.ieeton.agency.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ieeton.agency.activity.TaskTopActivity;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.City;
import com.ieeton.agency.models.Patient;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.net.Reflection;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
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
		//((Activity)context).finish();
		//android.os.Process.killProcess(android.os.Process.myPid());
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
		//Utils.logd("getNickCache uid:"+uid);
		if (TextUtils.isEmpty(uid)){
			return "";
		}
		SharedPreferences mySharedPreferences= context.getSharedPreferences(Constants.PREFERENCE_NICK,
				Activity.MODE_PRIVATE); 
		//Utils.logd("getNickCache nick:"+mySharedPreferences.getString(uid, ""));
		return mySharedPreferences.getString(uid, "");
	}
		
	/*
	 * 根据uid和头像url拼出缓存图片的路径
	 */
	public static String getPortraitPath(String uid, String url){
		if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(url)){
			return null;
		}
//		Utils.logd("url:"+url);
		int start = url.lastIndexOf("/") == -1 ? 0 : url.lastIndexOf("/")+1;
		int end = url.lastIndexOf(".") == -1 ? url.length() : url.lastIndexOf(".")-1;
		String path = Constants.FOLDER_PORTRAIT + uid + "_" + 
				url.substring(start, end);
//		Utils.logd("path:"+path);
		return path;
	}
	
	public static String getBigPicPath(String url){
		if (TextUtils.isEmpty(url)){
			return null;
		}
		int start = url.lastIndexOf("/") == -1 ? 0 : url.lastIndexOf("/")+1;
		int end = url.lastIndexOf(".") == -1 ? url.length() : url.lastIndexOf(".")-1;
		String path = Constants.FOLDER_PORTRAIT + url.substring(start, end);
		return path;
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
			// TODO Auto-generated catch block
			Utils.loge(e);
			exifInterface = null;
		}
		return exifInterface;
	}
	
	public static String getPassport(Context context){
		String id = "";
		id = context.getSharedPreferences(Constants.PASSPORT, 0).getString(Constants.PASSPORT, "");
		return id;
	}
	
	public static void setPassport(Context context, String id){
		if (id == null){
			id = "";
		}
		Editor editor = context.getSharedPreferences(Constants.PASSPORT, 0).edit();
		editor.putString(Constants.PASSPORT, id);
		editor.commit();
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
//        Utils.logd("errorMsg:"+errorMsg);
        if(errorMsg.equals("passport is invalid")){
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setTitle("温馨提示")
					.setMessage("你的登录已过期，请重新登录！")
					.setCancelable(false)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent successIntent = new Intent(Constants.NEED_RELOGIN_ACTION);
							ctx.sendBroadcast(successIntent);
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
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
    		}
    	}else{
        	Utils.showToast(ctx, errorMsg, Toast.LENGTH_LONG);
        	return true;
        }
        return false;
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
	
	public static long calculateLength(CharSequence c) {  
        double len = 0;  
        for (int i = 0; i < c.length(); i++) {  
            int tmp = (int) c.charAt(i);  
            if (tmp > 0 && tmp < 127) {  
                len += 0.5;  
            } else {  
                len++;  
            }  
        }  
        return Math.round(len);  
    } 

    private static Object getMetaValue(Context context, String key)
    {
      Object value = null;

      String packageName = context.getPackageName();
      try
      {
        ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 128);
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
		        总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9 
        */  
        String telRegex = "[1][345789]\\d{9}";//"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。  
        if (TextUtils.isEmpty(mobiles)){
        	return false;  
        }else{
        	return mobiles.matches(telRegex);  
        }
    }
    
    /*
     * 验证密码是否符合规则(6-16位字母或数字)
     * 符合返回true，否则返回false
     */
    public static boolean checkPassword(String password){
    	if (TextUtils.isEmpty(password)){
    		return false;
    	}
    	String regex = "[0-9|a-z|A-Z]{6,16}";
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
	
	public static String getMyUid(Context context){
		String uid = "";
		uid = context.getSharedPreferences(Constants.MYUID, 0).getString(Constants.MYUID, "");
		return uid;
	}
	
	public static void setMyUid(Context context, String uid){
		if (uid == null){
			uid = "";
		}
		Editor editor = context.getSharedPreferences(Constants.MYUID, 0).edit();
		editor.putString(Constants.MYUID, uid);
		editor.commit();
	}
	
	public static String getMyLoginName(Context context){
		String name = "";
		name = context.getSharedPreferences(Constants.MY_LOGINNAME, 0).getString(Constants.MY_LOGINNAME, "");
		return name;
	}
	
	public static void setMyLoginName(Context context, String name){
		if (name == null){
			name = "";
		}
		Editor editor = context.getSharedPreferences(Constants.MY_LOGINNAME, 0).edit();
		editor.putString(Constants.MY_LOGINNAME, name);
		editor.commit();
	}
	
	public static boolean needPerfectInfo(Context context){
		return context.getSharedPreferences(Constants.NEED_PERFECT_INFO, 0).
					getBoolean(Constants.NEED_PERFECT_INFO, false);
	}
	
	public static void setNeedPerfectInfo(Context context, boolean need){
		Editor editor = context.getSharedPreferences(Constants.NEED_PERFECT_INFO, 0).edit();
		editor.putBoolean(Constants.NEED_PERFECT_INFO, need);
		editor.commit();
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
	
	public static City getMyCity(Context context){
		String name = context.getSharedPreferences(Constants.MYCITY, 0).getString(Constants.MYCITY_NAME, context.getString(R.string.default_city_name));
		int id = context.getSharedPreferences(Constants.MYCITY, 0).getInt(Constants.MYCITY_ID, 1);	//1,表示全国
		City city = new City(id, name);
		return city;
	}
	
	public static void setMyCity(Context context, City city){
		Log.v("sereinli",""+city.getCityName()+","+city.getCityID());
		if (city == null){
			return;
		}
		Editor editor = context.getSharedPreferences(Constants.MYCITY, 0).edit();
		editor.putString(Constants.MYCITY_NAME, city.getCityName());
		editor.putInt(Constants.MYCITY_ID, city.getCityID());
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
	
	public static String getTime(String serverTime){
		String time = "";
		if (TextUtils.isEmpty(serverTime)){
			return time;
		}
		serverTime = serverTime.replaceAll("-", ".")
						.replaceAll("T", " ");
		int end = serverTime.lastIndexOf(":");
		if (end > 0){
			time = serverTime.substring(0, end);
		}
		return time;
	}
	
	public static int getDefaultPortraitId(String type, Object user){
		if ("doctor".equals(type)){
			return R.drawable.docphoto;
		}else if ("public".equals(type)){
			return R.drawable.userphoto_female;
		}else if ("patient".equals(type)){
			if (user != null){
				if (user instanceof Patient 
						&& "男".equals(((Patient)user).getGender())){
					return R.drawable.userphoto_male;
				}else{
					return R.drawable.userphoto_female;
				}
			}else{
				return R.drawable.userphoto_female;
			}
		}else{
			return R.drawable.userphoto_female;
		}
	}
	
    public static boolean checkSpecialCharacters(String source){
    	String regEx="[ ~!@#$%^&*<> ?/+=,\\！。？“”，、]";
    	Pattern p = Pattern.compile(regEx);
    	Matcher m = p.matcher(source);
    	if(m.find()){
    		return true;
    	} 
    	return false;
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
    
    private static long sTodayStart;
    private static long sYearStart;
    private static final int    SECOND       = 1000;
    private static final int    MINUTE       = 60 * SECOND;
    private static final int    HOUR         = 60 * MINUTE;
    private static final int    DAY          = 24 * HOUR;
    private static final int    WEEK         = 7 * DAY;
    private static SimpleDateFormat sdf;
    private static SimpleDateFormat sdfToday;
    private static SimpleDateFormat sdfYear;
    private static SimpleDateFormat sdfMonthNoTime;
    private static SimpleDateFormat sdfYearNoTime;
    private static SimpleDateFormat sdfYesterday;
    
    private static boolean isDayChanged( long day ) {
        long currentTime = System.currentTimeMillis();
        return day == 0 || day <= currentTime - DAY || day >= currentTime;
    }

    private static long getTodayStartTime() {
        if (isDayChanged(sTodayStart)) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.MILLISECOND, 0);
            sTodayStart = cal.getTimeInMillis();
        }

        return sTodayStart;
    }

    private static long getYesterdayStartTime() {
        return getTodayStartTime() - DAY;
    }
    
    private static long getThisYearStartTime() {
        if (sYearStart == 0 || isDayChanged(sTodayStart)) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.MILLISECOND, 0);
            sYearStart = cal.getTimeInMillis();
        }

        return sYearStart;
    }
    
    private static SimpleDateFormat getDateFormatYesterday() {
        if (sdfYesterday == null) {
            sdfYesterday = new SimpleDateFormat(Constants.DATE_FORMAT_TODAY);
        }
        return sdfYesterday;
    }

    private static SimpleDateFormat getDateFormatYear() {
        if (sdfYear == null) {
            sdfYear = new SimpleDateFormat(Constants.DATE_FORMAT_YEAR_NEW);
        }
        return sdfYear;
    }
    
    private static SimpleDateFormat getDateFormat() {
        if (sdf == null)
            sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        return sdf;
    }
    
    
	public static String formatDate( Context ctx, Date date ) {
    	if (date == null) {
    		return "";
    	} 
    	
    	if (date.getTime() < getThisYearStartTime()) {
            return getDateFormatYear().format(date);
        }else{ 
        	return getDateFormat().format(date);
        }
    }
    
	public static Date strToDateLong(String strDate) {
		  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  ParsePosition pos = new ParsePosition(0);
		  Date strtodate = formatter.parse(strDate, pos);
		  return strtodate;
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
}
