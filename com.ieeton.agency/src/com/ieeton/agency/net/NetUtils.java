package com.ieeton.agency.net;

import internal.org.apache.http.entity.mime.MultipartEntity;
import internal.org.apache.http.entity.mime.content.ContentBody;
import internal.org.apache.http.entity.mime.content.FileBody;
import internal.org.apache.http.entity.mime.content.StringBody;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParamBean;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.ErrorMessage;
import com.ieeton.agency.net.NetworkConnectivityListener;
import com.ieeton.agency.net.Reflection;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


public class NetUtils {
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String TYPE_FILE_NAME = "TYPE_FILE_NAME";
    public static final String GZIP_FILE_NAME = "GZIP_FILE_NAME";
    private static Timer sTimer = new Timer();
    
    
    public static class APNWrapper {
        public String name;
        public String apn;
        public String proxy;
        public int port;

        public String getApn() {
            return apn;
        }

        public String getName() {
            return name;
        }

        public int getPort() {
            return port;
        }

        public String getProxy() {
            return proxy;
        }

        APNWrapper() {
        }
    }

    public static String openUrl(String url, String method, Bundle params, Context context,
            boolean checkResult, IDownloadState callback) throws PediatricsIOException, PediatricsParseException, PediatricsApiException {
        String lang = "";//getLang(context);
        HttpClient client = null;
        /**
         * 支持https： 逻辑：如果在wifi下，并且请求的链接是https开头的，则启用https支持，
         * 否则，修改请求链接为http，使用普通http方式完成网络请求
         */
        client = getHttpsClientForWifi(context, url);
        StringBuilder newUrl = new StringBuilder();
        String response = "";
        
        
        try{
            if (NetUtils.METHOD_GET.equals(method)) {
            	if (params == null){
            		params = new Bundle();
            	}
        		params.putString("from", Utils.getIeetonFrom(context));
                newUrl.append(getCompleteUrl(url, params));
            	Utils.logd("url:"+newUrl.toString());
                HttpGet request = new HttpGet(newUrl.toString());
                if (checkResult) {
                    response = execute(client, request, context);
                } else {
                    response = executeWithoutParse(client, request, context);
                }
                return response;
            } else if (NetUtils.METHOD_POST.equals(method)) {
//                MultipartEntity multipartContent = buildMultipartEntity(params, callback);
//                String[] items = url.split("\\?");
//                if (items.length == 2) {
//                    newUrl.append(items[0]).append("?");
//                    String array[] = items[1].split("&");
//                    boolean first = true;
//                    for (String parameter : array) {
//                        String v[] = parameter.split("=");
//                        if (first) {
//                            first = false;
//                        } else {
//                            newUrl.append("&");
//                        }
//                        if (v.length == 2) {
//                            newUrl.append(URLEncoder.encode(v[0])).append("=")
//                            .append(URLEncoder.encode(v[1]));
//                        } else {
//                            newUrl.append(parameter);
//                        }
//                        
//                    }
//                } else {
//                    newUrl.append(url);
//                }
//                newUrl.append("&lang=" + lang);
//                HttpPost request = new HttpPost(newUrl.toString());
//                request.setEntity(multipartContent);
//                if (checkResult) {
//                    response = execute(client, request, context);
//                } else {
//                    response = executeWithoutParse(client, request, context);
//                }
                url += "?from="+Utils.getIeetonFrom(context);
            	Utils.logd("url:"+url.toString());
        		HttpPost httpPost = new HttpPost(url.toString()); 
        		HttpResponse httpResponse = null; 

        		try {
					httpPost.setEntity(new UrlEncodedFormEntity(buildNameValuePairs(params), HTTP.UTF_8));
					httpResponse = new DefaultHttpClient().execute(httpPost);
	            	Utils.logd("getStatusCode:"+httpResponse.getStatusLine().getStatusCode());
					if (httpResponse.getStatusLine().getStatusCode() == 200) { 
						response = EntityUtils.toString(httpResponse.getEntity());
					}
	            	Utils.logd("result:"+response);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new PediatricsApiException("Encoding error");
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					throw new PediatricsApiException("Api error");
				}catch (IOException e) {
					e.printStackTrace();
					throw new PediatricsParseException("IO error");
				}catch (Exception e){
					e.printStackTrace();
					throw new PediatricsApiException("Api error");
				}
                ErrorMessage err = new ErrorMessage(response);
                /**
                 * 没有错误
                 */
                if (err == null || err.errorcode == null || "".equals(err.errorcode) || "1".equals(err.errorcode)) {
                    return response;
                } else {
                    throw new PediatricsApiException(err);
                }
            } else {
                throw new PediatricsIOException(PediatricsIOException.REASON_HTTP_METHOD);
            }
        }finally{
            
        }

    }
    
