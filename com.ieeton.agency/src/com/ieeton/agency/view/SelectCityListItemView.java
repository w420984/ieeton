package com.ieeton.agency.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ieeton.agency.models.City;
import com.ieeton.agency.R;

public class SelectCityListItemView extends LinearLayout{
	private Context mContext;
	
	public SelectCityListItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	
	public SelectCityListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public SelectCityListItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	public void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.select_city_list_item, this);

	}

	public void update(City city){
		TextView tv = (TextView)findViewById(R.id.tv_city);
		tv.setText(city.getCityName());
	}
}
