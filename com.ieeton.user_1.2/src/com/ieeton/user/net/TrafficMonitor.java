package com.ieeton.user.net;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

import com.ieeton.user.net.NetUtils.NetworkState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;


public class TrafficMonitor {

    public static final int NET_MOUDLE_WEIBO = 901;

    public static final int NET_MOUDLE_APPMAKERT = 902;

    public static final int NET_MOUDLE_DIANXIN = 903;

    public static final int NET_MOUDLE_POPUPSDK = 904;

    public static final String NET_MOUDLE_NAME_WEIBO = "weibo";

    public static final String NET_MOUDLE_NAME_APPMAKERT = "appmarket";

    public static final String NET_MOUDLE_NAME_DIANXIN = "dianxin";

    public static final String NET_MOUDLE_NAME_POPUPSDK = "popupsdk";

    public static String BACK_TO_BACKGROUND = "com.sina.weibo.action.BACK_TO_BACKGROUND";

    public static String BACK_TO_FORGROUND = "com.sina.weibo.action.BACK_TO_FORGROUND";

    private static TrafficMonitor instance;

    private List<TrafficInfo> mInfos;

    private TrafficDataCache mDataSource;

    private static String TRAFFIC_CACHE = "traffic_cache";

    private BlockingQueue<TrafficRecordTask> mTasks = new LinkedBlockingQueue<TrafficMonitor.TrafficRecordTask>();

    private boolean mIsRunning = false;

    private Thread mThread = null;

    private int mCount;

    private int MAX_COUNT = 30;

    private Context mContext;

    private BroadcastReceiver mReceiver;

    private NetworkState mNetState = NetworkState.NOTHING;

    public static boolean gIsForground = true;

    private TrafficMonitor(Context context) {
        mContext = context.getApplicationContext();
        mDataSource = new TrafficDataCache(mContext, "/" + TRAFFIC_CACHE,
                TrafficDataCache.MEMORY_MODE);

        mReceiver = new StateBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);// 监听网络状态变化
        filter.addAction(BACK_TO_FORGROUND);
        filter.addAction(BACK_TO_BACKGROUND);
        mContext.registerReceiver(mReceiver, filter);

        mInfos = loadTrafficInfos();

        if (mInfos == null) {
            mInfos = new ArrayList<TrafficMonitor.TrafficInfo>();
            mInfos.add(new TrafficInfo(NET_MOUDLE_WEIBO, NET_MOUDLE_NAME_WEIBO));
            mInfos.add(new TrafficInfo(NET_MOUDLE_APPMAKERT, NET_MOUDLE_NAME_APPMAKERT));
            mInfos.add(new TrafficInfo(NET_MOUDLE_DIANXIN, NET_MOUDLE_NAME_DIANXIN));
            mInfos.add(new TrafficInfo(NET_MOUDLE_POPUPSDK, NET_MOUDLE_NAME_POPUPSDK));
            mDataSource.saveToCache(mInfos);
        }

