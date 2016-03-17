package com.kgd.zhen.detectusb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.kgd.zhen.usbdriver.UsbSerialDriver;
import com.kgd.zhen.usbdriver.UsbSerialPort;
import com.kgd.zhen.usbdriver.UsbSerialProber;
import com.kgd.zhen.usbutil.HexDump;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static List<UsbSerialPort> mEntries = new ArrayList<UsbSerialPort>(); // usb
	public static final String TAG = "DetectUSB";
	public static final long REFRESH_TIMEOUT_MILLIS = 5000;
	public static final int MESSAGE_REFRESH = 101;
	UsbManager mUsbManager;
	UsbEndpoint end_in, end_out;
	UsbDeviceConnection connection = null;
	UsbInterface interf = null;
	PendingIntent localPendingIntent = null;
	
	public TextView tv_usb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 获取android USB
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		
		Intent intent = new Intent();
		intent.addCategory("com.android.example.USB_PERMISSION");
		intent.addCategory(Intent.ACTION_MEDIA_MOUNTED);
		intent.addCategory(Intent.ACTION_MEDIA_UNMOUNTED);
		localPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		initView();
	}
	
	public void initView() {
		tv_usb = (TextView) findViewById(R.id.tv_usb);
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
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mHandler.sendEmptyMessage(MESSAGE_REFRESH);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mHandler.removeMessages(MESSAGE_REFRESH);
		if (connection != null) {
			connection.releaseInterface(interf);
			connection.close();
		}
		interf = null;
		connection = null;
	}

	public void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * 获取USB打印刷新列表
	 */
	private void refreshDeviceList() {
		new AsyncTask<Void, Void, List<UsbSerialPort>>() {
			@Override
			protected List<UsbSerialPort> doInBackground(Void... params) {
				Log.d(TAG, "Refreshing device list ...");
				SystemClock.sleep(1000 * 60);

				final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
//				UsbSerialProber.getDefaultProber().mProbeTable.addProduct(0, 0, CdcAcmSerialDriver.class);
				final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
				for (final UsbSerialDriver driver : drivers) {
					final List<UsbSerialPort> ports = driver.getPorts();
					Log.d(TAG, String.format("+ %s: %s port%s", driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
					result.addAll(ports);
				}
				return result;
			}

			@Override
			protected void onPostExecute(List<UsbSerialPort> result) {
				mEntries.clear();
				mEntries.addAll(result);
				tv_usb.setText("");
				for (int i=0; i<mEntries.size(); i++) {
					UsbSerialPort port1 = mEntries.get(i);
					UsbSerialDriver driver1 = port1.getDriver();
					UsbDevice device = driver1.getDevice();
					String title = String.format("Vendor %s Product %s",
							HexDump.toHexString((short) device.getVendorId()),
							HexDump.toHexString((short) device.getProductId()));
					String subtitle = driver1.getClass().getSimpleName();
					String org = tv_usb.getText().toString();
					tv_usb.setText(org+"\n" + title+" " + subtitle + "\n");
				}
				Log.d(TAG, "Done refreshing, " + mEntries.size() + " entries found.");
			}
		}.execute((Void) null);
	}
	
	// usb 打印初始化
	public void posPrintln(UsbSerialPort port, byte[] println) {

		try {

			UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
			// 权限
			if (!usbManager.hasPermission(port.getDriver().getDevice())) {
				usbManager.requestPermission(port.getDriver().getDevice(), localPendingIntent);
			}
			if (connection == null)
				connection = usbManager.openDevice(port.getDriver().getDevice());
			if (interf == null)
				interf = port.getDriver().getDevice().getInterface(0);
			int k = interf.getEndpointCount();
			for (int m = 0; m < k; m++) {

				UsbEndpoint localUsbEndpoint = interf.getEndpoint(m);
				System.out.println("localUsbEndpoint.getDirection()" + localUsbEndpoint.getDirection());
				if (localUsbEndpoint.getDirection() == 0) {
					end_out = localUsbEndpoint;
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
		}

		if (end_out != null) {
			try {
				connection.claimInterface(interf, true);
			} catch (NullPointerException e) {
				e.printStackTrace();
				mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
			}
			int falg;
			if (println != null && println.length > 0) {
				falg = connection.bulkTransfer(end_out, println, println.length, 0);
				if (falg < 0) {
					System.out.println("打印失败========");
				}
			}
		}
	}
	
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_REFRESH:
				refreshDeviceList();
				checkUsbDeviceInfo();
				if (mEntries.size() < 1)
					mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};

	// UsbManager.getDeviceList()
	public void checkUsbDeviceInfo() {
		HashMap<String, UsbDevice> usbMap = mUsbManager.getDeviceList();
		
		Iterator<UsbDevice> it = usbMap.values().iterator(); 
		System.out.println("---checkUsbDeviceInfo---");
		while (it.hasNext()){
			UsbDevice device = it.next();
			if(device.getDeviceClass() == 0 && device.getVendorId() != 2362){
				System.out.println(device.toString());
				String content = "kgd.zhen@gmail.com";
				posPrintln2(device,content.getBytes());
			}
		}
		System.out.println("---checkUsbDeviceInfo-----end--");
		UsbAccessory[] usbA = mUsbManager.getAccessoryList();
		System.out.println("checkUsbDeviceInfo");
	}
	
	public void posPrintln2(UsbDevice device, byte[] println) {

		try {

			UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
			// 权限
			if (!usbManager.hasPermission(device)) {
				usbManager.requestPermission(device, localPendingIntent);
			}
			if (connection == null)
				connection = usbManager.openDevice(device);
			if (interf == null)
				interf = device.getInterface(0);
			int k = interf.getEndpointCount();
			for (int m = 0; m < k; m++) {

				UsbEndpoint localUsbEndpoint = interf.getEndpoint(m);
				System.out.println("localUsbEndpoint.getDirection()" + localUsbEndpoint.getDirection());
				if (localUsbEndpoint.getDirection() == 0) {
					end_out = localUsbEndpoint;
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
		}

		if (end_out != null) {
			try {
				connection.claimInterface(interf, true);
			} catch (NullPointerException e) {
				e.printStackTrace();
				mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
			}
			int falg;
			if (println != null && println.length > 0) {
				falg = connection.bulkTransfer(end_out, println, println.length, 0);
				if (falg < 0) {
					System.out.println("打印失败========");
				}
			}
		}
	}
}
