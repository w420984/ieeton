package com.ieeton.agency.db;

import java.util.ArrayList;
import java.util.List;

import com.ieeton.agency.models.ServerHostData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ServerHost{
	private final static int Version = 1;
	public final static String    SERVER_HOST_TABLE     		 = "server_host_table";
	
	public final static String    SERVER_HOST_TABLE_ID     		 = "server_host_table_id";
    public final static String    SERVER_HOST_PASSPORT_SERVER     		 = "passport_server";
    public final static String    SERVER_HOST_IMAGE_SERVER        		 = "image_server";
    public final static String    SERVER_HOST_UPLOAD_SERVER           = "upload_server";
    public final static String    SERVER_HOST_CONTENT_SERVER            = "content_server";
    
    public final static String    SERVER_HOST_TABLE_ID_VALUE     		 = "0";
    
    private static class DatabaseHelper extends SQLiteOpenHelper{

    	public DatabaseHelper(Context context){
    		super(context, SERVER_HOST_TABLE, null, Version);
    	}
    	
		@Override
		public void onCreate(SQLiteDatabase db) {
	        try {
	            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
	            sql.append(SERVER_HOST_TABLE).append(" (")
	            		.append(SERVER_HOST_TABLE_ID).append(" TEXT, ")
	            		.append(SERVER_HOST_PASSPORT_SERVER).append(" TEXT, ")
	                    .append(SERVER_HOST_IMAGE_SERVER).append(" TEXT, ")
	                    .append(SERVER_HOST_UPLOAD_SERVER).append(" TEXT, ")
	                    .append(SERVER_HOST_CONTENT_SERVER).append(" TEXT)");
	            db.execSQL(sql.toString());
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }				
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String sql = "DROP TABLE IF EXISTS " + SERVER_HOST_TABLE;
	        db.execSQL(sql);
	        //db.execSQL(sql.toString());
	        
	        onCreate(db);			
		}
    	
    }
    
    private SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mContext;
    
    public ServerHost(Context context){
    	this.mContext = context;
    	mDBHelper = new DatabaseHelper(context);
    }
    
	public ServerHost open() throws SQLException{
		mDB = mDBHelper.getWritableDatabase();
		return this;
	}
	
	public void close(){
		mDBHelper.close();
	}
	
	public void deleteTable(){
		final String sql = "DROP TABLE IF EXISTS " + SERVER_HOST_TABLE;
        try {
        	mDB.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	public void replace(ServerHostData value){
		mDB.replace(SERVER_HOST_TABLE, null, data2ContentValues(value));
	}
	
	public void cleanTable(){
		final String sql = "delete from " + SERVER_HOST_TABLE;
		try {
        	mDB.execSQL(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	public long insert(ServerHostData data){
		if(data == null){
			return 0;
		}

        ContentValues c = data2ContentValues(data);
        return mDB.insert(SERVER_HOST_TABLE, null, c);
	}
	
	public boolean delete(String ServerHostDataId){
		String whereClause = String.format("%s=%s", SERVER_HOST_TABLE_ID, ServerHostDataId);
		if(mDB.delete(SERVER_HOST_TABLE, whereClause, null) > 0){
			return true;
		}
		return false;
	}
	
	public Cursor queryAll(){
		Cursor cursor = mDB.query(SERVER_HOST_TABLE, new String[] {SERVER_HOST_TABLE_ID, SERVER_HOST_PASSPORT_SERVER, SERVER_HOST_IMAGE_SERVER, SERVER_HOST_UPLOAD_SERVER,
							SERVER_HOST_CONTENT_SERVER}, null, null, null, null,
							SERVER_HOST_TABLE_ID + " DESC");
		
		if(cursor != null){
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public Cursor query(String ServerHostDataId){
		String whereClause = String.format("%s=?", SERVER_HOST_TABLE_ID);
		Cursor cursor = mDB.query(SERVER_HOST_TABLE, new String[] {SERVER_HOST_TABLE_ID, SERVER_HOST_PASSPORT_SERVER, SERVER_HOST_IMAGE_SERVER, SERVER_HOST_UPLOAD_SERVER,
				SERVER_HOST_CONTENT_SERVER}, whereClause, new String[]{ServerHostDataId}, null, null, SERVER_HOST_TABLE_ID + " DESC");
		
		if(cursor != null){
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public boolean update(ServerHostData data){
		if(data == null){
			return false;
		}
        ContentValues c = data2ContentValues(data);
        String whereClause = String.format("%s=?", SERVER_HOST_TABLE_ID);
        if(mDB.update(SERVER_HOST_TABLE, c, whereClause, new String[]{SERVER_HOST_TABLE_ID_VALUE}) > 0){
        	return true;
        }
        return false;
	}
	
	public static final ContentValues data2ContentValues( ServerHostData data ) {
		ContentValues c = new ContentValues();
		c.put(SERVER_HOST_TABLE_ID, SERVER_HOST_TABLE_ID_VALUE);
		c.put(SERVER_HOST_PASSPORT_SERVER, null2Blank(data.getPassportServerUrl()));
		c.put(SERVER_HOST_IMAGE_SERVER, null2Blank(data.getImageServerUrl()));
		c.put(SERVER_HOST_UPLOAD_SERVER, null2Blank(data.getUploadServerUrl()));
		c.put(SERVER_HOST_CONTENT_SERVER, null2Blank(data.getContentServerUrl()));
		
		return c;
	}
 
	public static final ServerHostData cursor2ServerHostData(Cursor c){
		if(c == null){
			return null;
		}
		
		ServerHostData ts = new ServerHostData();
		ts.setPassportServerUrl(getStringFromCursor(c, SERVER_HOST_PASSPORT_SERVER));
		ts.setImageServerUrl(getStringFromCursor(c, SERVER_HOST_IMAGE_SERVER));
		ts.setUploadServerUrl(getStringFromCursor(c, SERVER_HOST_UPLOAD_SERVER));
		ts.setContentServerUrl(getStringFromCursor(c, SERVER_HOST_CONTENT_SERVER));

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
    
    public List<ServerHostData> getServerHostDataList() {
        List<ServerHostData> lst = new ArrayList<ServerHostData>();
        Cursor c = queryAll();
        if(c == null){
        	return null;
        }
        c.moveToFirst();
        while (!c.isAfterLast()) {
        	ServerHostData data = cursor2ServerHostData(c);
            lst.add(data);
            c.moveToNext();
        }
        if (c != null) {
            c.close();
        }
        return lst;
        
    }
    
    public ServerHostData getServerHostDataById(String id) {
        Cursor c = query(id);
        if(c == null){
        	return null;
        }
        c.moveToFirst();

        ServerHostData data = cursor2ServerHostData(c);

        if (c != null) {
            c.close();
        }
        return data;
        
    }    
}
