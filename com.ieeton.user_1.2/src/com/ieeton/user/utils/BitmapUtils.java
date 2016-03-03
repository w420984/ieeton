package com.ieeton.user.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.text.TextUtils;

public class BitmapUtils {
	
	public static Bitmap createScaledBitmap(Bitmap src_, int width, int height,
			Bitmap.Config config) {
		if ((width == src_.getWidth()) && (height == src_.getHeight())) {
			return src_.copy(config, true);
		}
		return Bitmap.createScaledBitmap(src_, width, height, true);
	}

	public static Rect getMaxBound(long maxSize, Bitmap.Config conf, Rect src_,
			Rect out_) {
		if (src_== null || out_ == null){
			return null;
		}

		int rate = 2;
		if (conf == Bitmap.Config.ALPHA_8)
			rate = 1;
		else if (conf == Bitmap.Config.ARGB_8888) {
			rate = 4;
		}

		double width = src_.width();
		double height = src_.height();

		double newWidth = Math.sqrt(maxSize * width / (rate * height));
		double newHeight = newWidth * height / width;

		out_.set(0, 0, (int) newWidth, (int) newHeight);
		return out_;
	}

	public static Rect getMaxBound(long maxSize, Bitmap.Config conf,
			int srcWidth, int srcHeight) {
		return getMaxBound(maxSize, conf, new Rect(0, 0, srcWidth, srcHeight),
				new Rect());
	}

	public static Bitmap createScaledBitmap_(Bitmap src_, int desW, int desH,
			Bitmap.Config config_) {
		 checkNull(src_);
		 checkNull(config_);
		 check(desW > 0);
		 check(desH > 0);

		Bitmap desImg = null;
		int srcW = src_.getWidth();
		int srcH = src_.getHeight();
		int[] srcBuf = new int[srcW * srcH];

		src_.getPixels(srcBuf, 0, srcW, 0, 0, srcW, srcH);

		int[] tabY = new int[desH];
		int[] tabX = new int[desW];

		int sb = 0;
		int db = 0;
		int tems = 0;
		int temd = 0;
		int distance = srcH > desH ? srcH : desH;
		for (int i = 0; i <= distance; i++) {
			tabY[db] = sb;
			tems += srcH;
			temd += desH;
			if (tems > distance) {
				tems -= distance;
				sb++;
			}
			if (temd > distance) {
				temd -= distance;
				db++;
			}
		}

		sb = 0;
		db = 0;
		tems = 0;
		temd = 0;
		distance = srcW > desW ? srcW : desW;
		for (int i = 0; i <= distance; i++) {
			tabX[db] = (short) sb;
			tems += srcW;
			temd += desW;
			if (tems > distance) {
				tems -= distance;
				sb++;
			}
			if (temd > distance) {
				temd -= distance;
				db++;
			}

		}

		int[] desBuf = new int[desW * desH];
		int dx = 0;
		int dy = 0;
		int sy = 0;

		int oldy = -1;
		for (int i = 0; i < desH; i++) {
			if (oldy == tabY[i]) {
				System.arraycopy(desBuf, dy - desW, desBuf, dy, desW);
			} else {
				dx = 0;
				for (int j = 0; j < desW; j++) {
					desBuf[(dy + dx)] = srcBuf[(sy + tabX[j])];
					dx++;
				}
				sy += (tabY[i] - oldy) * srcW;
			}
			oldy = tabY[i];
			dy += desW;
		}

		desImg = Bitmap.createBitmap(desBuf, desW, desH, config_);

		return desImg;
	}

	public static Bitmap createZoomOutBitmap(InputStream input_, int zoomOutRate) {
		 checkNull(input_);
		Bitmap datas = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		input_ = FileUtils.makeInputBuffered(input_);
		if (zoomOutRate > 1) {
			opts.inSampleSize = zoomOutRate;
		}
		datas = BitmapFactory.decodeStream(input_, null, opts);
		FileUtils.closeStream(input_);
		return datas;
	}

	public static Bitmap createZoomOutBitmap(File inputFile, int zoomOutRate) {
		return createZoomOutBitmap(
				new BufferedInputStream(FileUtils.getFileInputStream(inputFile)),
				zoomOutRate);
	}

	public static Bitmap createZoomOutBitmap(String inputPath, int zoomOutRate) {
		return createZoomOutBitmap(new File(inputPath), zoomOutRate);
	}