    private static List<NameValuePair> buildNameValuePairs(Bundle params){
    	List<NameValuePair> paramers = new ArrayList<NameValuePair>();
    	
    	for (String key : params.keySet()) {
			Object objValue = params.get(key);
			
			if(objValue != null && objValue instanceof String){
				paramers.add(new BasicNameValuePair(key, (String)objValue));
			}else if(objValue != null && objValue instanceof Integer){
				paramers.add(new BasicNameValuePair(key, Integer.toString((Integer)objValue)));
			}else if (objValue != null && objValue instanceof Float){
				paramers.add(new BasicNameValuePair(key, String.valueOf(objValue)));
			}else if (objValue != null && objValue instanceof Double){
				paramers.add(new BasicNameValuePair(key, String.valueOf(objValue)));
			}
    	}
    	Utils.logd("paramers:"+paramers.toString());
    	return paramers;
    }
    
    public static String postImage(Context context, String url, String image_path) throws PediatricsIOException, PediatricsApiException, PediatricsParseException{
    	HttpClient client = null;
    	client = getHttpsClientForWifi(context, url);
        StringBuilder newUrl = new StringBuilder();
        String response = "";
        
		HttpPost httpPost = new HttpPost(url.toString()); 
		HttpResponse httpResponse = null; 
		
		MultipartEntity mpEntity = new MultipartEntity();
		File dest_file = new File(image_path);
	    ContentBody cbFile = new FileBody(dest_file, "image/jpeg");
	    mpEntity.addPart("userfile", cbFile);
	    
	    httpPost.setEntity(mpEntity);
	    Utils.logd("postImage, request:"+httpPost.getRequestLine());
	    try {
			httpResponse = client.execute(httpPost);
			response= EntityUtils.toString(httpResponse.getEntity());
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new PediatricsApiException("Api error");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new PediatricsIOException("IO error");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new PediatricsParseException("IO error");
		}
	    Utils.logd("postImage, response:"+response);

	    ErrorMessage err = new ErrorMessage(response);
        /**
         * 没有错误
         */
        if (err == null || err.errorcode == null || "".equals(err.errorcode) || "1".equals(err.errorcode)) {
            return response;
        } else {
            throw new PediatricsApiException(err);
        }
    }
    
    private static MultipartEntity buildMultipartEntity(Bundle params, final IDownloadState callback){

        MultipartEntity multipartContent = null;
        if (callback != null) {
            multipartContent = new CustomMultiPartEntity(callback);
        } else {
            multipartContent = new MultipartEntity();
        }
        
        for (String key : params.keySet()) {
            if (TYPE_FILE_NAME.equals(key) || GZIP_FILE_NAME.equals(key)) {
                Object fileNames = params.get(key);
                if (fileNames != null && fileNames instanceof Bundle) {
                    Bundle pathBundle = (Bundle) fileNames;
                    // StringBuffer data = new StringBuffer();
                    for (String uploadFileKey : pathBundle.keySet()) {
                        final File file = new File(pathBundle.getString(uploadFileKey));
                        if (file.exists()) {
                            FileBody bin;
                            if (TYPE_FILE_NAME.equals(key)) {
                                bin = new FileBody(file, "image/jpeg");
                            } else {
                                bin = new FileBody(file, "application/zip");
                            }
                            multipartContent.addPart(uploadFileKey, bin);
                        }
                        
                    }
                }
            } else {
                StringBody sb1;
                try {
                    String value = params.getString(key);
                    value = (value == null ? "" : value);
                    sb1 = new StringBody(value, Charset.forName(HTTP.UTF_8));
                    multipartContent.addPart(URLEncoder.encode(key), sb1);
                    
                } catch (UnsupportedEncodingException e) {
                    //throw new WeiboIOException(e);
                }
            }
        }
        return multipartContent;
    }

