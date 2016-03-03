package com.ieeton.user.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import com.ieeton.user.R;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.net.NetUtils;
import com.ieeton.user.net.Reflection;
import com.ieeton.user.utils.BitmapHelper;
import com.ieeton.user.utils.BitmapUtils;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.FileUtils;
import com.ieeton.user.utils.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

/**
 * Filter activity
 * @author SinaDev
 *
 */
public class ImageViewerActivity extends TemplateActivity implements OnClickListener,
        OnPageChangeListener {
    private static final int   RECOVER_FROM_OOM_MAX_TRIAL = 3;

    // used to transfer parameters via intent
    public static final String TAG_PIC_ATTACHMENTS        = "pic_attachment_list";
    public static final String TAG_CURRENT_PIC_INDEX      = "current_pic_index";


    //********************data***********************//
    private List<String>  mPicsAttachmentList;

    /**
     * Current picture index
     */
    private int                mCurrentPicIndex           = 0;
    private TextView           mSelection;


    //********************views**********************//

    private Bitmap             mOriginPreviewBitmap;

    private ViewPager          mFilterViewPager;
    private ImageView           mBtnCancel;


    private ProgressDialog     mProgressDialog            = null;

    private AsyncTask<?, ?, ?> mCurrentTask               = null;

//    private AlertDialog        mAlertDialog               = null;

    private FilterPagerAdpter  mFilterAdapter;

    Reflection                 mReflection                = null;

    private SparseArray<SoftReference<Bitmap>> mOriBmpCache = new SparseArray<SoftReference<Bitmap>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        if ( i != null ) {
            mPicsAttachmentList = (List<String>) i.getSerializableExtra( TAG_PIC_ATTACHMENTS );
            mCurrentPicIndex = i.getIntExtra( TAG_CURRENT_PIC_INDEX, 0 );
        }

        restoreInstanceState( savedInstanceState );

        // finish when no picture
        if (mPicsAttachmentList == null || mPicsAttachmentList.isEmpty()) {
            finish();
            return;
        }

        setView(R.layout.activity_image_viewer);
        setTitleBar(null, null, null);
        
        mReflection = new Reflection();

        mSelection = (TextView)findViewById(R.id.txt_count);
        mBtnCancel = (ImageView) findViewById( R.id.btnCancel );
        mBtnCancel.setOnClickListener( this );

        findViewById( R.id.filterRoot );

        mFilterViewPager = (ViewPager) findViewById( R.id.filterPager );
        mFilterAdapter = new FilterPagerAdpter();
        mFilterViewPager.setAdapter( mFilterAdapter );
        mFilterViewPager.setOnPageChangeListener( this );
        mFilterViewPager.setCurrentItem( mCurrentPicIndex );

        //delayedAsyncLoadPreviewBitmap();
        asyncLoadPreviewBitmap();
        updateCountShow(mCurrentPicIndex);
    }

    private void updateCountShow(int show) {
        if (mFilterAdapter.getCount() > 1) {
            mSelection.setText((show + 1) + "/" + mFilterAdapter.getCount());
        } else {
            mSelection.setText("");
        }
    }

    private void restoreInstanceState( Bundle savedInstanceState ) {
        if( savedInstanceState != null ) {
            mPicsAttachmentList = (List<String>) savedInstanceState.getSerializable( TAG_PIC_ATTACHMENTS );
        }
    }

    @Override
    protected void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState( outState );
        if( outState != null ) {
            outState.putSerializable( TAG_PIC_ATTACHMENTS, (Serializable) mPicsAttachmentList );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // no screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    protected void onDestroy() {
        if( mCurrentTask != null ) {
            mCurrentTask.cancel( false );
            mCurrentTask = null;
        }
        dismissProgressDialog();

        setBitmapToCurrentPicView( null );

        for( int i = 0; i < mOriBmpCache.size(); ++i ) {
            final SoftReference<Bitmap> bmpSoftRef = mOriBmpCache.get( i );
            if( bmpSoftRef != null ) {
                recycleBitmap( mOriBmpCache.get( i ).get() );
            }
        }

        System.gc();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {
        if( keyCode == KeyEvent.KEYCODE_BACK ) {
            finish();
            return true;
		}
        return super.onKeyDown( keyCode, event );
    }

    @Override
    public void onClick( View v ) {
        int resId = v.getId();
        switch( resId ) {
            case R.id.btnCancel:
                finish();
                break;

            default:
                break;
        }
        
    }

    @Override
    public void onPageScrollStateChanged( int arg ) {
    }

    @Override
    public void onPageScrolled( int arg0, float arg1, int arg2 ) {
    }

    @Override
    public void onPageSelected( int pos ) {
        mCurrentPicIndex = pos;

        Bitmap cachedOriBmp = getCacheBitmap( mOriBmpCache, pos );
        if( cachedOriBmp == null) {
            asyncLoadPreviewBitmap();
        }
        else {
            mOriginPreviewBitmap = cachedOriBmp;

            showPreviewBitmap( mOriginPreviewBitmap );
        }
        updateCountShow(pos);
        doRotate();
    }

    private static Bitmap getCacheBitmap( SparseArray<SoftReference<Bitmap>> mCacheArray, int pos ) {
        SoftReference<Bitmap> bmpSoftRef = mCacheArray.get( pos );
        if( bmpSoftRef != null ) {
            return bmpSoftRef.get();
        }

        return null;
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
    	super.onActivityResult( requestCode, resultCode, data );
    }

    // async load preview bitmap
    private void asyncLoadPreviewBitmap() {
        try {
            final int currentPicIndex = mCurrentPicIndex;
            mCurrentTask = new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    showProgressDialog( false );
                }

                @Override
                protected Void doInBackground( Void... params ) {
                    if ( !isCancelled() ) {
                        // to recover from OOM error, try 3 times
                        for( int i = 0; i < RECOVER_FROM_OOM_MAX_TRIAL; ++i ) {
                            int ratio = (int) Math.pow( 2, i );
                            try {
                            	String oriUrl = Utils.getCommentOriUrl(mPicsAttachmentList.get(mCurrentPicIndex));
                            	//Utils.logd("oriUrl:"+oriUrl);
                            	if (!FileUtils.isFileExist(Utils.getBigPicPath(oriUrl))){
                            		//原图缓存文件不存在，下载原图
                        			Bitmap bitmap = null;
                        			try {
                        				bitmap = BitmapFactory.decodeStream(NetUtils.getImageStream(NetEngine.getImageUrl(oriUrl)));
                        			} catch (Exception e) {
                        				Utils.logd("load OriPic network failed");
                        				e.printStackTrace();
                        				return null;
                        			}
                        			if (bitmap == null){
                        				Utils.logd("load OriPic network failed");
                        				return null;
                        			}                        			
                        			
                        			File dir=new File(Constants.FOLDER_PORTRAIT); 
                        			if(!dir.exists()){ 
                        				dir.mkdirs(); 
                        			} 
                        			String path = Utils.getBigPicPath(oriUrl);
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
                            	}
                            	
                                mOriginPreviewBitmap = loadPreviewBitmap( getApplication(), 
                                        Utils.getBigPicPath(oriUrl), ratio );
                                
                                break;
                            }
                            catch( OutOfMemoryError e ) {
                                e.printStackTrace();
                                recycleBitmap( mOriginPreviewBitmap );
                            }
                        }

                    }
                    return null;
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                    mCurrentTask = null;
                    dismissProgressDialog();
                }

                @Override
                protected void onPostExecute( Void result ) {
                    super.onPostExecute( result );

                    mOriBmpCache.put( currentPicIndex, new SoftReference<Bitmap>( mOriginPreviewBitmap ));

                    showPreviewBitmap( mOriginPreviewBitmap );
                    doRotate();
                    mCurrentTask = null;
                    dismissProgressDialog();
                }
            }.execute();
        }
        catch( RejectedExecutionException e ) {
            Utils.loge( e );
        }
    }

    // load preview bitmap
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
    
    // show progress dialog
    private void showProgressDialog( boolean cancelable ) {
        if ( mProgressDialog == null ) {
            mProgressDialog = new ProgressDialog( ImageViewerActivity.this );

            mProgressDialog.setMessage( getString( R.string.loading ) );
            mProgressDialog.setOnCancelListener( new OnCancelListener() {
                @Override
                public void onCancel( DialogInterface dialog ) {
                    if ( mCurrentTask != null ) {
                        mCurrentTask.cancel( false );
                        mCurrentTask = null;
                    }
                }
            } );
        }

        mProgressDialog.setCancelable( cancelable );

        mProgressDialog.show();
    }

    // dismiss progress dialog
    private void dismissProgressDialog() {
        if ( mProgressDialog != null ) {
            mProgressDialog.cancel();
        }
    }

    // show bitmap
    private void showPreviewBitmap( Bitmap bitmap ) {
         setBitmapToCurrentPicView( bitmap );
    }
    
    private static void recycleBitmap( Bitmap bmp ) {
        BitmapUtils.recycleBitmap( bmp );
    }

    // Convert Uri to absolute path
    private static String getAbsolutePath( Context context, Uri uri ) {
        
        return Utils.getUriFilePath( uri.toString(), context );
    }

    private String currentPicAttachment() {
        if( mCurrentPicIndex < mPicsAttachmentList.size() ) {
            return mPicsAttachmentList.get( mCurrentPicIndex );
        }
        return null;
    }

    // for short
    private Uri currentOriPicUri() {
        final String originPicUri = currentPicAttachment();
        if( !TextUtils.isEmpty( originPicUri ) ) {
            return Uri.parse( originPicUri );
        }

        return null;
    }

    private void setBitmapToCurrentPicView( Bitmap bmp ) {
        ImageView picView = currentPicView();
        if( picView != null && bmp != null && !bmp.isRecycled()) {
            picView.setImageBitmap( bmp );
        }
    }

    private ImageView currentPicView() {
        if (mFilterAdapter != null
                && mFilterAdapter.mSubViewCache.get( mFilterViewPager.getCurrentItem() ) != null) {
            return mFilterAdapter.mSubViewCache.get( mFilterViewPager.getCurrentItem() ).get();
        }
        return null;
    }
    
    private void setMarixToCurrentPicView( Matrix matrix ) {
        ImageView picView = currentPicView();
        if( picView != null ) {
            picView.setImageMatrix( matrix );
        }
    }
    
    /**
     * Get matrix for rotate
     * @param rotateAngle
     * @param vWidth width of image view
     * @param vHeight height of image view
     * @param bmpWidth width of bitmap
     * @param bmpHeight width of bitmap
     * @return
     */
    public static Matrix getRotateMatrix( int rotateAngle, int vWidth, int vHeight, int bmpWidth,
            int bmpHeight ) {
        // scale
        float scale = 1f;
        if( rotateAngle % 2 == 0 ) {
            scale = Math.min( vWidth / (float)bmpWidth, vHeight / (float)bmpHeight ) ;
        }
        else {
            scale = Math.min( vWidth / (float)bmpHeight, vHeight / (float)bmpWidth ) ;
        }

        Matrix matrix = new Matrix();
        matrix.setScale( scale, scale );

        // Translate for center
        RectF rect = new RectF( 0, 0, bmpWidth, bmpHeight );
        matrix.mapRect( rect );

        float xOffset = ( vWidth - rect.width() ) / 2;
        float yOffset = ( vHeight - rect.height() ) / 2;
        matrix.postTranslate( xOffset, yOffset );

        // rotate
        float degrees = 90 * rotateAngle;
        matrix.postRotate( degrees, vWidth/2f, vHeight/2f );

        return matrix;
    }
    
    private void doRotate() {
        if( mOriginPreviewBitmap == null ) {
            return;
        }

        int vWidth = mFilterViewPager.getWidth();
        int vHeight = mFilterViewPager.getHeight();
        int bmpWidth = mOriginPreviewBitmap.getWidth();
        int bmpHeight = mOriginPreviewBitmap.getHeight();

        String oriPicPath = getAbsolutePath( getApplication(), currentOriPicUri() );
        int exifRotation = BitmapHelper.getImageRotatation(oriPicPath);
        Matrix matrix = getRotateMatrix( exifRotation, vWidth, vHeight, bmpWidth,
                bmpHeight );
        setMarixToCurrentPicView( matrix );
    }

    private class FilterPagerAdpter extends PagerAdapter {
        private SparseArray<SoftReference<ImageView>> mSubViewCache = new SparseArray<SoftReference<ImageView>>();

        @Override
        public int getCount() {
            if( mPicsAttachmentList != null) {
                return mPicsAttachmentList.size();
            }

            return 0;
        }

        @Override
        public boolean isViewFromObject( View view, Object obj ) {
            return view == obj;
        }

        @Override
        public void destroyItem( ViewGroup container, int position, Object object ) {
            container.removeView( (View)object );
        }

        @Override
        public Object instantiateItem( ViewGroup container, int position ) {
            ImageView v = null;
            if( mSubViewCache.get( position ) != null ) {
                v = mSubViewCache.get( position ).get();
            }

            if( v == null ) {
                v = new ImageView( ImageViewerActivity.this );

                ViewGroup.LayoutParams params = v.getLayoutParams();
                if( params == null ) {
                    params = new ViewPager.LayoutParams();
                }
                params.width = ViewGroup.LayoutParams.FILL_PARENT;
                params.height = ViewGroup.LayoutParams.FILL_PARENT;

                v.setScaleType( ScaleType.MATRIX );

                mSubViewCache.put( position, new SoftReference<ImageView>( v ) );

                v.setOnClickListener( new OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                    }
                } );
            }

            if( v.getParent() == null ) {
                container.addView( v );
            }
            else {
                container.requestLayout();
                container.invalidate();
            }

            v.setTag( currentOriPicUri().toString() );

            return v;
        }

        @Override
        public int getItemPosition( Object object ) {
            final View cachedView = currentPicView();
            final View v = (View) object;
            if (cachedView != null 
                    && cachedView.getTag() != null
                    && cachedView.getTag().equals( v.getTag() )) {
                return POSITION_UNCHANGED;
            }
            else {
                ((ImageView)v).setImageBitmap( null );
                return POSITION_NONE;
            }
        }

    }

	@Override
	protected void handleTitleBarEvent(int eventId) {
		
	}

}