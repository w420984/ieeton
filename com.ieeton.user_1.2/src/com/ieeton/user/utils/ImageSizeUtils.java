package com.ieeton.user.utils;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.ieeton.user.net.NetUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;


public final class ImageSizeUtils {
    /**
     * 上传图片的策略
     * @author SinaDev
     *
     */
    public static final class UploadImageUtils {
        public static boolean revitionPostImageSize( Context context, String originalPic, String picfile) {
            if (!FileUtils.doesExisted( originalPic ) || !BitmapHelper.verifyBitmap( originalPic )) {
                return false;
            }

            final BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile( originalPic, opts );

            final int w = opts.outWidth;
            final int h = opts.outHeight;
            final boolean isWifiNet = NetUtils.isWifi(context);

            try {
                // 图片长边:短边 > 10:3
                if( 3 * w > 10 * h || 3 * h > 10 * w ) {
                    // WIFI下压缩75% 非WIFI下压缩50%
                    int quality = isWifiNet ? 75 : 50;
                    // 竖图：宽压成640  横图：高压成960px
                    revitionImageSizeHD_OnShortSide( originalPic, picfile, new Point( 640, 960 ), quality );
                }
                // 长边:短边 < 10:3
                else {
                    // 最长边1600px   压缩比75%
                    if( isWifiNet ) {
                        revitionImageSizeHD( originalPic, picfile, 1600, 75 );
                    }
                    else {
                        // 控制在640x960范围内 压缩比75%
                        Point size = w < h ? new Point( 640, 960 ) : new Point( 960, 640 );
                        revitionImageSizeHD_InRegion( originalPic, picfile, size, 45 );
                    }
                }

                return true;
            } catch (IOException e) {
                Utils.loge(e);
            }
            return false;
        }

        public static boolean revitionImageSizeHighQuality( Context context, String originalPic, String picfile) {
            if (!FileUtils.doesExisted( originalPic ) || !BitmapHelper.verifyBitmap( originalPic )) {
                return false;
            }

            final BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile( originalPic, opts );

            final int w = opts.outWidth;
            final int h = opts.outHeight;

            try {
                // 图片长边:短边 > 10:3
                if( 3 * w > 10 * h || 3 * h > 10 * w ) {
                    // 竖图：宽压成640  横图：高压成960px
                    revitionImageSizeHD_OnShortSide( originalPic, picfile, new Point( 640, 960 ), 75 );
                }
                // 长边:短边 < 10:3
                else {
                    revitionImageSizeHD( originalPic, picfile, 1600, 75 );
                }

                return true;
            } catch (IOException e) {
                Utils.loge(e);
            }
            return false;
        }

        /**
         *  此方法为缩图规则为：限制长边小于size，保持宽高比不变,缩放比率呈2的指数级递增
         * @param originalPic
         * @param picfile
         * @param size
         * @param quality
         * @throws IOException
         */
        @SuppressWarnings("unused")
        private static void revitionImageSize( String originalPic, String picfile, int size ,int quality) throws IOException {
            if (size <= 0) {
                throw new IllegalArgumentException("size must be greater than 0!");
            }
            
            if (!FileUtils.doesExisted(originalPic)) {
                throw new FileNotFoundException(originalPic == null ? "null" : originalPic );
            }
            
            if (!BitmapHelper.verifyBitmap(originalPic)) {
                throw new IOException("");
            }
            
            FileInputStream input = new FileInputStream(originalPic);
            final BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, opts);
            FileUtils.closeStream(input);
            
            int rate = 0;
            for (int i = 0;; i++) {
                if ((opts.outWidth >> i <= size) && (opts.outHeight >> i <= size)) {
                    rate = i;
                    break;
                }
            }
            Utils.logd("\t opts.outWidth:" + opts.outWidth + "\t opts.outHeight:"
                    + opts.outHeight);
            Utils.logd("\t rate:" + Math.pow(2, rate));
            
            opts.inSampleSize = (int) Math.pow(2, rate);
            opts.inJustDecodeBounds = false;
            
            Bitmap temp = safeDecodeBimtapFile( originalPic, opts );
            
            if (temp == null) {
                throw new IOException("Bitmap decode error!");
            }
            
            FileUtils.deleteDependon(picfile);
            FileUtils.makesureFileExist(picfile);
            final FileOutputStream output = new FileOutputStream(picfile);
            if (opts != null && opts.outMimeType != null
                    && opts.outMimeType.contains("png")) {
                temp.compress(Bitmap.CompressFormat.PNG, quality, output);
            } else {
                temp.compress(Bitmap.CompressFormat.JPEG, quality, output);
            }
            FileUtils.closeStream(output);
            temp.recycle();
        }