    private static String execute(HttpClient client, HttpUriRequest request, Context context) throws PediatricsIOException, PediatricsParseException, PediatricsApiException{
        /**
         * 在wifi下，如果链接是https且请求失败，则替换为http， 替换为https再次请求。如果请求成功将状态设置为1
         * 如果失败将不修改状态值。 在同样情况下，再次请求，在将https替换为http之后如果成功 将状态设置为2.
         * 如果在wifi下，链接是https第一次请求失败，在替换为http之后成功， 在第二次请求设置链接https直接成功，将状态值设置为0
         * 在每一次启动程序之后，在weiboapplication中将相应的状态设置为0
         * */
        String result = "";
        HttpClient clientHttp = client;
        ClientConnectionManager ccm = clientHttp.getConnectionManager();
        try {
            result = executeWithoutParse(clientHttp, request, context);
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new PediatricsIOException(e);
        }
    	Utils.logd("result:"+result);

        ErrorMessage err = new ErrorMessage(result);
        /**
         * 没有错误
         */
        if (err == null || err.errorcode == null || "".equals(err.errorcode) || "1".equals(err.errorcode)) {
            return result;
        } else {
            throw new PediatricsApiException(err);
        }
    }
    
    private static TimerTask getRequestTimerTask(final RequestWrapper wrapper) {

        return new TimerTask() {

            @Override
            public void run() {

                if (wrapper != null && wrapper.mRequest != null && !wrapper.isFinish) {
                    wrapper.mRequest.abort();
                }
            }
        };
    }
    
    private static String executeWithoutParse(HttpClient client, HttpUriRequest request,
            Context context) throws PediatricsIOException {
        /**
         * 按照业务逻辑，在非wifi状态下，不需要使用https， 因此需要把url的schema替换回http
         */
        String url = request.getURI().toString();
        ClientConnectionManager ccm = client.getConnectionManager();
        if (ccm != null && !(ccm instanceof ThreadSafeClientConnManager)) {
            url = url.replace("https://", "http://");
            try {
                if (request instanceof HttpGet) {
                    ((HttpGet) request).setURI(new URI(url));
                } else if (request instanceof HttpPost) {
                    ((HttpPost) request).setURI(new URI(url));
                }
            } catch (URISyntaxException e) {
                throw new PediatricsIOException(e);
            }
        }

        request.setHeader("User-Agent", Constants.USER_AGENT);
        request.setHeader("Accept-Encoding", "gzip,deflate");
        InputStream inputStream=null;

        RequestWrapper wrapper = new RequestWrapper(request);
        TimerTask task = getRequestTimerTask(wrapper);

        try {
            HttpResponse response = null;
            try {
                // 启动应用级别的超时计时器
                sTimer.schedule(task, Constants.REQUEST_TIMEOUT);

                TrafficMonitor.getInstace(context).recordTxTraffic(TrafficMonitor.NET_MOUDLE_WEIBO,
                        request);
                response = client.execute(request);
                wrapper.isFinish = true;
            } catch (NullPointerException e) {
                // google issue, doing this to work around

                // 取消掉当前的计时任务
                task.cancel();

                // 启动应用级别的超时计时器
                task = getRequestTimerTask(wrapper);
                sTimer.schedule(task, Constants.REQUEST_TIMEOUT);

                try {
                    TrafficMonitor.getInstace(context).recordTxTraffic(
                            TrafficMonitor.NET_MOUDLE_WEIBO, request);
                    response = client.execute(request);
                    wrapper.isFinish = true;
                } catch (NullPointerException e1) {
                    throw new PediatricsIOException(e1);
                } finally {
                    // 请求正常返回,取消计时,并且清除已取消计时任务
                    task.cancel();
                    sTimer.purge();
                }
            } finally {
                // 请求正常返回,取消计时,并且清除已取消计时任务
                task.cancel();
                sTimer.purge();
            }

            TrafficMonitor.getInstace(context).recordRxTraffic(TrafficMonitor.NET_MOUDLE_WEIBO,
                    response);

            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
                        
            if (statusCode != Constants.HTTP_STATUS_OK) {
            	
            	PediatricsIOException ex = new PediatricsIOException(String.format(
            			"Invalid response from server: %s", status.toString()));
            	ex.setStatusCode(statusCode);
            	throw ex;

            }

            // Pull content stream from response
            HttpEntity entity = response.getEntity();
            inputStream = entity.getContent();
            ByteArrayOutputStream content = new ByteArrayOutputStream();

            Header header = response.getFirstHeader("Content-Encoding");
            if (header != null && header.getValue().toLowerCase().indexOf("gzip") > -1) {
                inputStream = new GZIPInputStream(inputStream);
            }

            // Read response into a buffered stream
            int readBytes = 0;
            byte[] sBuffer = new byte[512];
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }
            // Return result from buffered stream
            String result = new String(content.toByteArray());
            
            return result;
        } catch (IOException e) {
        	e.printStackTrace();
            //LogCenter.getInstance(context).logNetworkError(e, request,null);
            throw new PediatricsIOException(e);
        } finally {
            if (client != null) {
                client.getConnectionManager().shutdown();
            }
			if (inputStream!=null){
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
        }
    }
    
