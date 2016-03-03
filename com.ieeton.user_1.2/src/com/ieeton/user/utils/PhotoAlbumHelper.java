package com.ieeton.user.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;

public class PhotoAlbumHelper {
    private static final Uri URI = Images.Media.EXTERNAL_CONTENT_URI;
    private static final String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
    // SELECT ... FROM ... WHERE (1) GROUP BY 1,(2)
    private static final String BUCKET_ORDER_BY = "MAX(datetaken) DESC";
    private static final String COUNT_PROJECTION = "count(*)";
    private static final String IMAGE_ORDER_BY = ImageColumns.DATE_TAKEN + " DESC";

    private static String[] BucketSelection = { ImageColumns.BUCKET_ID,
            ImageColumns.BUCKET_DISPLAY_NAME, ImageColumns.DATE_TAKEN, ImageColumns.DATA,
            ImageColumns.DISPLAY_NAME, COUNT_PROJECTION };
    private static String[] ImageSelection = { ImageColumns._ID, ImageColumns.DATA,
            ImageColumns.DATE_TAKEN };

    public static final class BucketInfo {
        private int id;
        private String name = "";
        private int count = 0;
        private String path = "";
        private String imgPath = "";
        private int position = Integer.MAX_VALUE;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getImgPath() {
            return imgPath;
        }

        public void setImgPath(String imgPath) {
            this.imgPath = imgPath;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String toString() {
            return "BucketInfo id=" + id + " name=" + name + " count=" + count + " path=" + path
                    + " imgPath=" + imgPath;
        }
    }

    public static final class ImageInfo {
        private int id;
        private String path = "";
        private boolean isSelect = false;
        private int position = Integer.MAX_VALUE;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isSelect() {
            return isSelect;
        }

        public void setSelect(boolean isSelect) {
            this.isSelect = isSelect;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String toString() {
            return "ImageInfo id=" + id + " path=" + path;
        }
    }

    public static List<ImageInfo> getImageByBucket(Context context, int bucketid) {

        String where = ImageColumns.BUCKET_ID + " = ?";

        Cursor cursor = context.getContentResolver().query(URI, ImageSelection, where,
                new String[] { String.valueOf(bucketid) }, IMAGE_ORDER_BY);

        List<ImageInfo> list;
        if (cursor != null) {
            list = new ArrayList<ImageInfo>(cursor.getCount());
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ImageInfo imageinfo = cursor2ImageInfo(cursor);
                list.add(imageinfo);
            }
            cursor.close();
        } else {
            list = Collections.emptyList();
        }
        return list;

    }

    private static List<ImageInfo> getRecentList(Context context, Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = context.getContentResolver().query(uri, projection, selection,
                selectionArgs, sortOrder);

        List<ImageInfo> list;
        if (cursor != null) {
            list = new ArrayList<ImageInfo>(cursor.getCount());
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ImageInfo imageinfo = cursor2ImageInfo(cursor);
                // 检测文件夹是否存在，防止文件被删除，图库数据未更新
                if (FileUtils.isFileExist(imageinfo.getPath())) {
                    list.add(imageinfo);
                }
            }
            cursor.close();
        } else {
            list = Collections.emptyList();
        }
        return list;
    }

    public static List<ImageInfo> getRecentList(Context context, int number) {
        if (number > 0) {
            String limit = " LIMIT " + number;
            return getRecentList(context, URI, ImageSelection, null, null, IMAGE_ORDER_BY + limit);
        } else {
            return null;
        }
    }

    /**
     * 某路径下的最新
     * 
     * @param context
     * @param number
     * @param path
     * @return
     */
    public static List<ImageInfo> getRecentListOnePath(Context context, int number, String path) {
        if (number > 0) {
            String limit = " LIMIT " + number;
            String where = null;
            if (path != null) {
                where = ImageColumns.DATA + " LIKE '" + path + "%'";
            }
            return getRecentList(context, URI, ImageSelection, where, null, IMAGE_ORDER_BY + limit);
        } else {
            return null;
        }
    }

    /**
     * 不包含某路径下的图片
     * 
     * @param context
     * @param number
     * @param path
     * @return
     */
    public static List<ImageInfo> getRecentListNotPath(Context context, int number, String path) {
        if (number > 0) {
            String limit = " LIMIT " + number;
            String where = null;
            if (path != null) {
                where = ImageColumns.DATA + " NOT LIKE '" + path + "%'";
            }
            return getRecentList(context, URI, ImageSelection, where, null, IMAGE_ORDER_BY + limit);
        } else {
            return Collections.emptyList();
        }
    }

    public static List<BucketInfo> getBucketList(Context context) {

        Cursor cursor = context.getContentResolver().query(URI, BucketSelection, BUCKET_GROUP_BY,
                null, BUCKET_ORDER_BY);

        List<BucketInfo> list;
        if (cursor != null) {
            list = new ArrayList<BucketInfo>(cursor.getCount());
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                BucketInfo bucketinfo = cursor2BucketInfo(cursor);
                // 检测文件夹是否存在，防止文件夹被删除，图库数据未更新
                if (FileUtils.isDirectoryExist(bucketinfo.getPath())) {
                    list.add(bucketinfo);
                }
            }
            cursor.close();
        } else {
            list = Collections.emptyList();
        }
        return list;
    }

    private static ImageInfo cursor2ImageInfo(Cursor cursor) {
        ImageInfo imageinfo = new ImageInfo();

        try {
            imageinfo.setId(cursor.getInt(cursor.getColumnIndex(ImageColumns._ID)));
            imageinfo.setPath(cursor.getString(cursor.getColumnIndex(ImageColumns.DATA)));
        } catch (Exception e) {
        }
        return imageinfo;
    }

    private static BucketInfo cursor2BucketInfo(Cursor cursor) {
        BucketInfo bucketinfo = new BucketInfo();

        try {
            bucketinfo.setId(cursor.getInt(cursor.getColumnIndex(ImageColumns.BUCKET_ID)));
            bucketinfo.setName(cursor.getString(cursor
                    .getColumnIndex(ImageColumns.BUCKET_DISPLAY_NAME)));

            String name = cursor.getString(cursor.getColumnIndex(ImageColumns.DISPLAY_NAME));
            String imgPath = cursor.getString(cursor.getColumnIndex(ImageColumns.DATA));

            bucketinfo.setPath(imgPath.substring(0, imgPath.length() - name.length() - 1));
            bucketinfo.setImgPath(imgPath);
            bucketinfo.setCount(cursor.getInt(cursor.getColumnIndex(COUNT_PROJECTION)));
        } catch (Exception e) {
        }
        return bucketinfo;
    }

    /**
     * 通过uri 获取绝对路径
     * 
     * @param context
     * @param uri
     * @return
     */
    public static String getRealPathFromURI(Context context, Uri uri) {
//        String[] proj = { MediaStore.Images.Media.DATA };
//        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
//        String data = null;
//        if (cursor != null) {
//            cursor.moveToFirst();
//            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
//            data = cursor.getString(idx);
//            cursor.close();
//        }
//        return data;
        
       return Utils.getUriFilePath(uri.toString(), context);
    }
}
