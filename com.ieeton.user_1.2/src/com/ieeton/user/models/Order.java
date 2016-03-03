package com.ieeton.user.models;

import java.io.Serializable;

import org.json.JSONObject;

import com.ieeton.user.utils.Utils;

public class Order implements Serializable{
	private static final long serialVersionUID = 7865311655821811997L;

	private String id;				//订单记录id
	private int amount;				//订单总金额
	private int integral;			//订单总积分
	private int productCount;		//该笔订单购买产品数量
	private String date;			//订单时间
	private int status;				//订单状态
	private int isComment;			//是否已评价
	private String orderId;			//订单号
	private Product product;		//订单产品信息
	
	public Order(JSONObject obj){
		if (obj == null){
			return;
		}
		id = obj.optString("id");
		amount = obj.optInt("amount");
		integral = obj.optInt("integral");
		productCount = obj.optInt("quantity");
		date = obj.optString("buytime");
		status = obj.optInt("status");
		orderId = obj.optString("internalId");
		isComment = obj.optInt("Commentstatus");
		product = new Product(obj.optJSONObject("productinfo"));
	}
	
	public String getId(){
		return id;
	}
	
	public int getAmount(){
		return amount;
	}
	
	public int getIntegral(){
		return integral;
	}
	
	public int getProductCount(){
		return productCount;
	}
	
	public String getOrderId(){
		return orderId;
	}
	
	public String getDate(){
		return date;
	}
	
	public int getStatus(){
		return status;
	}
	
	public int isComment(){
		return isComment;
	}
	
	public Product getProduct(){
		return product;
	}
	
}
