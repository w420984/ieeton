package com.ieeton.user.view;

import java.util.ArrayList;

import com.ieeton.user.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

/**
 * 修改于：2013-2-28 17:03:35
 *         修正 ListView item 点击响应失败！
 * 
 * @author Yichou
 *
 */
public class PopMenu implements OnItemClickListener {
        public interface OnItemClick {
                public void onItemClick(int index, int type);
        }
         
        private ArrayList<String> itemList;
        private Context context;
        private PopupWindow popupWindow;
        private ListView listView;
        private OnItemClick listener;
        private LayoutInflater inflater;
        private int mType;
        
         
        public PopMenu(Context context) {
                this.context = context;
 
                itemList = new ArrayList<String>();
 
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.common_list, null);
 
                listView = (ListView) view.findViewById(R.id.list);
                listView.setAdapter(new PopAdapter());
                listView.setOnItemClickListener(this);
 
                popupWindow = new PopupWindow(view, 
                				LayoutParams.FILL_PARENT,
                                LayoutParams.WRAP_CONTENT);
                // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景（很神奇的）
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
 
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                        listener.onItemClick(position, mType);
                }
                dismiss();
        }
 
        public void setPopType(int type){
        	mType = type;
        }
        
        public int getType(){
        	return mType;
        }
        
        // 设置菜单项点击监听器
        public void setOnItemClick(OnItemClick listener) {
                 this.listener = listener;
        }
 
        // 批量添加菜单项
        public void addItems(String[] items) {
                for (String s : items)
                        itemList.add(s);
        }
 
        // 单个添加菜单项
        public void addItem(String item) {
                itemList.add(item);
        }
 
        public void clearList(){
        	itemList.clear();
        }
        
        // 下拉式 弹出 pop菜单 parent 右下角
        public void showAsDropDown(View parent) {
                popupWindow.showAsDropDown(parent, 10,
                // 保证尺寸是根据屏幕像素密度来的
                                0);
 
                // 使其聚集
                popupWindow.setFocusable(true);
                // 设置允许在外点击消失
                popupWindow.setOutsideTouchable(false);
                // 刷新状态
                popupWindow.update();
        }
 
        // 隐藏菜单
        public void dismiss() {
                popupWindow.dismiss();
        }
 
        public boolean isShowing(){
        	return popupWindow.isShowing();
        }
        
        public void setOnDismissListener(OnDismissListener l){
        	popupWindow.setOnDismissListener(l);
        }
        
        // 适配器
        private final class PopAdapter extends BaseAdapter {
                @Override
                public int getCount() {
                        return itemList.size();
                }
 
                @Override
                public Object getItem(int position) {
                        return itemList.get(position);
                }
 
                @Override
                public long getItemId(int position) {
                        return position;
                }
 
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                        ViewHolder holder;
                        if (convertView == null) {
                                convertView = inflater.inflate(R.layout.common_item_view, null);
                                holder = new ViewHolder();
                                convertView.setTag(holder);
                                holder.groupItem = (TextView) convertView.findViewById(R.id.tv_name);
                        } else {
                                holder = (ViewHolder) convertView.getTag();
                        }
                        holder.groupItem.setText(itemList.get(position));
 
                        return convertView;
                }
 
                private final class ViewHolder {
                        TextView groupItem;
                }
        }
}