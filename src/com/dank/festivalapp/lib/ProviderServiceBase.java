package com.dank.festivalapp.lib;

import java.util.Date;
import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public abstract class ProviderServiceBase extends IntentService {
	
	protected DownloadFilesTask downloadFile = new DownloadFilesTask();
	
	public ProviderServiceBase()
	{
		super("DataProviderService");
	}
	
	private Context getSharedContext()
	{
		Context sharedContext = null;
		try {
			sharedContext = this.createPackageContext("com.dank.festivalapp", Context.CONTEXT_INCLUDE_CODE);				
		} catch (Exception e) {
			e.getMessage();
		}   
		return sharedContext;
	}
	
	protected void onHandleIntent(Intent intent) 
	{
		updateNews();
		updateBands();
		updateRunningOrder();
	}

	/**
	 * returns the name of the festival
	 * @return
	 */
	abstract protected String getFestivalName();

	/**
	 * returns a list of all currently available Bands
	 * @return
	 */
	abstract protected List<Band> getBands();

	/**
	 * method to make some band detail actions, e.g. in case band
	 * details are on a seconds url
	 * @param band
	 * @return
	 */
	protected Band getBandDetailed(Band band)
	{
		return band;
	}

	/**
	 * update all available bands, 
	 * download band short list, if band already exists within the cache, drop them, 
	 * otherwise download details and add it to the database
	 */
	private void updateBands()
	{
		List<Band> bands = getBands();
		
		if (bands == null || bands.size() == 0)
			return;
		
		BandsDataSource datasource = new BandsDataSource( getSharedContext() );
		datasource.open();

		// remove already added bands from the list
		int i = 0;
		while (i < bands.size() )
		{
			if ( datasource.existsBand( bands.get(i).getBandname() ) ) 
				bands.remove( i );
			else 
				i++;			
		}

		if ( bands.size() == 0)
			return;
		
		int j = 0;
		// download detail page and parse detail
		for(Band band:bands)
		{			
//			if (j < 3) // TODO Remove for final
				datasource.insertBand( getBandDetailed(band), getFestivalName() );
			
			j++;
		}
	}

	/**
	 * returns a list of all current News for this festival
	 * @return
	 */
	abstract protected List<News> getNewsShort();

	/**
	 * optional extension for news, e.g. if news details are on
	 * an another url  
	 * @param news
	 * @return
	 */
	protected News getNewsDetailed(News news)
	{
		return news;
	}

	/**
	 * update news, catch news (e.g. from an url) an write them 
	 * to the database (if necessary)
	 */
	private void updateNews()
	{
		List<News> newsList = getNewsShort();
		
		if (newsList == null || newsList.size() == 0)
			return;
		
		NewsDataSource datasource = new NewsDataSource( getSharedContext() );
		datasource.open();

		String festivalID = getFestivalName();
				
		int i = 0;
		for (News news : newsList)
		{
//			if (i < 2)
			if ( ! datasource.existsNews(news, festivalID) )
				datasource.insertData( getNewsDetailed( news ), festivalID );
			
			i++;
		}

		datasource.close();
	}

	protected class BandGigTime {
		public BandGigTime() {}
		
		public String bandname;
		public Date beginTime;
		public Date endTime;
		public String stage;
	}
	
	/**
	 * returns a list of the current Running Order
	 * @return
	 */
	abstract protected List<BandGigTime> getRunningOrder();
	
	/**
	 * update the current running order, 
	 * catch running order from e.g. url and add these to the database 
	 */
	private void updateRunningOrder()
	{
		List<BandGigTime> allGigs = getRunningOrder();
		
		if (allGigs == null || allGigs.size() == 0)
			return;
	
		RunningOrderDataSource datasource = new RunningOrderDataSource( getSharedContext() );
		datasource.open();

		for(BandGigTime gig:allGigs)
		{			
			datasource.insertGig(
					gig.bandname, 
					gig.beginTime, 
					gig.endTime,
					gig.stage,
					getFestivalName() );
		}
	}
	
	/**
	 * downloads the band logo to a predefined folder
	 * if the download was successfully returns the bandlogo file name, otherwise returns null
	 * @param logoUrl
	 * @param bandName
	 * @return
	 */
	protected String getBandLogo(String logoUrl, String bandName)
	{
		String relLogoUrlFiletype = logoUrl.substring(logoUrl.lastIndexOf("."));
		String logoFileName = bandName + "_logo_" + getFestivalName() + relLogoUrlFiletype;

		if ( downloadFile.downloadUrlToFile(logoUrl, logoFileName ) )
			return logoFileName;
		
		return null;
	}
	
	/**
	 * downloads the band picture to the predefined folder
	 * if the download was successfully returns the bandfoto file name, otherwise returns null
	 * @param fotoUrl
	 * @param bandName
	 * @return
	 */
	protected String getBandPicture(String fotoUrl, String bandName)
	{
		String fotoUrlFiletype = fotoUrl.substring(fotoUrl.lastIndexOf("."));
		String fotoFileName =  bandName + fotoUrlFiletype;
		
		if ( downloadFile.downloadUrlToFile(fotoUrl, fotoFileName ) )
			return fotoFileName;
		
		return null;
	}
	
	
}