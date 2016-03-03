package com.ieeton.user.activity;

import java.util.ArrayList;
import java.util.List;
import com.ieeton.user.IeetonApplication;
import com.ieeton.user.R;
import com.ieeton.user.models.City;
import com.ieeton.user.models.CommonItem;
import com.ieeton.user.view.CommonItemView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class SelectCityActivity extends TemplateActivity {
	private List<CommonItem> mCityList;
	private CityListAdapter mCityListAdapter;
	private ListView mListView;
	public static String RETURN_DATA = "return_data";
	public static String MODE = "mode";
	public static int MODE_SEARCH = 1;
	public static int MODE_EDIT_PROFILE = 2;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
        setView(R.layout.select_city);
        setTitleBar(null, null, null);
        
        mCityList = new ArrayList<CommonItem>();
        for(City city : IeetonApplication.mCityList){
        	mCityList.add(new CommonItem(city, false));
        }
        
        ImageView back = (ImageView) findViewById(R.id.iv_back);
        back.setOnClickListener(this);
        mListView = (ListView)findViewById(R.id.lv_city_list);
        mCityListAdapter = new CityListAdapter();
        mListView.setAdapter(mCityListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long aid) {
				CommonItem item = mCityList.get(position);
				if (item == null){
					return;
				}
				selectCityDone(item);				
			}
		});
	}

	private void selectCityDone(CommonItem item){
		City city = new City(item);
		Intent intent = new Intent();
		intent.putExtra(RETURN_DATA, city);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	class CityListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if (mCityList != null && !mCityList.isEmpty()){
				return mCityList.size();
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
			CommonItemView view = null;
			if (convertView == null){
				view = new CommonItemView(SelectCityActivity.this);
			}else{
				view = (CommonItemView)convertView;
			}
			
			view.update(mCityList.get(position));

			return view;
		}
		
	}


	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.iv_back){
			setResult(RESULT_CANCELED);
			finish();
		}
		super.onClick(v);
	}
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
	}
}
