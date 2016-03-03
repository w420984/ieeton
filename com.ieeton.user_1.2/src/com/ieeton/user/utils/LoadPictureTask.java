package com.ieeton.user.utils;

import com.ieeton.user.R;
import com.ieeton.user.net.NetUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

public class LoadPictureTask extends AsyncTask<Object, Void, Bitmap> {
	
    protected void onPreExecute() {

    }

    Context mContext;
    String mPortraitUrl;
    ImageView mPortraitIv;
    
    protected Bitmap doInBackground( Object... args ) {

    	mContext = (Context) args[0];
    	mPortraitUrl = (String) args[1];
        mPortraitIv = (ImageView) args[2];
    	String mCacheDir = (String)mContext.getCacheDir().getAbsolutePath();
        // String cacheDir = TextUtils.isEmpty(mSdFileDir)? mCacheDir :
        // mSdFileDir;
        Bitmap result = null;
        try {
            Utils.loge(mCacheDir);
            result = BitmapFactory.decodeStream(NetUtils.getImageStream(mPortraitUrl));
        } catch (java.lang.OutOfMemoryError err) {
        	result = null;
            // try my best to release memory :(
            System.gc();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return result;

    }

    protected void onProgressUpdate( Void... unused ) {
    }

    protected void onPostExecute( Bitmap ret ) {

        if (TextUtils.isEmpty(mPortraitUrl)) {
            return;
        }

        if (ret != null && !ret.isRecycled()) {
            mPortraitIv.setImageBitmap(ret);
            BmpCache.getInstance().save(mPortraitUrl, ret);
        } else {
            mPortraitIv.setImageResource(R.drawable.docprofile_photo);
        }
    }
}
