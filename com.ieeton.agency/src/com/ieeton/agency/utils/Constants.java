package com.ieeton.agency.utils;

public class Constants {
	public static boolean DEBUG = true;
	public static String TAG = "doctor";

	public static String LOGIN_MOBILE = "mobile";
	public static String LOGIN_WX = "wx";
	public static String LOGIN_QQ = "qq";
	public static String LOGIN_WEIBO = "weibo";
	
	public static int REQUEST_ITEM_NUM = 15;
	public static int MAX_UPLOAD_PIC_NUM = 3;
	public static int TIMEOUT = 30000;
	public static int UPLOAD_TIMEOUT = 60000;
	public static int SOCKET_BUFFER_SIZE = 8192;
	public static String USER_AGENT = "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.4) Gecko/20091111 Gentoo Firefox/3.5.4";
	public static int HTTP_STATUS_OK = 200;
	public static int HTTP_STATUS_DOWNLOAD_OK = 206;
	public static final int REQUEST_TIMEOUT = 2*60*1000;
	public static String MYUID = "MYUID";
	public static String MY_LOGINNAME = "LOGINNAME";
	public static int MAX_INPUT_TEXT_NUM = 500;
	public static String MYCITY = "MYCITY";
	public static String MYCITY_NAME = "MYCITY_NAME";
	public static String MYCITY_ID = "MYCITY_ID";
	public static String NEED_PERFECT_INFO = "PERFECT_INFO";
	public static String NEED_BIND_MOBILE = "BIND_MOBILE";
	public static String SEARCH_HISTORY = "SEARCH_HISTORY";

	public static int MAX_PAGE_SIZE = 20;

	public static final String PACKAGE_NAME = "com.ieeton.agency";
	//医通from值
	public static final String IEETON_FROM = "IEETON_FROM";
	
	public static final String UMENG_APPKEY = "UMENG_APPKEY";
	public static final String WEIXIN_APPKEY = "WEIXIN_APPKEY";
	public static final String WEIXIN_APPSECRET = "WEIXIN_APPSECRET";
	public static final String WEIBO_APPKEY = "WEIBO_APPKEY";
	public static final String QQ_APPKEY = "QQ_APPKEY";
	public static final String QQ_APPID = "QQ_APPID";

	public static final String SERVER_HOST_PASSPORT_SERVER = "http://passport.ieeton.com/passport/";
	public static final String SERVER_HOST_IMAGE_SERVER = "http://121.41.46.46";
	public static final String SERVER_HOST_UPLOAD_SERVER = "http://121.41.46.46:8080/upload.do";
	public static final String SERVER_HOST_CONTENT_SERVER = "http://data.ieeton.com/data/content/phone/";
	public static final String SERVER_FEEDBACK_HUANXIN_ID = "xiaomishu";
	public static final String SERVER_IVR_NUMBER = "4008636181";
	public static final String SERVER_HOST_SHARE_SERVER = "http://www.ieeton.com/msg.html";
	public static final String SERVER_HOST_SHARE_ARTICLE_SERVER = "http://www.ieeton.com/getNEWS.html";
	
	public static final String PASSPORT = "passport";
	
	public static final String LOCATION_FINISH_ACTION = "com.ieeton.agency.location";
	public static final String NEED_RELOGIN_ACTION = "com.ieeton.agency.relogin";
	public static final String ACTION_UNFOLLOW = "com.ieeton.agency.unfollow";
	public static final String ACTION_FOLLOW = "com.ieeton.agency.follow";
	public static final String ACTION_UPDATE_INFO = "com.ieeton.agency.updateinfo";
	public static final String UNFAVORITE_ARTICLE_ACTION = "com.ieeton.agency.unfavorite_article";
	public static final String FAVORITE_ARTICLE_ACTION = "com.ieeton.agency.favorite_article";	
	public static final String VIEW_ARTICLE_ACTION = "com.ieeton.agency.view_article";
	public static String ACTION_LIKE_ARTICLE = "com.ieeton.agency.like_article";
	public static String ACTION_UNLIKE_ARTICLE = "com.ieeton.agency.unlike_article";
	
	public static final String OPERATION_UID = "operation_uid";
	public static final String EXTRA_UID = "extra_uid";
	public static final String EXTRA_DOCTORID = "extra_doctorid";
	public static final String EXTRA_ARTICLEID = "extra_articleid";
	
	public static final String FOLDER_PORTRAIT = Utils.getSDPath() + 
				"/ieeton/.portrait/";
	
	public static final String PREFERENCE_NICK = "preference_nick";
	public static final String PREFERENCE_FROM = "preference_from";
	
	public static final int LOAD_REFRESH = 0;
	public static final int LOAD_MORE = 1;
	
    public static String DATE_FORMAT = "M月d日 HH:mm";
    public static String DATE_FORMAT_TODAY = "HH:mm";
    public static String DATE_FORMAT_YEAR = "yyyy-M-d HH:mm";
    public static String DATE_FORMAT_YEAR_NEW = "yy-M-d HH:mm";

    public static String DATE_FORMAT_MONTH_NO_TIME = "M-d";
    
    public static final String WEB_BROWSER_URL ="url";
    public static final String WEB_BROWSER_INTERNAL ="internal";
    public static final String WEB_BROWSER_TITLE ="title";
}
