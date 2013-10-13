package com.dank.festivalapp.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.dank.festivalapp.lib.DataBaseHelper;

public class BandsDataSource {
		
	public static final String FLAVORS = "flavors";
	private static final String FLAVOR_ID = "fid";
	private static final String FLAVOR_NAME = "name";
	private static final String FLAVOR_NAME_NORM = "name_upper_case";
	
	private static final String FLAVORS_CREATE = "CREATE TABLE " + FLAVORS + " ("
			+ FLAVOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FLAVOR_NAME + " TEXT NOT NULL, "
			+ FLAVOR_NAME_NORM + " TEXT NOT NULL "
			+");";
	
	protected static final String BANDS = "bands";
	protected static final String BAND_ID = "bid";
	protected static final String BAND_NAME = "name";
	private static final String BAND_DESC = "desc";
	private static final String BAND_ADDED = "added";
	protected static final String BAND_WATCH = "watch";
	private static final String BAND_LOGO_FILE = "logo_file";
	private static final String BAND_FOTO_FILE = "foto_file";
	private static final String BAND_URL = "url";
	public static final String FESTIVAL_ID ="festival_id";
	
	private static final String BANDS_CREATE = "CREATE TABLE " + BANDS + " ("
			+ BAND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ BAND_NAME + " TEXT NOT NULL, "
			+ BAND_DESC + " TEXT, "
			+ BAND_ADDED + " DATETIME, "
			+ BAND_WATCH + " INTEGER NOT NULL, "
			+ BAND_LOGO_FILE + " TEXT, "
			+ BAND_FOTO_FILE + " TEXT, "
			+ BAND_URL + " TEXT, "
			+ FESTIVAL_ID + " TEXT NOT NULL"
			+ " );";
	
	private static final String BAND_FLAVORS = "band_flavors";
	
	// TODO: cascade delete not supported by sqlite
	private static final String BAND_FLAVORS_CREATE = "CREATE TABLE " + BAND_FLAVORS + " ("
			+ BAND_ID + " INTEGER NOT NULL, "
			+ FLAVOR_ID + " INTEGER NOT NULL,"
			+ "FOREIGN KEY(" + BAND_ID +") REFERENCES " + BANDS + "(" + BAND_ID +"), "
			+ "FOREIGN KEY(" + FLAVOR_ID +") REFERENCES " + FLAVORS + "(" + FLAVOR_ID +") );";
	
	
	protected SQLiteDatabase database;
	protected DataBaseHelper dbHelper;

	public BandsDataSource(Context context) {
		dbHelper = DataBaseHelper.getInstance(context);		
	}
	
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		
		if ( !dbHelper.isTableExists(database, FLAVORS) )
			database.execSQL(FLAVORS_CREATE);
		
		if ( !dbHelper.isTableExists(database, BANDS) )
			database.execSQL(BANDS_CREATE);
		
