package com.kgd.zhen.usbutil;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.EncodingUtils;


import android.content.Context;
import android.content.res.AssetManager;

public class PrintTools {

	public static final String PRINT_FONT_GENERAL = "27,33,0,28,33,0";// 普通字体

	public static final String PRINT_FONT_DOUBLE_WIDTH = "27,33,32,28,33,4";// 倍宽字体

	public static final String PRINT_FONT_DOUBLE_HIGHER = "27,33,16,28,33,8";// 倍高字体

	public static final String PRINT_FONT_DOUBLE_WIDTHANDHIGHER = "27,33,48,28,33,12";// 倍高倍宽
	
	public static final String PRINT_FONT_BOLD = "27,33,8,28,33,12";// 加粗
	
	public static final String PRINT_FONT_BOLD_DOUBLE_WIDTH = "27,33,40,28,33,12";// 加粗,倍宽
	
	public static final String PRINT_FONT_BOLD_DOUBLE_HIGH = "27,33,24,28,33,12";// 加粗,倍高
	
	public static final String PRINT_FONT_BOLD_DOUBLE_HIGH_WIDTH = "27,33,56,28,33,12";// 加粗,倍高倍宽

	public static final String PRINT_OPEN_BOX = "27,112,0,100,100";// 开钱箱 27,112,0,128,128

	public static final String PRINT_PAPER_CUTTING = "29,86,66,10,13";// 切刀

	public static final String ENCODING = "UTF-8";
	AssetManager assetManager;

	public PrintTools(Context context) {
		if (context != null) {
			assetManager = context.getAssets();
		}
	}

	public static String kongge(int num) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < num; i++) {
			builder.append(" ");
		}
		return builder.toString();
	}

	public String getTextFromAssets(String fileName) {
		String result = "";

		try {
			InputStream in = assetManager.open(fileName);
			int length = in.available();
			byte[] buffer = new byte[length];
			in.read(buffer);
			result = EncodingUtils.getString(buffer, ENCODING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static int getLength(String s) {
		int valueLength = 0;
		String chinese = "[\u4e00-\u9fa5[\u2103]]"; //添加对(℃  \u2103)的支持
		// 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
		for (int i = 0; i < s.length(); i++) {
			// 获取一个字符
			String temp = s.substring(i, i + 1);
			// 判断是否为中文字符
			if (temp.matches(chinese)) {
				// 中文字符长度为1
				valueLength += 2;
			} else {
				// 其他字符长度为0.5
				valueLength += 1;
			}
		}
		// 进位取整
		return valueLength;
	}

	public static String ascii2String(String ASCIIs) {
		String[] chararry = ASCIIs.split(",");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chararry.length; i++) {
			sb.append((char) Integer.parseInt(chararry[i]));
		}
		return sb.toString();
	}

}
