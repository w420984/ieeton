package com.ieeton.user.view;

import com.ieeton.user.R;
import com.ieeton.user.models.IeetonUser;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HorizontalListItemView extends RelativeLayout {
	private TextView name;
	private RoundedImageView header;
	private Context mContext;
	
	public HorizontalListItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public HorizontalListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}

	public HorizontalListItemView(Context context, AttributeSet attrs, int arg) {
		super(context, attrs);
		mContext = context;
		initView();
	}
	
	private void initView(){
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	inflater.inflate(R.layout.horizontal_list_item, this);
    	name = (TextView) findViewById(R.id.name);
    	header = (RoundedImageView) findViewById(R.id.header);		
	}
	
	public void update(IeetonUser user){
		if (user == null){
			return;
		}
		name.setText(user.getName());
		AsyncBitmapLoader.getInstance().loadBitmap(mContext, header, NetEngine.getImageUrl(user.getAvatar()));
	}
}
