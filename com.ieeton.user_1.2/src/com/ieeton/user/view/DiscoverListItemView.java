package com.ieeton.user.view;

import com.ieeton.user.R;
import com.ieeton.user.models.Article;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DiscoverListItemView extends RelativeLayout {
	private Context mContext;
	private TextView mTitle;
	private ImageView mSummaryPic;

	public DiscoverListItemView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public DiscoverListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public DiscoverListItemView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init();
	}

	public void init(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.discover_list_item, this);
		
		initViews();

	}
	
	public void initViews(){
		mTitle = (TextView)findViewById(R.id.tv_title);
		mSummaryPic = (ImageView)findViewById(R.id.iv_pic);
	}
	
	public void update(Article article){
		if (article == null){
			return;
		}
		mTitle.setText(article.getTitle());
		AsyncBitmapLoader.getInstance().loadBitmap(mContext, mSummaryPic, NetEngine.getImageUrl(article.getSummaryPicUrl()));
	}
}