    private static class RequestWrapper {

        private HttpUriRequest mRequest;

        private boolean isFinish;

        public RequestWrapper(HttpUriRequest request) {
            mRequest = request;
            isFinish = false;
        }
    }
    
    public static String getCompleteUrl(String url, Bundle getParams) {
        StringBuilder newUrl = new StringBuilder();
        String[] items = url.split("\\?");
        if (items.length == 2) {
            newUrl.append(items[0]).append("?");
            String array[] = items[1].split("&");
            boolean first = true;
            for (String parameter : array) {
                String v[] = parameter.split("=");
                if (first) {
                    first = false;
                } else {
                    newUrl.append("&");
                }
                if (v.length == 2) {
                    newUrl.append(URLEncoder.encode(v[0])).append("=")
                            .append(URLEncoder.encode(v[1]));
                } else {
                    newUrl.append(parameter);
                }

            }
            if (getParams != null) {
                newUrl.append(encodeUrl(getParams));
            }
        } else {
            newUrl.append(url).append("?").append(encodeUrl(getParams));
        }

        return newUrl.toString();
    }
    
    public static String encodeUrl(Bundle parameters) {
        if (parameters == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : parameters.keySet()) {
            if (first) {
                first = false;
            } else {
                sb.append("&");
            }
            sb.append(urlEncode(key) + "=" + urlEncode(parameters.getString(key)));
        }
        return sb.toString();
    }
    