	public static Bitmap createZoomOutBitmap(byte[] data_, int offset,
			int length, int zoomOutRate) {
		checkArrayNullOrEmpty(data_);
		BitmapFactory.Options opts = new BitmapFactory.Options();
		if (zoomOutRate > 1) {
			opts.inSampleSize = zoomOutRate;
		}
		return BitmapFactory.decodeByteArray(data_, offset, length, opts);
	}

	public static boolean makeZoomOutBitmap(File src, File dest, int zoomOutRate) {
		checkNull(src);
		checkNull(dest);
		try {
			if (FileUtils.doesExisted(src)) {
				InputStream input = FileUtils
						.makeInputBuffered(new FileInputStream(src));
				Bitmap bmp = createZoomOutBitmap(input, zoomOutRate);
				FileUtils.createNewFile(dest);
				OutputStream output = FileUtils
						.makeOutputBuffered(new FileOutputStream(dest));
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
				return FileUtils.closeStream(output);
			}
		} catch (FileNotFoundException e) {
			Utils.loge(e);
		}

		return false;
	}

	public static boolean makeZoomOutBitmap(File bmpfile, int zoomOutRate) {
		return makeZoomOutBitmap(bmpfile, bmpfile, zoomOutRate);
	}

	public static boolean makeZoomOutBitmap(String srcpath, String destpath,
			int zoomOutRate) {
		checkStringNullOrEmpty(srcpath);
		checkStringNullOrEmpty(destpath);
		return makeZoomOutBitmap(new File(srcpath), new File(destpath),
				zoomOutRate);
	}

	public static boolean makeZoomOutBitmap(String path, int zoomOutRate) {
		checkStringNullOrEmpty(path);
		return makeZoomOutBitmap(new File(path), zoomOutRate);
	}

	public static boolean getZoomOutBitmapBound(InputStream input_,
			int zoomOutRate, Rect outRect_) {
		checkNull(input_);
		checkNull(outRect_);
		BitmapFactory.Options opts = new BitmapFactory.Options();
		input_ = FileUtils.makeInputBuffered(input_);
		opts.inJustDecodeBounds = true;
		if (zoomOutRate > 1) {
			opts.inSampleSize = zoomOutRate;
		}
		BitmapFactory.decodeStream(input_, null, opts);
		FileUtils.closeStream(input_);
		if ((opts.outWidth > 0) && (opts.outHeight > 0)) {
			outRect_.set(0, 0, opts.outWidth, opts.outHeight);
			return true;
		}
		return false;
	}

	public static boolean getZoomOutBitmapBound(byte[] data_, int offset,
			int length, int zoomOutRate, Rect outRect_) {
		checkArrayNullOrEmpty(data_);
		checkNull(outRect_);
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		if (zoomOutRate > 1) {
			opts.inSampleSize = zoomOutRate;
		}
		BitmapFactory.decodeByteArray(data_, offset, length, opts);
		if ((opts.outWidth > 0) && (opts.outHeight > 0)) {
			outRect_.set(0, 0, opts.outWidth, opts.outHeight);
			return true;
		}
		return false;
	}

	public static boolean getZoomOutBitmapBound(File bmpfile, int zoomOutRate,
			Rect outRect_) {
		checkNull(bmpfile);
		checkNull(outRect_);
		try {
			if (FileUtils.doesExisted(bmpfile)) {
				InputStream input = FileUtils
						.makeInputBuffered(new FileInputStream(bmpfile));
				return getZoomOutBitmapBound(input, zoomOutRate, outRect_);
			}
		} catch (FileNotFoundException e) {
			Utils.loge(e);
		}

		return false;
	}

	public static boolean getZoomOutBitmapBound(String path, int zoomOutRate,
			Rect outRect_) {
		checkStringNullOrEmpty(path);
		return getZoomOutBitmapBound(new File(path), zoomOutRate, outRect_);
	}

	public static boolean createRightRotatedBitmap(File bmpfile,
			Bitmap.Config config) {
		checkNull(bmpfile);
		checkNull(config);
		try {
			InputStream input = FileUtils
					.makeInputBuffered(new FileInputStream(bmpfile));
			Bitmap srcbmp = BitmapFactory.decodeStream(input);
			input.close();
			if (srcbmp != null) {
				Bitmap newBmp = createRightRotatedBitmap(srcbmp, config);
				FileUtils.createNewFile(bmpfile);
				OutputStream out = FileUtils
						.makeOutputBuffered(new FileOutputStream(bmpfile));
				newBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();
				srcbmp.recycle();
				newBmp.recycle();

				return true;
			}
		} catch (IOException e) {
			Utils.loge(e);
		}

		return false;
	}

