package com.dank.festivalapp.lib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

public class RunningOrderDataSource extends BandsDataSource {

	private static final String STAGES = "stages";
		
	private static final String SID = "sid";
	private static final String NAME = "name";
	private static final String FESTIVAL ="festival_id";
	
	private static final String STAGES_ID = STAGES + "." + SID;
	private static final String STAGES_NAME = STAGES + "." + NAME;
	private static final String STAGES_FESTIVAL = STAGES + "." + FESTIVAL;
	
	private static final String CREATE_STAGES = 
			"CREATE TABLE " + STAGES + " ("
				+ SID 	+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ NAME 		+ " TEXT NOT NULL, "
				+ FESTIVAL	+ " TEXT NOT NULL"
			+ ");";
	
	private static final String RUNNING_ORDER = "runorder";
	private static final String START_TIME = "start_time";
	private static final String END_TIME = "end_time";
	
	private static final String CREATE_RUNNING_ORDER = 
			"CREATE TABLE " + RUNNING_ORDER + " ("
					+ START_TIME 	+ " DATETIME NOT NULL, "			
					+ END_TIME		+ " DATETIME NOT NULL, "
					+ BandsDataSource.BAND_ID +  " INTEGER NOT NULL, "
					+ SID 		+ " INTEGER NOT NULL "			
			+ ");";
	
	private static final String RUNNING_ORDER_STAGE_ID = RUNNING_ORDER + "." + SID;
	private static final String RUNNING_ORDER_BAND_ID = RUNNING_ORDER + "." + BandsDataSource.BAND_ID;
	private static final String RUNNING_ORDER_START_TIME = RUNNING_ORDER + "." + START_TIME;
	private static final String RUNNING_ORDER_END_TIME = RUNNING_ORDER + "." + END_TIME;

	
	public RunningOrderDataSource(Context context) {
		super(context);
	}
	
	public void open() throws SQLException {
		super.open();
		
		if ( ! dbHelper.isTableExists(database, STAGES) )			
			database.execSQL(CREATE_STAGES);
		
		if ( ! dbHelper.isTableExists(database, RUNNING_ORDER) )
			database.execSQL(CREATE_RUNNING_ORDER);			
	}
	
	/**
	 * close the data base connection
	 */
	public void close() {
		dbHelper.close();
	}
	
	/**
	 * returns the ID to the given stage, -1 means, the stage does not exists
	 * @return
	 */
	private Integer getStageID(String stage)
	{
		String query = "SELECT " + STAGES_ID
				+ " FROM " + STAGES
				+ " WHERE " + STAGES_NAME + "='" + stage + "'";
		
		 Cursor cursor = database.rawQuery(query, null);
		 
		 if (cursor != null)
			 cursor.moveToFirst();
		 
		 if (cursor.getCount() > 0)
			 return cursor.getInt(0);
		 
		 return -1;
	}
	
	/**
	 * insert a new gig to the database
	 * in case the given stage does not exists, a new entry will be generated with the given name
	 * @param bandID
	 * @param beginTime
	 * @param endTime
	 * @param stage
	 */
	public int insertGig(String bandname, Date beginTime, Date endTime, String stage, String festivalID)
	{
		// get BandID
		int bandID = getBandID(bandname);
		if (bandID == -1)
			return -1;
		
		// get StageID
		int stageID = getStageID(stage);
		if (stageID == -1)
		{ // no such stage, add it to the database
			ContentValues stage_values = new ContentValues();		
			stage_values.put(NAME, stage );
			stage_values.put(FESTIVAL_ID, festivalID);
			database.insert(STAGES, null, stage_values);
			stageID = getStageID(stage);
		}
		
		ContentValues runorderValues = new ContentValues();
		
		runorderValues.put(BAND_ID, bandID);
		runorderValues.put(START_TIME, "" + new java.sql.Timestamp( beginTime.getTime() ) );
		runorderValues.put(END_TIME, "" + new java.sql.Timestamp( endTime.getTime() ) );
		runorderValues.put(SID, stageID );
		
		database.insert(RUNNING_ORDER, null, runorderValues);
		
		return 0;
	}
	
	/**
	 * returns a list of all festival days
	 * @return
	 */
	public List<String> getAllDays(String festivalID)
	{
		Cursor cursor = database.rawQuery(
				"SELECT DISTINCT date(" + RUNNING_ORDER_START_TIME + ") AS A " 
						+ " FROM " 
							+ RUNNING_ORDER + ", "
							+ STAGES
						+ " WHERE " 
							+ STAGES_FESTIVAL + "='" + festivalID + "'"
							+ " AND " + RUNNING_ORDER_STAGE_ID + "=" + STAGES_ID
						+ " ORDER BY A "
				, null);

		cursor.moveToFirst();
	
		ArrayList<String> allDays = new ArrayList<String>(); 
		while (!cursor.isAfterLast()) 
		{	
			allDays.add( cursor.getString(0) );
			cursor.moveToNext();
		}
		
		cursor.close();	
		return allDays;
	}

