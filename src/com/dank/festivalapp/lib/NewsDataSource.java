package com.dank.festivalapp.lib;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NewsDataSource {

	// Database fields
	// Database creation sql statement
	// News
	public static final String NEWS = "news";
	public static final String ID = "id";
	public static final String DATE = "date";
	public static final String SUBJECT = "subject";
	public static final String MSG = "msg";
	public static final String FESTIVAL_ID ="festival_id";
	
	public static final String DATABASE_CREATE = "CREATE TABLE " + NEWS + " ("
			+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ DATE + " DATETIME NOT NULL, "
			+ SUBJECT + " TEXT NOT NULL, "			
			+ MSG + " TEXT NOT NULL, "
			+ FESTIVAL_ID + " TEXT NOT NULL" 
			+ ");";
	
	private SQLiteDatabase database;
	private DataBaseHelper dbHelper;

	public NewsDataSource(Context context) {
		dbHelper = DataBaseHelper.getInstance(context);		
	}

	/**
	 * open a connection to the database
	 * @throws SQLException
	 */
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
				
		if ( !dbHelper.isTableExists(database, NEWS) )
			database.execSQL(DATABASE_CREATE);		
	}

	/**
	 * close the data base connection
	 */
	public void close() {
		dbHelper.close();
	}

	
	/**
	 * add a new news data set to the data base
	 * @param n
	 */
	public void insertData(News n, String festivalID)
	{
		ContentValues values = new ContentValues();
				
		values.put(DATE, "" + new java.sql.Timestamp( n.getDate().getTime() ) );
		values.put(SUBJECT, n.getSubject() );
		values.put(MSG, n.getMessage() );
		values.put(FESTIVAL_ID, festivalID );
		
		Log.d("NewsDataSource.insertData", n.getDate().toString()  + n.getSubject() );
		
		database.insert(NEWS, null, values);
	}
	

	/**
	 * returns the news for a defined news id
	 * @param id
	 * @return
	 */
	public News getNews(int id)
	{
		Cursor cursor = database.query(NEWS,
				null, NewsDataSource.ID + " = " + id , null, null, null, null);
		
		if (cursor.getCount() < 1)
			return null;
		
		cursor.moveToFirst();
		News news = cursorToNews(cursor);
		cursor.close();
		return news;
	}
	
	/**
	 * returns a list of all news for the given festivalID
	 * in case of an empty festivalID, all news are returned
	 * @param festivalID
	 * @return
	 */
	public List<News> getAllNews(String festivalID) {
		List<News> allNews = new ArrayList<News>();
		
		String query = "SELECT * FROM " + NEWS;
		
		if (festivalID == null || festivalID.length() > 0)
			query += " WHERE " + NEWS + "." + FESTIVAL_ID + "= '" + festivalID + "'";
		
		query += " ORDER BY " + DATE + " DESC "; 
	
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			News news = cursorToNews(cursor);
			allNews.add(news);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return allNews;
	}
	
	/**
	 * translates the news pointed by the cursor to a news
	 * @param cursor
	 * @return
	 */
	private News cursorToNews(Cursor cursor) {
		News news = new News();
		news.setID(cursor.getInt(0));
		
	    news.setDate(cursor.getString(1));	    
	    news.setSubject(cursor.getString(2));
	    news.setMessage(cursor.getString(3));
	    news.setFestival(cursor.getString(4));
	    return news;
	  }
	
	
	/**
	 * returns the ID of the previous/next news to the given ID
	 * a neighbor news is that one with the minimal time distance to
	 * the given news id
	 * prev defines, which neighbor will be computed, 
	 * 		true -> prev, 
	 * 		false -> next
	 * @param id
	 * @param prev
	 * @return
	 */
	private int getNeighbourNewsID(int id, String festivalID, Boolean prev)
	{
		String query = "SELECT " 
							+ NEWS + "2." + ID + ", "
							+ "( strftime('%s', " + NEWS + "." + DATE + ") - strftime('%s', " + NEWS + "2." + DATE + ") ) AS A"
						+ " FROM "
							+ NEWS + ", "
							+ NEWS + " as " + NEWS + "2"
						+ " WHERE "		
							+ NEWS + "." + ID + "=" + id
							+ " AND " + NEWS + "2." + ID + "!=" + id;
		
		if (festivalID == null || festivalID.length() > 0)
			query += " AND " + NEWS + "2." + FESTIVAL_ID + "= '" + festivalID + "'";
		
		if (!prev) query += " AND A < 0 ORDER BY A DESC ";
		else	  query += " AND A > 0 ORDER BY A ";
		
		query += " LIMIT 1";
		
//		Log.w("", query);
				
		Cursor cursor = database.rawQuery(query, null);
		
		if (cursor.getCount() < 1)
			return -1;
		
		if (cursor != null)
			cursor.moveToFirst();
				
		return cursor.getInt(0);
	}
	
	/**
	 * returns the ID of the previous news to the given ID
	 * a neighbor news is that one with the minimal time distance to
	 * the given news id
	 * @param id
	 * @return
	 */
	public int getPrevNewsID(int id, String festivalID)
	{		
		return getNeighbourNewsID(id, festivalID, true);
	}
	
	/**
	 * returns the ID of the next news to the given ID
	 * a neighbor news is that one with the minimal time distance to
	 * the given news id
	 * @param id
	 * @return
	 */
	public int getNextNewsID(int id, String festivalID)
	{		
		return getNeighbourNewsID(id, festivalID, false);
	}
	
	/**
	 * returns true if a news with this date and subject already exists
	 * otherwise false
	 * @param news
	 * @return
	 */
	public boolean existsNews(News news, String festivalID)
	{		
		String query = "SELECT * FROM " + NEWS 
				+ " WHERE " 
//					+ DATE + "='" + new java.sql.Timestamp( news.getDate().getTime() ) + "'"
//					+ " AND " + 
					+ SUBJECT + "='" +news.getSubject() + "'"
					+ " AND " + NEWS + "." + FESTIVAL_ID + "= '" + festivalID + "'";
		
		Cursor cursor = database.rawQuery(query, null);
		
		if (cursor.getCount() == 0)
			return false;
		
		return true;
	}
	
	
	
	public News getLatestNews(String festivalID)
	{
		String query = "SELECT * FROM " + NEWS 
					+ " WHERE " 
					+ NEWS + "." + FESTIVAL_ID + "= '" + festivalID + "'"
					+ " ORDER BY " + DATE + " DESC "
					+ " LIMIT 1";
		
		Cursor cursor = database.rawQuery(query, null);
		
		if (cursor.getCount() < 1)
			return null;
		
		cursor.moveToFirst();
		
		return cursorToNews(cursor);
	}
	
}