	public static boolean createRightRotatedBitmap(String bmpPath,
			Bitmap.Config config) {
		checkStringNullOrEmpty(bmpPath);
		checkNull(config);
		return createRightRotatedBitmap(new File(bmpPath), config);
	}

	public static boolean createRightRotatedBitmap(File srcFile, File destFile,
			Bitmap.Config config) {
		checkNull(srcFile);
		checkNull(destFile);
		checkNull(config);
		try {
			if (FileUtils.doesExisted(srcFile)) {
				InputStream input = FileUtils
						.makeInputBuffered(new FileInputStream(srcFile));
				Bitmap srcbmp = BitmapFactory.decodeStream(input);
				input.close();
				if (srcbmp != null) {
					Bitmap newBmp = createRightRotatedBitmap(srcbmp, config);
					FileUtils.createNewFile(destFile);
					OutputStream out = FileUtils
							.makeOutputBuffered(new FileOutputStream(destFile));
					newBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.close();
					srcbmp.recycle();
					newBmp.recycle();
					return true;
				}
			}
		} catch (IOException e) {
			Utils.loge(e);
		}
		return false;
	}

	public static boolean createRightRotatedBitmap(String srcPath,
			String destPath, Bitmap.Config config) {
		checkStringNullOrEmpty(srcPath);
		checkStringNullOrEmpty(destPath);
		checkNull(config);
		return createRightRotatedBitmap(new File(srcPath), new File(destPath),
				config);
	}

	public static byte[] createRightRotatedBitmap(byte[] data, int offset,
			int length, Bitmap.Config config) {
		checkArrayNullOrEmpty(data);
		checkNull(config);
		try {
			Bitmap srcbmp = BitmapFactory.decodeByteArray(data, offset, length);
			if (srcbmp != null) {
				Bitmap newBmp = createRightRotatedBitmap(srcbmp, config);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(524288);
				newBmp.compress(Bitmap.CompressFormat.JPEG, 100,
						FileUtils.makeOutputBuffered(baos));
				srcbmp.recycle();
				newBmp.recycle();
				byte[] newData = baos.toByteArray();
				baos.close();

				return newData;
			}
		} catch (IOException e) {
			Utils.loge(e);
		}

		return null;
	}

	public static Bitmap createRightRotatedBitmap(Bitmap srcbmp,
			Bitmap.Config config) {
		checkNull(srcbmp);
		checkNull(config);

		Matrix m = new Matrix();
		m.postRotate(90.0F);
		Bitmap newBmp = Bitmap.createBitmap(srcbmp, 0, 0, srcbmp.getWidth(),
				srcbmp.getHeight(), m, true);
		return newBmp;
	}

	public static boolean createLeftRotatedBitmap(File bmpfile,
			Bitmap.Config config) {
		checkNull(bmpfile);
		checkNull(config);
		try {
			InputStream input = FileUtils
					.makeInputBuffered(new FileInputStream(bmpfile));
			Bitmap srcbmp = BitmapFactory.decodeStream(input);
			input.close();
			if (srcbmp != null) {
				Bitmap newBmp = createLeftRotatedBitmap(srcbmp, config);
				FileUtils.createNewFile(bmpfile);
				OutputStream out = FileUtils
						.makeOutputBuffered(new FileOutputStream(bmpfile));
				newBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();
				srcbmp.recycle();
				newBmp.recycle();

				return true;
			}
		} catch (IOException e) {
			Utils.loge(e);
		}

		return false;
	}

	public static boolean createLeftRotatedBitmap(String bmpPath,
			Bitmap.Config config) {
		checkStringNullOrEmpty(bmpPath);
		checkNull(config);
		return createLeftRotatedBitmap(new File(bmpPath), config);
	}

	public static boolean createLeftRotatedBitmap(File srcfile, File destfile,
			Bitmap.Config config) {
		checkNull(srcfile);
		checkNull(destfile);
		checkNull(config);
		try {
			if (FileUtils.doesExisted(srcfile)) {
				InputStream input = FileUtils
						.makeInputBuffered(new FileInputStream(srcfile));
				Bitmap srcbmp = BitmapFactory.decodeStream(input);
				input.close();
				if (srcbmp != null) {
					Bitmap newBmp = createLeftRotatedBitmap(srcbmp, config);
					FileUtils.createNewFile(destfile);
					OutputStream out = FileUtils
							.makeOutputBuffered(new FileOutputStream(destfile));
					newBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.close();
					srcbmp.recycle();
					newBmp.recycle();

					return true;
				}
			}
		} catch (IOException e) {
			Utils.loge(e);
		}
		return false;
	}

