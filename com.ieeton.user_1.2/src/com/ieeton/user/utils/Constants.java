package com.ieeton.user.utils;

public class Constants {
	public static boolean DEBUG = false;
	public static String TAG = "pediatrics";

	public static int REQUEST_ITEM_NUM = 15;
	public static int MAX_UPLOAD_PIC_NUM = 3;
	public static int TIMEOUT = 30000;
	public static int UPLOAD_TIMEOUT = 60000;
	public static int SOCKET_BUFFER_SIZE = 8192;
	public static String USER_AGENT = "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1.4) Gecko/20091111 Gentoo Firefox/3.5.4";
	public static int HTTP_STATUS_OK = 200;
	public static int HTTP_STATUS_DOWNLOAD_OK = 206;
	public static final int REQUEST_TIMEOUT = 2*60*1000;
	public static String MYINFO = "MYINFO";
	public static String MY_USERTYPE = "MY_USERTYPE";
	public static String MYUID = "MYUID";
	public static String VISITOR = "VISITOR";
	public static String MY_LOGINNAME = "LOGINNAME";
	public static String MY_PORTRAIT_URL = "MY_PORTRAIT_URL";
	public static int MAX_INPUT_TEXT_NUM = 500;
	public static String MYCITY = "MYCITY";
	public static String MYCITY_NAME = "MYCITY_NAME";
	public static String MYCITY_ID = "MYCITY_ID";
	public static String MY_LOCATION = "location";
	public static String LOCATION_LA = "MYCITY_LAI";
	public static String LOCATION_LON = "MYCITY_LON";
	public static String LOCATION_CITYNAME = "MYCITY_NAME";
	public static String NEED_BIND_MOBILE = "BIND_MOBILE";
	public static String USER_GUIDE_SHOW = "USER_GUIDE_SHOW";
	public static String FIRST_ENTRY = "FIRST_ENTRY";
	public static String SEARCH_HISTORY = "SEARCH_HISTORY";
	public static String SETTINGS = "SETTINGS";
	public static String SETTINGS_MESSAGE_NOTIFY = "SETTINGS_MESSAGE_NOTIFY";
	public static String SETTINGS_MESSAGE_NOTIFY_SOUND = "SETTINGS_MESSAGE_NOTIFY_SOUND";
	public static String SETTINGS_MESSAGE_NOTIFY_VIBRATE = "SETTINGS_MESSAGE_NOTIFY_VIBRATE";
	public static String SETTINGS_VIA_RECEIVER = "SETTINGS_VIA_RECEIVER";
	public static String SETTINGS_AUTO_CLEAR_CACHE = "SETTINGS_AUTO_CLEAR_CACHE";
	
	public static String ACCOUNT = "ACCOUNT";

	public static int MAX_PAGE_SIZE = 20;
	
	public static int MIN_NICKNAME_LENGTH = 4;
	public static int MAX_NICKNAME_LENGTH = 20;

	public static final String PACKAGE_NAME = "com.ieeton.pediatrics";
	
	//医通from值
	public static final String IEETON_FROM = "IEETON_FROM";
	
	public static String LOGIN_MOBILE = "mobile";
	public static String LOGIN_WX = "wx";
	public static String LOGIN_QQ = "qq";
	public static String LOGIN_WEIBO = "weibo";
	
	public static String LOGIN_TYPE = "LOGIN_TYPE";
	
	public static final String UMENG_APPKEY = "UMENG_APPKEY";
	public static final String WEIXIN_APPKEY = "WEIXIN_APPKEY";
	public static final String WEIXIN_APPSECRET = "WEIXIN_APPSECRET";
	public static final String WEIBO_APPKEY = "WEIBO_APPKEY";
	public static final String QQ_APPKEY = "QQ_APPKEY";
	public static final String QQ_APPID = "QQ_APPID";

