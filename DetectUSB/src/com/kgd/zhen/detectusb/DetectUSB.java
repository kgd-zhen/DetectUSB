package com.kgd.zhen.detectusb;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class DetectUSB extends BroadcastReceiver {
	private static final String TAG = "DetectUSB"; 
	 
	private static final String PLUG_HOST_IN = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
	private static final String PLUG_HOST_OUT = "android.hardware.usb.action.USB_DEVICE_DETACHED";
	
	private static final String PLUG_ACCESSORY_IN = "android.hardware.usb.action.USB_ACCESSORY_ATTACHED";
	private static final String PLUG_ACCESSORY_OUT = "android.hardware.usb.action.USB_ACCESSORY_DETACHED";
	
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		this.mContext = context;
		if (intent.getAction().equalsIgnoreCase(PLUG_HOST_IN)) {
			 System.out.println("android.hardware.usb.action.USB_DEVICE_ATTACHED");
			 checkUsbDeviceInfo();
		}
		 
		if (intent.getAction().equalsIgnoreCase(PLUG_HOST_OUT)) {
			TextView textView = new TextView(context); 
			textView.setBackgroundColor(Color.MAGENTA); 
			textView.setTextColor(Color.BLUE); 
			textView.setPadding(10,10,10,10); 
			textView.setText("USB Disconnected."); 
			Toast toastView = new Toast(context); 
			toastView.setDuration(Toast.LENGTH_LONG); 
			toastView.setGravity(Gravity.CENTER, 0,0); 
			toastView.setView(textView); 
			toastView.show(); 
		}
		
		if (intent.getAction().equalsIgnoreCase(PLUG_ACCESSORY_IN)) {
			 System.out.println("PLUG_ACCESSORY_IN");
		}
		
		if (intent.getAction().equalsIgnoreCase(PLUG_ACCESSORY_OUT)) {
			 System.out.println("PLUG_ACCESSORY_OUT");
		}
	}

	/**
	 * 查看USB设备硬件相关信息
	 */
	public void checkUsbDeviceInfo() {
		try {
				//获得外接USB输入设备的信息
				//Process p=Runtime.getRuntime().exec("cat /proc/bus/input/devices");
			
				Process p=Runtime.getRuntime().exec("cat /proc/bus/usb/devices");
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = null;
				System.out.println("============checkUsbDeviceInfo==================");
				while((line = in.readLine())!= null){
					String deviceInfo = line.trim();
					//对获取的每行的设备信息进行过滤，获得自己想要的。
					System.out.println(deviceInfo);
				}
				System.out.println("============checkUsbDeviceInfo====END==============");
			} catch (Exception e) {
				//TODO: handle exception
				e.printStackTrace();
		    }
	}
	
	  //获得挂载的USB设备的存储空间使用情况
	public static String checkUSBStorage(Context context){
	      // USB Storage 

	      //storage/udisk为USB设备在Android设备上的挂载路径.不同厂商的Android设备路径不同。

	      //这样写同样适合于SD卡挂载。
	      File path = new File("/storage/udisk");

	      StatFs stat = new StatFs(path.getPath());
	      long blockSize = stat.getBlockSize();
	      long totalBlocks = stat.getBlockCount();
	      long availableBlocks = stat.getAvailableBlocks();
	      String usedSize = Formatter.formatFileSize(context, (totalBlocks-availableBlocks) * blockSize);
	      String availableSize = Formatter.formatFileSize(context, availableBlocks * blockSize);
	      return usedSize + " / " + availableSize;//空间:已使用/可用的
	 }
}
