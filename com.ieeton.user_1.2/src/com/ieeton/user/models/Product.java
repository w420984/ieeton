package com.ieeton.user.models;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ieeton.user.utils.Utils;

import android.text.TextUtils;

public class Product implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3218149909907634598L;
	
	private int id;							//产品id
	private int categoryId;					//产品类型
	private String name;					//产品名称
	private String ownerId;					//产品拥有者uid
	private int integral;					//产品兑换积分
	private int price;						//产品价格
	private int goodrate;					//产品好评率
	private String distance;				//产品距离
	private String introduce;				//产品简介
	private String date;					//产品起止时间
	private String buynote;					//购买须知
	private String externalUrl;				//购买外链
	private String productUrl;				//推荐列表图片
	private String productionUrl;			//搜索列表图片
	private String videoUrl;				//视频连接
	private boolean isCollection;			//是否收藏该产品
	private List<String> mPics;				//产品图片列表
	private IeetonUser owner;				//产品拥有者
	private int status;						//产品状态
	private List<IeetonUser> doctorList;	//负责该产品的医生列表
	
	public Product(JSONObject obj){
		if (obj == null){
			return;
		}
		id = obj.optInt("productid");
		categoryId = obj.optInt("categoryid");
		name = obj.optString("productname");
		ownerId = obj.optString("owneruid");
		integral = obj.optInt("Integral");
		price = obj.optInt("price");
		if (price == 0){
			price = obj.optInt("ptprice");
		}
		goodrate = obj.optInt("goodrate");
		double km = obj.optDouble("km");
		distance = String.format("%.2f", km);
		if ("NaN".equals(distance)){
			distance = "";
		}
		introduce = obj.optString("introduce");
		date = obj.optString("daTime");
		buynote = obj.optString("buynote");
		externalUrl = obj.optString("externalurl");
		videoUrl = obj.optString("videoUrl");
		productUrl = obj.optString("productUrl");
		productionUrl = obj.optString("productionurl");
		if (TextUtils.isEmpty(productionUrl)){
			productionUrl = obj.optString("producticonurl");
		}
		isCollection = obj.optBoolean("isCollection");
		status = obj.optInt("status");
		mPics = new ArrayList<String>();
		for (int i=0; i<9; i++){
			String key = "image" + (i+1) + "url";
			String value = obj.optString(key);
			if (!TextUtils.isEmpty(value)){
				mPics.add(value);
			}
		}
		owner = new IeetonUser(null, obj.optJSONObject("user"));
		
		JSONArray array = obj.optJSONArray("doctor");
		if (array != null && array.length()>0){
			doctorList = new ArrayList<IeetonUser>();
			for(int i=0; i<array.length(); i++){
				IeetonUser item = new IeetonUser(null, array.optJSONObject(i));
				doctorList.add(item);
			}
		}
	}
	
	public int getId(){
		return id;
	}
	
	public int getCategoryId(){
		return categoryId;
	}
	
	public String getName(){
		return name;
	}
	
	public String getOwnerUid(){
		return ownerId;
	}
	
	public int getIntegral(){
		return integral;
	}
	
	public int getPrice(){
		return price;
	}

	public int getGoodrate(){
		return goodrate;
	}
	
	public String getDistance(){
		return distance;
	}
	
	public String getIntroduce(){
		return introduce;
	}
			
	public String getDate(){
		return date;
	}
	
	public String getBuynote(){
		return buynote;
	}
	
	public String getExternalUrl(){
		return externalUrl;
	}
	
	public String getProductUrl(){
		return productUrl;
	}
	
	public String getProductionUrl(){
		return productionUrl;
	}
	
	public String getVideoUrl(){
		return videoUrl;
	}
	
	public boolean isCollection(){
		return isCollection;
	}
	
	public void setIsCollection(boolean status){
		isCollection = status;
	}
	
	public List<String> getPics(){
		return mPics;
	}
	
	public IeetonUser getOwner(){
		return owner;
	}
	
	public int getStatus(){
		return status;
	}
	
	public List<IeetonUser> getDoctorList(){
		return doctorList;
	}
}
