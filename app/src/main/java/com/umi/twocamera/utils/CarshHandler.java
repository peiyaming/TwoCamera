package com.umi.twocamera.utils;

import android.content.Context;
import android.os.Environment;

import com.umi.twocamera.app.Const;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;

/**
* @author 郑州优米科技有限公司
* @date 2016-10-21
* @version V1.0  
*/
public class CarshHandler implements UncaughtExceptionHandler {
	private static CarshHandler mCrashHandler;
	private UncaughtExceptionHandler mDefaultHandler;
	Context context;
	public CarshHandler() {

	}

	public static CarshHandler getIntance() {
		if (mCrashHandler == null) {
			mCrashHandler = new CarshHandler();
		}
		return mCrashHandler;

	}

	public void init(Context ctx) {
		this.context=ctx;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			/*Intent intent = new Intent(context.getApplicationContext(), CameraActivity.class);  
            PendingIntent restartIntent = PendingIntent.getActivity(    
            		context.getApplicationContext(), 0, intent,    
                    Intent.FLAG_ACTIVITY_NEW_TASK);                                                 
            //退出程序                                          
            AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);    
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,    
                    restartIntent); // 1秒钟后重启应用   
			android.os.Process.killProcess(android.os.Process.myPid());*/
		}
	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		// 保存日志文件
		saveCrashInfo2File(ex);
		 /*new Thread(){    
	            @Override    
	            public void run() {    
	                Looper.prepare();    
	                Toast.makeText(context.getApplicationContext(), "很抱歉,程序出现异常,即将退出.",   
	                        Toast.LENGTH_SHORT).show();    
	                Looper.loop();    
	            }   
	        }.start();  */  
		return true;
	}

	private String saveCrashInfo2File(Throwable ex) {
		try {

			StringBuffer sb = new StringBuffer();
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			ex.printStackTrace(printWriter);
			Throwable cause = ex.getCause();
			while (cause != null) {
				cause.printStackTrace(printWriter);
				cause = cause.getCause();
			}
			printWriter.close();
			String result = writer.toString();
			sb.append(result);
			String path = Const.filePath + "logs/" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(System.currentTimeMillis()) + ".txt";
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File dir = new File(path);
				if (!dir.getParentFile().exists()) {
					dir.getParentFile().mkdirs();
				}

				FileOutputStream fos = new FileOutputStream(path);
				fos.write(sb.toString().getBytes());
				fos.close();
			}
			return path;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
