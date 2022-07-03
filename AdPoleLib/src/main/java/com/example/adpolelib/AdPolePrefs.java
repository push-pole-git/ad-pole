package com.example.adpolelib;

import static android.content.Context.MODE_PRIVATE;
import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.HashMap;

public class AdPolePrefs {
    public static final String PREFS_ADPOLE = AdPole.class.getSimpleName();
    // PREFERENCES KEYS
    static final String PREF_SUBSCRIPTION_ID = "PREF_SUBSCRIPTION_ID";
    static final String PREF_TOKEN_ID = "PREF_TOKEN_ID";
    static final String PREF_APP_ID = "PREF_APP_ID";
    static final String PREFS_OS_LAST_SESSION_TIME = "LAST_SESSION_TIME";
    static final String PREFS_GT_VIBRATE_ENABLED = "VIBRATE_ENABLED";
    static final String PREFS_SUBSCRIBE_TOKEN_REPORT = "SUBSCRIBE_TOKEN_REPORT";
    static final String IS_LOADED="false";
    static HashMap<String, HashMap<String, Object>> prefsToApply;
    public static WritePrefHandlerThread prefsHandler;
    static {
        initializePool();
    }

    public static void saveString(Context context, String isLoad){
        SharedPreferences.Editor editor = context.getSharedPreferences("CHECK_LOAD", MODE_PRIVATE).edit();
        editor.putString("isLoad", isLoad);
        editor.apply();
    }
    public static String getString(Context context){
        SharedPreferences prefs = context.getSharedPreferences("CHECK_LOAD", MODE_PRIVATE);
        String isLoad = prefs.getString("isLoad", "no");
        return isLoad;
    }
    public static void saveString(final String prefsName, final String key, final String value) {
        save(prefsName, key, value);
    }
    static private void save(String prefsName, String key, Object value) {
        HashMap<String, Object> pref = prefsToApply.get(prefsName);
        synchronized (pref) {
            pref.put(key, value);
        }
        startDelayedWrite();
    }
    public static void initializePool() {
        prefsToApply = new HashMap<>();
        prefsToApply.put(PREFS_ADPOLE, new HashMap<String, Object>());

        prefsHandler = new WritePrefHandlerThread();
    }

    public static void startDelayedWrite() {
        prefsHandler.startDelayedWrite();
    }


    public static void saveBool(String prefsName, String key, boolean value) {
        save(prefsName, key, value);
    }

    public static void saveInt(String prefsName, String key, int value) {
        save(prefsName, key, value);
    }

    public static void saveLong(String prefsName, String key, long value) {
        save(prefsName, key, value);
    }

    static String getString(String prefsName, String key, String defValue) {
        return (String) get(prefsName, key, String.class, defValue);
    }

    static boolean getBool(String prefsName, String key, boolean defValue) {
        return (Boolean) get(prefsName, key, Boolean.class, defValue);
    }

    static int getInt(String prefsName, String key, int defValue) {
        return (Integer) get(prefsName, key, Integer.class, defValue);
    }

    static long getLong(String prefsName, String key, long defValue) {
        return (Long) get(prefsName, key, Long.class, defValue);
    }

    // If type == Object then this is a contains check
    private static Object get(String prefsName, String key, Class type, Object defValue) {
        HashMap<String, Object> pref = prefsToApply.get(prefsName);

        synchronized (pref) {
            if (type.equals(Object.class) && pref.containsKey(key))
                return true;

            Object cachedValue = pref.get(key);
            if (cachedValue != null || pref.containsKey(key))
                return cachedValue;
        }

        SharedPreferences prefs = getSharedPrefsByName(prefsName);
        if (prefs != null) {
            if (type.equals(String.class))
                return prefs.getString(key, (String) defValue);
            else if (type.equals(Boolean.class))
                return prefs.getBoolean(key, (Boolean) defValue);
            else if (type.equals(Integer.class))
                return prefs.getInt(key, (Integer) defValue);
            else if (type.equals(Long.class))
                return prefs.getLong(key, (Long) defValue);
            else if (type.equals(Object.class))
                return prefs.contains(key);

            return null;
        }

        return defValue;
    }

    public static synchronized SharedPreferences getSharedPrefsByName(String prefsName) {
        if (AdPole.appContext == null) {
            String msg = "AdPole.appContext null, could not read " + prefsName + " from getSharedPreferences.";
            AdPoleLog.log(msg, new Throwable());
            return null;
        }

        return AdPole.appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }
    public static class WritePrefHandlerThread extends HandlerThread {
        public Handler mHandler;

        private static final int WRITE_CALL_DELAY_TO_BUFFER_MS = 200;
        private long lastSyncTime = 0L;

        WritePrefHandlerThread() {
            super("OSH_WritePrefs");
            start();
            mHandler = new Handler(getLooper());
        }

        void startDelayedWrite() {
            synchronized (mHandler) {
                mHandler.removeCallbacksAndMessages(null);
                if (lastSyncTime == 0)
                    lastSyncTime = System.currentTimeMillis();

                long delay = lastSyncTime - System.currentTimeMillis() + WRITE_CALL_DELAY_TO_BUFFER_MS;

                mHandler.postDelayed(getNewRunnable(), delay);
            }
        }

        private Runnable getNewRunnable() {
            return new Runnable() {
                @Override
                public void run() {
                    flushBufferToDisk();
                }
            };
        }

        private void flushBufferToDisk() {
            // A flush will be triggered later once a context is set via OneSignal.setAppContext(...)
            if (AdPole.appContext == null)
                return;

            for (String pref : prefsToApply.keySet()) {
                SharedPreferences prefsToWrite = getSharedPrefsByName(pref);
                SharedPreferences.Editor editor = prefsToWrite.edit();
                HashMap<String, Object> prefHash = prefsToApply.get(pref);
                synchronized (prefHash) {
                    for (String key : prefHash.keySet()) {
                        Object value = prefHash.get(key);
                        if (value instanceof String)
                            editor.putString(key, (String) value);
                        else if (value instanceof Boolean)
                            editor.putBoolean(key, (Boolean) value);
                        else if (value instanceof Integer)
                            editor.putInt(key, (Integer) value);
                        else if (value instanceof Long)
                            editor.putLong(key, (Long) value);
                    }
                    prefHash.clear();
                }
                editor.apply();
            }

            lastSyncTime = System.currentTimeMillis();
        }
    }
}
