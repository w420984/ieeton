package com.ieeton.user.activity;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ieeton.user.R;
import com.ieeton.user.utils.BitmapHelper;
import com.ieeton.user.utils.FileUtils;
import com.ieeton.user.utils.PhotoAlbumHelper;
import com.ieeton.user.utils.Utils;
import com.ieeton.user.view.CustomToast;
import com.ieeton.user.view.ImageSquareGrideView;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 相册 <br>
 * 传入参数PARAM_DATA_SELECT_NUMBER确定可选择的数量:默认1,单选<br>
 * 传入参数PARAM_DATA_SHOW_RECENT_NUMBER确定顶部最近照片显示张数:默认7<br>
 * 传入参数PARAM_DATA_COLUMS_NUMBER确定每行显示的图片个数默认4，负值为自动适配<br>
 * 传入参数PARAM_DATA预选的图片带入(PicAttachmentList)<br>
 * 返回参数RETURN_DATA(PicAttachmentList)
 * 
 * @author shenrh
 * 
 */
public class PhotoAlbumActivity extends TemplateActivity {
    public static final String TAG = "PhotoAlbumActivity";

    /**
     * 返回数据标识，返回数据为PicAttachmentList
     */
    public static final String RETURN_DATA = "album_return_data";

    /**
     * 传入数据标识
     */
    public static final String PARAM_DATA = "album_param_data";

    /**
     * 调用类型
     */
    public static final String PARAM_DATA_CALL_TYPE = "album_param_data_call_type";

    /**
     * 选取张数(单选多选模式)
     */
    public static final String PARAM_DATA_SELECT_NUMBER = "album_param_data_select_number";

    /**
     * 顶部最近图片显示张数可配置
     */
    public static final String PARAM_DATA_SHOW_RECENT_NUMBER = "album_param_data_show_recent_number";

    /**
     * 每行显示图片个数（默认都显示4个；if <0 则根据屏幕自动适配，480显示4个。顶部最近图片显示张数如果没指定默认为该值*2-1，）
     */
    public static final String PARAM_DATA_COLUMS_NUMBER = "album_param_data_colums_number";

    /*
     * That is Nexus-One stores its Camera files in to folder named "Camera"
     * (/sdcard/DCIM/Camera). HTC Incredible stores its camera files into a
     * folder named "100MEDIA" (/sdcard/DCIM/100MEDIA). Sony Xperia x10 stores
     * its camera files into folder named "100ANDRO" (/sdcard/DCIM/100ANDRO)
     */
    private static final String CAMERA_FOLDER = Environment.getExternalStorageDirectory().getPath()
            + "/DCIM/";// 手机相册所在文件夹
    // 为了尽量找准相册文件夹
    private static final String CAMERA_FOLDER_CAMERA = "Camera";// 手机相册所在文件夹
    private static final String CAMERA_FOLDER_100MEDIA = "100MEDIA";// 手机相册所在文件夹
    private static final String CAMERA_FOLDER_100ANDRO = "100ANDRO";// 手机相册所在文件夹

    private static final String FILE_URI = "file://";
    // file:///mnt/sdcard/
    private static final String CONTENT_URI = "content://";
    // content://media/external/images/media/31846

    private static final int THREAD_NUMBER = 4;// 启动加载线程数
    private static final int DEFAULT_SELECT_NUMBER = 1;// 默认选择图片张数单选模式)
    private static final int DEFAULT_COLUMS_NUMBER = 4;// 默认每行显示图片张数

    private static int MAX_IMAGE_SIZE = 0;// 读取图片缩略图的最大尺寸 dimens配置

    public static final String CALL_TYPE = "call_type";// 调用类型
    public static final String SELECT_LIST = "select_list";// 当前选择
    public static final String INPUT_LIST = "input_list";// 外部传入预设
    public static final String SELECT_NUMBER = "select_number";// 可选择张数
    public static final String RECENT_NUMBER = "recent_number";// 显示的最近张数
    public static final String AUTO_ADAPTER = "auto_adapter";// 自适应屏幕
    public static final String IS_SHOW_IMAGE = "is_show_image";// 当前显示页面

    private static final int REQUEST_CODE_PIC_CROP = 200;// 打开图片裁剪
    private static final int REQUEST_CODE_PIC_9CUT = 300;// 打开图片裁剪

    private final int LOAD_RECENT_DATA = 111;// 最新照片
    private final int LOAD_BUCKET_DATA = LOAD_RECENT_DATA + 1;// 相册列表
    private final int LOAD_IMAGE_DATA = LOAD_BUCKET_DATA + 1;// 相册照片
    private final int LOAD_IMAGE = LOAD_IMAGE_DATA + 1;// 相册页面下载图片
    private final int LOAD_BUCKET_IMAGE = LOAD_IMAGE + 1;// 相册封面图片下载
    private final int LOAD_RECENT_IMAGE = LOAD_BUCKET_IMAGE + 1;// 最新图片下载
    private final int LOAD_SELECT_IMAGE = LOAD_RECENT_IMAGE + 1;// 已经选择图片

    private int mCallType; // 被调用类型（暂时使用于TYPE_WATERMARK）
    
    private ExecutorService mLoadImageTaskExecutor;

    private CustomToast mLoadingDialog;

    private ImageSquareGrideView mImageGridView;
    private GridViewImageAdapter mImageGridViewAdapter;
    private List<PhotoAlbumHelper.ImageInfo> mImageList;// 图片
    private ListView mBucketListView;
    private ListViewAdapter mBucketListViewAdapter;
    private List<PhotoAlbumHelper.BucketInfo> mBucketList;// 相册

    private LayoutInflater mInflater;

    private RelativeLayout mSelectRelativeLayout;// 显示选择照片区域
    private HorizontalScrollView mHorizontalScrollView;
    private HashMap<String, ImageView> mSelectImageListView;// 选择图片
    private ArrayList<String> mSelectImageList;// 选择图片路径
    private LinearLayout mSelectImageLinearLayout;// 选择的图片展示
    private Button mSaveButton;