	public static final String SERVER_HOST_PASSPORT_SERVER = "";
	public static final String SERVER_HOST_IMAGE_SERVER = "http://m.dadahealth.com:1500";
	public static final String SERVER_HOST_CONTENT_SERVER = "http://m.dadahealth.com:81/DaDaInterface_m_1.3.0/";
	public static final String SERVER_HOST_SHARE_CONVERSATION_SERVER = "http://www.ieeton.com/msg.html";
	public static final String SERVER_HOST_SHARE_ARTICLE_SERVER = "http://m.dadahealth.com:81/pages/title/article.html";
	public static final String SERVER_HOST_SHARE_DOCTOR_DETAIL_SERVER = "http://www.ieeton.com/details.html";
	public static final String SERVER_HOST_SHARE_INSTITUTION_DETAIL_SERVER = "http://www.ieeton.com/DocDetails.html";
	public static final String SERVER_HOST_SUBSCRIBE_URL = "http://m.dadahealth.com:81/pages/title/my97time.aspx";
	public static final String SERVER_HOST_INTEGRAL_URL = "http://m.dadahealth.com:81/pages/title/Integral.html";
	public static final String SERVER_HOST_SHARE_PRODUCT_URL = "http://m.dadahealth.com:81/pages/title/productdetll.html";

	public static final String PASSPORT = "passport";
	
	public static final String LOCATION_FINISH_ACTION = "com.ieeton.user.location";
	public static final String NEED_RELOGIN_ACTION = "com.ieeton.user.relogin";
	public static final String LOGIN_ACTION = "com.ieeton.user.login";
	public static final String REFRESH_ACTION = "com.ieeton.user.refresh";
	public static final String PAY_SUCCESS_ACTION = "com.ieeton.user.pay.success";
	public static final String WEIXIN_PAY_SUCCESS_ACTION = "com.ieeton.user.weixin.pay.success";
	public static final String RECHARGE_SUCCESS_ACTION = "com.ieeton.user.recharge.success";
	public static final String UNFAVORITE_ARTICLE_ACTION = "com.ieeton.user.unfavorite_article";
	public static final String FAVORITE_ARTICLE_ACTION = "com.ieeton.user.favorite_article";	
	public static final String VIEW_ARTICLE_ACTION = "com.ieeton.user.view_article";
	public static String ACTION_FOLLOW = "com.ieeton.user.follow";
	public static String ACTION_UNFOLLOW = "com.ieeton.user.unfollow";
	public static String ACTION_LIKE_DOCTOR = "com.ieeton.user.like_doctor";
	public static String ACTION_UNLIKE_DOCTOR = "com.ieeton.user.unlike_doctor";
	public static String ACTION_LIKE_ARTICLE = "com.ieeton.user.like_article";
	public static String ACTION_UNLIKE_ARTICLE = "com.ieeton.user.unlike_article";
	public static String ACTION_FAVORITE_PRODUCT = "com.ieeton.user.favorite_product";
	public static String ACTION_UNFAVORITE_PRODUCT = "com.ieeton.user.unfavorite_product";
	public static String ACTION_REFRESH_ORDER_LIST = "com.ieeton.user.refre_order_list";
	
	public static final String EXTRA_ORDERID = "extra_orderid";
	public static final String EXTRA_PRODUCT = "extra_product";
	public static final String EXTRA_PRODUCTID = "extra_productid";
	public static final String EXTRA_CATEGORYID = "extra_categoryid";
	public static final String EXTRA_CATEGORY = "extra_category";
	public static final String EXTRA_LABLE = "extra_lable";
	public static final String EXTRA_CITY = "extra_city";
	public static final String EXTRA_UID = "extra_uid";
	public static final String EXTRA_USER = "extra_user";
	public static final String EXTRA_ARTICLEID = "extra_articleid";
	public static final String EXTRA_ARTICLE = "extra_article";
	public static final String EXTRA_MODE = "extra_mode";
	public static final String EXTRA_AMOUNT = "extra_amount";
	public static final String EXTRA_BALANCE = "extra_balance";
	public static final String EXTRA_NUMBER = "extra_number";
	public static final String EXTRA_IS_INTEGRAL = "extra_isIntegral";
	public static final String EXTRA_URL = "extra_url";
		
	public static final String FOLDER_PORTRAIT = Utils.getSDPath() + 
				"/ieeton/user/.portrait/cache/";
	public static final String PREFERENCE_NICK = "preference_nick";
	public static final String PREFERENCE_FROM = "preference_from";
	
    public static String DATE_FORMAT = "M月d日 HH:mm";
    public static String DATE_FORMAT_TODAY = "HH:mm";
    public static String DATE_FORMAT_YEAR = "yyyy-M-d HH:mm";
    public static String DATE_FORMAT_YEAR_NEW = "yy-M-d HH:mm";

    public static String DATE_FORMAT_MONTH_NO_TIME = "M-d";
    
    public static final String WEB_BROWSER_URL ="url";
    public static final String WEB_BROWSER_INTERNAL ="internal";
    public static final String WEB_BROWSER_TITLE ="title";
}