	public static boolean createLeftRotatedBitmap(String srcPath,
			String destPath, Bitmap.Config config) {
		checkStringNullOrEmpty(srcPath);
		checkStringNullOrEmpty(destPath);
		checkNull(config);
		return createLeftRotatedBitmap(new File(srcPath), new File(destPath),
				config);
	}

	public static byte[] createLeftRotatedBitmap(byte[] data, int offset,
			int length, Bitmap.Config config) {
		checkArrayNullOrEmpty(data);
		checkNull(config);
		try {
			Bitmap srcbmp = BitmapFactory.decodeByteArray(data, offset, length);
			if (srcbmp != null) {
				Bitmap newBmp = createLeftRotatedBitmap(srcbmp, config);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(524288);
				newBmp.compress(Bitmap.CompressFormat.JPEG, 100,
						FileUtils.makeOutputBuffered(baos));
				srcbmp.recycle();
				newBmp.recycle();
				byte[] newData = baos.toByteArray();
				baos.close();

				return newData;
			}
		} catch (IOException e) {
			Utils.loge(e);
		}

		return null;
	}

	public static Bitmap createLeftRotatedBitmap(Bitmap srcbmp,
			Bitmap.Config config) {
		checkNull(srcbmp);
		checkNull(config);

		Matrix m = new Matrix();
		m.postRotate(-90.0F);
		Bitmap newBmp = Bitmap.createBitmap(srcbmp, 0, 0, srcbmp.getWidth(),
				srcbmp.getHeight(), m, true);
		return newBmp;
	}

	public static boolean create180RotatedBitmap(File bmpfile,
			Bitmap.Config config) {
		checkNull(bmpfile);
		checkNull(config);
		try {
			InputStream input = FileUtils
					.makeInputBuffered(new FileInputStream(bmpfile));
			Bitmap srcbmp = BitmapFactory.decodeStream(input);
			input.close();
			if (srcbmp != null) {
				Bitmap newBmp = create180RotatedBitmap(srcbmp, config);
				FileUtils.createNewFile(bmpfile);
				OutputStream out = FileUtils
						.makeOutputBuffered(new FileOutputStream(bmpfile));
				newBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();
				srcbmp.recycle();
				newBmp.recycle();

				return true;
			}
		} catch (IOException e) {
			Utils.loge(e);
		}

		return false;
	}

	public static boolean create180RotatedBitmap(String bmpPath,
			Bitmap.Config config) {
		checkStringNullOrEmpty(bmpPath);
		checkNull(config);
		return create180RotatedBitmap(new File(bmpPath), config);
	}

	public static boolean create180RotatedBitmap(File srcFile, File destFile,
			Bitmap.Config config) {
		checkNull(srcFile);
		checkNull(destFile);
		checkNull(config);
		try {
			if (FileUtils.doesExisted(srcFile)) {
				InputStream input = FileUtils
						.makeInputBuffered(new FileInputStream(srcFile));
				Bitmap srcbmp = BitmapFactory.decodeStream(input);
				input.close();
				if (srcbmp != null) {
					Bitmap newBmp = create180RotatedBitmap(srcbmp, config);
					FileUtils.createNewFile(destFile);
					OutputStream out = FileUtils
							.makeOutputBuffered(new FileOutputStream(destFile));
					newBmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.close();
					srcbmp.recycle();
					newBmp.recycle();

					return true;
				}
			}
		} catch (IOException e) {
			Utils.loge(e);
		}

		return false;
	}

	public static boolean create180RotatedBitmap(String srcPath,
			String destPath, Bitmap.Config config) {
		checkStringNullOrEmpty(srcPath);
		checkStringNullOrEmpty(destPath);
		checkNull(config);
		return create180RotatedBitmap(new File(srcPath), new File(destPath),
				config);
	}

