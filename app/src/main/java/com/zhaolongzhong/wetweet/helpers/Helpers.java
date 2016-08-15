package com.zhaolongzhong.wetweet.helpers;

import android.util.Log;

import java.io.IOException;

/**
 * Created by Zhaolong Zhong on 8/13/16.
 */

public class Helpers {
    private static final String TAG = Helpers.class.getSimpleName();

    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();

        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          {
            Log.e(TAG, "Error checking internet.", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "Error checking internet.", e);
        }
        return false;
    }
}
