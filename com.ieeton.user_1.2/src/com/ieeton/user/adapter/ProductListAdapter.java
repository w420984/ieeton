package com.ieeton.user.adapter;

import java.util.List;

import com.ieeton.user.models.Product;
import com.ieeton.user.view.ProductItemView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ProductListAdapter extends BaseAdapter {
	private List<Product> mList;
	private Context mContext;
	
	public ProductListAdapter(Context context, List<Product> list){
		mList = list;
		mContext = context;
	}
	
	public void refresh(List<Product> list){
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
		ProductItemView view;
		if (convertView == null){
			view = new ProductItemView(mContext);
		}else{
			view = (ProductItemView) convertView;
		}
		if (mList != null && !mList.isEmpty() && position<mList.size()){
			view.update(mList.get(position));
		}
		return view;
	}

}
