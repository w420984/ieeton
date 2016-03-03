package com.ieeton.user.models;

import org.json.JSONObject;

import com.ieeton.user.R;
import android.content.Context;
import android.text.TextUtils;

public class AccountDetailItem {
	public static final int DETAIL_TYPE_RECHARGE = 4;
	public static final int DETAIL_TYPE_PRODUCT_BUY = 1;
	public static final int DETAIL_TYPE_PRODUCT_SALE = 6;
	
	private int type;
	private String title;
	private String time;
	private String amount;
	private Product product;
	
	public AccountDetailItem(Context context, JSONObject obj){
		type = obj.optInt("paymethod");
		amount =obj.optString("changeamount");
		String createTime = obj.optString("changetime");
//		createTime = getTime(createTime);
		time = createTime;
		if (DETAIL_TYPE_RECHARGE == type){
			title = context.getString(R.string.recharge);
			amount = "+" + amount;
		}else if (DETAIL_TYPE_PRODUCT_BUY == type){
			product = new Product(obj.optJSONObject("productinfo"));
			title = product.getName();
			amount = "-" + amount;
		}else if (DETAIL_TYPE_PRODUCT_SALE == type){
			product = new Product(obj.optJSONObject("productinfo"));
			title = product.getName();
			amount = "+" + amount;
		}
	}
	
	public int getType(){
		return type;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getTime(){
		return time;
	}
	
	public String getAmount(){
		return amount;
	}
	
	public Product getProduct(){
		return product;
	}
	
	private String getTime(String serverTime){
		String time = "";
		if (TextUtils.isEmpty(serverTime)){
			return time;
		}
		serverTime = serverTime.replaceAll("-", ".")
						.replaceAll("T", " ");
		int end = serverTime.lastIndexOf(":");
		if (end > 0){
			time = serverTime.substring(0, end);
		}
		return time;
	}

}