    private List<String> mLoadingList;// 正在下载

    private int mBucketId;// 当前显示相册id
    private String mBucketDisplayName;// 当前显示相册名字

    // 获取图片
    private enum FetchType {
        BUCKET, IMAGE
    }

    private int mGridScrollState;
    private int mLastListPosition;

    // 为了可替换皮肤
    private int mDefaultImageId;
    private int mDefaultSelectId;
    // private int mDefaultRemoveId;

    private ArrayList<String> mPicAttachmentList;// 保存传入参数

    private int mSelectNumber = DEFAULT_SELECT_NUMBER;// 1
    private int mNumColums = DEFAULT_COLUMS_NUMBER;// 4

    private boolean mAutoAdapter = false;

    // Title
    private RelativeLayout mTitleView;
    private LinearLayout mTitleLeft;
    private LinearLayout mTitleRight;
    private TextView mTitleLeftButton;
    private TextView mTitleRightButton;
    private TextView mTitleText;

    // 没有照片
    private LinearLayout mEmptyLinearLayout;

    // 当前显示相册还是照片
    private boolean mIsShowImage;

    // 缓存已选择的图片
    private Map<String, Bitmap> mBmpShowMap = new HashMap<String, Bitmap>();

    // 缓存封面和最近
    private Map<String, SoftReference<Bitmap>> mBmpBucketMap = new HashMap<String, SoftReference<Bitmap>>();

    // 缓存图片列表
    private static final int MAX_NUMBER_CACHE = 80;
    private LinkedHashMap<String, Bitmap> mBmpCacheMap = new LinkedHashMap<String, Bitmap>(32,
            0.75f, true) {
        private static final long serialVersionUID = 7147209588009264741L;

        @Override
        protected boolean removeEldestEntry(Entry<String, Bitmap> eldest) {
            if (size() > MAX_NUMBER_CACHE) {
                if (eldest != null && eldest.getValue() != null && !eldest.getValue().isRecycled()) {
                    eldest.getValue().recycle();
                }
                return true;
            }
            return false;
        }
    };

    private synchronized void saveBitmapCache(String path, Bitmap bm) {
        if (mBmpCacheMap == null || bm == null || bm.isRecycled() || TextUtils.isEmpty(path)) {
            return;
        }

        mBmpCacheMap.put(path, bm);
    }

    private synchronized void removeBitmapCache(String path) {
        if (TextUtils.isEmpty(path) || !mBmpCacheMap.containsKey(path)) {
            return;
        }
        // just remove do not recyle
        mBmpCacheMap.remove(path);
    }

    private synchronized void saveShowCache(String path, Bitmap bm) {
        if (mBmpShowMap == null || bm == null || bm.isRecycled() || TextUtils.isEmpty(path)
                || mBmpShowMap.containsKey(path)) {
            return;
        }
        mBmpShowMap.put(path, bm);
    }

    private synchronized void saveBucketCache(String path, Bitmap bm) {
        if (mBmpBucketMap == null || bm == null || bm.isRecycled() || TextUtils.isEmpty(path)
                || mBmpBucketMap.containsKey(path)) {
            return;
        }
        mBmpBucketMap.put(path, new SoftReference<Bitmap>(bm));
    }

    private synchronized void removeBitmapShow(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        if (mBmpShowMap.containsKey(path)) {
            saveBitmapCache(path, mBmpShowMap.get(path));// 放入缓存
            mBmpShowMap.remove(path);
        }
    }

    private void moveCacheToBucket(String path, Bitmap bmp) {
        saveBucketCache(path, bmp);
        removeBitmapCache(path);
    }

    private void moveCacheToSelect(String path, Bitmap bmp) {
        saveShowCache(path, bmp);
        removeBitmapCache(path);
    }

    private synchronized Bitmap getBitmapCache(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        if (mBmpShowMap != null && mBmpShowMap.containsKey(path)) {
            Bitmap bmp = mBmpShowMap.get(path);
            if (bmp == null || bmp.isRecycled()) {
                mBmpShowMap.remove(path);
            }
            return bmp;
        }

        if (mBmpBucketMap != null && mBmpBucketMap.containsKey(path)) {
            Bitmap bmp = mBmpBucketMap.get(path).get();
            if (bmp == null || bmp.isRecycled()) {
                mBmpBucketMap.remove(path);
            }
            return bmp;
        }

        if (mBmpCacheMap != null && mBmpCacheMap.containsKey(path)) {
            Bitmap bmp = mBmpCacheMap.get(path);
            if (bmp == null || bmp.isRecycled()) {
                mBmpCacheMap.remove(path);
            }
            return bmp;
        }
        return null;
    }

    private synchronized void recycleBitmap() {
        int sizeShow = mBmpShowMap.size();
        for (int i = 0; i < sizeShow; i++) {
            Bitmap bmp = mBmpShowMap.get(i);
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
            }
            bmp = null;
        }
        mBmpShowMap.clear();
        mBmpShowMap = null;

        int sizeBucket = mBmpBucketMap.size();
        for (int i = 0; i < sizeBucket; i++) {
            final SoftReference<Bitmap> bmpSoftRef = mBmpBucketMap.get(i);
            if (bmpSoftRef != null) {
                Bitmap bmp = bmpSoftRef.get();
                if (bmp != null && !bmp.isRecycled()) {
                    bmp.recycle();
                }
                bmp = null;
            }
        }
        mBmpBucketMap.clear();
        mBmpBucketMap = null;

        int sizeCache = mBmpCacheMap.size();

