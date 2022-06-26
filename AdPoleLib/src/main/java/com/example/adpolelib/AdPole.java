package com.example.adpolelib;

import static com.example.adpolelib.AdPolePrefs.PREFS_ADPOLE;
import static com.example.adpolelib.AdPolePrefs.PREF_APP_ID;
import static com.example.adpolelib.RestClientMethods.sendSubscriptionRequest;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

public class AdPole {
    static Context appContext;
    private static OsUtils osUtils;
    private static int subscribeAbleStatus;
    private static String appId;

    public static void initialize(Context context, String adPoleAppId){
        appContext = context;
        osUtils = new OsUtils();
        subscribeAbleStatus = osUtils.initializationChecker(context, adPoleAppId);
        if (subscribeAbleStatus == OsUtils.UNINITIALIZEABLE_STATUS)
            return;
        boolean contextIsActivity = (context instanceof Activity);

        appId = adPoleAppId;
        AdPolePrefs.saveString(PREFS_ADPOLE, PREF_APP_ID, appId);

        if (contextIsActivity) {
            ActivityLifecycleHandler.curActivity = (Activity) context;
        } else
            ActivityLifecycleHandler.nextResumeIsFirstActivity = true;

        registerUser();

    }


    private static void registerUser() {

        new Thread(new Runnable() {
            public void run() {
                try {
                    registerUserTask();
                } catch (JSONException t) {
                }
            }
        }, "OS_REG_USER").start();
    }

    private static void registerUserTask() throws JSONException {

        JSONObject body = new JSONObject();
        body.put("AppId", appId);
        body.put("DeviceId", DeviceInfo.id(appContext));
        body.put("DeviceBrand", Build.BRAND);
        body.put("DeviceModel", Build.MODEL);
        body.put("HostApplicationVersionName", getHostAppVersionName());
        sendSubscriptionRequest(body);
    }

    private static String getHostAppVersionName(){
        try {
            PackageManager packageManager = appContext.getPackageManager();
            String packageName = appContext.getPackageName();
            PackageInfo pInfo = packageManager.getPackageInfo(packageName, 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    protected static void setContext (Context context){
        appContext = context;
    }

}