        startHandle();

    }

    public synchronized static TrafficMonitor getInstace(Context context) {
        if (instance == null) {
            instance = new TrafficMonitor(context);
        }

        return instance;
    }

    public void recordTxTraffic(int mId, HttpUriRequest request) {
        if (request == null) {
            return;
        }

        mTasks.add(new TrafficRecordTask(mId, request, mNetState, gIsForground));

    }

    public void recordRxTraffic(int mId, HttpResponse response) {
        if (response == null) {
            return;
        }

        mTasks.add(new TrafficRecordTask(mId, response, mNetState, gIsForground));
    }

    private void startHandle() {
        mIsRunning = true;

        mThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (mIsRunning) {
                        mCount++;
                        TrafficRecordTask task = mTasks.take();
                        task.run();

                        // 每隔一定次数把数据同步到文件保存起来
                        if (mCount > MAX_COUNT) {
                            mDataSource.saveToCache(mInfos);
                            mCount = 0;
                        }

                    }
                } catch (InterruptedException e) {
                    // TODO: handle exception
                } finally {
                    mDataSource.saveToCache(mInfos);
                    mIsRunning = false;
                }
            }
        });
        mThread.setName("TrafficMonitor-Thread");
        mThread.start();
    }

    public void stopHandle() {
        mIsRunning = false;
        if (mThread != null) {
            mThread.interrupt();
        }

        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
    }

    public List<TrafficInfo> getTrafficInfo() {
        return mInfos;
    }

    class StateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BACK_TO_BACKGROUND.equals(action)) {
                gIsForground = false;
            } else if (BACK_TO_FORGROUND.equals(action)) {
                gIsForground = true;
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                mNetState = NetUtils.getNetworkState(mContext);
            }

        }
    }

    class TrafficRecordTask implements Runnable {

        private WeakReference<HttpUriRequest> mRequest;

        private WeakReference<HttpResponse> mResponse;

        private NetworkState netState;

        private boolean isForgroud;

        private int mId;

        public TrafficRecordTask(int mId, HttpUriRequest request, NetworkState netState,
                boolean isForgroud) {
            this.mId = mId;
            this.mRequest = new WeakReference<HttpUriRequest>(request);
            this.netState = netState;
            this.isForgroud = isForgroud;
        }

        public TrafficRecordTask(int mId, HttpResponse response, NetworkState netState,
                boolean isForgroud) {
            this.mId = mId;
            this.mResponse = new WeakReference<HttpResponse>(response);
            this.netState = netState;
            this.isForgroud = isForgroud;
        }

        @Override
        public void run() {
            if (mRequest != null) {
                HttpUriRequest request = mRequest.get();
                if (request != null) {
                    calcTxBytes(request);
                }
            }

            if (mResponse != null) {
                HttpResponse response = mResponse.get();
                if (response != null) {
                    calcRxBytes(response);
                }
            }

        }

        private void calcTxBytes(HttpUriRequest request) {
            TrafficInfo info = getTrafficInfoById(mId);
            if (info != null) {
                long txBytes = 0;

                // 加上请求行
                txBytes += request.getRequestLine().toString().getBytes().length;

                // 累加所有头信息
                Header[] headers = request.getAllHeaders();
                if (headers != null) {
                    for (Header header : headers) {
                        txBytes += header.getName().getBytes().length;
                        String value = header.getValue();
                        if (value != null) {
                            txBytes += value.getBytes().length;
                        }
                    }
                }

                if (request instanceof HttpPost) {
                    // post方法加上实体长度
                    HttpEntity entity = ((HttpPost) request).getEntity();
                    if (entity != null) {
                        txBytes += entity.getContentLength();
                    }

                }

                if (netState == NetworkState.WIFI) {
                    info.addWifiTxBytes(isForgroud, txBytes);
                } else if (netState == NetworkState.MOBILE) {
                    info.addMobileTxBytes(isForgroud, txBytes);
                }

            }
        }

        private void calcRxBytes(HttpResponse response) {
            TrafficInfo info = getTrafficInfoById(mId);
            if (info != null) {
                long rxBytes = 0;

                // 加上首行
                rxBytes += response.getStatusLine().toString().getBytes().length;

                // 累加所有头
                Header[] headers = response.getAllHeaders();
                if (headers != null) {
                    for (Header header : headers) {
                        rxBytes += header.getName().getBytes().length;
                        String value = header.getValue();
                        if (value != null) {
                            rxBytes += value.getBytes().length;
                        }
                    }
                }

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    rxBytes += entity.getContentLength();
                }

                if (netState == NetworkState.WIFI) {
                    info.addWifiRxBytes(isForgroud, rxBytes);
                } else if (netState == NetworkState.MOBILE) {
                    info.addMobileRxBytes(isForgroud, rxBytes);
                }
            }
        }

    }

    public static class TrafficInfo implements Serializable {

        private static final long serialVersionUID = -8951775264426542125L;

        private int mId;

        private String mName;

        private TrafficHolder mBackground;

        private TrafficHolder mForground;

        public TrafficInfo(int moudleId, String name) {
            this.mId = moudleId;
            this.mName = name;
            mBackground = new TrafficHolder();
            mForground = new TrafficHolder();
        }

        public int getmId() {
            return mId;
        }

        public void setmId(int mId) {
            this.mId = mId;
        }

        public String getmName() {
            return mName;
        }

        public void setmName(String mName) {
            this.mName = mName;
        }

        public TrafficHolder getmBackground() {
            return mBackground;
        }

        public void setmBackground(TrafficHolder mBackground) {
            this.mBackground = mBackground;
        }

        public TrafficHolder getmForground() {
            return mForground;
        }

        public void setmForground(TrafficHolder mForground) {
            this.mForground = mForground;
        }

        public long getTotalBytes() {
            return mForground.getTotalBytes() + mBackground.getTotalBytes();
        }

        public void addWifiRxBytes(boolean isForgroud, long bytes) {
            if (isForgroud) {
                mForground.addWifiRxBytes(bytes);
            } else {
                mBackground.addWifiRxBytes(bytes);
            }
        }

        public void addWifiTxBytes(boolean isForgroud, long bytes) {
            if (isForgroud) {
                mForground.addWifiTxBytes(bytes);
            } else {
                mBackground.addWifiTxBytes(bytes);
            }
        }

        public void addMobileRxBytes(boolean isForgroud, long bytes) {
            if (isForgroud) {
                mForground.addMobileRxBytes(bytes);
            } else {
                mBackground.addMobileRxBytes(bytes);
            }
        }

        public void addMobileTxBytes(boolean isForgroud, long bytes) {
            if (isForgroud) {
                mForground.addMobileTxBytes(bytes);
            } else {
                mBackground.addMobileTxBytes(bytes);
            }
        }

    }

    public static class TrafficHolder implements Serializable {

        private static final long serialVersionUID = 1033235514066319756L;

        private long mWifiTx;
        private long mWifiRx;
        private long mMobileTx;
        private long mMobileRx;

        public long getmWifiTx() {
            return mWifiTx;
        }

        public void setmWifiTx(long mWifiTx) {
            this.mWifiTx = mWifiTx;
        }

        public long getmWifiRx() {
            return mWifiRx;
        }

        public void setmWifiRx(long mWifiRx) {
            this.mWifiRx = mWifiRx;
        }

        public long getmMobileTx() {
            return mMobileTx;
        }

        public void setmMobileTx(long mMobileTx) {
            this.mMobileTx = mMobileTx;
        }

        public long getmMobileRx() {
            return mMobileRx;
        }

        public long getTotalBytes() {
            return mWifiTx + mWifiRx + mMobileTx + mMobileRx;
        }

        public void setmMobileRx(long mMobileRx) {
            this.mMobileRx = mMobileRx;
        }

        public void addWifiRxBytes(long bytes) {
            mWifiRx += bytes;
        }

        public void addWifiTxBytes(long bytes) {
            mWifiTx += bytes;
        }

        public void addMobileRxBytes(long bytes) {
            mMobileRx += bytes;
        }

        public void addMobileTxBytes(long bytes) {
            mMobileTx += bytes;
        }

        @Override
        public String toString() {
            return "TrafficHolder [mWifiTx=" + mWifiTx + ", mWifiRx=" + mWifiRx + ", mMobileTx="
                    + mMobileTx + ", mMobileRx=" + mMobileRx + "]";
        }

    }

    private List<TrafficInfo> loadTrafficInfos() {
        return (List<TrafficInfo>) mDataSource.getFromCache();
    }

    private TrafficInfo getTrafficInfoById(int moudleId) {
        if (mInfos != null) {
            for (TrafficInfo info : mInfos) {
                if (info.getmId() == moudleId) {
                    return info;
                }
            }
        }
        return null;
    }

}
