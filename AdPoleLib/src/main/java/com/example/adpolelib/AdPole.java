package com.example.adpolelib;

import static com.example.adpolelib.AdPoleLog.TAG;
import static com.example.adpolelib.AdPolePrefs.IS_LOADED;
import static com.example.adpolelib.AdPolePrefs.PREFS_ADPOLE;
import static com.example.adpolelib.AdPolePrefs.PREFS_SUBSCRIBE_TOKEN_REPORT;
import static com.example.adpolelib.AdPolePrefs.PREF_APP_ID;
import static com.example.adpolelib.RestClientMethods.sendSubscriptionRequest;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.example.adpolelib.Interfaces.SubscribeUserListener;

import org.json.JSONException;
import org.json.JSONObject;

public class AdPole {
    static Context appContext;
    private static OsUtils osUtils;
    private static int subscribeAbleStatus;
    public static String appId;
    private static SubscribeUserListener listener;

    private static InAppRestClient.InAppResponseHandler responseHandler = new InAppRestClient.InAppResponseHandler() {
        @Override
        public void onSuccess(String response, InAppConstants.RequestType requestType) {
            Log.i(TAG, "FUNCTION : onSuccess");
            AdPolePrefs.saveBool(PREFS_ADPOLE, PREFS_SUBSCRIBE_TOKEN_REPORT, true);
            AdPoleLog.log(response);
            if (listener!=null)
                listener.success();
        }

        @Override
        public void onFailure(int statusCode, String response, Throwable throwable, InAppConstants.RequestType requestType) {
            Log.e(TAG, "FUNCTION : onFailure");
            AdPolePrefs.saveBool(PREFS_ADPOLE, PREFS_SUBSCRIBE_TOKEN_REPORT, false);
            AdPoleLog.log(response);
            AdPoleLog.catchException(this.getClass().getName(), "error occurred when registering to server.", statusCode, response, throwable);
            AdPoleLog.log("errors during have registrations via api");
            if(throwable != null){
                Log.e(TAG, "FUNCTION : onFailure => Error: " + throwable.toString());
                throwable.printStackTrace();
            } else {
                Log.e(TAG, "FUNCTION : onFailure => Throwable is null");
            }
            if (listener!=null)
                listener.failure();
        }
    };

    public static void initialize(Context context, String adPoleAppId){
        //AdPolePrefs.saveBool(PREFS_ADPOLE, IS_LOADED, false);
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
        String query = "?package_name=" + appContext.getPackageName() + "&app_id=" + appId;
        InAppRestClient.getApplicationRegister(query, responseHandler);
    }
    protected static void setContext (Context context){
        appContext = context;
    }

    public static boolean getAdPoleSubRepo(){
        return AdPolePrefs.getBool(PREFS_ADPOLE, PREFS_SUBSCRIBE_TOKEN_REPORT, false);
    }
    public static void setSubscribeAbleStatusListener(SubscribeUserListener listener2) {
        listener = listener2;
    }

}