	/**
	 * returns a list of all stage names
	 * @return
	 */
	public ArrayList<String> getAllStages(String festivalID)
	{
		Cursor cursor = database.rawQuery(
				"SELECT " + STAGES_NAME 
				+ " FROM " + STAGES
				+ " WHERE " + STAGES_FESTIVAL + "='" + festivalID + "'"
				+ " ORDER BY " + STAGES_NAME
				, null);

		cursor.moveToFirst();
	
		ArrayList<String> allStages = new ArrayList<String>(); 
		while (!cursor.isAfterLast()) 
		{	
			allStages.add( cursor.getString(0) );
			cursor.moveToNext();
		}
		
		cursor.close();	
		return allStages;
	}

	/**
	 * returns a list containing all gigs for the given day and the given stage
	 * @param day
	 * @param stageName
	 * @return
	 */
	public ArrayList<Band> getROatDay(String day, String stageName, boolean privateRO, String festivalID){
				
		String query = "SELECT DISTINCT "
				+ RUNNING_ORDER_START_TIME + " AS A, "
				+ RUNNING_ORDER_END_TIME + " AS B, "
				+ RUNNING_ORDER_BAND_ID + ", "
				+ BandsDataSource.BANDS + "." + BandsDataSource.BAND_NAME + ", "
				+ BandsDataSource.BANDS + "." + BandsDataSource.BAND_WATCH + ", "
				+ STAGES_NAME
			+ " FROM " 
				+ RUNNING_ORDER  + ", " 
				+ BandsDataSource.BANDS + ", "
				+ STAGES
			+ " WHERE "
				+ " DATE (" + RUNNING_ORDER_START_TIME + ") = '" + day + "'" 
				+ " AND " + RUNNING_ORDER_BAND_ID + "=" + BandsDataSource.BANDS + "." + BandsDataSource.BAND_ID
				+ " AND " + RUNNING_ORDER_STAGE_ID + "=" + STAGES_ID
				+ " AND " + STAGES_FESTIVAL + "= '" + festivalID + "'";				
		
		if (privateRO)
			query +=  " AND " + BandsDataSource.BAND_WATCH + "=1 ";
		
		if (stageName != null)
			query += " AND " + STAGES_NAME + "='" + stageName + "'";
		
		query += " ORDER BY A";
		
		Cursor cursor = database.rawQuery(query, null);
				
		cursor.moveToFirst();
		
		ArrayList<Band> roDay = new ArrayList<Band>();
		
		while (!cursor.isAfterLast()) 
		{
			Band band = new Band(
					cursor.getInt(2),
					cursor.getString(3),
					cursor.getString(4),
					getFlavorsForBand( cursor.getInt(2) )
					);
			band.setGigTime( cursor.getString(0) , cursor.getString(1) );
			band.setStageName( cursor.getString(5) );
			// get flavors
			
			roDay.add( band );
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();		
		
		return roDay;
	}
	
	public Date getTimeFirstAct(String festivalID)
	{
		Cursor cursor = database.rawQuery(
				"SELECT " + RUNNING_ORDER_START_TIME 
				+ " FROM " + RUNNING_ORDER + ", " + STAGES  
				+ " WHERE "
						+ RUNNING_ORDER_STAGE_ID + "=" + STAGES_ID
						+ " AND " + STAGES_FESTIVAL + "= '" + festivalID + "'"	
				+ " ORDER BY " + RUNNING_ORDER_START_TIME + " LIMIT 1"
				, null);
		
		cursor.moveToFirst();
		Date date = null;
		
		try {
			if (cursor.getCount() > 0)
				date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse( cursor.getString(0) );
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		cursor.close();	
		return date;
	}
	
	/**
	 * return all bands, running at the given time
	 * @param currTime
	 * @return
	 */
	public List<Band> getCurrentRunningBands(Date currTime, String festivalID)
	{
		List<Band> bands = new ArrayList<Band>();
		
		java.sql.Timestamp t = new java.sql.Timestamp( currTime.getTime() );
				
		String query = "SELECT DISTINCT " 
						+ RUNNING_ORDER_BAND_ID + ", " 
						+ RUNNING_ORDER_START_TIME + ", " 
						+ RUNNING_ORDER_END_TIME
					+ " FROM " 
						+ RUNNING_ORDER + ", "
						+ STAGES
					+ " WHERE " 
						+ RUNNING_ORDER_START_TIME + " < '" + t.toString() + "'" 
						+ " AND " + RUNNING_ORDER_END_TIME + " > '" + t.toString() + "'"
						+ " AND " + RUNNING_ORDER_STAGE_ID + "=" + STAGES_ID 
						+ " AND " + STAGES_FESTIVAL + "= '" + festivalID + "'";
			
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();
			
		while (!cursor.isAfterLast()) 
		{
			Band band = getBandInfos( cursor.getInt(0) );
			band.setGigTime(cursor.getString(1), cursor.getString(2));

			bands.add(band);
			cursor.moveToNext();
		}				
		
		return bands;
	}
	
	
	/** 
	 * returns the next playing bands, limited to limit
	 * @param currTime
	 * @return
	 */
	public List<Band> getNextRunningBands(Date currTime, int limit, String festivalID)
	{				
		java.sql.Timestamp t = new java.sql.Timestamp( currTime.getTime() );
				
		String query = "SELECT DISTINCT " 
						+ RUNNING_ORDER_BAND_ID + ", " 
						+ RUNNING_ORDER_START_TIME + ", " 
						+ RUNNING_ORDER_END_TIME + ", "
						+ " ( strftime('%s', " + RUNNING_ORDER_START_TIME + ") - strftime('%s','" + t.toString() + "') ) AS A " 
					+ " FROM " 
						+ RUNNING_ORDER + ", "
						+ STAGES
					+ " WHERE " 
						+ RUNNING_ORDER_START_TIME + " > '" + t.toString() + "'"
						+ " AND " + RUNNING_ORDER_STAGE_ID + "=" + STAGES_ID 
						+ " AND " + STAGES_FESTIVAL + "= '" + festivalID + "'"
					+ " ORDER BY A LIMIT " + limit;
		
		Cursor cursor = database.rawQuery(query, null);
		
		List<Band> bands = new ArrayList<Band>();
		
		if (cursor.getCount() == 0)
			return bands;
		
		cursor.moveToFirst();
			
		while (!cursor.isAfterLast()) 
		{
			Band band = getBandInfos( cursor.getInt(0) );
			band.setGigTime(cursor.getString(1), cursor.getString(2));

			bands.add(band);
			cursor.moveToNext();
		}				
		
		return bands;
	}

	
	public List<Band> getNextWatchRunningBandWithinTimeInterval(long currTime, long intervalMin, String festivalID)
	{
		List<Band> bands = new ArrayList<Band>();
				
		java.sql.Timestamp t = new java.sql.Timestamp( currTime );
		java.sql.Timestamp maxTime = new java.sql.Timestamp(currTime + intervalMin * 60 * 1000);
		
		Log.w("", "" + currTime + " " + intervalMin + " " + maxTime.getTime() + " " + t.getTime() );
		
		String query = "SELECT DISTINCT " 
						+ BANDS + "." + BAND_ID + ", "						
						+ RUNNING_ORDER_START_TIME + ", " 
						+ RUNNING_ORDER_END_TIME + ", "
						+ STAGES_NAME + ", "
						+ " ( strftime('%s', " + RUNNING_ORDER_START_TIME + ") - strftime('%s','" + t.toString() + "') ) AS A "
					+ " FROM " 
						+ RUNNING_ORDER + ", " 
						+ BANDS + ", "
						+ STAGES
					+ " WHERE " 
						+ RUNNING_ORDER_START_TIME + " > '" + t.toString() + "'"
						+ " AND " + RUNNING_ORDER_START_TIME + " < '" + maxTime.toString() + "'"
						+ " AND " + BANDS + "." + BAND_ID + "=" + RUNNING_ORDER_BAND_ID 
						+ " AND " + STAGES_ID + "=" + RUNNING_ORDER_STAGE_ID 
						+ " AND " + BAND_WATCH + "= 1 "
						+ " AND " + RUNNING_ORDER_STAGE_ID + "=" + STAGES_ID 
						+ " AND " + STAGES_FESTIVAL + "= '" + festivalID + "'"
						+ " ORDER BY A ";

//		Log.d("", query);

		Cursor cursor = database.rawQuery(query, null);
				
		if (cursor.getCount() == 0)
			return bands;
		
		cursor.moveToFirst();
			
		while (!cursor.isAfterLast()) 
		{
			Band band = getBandInfos( cursor.getInt(0) );
			band.setGigTime(cursor.getString(1), cursor.getString(2));
			band.setStageName(cursor.getString(3));
			
			bands.add(band);
			cursor.moveToNext();
		}				
		
		return bands;
	}
	
}
