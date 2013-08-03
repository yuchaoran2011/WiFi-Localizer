package com.example.wifilocalizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;





class ImageQueryTask extends AsyncTask<Context, Void, Void> 
    {
        private String url_str;
        private JSONObject json;

        public ImageQueryTask(String url, JSONObject json)
        {
            this.url_str = url;
            this.json = json;
        }
        
        
        protected Void doInBackground(Context... c) {
        	
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	
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
				     
				     
				     // Parse response sent from Wherelab image localization server
				     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				     BufferedReader br = new BufferedReader(new InputStreamReader(in));
				     
				     
				     StringBuilder builder = new StringBuilder();
				     String line = null;
				     for (; (line = br.readLine()) != null;) {
				         builder.append(line).append("\n");
				     }     
				     
				     
				     JSONTokener tokener = new JSONTokener(builder.toString());
				     JSONObject finalResult = new JSONObject(tokener); 
				   
				     HashMap<String, String> imageResponseMap = new HashMap<String, String>();
				     imageResponseMap.put("location", (Integer.valueOf(finalResult.getInt("local_x")).toString()) + " " +(Integer.valueOf(finalResult.getInt("local_y")).toString()));
				     imageResponseMap.put("confidence", (Double.valueOf(finalResult.getDouble("overall_confidence")).toString()));
				     LocalizePhone.imageResponse = new JSONObject(imageResponseMap);
				     

				     Log.d("Image location", finalResult.getString("local_x") + " " + finalResult.getString("local_y"));
				     Log.d("Image confidence", (Double.valueOf(finalResult.getDouble("overall_confidence")).toString()));
				     
				     
				     
				     in.close();
				     out.close();
				     
				   } catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					    finally {
					     urlConnection.disconnect();
					    }
				Log.d("URL_CONNECTION","SUCCESS!");
			}
			catch (MalformedURLException e){ }
			catch (IOException e) {Log.d("URL_EXCEPTION","FAILURE!"+ e.getMessage()); }
			
			return null;
        }
    }
	
	
	
	
	
	
	class CentralQueryTask extends AsyncTask<Context, Void, Void> 
    {
        private String url_str;
        private JSONObject json;

        public CentralQueryTask(String url, JSONObject json)
        {
            this.url_str = url;
            this.json = json;
        }
        
        
        protected Void doInBackground(Context... c) {
        	
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	
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
				     
				     
				     // Parse response sent from WiFi localization server
				     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				     BufferedReader br = new BufferedReader(new InputStreamReader(in));
				     
				     
				     StringBuilder builder = new StringBuilder();
				     String line = null;
				     for (; (line = br.readLine()) != null;) {
				         builder.append(line).append("\n");
				     }     
				     
				     
				     JSONTokener tokener = new JSONTokener(builder.toString());
				     JSONObject finalResult = new JSONObject(tokener);
				     
				     Log.d("central_x", (Double.valueOf(finalResult.getDouble("x")).toString()));
				     Log.d("central_y", (Double.valueOf(finalResult.getDouble("y")).toString()));
				     		     
				     
				     in.close();
				     out.close();
				   }catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    finally {
				     urlConnection.disconnect();
				    }
				  
				Log.d("URL_CONNECTION","SUCCESS!");
			}
			catch (MalformedURLException e){ }
			catch (IOException e) {Log.d("URL_EXCEPTION","FAILURE!"+ e.getMessage()); }
			
			return null;
        }
    }
	

	

	
	
	class WifiQueryTask extends AsyncTask<Context, Void, Void> 
    {
        private String url_str;
        private JSONObject json;

        public WifiQueryTask(String url, JSONObject json)
        {
            this.url_str = url;
            this.json = json;
        }
        
        
        protected Void doInBackground(Context... c) {
        	
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	
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
				     
				     
				     // Parse response sent from WiFi localization server
				     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				     BufferedReader br = new BufferedReader(new InputStreamReader(in));
				     
				     
				     StringBuilder builder = new StringBuilder();
				     String line = null;
				     for (; (line = br.readLine()) != null;) {
				         builder.append(line).append("\n");
				     }     
				     
				     
				     JSONTokener tokener = new JSONTokener(builder.toString());
				     JSONObject finalResult = new JSONObject(tokener);
				     
				   
				     HashMap<String, String> wifiResponseMap = new HashMap<String, String>();
				     wifiResponseMap.put("status", (Integer.valueOf(finalResult.getInt("status")).toString()));
				     wifiResponseMap.put("location", finalResult.getString("location"));
				     wifiResponseMap.put("confidence", (Double.valueOf(finalResult.getDouble("confidence")).toString()));
				     LocalizePhone.wifiResponse = new JSONObject(wifiResponseMap);
				 
				     
				     HashMap<String, JSONObject> overallMap = new HashMap<String, JSONObject>();
				     overallMap.put("imageResponse", LocalizePhone.imageResponse);
				     overallMap.put("wifiResponse", LocalizePhone.wifiResponse);
				     
				     LocalizePhone.overallResponse = new JSONObject(overallMap);  
				     
				     new CentralQueryTask(LocalizePhone.CENTRAL_DYNAMIC_URL, LocalizePhone.overallResponse).execute(c);
				     
				     Log.d("REQUEST", "Integrated WiFi+Image sent to central server!");
				     
				     
				     
				     in.close();
				     out.close();
				     
				   } catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				    finally {
				     urlConnection.disconnect();
				    }
				  
				Log.d("URL_CONNECTION","SUCCESS!");
			}
			catch (MalformedURLException e){ }
			catch (IOException e) {Log.d("URL_EXCEPTION","FAILURE!"+ e.getMessage()); }
			
			return null;
        }
    }