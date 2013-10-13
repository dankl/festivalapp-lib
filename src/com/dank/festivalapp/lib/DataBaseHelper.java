package com.dank.festivalapp.lib;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper{
    
	//The Android's default system path of your application database.

	private static final String DATABASE_NAME = "FestivalApp.db";
	private static final int DATABASE_VERSION = 1;
  
    private static DataBaseHelper mInstance = null;
	
	private DataBaseHelper(Context context) 
	{	
		super(context, DATABASE_NAME, null, DATABASE_VERSION);	
	}
	
	public static DataBaseHelper getInstance(Context ctx) {
		/** 
		 * use the application context as suggested by CommonsWare.
		 * this will ensure that you dont accidentally leak an Activitys
		 * context (see this article for more information: 
		 * http://developer.android.com/resources/articles/avoiding-memory-leaks.html)
		 */
		if (mInstance == null) {		
			mInstance = new DataBaseHelper(ctx);			
		}
		return mInstance;
	}

	
	public boolean isTableExists(SQLiteDatabase db, String tableName) 
	{
	    Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+ tableName +"'", null);
	    if(cursor != null) {
	        if(cursor.getCount() > 0) {
	        	cursor.close();
	            return true;
	        }
	        cursor.close();
	    }
	    return false;
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) 
	{		
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {		
		    Log.w(DataBaseHelper.class.getName(), "TODO ");
	}

}
