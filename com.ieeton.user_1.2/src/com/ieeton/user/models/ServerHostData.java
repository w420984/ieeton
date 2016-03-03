package com.ieeton.user.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ieeton.user.R;
import com.ieeton.user.utils.Constants;
import com.ieeton.user.utils.Utils;

import android.content.Context;
import android.text.TextUtils;

public class ServerHostData {
	private String mImageServer;
	private String mContentServer;
	private String mIvrNumber;
	private String mXiaomishu;
	private List<ProductCategory> mProductCategoryList;
	private List<Lable> mLableList;
	private List<ThirdPartner> mThirdPartnerList;
	
	public ServerHostData(Context context){
		mImageServer = Constants.SERVER_HOST_IMAGE_SERVER;
		mContentServer = Constants.SERVER_HOST_CONTENT_SERVER;
		mIvrNumber = context.getResources().getString(R.string.default_number);;
		mXiaomishu = "passport_5200";
		mProductCategoryList = new ArrayList<ProductCategory>();
		{
			ProductCategory item = new ProductCategory();
			item.setCategoryId(1);
			item.setCategoryName(context.getString(R.string.category1));
			item.setCategoryIconUrl("/upload/stationtemp/000/000/004.png");
			mProductCategoryList.add(item);
		}
		{
			ProductCategory item = new ProductCategory();
			item.setCategoryId(2);
			item.setCategoryName(context.getString(R.string.category2));
			item.setCategoryIconUrl("/upload/stationtemp/000/000/005.png");
			mProductCategoryList.add(item);
		}
		{
			ProductCategory item = new ProductCategory();
			item.setCategoryId(3);
			item.setCategoryName(context.getString(R.string.category3));
			item.setCategoryIconUrl("/upload/stationtemp/000/000/006.png");
			mProductCategoryList.add(item);
		}
		{
			ProductCategory item = new ProductCategory();
			item.setCategoryId(4);
			item.setCategoryName(context.getString(R.string.category4));
			item.setCategoryIconUrl("/upload/stationtemp/000/000/007.png");
			mProductCategoryList.add(item);
		}
		{
			ProductCategory item = new ProductCategory();
			item.setCategoryId(5);
			item.setCategoryName(context.getString(R.string.category5));
			item.setCategoryIconUrl("/upload/stationtemp/000/000/008.png");
			mProductCategoryList.add(item);
		}
	}
	
	public ServerHostData(Context context, JSONObject json){
		mImageServer = json.optString("imageserver");
		mContentServer = json.optString("dataserver");
		mIvrNumber = json.optString("servicephonenumber");
		mIvrNumber.replace("-", "");
		mXiaomishu = json.optString("dadamishuid");
		String uid = json.optString("uid");
		if (!TextUtils.isEmpty(uid)){
			Utils.setMyUid(context, uid);
		}
		if (mProductCategoryList == null){
			mProductCategoryList = new ArrayList<ProductCategory>();
		}
		JSONArray array = json.optJSONArray("productcategory");
		if (array != null && array.length()>0){
			mProductCategoryList.clear();
			for(int i=0; i<array.length(); i++){
				ProductCategory item = new ProductCategory(array.optJSONObject(i));
				mProductCategoryList.add(item);
			}
		}
		
		if (mLableList == null){
			mLableList = new ArrayList<Lable>();
		}
		array = json.optJSONArray("label");
		if (array != null && array.length()>0){
			mLableList.clear();
			for(int i=0; i<array.length(); i++){
				Lable item = new Lable(array.optJSONObject(i));
				mLableList.add(item);
			}
		}
		
		array = json.optJSONArray("menu");
		if (array != null && array.length() > 0){
			if (mThirdPartnerList == null){
				mThirdPartnerList = new ArrayList<ThirdPartner>();
			}
			mThirdPartnerList.clear();
			for(int i=0; i<array.length(); i++){
				ThirdPartner item = new ThirdPartner(array.optJSONObject(i));
				mThirdPartnerList.add(item);
			}
			 
		}
	}
	public String getSecretaryID(){
		return mXiaomishu;
	}
		
	public String getIvrNumber(){
		return mIvrNumber;
	}
	
	public String getImageServerUrl(){
		return mImageServer;
	}

	public String getContentServerUrl(){
		return mContentServer;
	}
	
	public void setImageServerUrl(String url){
		mImageServer = url;
	}

	public List<ProductCategory> getProductCategoryList(){
		return mProductCategoryList;
	}
	
	public List<ThirdPartner> getThirdPartnerList(){
		return mThirdPartnerList;
	}
	
	public List<Lable> getLableList(){
		return mLableList;
	}
}
