package com.ieeton.user.adapter;

import java.util.List;

import com.ieeton.user.models.Comment;
import com.ieeton.user.models.Product;
import com.ieeton.user.view.CommentItemView;
import com.ieeton.user.view.ProductItemView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CommentListAdapter extends BaseAdapter {
	private List<Comment> mList;
	private Context mContext;
	
	public CommentListAdapter(Context context, List<Comment> list){
		mList = list;
		mContext = context;
	}
	
	public void refresh(List<Comment> list){
		mList = list;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if (mList != null){
			return mList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CommentItemView view;
		if (convertView == null){
			view = new CommentItemView(mContext);
		}else{
			view = (CommentItemView) convertView;
		}
		if (mList != null && !mList.isEmpty() && position<mList.size()){
			view.update(mList.get(position));
		}
		return view;
	}

}