    private static String urlEncode(String in) {
        if (TextUtils.isEmpty(in)) {
            return "";
        }

        try {
            return URLEncoder.encode(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
    
    public static HttpClient getHttpsClientForWifi(Context context, String url) throws PediatricsIOException{
        HttpClient client = null;
        NetUtils.NetworkState state = NetUtils.getNetworkState(context);
        if (!TextUtils.isEmpty(url) && url.toLowerCase().startsWith("https")
                && state == NetUtils.NetworkState.WIFI) {
            client = getHttpsClient(context);
        } else {
            client = getHttpClient(context);
        }
        return client;
    }

    private static HttpClient getHttpsClient(Context context) {
        KeyStore trustStore = null;
        SSLSocketFactory sf = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            sf = new SSLSocketFactoryEx(trustStore);
        } catch (Exception e) {
            
        }

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("https", sf, 443));
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));// 防止重定向到http的url
        HttpParams params = new BasicHttpParams();
        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, schemeRegistry);

        HttpClient client = new DefaultHttpClient(ccm, params);

        HttpConnectionParamBean paramHelper = new HttpConnectionParamBean(client.getParams());
        paramHelper.setSoTimeout(Constants.TIMEOUT);
        paramHelper.setConnectionTimeout(Constants.TIMEOUT);
        paramHelper.setSocketBufferSize(Constants.SOCKET_BUFFER_SIZE);
        return client;
    }
    
    public static HttpClient getHttpClient(Context context) throws PediatricsIOException {
        NetUtils.NetworkState state = NetUtils.getNetworkState(context);
        HttpClient client = new DefaultHttpClient();
        if (state == NetUtils.NetworkState.NOTHING) {
            throw new PediatricsIOException("NoSignalException");
        } else if (state == NetUtils.NetworkState.MOBILE) {
            NetUtils.APNWrapper wrapper = null;
            wrapper = NetUtils.getAPN(context);
            if (!TextUtils.isEmpty(wrapper.proxy)) {
                client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,
                        new HttpHost(wrapper.proxy, wrapper.port));
            }
        }

        HttpConnectionParamBean paramHelper = new HttpConnectionParamBean(client.getParams());
        paramHelper.setSoTimeout(Constants.TIMEOUT);
        paramHelper.setConnectionTimeout(Constants.TIMEOUT);
        paramHelper.setSocketBufferSize(Constants.SOCKET_BUFFER_SIZE);
        return client;
    }
    
    public static NetworkState getNetworkState( Context ctx ) {
        ConnectivityManager cm = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        
        if (info == null || !info.isAvailable()) {
            return NetworkState.NOTHING;
        } else {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return NetworkState.MOBILE;
            } else {
                return NetworkState.WIFI;
            }
        }
    }

    public enum NetworkState {
        NOTHING, MOBILE, WIFI
    }
    
    private static NetworkConnectivityListener mConnectivityListener;
    private static ServiceHandler mServiceHandler;
    private static final String NO_APN = "N/A";
    static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    public static APNWrapper getAPN(Context ctx) {
        TelephonyManager telManager = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telManager.getSimOperator();
        String phoneSystem = getPhoneSystem();
        APNWrapper wrapper = new APNWrapper();
        // 如果是ophone系统
        if ((!TextUtils.isEmpty(phoneSystem) && (phoneSystem.equals("Ophone OS 2.0") || phoneSystem
                .equals("OMS2.5"))) && (operator.equals("46000") || operator.equals("46002"))) {
            mConnectivityListener = new NetworkConnectivityListener();
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            mServiceHandler = new ServiceHandler(Looper.myLooper());
            mConnectivityListener.registerHandler(mServiceHandler, 1);
            mConnectivityListener.startListening(ctx);
            String feature[] = queryApn(ctx, true);
            if (feature != null) {
                int fan = beginConnect(feature[0], ctx);
                if ((phoneSystem.equals("Ophone OS 2.0") && (fan == -1 || fan == 0))
                        || (phoneSystem.equals("OMS2.5") && (fan == 0))) {
                    feature = queryApn(ctx, false);// get the cmwap apntype
                }
                Object[] apnsetting = null;
                if (reflection == null) {
                    reflection = new Reflection();
                }
                String feature2 = null;
                String V[] = null;
                try {
                    apnsetting = (Object[]) reflection.invokeStaticMethod(
                            "oms.dcm.DataConnectivityHelper", "getApnSettings", new Object[] { ctx,
                                    feature });
                    String S = apnsetting[0].toString();
                    V = S.split(",");
                    feature2 = (String) reflection.invokeStaticMethod(
                            "oms.dcm.DataConnectivityHelper", "getProxyAndPort", new Object[] {
                                    ctx, V[2].trim() });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (feature2 == null) {
                    wrapper.name = feature[0] == null ? "" : feature[0].trim();
                    wrapper.apn = feature[0] == null ? "" : feature[1].trim();
                    wrapper.proxy = android.net.Proxy.getDefaultHost();
                    wrapper.proxy = TextUtils.isEmpty(wrapper.proxy) ? "" : wrapper.proxy;
                    wrapper.port = android.net.Proxy.getDefaultPort();
                    wrapper.port = wrapper.port > 0 ? wrapper.port : 80;
                    endConnectivity(feature[0]);
                    return wrapper;
                } else {
                    String[] address = feature2.split(":");
                    String IpAddress = null;
                    String PortAddress = null;

                    if (address != null && address.length >= 2) {
                        IpAddress = address[0];
                        PortAddress = address[1];
                    }
                    wrapper.name = V[1].substring(1);
                    wrapper.name = V[2].substring(1);
                    wrapper.proxy = IpAddress;
                    wrapper.port = Integer.parseInt(PortAddress);
                    endConnectivity(feature[0]);
                    return wrapper;
                }
            }
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(PREFERRED_APN_URI,
                    new String[] { "name", "apn", "proxy", "port" }, null, null, null);
        } catch (Exception e) {
            // 为了解决在4.2系统上禁止非系统进程获取apn相关信息，会抛出安全异常
            // java.lang.SecurityException: No permission to write APN settings
        }
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                wrapper.name = cursor.getString(0) == null ? "" : cursor.getString(0).trim();
                wrapper.apn = cursor.getString(1) == null ? "" : cursor.getString(1).trim();
            }
            cursor.close();
        }
        if (TextUtils.isEmpty(wrapper.apn)) {
            ConnectivityManager conManager = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = conManager.getActiveNetworkInfo();
            if (info != null) {
                wrapper.apn = info.getExtraInfo();
            }
        }
        if (TextUtils.isEmpty(wrapper.apn)) {
            TelephonyManager telephonyManager = (TelephonyManager) ctx
                    .getSystemService(Context.TELEPHONY_SERVICE);
            wrapper.apn = telephonyManager.getNetworkOperatorName();
        }
        if (TextUtils.isEmpty(wrapper.apn)) {
            wrapper.name = NO_APN;
            wrapper.apn = NO_APN;
        }
        wrapper.proxy = android.net.Proxy.getDefaultHost();
        wrapper.proxy = TextUtils.isEmpty(wrapper.proxy) ? "" : wrapper.proxy;
        wrapper.port = android.net.Proxy.getDefaultPort();
        wrapper.port = wrapper.port > 0 ? wrapper.port : 80;
        return wrapper;
    }
    
    private static Reflection reflection;
    private static String getPhoneSystem() {
        if (reflection == null) {
            reflection = new Reflection();
        }
        try {
            Object opp = reflection.newInstance("android.os.SystemProperties", new Object[] {});
            String system = (String) reflection.invokeMethod(opp, "get", new Object[] {
                    "apps.setting.platformversion", "" });
            return system;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
    }
    
    private static ConnectivityManager mConnMgr;

    private static int beginConnect(String apType, Context ctx) {
        int result = -1;
        Integer result1 = 0;
        if (mConnMgr == null) {
            mConnMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, apType);
        Integer result0 = 0;
        try {
            result0 = (Integer) reflection.getStaticProperty("oms.dcm.DataConnectivityConstants",
                    "FEATURE_ALREADY_ACTIVE");
            result1 = (Integer) reflection.getStaticProperty("oms.dcm.DataConnectivityConstants",
                    "FEATURE_REQUEST_STARTED");
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (result == result0.intValue()) {
            return -1;
        } else if (result == result1.intValue()) {
            return 1;
        } else {
            return -1;
        }
    }
    
    private static String[] queryApn(Context ctx, boolean isFirst) {
        Cursor c = ctx.getContentResolver().query(Uri.parse("content://telephony/apgroups"),
                new String[] { "type", "name" }, null, null, null);
        String feature[] = new String[2];
        if (c != null) {
            try {
                if (isFirst) {
                    c.moveToFirst();
                } else {
                    c.moveToFirst();
                    c.moveToNext();
                    c.moveToNext();
                }
                feature[0] = c.getString(0);
                feature[1] = c.getString(1);
                return feature;
            } finally {
                c.close();
            }
        }
        return null;
    }
    
    protected static void endConnectivity(String apType) {
        if (mConnMgr != null) {
            mConnMgr.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, apType);
        }
    }
    
    public static String generateUA(Context ctx) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(Build.MANUFACTURER).append("-").append(Build.MODEL);
        buffer.append("__");
        buffer.append("weibo");
        buffer.append("__");
        try {
            String versionCode = Utils.getVersion(ctx);
            buffer.append(versionCode.replaceAll("\\s+", "_"));
        } catch (final Exception localE) {
            Utils.loge(localE);
            buffer.append("unknown");
        }
        buffer.append("__").append("android").append("__android").append(android.os.Build.VERSION.RELEASE);
        return buffer.toString();
    }
    
    public byte[] getImage(String path) throws Exception{  
        URL url = new URL(path);  
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
        conn.setConnectTimeout(5 * 1000);  
        conn.setRequestMethod("GET");  
        InputStream inStream = conn.getInputStream();  
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){  
            return readStream(inStream);  
        }  
        return null;  
    }
    
    public static InputStream getImageStream(String path) throws Exception{  
        URL url = new URL(path);  
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
        conn.setConnectTimeout(5 * 1000);  
        conn.setRequestMethod("GET");  
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){  
            return conn.getInputStream();  
        }  
        return null;  
    }
    
    public static byte[] readStream(InputStream inStream) throws Exception{  
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] buffer = new byte[1024];  
        int len = 0;  
        while( (len=inStream.read(buffer)) != -1){  
            outStream.write(buffer, 0, len);  
        }  
        outStream.close();  
        inStream.close();  
        return outStream.toByteArray();  
    }
    
    public void saveFile(Bitmap bm, String fileName) throws IOException {  
        File dirFile = new File(Constants.FOLDER_PORTRAIT);  
        if(!dirFile.exists()){  
            dirFile.mkdir();  
        }  
        File myCaptureFile = new File(Constants.FOLDER_PORTRAIT + fileName);  
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));  
        bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);  
        bos.flush();  
        bos.close();  
    }
    
}
