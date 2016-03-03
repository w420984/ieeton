package com.ieeton.user.db;

import java.util.ArrayList;
import java.util.List;

import com.ieeton.user.models.ChatInfo;
import com.ieeton.user.utils.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatInfoDB{
	private final static int Version = 1;
	
    public final static String    CHAT_INFO_TABLE     		 = "chat_info";
    public final static String    CHAT_INFO_ID        		 = "id";
    public final static String    CHAT_INFO_FROM_MSG_NUM	 = "msg_num";
    public final static String    CHAT_INFO_LIKE_BAR_STATUS  = "like_bar";
    
    private static class DatabaseHelper extends SQLiteOpenHelper{

    	public DatabaseHelper(Context context){
    		super(context, getTableName(), null, Version);
    	}
    	
		@Override
		public void onCreate(SQLiteDatabase db) {
	        try {
	            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
	            sql.append(getTableName()).append(" (")
	            		.append(CHAT_INFO_ID).append(" TEXT, ")
	                    .append(CHAT_INFO_FROM_MSG_NUM).append(" INTEGER, ")
	                    .append(CHAT_INFO_LIKE_BAR_STATUS).append(" INTEGER)");
	            db.execSQL(sql.toString());
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }				
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String sql = "DROP TABLE IF EXISTS " + getTableName();
	        db.execSQL(sql);
	        //db.execSQL(sql.toString());
	        
	        onCreate(db);			
		}
    	
    }
    
    private SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private static Context mContext;
    
    public ChatInfoDB(Context context){
    	this.mContext = context;
    	mDBHelper = new DatabaseHelper(context);
    }
    
	public ChatInfoDB open() throws SQLException{
		mDB = mDBHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		mDBHelper.close();
	}
	
	public static String getTableName(){
		return Utils.getMyUid(mContext) + CHAT_INFO_TABLE;
	}
	
	public void deleteTable(){
		final String sql = "DROP TABLE IF EXISTS " + getTableName();
        try {
        	mDB.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	public void cleanTable(){
		final String sql = "delete from " + getTableName();
		try {
        	mDB.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	public long insert(ChatInfo data){
		if(data == null){
			return 0;
		}

        ContentValues c = data2ContentValues(data);
        return mDB.insert(getTableName(), null, c);
	}
	
	public boolean delete(String id){
		String whereClause = String.format("%s=%s", CHAT_INFO_ID, id);
		if(mDB.delete(getTableName(), whereClause, null) > 0){
			return true;
		}
		return false;
	}
	
	public Cursor queryAll(){
		Cursor cursor = mDB.query(getTableName(), new String[] {CHAT_INFO_ID, CHAT_INFO_FROM_MSG_NUM, CHAT_INFO_LIKE_BAR_STATUS}, null, null, null, null,
							CHAT_INFO_FROM_MSG_NUM + " DESC");
		
		boolean isEmpty = false;
		if(cursor != null){
			isEmpty = cursor.moveToFirst();
			if(!isEmpty){
				return null;
			}
		}
		return cursor;
	}
	
	public Cursor query(String id){
		String whereClause = String.format("%s=?", CHAT_INFO_ID);
		Cursor cursor = mDB.query(getTableName(), new String[] {CHAT_INFO_ID, CHAT_INFO_FROM_MSG_NUM, CHAT_INFO_LIKE_BAR_STATUS}, whereClause, new String[]{id}, null, null,
						CHAT_INFO_FROM_MSG_NUM + " DESC");
		
		boolean isEmpty = false;
		if(cursor != null){
			isEmpty = cursor.moveToFirst();
			if(!isEmpty){
				return null;
			}
		}
		return cursor;
	}
	
	public boolean update(ChatInfo data){
		if(data == null){
			return false;
		}
        ContentValues c = data2ContentValues(data);
        String whereClause = String.format("%s=?", CHAT_INFO_ID);
        if(mDB.update(getTableName(), c, whereClause, new String[]{data.getDoctorID()}) > 0){
        	return true;
        }
        return false;
	}
	
	public static final ContentValues data2ContentValues( ChatInfo data ) {
		ContentValues c = new ContentValues();
		c.put(CHAT_INFO_ID, null2Blank(data.getDoctorID()));
		c.put(CHAT_INFO_FROM_MSG_NUM, data.getFromMsgNum());
		c.put(CHAT_INFO_LIKE_BAR_STATUS, data.getLikeBarStatus());
		
		return c;
	}
 
	public static final ChatInfo cursor2ChatInfo(Cursor c){
		if(c == null){
			return null;
		}
		
		ChatInfo ts = new ChatInfo(getStringFromCursor(c, CHAT_INFO_ID),
									getIntFromCursor(c, CHAT_INFO_FROM_MSG_NUM),
									getIntFromCursor(c, CHAT_INFO_LIKE_BAR_STATUS));
		return ts;
	}
	
	public static final String null2Blank(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }
	
	public static final long getLongFromCursor(Cursor c, String colName){
        int colIndex = c.getColumnIndex(colName);
        if (colIndex == -1) {
            return (long) 0;
        }
        
        try {
        	long value = c.getLong(colIndex);
        	return value;
        } catch (NumberFormatException e) {
        }
        return (long) 0;
	}
	
    public static final String getStringFromCursor(Cursor c, String colName) {
        int colIndex = c.getColumnIndex(colName);
        if (colIndex == -1) {
            return "";
        }
        String value = c.getString(colIndex);
        if (value == null) {
            value = "";
        }
        return value;
    }
    
    public static final int getIntFromCursor(Cursor c, String colName) {
        int colIndex = c.getColumnIndex(colName);
        if (colIndex == -1) {
            return 0;
        }
        try {
            int value = c.getInt(colIndex);
            return value;
        } catch (NumberFormatException e) {
        }
        return 0;
    }
    
    public static final float getFloatFromCursor(Cursor c, String colName) {
    	int colIndex = c.getColumnIndex(colName);
        if (colIndex == -1) {
            return 0;
        }
        try {
            float value = c.getFloat(colIndex);
            return value;
        } catch (NumberFormatException e) {
        }
        return 0;
    }
    
    public List<ChatInfo> getChatInfoList() {
        List<ChatInfo> lst = new ArrayList<ChatInfo>();
        Cursor c = queryAll();
        if(c == null){
        	return null;
        }
        c.moveToFirst();
        while (!c.isAfterLast()) {
        	ChatInfo task = cursor2ChatInfo(c);
            lst.add(task);
            c.moveToNext();
        }
        if (c != null) {
            c.close();
        }
        return lst;
        
    }
    
    public ChatInfo getChatInfoBarById(String id) {
        Cursor c = query(id);
        if(c == null){
        	return null;
        }
        c.moveToFirst();

        ChatInfo task = cursor2ChatInfo(c);

        if (c != null) {
            c.close();
        }
        return task;
        
    }    
}
