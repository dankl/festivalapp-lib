package com.dank.festivalapp.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.util.Log;

public class FlavorProvider {
	
	private DownloadFilesTask downloadFile = new DownloadFilesTask();
	
	private String bandNameToWikipedianame(String name)
	{
		String res = "";

		String[] words = name.split("\\s");
		for (int i = 0; i < words.length; i++) 
		{
			if (i > 0) res += "_";  
			res += words[i].substring(0,1).toUpperCase() + words[i].substring(1).toLowerCase();        	
		}

		return res;
	}

	
	private List<String> getFromWikipedia(String bandname)
	{
		String page = "http://www.metal-archives.com/bands/" + bandNameToWikipedianame( bandname );
		Document doc = Jsoup.parse(page, "UTF-8");
		
		return new ArrayList<String>();

	}
	
	/**
	 * normalizes short version of flavors 
	 * (e.g. "Trash / Black Metal" to Trash Metal, Black Metal)
	 */
	private List<String> normalizeFlavor(String in)
	{
		List<String> res = new ArrayList<String>();

		if (in.contains("/"))
		{
			List<String> items = Arrays.asList(in.split("\\s*/\\s*"));

			String lastItem = items.get(items.size()-1).trim();
			String base = "";
			if (lastItem.contains(" "))
			{
				base = items.get(items.size()-1);				
				base = base.substring(base.indexOf(" ")).trim();
			}
			
			for (int i = 0; i < items.size()-1; i++)
				if(base.length() > 0)
					res.add( items.get(i).trim() + " " + base );
				else 
					res.add( items.get(i).trim() );
	
			res.add( lastItem );
		} else
		{
			res.add(in);
		}

		return res;
	}
	
	/**
	 * EncyclopediaMetallum contains sometimes comments like "(early)" or "(later)"
	 * @param flavor
	 * @return
	 */
	private String cleanUpEncyclopediaMetallumComments(String flavor)
	{
		if (flavor.indexOf("(") != -1)
			flavor = flavor.substring(0, flavor.indexOf("("));
		return flavor.trim();
	}
	
	/**
	 * download genre infos from encyclopedia metallum
	 */
	private List<String> getFromEncyclopediaMetallum(String bandname)
	{
		ArrayList<String> flavors = new ArrayList<String>();
		
		String url = "http://www.metal-archives.com/bands/" + bandNameToWikipedianame( bandname );
		Log.w("getFromEncyclopediaMetallum", url);
		
		String page = downloadFile.downloadUrl(url);
		Document doc = Jsoup.parse(page, "UTF-8");
		
		Element e = doc.getElementsByAttributeValueContaining("id","band_stats").first();
		
		if (e != null)
		{	// found good band page
			Element e1 = e.getElementsByClass("float_right").first().getElementsByTag("dd").first();

			if (e1 != null)
			{
				for(String flavor : normalizeFlavor( e1.text() ) )
					flavors.add( cleanUpEncyclopediaMetallumComments(flavor));
			}
		}		
		return flavors;
	}
	
	
	/**
	 * returns a list of flavors for the defined band,
	 * search at encyclopedia metallum
	 * @param bandname
	 * @return
	 */
	public List<String> getFlavors(String bandname)
	{			
		List<String> flavors = getFromEncyclopediaMetallum(bandname);
		
		return flavors;
	}
	
}
