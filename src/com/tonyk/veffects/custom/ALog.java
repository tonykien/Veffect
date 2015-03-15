package com.tonyk.veffects.custom;

import android.util.Log;

import com.tonyk.veffects.config.DebugOptions;

/**
 * Using this ALog instead of class Log - To disable all log: set value of
 * Define.ENABLE_LOG to FALSE
 */
public class ALog {

	public static void d(String tag, String msg) {
		if (DebugOptions.ENABLE_LOG) {
			Log.d(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (DebugOptions.ENABLE_LOG) {
			Log.e(tag, msg);
		}
	}

	public static void e(String tag, String msg, Exception e) {
		if (DebugOptions.ENABLE_LOG) {
			Log.e(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (DebugOptions.ENABLE_LOG) {
			Log.i(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (DebugOptions.ENABLE_LOG) {
			Log.v(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (DebugOptions.ENABLE_LOG) {
			Log.w(tag, msg);
		}
	}

	public static void println(int priority, String tag, String msg) {
		if (DebugOptions.ENABLE_LOG) {
			Log.println(priority, tag, msg);
		}
	}
	
	public static void printToFile(String strLog) {
		
		
		// File logFile = new File("sdcard/" + SplashActivity.logFile);
		//
		// if (!logFile.exists())
		// {
		// try
		// {
		// logFile.createNewFile();
		// }
		// catch (IOException e)
		// {
		// ALog.e("TAG", "ERROR = " + e.getMessage());
		// }
		// }
		// try
		// {
		// BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
		// true));
		// buf.append(strLog);
		// buf.newLine();
		// buf.flush();
		// buf.close();
		// }
		// catch (IOException e)
		// {
		// ALog.e("TAG", "ERROR = " + e.getMessage());
		// }
	}
}
