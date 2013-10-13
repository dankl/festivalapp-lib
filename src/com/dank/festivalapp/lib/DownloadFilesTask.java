package com.dank.festivalapp.lib;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Environment;
import android.util.Log;

/**
 * 
 * @author dank
 *	download one file from the network
 *  be careful: this task could block
 */
public class DownloadFilesTask {

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	public String downloadUrl(String myurl) {

		String page = "";

		try {
			InputStream is = null;
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);

			conn.connect();	        
			is = conn.getInputStream();

			Reader reader = new InputStreamReader( is, "UTF-8");
			char buffer[] = new char[1024];


			while (reader.read(buffer) != -1) {
				String tmp = new String(buffer);
				page += tmp;
			}

			is.close();

			//			Log.w("", "The response is: " + page);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		

		return page;		
	}

	/** 
	 * @param myurl
	 * @param filename
	 * @return true in case of success, otherwise false
	 */
	public Boolean downloadUrlToFile(String myurl, String filename)
	{
		try {
			File root = Environment.getExternalStorageDirectory();
			File dir = new File (root.getAbsolutePath() + "/FestivalApp");
			if (! dir.isDirectory() )
				dir.mkdirs(); 
			
			File targetFile = new File( dir, filename );
			if (targetFile.exists())
			{
				Log.w("downloadUrlToFile", targetFile.toString() + " already exists.");
				return true;
			}
			
			Log.d("downloadUrlToFile", myurl + " to " + targetFile.toString() );
			
			InputStream is = null;
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);

			conn.connect();	        
			is = conn.getInputStream();

			FileOutputStream f = new FileOutputStream( targetFile );

			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = is.read(buffer)) > 0) {
				f.write(buffer, 0, len);
			}
			
			is.close();
			f.close();
			
			return true;

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}