        for (int i = 0; i < sizeCache; i++) {
            Bitmap bmp = mBmpCacheMap.get(i);
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
            }
            bmp = null;
        }
        mBmpCacheMap.clear();
        mBmpCacheMap = null;

        System.gc();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
            case LOAD_BUCKET_DATA:
                onLoadBucketDataEnd();
                break;

            case LOAD_IMAGE_DATA:
                onLoadImageDataEnd();
                break;

            case LOAD_IMAGE:
                onLoadImageEnd((Object[]) msg.obj);
                break;

            case LOAD_BUCKET_IMAGE:
                onLoadBucketImageEnd((Object[]) msg.obj);
                break;

            case LOAD_SELECT_IMAGE:
                onLoadSelectImageEnd((Object[]) msg.obj);
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 开启硬件加速
        Utils.setWindowHardWareAccelerated(this);

        super.onCreate(savedInstanceState);

        init();

        loadInstanceState(savedInstanceState);

        loadParam();
        setColums();
        setSingleMode(isSingleNotMultipleMode());
        initSkin();
        updateSaveButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isShowImageNotBucket()) {
            loadData();
        }
		MobclickAgent.onPageStart("PhotoAlbumActivity");
		MobclickAgent.onResume(this);    }

    private void setColums() {
        mImageGridView.setNumColumns(mNumColums);
    }

    /**
     * 根据屏幕像素确定显示个数
     * 
     * @return
     */
    private int getColums() {

        int cloms = 0;
        // final float scale = getResources().getDisplayMetrics().density;
        final float width = getWindowManager().getDefaultDisplay().getWidth();

        if (width > 800) {
            cloms = 8;
        } else if (width > 500) {
            cloms = 6;
        } else {
            cloms = 4;
        }

        return cloms;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mAutoAdapter) {
            mNumColums = getColums();
            setColums();
            loadData();
        }
    }

    /**
     * 单选多选模式
     * 
     * @return
     */
    private boolean isSingleNotMultipleMode() {
        if (mSelectNumber > 1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 设置单多选模式
     */
    private void setSingleMode(boolean single) {
        int gridePadding = getResources().getDimensionPixelSize(R.dimen.photo_album_gride_padding);
        int listPadding = getResources().getDimensionPixelSize(R.dimen.photo_album_list_padding);
        // int titleHeight =
        // getResources().getDimensionPixelSize(R.dimen.baselayout_title_height);

        RelativeLayout.LayoutParams lylp = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        if (single) {
            lylp.setMargins(0, 0, 0, 0);
            mBucketListView.setLayoutParams(lylp);
            mImageGridView.setLayoutParams(lylp);
            mBucketListView.setPadding(listPadding, 0, listPadding, listPadding);
            mImageGridView.setPadding(gridePadding, 0, gridePadding, getResources()
                    .getDimensionPixelSize(R.dimen.photo_album_gride_padding));
            mSelectRelativeLayout.setVisibility(View.GONE);
        } else {
            lylp.setMargins(0, 0, 0,
                    getResources().getDimensionPixelSize(R.dimen.photo_album_bottom_margin));
            mBucketListView.setLayoutParams(lylp);
            mImageGridView.setLayoutParams(lylp);
            mBucketListView.setPadding(listPadding, 0, listPadding, 0);
            mImageGridView.setPadding(gridePadding, 0, gridePadding, 0);
            mSelectRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    private void loadParam() {
        Intent intent = getIntent();
        if (intent != null) {
             
            mSelectNumber = intent.getIntExtra(PARAM_DATA_SELECT_NUMBER, DEFAULT_SELECT_NUMBER);
            mNumColums = intent.getIntExtra(PARAM_DATA_COLUMS_NUMBER, DEFAULT_COLUMS_NUMBER);

            if (mNumColums < 0) {
                // 自动适配
                mAutoAdapter = true;
                mNumColums = getColums();
            }

            if (isSingleNotMultipleMode()) {
                // 单选模式
                return;
            }
            mPicAttachmentList = (ArrayList<String>) intent.getSerializableExtra(PARAM_DATA);
            if (mPicAttachmentList != null) {
                int size = mPicAttachmentList.size();
                ArrayList<String> listPath = new ArrayList<String>(size);
                for (int i = 0; i < size; i++) {
                    String path = filtrateUriToPath(mPicAttachmentList.get(i));
                    if (path != null) {
                        listPath.add(path);
                        Utils.logd("input path:" + path);
                    }
                }
                initDataFromList(listPath);
            }
        }
    }

    private void loadData() {
        showLoadingProgress();
        fetchData(FetchType.BUCKET);
        // fetchData(FetchType.BUCKET); 串行
    }

    public void initSkin() {
        // super.initSkin();
        // title栏 ui重新设计，没按照title栏默认设计。没办法 单独适配
        mTitleView.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.navigationbar_photos_background));
        mTitleText.setTextColor(getResources().getColor(R.color.photo_album_title_color));
        mTitleRight.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.photo_album_bucket_cancel));
        mTitleLeft.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.photo_album_bucket_back));

        mSelectRelativeLayout.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.compose_photo_choose_background));
        mSaveButton.setTextColor(getResources().getColor(R.color.photo_album_save_color));
    }

    private void showLoadingProgress() {
        if (mLoadingDialog == null) {
            mLoadingDialog = Utils.createProgressCustomToast(R.string.photo_album_loading,
                    PhotoAlbumActivity.this);
        }
        mLoadingDialog.show();
    }

    private void dismissLoadingProgress() {
        if (mLoadingDialog != null) {
            mLoadingDialog.cancel();
        }
    }

    @Override
    protected void onPause() {
        dismissLoadingProgress();
        super.onPause();
		MobclickAgent.onPageEnd("PhotoAlbumActivity"); 
		MobclickAgent.onPause(this);    }

    private void init() {
        setView(R.layout.photo_album);
        // setTitleBar(BaseLayout.TYPE_NORMAL,
        // getString(R.string.photo_album_bucket_title),
        // getString(R.string.photo_album_bucket_title),
        // getString(R.string.cancel));

        ly.titlebar.setVisibility(View.GONE);

        MAX_IMAGE_SIZE = getResources().getDimensionPixelSize(R.dimen.photo_album_image_max_size);

        // mLoadImageTaskExecutor = Executors.newCachedThreadPool();
        mLoadImageTaskExecutor = Executors.newFixedThreadPool(THREAD_NUMBER);

        mSelectImageListView = new HashMap<String, ImageView>();
        mSelectImageList = new ArrayList<String>();

        mTitleView = (RelativeLayout) findViewById(R.id.photo_album_title);

        mTitleLeft = (LinearLayout) findViewById(R.id.photo_album_title_left);
        mTitleLeftButton = (TextView) findViewById(R.id.photo_album_title_left_button);
        mTitleLeft.setOnClickListener(mViewOnClickListener);
        mTitleLeftButton.setText(R.string.photo_album_bucket_title);

        mTitleRight = (LinearLayout) findViewById(R.id.photo_album_title_right);
        mTitleRightButton = (TextView) findViewById(R.id.photo_album_title_right_button);
        mTitleRight.setOnClickListener(mViewOnClickListener);
        mTitleRightButton.setText(R.string.cancel);

        mTitleText = (TextView) findViewById(R.id.photo_album_title_text);
        mTitleText.setText(R.string.photo_album_bucket_title);

        mEmptyLinearLayout = (LinearLayout) findViewById(R.id.photo_album_empty);

        mSelectRelativeLayout = (RelativeLayout) findViewById(R.id.photo_album_select_show);
        mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.photo_album_select_show_hs);
        mSelectImageLinearLayout = (LinearLayout) findViewById(R.id.photo_album_select_show_LL);

        mDefaultImageId = R.drawable.timeline_image_loading;
        mDefaultSelectId = R.drawable.compose_photo_focus; // 复用皮肤选择图标 skin_check_icon
        // mDefaultRemoveId = R.drawable.ad_close_icon;

        mInflater = LayoutInflater.from(this);
        mLoadingList = new ArrayList<String>();

        // save button
        mSaveButton = (Button) findViewById(R.id.photo_album_select_show_save_button);
        mSaveButton.setOnClickListener(mViewOnClickListener);

        // image
        mImageList = new ArrayList<PhotoAlbumHelper.ImageInfo>();
        mImageGridView = (ImageSquareGrideView) findViewById(R.id.photo_album_gridview);

        mImageGridViewAdapter = new GridViewImageAdapter();
        mImageGridView.setAdapter(mImageGridViewAdapter);
        mImageGridView.setOnItemClickListener(mGrideViewOnItemClickListener);
        mImageGridView.setOnScrollListener(mImageGrideViewOnScrollListener);
        mGridScrollState = 0;

        // bucket
        mLastListPosition = Integer.MIN_VALUE;
        mBucketList = new ArrayList<PhotoAlbumHelper.BucketInfo>();
        mBucketListView = (ListView) findViewById(R.id.photo_album_listview);

        View view = new View(this);
        view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                getResources().getDimensionPixelSize(R.dimen.baselayout_title_height)));
        mBucketListView.addHeaderView(view);

        mBucketListViewAdapter = new ListViewAdapter();
        mBucketListView.setAdapter(mBucketListViewAdapter);
        mBucketListView.setOnItemClickListener(mBucketListViewOnItemClickListener);
        mBucketListView.setItemsCanFocus(true);

        setShowBucket();

        // 防止mBucketListView获取数据前就被触摸 java.lang.IllegalStateException:
        mBucketListView.setVisibility(View.GONE);
    }

    // 照片列表
    private OnItemClickListener mGrideViewOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int numColumn = mImageGridView.getNumColumn();
            if (position < numColumn || position > mImageList.size() + numColumn) {
                return;
            }

            PhotoAlbumHelper.ImageInfo image = mImageList.get(position - numColumn);

            if (!FileUtils.isFileExist(image.getPath())) {
                // 文件已经被删除，数据库未更新
                Utils.showToast(PhotoAlbumActivity.this, R.string.photo_album_file_deiete,
                        Toast.LENGTH_SHORT);
                return;
            }

            if (isSingleNotMultipleMode()) {
                onItemClickSingleMode(image.getPath());
            } else {
                if (image.isSelect()) {
                    startScaleAnimation((ImageViewHolder) view.getTag(), false);
                    image.setSelect(!image.isSelect());
                    removeShowImageSelect(image.getPath());
                } else if (isCanAddToSelect()) {
                    startScaleAnimation((ImageViewHolder) view.getTag(), true);
                    image.setSelect(!image.isSelect());
                    addShowImageSelect(image.getPath());
                } else {
                    startSharkAnimation();
                }
            }
        }
    };

    private OnScrollListener mImageGrideViewOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

            if (mGridScrollState == SCROLL_STATE_FLING && scrollState == SCROLL_STATE_IDLE) {
                // 快速滑动结束
                mImageGridViewAdapter.notifyDataSetChanged();
            }
            mGridScrollState = scrollState;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
        }
    };

    // 相册列表
    private OnItemClickListener mBucketListViewOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position < mBucketListView.getHeaderViewsCount()) {
                return;
            }
            if (mLastListPosition != position) {// 连续进入同一个相册不重新加载
                mImageList.clear();

                mImageGridViewAdapter.notifyDataSetChanged();
                PhotoAlbumHelper.BucketInfo bucketinfo = mBucketList.get(position
                        - mBucketListView.getHeaderViewsCount());
                mBucketId = bucketinfo.getId();
                mBucketDisplayName = bucketinfo.getName();
                showLoadingProgress();
                fetchData(FetchType.IMAGE);

                // System.gc();
            }

            setShowImage();
            mLastListPosition = position;
        }
    };

    private View.OnClickListener mViewOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.photo_album_title_left:
                setShowBucket();
                break;

            case R.id.photo_album_title_right:
                cancel();
                break;

            case R.id.photo_album_select_show_save_button:
                onSaveButtonClick();
                break;
            }
        }
    };

    private void setShowBucket() {
        mIsShowImage = false;
        showImageNotBucket(false);
    }

    private void setShowImage() {
        mIsShowImage = true;
        showImageNotBucket(true);
    }

    private void onItemClickSingleMode(String path) {
        // mSelectImageListView.put(path, null);
        mSelectImageList.add(path);
        saveAndFinish();
    }

    /**
     * 未达到数量限制
     * 
     * @return
     */
    private boolean isCanAddToSelect() {
        if (mSelectImageLinearLayout.getChildCount() < mSelectNumber) {
            return true;
        }
        return false;
    }

    /**
     * 控制底部被选择的图片
     * 
     * @param image
     */
    private void addShowImageSelect(final String path) {
        if (path == null || !new File(path).exists() || !isCanAddToSelect()
                || mSelectImageListView.containsKey(path)) {
            return;
        }

        final View view = mInflater.inflate(R.layout.photo_album_select_item, null);
        final ImageView image = (ImageView) view.findViewById(R.id.photo_album_select_image);
        // view.findViewById(R.id.photo_album_select_select).setBackgroundDrawable(
        // mTheme.getDrawableFromIdentifier(mDefaultRemoveId));
        LinearLayout.LayoutParams lylp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);

        lylp.setMargins(0, 0,
                getResources().getDimensionPixelOffset(R.dimen.photo_album_left_margin), 0);
        view.setLayoutParams(lylp);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeShowImageSelect(path);
                syncImageSelect(path, false);
            }
        });
        mSelectImageListView.put(path, image);
        mSelectImageList.add(path);

        Bitmap bmp = getBitmapCache(path);

        if (bmp != null && !bmp.isRecycled()) {
            image.setImageBitmap(bmp);
            moveCacheToSelect(path, bmp);
        } else {
            loadImage(path, null, image, LOAD_SELECT_IMAGE);
            image.setImageDrawable(getResources().getDrawable(mDefaultImageId));
        }
        mSelectImageLinearLayout.addView(view);
        updateSaveButton();

        mHandler.postDelayed(new Runnable() {
            public void run() {
                mHorizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100);
    }

    private void startScaleAnimation(final ImageViewHolder view, final boolean show) {
        // 用赞的动画效果
        setSelectVisibility(view.select, show);
        if (show) {
            setSelectAlpha(view.image, true);
        }
//        PageLikeAnimation animation = null;
//        if (show) {
//            animation = new PageLikeAnimation(1.1f, 1.0f);
//        } else {
//            animation = new PageLikeAnimation(0.98f);
//        }
//
//        animation.setFillAfter(false);
//        animation.setFillEnabled(false);
//
//        // 不用Animation.AnimationListener()，快速离开页面时动画执行不完
//        view.select.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                setSelectVisibility(view.select, false);
//                view.select.clearAnimation();
//                setSelectVisibility(view.select, show);
//                setSelectAlpha(view.image, show);
//            }
//        }, animation.getDuration());
//
//        view.select.startAnimation(animation);
    }

    /**
     * save 按钮的抖动
     */
    private void startSharkAnimation() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.view_shake);
        mSaveButton.startAnimation(animation);
    }

    private void removeShowImageSelect(final String path) {
        if (mSelectImageListView.containsKey(path)) {
            final View view = (View) mSelectImageListView.get(path).getParent();
            if (view != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSelectImageLinearLayout.removeView(view);

                        mSelectImageListView.remove(path);
                        mSelectImageList.remove(path);
                        updateSaveButton();
                        removeBitmapShow(path);
                    }
                }, 100);
            }
        }
    }

    private void changeTitle(String title) {
        mTitleText.setText(title);
    }

    private void updateSaveButton() {
        int count = mSelectImageLinearLayout.getChildCount();
        if (count > 0) {
            String message = String.format(getString(R.string.photo_album_save_number), count,
                    mSelectNumber);

            mSaveButton.setClickable(true);
            mSaveButton.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.btn_photo_album_save));
            mSaveButton.setText(message);

        } else {
            mSaveButton.setClickable(false);
            mSaveButton.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.compose_photo_graychoose_button));
            mSaveButton.setText(getString(R.string.photo_album_save));
        }
    }

    /**
     * 底部是否已经选择
     * 
     * @param image
     * @return
     */
    private boolean isImageSelected(String path) {
        if (mSelectImageListView.size() == 0) {
            return false;
        }
        if (mSelectImageListView.containsKey(path)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 显示图片不是相册
     * 
     * @param show
     */
    private void showImageNotBucket(boolean show) {
        if (show) {
            mTitleLeft.setVisibility(View.VISIBLE);
            changeTitle(mBucketDisplayName);
            mBucketListView.setVisibility(View.GONE);
            mImageGridView.setVisibility(View.VISIBLE);
        } else {
            mTitleLeft.setVisibility(View.INVISIBLE);
            changeTitle(getString(R.string.photo_album_bucket_title));
            mImageGridView.setVisibility(View.GONE);
            mBucketListView.setVisibility(View.VISIBLE);

            // 防止首页相册封面没有加载状况
            mBucketListViewAdapter.notifyDataSetChanged();

            mBucketListView.requestFocus();
        }
    }

    private boolean isShowImageNotBucket() {
        // home切回时不准
        // return mImageGridView.isShown();
        return mIsShowImage;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        dismissLoadingProgress();
        if (isShowImageNotBucket()&&keyCode!=19&&keyCode!=20&&keyCode!=21&&keyCode!=22&&keyCode!=23) {
        	setShowBucket();
            return true;
        }
        setResult(RESULT_CANCELED);
        return super.onKeyDown(keyCode, event);
    }

    private void fetchData(final FetchType type) {
        // new FetchAsyncTask().execute(type);

        mLoadImageTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                switch (type) {
                case BUCKET:
                    mBucketList = PhotoAlbumHelper.getBucketList(PhotoAlbumActivity.this);
                    msg.what = LOAD_BUCKET_DATA;
                    break;

                case IMAGE:
                    mImageList = PhotoAlbumHelper.getImageByBucket(PhotoAlbumActivity.this,
                            mBucketId);
                    msg.what = LOAD_IMAGE_DATA;
                    break;
                }
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                mHandler.sendMessage(msg);
            }
        });
    }

    private void onLoadBucketDataEnd() {
        dismissLoadingProgress();
        if (mBucketList == null) {
            return;
        }
        sortBucketList();
        mBucketListViewAdapter.notifyDataSetChanged();
        mBucketListView.setVisibility(View.VISIBLE);
    }

    private void onLoadImageDataEnd() {
        dismissLoadingProgress();
        if (mImageList == null) {
            return;
        }
        setImageSelectStatus();
    }

    /**
     * 手机相册为空时
     */
    private void setEmptyState(boolean isEmpty) {
        if (isEmpty) {
            mSelectRelativeLayout.setVisibility(View.GONE);
            mBucketListView.setVisibility(View.VISIBLE);
            mBucketListView.setDividerHeight(0);
            setSingleMode(true);
            mEmptyLinearLayout.setVisibility(View.VISIBLE);
            mBucketList.clear();
            mBucketListViewAdapter.notifyDataSetInvalidated();
        } else {
            mEmptyLinearLayout.setVisibility(View.GONE);
            mBucketListView.setDividerHeight(getResources().getDimensionPixelSize(
                    R.dimen.photo_album_bucket_list_divider));
            mBucketListView.setDivider(getResources().getDrawable(R.drawable.compose_photo_rule));

            setSingleMode(isSingleNotMultipleMode());
        }
    }

    /**
     * 相机路径排第一位,找不到没办法
     */
    private void sortBucketList() {
        if (mBucketList == null) {
            return;
        }
        int size = mBucketList.size();
        PhotoAlbumHelper.BucketInfo buckInfo = null;
        for (int i = 0; i < size; i++) {
            String path = mBucketList.get(i).getPath();
            if ((CAMERA_FOLDER + CAMERA_FOLDER_CAMERA).equals(path)
                    || (CAMERA_FOLDER + CAMERA_FOLDER_100ANDRO).equals(path)
                    || (CAMERA_FOLDER + CAMERA_FOLDER_100MEDIA).equals(path)) {
                buckInfo = mBucketList.get(i);
                mBucketList.remove(i);
                break;
            }
        }
        if (buckInfo != null) {
            mBucketList.add(0, buckInfo);
        }
    }

    /**
     * 每次进入具体相册，设置勾选选项
     */
    private void setImageSelectStatus() {
        if (mSelectImageListView != null && mSelectImageListView.size() > 0) {
            int size = mImageList.size();
            for (int i = 0; i < size; i++) {
                PhotoAlbumHelper.ImageInfo image = mImageList.get(i);
                image.setSelect(isImageSelected(image.getPath()));
            }
        }
        mImageGridViewAdapter.notifyDataSetChanged();
    }

    /**
     * 同步当前imagelist的选中、取消状态（再次进入统一相册不重新获取）
     * 
     * @param path
     * @param show
     */
    private void syncImageSelect(String path, boolean show) {
        if (mImageList == null) {
            return;
        }
        int size = mImageList.size();
        for (int i = 0; i < size; i++) {
            PhotoAlbumHelper.ImageInfo image = mImageList.get(i);
            if (path.equals(image.getPath())) {
                image.setSelect(show);
                if (isImageShow(image.getPosition())) {
                    mImageGridViewAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
    }


    /**
     * 获取相册图片
     * 
     * @param data
     */
    private void onLoadImageEnd(Object[] data) {
        PhotoAlbumHelper.ImageInfo image = (PhotoAlbumHelper.ImageInfo) data[0];
        ImageViewHolder viewHolder = (ImageViewHolder) data[1];
        Bitmap bitmap = (Bitmap) data[2];

        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        if (isImageShow(image.getPosition())) {
            if (image.getPath().equals(viewHolder.imagePath)) {
                viewHolder.image.setImageBitmap(bitmap);
            } else {
                Bitmap bmp = getBitmapCache(viewHolder.imagePath);
                if (bmp != null && !bmp.isRecycled()) {
                    viewHolder.image.setImageBitmap(bmp);
                    mImageGridViewAdapter.notifyDataSetChanged();
                }
            }
        }
        // 更新在未load出来图片时就选择的图片
        if (mSelectImageListView.containsKey(image.getPath())) {
            ImageView view = mSelectImageListView.get(image.getPath());
            view.setImageBitmap(bitmap);
        }
    }


    /**
     * 获取相册封面图片
     * 
     * @param data
     */
    private void onLoadBucketImageEnd(Object[] data) {
        PhotoAlbumHelper.BucketInfo bucket = (PhotoAlbumHelper.BucketInfo) data[0];
        BucketViewHolder viewHolder = (BucketViewHolder) data[1];
        Bitmap bitmap = (Bitmap) data[2];

        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        if (isBucketImageShow(bucket.getPosition())) {
            // 在快速滑动时会position所在的view不是之前的view了(快速滑走再滑回来),增加path的比对
            if (bucket.getImgPath().equals(viewHolder.imagePath)) {
                viewHolder.image.setImageBitmap(bitmap);
            } else {
                Bitmap bmp = getBitmapCache(viewHolder.imagePath);
                if (bmp != null && !bmp.isRecycled()) {
                    viewHolder.image.setImageBitmap(bmp);
                    mBucketListViewAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * 获取底部已选择图片
     * 
     * @param data
     */
    private void onLoadSelectImageEnd(Object[] data) {
        ImageView image = (ImageView) data[1];
        Bitmap bitmap = (Bitmap) data[2];
        if (image != null && image.isShown()) {
            image.setImageBitmap(bitmap);
        }
    }

    private boolean isImageShow(int position) {
        if (position < mImageGridView.getFirstVisiblePosition()
                || position > mImageGridView.getLastVisiblePosition()) {
            return false;
        }
        return true;
    }

    private boolean isBucketImageShow(int position) {
        if (position < mBucketListView.getFirstVisiblePosition()
                - mBucketListView.getHeaderViewsCount()
                || position > mBucketListView.getLastVisiblePosition()
                        - mBucketListView.getHeaderViewsCount()) {
            return false;
        }

        return true;
    }

    /**
     * 选中/取消 后的选择状态
     * 
     * @param view
     * @param select
     */
    private void setSelectVisibility(View view, boolean select) {
        if (select) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * 选中/取消 后的半透明效果
     * 
     * @param view
     * @param select
     */
    private void setSelectAlpha(ImageView view, boolean select) {
        if (select) {
            view.setAlpha(255 * 4 / 10);
        } else {
            view.setAlpha(255);
        }
    }

    @Override
    protected void onDestroy() {
        mLoadImageTaskExecutor.shutdownNow();
        mLoadImageTaskExecutor = null;
        recycleBitmap();

        super.onDestroy();
        finish();
    }

    protected boolean isOnGestureBack(MotionEvent event) {
        return false;
    }

    private void cancel() {
        // if (isShowImageNotBucket()) {
        // setShowBucket();
        // } else {
        setResult(RESULT_CANCELED);
        finish();
        // }
    }

    /**
     * 相册
     * 
     * @author shenrh
     * 
     */
    private class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mBucketList.size();
        }

        @Override
        public Object getItem(int position) {
            return mBucketList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BucketViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new BucketViewHolder();
                convertView = mInflater.inflate(R.layout.photo_album_listview_item, null);

                convertView.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.photos_list_item_bg));

                viewHolder.image = (ImageView) convertView
                        .findViewById(R.id.photo_album_listeview_item_image);
                viewHolder.name = (TextView) convertView
                        .findViewById(R.id.photo_album_listview_item_name);
                // viewHolder.path = (TextView) convertView
                // .findViewById(R.id.photo_album_listview_item_path);
                viewHolder.count = (TextView) convertView
                        .findViewById(R.id.photo_album_listview_item_count);

                viewHolder.image.setImageDrawable(getResources().getDrawable(mDefaultImageId));
                viewHolder.name.setTextColor(getResources().getColor(
                        R.color.photo_album_bucket_color));
                viewHolder.count.setTextColor(getResources().getColor(
                        R.color.photo_album_bucket_color));

                ((ImageView) convertView.findViewById(R.id.photo_album_show_triangle))
                        .setImageDrawable(getResources().getDrawable(R.drawable.common_icon_arrow));

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (BucketViewHolder) convertView.getTag();
                viewHolder.image.setImageDrawable(getResources().getDrawable(mDefaultImageId));
            }

            final PhotoAlbumHelper.BucketInfo bucketInfo = mBucketList.get(position);
            bucketInfo.setPosition(position);

            viewHolder.imagePath = bucketInfo.getImgPath();

            Bitmap bmp = getBitmapCache(bucketInfo.getImgPath());

            if (bmp != null && !bmp.isRecycled()) {
                viewHolder.image.setImageBitmap(bmp);
                moveCacheToBucket(bucketInfo.getImgPath(), bmp);
            } else {
                if (!isRunningOrAdd(bucketInfo.getImgPath())) {
                    loadImage(bucketInfo.getImgPath(), bucketInfo, viewHolder, LOAD_BUCKET_IMAGE);
                }
            }
            viewHolder.name.setText(bucketInfo.getName());
            viewHolder.count.setText(" (" + bucketInfo.getCount() + ")");
            // viewHolder.path.setText(bucketInfo.getPath());
            return convertView;
        }

    }

    /**
     * 图片列表
     * 
     * @author shenrh
     * 
     */
    private class GridViewImageAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mImageList.size() + mImageGridView.getNumColumn();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int numColumn = mImageGridView.getNumColumn();
            if (mImageList == null) {
                return null;
            }

            if (position >= mImageList.size() + numColumn) {
                return null;
            }

            if (position < numColumn) {
                // 置顶view
                View view = new View(PhotoAlbumActivity.this);
                view.setLayoutParams(new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT, getResources().getDimensionPixelSize(
                                R.dimen.baselayout_title_height)));
                return view;
            }

            ImageViewHolder viewHolder = null;
            if (convertView == null || convertView.getTag() == null
                    || !(convertView.getTag() instanceof ImageViewHolder)) {
                viewHolder = new ImageViewHolder();
                convertView = mInflater.inflate(R.layout.photo_album_gridview_item, null);
                viewHolder.image = (ImageView) convertView
                        .findViewById(R.id.photo_album_grideview_item_image);
                viewHolder.select = (ImageView) convertView
                        .findViewById(R.id.photo_album_grideview_item_select);
                viewHolder.select.setBackgroundDrawable(getResources()
                        .getDrawable(mDefaultSelectId));
                viewHolder.image.setImageDrawable(getResources().getDrawable(mDefaultImageId));

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ImageViewHolder) convertView.getTag();
                viewHolder.image.setImageDrawable(getResources().getDrawable(mDefaultImageId));
            }

            final PhotoAlbumHelper.ImageInfo image = mImageList.get(position - numColumn);
            image.setPosition(position);

            Bitmap bmp = getBitmapCache(image.getPath());
            viewHolder.imagePath = image.getPath();

            if (bmp != null && !bmp.isRecycled()) {
                viewHolder.image.setImageBitmap(bmp);
            } else if (mGridScrollState != OnScrollListener.SCROLL_STATE_FLING) {
                if (!isRunningOrAdd(image.getPath())) {
                    loadImage(image.getPath(), image, viewHolder, LOAD_IMAGE);
                }
            }
            setSelectVisibility(viewHolder.select, image.isSelect());
            setSelectAlpha(viewHolder.image, image.isSelect());
            return convertView;
        }

    }

    private void loadImage(final String path, final Object data, final Object viewHolder,
            final int id) {
        mLoadImageTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap result = BitmapHelper.getBitmapFromFile(path, MAX_IMAGE_SIZE,
                        MAX_IMAGE_SIZE, true, true);

                if (result != null && !result.isRecycled()) {

                    if (Thread.currentThread().isInterrupted()) {
                        result.recycle();
                        result = null;
                        return;
                    }
                    removeRunning(path);

                    switch (id) {
                    case LOAD_SELECT_IMAGE:
                        saveShowCache(path, result);
                        break;

                    case LOAD_BUCKET_IMAGE:
                    case LOAD_RECENT_IMAGE:
                        saveBucketCache(path, result);
                        break;

                    default:
                        saveBitmapCache(path, result);
                        break;
                    }

                    Message msg = new Message();
                    msg.what = id;
                    msg.obj = new Object[] { data, viewHolder, result };

                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    private synchronized boolean isRunningOrAdd(String path) {
        if (mLoadingList.contains(path)) {
            return true;
        }
        mLoadingList.add(path);
        return false;
    }

    private synchronized void removeRunning(String path) {
        if (mLoadingList.contains(path)) {
            mLoadingList.remove(path);
        }
    }

    /**
     * 图片条目信息
     * 
     * @author shenrh
     * 
     */
    public static final class ImageViewHolder {
        private ImageView image;
        private ImageView select;

        private String imagePath = "";// 对应的相册封面图片路径
    }

    /**
     * 相册条目信息
     * 
     * @author shenrh
     * 
     */
    public static final class BucketViewHolder {
        private ImageView image;
        private TextView name;// 相册名字
        private TextView path;// 相册路径
        private TextView count;// 相册照片数量

        private String imagePath = "";// 对应的相册封面图片路径
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putInt(CALL_TYPE, mCallType);
            outState.putStringArrayList(SELECT_LIST, mSelectImageList);
            outState.putSerializable(INPUT_LIST, mPicAttachmentList);
            outState.putInt(SELECT_NUMBER, mSelectNumber);
            outState.putBoolean(AUTO_ADAPTER, mAutoAdapter);
            outState.putBoolean(IS_SHOW_IMAGE, mIsShowImage);
        }
    }

    private void loadInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Utils.logd("call loadInstanceState...");
            mSelectImageList = savedInstanceState.getStringArrayList(SELECT_LIST);
            mPicAttachmentList = (ArrayList<String>) savedInstanceState.getSerializable(INPUT_LIST);
            mSelectNumber = savedInstanceState.getInt(SELECT_NUMBER);
            mAutoAdapter = savedInstanceState.getBoolean(AUTO_ADAPTER);
            mIsShowImage = savedInstanceState.getBoolean(IS_SHOW_IMAGE);
            initDataFromList(mSelectImageList);
        }
    }

    /**
     * 预设选中状态
     * 
     * @param list
     */
    private void initDataFromList(ArrayList<String> list) {
        if (list == null) {
            return;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            addShowImageSelect(list.get(i));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
            // 从裁剪页面回来的关闭通知(水印逻辑)
            case REQUEST_CODE_PIC_CROP:
                setResult(RESULT_OK);
                finish();
                break;
            case REQUEST_CODE_PIC_9CUT:
            	Intent intent = new Intent();           
            	 intent.putExtra(PhotoAlbumActivity.RETURN_DATA, data.getSerializableExtra(PhotoAlbumActivity.RETURN_DATA));
                 setResult(RESULT_OK, intent);
                 finish();
                 break;
            }
        }
    }

    private void onSaveButtonClick() {
        saveAndFinish();
    }    

    private void saveAndFinish() {

        // 保证顺序
        addStatusParamToData(mSelectImageList);

        Intent intent = new Intent();
        intent.putExtra(RETURN_DATA, mSelectImageList);
        setResult(RESULT_OK, intent);
        finish();
    }
    
    /**
     * 从mPicAttachmentList获取信息到最终结果(保证传入的信息保存回去)
     */
    private void addStatusParamToData(ArrayList<String> data) {
        if (mPicAttachmentList != null && mPicAttachmentList.size() > 0) {

            int oriSize = data.size();
            int endSize = mPicAttachmentList.size();

            for (int i = 0; i < oriSize; i++) {
                int location = isInAttachmentList(data.get(i), mPicAttachmentList);
                if (location >= 0 && location < endSize) {
                	data.remove(i);
                	data.add(i, mPicAttachmentList.get(location));
                }
            }
        }
    }

    /**
     * 靠原始路径区分是否已存在
     * 
     * @param pic
     * @param list
     * @return
     */
    private int isInAttachmentList(String pic, ArrayList<String> list) {
        if (pic == null || list == null) {
            return -1;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            String path1 = filtrateUriToPath(list.get(i));
            String path2 = filtrateUriToPath(pic);
            if (path1 != null && path2 != null && path1.equals(path2)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 过滤 file:// 为绝对路径
     * 
     * @param data
     * @return
     */
    private String filtrateUriToPath(String data) {
        if (data == null) {
            return data;
        }
        if (data.startsWith(FILE_URI)) {
            return data.substring(FILE_URI.length());
        } else if (data.startsWith(CONTENT_URI)) {
            // 暂且放在主线程
            return PhotoAlbumHelper.getRealPathFromURI(PhotoAlbumActivity.this, Uri.parse(data));
        } else {
            return data;
        }
    }

    @Override
    protected void handleTitleBarEvent(int eventId) {
        // do nothing
    }

    private static String getAbsolutePath(Context context, Uri uri) {
        return Utils.getUriFilePath(uri.toString(), context);
    }
}
