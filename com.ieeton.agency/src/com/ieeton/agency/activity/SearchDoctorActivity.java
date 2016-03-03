package com.ieeton.agency.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fortysevendeg.swipelistview.SwipeListView;
import com.ieeton.agency.exception.PediatricsApiException;
import com.ieeton.agency.exception.PediatricsIOException;
import com.ieeton.agency.exception.PediatricsParseException;
import com.ieeton.agency.models.City;
import com.ieeton.agency.models.Doctor;
import com.ieeton.agency.net.NetEngine;
import com.ieeton.agency.utils.Constants;
import com.ieeton.agency.utils.Utils;
import com.ieeton.agency.view.CustomToast;
import com.ieeton.agency.view.PatientListItemView;
import com.ieeton.agency.view.SelectCityListItemView;
import com.ieeton.agency.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class SearchDoctorActivity extends Activity {
//	private static final int REQUEST_SELECT_CITY = 1;
//	
//	private ImageView mChooseCityBtn;
//	private ImageView mSearchCityBtn;
//	private ImageView mCancelBtn;
//	private EditText mEditText;
//	private ListView mHistoryListView;
//	private LinearLayout mSearchHistory;
//	private ImageView mClearHistoryBtn;
//	private CustomToast mProgressDialog;
//	private SwipeListView mSearchDoctorListView;
//	private DoctorListAdapter mSearchDoctorListAdapter;
//	private List<Doctor> mSearchDoctorList;
//	private LoadDoctorListTask mLoadDoctorListTask;
//	private boolean mTaskFree = true;
//	private boolean mHotSearchTaskFree = true;
//	private int mPageCount = 1;
//	private HistoryListAdapter mHistoryListAdapter;
//    private LinkedHashSet<String>         mSearchRecordSet;                                    //历史搜索记录数据
//    private List<String>                  mSearchRecordList;                                    //历史搜索记录数据
//	private String mKeywords;
//	private TextView mCurrentCity;
//	private String mCache;
//	public static final String            NEW_SEARCHKEYWORDLISTPATH   = "/searchkeywordlistcaches";
//	private static final int              MAX_NUM_RECORD          = 5;
//	private LoadSearchHistoryTask         mLoadHistoryTask;
//	
//	private ListView mHotSearchListView;
//	private LinearLayout mHotSearchLayout;
//	private HotSearchListAdapter mHotSearchListAdapter;
//    private List<String>                  mHotSearchList;
//    private boolean mShowSuggestion = true; 
//	
//    /**
//     * save search history to local cache file
//     */
//    private void saveSearchHistory(String key){
//        if( (mSearchRecordSet != null && mSearchRecordSet.contains( key ) )){
//            return;
//        }
//
//        final String filePath = mCache + NEW_SEARCHKEYWORDLISTPATH +"/"+Utils.getPassport(SearchDoctorActivity.this)+"passport";
//
//        //make sure mSearchRecordSet not null
//        if( mSearchRecordSet == null ){
//            Set<String> set = Utils.loadKeyWordList(filePath);
//            if( set == null ){
//                mSearchRecordSet = new LinkedHashSet<String>();
//            }else{
//                //兼容3.2.0之前的搜索历史数据(3.2.0之前以HashSet存储)
//                if( set instanceof HashSet ){
//                    mSearchRecordSet = new LinkedHashSet<String>(set);
//                }else {
//                    mSearchRecordSet = (LinkedHashSet<String>) set;
//                }
//            }
//        }
//
//        Iterator<String> itelator = mSearchRecordSet.iterator(); 
//        int size = mSearchRecordSet.size();
//        while( ( size > MAX_NUM_RECORD - 1 ) && itelator.hasNext() ){
////            在通过itelator遍历列表时，不能直接通过collection来remove元素，否则会发生ConcurrentModificationException
////            mSearchRecordSet.remove( itelator.next() );
//            itelator.next();
//            itelator.remove();
//            size = mSearchRecordSet.size();
//        }
//
//        mSearchRecordSet.add( key );
//
//        if( mSearchRecordList != null ){
//            mSearchRecordList.clear();
//            mSearchRecordList.addAll( mSearchRecordSet );
//        }else{
//            mSearchRecordList = new ArrayList<String>( mSearchRecordSet );
//        }
//        Utils.saveKeyWordList(filePath, mSearchRecordSet);
//    }
//    
//    /**
//     * load search history from local cache file
//     */
//    private List<String> loadSearchHistroy() {       
//        final String filePath = mCache + NEW_SEARCHKEYWORDLISTPATH+"/"+Utils.getPassport(SearchDoctorActivity.this)+"passport";
//        Set<String> set = Utils.loadKeyWordList( filePath );
//        if( set == null ){
//            return new ArrayList<String>( );
//        }
//        
//        if( set instanceof HashSet ){
//            mSearchRecordSet = new LinkedHashSet<String>(set);
//        }else {
//            mSearchRecordSet = (LinkedHashSet<String>) set;
//        }
//        if ( mSearchRecordSet != null ) {
//            ArrayList<String> list =  new ArrayList<String>( mSearchRecordSet );
//            Collections.reverse( list );
//            return list;
//        }else {
//            return new ArrayList<String>( );
//        }
//    }
//    
//    /**
//     *load Search History task 
//     */
//    private class LoadSearchHistoryTask extends AsyncTask<Void, Void, List<String>>{
//
//        @Override
//        protected List<String> doInBackground( Void... params ) {
//            if (mSearchRecordList != null && mSearchRecordList.size() != 0) {
//                return mSearchRecordList;
//            }else {
//                return loadSearchHistroy();
//            }
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            if( mHistoryListAdapter != null ){
//                mHistoryListAdapter.clear();
//            }
//            ((BaseAdapter)mHistoryListView.getAdapter()).notifyDataSetChanged();
//        }
//
//        @Override
//        protected void onPostExecute( List<String> result ) {
//            super.onPostExecute(result);
//            mSearchRecordList = result ;
//            ((BaseAdapter)mHistoryListView.getAdapter()).notifyDataSetChanged();
//            
//            if(mShowSuggestion){
//        		mSearchHistory.setVisibility(View.VISIBLE);
//            }
//        }
//        
//    }
//
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.search_doctor);
//		
//		mShowSuggestion = true;
//		
//		City city = Utils.getMyCity(SearchDoctorActivity.this);
//		mCurrentCity = (TextView)findViewById(R.id.tv_city);
//		mCurrentCity.setText(city.getCityName());
//		
//		mChooseCityBtn = (ImageView)findViewById(R.id.iv_select_city);
//		mChooseCityBtn.setOnClickListener(this);
//		
//		mSearchCityBtn = (ImageView)findViewById(R.id.search_btn);
//		mSearchCityBtn.setOnClickListener(this);
//		
//		mCancelBtn = (ImageView)findViewById(R.id.cancel_btn);
//		mCancelBtn.setOnClickListener(this);
//		
//		mEditText = (EditText)findViewById(R.id.search_input_box);
//		mEditText.setOnClickListener(this);
//		mEditText.addTextChangedListener( this );
//		mEditText.setImeOptions( EditorInfo.IME_ACTION_SEARCH );
//		mEditText.setOnEditorActionListener( new OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction( TextView v, int actionId, KeyEvent event ) {
//                switch (actionId) {
//                    case EditorInfo.IME_ACTION_UNSPECIFIED:
//                    case EditorInfo.IME_ACTION_SEARCH:
//                		mKeywords = mEditText.getEditableText().toString(); 
//                        search();
//                        return true;
//                    default:
//                        return false;
//                }
//            }
//        } );
//		
//		mSearchHistory = (LinearLayout)findViewById(R.id.search_history);
//		mSearchHistory.setVisibility(View.GONE);
//		
//		mHistoryListView = (ListView)findViewById(R.id.lv_search_history);
//		mHistoryListAdapter = new HistoryListAdapter();
//        mHistoryListView.setAdapter( mHistoryListAdapter );
//
//		mHistoryListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position,
//					long arg3) {
//				// TODO Auto-generated method stub
//				mKeywords = mSearchRecordList.get(position);
//				search();
//			}
//		});
//		
//		mHotSearchLayout = (LinearLayout)findViewById(R.id.ll_hot_search_keywords);
//		mHotSearchLayout.setVisibility(View.GONE);
//		
//		mHotSearchListView = (ListView)findViewById(R.id.lv_hot_search);
//		mHotSearchListAdapter = new HotSearchListAdapter();
//		mHotSearchListView.setAdapter(mHotSearchListAdapter);
//		mHotSearchListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position,
//					long arg3) {
//				// TODO Auto-generated method stub
//				mKeywords = mHotSearchList.get(position);
//				search();
//			}
//		});
//		
//		mClearHistoryBtn = (ImageView)findViewById(R.id.clear_search_history);
//		mClearHistoryBtn.setOnClickListener(this);
//		
//		mSearchDoctorListView = (SwipeListView)findViewById(R.id.lv_search_doctor_list);
//		mSearchDoctorListAdapter = new DoctorListAdapter();
//		mSearchDoctorListView.setAdapter(mSearchDoctorListAdapter);
//		mSearchDoctorListView.setVisibility(View.GONE);
//		
//		mCache = getCacheDir().getAbsolutePath();
//		try{
//			mLoadHistoryTask = new LoadSearchHistoryTask();
//			mLoadHistoryTask.execute();
//		}catch(RejectedExecutionException e){
//			e.printStackTrace();
//		}
//		
//		
//		try{
//			LoadHotSearchWordsTask task = new LoadHotSearchWordsTask();
//			task.execute();
//		}catch(RejectedExecutionException e){
//			e.printStackTrace();
//		}
//	}
//	
//	void search(){
//		if(mKeywords == null || mKeywords.equals("")){
//			Toast.makeText(SearchDoctorActivity.this, "请输入你要搜索的内容", Toast.LENGTH_SHORT).show();
//			return;
//		}
//		if(mTaskFree){
//			try{
//				mLoadDoctorListTask = new LoadDoctorListTask();
//				mLoadDoctorListTask.execute();
//			}catch(RejectedExecutionException e){
//				e.printStackTrace();
//			}
//		}
//	}
//
//	@Override
//	public void onClick(View v) {
//		if(v == mChooseCityBtn){
//			Intent intent = new Intent(this, SelectCityActivity.class);
//			intent.putExtra(SelectCityActivity.MODE, SelectCityActivity.MODE_SEARCH);
//			startActivityForResult(intent, REQUEST_SELECT_CITY);
//		}else if(v == mSearchCityBtn){
//			mKeywords = mEditText.getEditableText().toString(); 
//			search();
//		}else if(v == mCancelBtn){
//			finish();
//		}else if(v == mClearHistoryBtn){
//			mSearchRecordList.clear();
//			mHistoryListAdapter.notifyDataSetChanged();
//		}
//	}
//
//   class DoctorListAdapter extends BaseAdapter{
//
//		@Override
//		public int getCount() {
//			if(mSearchDoctorList != null){
//				return mSearchDoctorList.size();
//			}
//			return 0;
//		}
//
//		@Override
//		public Object getItem(int position) {
//			return position;
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			PatientListItemView view = null;
//			if (convertView == null){
//				view = new PatientListItemView(SearchDoctorActivity.this);
//			}else{
//				view = (PatientListItemView)convertView;
//			}
//			
//			if(mSearchDoctorList != null && mSearchDoctorList.size() > 0 && position < mSearchDoctorList.size()){
//				view.update(mSearchDoctorList.get(position));
//			}
//
//			return view;
//		}
//		
//	}
//
//	class LoadDoctorListTask extends AsyncTask<Void, Void, List<Doctor>>{
//		private Throwable mThr;
//
//		@Override
//		protected void onPreExecute() {
//			// TODO Auto-generated method stub
//			super.onPreExecute();
//			showProgress();
//			mTaskFree = false;
//		}
//
//		@Override
//		protected List<Doctor> doInBackground(Void... arg0) {
//			String result = "";
//
//				try {
//					//保存搜索关键字
//					saveSearchHistory( mKeywords.trim() );
//					 
//					result = NetEngine.getInstance(SearchDoctorActivity.this)
//									.searchDoctor(Utils.getPassport(SearchDoctorActivity.this),
//											Utils.getMyCity(SearchDoctorActivity.this).getCityID(),
//											mKeywords, Constants.MAX_PAGE_SIZE, mPageCount);
//				} catch (PediatricsIOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					mThr = e;
//				} catch (PediatricsParseException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					mThr = e;
//				} catch (PediatricsApiException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					mThr = e;
//				}
//
//				JSONObject object = null;
//				try {
//					object = new JSONObject(result);
//					if(!object.getBoolean("error")){
//						JSONObject json_data = object.getJSONObject("messages").getJSONObject("data");
//						JSONArray array = json_data.getJSONArray("docotors");
//		
//						List<Doctor> list = new ArrayList<Doctor>();
//						for(int i=0; i<array.length(); i++){
//							Doctor item = new Doctor((JSONObject)array.get(i));
//							list.add(item);
//						}
//						return list;
//					}else{
//						return null;
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//					return null;
//				}	
//		}
//
//		@Override
//		protected void onCancelled() {
//			// TODO Auto-generated method stub
//			super.onCancelled();
//			dismissProgress();
//		}
//
//		@Override
//		protected void onPostExecute(List<Doctor> result) {
//			mTaskFree = true;
//			dismissProgress();
//			
//			if (result == null || result.equals("")){
//				if(mThr != null){
//					Utils.handleErrorEvent(mThr, SearchDoctorActivity.this);
//				}else{
//					Utils.showToast(SearchDoctorActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
//				}
//				return;
//			}
//			if(mSearchDoctorList != null){
//				mSearchDoctorList.clear();
//			}
//			mSearchDoctorList = result;
//			if(mSearchDoctorListAdapter != null){
//				mSearchDoctorListAdapter.notifyDataSetChanged();
//			}
//			mSearchHistory.setVisibility(View.GONE);
//			mHotSearchLayout.setVisibility(View.GONE);
//			mSearchDoctorListView.setVisibility(View.VISIBLE);
//		}
//		
//	}
//	
//	public class HotSearchKeywords{
//		String id;
//		String keywords;
//		
//		public HotSearchKeywords(JSONObject data){
//			try{
//				id = data.getString("id");
//				keywords = data.getString("name");
//			}catch(JSONException e){
//				e.printStackTrace();
//			}
//		}
//		
//		public String getKeyword(){
//			return keywords;
//		}
//	}
//
//	class LoadHotSearchWordsTask extends AsyncTask<Void, Void, List<String>>{
//		private Throwable mThr;
//
//		@Override
//		protected void onPreExecute() {
//			// TODO Auto-generated method stub
//			super.onPreExecute();
//			showProgress();
//			mHotSearchTaskFree = false;
//		}
//
//		@Override
//		protected List<String> doInBackground(Void... arg0) {
//			String result = "";
//
//				try {					 
//					result = NetEngine.getInstance(SearchDoctorActivity.this).getHotSearchKeywords();
//				} catch (PediatricsIOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					mThr = e;
//				} catch (PediatricsParseException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					mThr = e;
//				} catch (PediatricsApiException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					mThr = e;
//				}
//Log.v("sereinli","Result:"+result);
//				JSONObject object = null;
//				try {
//					object = new JSONObject(result);
//					if(!object.getBoolean("error")){
//						JSONObject json_data = object.getJSONObject("messages").getJSONObject("data");
//						JSONArray array = json_data.getJSONArray("hots");
//		
//						List<String> list = new ArrayList<String>();
//						for(int i=0; i<array.length(); i++){
//							HotSearchKeywords item = new HotSearchKeywords((JSONObject)array.get(i));
//							list.add(item.getKeyword());
//						}
//						return list;
//					}else{
//						return null;
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//					return null;
//				}	
//		}
//
//		@Override
//		protected void onCancelled() {
//			super.onCancelled();
//			dismissProgress();
//		}
//
//		@Override
//		protected void onPostExecute(List<String> result) {
//			mHotSearchTaskFree = true;
//			dismissProgress();
//			
//			if (result == null || result.equals("")){
//				if(mThr != null){
//					Utils.handleErrorEvent(mThr, SearchDoctorActivity.this);
//				}else{
//					Utils.showToast(SearchDoctorActivity.this, R.string.PediatricsParseException, Toast.LENGTH_SHORT);
//				}
//				return;
//			}
//			if(mHotSearchList != null){
//				mHotSearchList.clear();
//			}
//			mHotSearchList = result;
//			if(mHotSearchListAdapter != null){
//				mHotSearchListAdapter.notifyDataSetChanged();
//			}
//			
//            if(mShowSuggestion){
//        		mHotSearchLayout.setVisibility(View.VISIBLE);
//            }
//		}
//		
//	}
//	
//	private void showProgress(){
//		if (mProgressDialog == null){
//			mProgressDialog = Utils.createProgressCustomToast(R.string.loading, SearchDoctorActivity.this);
//		}
//		mProgressDialog.show();
//	}
//	
//	private void dismissProgress(){
//		if (mProgressDialog != null){
//			mProgressDialog.cancel();
//		}
//	}
//
//	class HistoryListAdapter extends BaseAdapter{
//
//		@Override
//		public int getCount() {
//			if (mSearchRecordList != null && !mSearchRecordList.isEmpty()){
//				return mSearchRecordList.size();
//			}
//			return 0;
//		}
//		
//		public void clear(){
//            if( mSearchRecordList != null ){
//            	mSearchRecordList.clear();
//            }
//        }
//		
//		@Override
//		public Object getItem(int position) {
//			return position;
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			View view = null;
//			if (convertView == null){
//				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				view = inflater.inflate(R.layout.select_city_list_item, null);
//			}else{
//				view = convertView;
//			}
//			TextView record = (TextView)view.findViewById(R.id.tv_city);
//			record.setText(mSearchRecordList.get(position));
//
//			return view;
//		}
//		
//	}
//
//	class HotSearchListAdapter extends BaseAdapter{
//
//		@Override
//		public int getCount() {
//			if (mHotSearchList != null && !mHotSearchList.isEmpty()){
//				return mHotSearchList.size();
//			}
//			return 0;
//		}
//		
//		public void clear(){
//            if( mHotSearchList != null ){
//            	mHotSearchList.clear();
//            }
//        }
//		
//		@Override
//		public Object getItem(int position) {
//			return position;
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			View view = null;
//			if (convertView == null){
//				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				view = inflater.inflate(R.layout.select_city_list_item, null);
//			}else{
//				view = convertView;
//			}
//			TextView record = (TextView)view.findViewById(R.id.tv_city);
//			record.setText(mHotSearchList.get(position));
//
//			return view;
//		}
//		
//	}
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// TODO Auto-generated method stub
//		super.onActivityResult(requestCode, resultCode, data);
//		
//		if(resultCode != RESULT_OK){
//			return;
//		}
//		
//		switch (requestCode) {
//		case REQUEST_SELECT_CITY:
//			City city = (City) data.getExtras().getSerializable("city");
//			String name = city.getCityName();
//			if(name != null && !name.equals("")){
//				mCurrentCity.setText(city.getCityName());
//			}
//			break;
//
//		default:
//			break;
//		}
//	}
//
//	@Override
//	public void afterTextChanged(Editable s) {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void beforeTextChanged(CharSequence s, int start, int count,
//			int after) {
//
//		mShowSuggestion = true;
//
//		if(mSearchRecordList != null && mSearchRecordList.size() > 0){
//			mSearchHistory.setVisibility(View.VISIBLE);
//		}
//		if(mHotSearchList != null && mHotSearchList.size() > 0){
//			mHotSearchLayout.setVisibility(View.VISIBLE);
//		}
//		
//	}
//
//	@Override
//	public void onTextChanged(CharSequence s, int start, int before, int count) {
//		// TODO Auto-generated method stub
//		
//	}
//
}