        /**
         * 此方法为缩图规则为：限制短边小于size，保持宽高比不变,缩放比率呈2的指数级递增
         * @param originalPic
         * @param picfile
         * @param size
         * @param quality
         * @throws IOException
         */
        @SuppressWarnings("unused")
        private static void revitionImageSizeOnShortSide( String originalPic, String picfile, int size ,int quality) throws IOException {
            if (size <= 0) {
                throw new IllegalArgumentException("size must be greater than 0!");
            }
            
            if (!FileUtils.doesExisted(originalPic)) {
                throw new FileNotFoundException(originalPic == null ? "null" : originalPic );
            }
            
            if (!BitmapHelper.verifyBitmap(originalPic)) {
                throw new IOException("");
            }

            final BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(originalPic, opts);

            final int w = opts.outWidth;
            final int h = opts.outHeight;
            final int shortSideLength = w < h ? w : h;

            int rate = 0;
            for (int i = 0;; i++) {
                if ( shortSideLength >> i <= size ) {
                    rate = i;
                    break;
                }
            }
            Utils.logd("\t opts.outWidth:" + opts.outWidth + "\t opts.outHeight:"
                    + opts.outHeight);
            Utils.logd("\t rate:" + Math.pow(2, rate));
            
            opts.inSampleSize = (int) Math.pow(2, rate);
            opts.inJustDecodeBounds = false;
            
            Bitmap temp = safeDecodeBimtapFile( originalPic, opts );
            
            if (temp == null) {
                throw new IOException("Bitmap decode error!");
            }
            
            FileUtils.deleteDependon(picfile);
            FileUtils.makesureFileExist(picfile);
            final FileOutputStream output = new FileOutputStream(picfile);
            if (opts != null && opts.outMimeType != null
                    && opts.outMimeType.contains("png")) {
                temp.compress(Bitmap.CompressFormat.PNG, quality, output);
            } else {
                temp.compress(Bitmap.CompressFormat.JPEG, quality, output);
            }
            FileUtils.closeStream(output);
            temp.recycle();
        }


        /**
         * 规则为：限制长边小于size，保持宽高比不变
         * @param originalPic
         * @param picfile
         * @param size
         * @param quality
         * @throws IOException
         */
        private static void revitionImageSizeHD( String originalPic, String picfile, int size,
                int quality ) throws IOException {
            if (size <= 0) {
                throw new IllegalArgumentException("size must be greater than 0!");
            }
            if (!FileUtils.doesExisted(originalPic)) {
                throw new FileNotFoundException(originalPic == null ? "null" : originalPic );
            }
            
            if (!BitmapHelper.verifyBitmap(originalPic)) {
                throw new IOException("");
            }
            
            int photoSizesOrg = 2 * size;
            FileInputStream input = new FileInputStream(originalPic);
            final BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, opts);
            FileUtils.closeStream(input);
            
            int rate = 0;
            for (int i = 0;; i++) {
                if ((opts.outWidth >> i <= photoSizesOrg && (opts.outHeight >> i <= photoSizesOrg))) {
                    rate = i;
                    break;
                }
            }
            Utils.logd("\t opts.outWidth:" + opts.outWidth + "\t opts.outHeight:"
                    + opts.outHeight);
            Utils.logd("\t rate:" + Math.pow(2, rate));
            
            opts.inSampleSize = (int) Math.pow(2, rate);
            opts.inJustDecodeBounds = false;
            
            Bitmap temp = safeDecodeBimtapFile( originalPic, opts );
            
            if (temp == null) {
                throw new IOException("Bitmap decode error!");
            }
            
            FileUtils.deleteDependon(picfile);
            FileUtils.makesureFileExist(picfile);
            
            int org = temp.getWidth()>temp.getHeight()?temp.getWidth():temp.getHeight();
            float rateOutPut = size/(float)org;

            // exif角度信息
            final int rotateAngle = BitmapHelper.getImageRotatation( originalPic );
            int targetWidthBase = rotateAngle % 2 == 0 ? temp.getWidth() : temp.getHeight();
            int targetHeightBase = rotateAngle % 2 == 0 ? temp.getHeight() : temp.getWidth();

            rateOutPut = rateOutPut > 1 ? 1 : rateOutPut;

        	Bitmap outputBitmap;
        	while(true) {
        		try {
        			outputBitmap = Bitmap.createBitmap(((int)(targetWidthBase*rateOutPut)), ((int)(targetHeightBase*rateOutPut)), Bitmap.Config.ARGB_8888);
					break;
				} catch (OutOfMemoryError e) {
					System.gc();
					rateOutPut = (float)(rateOutPut * 0.8); 
				}
			}

        	Canvas canvas = new Canvas(outputBitmap);

            Matrix matrix = getMatrix( rotateAngle, outputBitmap.getWidth(),
                    outputBitmap.getHeight(), temp.getWidth(), temp.getHeight() );

