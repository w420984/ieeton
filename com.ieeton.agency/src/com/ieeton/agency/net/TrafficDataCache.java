package com.ieeton.agency.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class TrafficDataCache {

    private static final String PATH_DELIMITER = "//";
    private static final String CACHE_SDCARD_FOLDER = "sina" + PATH_DELIMITER + "weibo";
    public static final int SDCARD_MODE = 1;
    public static final int MEMORY_MODE = 2;

    private Context context;
    private int mode;
    private String subPath;

    public TrafficDataCache(Context context, String subPath, int mode) {
        this.context = context;
        this.mode = mode;
        this.subPath = subPath;
    }

    public Object getFromCache() {
        String filePath = getCacheFolder(context) + subPath;
        if (new File(filePath).exists()) {
            return load(filePath);
        } else {
            if (mode == SDCARD_MODE) {
                String filePathInCache = context.getCacheDir().getPath() + subPath;
                if (new File(filePathInCache).exists()) {
                    return load(filePathInCache);
                }
            }
            return null;
        }
    }

    public void saveToCache(Object obj) {
        String filePath = getCacheFolder(context) + subPath;
        String dirPath = new File(filePath).getParent();
        File dir = new File(dirPath);
        while (!dir.exists()) {
            dir.mkdirs();
        }
        deleteFile(filePath);
        save(obj, filePath);
    }

    private boolean save(Object obj, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean deleteFile(String filePath) {
        // TODO Auto-generated method stub
        File file = new File(filePath);
        return file.delete();
    }

    private Object load(String path) {
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
                    // do nothing
                } catch (ClassCastException e) {
                    // fix MBAD-1758 monkey crash
                }
                ois.close();
            }
        } catch (IOException e) {

        }
        return obj;
    }

    private String getCacheFolder(Context context) {
        String path = "";
        File sdDir = null;
        // 判断sd卡是否存在
        if (mode == SDCARD_MODE) {
            boolean sdCardExist = Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED);
            if (sdCardExist) {
                sdDir = Environment.getExternalStorageDirectory();// 获取根目录
            }
            if (sdDir != null && haveFreeSpace()) {
                path = sdDir.toString() + PATH_DELIMITER + CACHE_SDCARD_FOLDER;
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
            } else {
                path = context.getCacheDir().getPath();
            }
        } else if (mode == MEMORY_MODE) {
            path = context.getCacheDir().getPath();
        }

        return path;
    }

    public static boolean haveFreeSpace() {
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            StatFs st = new StatFs(Environment.getExternalStorageDirectory().getPath());
            int blockSize = st.getBlockSize();
            long available = st.getAvailableBlocks();
            long availableSize = (blockSize * available);
            if (availableSize < 1024 * 1024 * 10) {// //sd卡空间如果小于10M，就认为sd卡空间不足
                return false;
            }
            return true;
        }
        return false;
    }

}
