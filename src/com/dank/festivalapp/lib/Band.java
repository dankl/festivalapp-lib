package com.dank.festivalapp.lib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Band { 
	
	private int id;
	private Date add_date;	// Date, when the band was added
	private String name;
	private String desc;
	private String watch;
	private List<String> flavors = new ArrayList<String>();
	private String logoFile;
	private String bandFile;
	private Date startTime;
	private Date endTime; 
	private String stageName;
	private String url;
	
	public Band()
	{}
	
	/**
	 * 
	 * @param bandname
	 */
	public Band(String b)
	{
		this.name = b;
		this.add_date = new Date();
	}
	
	/** 
	 * @param b
	 * @param d
	 */
	public Band(String b, String d)
	{
		this.name = b;
		this.desc = d;
		this.add_date = new Date();
	}
		
	/**
	 * @param add Date
	 * @param bandname
	 * @param description
	 */
	public Band(String d, String s, String msg)
	{
		try {
			this.add_date = new SimpleDateFormat("dd.MM.yyyy").parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.name = s;
		this.desc = msg;
	}
		
	/**
	 * 
	 * @param id
	 * @param bandname
	 * @param watch
	 * @param flavors
	 */
	public Band(int id, String s, String watch, List<String> f)
	{
		this.id = id;
		this.name = s;
		this.watch = watch;
		this.flavors = f;
		this.add_date = new Date();
	}
		
	public void setUrl(String url)
	{
		this.url = url;
	}
	
	public String getUrl()
	{
		return this.url;
	}
	
	public void setGigTime(Date start, Date end)
	{
		this.startTime = start;
		this.endTime = end;
	}
	
	public void setStageName(String stageName)
	{
		this.stageName = stageName;
	}
	
	public String getStageName()
	{
		return this.stageName;
	}
	
	public Date getStartTime()
	{
		return startTime;
	}
	
	public void setGigTime(String start, String end)
	{
		try {
			this.startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse( start );
			this.endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(end);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
		
	public Date getGigStartTime()
	{
		return this.startTime;
	}
	
	public Date getGigEndTime()
	{
		return this.endTime;
	}
		
	public String getShortTimeIntervall()
	{
		SimpleDateFormat ft = new SimpleDateFormat("HH:mm");
		return ft.format( this.startTime ) + "-" + ft.format( this.endTime ); 	
	}
	
	public Boolean watch() {
		if (this.watch.compareTo("1") == 0)
			return true;
					
		return false;
	}
	
	public Integer getID(){
		return this.id;
	}
	
	public void setID(int id){
		this.id = id;
	}
	
	public Date getDate() {
		return this.add_date;
	}

	public void setAddDate(String d) {		
		try {
			this.add_date = new SimpleDateFormat("dd.MM.yyyy").parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void setAddDate(Date date) {
		this.add_date = date;
	}
	
	public void setBandname(String b) {
		this.name = b;
	}

	public String getBandname() {
		return name;
	}

	
	public String getDescription() {
		return desc;
	}

	public void setDescription(String d) {
		this.desc = d;
	}
	
	
	public void addFlavor(String f)
	{
		this.flavors.add(f);
	}
	
	public List<String> getFlavors()
	{
		return this.flavors;
	}
	
	public String getFlavorsAsString()
	{
		if (this.flavors.size() > 0)
			return this.flavors.toString();
		return "";
	}
	
	public void setLogoFile(String f)
	{
		this.logoFile = f;
	}

	public String getLogoFile()
	{
		return this.logoFile;
	}
	
	public void setFotoFile(String f)
	{
		this.bandFile = f;
	}
	
	public String getFotoFile()
	{
		return this.bandFile;
	}
}
