package com.example.wifilocalizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
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
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.TextView;




class ScanComparable implements Comparator<ScanResult> {
	 
    @Override
    public int compare(ScanResult s1, ScanResult s2) {
        return (s1.level>s2.level ? -1 : (s1.level==s2.level ? 0 : 1));
    }
}




public class LocalizePhone extends Activity implements SensorEventListener {
	
	@SuppressLint("NewApi")
	
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	
	/* Constants used to process gyroscope data
	// Create a constant to convert nanoseconds to seconds.
	private static final float NS2S = 1.0f / 1000000000.0f;
	
	private double EPSILON = 0.1;
	*/
	private long timestamp;
	
	
	TextView textView;
	
	
	private WifiManager wifi;
	private Camera camera;
	private CameraPreview mPreview;
	private SensorManager mSensorManager;
	private Sensor linearAccelerometer, rotationSensor;
	
	
	private float[] linearAcceleration = new float[4];
	private float[] globalDeltaRotationVector = {0, 0, 0, 0};
	private float[] globalAcceleration = {0, 0, 0, 0};
	
	private float[] rotationMatrix = new float[16];
	private float[] newRotationVector = new float[3], oldRotationVector = new float[3], deltaRotationVector = new float[4];
	
	private float[] cameraPose = new float[3];
	
	

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_localize_phone);
		
		
		
		camera = getCameraInstance();
		mPreview = new CameraPreview(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        linearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        rotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        
		
		IntentFilter i = new IntentFilter();
		i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(new BroadcastReceiver(){
			
		//long prev = 0;
		
		
		
		@Override
		public void onReceive(Context c, Intent i){
		// Code to execute when SCAN_RESULTS_AVAILABLE_ACTION event occurs
			
			//textView = new TextView(c);
			//textView.setMovementMethod(new ScrollingMovementMethod());
			//textView.setTextSize(16);
			
		
			
			
			//Date d = new Date();
			//long curr = d.getTime();
			JSONObject query, queryCore;
			
			HashMap<String,Integer> macRSSI = new HashMap<String,Integer>();
			HashMap<String, JSONObject> postedData = new HashMap<String, JSONObject>();
			
			
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
					//textView.append("\n\n" + scan.SSID.toString() + " " + scan.BSSID.toString() + " " + macRSSI.get(scan.BSSID.toString()));
				}
				
				
				
				queryCore = new JSONObject(macRSSI);
				postedData.put("fingerprint_data", queryCore);
				query = new JSONObject(postedData);
				
				
				new QueryTask("http://10.10.65.58:8000/wifi/add_fingerprint", query).execute(c);

				
				
				/*
				textView.setText("\nQuery sent to the server!\n" + textView.getText());
				
				
				textView.setText("\nCurrent System Timestamp: " + d.getTime() + " " 
				+ "\nNumber of Access Points detected: " + macRSSI.size() + "\n\nSignature (Ordered by RSSI values from strongest to weakest): "
						+ textView.getText());*/
			
				}
			
			/*
			if (prev != 0)
				textView.setText("Hey! Scan results are now available!\n" + "Time spent to finish the scan: " + (curr-prev) + "\n" + textView.getText());
			else
				textView.setText("Hey! Scan results are now available!\n\n" + textView.getText());
				
			prev = curr;
			*/
			
			/*
			textView.setMovementMethod(new ScrollingMovementMethod());
			textView.setTextSize(16);	
			setContentView(textView);
			*/
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
        
        
        mSensorManager.registerListener(this, linearAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        
        
        
        wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()) {			
		
			final Runnable r = new Runnable()
	        {
	            public void run() 
	            {
	            	camera.startPreview();
	                scan();
	                timestamp = System.currentTimeMillis();
	                //camera.takePicture(null, null, mPicture);
	                //camera.startPreview();
	                handler.postDelayed(this, 2000);
	                //camera.release();
	                
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
			Log.d("SCANNING_FAILURE","Wi-Fi is turned off!");
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
	
	
	
	protected void onPause() {
		super.onPause();
		
		mSensorManager.unregisterListener(this, linearAccelerometer);
		mSensorManager.unregisterListener(this, rotationSensor);
	}
	
	
	
	
	
	
	
	
	
	
	/*******    Camera Code     ********/
	
	/** Check if this device has a camera 
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
	}*/
	
	
	
	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    }
	    return c; // returns null if camera is unavailable
	}
	
	
	
	/** A basic Camera preview class */
	public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	    private SurfaceHolder mHolder;
	    private Camera mCamera;

	    public CameraPreview(Context context, Camera camera) {
	        super(context);
	        mCamera = camera;

	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	    }

	    public void surfaceCreated(SurfaceHolder holder) {
	        // The Surface has been created, now tell the camera where to draw the preview.
	    	/*
	        try {
	            mCamera.setPreviewDisplay(holder);
	            mCamera.startPreview();
	        } catch (IOException e) {
	            Log.d("TAG1: ", "Error setting camera preview: " + e.getMessage());
	        }*/
	        mCamera.setDisplayOrientation(90);
	        try {
	        	mCamera.setPreviewDisplay(holder);
	        	mCamera.setPreviewCallback(new PreviewCallback() {

	        		@Override
	        		public void onPreviewFrame(byte[] data, Camera camera) {
	        		}
	        	});

	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) {
	        // empty. Take care of releasing the Camera preview in your activity.
	    }

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	        // If your preview can change or rotate, take care of those events here.
	        // Make sure to stop the preview before resizing or reformatting it.

	        if (mHolder.getSurface() == null){
	          // preview surface does not exist
	          return;
	        }

	        // stop preview before making changes
	        try {
	            mCamera.stopPreview();
	        } catch (Exception e){
	          // ignore: tried to stop a non-existent preview
	        }

	        // set preview size and make any resize, rotate or
	        // reformatting changes here

	        // start preview with new settings
	        try {
	            mCamera.setPreviewDisplay(mHolder);
	            mCamera.startPreview();

	        } catch (Exception e){
	            Log.d("TAG2: ", "Error starting camera preview: " + e.getMessage());
	        }
	    }
	}
	
	
	
	
	@SuppressWarnings("unused")
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {

	        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	        if (pictureFile == null){
	            Log.d("TAG3: ", "Error creating media file, check storage permissions!");
	            return;
	        }

	        try {
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            fos.write(data);
	            fos.close();
	        } catch (FileNotFoundException e) {
	            Log.d("TAG4: ", "File not found: " + e.getMessage());
	        } catch (IOException e) {
	            Log.d("TAG5: ", "Error accessing file: " + e.getMessage());
	        }
	    }
	};
	
	
	
	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "LocalizingImages");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("LocalizingImages", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	
	
	
	
	
	
	
	
	
	/*****    Sensor Code    ****/
	
	public final void onAccuracyChanged(Sensor sensor, int accuracy) {
	    // Do something here if sensor accuracy changes.
	  }
	
	
	
	public void onSensorChanged(SensorEvent event){
		int type = event.sensor.getType();
		
		/*
		if (type == Sensor.TYPE_ACCELEROMETER) {
		
		  // In this example, alpha is calculated as t / (t + dT),
		  // where t is the low-pass filter's time-constant and
		  // dT is the event delivery rate.

		  final float alpha = (float)0.8;

		  // Isolate the force of gravity with the low-pass filter.
		  gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
		  gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
		  gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

		  // Remove the gravity contribution with the high-pass filter.
		  linearAcceleration[0] = event.values[0] - gravity[0];
		  linearAcceleration[1] = event.values[1] - gravity[1];
		  linearAcceleration[2] = event.values[2] - gravity[2];
		  //Log.d("Acceleration", linearAcceleration[0] + " " + linearAcceleration[1] + " " + linearAcceleration[2]);
		}*/
		
		
		
		
		if (type == Sensor.TYPE_LINEAR_ACCELERATION) {
			linearAcceleration[0] = event.values[0];
			linearAcceleration[1] = event.values[1];
			linearAcceleration[2] = event.values[2];
			linearAcceleration[3] = 0;
		}	
	   
		
	   else if (type == Sensor.TYPE_ROTATION_VECTOR) {
		   long currTimestamp = System.currentTimeMillis(); 
		   float[] orientation = new float[3];
		   
		   newRotationVector = event.values.clone();
		   if (oldRotationVector != null) {
			   deltaRotationVector[0] = newRotationVector[0] - oldRotationVector[0];
			   deltaRotationVector[1] = newRotationVector[1] - oldRotationVector[1];
			   deltaRotationVector[2] = newRotationVector[2] - oldRotationVector[2];
			   deltaRotationVector[3] = 0;
			   //Log.d("DELTA_ROTATION", deltaRotationVector[0] + " " + deltaRotationVector[1] + " " + deltaRotationVector[2]);
		   }
		   SensorManager.getRotationMatrixFromVector(rotationMatrix, newRotationVector);
		   
		   
		   
		   if (currTimestamp >= timestamp) {
			   SensorManager.getOrientation(rotationMatrix, orientation);
			   cameraPose[0] = (float) Math.round(Math.toDegrees(orientation[2])*1000)/1000;
			   cameraPose[1] = (float) Math.round(Math.toDegrees(orientation[1])*1000)/1000;
			   cameraPose[2] = (float) Math.round(Math.toDegrees(orientation[0])*1000)/1000;
			   //Log.d("ORIENTATION", cameraPose[0]+" "+cameraPose[1]+" "+cameraPose[2]);
		   }
		   
		   
		   
		   for (int i=0;i<4;i++) {
			   globalDeltaRotationVector[i] = 0;
			   globalAcceleration[i] = 0;
           }
           
           for (int i=0;i<4;i++) {
        	   for (int j=0;j<4;j++) {
        		   globalDeltaRotationVector[i] += rotationMatrix[(i+1)*(j+1)-1] * deltaRotationVector[i];
        		   globalAcceleration[i] += rotationMatrix[(i+1)*(j+1)-1] * linearAcceleration[i];
        	   }
           }
           //Log.d("GLOBAL_ACCELERATION", globalAcceleration[0] + " " + globalAcceleration[1] + " " + globalAcceleration[2]);
           oldRotationVector[0] = newRotationVector[0];
           oldRotationVector[1] = newRotationVector[1];
           oldRotationVector[2] = newRotationVector[2];
	   }
		
		
		
		
		
		/*
	   else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
		   magneticField = new float[3];
		   magneticField = event.values.clone();
	   }*/	
		
		/*
	   else if (type == Sensor.TYPE_GYROSCOPE) {
		// This timestep's delta rotation to be multiplied by the current rotation
		   // after computing it from the gyro sample data.
		   if (timestamp != 0) {
		     final float dT = (event.timestamp - timestamp) * NS2S;
		     // Axis of the rotation sample, not normalized yet.
		     float axisX = event.values[0];
		     float axisY = event.values[1];
		     float axisZ = event.values[2];

		     // Calculate the angular speed of the sample
		     float omegaMagnitude = (float)Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

		     // Normalize the rotation vector if it's big enough to get the axis
		     // (that is, EPSILON should represent your maximum allowable margin of error)
		     if (omegaMagnitude > EPSILON) {
		       axisX /= omegaMagnitude;
		       axisY /= omegaMagnitude;
		       axisZ /= omegaMagnitude;
		     }

		     // Integrate around this axis with the angular speed by the timestep
		     // in order to get a delta rotation from this sample over the timestep
		     // We will convert this axis-angle representation of the delta rotation
		     // into a quaternion before turning it into the rotation matrix.
		     float thetaOverTwo = omegaMagnitude * dT / 2.0f;
		     float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
		     float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
		     deltaRotationVector[0] = sinThetaOverTwo * axisX;
		     deltaRotationVector[1] = sinThetaOverTwo * axisY;
		     deltaRotationVector[2] = sinThetaOverTwo * axisZ;
		     deltaRotationVector[3] = cosThetaOverTwo;
		     
		     //Log.d("LOCAL_ROTATION", deltaRotationVector[0] + " " + deltaRotationVector[1] + " " + deltaRotationVector[2]);
		   }
		   timestamp = event.timestamp;
		   //float[] deltaRotationMatrix = new float[9];
		   //SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
			     // User code should concatenate the delta rotation we computed with the current rotation
			     // in order to get the updated rotation.
			     // rotationCurrent = rotationCurrent * deltaRotationMatrix;
	   }*/
	   
		
	
		/*
		if (magneticField != null && !(linearAcceleration[0] == 0.0 && linearAcceleration[1] == 0.0 && linearAcceleration[2] == 0.0)) {
            float[] R = new float[16];
            float[] I = new float[16];
            SensorManager.getRotationMatrix(R, I, linearAcceleration, magneticField);
            
            for (int i=0;i<4;i++) {
            	globalDeltaRotationVector[i] = (float)0.0;
            	globalAcceleration[i] = (float)0.0;
            }
            
            for (int i=0;i<4;i++) {
            	for (int j=0;j<4;j++) {
            	globalDeltaRotationVector[i] += R[(i+1)*(j+1)-1] * deltaRotationVector[i];
            	if (i<3)
            		globalAcceleration[i] += R[(i+1)*(j+1)-1] * linearAcceleration[i];
            	}
            }
            //Log.d("GLOBAL_ROTATION", globalDeltaRotationVector[0] + " " + globalDeltaRotationVector[1] + " " + globalDeltaRotationVector[2]);
            Log.d("GLOBAL_ACCELERATION", globalAcceleration[0] + " " + globalAcceleration[1] + " " + globalAcceleration[2]);
        }*/
	 }
}
