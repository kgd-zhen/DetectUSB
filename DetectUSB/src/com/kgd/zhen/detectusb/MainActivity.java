package com.kgd.zhen.detectusb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public class DetactUSB extends BroadcastReceiver {
		 private static final String TAG = "DetectUSB"; 
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase( "android.intent.action.UMS_CONNECTED")) {
				 showToast("android.intent.action.UMS_CONNECTED");
			}
			 
			if (intent.getAction().equalsIgnoreCase( "android.intent.action.UMS_DISCONNECTED")) {
				TextView textView = new TextView(context); 
                textView.setBackgroundColor(Color.MAGENTA); 
                textView.setTextColor(Color.BLUE); 
                textView.setPadding(10,10,10,10); 
                textView.setText("USB Disconnected¡­¡­¡­."); 
                Toast toastView = new Toast(context); 
                toastView.setDuration(Toast.LENGTH_LONG); 
                toastView.setGravity(Gravity.CENTER, 0,0); 
                toastView.setView(textView); 
                toastView.show(); 
			}
		}
		
	}
	
	public void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}
}
