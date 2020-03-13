
package com.umi.twocamera.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author dllik 2013-11-23
 */
public class DeviceUtils {
    /**
     *
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
	public static String getWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth() + "";
    }

    /**
     *
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
	public static String getHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getHeight() + "";
    }

    /**
     *
     * @return
     */
    public static String getBrand() {
        return android.os.Build.BRAND;
    }

    /**
     *
     * @return
     */
    public static String getModel() {
        return android.os.Build.MODEL;
    }

    /**
     * IMEI
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
	public static String getImei(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     *
     * @param context
     * @return
     */
    public static String getMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }


    /**
     * @return
     */
    public static String getOsVersion() {
        return "android" + android.os.Build.VERSION.SDK_INT;
    }

    /**
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
	public static String getImsi(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return  tm.getSubscriberId();
    }
    
    
    

	public static int getScreenWidth(Context context) {
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		return display.getWidth();
	}

	public static int getScreenHeight(Context context) {
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		return display.getHeight();
	}
	/**
	 * 手机号验证
	 * 
	 * @param  str
	 * @return 验证通过返回true
	 */
	public static boolean isMobile(String str) { 
		Pattern p = null;
		Matcher m = null;
		boolean b = false; 
		p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
		m = p.matcher(str);
		b = m.matches(); 
		return b;
	}
	/**
	 * 电话号码验证
	 * 
	 * @param  str
	 * @return 验证通过返回true
	 */
	public static boolean isPhone(String str) { 
		Pattern p1 = null,p2 = null;
		Matcher m = null;
		boolean b = false;  
		p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$");  // 验证带区号的
		p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");         // 验证没有区号的
		if(str.length() >9)
		{	m = p1.matcher(str);
 		    b = m.matches();  
		}else{
			m = p2.matcher(str);
 			b = m.matches(); 
		}  
		return b;
	}
}
