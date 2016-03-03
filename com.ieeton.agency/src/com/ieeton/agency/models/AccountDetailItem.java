package com.ieeton.agency.models;

import org.json.JSONObject;

import com.ieeton.agency.R;

import android.content.Context;
import android.text.TextUtils;

public class AccountDetailItem {
	public static final String DETAIL_TYPE_CALL = "call";
	public static final String DETAIL_TYPE_WITHDRAW = "withdraw";
	public static final String DETAIL_TYPE_RECHARGE = "recharge";
	public static final String DETAIL_TYPE_REWARDIN = "rewardIn";
	public static final String DETAIL_TYPE_REWARDOUT = "rewardOut";
	public static final String DETAIL_TYPE_PRODUCT_BUT = "productBuy";
	public static final String DETAIL_TYPE_PRODUCT_SELL = "productSell";
	
	private String type;
	private String title;
	private String time;
	private String amount;
	private ChatUser user;
	//private Product product;
	
	public AccountDetailItem(Context context, JSONObject obj){
		type = obj.optString("type");
		amount = Math.abs(obj.optDouble("amount")) + "";
		String createTime = obj.optString("createDate");
		createTime = getTime(createTime);
		time = createTime;
		if (DETAIL_TYPE_CALL.equals(type)){
			int duration = obj.optInt("callDuration");
			duration = duration%60 == 0 ? duration/60 : duration/60 +1;
			user = new ChatUser(null, obj.optJSONObject("callUser"));
			title = user.getName();
			amount = "-" + amount;
			time += "   " +
					String.format(context.getString(R.string.call_duration), (duration+""));
		}else if (DETAIL_TYPE_RECHARGE.equals(type)){
			title = context.getString(R.string.recharge);
			amount = "+" + amount;
		}else if (DETAIL_TYPE_PRODUCT_BUT.equals(type) || 
				DETAIL_TYPE_PRODUCT_SELL.equals(type)){
//			product = new Product(obj.optJSONObject("product"));
//			title = product.getName();
//			String s = DETAIL_TYPE_PRODUCT_BUT.equals(type) ? "-" : "+";
//			amount = s + amount;
		}else if (DETAIL_TYPE_REWARDIN.equals(type) || 
				DETAIL_TYPE_REWARDOUT.equals(type)){
			user = new ChatUser(null, obj.optJSONObject("rewardUser"));
			if (DETAIL_TYPE_REWARDIN.equals(type)){
				title = user.getName() + "  打赏了我";
				amount = "+" + amount;
			}else{
				title = "我打赏了  " + user.getName();
				amount = "-" + amount;
			}
		}else{
			title = context.getString(R.string.withdraw);
			amount = "-" + amount;
		}
	}
	
	public String getType(){
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
	
	public ChatUser getUser(){
		return user;
	}
	
//	public Product getProduct(){
//		return product;
//	}
	
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