	public static byte[] create180RotatedBitmap(byte[] data, int offset,
			int length, Bitmap.Config config) {
		checkArrayNullOrEmpty(data);
		checkNull(config);
		try {
			Bitmap srcbmp = BitmapFactory.decodeByteArray(data, offset, length);
			if (srcbmp != null) {
				Bitmap newBmp = create180RotatedBitmap(srcbmp, config);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(524288);
				newBmp.compress(Bitmap.CompressFormat.JPEG, 100,
						FileUtils.makeOutputBuffered(baos));
				srcbmp.recycle();
				newBmp.recycle();
				byte[] newData = baos.toByteArray();
				baos.close();

				return newData;
			}
		} catch (IOException e) {
			Utils.loge(e);
		}

		return null;
	}

	public static Bitmap create180RotatedBitmap(Bitmap srcbmp,
			Bitmap.Config config) {
		Matrix m = new Matrix();
		m.postRotate(180.0F);
		Bitmap newBmp = Bitmap.createBitmap(srcbmp, 0, 0, srcbmp.getWidth(),
				srcbmp.getHeight(), m, true);
		return newBmp;
	}

	public static int getZoomOutRate(InputStream input, Rect targetSize) {
		checkNull(input);
		check((targetSize != null) && (!targetSize.isEmpty()));
		Rect size = new Rect();
		boolean res = getZoomOutBitmapBound(input, 1, size);
		if (res) {
			return getZoomOutRate(size, targetSize);
		}
		return 0;
	}

	private static int getZoomOutRate(Rect srcSize, Rect targetSize) {
		int srcWidth = srcSize.width();
		int srcHeight = srcSize.height();
		int targetWidth = targetSize.width();
		int targetHeight = targetSize.height();

		if ((srcWidth < targetWidth) && (srcHeight < targetHeight)) {
			return 0;
		}

		int count = 0;
		int newWidth = 0;
		int newHeight = 0;
		do {
			count++;
			newWidth = (int) (srcWidth / Math.pow(2.0D, count));
			newHeight = (int) (srcHeight / Math.pow(2.0D, count));
		} while ((newWidth >= targetWidth) || (newHeight >= targetHeight));

		return (int) Math.pow(2.0D, count);
	}

	public static int getZoomOutRate(File inputFile, Rect targetSize) {
		check((inputFile != null) && (inputFile.exists())
				&& (inputFile.isFile()));
		check((targetSize != null) && (!targetSize.isEmpty()));
		return getZoomOutRate(
				new BufferedInputStream(FileUtils.getFileInputStream(inputFile)),
				targetSize);
	}

	public static int getZoomOutRate(String inputPath, Rect targetSize) {
		return getZoomOutRate(new File(inputPath), targetSize);
	}

	public static int getZoomOutRate(byte[] data, int offset, int length,
			Rect targetSize) {
		checkArrayNullOrEmpty(data);
		check((targetSize != null) && (!targetSize.isEmpty()));
		ByteArrayInputStream bais = new ByteArrayInputStream(data, offset,
				length);
		return getZoomOutRate(new BufferedInputStream(bais), targetSize);
	}

	public static void compressToFile(File outputFile, Bitmap bmp,
			Bitmap.CompressFormat format) {
		try {
			checkNull(outputFile);
			check((bmp != null) && (!bmp.isRecycled()));
			checkNull(format);
			FileUtils.createNewFile(outputFile);
			OutputStream os = new BufferedOutputStream(
					FileUtils.getFileOutputStream(outputFile));
			bmp.compress(format, 100, os);
			os.close();
		} catch (IOException e) {
			Utils.loge(e);
		}
	}

	public static void recycleBitmap(Bitmap bmp) {
		if ((bmp != null) && (!bmp.isRecycled()))
			bmp.recycle();
	}
	
	public static void checkNull(Object obj) {
		if ((obj == null) && (Constants.DEBUG))
			throw new NullPointerException();
	}
	
	public static void check(boolean isTrue) {
		if ((!isTrue) && (Constants.DEBUG))
			throw new IllegalArgumentException("isTrue must be TRUE!");
	}
	
	public static void checkArrayNullOrEmpty(byte[] array) {
		if ((array == null) && (Constants.DEBUG)) {
			throw new NullPointerException();
		}

		if ((array.length == 0) && (Constants.DEBUG))
			throw new IllegalArgumentException("array must have some datas");
	}
	
	public static void checkStringNullOrEmpty(CharSequence str) {
		if ((TextUtils.isEmpty(str)) && (Constants.DEBUG))
			throw new IllegalArgumentException("str must have some datas");
	}
}