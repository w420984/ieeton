package com.ieeton.user.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ieeton.user.R;
import com.ieeton.user.activity.ImageViewerActivity;
import com.ieeton.user.models.Comment;
import com.ieeton.user.net.NetEngine;
import com.ieeton.user.utils.AsyncBitmapLoader;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommentItemView extends LinearLayout {
	private Context mContext;
	private TextView mTvName;
	private RoundedImageView mIvHeader;
	private TextView mTvTime;
	private TextView mTvContent;
	private ViewGroup mVgPics;
	private List<ImageView> mIvStarList;
	private List<ImageView> mIvPics;
	private Comment mComment;

	public CommentItemView(Context context) {
		super(context);
		mContext = context;
		initView();
	}

	public CommentItemView(Context context, AttributeSet attrs) {
		super(context);
		mContext = context;
		initView();
	}

	public CommentItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context);
		mContext = context;
		initView();
	}
	
	private void initView(){
		LayoutInflater inflater = (LayoutInflater)getContext().
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.comment_item_view, this);
		
		mTvName = (TextView) findViewById(R.id.tv_name);
		mIvHeader = (RoundedImageView) findViewById(R.id.header);
		mTvTime = (TextView) findViewById(R.id.tv_time);
		mTvContent = (TextView) findViewById(R.id.tv_content);
		mVgPics = (ViewGroup) findViewById(R.id.ll_pics);
		
		mIvStarList = new ArrayList<ImageView>();
		{
			ImageView view = (ImageView) findViewById(R.id.iv_star1);
			mIvStarList.add(view);
		}
		{
			ImageView view = (ImageView) findViewById(R.id.iv_star2);
			mIvStarList.add(view);
		}
		{
			ImageView view = (ImageView) findViewById(R.id.iv_star3);
			mIvStarList.add(view);
		}
		{
			ImageView view = (ImageView) findViewById(R.id.iv_star4);
			mIvStarList.add(view);
		}
		{
			ImageView view = (ImageView) findViewById(R.id.iv_star5);
			mIvStarList.add(view);
		}
		mIvPics = new ArrayList<ImageView>();
		{
			ImageView view = (ImageView) findViewById(R.id.iv_pic1);
			mIvPics.add(view);
		}
		{
			ImageView view = (ImageView) findViewById(R.id.iv_pic2);
			mIvPics.add(view);
		}
		{
			ImageView view = (ImageView) findViewById(R.id.iv_pic3);
			mIvPics.add(view);
		}
		for(ImageView view : mIvPics){
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mComment == null || mComment.getPics() == null 
							|| mComment.getPics().isEmpty()){
						return;
					}
					Intent intent = new Intent(mContext, ImageViewerActivity.class);
					intent.putExtra(ImageViewerActivity.TAG_PIC_ATTACHMENTS, (Serializable)mComment.getPics());
					intent.putExtra(ImageViewerActivity.TAG_CURRENT_PIC_INDEX, mIvPics.indexOf(v));
					mContext.startActivity(intent);
				}
			});
		}
	}
	
	public void update(Comment comment){
		if (comment == null){
			return;
		}
		mComment = comment;
		if (comment.getCommentUser()!=null){
			mTvName.setText(comment.getCommentUser().getName());
			AsyncBitmapLoader.getInstance().loadBitmap(mContext, mIvHeader, 
					NetEngine.getImageUrl(comment.getCommentUser().getAvatar()));
		}
		mTvTime.setText(comment.getCommentDate());
		mTvContent.setText(comment.getCommentContent());
		for(int i=0; i<comment.getCommentLevel(); i++){
			mIvStarList.get(i).setImageResource(R.drawable.star_h);
		}
		List<String> picList = comment.getPics();
		if (picList == null || picList.isEmpty()){
			mVgPics.setVisibility(View.GONE);
		}else{
			mVgPics.setVisibility(View.VISIBLE);
			for(ImageView pic : mIvPics){
				pic.setVisibility(View.GONE);
			}
			for(int i=0; i<picList.size(); i++){
				mIvPics.get(i).setVisibility(View.VISIBLE);
				AsyncBitmapLoader.getInstance().loadBitmap(mContext, 
						mIvPics.get(i), NetEngine.getImageUrl(picList.get(i)));
			}
		}
	}
}
