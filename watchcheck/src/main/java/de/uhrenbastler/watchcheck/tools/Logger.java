package de.uhrenbastler.watchcheck.tools;

import android.util.Log;

/**
 * Created by clorenz on 14.02.14.
 */
public class Logger {

    public static final String TAG="WatchCheck";

    public static void debug(String message) {
        Log.d(TAG, message);
    }

    public static void info(String message) {
        Log.i(TAG, message);
    }

    public static void warn(String message) {
        Log.w(TAG, message);
    }

    public static void error(String message, Throwable t) {
        Log.e(TAG, message, t);
    }

    public static void error(String message) {
        Log.e(TAG, message);
    }

}
