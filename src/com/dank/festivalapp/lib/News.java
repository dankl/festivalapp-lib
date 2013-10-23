package com.dank.festivalapp.lib;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class News implements Comparable<News>{ 
	
	int id;
	Date date;
	String subject;
	String msg;
	String festival;
	
	public News()
	{
		this.date = new Date();
		this.subject = "";
		this.msg = "";
	}
	
	public News(String s, String msg)
	{
		this.date = new Date();
		this.subject = s;
		this.msg = msg;
	}
	
	public News(String d, String s, String msg)
	{
		try {
			this.date = new SimpleDateFormat("yyyy-MM-dd").parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.subject = s;
		this.msg = msg;
	}
	
	public Integer getID(){
		return this.id;
	}
	
	public void setID(int id){
		this.id = id;
	}
	
	public Date getDate() {
		return this.date;
	}
	
	public String getDateAsFormatedString() {
		SimpleDateFormat dateForm = new SimpleDateFormat("dd.MM.yyyy");
		return dateForm.format( date );
	}
		
	public void setDate(String d) {		
		try {
			this.date = new SimpleDateFormat("yyyy-MM-dd").parse(d);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return subject;
	}

	
	public String getMessage() {
		return msg;
	}

	public void setMessage(String msg) {
		this.msg = msg;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return msg;
	}
	
	public void setFestival(String f)
	{
		this.festival = f;
	}
	
	public String getFestival()
	{
		return this.festival;
	}

	@Override
	public int compareTo(News n) {
		if ( this.date.before( n.getDate() ) )
			return -1;
		if ( this.date.after( n.getDate() ) )
			return 1;
		return 0;
	}
}
