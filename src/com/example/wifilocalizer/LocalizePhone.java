package com.example.wifilocalizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;





class ScanComparable implements Comparator<ScanResult> {
	 
    @Override
    public int compare(ScanResult s1, ScanResult s2) {
        return (s1.level>s2.level ? -1 : (s1.level==s2.level ? 0 : 1));
    }
}




public class LocalizePhone extends Activity {
	
	@SuppressLint("NewApi")
	
	TextView textView;
	WifiManager wifi;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
			
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_localize_phone);
		
		
		
		IntentFilter i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(new BroadcastReceiver(){
			
		long prev = 0;
		
		@Override
		public void onReceive(Context c, Intent i){
		// Code to execute when SCAN_RESULTS_AVAILABLE_ACTION event occurs
			
			Date d = new Date();
			long curr = d.getTime();
			JSONObject query, queryCore;
			
			HashMap<String,Integer> macRSSI = new HashMap<String,Integer>();
			HashMap<String, JSONObject> postedData = new HashMap<String, JSONObject>();
			
			textView = new TextView(c);
			textView.setMovementMethod(new ScrollingMovementMethod());
			textView.setTextSize(16);
			
			List<ScanResult> scanResults = wifi.getScanResults();
			
			if(scanResults == null || scanResults.isEmpty()) {
				textView.setText("No wifi network detected!");
				setContentView(textView);
			}
			else {
			
				Collections.sort(scanResults, new ScanComparable());
			
				for (ScanResult scan : scanResults) {
					int linearLevel = WifiManager.calculateSignalLevel(scan.level, 99);
					
					macRSSI.put(scan.BSSID.toString(), scan.level*100-linearLevel);
					textView.append("\n\n" + scan.SSID.toString() + " " + scan.BSSID.toString() + " " + macRSSI.get(scan.BSSID.toString()));
				}
				
				
				
				queryCore = new JSONObject(macRSSI);
				postedData.put("fingerprint_data", queryCore);
				query = new JSONObject(postedData);
				
				
				new QueryTask("http://10.10.65.146:8000/wifi/add_fingerprint", query).execute(c);

				
				textView.setText("\nQuery sent to the server!\n" + textView.getText());
				
				
				textView.setText("\nCurrent System Timestamp: " + d.getTime() + " " 
				+ "\nNumber of Access Points detected: " + macRSSI.size() + "\n\nSignature (Ordered by RSSI values from strongest to weakest): "
						+ textView.getText());
			
				}
			
			
			if (prev != 0)
				textView.setText("Hey! Scan results are now available!\n" + "Time spent to finish the scan: " + (curr-prev) + "\n" + textView.getText());
			else
				textView.setText("Hey! Scan results are now available!\n\n" + textView.getText());
				
			prev = curr;
			
			textView.setMovementMethod(new ScrollingMovementMethod());
			textView.setTextSize(16);	
			setContentView(textView);
			}
		}
	,i);
	    	
			
			
		// Show the Up button in the action bar.
		setupActionBar();
	}

	
	
	
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.localize_phone, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	Handler handler=new Handler();
	@Override
    protected void onResume() {
        super.onResume();
        
        
        wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()) {		
			
			
			final Runnable r = new Runnable()
	        {
	            public void run() 
	            {
	                scan();
	                handler.postDelayed(this, 2000);
	            }
	        };

	        handler.postDelayed(r, 0);
			
			
		}
		else {
			textView = new TextView(this);
			textView.setTextSize(17);
			textView.setText("Your wifi is currently turned off. To find out your location in the building, turn on wifi and then try again.");
			setContentView(textView);
		} 
	}
	
	
	
	
	public void scan() {
		
		if (wifi.startScan()) { }
		else {
			Log.d("SCANNING_FAILURE","Wifi is turned off!");
		}
	}
	
	

	
	
	private class QueryTask extends AsyncTask<Context, Void, Void> 
    {
        private String url_str;
        private JSONObject json;

        public QueryTask(String url, JSONObject json)
        {
            this.url_str = url;
            this.json = json;
        }
        
        
        protected Void doInBackground(Context... c) {
			byte[] data = json.toString().getBytes();
		
			try {
				
				URL url = new URL(url_str);
		
				
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				   try {
					 urlConnection.setReadTimeout( 10000 /*milliseconds*/ );
					 urlConnection.setConnectTimeout( 15000 /* milliseconds */ );
					   
					 urlConnection.setDoInput(true);
				     urlConnection.setDoOutput(true);
				     urlConnection.setFixedLengthStreamingMode(data.length);
				     
				     urlConnection.setRequestProperty("content-type","application/json; charset=utf-8");
				     urlConnection.setRequestProperty("Accept", "application/json");
				     urlConnection.setRequestMethod("POST");

				     urlConnection.connect();
				     OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
			
				     out.write(data);
				     Log.d("DATA", json.toString());
				     out.flush();
				     
				     
				     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				     readStream(in);
				     
				     in.close();
				     out.close();
				   }
				    finally {
				     urlConnection.disconnect();
				    }
				  
				Log.d("URLEXCEPTION","SUCCESS!");
			}
			catch (MalformedURLException e){ }
			catch (IOException e) {Log.d("URLEXCEPTION","FAILURE!"+ e.getMessage()); }
			
			return null;
        }
        
        
        private String readStream(InputStream is) {
            try {
              ByteArrayOutputStream bo = new ByteArrayOutputStream();
              int i = is.read();
              while(i != -1) {
                bo.write(i);
                i = is.read();
              }
              return bo.toString();
            } catch (IOException e) {
            	
              Log.d("readStreamException", "Read Stream failed!!");
              return "";
         
            }
        }
    }
	
}
