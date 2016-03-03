package com.ieeton.user.view;

import com.ieeton.user.R;
import com.ieeton.user.models.ListItem;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListItemView extends LinearLayout{
	private Context mContext;
	
	public ListItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	
	public ListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	public ListItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView();
	}

	public void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.recharge_list_item, this);

	}

	public void update(ListItem item){
		if (item == null){
			return;
		}
		TextView tv = (TextView)findViewById(R.id.tv_text);
		tv.setText(item.getText());
		ImageView image = (ImageView) findViewById(R.id.iv_checked);
		image.setVisibility(item.isChecked() ? View.VISIBLE : View.INVISIBLE);
	}
}

