package com.example.wifilocalizer;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


class ScanComparable implements Comparator<ScanResult> {
	 
    @Override
    public int compare(ScanResult s1, ScanResult s2) {
        return (s1.level>s2.level ? 1 : (s1.level==s2.level ? 0 : -1));
    }
}





public class LocalizePhone extends Activity {
	
	@SuppressLint("NewApi")
	
	HashMap<String,Integer> macRSSI = new HashMap<String,Integer>();
	
	//private SensorManager mSensorManager;
	//private Sensor mSensor;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_localize_phone);
	    	
			
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
        
        
        final Runnable r = new Runnable()
        {
            public void run() 
            {
                scan();
                handler.postDelayed(this, 2000);
            }
        };

        handler.postDelayed(r, 100);
        
	}
	
	
	
	public void scan() {
	
		TextView textView = new TextView(this);
		textView.setMovementMethod(new ScrollingMovementMethod());
		textView.setTextSize(16);
		
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()) {
			if (wifi.startScan()) {


			Date d = new Date();
			int totalLevel = 0;
			List<ScanResult> scanResults = wifi.getScanResults();
			
			if(scanResults == null || scanResults.isEmpty()) {
				textView.setText("No wifi network detected!");
				setContentView(textView);
			}
			else {
			
				Collections.sort(scanResults, new ScanComparable());
			
				for (ScanResult scan : scanResults) {
					totalLevel += scan.level;
					macRSSI.put(scan.BSSID.toString(), scan.level);
					textView.append("\n\n" + scan.SSID.toString() + " " + scan.BSSID.toString() + " " + macRSSI.get(scan.BSSID.toString()));
				}
				textView.setText(totalLevel/macRSSI.size() +" " + d.getTime() + "\n" + textView.getText());
			
				//String signature = textView.getText().toString();
			
				//contactServerDatabase(macRSSI);
				//DisplayRetrievedLocation();
				}
			}
			else {
				textView.setText("Scanning failed!");
			}
			setContentView(textView);
			
		}
		else {
			textView.setText("Your wifi is currently turned off. To find out your location in the building, turn on wifi then try again.");
			setContentView(textView);
		}
	}
}