        	canvas.drawBitmap(temp, matrix, new Paint());
        	temp.recycle();
        	temp = outputBitmap;

            final FileOutputStream output = new FileOutputStream(picfile);
            if (opts != null && opts.outMimeType != null
                    && opts.outMimeType.contains("png")) {
                temp.compress(Bitmap.CompressFormat.PNG, quality, output);
            } else {
                temp.compress(Bitmap.CompressFormat.JPEG, quality, output);
            }
            FileUtils.closeStream(output);
            
            temp.recycle();
        }

        /**
         * 此方法为缩图规则为：限制短边小于size，保持宽高比不变
         * @param originalPic
         * @param picfile
         * @param size
         * @param quality
         * @throws IOException
         */
        private static void revitionImageSizeHD_OnShortSide( String originalPic, String picfile,
                Point size, int quality ) throws IOException {
            if (size.x <= 0 || size.y <=0) {
                throw new IllegalArgumentException( "size must be greater than 0!" );
            }

            if (!FileUtils.doesExisted( originalPic )) {
                throw new FileNotFoundException( originalPic == null ? "null" : originalPic );
            }

            if (!BitmapHelper.verifyBitmap( originalPic )) {
                throw new IOException( "" );
            }

            final BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile( originalPic, opts );

            final int w = opts.outWidth;
            final int h = opts.outHeight;
            final int shortSideLength = w < h ? w : h;

            // exif角度信息
            final int rotateAngle = BitmapHelper.getImageRotatation( originalPic );
            if( rotateAngle % 2 != 0 ) {
                int temp = size.x;
                size.x = size.y;
                size.y = temp;
            }
            final int targetSize = w < h ? size.x : size.y;

            int rate = 0;
            for (int i = 0;; i++) {
                if (shortSideLength >> i <= 2 * targetSize) {
                    rate = i;
                    break;
                }
            }

            opts.inSampleSize = (int) Math.pow( 2, rate );
            opts.inJustDecodeBounds = false;

            Utils.logd("\t opts.outWidth:" + opts.outWidth + "\t opts.outHeight:"
                    + opts.outHeight);
            Utils.logd( "\t rate:" + Math.pow( 2, rate ) );

            Bitmap temp = safeDecodeBimtapFile( originalPic, opts );

            if (temp == null) {
                throw new IOException( "Bitmap decode error!" );
            }

            int org = temp.getWidth() > temp.getHeight() ? temp.getHeight() : temp.getWidth();
            float rateOutPut = targetSize / (float) org;

            int targetWidthBase = rotateAngle % 2 == 0 ? temp.getWidth() : temp.getHeight();
            int targetHeightBase = rotateAngle % 2 == 0 ? temp.getHeight() : temp.getWidth();

            rateOutPut = rateOutPut > 1 ? 1 : rateOutPut;

            Bitmap outputBitmap;
            while (true) {
                try {
                    outputBitmap = Bitmap.createBitmap( ((int) (targetWidthBase * rateOutPut)),
                            ((int) (targetHeightBase * rateOutPut)), Bitmap.Config.ARGB_8888 );
                    break;
                }
                catch (OutOfMemoryError e) {
                    System.gc();
                    rateOutPut = (float) (rateOutPut * 0.8);
                }
            }
            if (outputBitmap == null) {
                temp.recycle();
            }
            Canvas canvas = new Canvas( outputBitmap );

            Matrix matrix = getMatrix( rotateAngle, outputBitmap.getWidth(),
                    outputBitmap.getHeight(), temp.getWidth(), temp.getHeight() );

            canvas.drawBitmap( temp, matrix, new Paint() );
            temp.recycle();
            temp = outputBitmap;

            FileUtils.deleteDependon( picfile );
            FileUtils.makesureFileExist( picfile );
            final FileOutputStream output = new FileOutputStream( picfile );
            if (opts != null && opts.outMimeType != null && opts.outMimeType.contains( "png" )) {
                temp.compress( Bitmap.CompressFormat.PNG, quality, output );
            }
            else {
                temp.compress( Bitmap.CompressFormat.JPEG, quality, output );
            }
            FileUtils.closeStream( output );

            temp.recycle();
        }

        /**
         * 此方法为缩图规则为：限制宽小于size.x且高小于size.y，保持宽高比不变
         * @param originalPic
         * @param picfile
         * @param size
         * @param quality
         * @throws IOException
         */
        private static void revitionImageSizeHD_InRegion( String originalPic, String picfile,
                Point size, int quality ) throws IOException {
            if (size.x <= 0 || size.y <= 0) {
                throw new IllegalArgumentException( "size must be greater than 0!" );
            }

            if (!FileUtils.doesExisted( originalPic )) {
                throw new FileNotFoundException( originalPic == null ? "null" : originalPic );
            }

            if (!BitmapHelper.verifyBitmap( originalPic )) {
                throw new IOException( "" );
            }

            final BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile( originalPic, opts );

            final int w = opts.outWidth;
            final int h = opts.outHeight;

            opts.inSampleSize = BitmapHelper.getSampleSizeAutoFitToScreen( size.x, size.y, w, h );
            opts.inJustDecodeBounds = false;

            Utils.logd("\t opts.outWidth:" + opts.outWidth + "\t opts.outHeight:"
                    + opts.outHeight);
            Bitmap temp = safeDecodeBimtapFile( originalPic, opts );

            if (temp == null) {
                throw new IOException( "Bitmap decode error!" );
            }

            final float wRateOutput = size.x / (float)temp.getWidth();
            final float hRateOutput = size.y / (float)temp.getHeight();
            float rateOutPut = wRateOutput < hRateOutput ? wRateOutput : hRateOutput;

            // exif角度信息
            final int rotateAngle = BitmapHelper.getImageRotatation( originalPic );
            int targetWidthBase = rotateAngle % 2 == 0 ? temp.getWidth() : temp.getHeight();
            int targetHeightBase = rotateAngle % 2 == 0 ? temp.getHeight() : temp.getWidth();

            rateOutPut = rateOutPut > 1 ? 1 : rateOutPut;

            Bitmap outputBitmap;
            while (true) {
                try {
                    outputBitmap = Bitmap.createBitmap( ((int) (targetWidthBase * rateOutPut)),
                            ((int) (targetHeightBase * rateOutPut)), Bitmap.Config.ARGB_8888 );
                    break;
                }
                catch (OutOfMemoryError e) {
                    System.gc();
                    rateOutPut = (float) (rateOutPut * 0.8);
                }
            }
            if (outputBitmap == null) {
                temp.recycle();
            }
            Canvas canvas = new Canvas( outputBitmap );

            Matrix matrix = getMatrix( rotateAngle, outputBitmap.getWidth(),
                    outputBitmap.getHeight(), temp.getWidth(), temp.getHeight() );

            canvas.drawBitmap( temp, matrix, new Paint() );
            temp.recycle();
            temp = outputBitmap;

            FileUtils.deleteDependon( picfile );
            FileUtils.makesureFileExist( picfile );
            final FileOutputStream output = new FileOutputStream( picfile );
            if (opts != null && opts.outMimeType != null && opts.outMimeType.contains( "png" )) {
                temp.compress( Bitmap.CompressFormat.PNG, quality, output );
            }
            else {
                temp.compress( Bitmap.CompressFormat.JPEG, quality, output );
            }
            FileUtils.closeStream( output );

            temp.recycle();
        }

        private static Matrix getMatrix( int rotateAngle, int targetWidth, int targetHeight,
                int bmpWidth, int bmpHeight ) {
            // scale
            float scale = 1f;
            if( rotateAngle % 2 == 0 ) {
                scale = Math.min( targetWidth / (float)bmpWidth, targetHeight / (float)bmpHeight ) ;
            }
            else {
                scale = Math.min( targetWidth / (float)bmpHeight, targetHeight / (float)bmpWidth ) ;
            }

            Matrix matrix = new Matrix();
            matrix.setScale( scale, scale );

            // Translate for center
            RectF rect = new RectF( 0, 0, bmpWidth, bmpHeight );
            matrix.mapRect( rect );

            float xOffset = ( targetWidth - rect.width() ) / 2;
            float yOffset = ( targetHeight - rect.height() ) / 2;
            matrix.postTranslate( xOffset, yOffset );

            // rotate
            float degrees = 90 * rotateAngle;
            matrix.postRotate( degrees, targetWidth/2f, targetHeight/2f );

            return matrix;
        }
    }
    
    /**
     * 如果加载时遇到OutOfMemoryError,则将图片加载尺寸缩小一半并重新加载
     * @param bmpFile
     * @param opts 注意：opts.inSampleSize 可能会被改变
     * @return
     */
    public static Bitmap safeDecodeBimtapFile( String bmpFile, BitmapFactory.Options opts ) {
        BitmapFactory.Options optsTmp = opts;
        if ( optsTmp == null ) {
            optsTmp = new BitmapFactory.Options();
            optsTmp.inSampleSize = 1;
        }
        
        Bitmap bmp = null;
        FileInputStream input = null;
        
        final int MAX_TRIAL = 5;
        for( int i = 0; i < MAX_TRIAL; ++i ) {
            try {
                input = new FileInputStream( bmpFile );
                bmp = BitmapFactory.decodeStream(input, null, opts);
                FileUtils.closeStream(input);
                break;
            }
            catch( OutOfMemoryError e ) {
                e.printStackTrace();
                optsTmp.inSampleSize *= 2;
                FileUtils.closeStream(input);
            }
            catch (FileNotFoundException e) {
                break;
            }
        }
        
        return bmp;
    }

}
