package com.example.wifilocalizer;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;


public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.example.wifilocalizer.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    } 


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /*
    public void sendMessage(View view) {
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText)findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    }
    */
    
    
    public void localize(View view) {
    	Intent intent = new Intent(this, LocalizePhone.class);
    	startActivity(intent);
    }
    
    
    public void yawTest(View view) {
    	Intent intent = new Intent(this, RotationVectorDemo.class);
    	startActivity(intent);
    }
}