		if ( !dbHelper.isTableExists(database, BAND_FLAVORS) )
			database.execSQL(BAND_FLAVORS_CREATE);		
	}
	
	
	/**
	 * returns the ID to the given bandname, -1 means, this band does not exists
	 * @return
	 */
	public Integer getBandID(String bandname)
	{
		String query = "SELECT " + BAND_ID 
				+ " FROM " + BANDS 
				+ " WHERE " + BAND_NAME + "='" + bandname + "'";
		
		 Cursor cursor = database.rawQuery(query, null);
		 
		 if (cursor != null)
			 cursor.moveToFirst();
		 
		 if (cursor.getCount() > 0)
			 return cursor.getInt(0);
		 
		 return -1;
	}
	
	public Integer getFlavorID(String flavor)
	{
		String query = "SELECT " + FLAVOR_ID 
					+ " FROM " + FLAVORS 
					+ " WHERE " + FLAVOR_NAME_NORM + "='" + flavor.toUpperCase(Locale.GERMAN) + "'";
	
		Cursor cursor = database.rawQuery(query, null);
	
		if (cursor != null)
			cursor.moveToFirst();
	
		if (cursor.getCount() > 0)
			return cursor.getInt(0);
	
		return -1;
	}
	
	/**
	 * insert a new band to the database
	 * @param band
	 */
	public void insertBand(Band band, String festivalID)
	{
		// insert band infos
		ContentValues band_values = new ContentValues();			
		band_values.put(BAND_NAME, band.getBandname());
		band_values.put(BAND_DESC, band.getDescription() );
		band_values.put(FESTIVAL_ID, festivalID );
		
		if (band.getDate() != null)
			band_values.put(BAND_ADDED, "" + new java.sql.Timestamp( band.getDate().getTime() ) );
					
		band_values.put(BAND_WATCH, 0 );
		band_values.put(BAND_LOGO_FILE, band.getLogoFile() );
		band_values.put(BAND_FOTO_FILE, band.getFotoFile() );
				
		if (band.getUrl() != null)
			band_values.put(BAND_URL, band.getUrl() );
		
		database.insert(BANDS, null, band_values);
		
		band.setID( getBandID( band.getBandname() ) );
				
		// insert flavors
		for (String flavor:band.getFlavors())
		{ // TODO trim flavor
			
			int flavorID = getFlavorID(flavor);

			// insert new flavor
			if (flavorID == -1)
			{ 
				ContentValues flavor_values = new ContentValues();
				flavor_values.put(FLAVOR_NAME, flavor );
				flavor_values.put(FLAVOR_NAME_NORM, flavor.toUpperCase(Locale.GERMAN) );
				database.insert(FLAVORS, null, flavor_values);
				flavorID = getFlavorID(flavor);				
			}
			
			ContentValues band_flavor_values = new ContentValues();			
			band_flavor_values.put(BAND_ID, band.getID() );
			band_flavor_values.put(FLAVOR_ID, flavorID );
			database.insert(BAND_FLAVORS, null, band_flavor_values);		
		}		
	}
	
	/**
	 * get a list of all flavors for the band with the given BandID 
	 * @param id
	 * @return
	 */
	public List<String> getFlavorsForBand(int id)
	{
		Cursor cursor = database.rawQuery(
				"SELECT " + FLAVOR_NAME + 
				" FROM " + BAND_FLAVORS + ", " + FLAVORS +  
				" WHERE " + BAND_FLAVORS + "." + BAND_ID + "=" + id + 
				" AND " + BAND_FLAVORS + "." + FLAVOR_ID + "=" + FLAVORS + "." + FLAVOR_ID
				, null);

		cursor.moveToFirst();
				
		List<String> flavors = new ArrayList<String>();
		while (!cursor.isAfterLast()) 
		{
			flavors.add( cursor.getString(0) );
			cursor.moveToNext();
		}
		cursor.close();
		return flavors;
	}

	/**
	 * get a list of all flavors 
	 * @param id
	 * @return
	 */
	public ArrayList<String> getAllFlavors(String festivalID)
	{
		// select distinct flavors.name  from bands, band_flavors, flavors where festival_id='PartySan 2014' 
		// and bands.bid=band_flavors.bid and band_flavors.fid=flavors.fid;
		
		Cursor cursor = database.rawQuery(
				  "SELECT DISTINCT " + FLAVORS + "." + FLAVOR_NAME 
				+ " FROM " 
						+ FLAVORS + ", "
						+ BANDS + ", "
						+ BAND_FLAVORS
				+ " WHERE "
					+ FESTIVAL_ID + "='" + festivalID + "'"
					+ " AND " + BANDS + "." + BAND_ID + "=" + BAND_FLAVORS + "." + BAND_ID
					+ " AND " + BAND_FLAVORS + "." + FLAVOR_ID + "=" + FLAVORS + "." + FLAVOR_ID
				+ " ORDER BY " + FLAVORS + "." + FLAVOR_NAME 
				, null);

		cursor.moveToFirst();
				
		ArrayList<String> flavors = new ArrayList<String>();
		while (!cursor.isAfterLast()) 
		{
			flavors.add( cursor.getString(0) );
			cursor.moveToNext();
		}
		cursor.close();
		return flavors;
	}
	
	
	/**
	 * returns a list of all bands, band contains the id, the name, the description and flavors
	 * @param festivalID
	 * @return
	 */
	
	public ArrayList<Band> getAllBandsWithFlavors(String festivalID)
	{	
		String query = "SELECT DISTINCT * FROM " + BANDS;
		
		if (festivalID != null && festivalID.length() > 0)
				query += " WHERE " + FESTIVAL_ID + " = '" + festivalID + "'";
		
		query += " ORDER BY " + BAND_NAME ;
		
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();
		
		ArrayList<Band> allBands = new ArrayList<Band>();
		
		while (!cursor.isAfterLast()) 
		{
			Band band = new Band(
					cursor.getInt(0),
					cursor.getString(1),
					cursor.getString(4),
					getFlavorsForBand( cursor.getInt(0) ));
			
			allBands.add(band);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();		
		
		return allBands;
	}
	
	/**
	 * returns a Pair containing the previous and the next ID to the given ID
	 * @param id
	 * @return Pair<prevID, nextID>
	 */
	public Pair<Integer, Integer> getNeighbourBandIDs(int id)
	{
		// TODO: is there a single query solution?
		String query = "SELECT " + BAND_ID + ", " + BAND_NAME +
				" FROM " + BANDS + 
				" WHERE " + BAND_ID + " ORDER BY " + BAND_NAME ;
		
		Cursor cursor = database.rawQuery(query, null);
				
		if (cursor != null)
			cursor.moveToFirst();

		int prevID = -1;
		int nextID = -1;
		
		while(! cursor.isAfterLast())
		{
			if (cursor.getInt(0) == id )				
			{
				cursor.moveToNext();
				if ( ! cursor.isAfterLast())
						nextID = cursor.getInt(0);
				break;
			}
			
			prevID = cursor.getInt(0);
			cursor.moveToNext();
		}
				
		return new Pair<Integer, Integer>(prevID, nextID);
	}
	
	
	private int getNeighbourBandID(int id, Boolean prev)
	{
		// is there a single query solution
		String query = "SELECT " + BAND_ID + ", " + BAND_NAME
				+ " FROM " + BANDS 
				+ " WHERE " 
					+ FESTIVAL_ID + "= ("
						+ " SELECT " + FESTIVAL_ID + " FROM " + BANDS 
						+ " WHERE " + BAND_ID + "=" + id
					+ ")"
				+ " ORDER BY " + BAND_NAME ;
		
		Cursor cursor = database.rawQuery(query, null);
		
		if (cursor.getCount() < 1)
			return -1;
		
		cursor.moveToFirst();

		int prevID = -1;
		
		while(! cursor.isAfterLast())
		{
			if (cursor.getInt(0) == id )				
			{
				if (prev)
					return prevID;
				else 
				{
					cursor.moveToNext();
					if (cursor.isAfterLast())
						return -1;
					return cursor.getInt(0);
				}
			}
			
			prevID = cursor.getInt(0);
			cursor.moveToNext();
		}
				
		return -1;
	}
	
	
	public int getPrevBandID(int id)
	{		
		return getNeighbourBandID(id, true);
	}
	
	public int getNextBandID(int id)
	{		
		return getNeighbourBandID(id, false);
	}
	
	/**
	 * returns all available infos to a band
	 * @param id
	 * @return
	 */
	public Band getBandInfos(int id)
	{				
		Cursor cursor = database.query(BANDS,
				null, BAND_ID + "=" + id, null, null, null, null);
		
		if (cursor != null)
			cursor.moveToFirst();
		
		Band band = new Band(
				cursor.getInt(0),
				cursor.getString(1),
				cursor.getString(4),
				getFlavorsForBand( cursor.getInt(0) ));
		
		band.setDescription(cursor.getString(2));
		band.setLogoFile( cursor.getString(5) );
		band.setFotoFile( cursor.getString(6) );
		band.setUrl( cursor.getString(7) );
				
		return band;
	}
	
	public Boolean existsBand(String bandname)
	{
		String query = "SELECT " + BAND_NAME + 
						" FROM " + BANDS +  
						" WHERE " + BAND_NAME  + "= '" + bandname +"'";
		
		Cursor cursor = database.rawQuery(query, null);
		
		if (cursor.getCount() == 0)
			return false;
		
		return true;
	}
	
	/**
	 * changes watch status of the given band from 0 to 1 or 1 to 0, depending on the current state
	 */
	public void updateBandLike(int bandID)
	{
		String[] proj = { BAND_WATCH };
		Cursor cursor = database.query(BANDS, proj, BAND_ID + "=" + bandID, null, null, null, null);

		Log.w(BandsDataSource.class.toString(), "Count:" + cursor.getCount() );
		
		if (cursor.getCount() > 0)
		{			
			cursor.moveToFirst();
			
			int newState = 0;
			
			if (cursor.getInt(0) == 0)
				newState = 1;
			
			Log.w(BandsDataSource.class.toString(), "Value:" + cursor.getInt(0) + " " + newState );
			
			ContentValues values = new ContentValues();
			values.put(BAND_WATCH, newState);
			database.update(BANDS, values, BAND_ID + "=" + bandID, null);
		}	
		cursor.close();
	}
	
	/**
	 * returns a list of all bands with the given flavor for the defined festival
	 * @param flavor
	 * @param festivalID
	 * @return
	 */
	public ArrayList<Band> getAllBandsWithFlavor(String flavor, String festivalID)
	{	
		String query = "SELECT "
				+ BANDS + "." + BAND_ID	
			+ " FROM " 
				+ BANDS  + ", "
				+ BAND_FLAVORS  + ", "
				+ FLAVORS
			+ " WHERE "
				+ FLAVORS + "." + FLAVOR_NAME + "='" + flavor + "'"
				+ " AND " + BANDS + "." + BAND_ID + "=" + BAND_FLAVORS + "." + BAND_ID
				+ " AND " + BAND_FLAVORS + "." + FLAVOR_ID + "=" + FLAVORS + "." + FLAVOR_ID;
		
		if (festivalID != null && festivalID.length() > 0)
			query += " AND " + FESTIVAL_ID + " = '" + festivalID + "'";
					
		Cursor cursor = database.rawQuery(query, null);
						
		ArrayList<Band> bandList = new ArrayList<Band>() ;
		
		cursor.moveToFirst();
	
		if (cursor.getCount() == 0)
			return bandList;
	
		while (!cursor.isAfterLast()) 
		{
			bandList.add( getBandInfos(cursor.getInt(0)) ) ;
			cursor.moveToNext();
		}
		
		cursor.close();		
		
		return bandList;
	}
	
	public int getCountBands()
	{	
		String query = "SELECT COUNT(*) FROM " + BANDS;
		Cursor cursor = database.rawQuery(query, null);
		
		if (cursor.getCount() == 0)
			return 0;
		
		if (cursor != null)
			cursor.moveToFirst();
		
		int res = cursor.getInt(0);
		
		cursor.close();		
		return res;
	}
	
	public void removeBand(String bandname)
	{
		String query = "DELETE FROM " + BANDS + " WHERE " + BAND_NAME + " = '" + bandname + "'";
		database.rawQuery(query, null);		
	}
	
}
